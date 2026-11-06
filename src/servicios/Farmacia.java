package servicios;

import dominio.Cliente;
import dominio.DetalleVenta;
import dominio.Lote;
import dominio.Producto;
import dominio.Venta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Comparator;

/**
 * Coordina catalogo, clientes y ventas de una farmacia monousuario.
 * Guarda ArrayList en memoria y no lee ni imprime datos de consola.
 */
public final class Farmacia {

    private final ArrayList<Producto> productos = new ArrayList<>();
    private final ArrayList<Cliente> clientes = new ArrayList<>();
    private final ArrayList<Venta> historial = new ArrayList<>();
    private Venta ventaEnCurso;
    private Venta resumenPreparado;
    private int proximoId = 1;

    // Productos y lotes
    public ArrayList<Producto> getProductos() {
        return new ArrayList<>(productos);
    }

    public ArrayList<Producto> listarProductos() {
        return getProductos();
    }

    public void registrarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        for (Producto existente : productos) {
            if (existente.getCodigo().equalsIgnoreCase(producto.getCodigo())) {
                throw new IllegalArgumentException("El codigo de producto ya esta registrado.");
            }
        }
        productos.add(producto);
    }

    /** Busca por codigo, ignorando mayusculas y espacios en los extremos. */
    public Producto buscarProducto(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigo es obligatorio.");
        }
        for (Producto producto : productos) {
            if (producto.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return producto;
            }
        }
        throw new IllegalArgumentException("No existe el producto con codigo " + codigo.trim() + ".");
    }

    public void actualizarProducto(String codigo, String nombre, BigDecimal precio) {
        buscarProducto(codigo).actualizarDatos(nombre, precio);
    }

    public void registrarLote(String codigo, String numero, int cantidad, LocalDate caducidad) {
        Producto producto = buscarProducto(codigo);
        producto.agregarLote(new Lote(producto, numero, cantidad, caducidad));
    }

    public int consultarStockDisponible(String codigo, LocalDate fecha) {
        return buscarProducto(codigo).calcularStockDisponible(fecha);
    }

    /** FEFO: primero salen los lotes utilizables que caducan antes. No descuenta. */
    public ArrayList<SimpleImmutableEntry<Lote, Integer>> planificarSalida(String codigo, int cantidad,
                                                    LocalDate fecha) {
        validarFecha(fecha);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser un entero positivo.");
        }
        Producto producto = buscarProducto(codigo);
        if (cantidad > producto.calcularStockDisponible(fecha)) {
            throw new IllegalStateException("Stock disponible insuficiente para " + producto.getNombre() + ".");
        }
        ArrayList<Lote> lotesOrdenados = producto.getLotes();
        lotesOrdenados.sort(Comparator.comparing(Lote::getFechaCaducidad)
                .thenComparing(Lote::getNumero));

        ArrayList<SimpleImmutableEntry<Lote, Integer>> asignaciones = new ArrayList<>();
        int pendiente = cantidad;
        for (Lote lote : lotesOrdenados) {
            if (pendiente == 0) {
                break;
            }
            if (lote.estaDisponible(fecha)) {
                int tomar = Math.min(pendiente, lote.getCantidadDisponible());
                asignaciones.add(new SimpleImmutableEntry<>(lote, tomar));
                pendiente -= tomar;
            }
        }
        return asignaciones;
    }

    /** Valida todas las asignaciones sin modificar existencias. */
    public void validarSalida(ArrayList<SimpleImmutableEntry<Lote, Integer>> asignaciones, LocalDate fecha) {
        prepararSalidaValidada(asignaciones, fecha);
    }

    /**
     * Se invoca al confirmar la operacion. Primero valida toda la salida y
     * acumula las referencias repetidas al mismo lote; despues descuenta.
     * En esta aplicacion monousuario, un rechazo no deja descuentos parciales.
     */
    public void aplicarSalida(ArrayList<SimpleImmutableEntry<Lote, Integer>> asignaciones, LocalDate fecha) {
        ArrayList<SimpleImmutableEntry<Lote, Integer>> salida = prepararSalidaValidada(asignaciones, fecha);
        for (SimpleImmutableEntry<Lote, Integer> asignacion : salida) {
            asignacion.getKey().descontar(asignacion.getValue(), fecha);
        }
    }

    private ArrayList<SimpleImmutableEntry<Lote, Integer>> prepararSalidaValidada(
            ArrayList<SimpleImmutableEntry<Lote, Integer>> asignaciones, LocalDate fecha) {
        validarFecha(fecha);
        if (asignaciones == null || asignaciones.isEmpty()) {
            throw new IllegalArgumentException("La salida debe contener al menos una asignacion.");
        }
        ArrayList<SimpleImmutableEntry<Lote, Integer>> acumuladas = new ArrayList<>();
        for (SimpleImmutableEntry<Lote, Integer> asignacion : new ArrayList<>(asignaciones)) {
            if (asignacion == null || asignacion.getKey() == null
                    || asignacion.getValue() == null || asignacion.getValue() <= 0) {
                throw new IllegalArgumentException("La asignacion requiere lote y cantidad positiva.");
            }
            Lote lote = asignacion.getKey();
            verificarLoteRegistrado(lote);
            int posicion = -1;
            for (int i = 0; i < acumuladas.size(); i++) {
                if (acumuladas.get(i).getKey() == lote) {
                    posicion = i;
                    break;
                }
            }
            if (posicion == -1) {
                acumuladas.add(asignacion);
            } else {
                long total = (long) acumuladas.get(posicion).getValue()
                        + asignacion.getValue();
                if (total > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("La cantidad acumulada supera el limite entero.");
                }
                acumuladas.set(posicion, new SimpleImmutableEntry<>(lote, (int) total));
            }
        }
        for (SimpleImmutableEntry<Lote, Integer> asignacion : acumuladas) {
            asignacion.getKey().validarSalida(asignacion.getValue(), fecha);
        }
        return acumuladas;
    }

    private void verificarLoteRegistrado(Lote lote) {
        Producto registrado = buscarProducto(lote.getProducto().getCodigo());
        if (registrado != lote.getProducto()) {
            throw new IllegalArgumentException("La instancia del producto no pertenece a este inventario.");
        }
        for (Lote existente : registrado.getLotes()) {
            if (existente == lote) {
                return;
            }
        }
        throw new IllegalArgumentException("El lote no esta registrado en este inventario.");
    }

    private static void validarFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de operacion es obligatoria.");
        }
    }

    // Clientes
    public ArrayList<Cliente> getClientes() {
        return new ArrayList<>(clientes);
    }

    public ArrayList<Cliente> listarClientes() {
        return getClientes();
    }

    public void registrarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        for (Cliente existente : clientes) {
            if (existente.getIdentificacion().equalsIgnoreCase(cliente.getIdentificacion())) {
                throw new IllegalArgumentException("La identificacion del cliente ya esta registrada.");
            }
        }
        clientes.add(cliente);
    }

    public Cliente buscarCliente(String identificacion) {
        if (identificacion == null || identificacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La identificacion es obligatoria.");
        }
        for (Cliente cliente : clientes) {
            if (cliente.getIdentificacion().equalsIgnoreCase(identificacion.trim())) {
                return cliente;
            }
        }
        throw new IllegalArgumentException("No existe el cliente con identificacion "
                + identificacion.trim() + ".");
    }

    public void actualizarCliente(String identificacion, String nombre) {
        buscarCliente(identificacion).setNombre(nombre);
    }

    // Venta en preparacion, revision y confirmacion
    public boolean hayVentaEnCurso() { return ventaEnCurso != null; }
    public boolean isResumenPreparado() { return resumenPreparado != null; }

    public ArrayList<Venta> getHistorial() {
        return new ArrayList<>(historial);
    }

    public ArrayList<Venta> listarVentas() {
        return getHistorial();
    }

    public boolean requiereReceta(String codigo) {
        exigirVentaEnCurso();
        return buscarProducto(codigo).requiereReceta();
    }

    public void iniciarVenta(String identificacionCliente) {
        if (hayVentaEnCurso()) {
            throw new IllegalStateException("Ya existe una venta en preparacion.");
        }
        Cliente cliente = buscarCliente(identificacionCliente);
        if (proximoId <= 0) {
            throw new IllegalStateException("Se agotaron los identificadores disponibles.");
        }
        ventaEnCurso = new Venta(proximoId, cliente);
        proximoId = proximoId == Integer.MAX_VALUE ? 0 : proximoId + 1;
        invalidarResumen();
    }

    public void agregarProducto(String codigo, int cantidad) {
        agregarProducto(codigo, cantidad, null, null, null, false);
    }

    public void agregarProducto(String codigo, int cantidad, String numeroReceta,
            String paciente, LocalDate emision, boolean aceptada) {
        Venta borrador = exigirVentaEnCurso();
        borrador.agregarProducto(buscarProducto(codigo), cantidad,
                numeroReceta, paciente, emision, aceptada);
        invalidarResumen();
    }

    public void asignarReceta(String codigo, String numero, String paciente,
                               LocalDate emision, boolean aceptada) {
        exigirVentaEnCurso().asignarReceta(codigo, numero, paciente, emision, aceptada);
        invalidarResumen();
    }

    /** Devuelve datos; no formatea texto, imprime, reserva ni descuenta stock. */
    public Venta prepararResumen(LocalDate fecha) {
        Venta borrador = exigirVentaEnCurso();
        invalidarResumen();
        ArrayList<DetalleVenta> detalles = planificarDetalles(fecha);
        resumenPreparado = borrador.crearResumen(fecha, detalles);
        return resumenPreparado;
    }

    /** Solo acepta el objeto resumen vigente que se entrego para revision. */
    public Venta confirmarVenta(Venta revisado, LocalDateTime fechaHora) {
        exigirVentaEnCurso();
        try {
            validarResumenVigente(revisado, fechaHora);
            if (!revisado.coincideCon(planificarDetalles(fechaHora.toLocalDate()))) {
                throw new IllegalStateException("Cambio la distribucion por lotes; revise otro resumen.");
            }
            // El comprobante se valida y construye antes de modificar el stock.
            Venta confirmada = revisado.crearComprobante(fechaHora);
            historial.ensureCapacity(historial.size() + 1);
            aplicarSalida(revisado.getAsignaciones(), fechaHora.toLocalDate());
            historial.add(confirmada);
            ventaEnCurso = null;
            invalidarResumen();
            return confirmada;
        } catch (RuntimeException error) {
            invalidarResumen();
            throw error;
        }
    }

    public void cancelarVenta() {
        exigirVentaEnCurso().cancelar();
        ventaEnCurso = null;
        invalidarResumen();
    }

    private ArrayList<DetalleVenta> planificarDetalles(LocalDate fecha) {
        Venta borrador = exigirVentaEnCurso();
        borrador.validarParaResumen(fecha);
        ArrayList<DetalleVenta> asignados = new ArrayList<>();
        for (DetalleVenta detalle : borrador.getDetalles()) {
            Producto registrado = buscarProducto(detalle.getCodigoProductoHistorico());
            if (registrado != detalle.getProducto()) {
                throw new IllegalStateException("El producto no pertenece al inventario compartido.");
            }
            asignados.add(detalle.conAsignaciones(planificarSalida(
                    registrado.getCodigo(), detalle.getCantidad(), fecha)));
        }
        return asignados;
    }

    private void validarResumenVigente(Venta revisado, LocalDateTime fechaHora) {
        if (revisado == null || revisado != resumenPreparado) {
            throw new IllegalStateException("Debe preparar y revisar el resumen vigente antes de confirmar.");
        }
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora son obligatorias.");
        }
        if (revisado.getId() != ventaEnCurso.getId()
                || !revisado.getFecha().equals(fechaHora.toLocalDate())) {
            throw new IllegalStateException("Cambio la venta o fecha; prepare y revise otro resumen.");
        }
    }

    private Venta exigirVentaEnCurso() {
        if (ventaEnCurso == null) {
            throw new IllegalStateException("No existe una venta en preparacion.");
        }
        return ventaEnCurso;
    }

    private void invalidarResumen() {
        resumenPreparado = null;
    }
}

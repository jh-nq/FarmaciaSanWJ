package servicios;

import dominio.AsignacionLote;
import dominio.Lote;
import dominio.Producto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

/** Operaciones de inventario para una aplicacion monousuario en memoria. */
public final class GestorInventario {

    private final ArrayList<Producto> productos;

    public GestorInventario() {
        productos = new ArrayList<>();
    }

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
    public ArrayList<AsignacionLote> planificarSalida(String codigo, int cantidad,
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

        ArrayList<AsignacionLote> asignaciones = new ArrayList<>();
        int pendiente = cantidad;
        for (Lote lote : lotesOrdenados) {
            if (pendiente == 0) {
                break;
            }
            if (lote.estaDisponible(fecha)) {
                int tomar = Math.min(pendiente, lote.getCantidadDisponible());
                asignaciones.add(new AsignacionLote(lote, tomar));
                pendiente -= tomar;
            }
        }
        return asignaciones;
    }

    /** Valida todas las asignaciones sin modificar existencias. */
    public void validarSalida(ArrayList<AsignacionLote> asignaciones, LocalDate fecha) {
        prepararSalidaValidada(asignaciones, fecha);
    }

    /**
     * Se invoca al confirmar la operacion. Primero valida toda la salida y
     * acumula las referencias repetidas al mismo lote; despues descuenta.
     * En esta aplicacion monousuario, un rechazo no deja descuentos parciales.
     */
    public void aplicarSalida(ArrayList<AsignacionLote> asignaciones, LocalDate fecha) {
        ArrayList<AsignacionLote> salida = prepararSalidaValidada(asignaciones, fecha);
        for (AsignacionLote asignacion : salida) {
            asignacion.getLote().descontar(asignacion.getCantidad(), fecha);
        }
    }

    private ArrayList<AsignacionLote> prepararSalidaValidada(
            ArrayList<AsignacionLote> asignaciones, LocalDate fecha) {
        validarFecha(fecha);
        if (asignaciones == null || asignaciones.isEmpty()) {
            throw new IllegalArgumentException("La salida debe contener al menos una asignacion.");
        }
        ArrayList<AsignacionLote> acumuladas = new ArrayList<>();
        for (AsignacionLote asignacion : new ArrayList<>(asignaciones)) {
            if (asignacion == null) {
                throw new IllegalArgumentException("Una asignacion no puede ser nula.");
            }
            Lote lote = asignacion.getLote();
            verificarLoteRegistrado(lote);
            int posicion = -1;
            for (int i = 0; i < acumuladas.size(); i++) {
                if (acumuladas.get(i).getLote() == lote) {
                    posicion = i;
                    break;
                }
            }
            if (posicion == -1) {
                acumuladas.add(asignacion);
            } else {
                long total = (long) acumuladas.get(posicion).getCantidad()
                        + asignacion.getCantidad();
                if (total > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("La cantidad acumulada supera el limite entero.");
                }
                acumuladas.set(posicion, new AsignacionLote(lote, (int) total));
            }
        }
        for (AsignacionLote asignacion : acumuladas) {
            asignacion.getLote().validarSalida(asignacion.getCantidad(), fecha);
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
}

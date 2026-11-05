package servicios;

import dominio.BorradorVenta;
import dominio.Cliente;
import dominio.DetalleVenta;
import dominio.Producto;
import dominio.Receta;
import dominio.ResumenVenta;
import dominio.Venta;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/** Unico punto de la aplicacion para registrar una venta y aplicar su salida. */
public final class GestorVentas {

    private final GestorInventario inventario;
    private final GestorClientes clientes;
    private final ArrayList<Venta> historial;
    private BorradorVenta ventaEnCurso;
    private ResumenVenta resumenPreparado;
    private int proximoId;

    public GestorVentas(GestorInventario inventario, GestorClientes clientes) {
        if (inventario == null || clientes == null) {
            throw new IllegalArgumentException("Inventario y gestor de clientes son obligatorios.");
        }
        this.inventario = inventario;
        this.clientes = clientes;
        this.historial = new ArrayList<>();
        this.proximoId = 1;
    }

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
        return inventario.buscarProducto(codigo).requiereReceta();
    }

    public void iniciarVenta(String identificacionCliente) {
        if (hayVentaEnCurso()) {
            throw new IllegalStateException("Ya existe una venta en preparacion.");
        }
        Cliente cliente = clientes.buscarCliente(identificacionCliente);
        if (proximoId <= 0) {
            throw new IllegalStateException("Se agotaron los identificadores disponibles.");
        }
        ventaEnCurso = new BorradorVenta(proximoId, cliente);
        proximoId = proximoId == Integer.MAX_VALUE ? 0 : proximoId + 1;
        invalidarResumen();
    }

    public void agregarProducto(String codigo, int cantidad, Receta receta) {
        BorradorVenta borrador = exigirVentaEnCurso();
        borrador.agregarProducto(inventario.buscarProducto(codigo), cantidad, receta);
        invalidarResumen();
    }

    public void asignarReceta(String codigo, Receta receta) {
        exigirVentaEnCurso().asignarReceta(codigo, receta);
        invalidarResumen();
    }

    /** Devuelve datos; no formatea texto, imprime, reserva ni descuenta stock. */
    public ResumenVenta prepararResumen(LocalDate fecha) {
        BorradorVenta borrador = exigirVentaEnCurso();
        invalidarResumen();
        ArrayList<DetalleVenta> detalles = planificarDetalles(fecha);
        resumenPreparado = new ResumenVenta(borrador.getId(), fecha,
                borrador.getIdentificacionCliente(), borrador.getNombreCliente(), detalles);
        return resumenPreparado;
    }

    /** Solo acepta el objeto resumen vigente que se entrego para revision. */
    public Venta confirmarVenta(ResumenVenta revisado, LocalDateTime fechaHora) {
        exigirVentaEnCurso();
        try {
            validarResumenVigente(revisado, fechaHora);
            if (!revisado.coincideCon(planificarDetalles(fechaHora.toLocalDate()))) {
                throw new IllegalStateException("Cambio la distribucion por lotes; revise otro resumen.");
            }
            // El comprobante se valida y construye antes de modificar el stock.
            Venta confirmada = new Venta(revisado, fechaHora);
            historial.ensureCapacity(historial.size() + 1);
            inventario.aplicarSalida(revisado.getAsignaciones(), fechaHora.toLocalDate());
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
        BorradorVenta borrador = exigirVentaEnCurso();
        borrador.validarParaResumen(fecha);
        ArrayList<DetalleVenta> asignados = new ArrayList<>();
        for (DetalleVenta detalle : borrador.getDetalles()) {
            Producto registrado = inventario.buscarProducto(detalle.getCodigoProductoHistorico());
            if (registrado != detalle.getProducto()) {
                throw new IllegalStateException("El producto no pertenece al inventario compartido.");
            }
            asignados.add(detalle.conAsignaciones(inventario.planificarSalida(
                    registrado.getCodigo(), detalle.getCantidad(), fecha)));
        }
        return asignados;
    }

    private void validarResumenVigente(ResumenVenta revisado, LocalDateTime fechaHora) {
        if (revisado == null || revisado != resumenPreparado) {
            throw new IllegalStateException("Debe preparar y revisar el resumen vigente antes de confirmar.");
        }
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora son obligatorias.");
        }
        if (revisado.getIdVenta() != ventaEnCurso.getId()
                || !revisado.getFecha().equals(fechaHora.toLocalDate())) {
            throw new IllegalStateException("Cambio la venta o fecha; prepare y revise otro resumen.");
        }
    }

    private BorradorVenta exigirVentaEnCurso() {
        if (ventaEnCurso == null) {
            throw new IllegalStateException("No existe una venta en preparacion.");
        }
        return ventaEnCurso;
    }

    private void invalidarResumen() {
        resumenPreparado = null;
    }
}

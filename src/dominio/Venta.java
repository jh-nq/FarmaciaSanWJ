package dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleImmutableEntry;

/**
 * Representa un borrador editable, un resumen o un comprobante.
 * Cada resumen y comprobante es una instancia nueva que ya no admite ediciones.
 */
public final class Venta {

    private static final String BORRADOR = "BORRADOR";
    private static final String RESUMEN = "RESUMEN";
    private static final String CONFIRMADA = "CONFIRMADA";
    private static final String CANCELADA = "CANCELADA";

    private final int id;
    private final Cliente cliente;
    private final String identificacionCliente;
    private final String nombreCliente;
    private final ArrayList<DetalleVenta> detalles;
    private final LocalDate fecha;
    private final LocalDateTime fechaHora;
    private final BigDecimal total;
    private String estado;

    public Venta(int id, Cliente cliente) {
        if (id <= 0 || cliente == null) {
            throw new IllegalArgumentException("Se requiere identificador positivo y cliente.");
        }
        this.id = id;
        this.cliente = cliente;
        this.identificacionCliente = cliente.getIdentificacion();
        this.nombreCliente = cliente.getNombre();
        this.detalles = new ArrayList<>();
        this.fecha = null;
        this.fechaHora = null;
        this.total = null;
        this.estado = BORRADOR;
    }

    private Venta(Venta origen, String estado, LocalDate fecha,
                  LocalDateTime fechaHora, ArrayList<DetalleVenta> planificados) {
        this.id = origen.id;
        this.cliente = origen.cliente;
        this.identificacionCliente = origen.identificacionCliente;
        this.nombreCliente = origen.nombreCliente;
        this.detalles = copiarDetallesPlanificados(planificados);
        this.fecha = fecha;
        this.fechaHora = fechaHora;
        this.total = sumarSubtotales();
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getIdentificacionCliente() { return identificacionCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public String getEstado() { return estado; }
    public LocalDate getFecha() { return fecha; }
    public LocalDateTime getFechaHora() { return fechaHora; }

    public BigDecimal getTotal() {
        if (total == null) {
            throw new IllegalStateException("Prepare un resumen para consultar el total.");
        }
        return total;
    }

    public ArrayList<DetalleVenta> getDetalles() {
        return new ArrayList<>(detalles);
    }

    /** El borrador permanece editable; la cotizacion devuelta queda protegida. */
    public Venta crearResumen(LocalDate fecha, ArrayList<DetalleVenta> planificados) {
        validarParaResumen(fecha);
        return new Venta(this, RESUMEN, fecha, null, planificados);
    }

    /** Construye el documento antes de que Farmacia descuente las existencias. */
    public Venta crearComprobante(LocalDateTime fechaHora) {
        if (!RESUMEN.equals(estado) || fechaHora == null
                || !fecha.equals(fechaHora.toLocalDate())) {
            throw new IllegalArgumentException("La venta requiere un resumen de la misma fecha.");
        }
        return new Venta(this, CONFIRMADA, fecha, fechaHora, detalles);
    }

    public ArrayList<SimpleImmutableEntry<Lote, Integer>> getAsignaciones() {
        ArrayList<SimpleImmutableEntry<Lote, Integer>> salida = new ArrayList<>();
        for (DetalleVenta detalle : detalles) {
            salida.addAll(detalle.getAsignaciones());
        }
        return salida;
    }

    public boolean coincideCon(ArrayList<DetalleVenta> actuales) {
        if (actuales == null || actuales.size() != detalles.size()) {
            return false;
        }
        for (int i = 0; i < detalles.size(); i++) {
            if (!detalles.get(i).mismasAsignacionesQue(actuales.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static ArrayList<DetalleVenta> copiarDetallesPlanificados(
            ArrayList<DetalleVenta> origen) {
        if (origen == null || origen.isEmpty()) {
            throw new IllegalArgumentException("La venta necesita detalles con lotes asignados.");
        }
        ArrayList<DetalleVenta> copia = new ArrayList<>();
        for (DetalleVenta detalle : origen) {
            if (detalle == null || !detalle.tieneAsignaciones()) {
                throw new IllegalArgumentException("Cada detalle necesita sus lotes asignados.");
            }
            for (DetalleVenta anterior : copia) {
                if (anterior.getCodigoProductoHistorico().equalsIgnoreCase(
                        detalle.getCodigoProductoHistorico())) {
                    throw new IllegalArgumentException("El resumen no admite productos duplicados.");
                }
            }
            copia.add(detalle);
        }
        return copia;
    }

    private BigDecimal sumarSubtotales() {
        BigDecimal suma = new BigDecimal("0.00");
        for (DetalleVenta detalle : detalles) {
            suma = suma.add(detalle.calcularSubtotal());
        }
        return suma;
    }

    public void agregarProducto(Producto producto, int cantidad) {
        agregarProducto(producto, cantidad, null, null, null, false);
    }

    public void agregarProducto(Producto producto, int cantidad, String numeroReceta,
            String paciente, LocalDate emision, boolean aceptada) {
        exigirEditable();
        DetalleVenta nuevo = new DetalleVenta(producto, cantidad);
        if (numeroReceta != null) {
            nuevo = nuevo.conReceta(numeroReceta, paciente, emision, aceptada);
        }
        for (int i = 0; i < detalles.size(); i++) {
            DetalleVenta existente = detalles.get(i);
            if (existente.getCodigoProductoHistorico().equalsIgnoreCase(producto.getCodigo())) {
                if (existente.getProducto() != producto) {
                    throw new IllegalArgumentException("Instancia de producto incompatible.");
                }
                DetalleVenta actualizado = existente.conUnidadesAgregadas(cantidad);
                if (numeroReceta != null) {
                    actualizado = actualizado.conReceta(numeroReceta, paciente, emision, aceptada);
                }
                detalles.set(i, actualizado);
                return;
            }
        }
        detalles.add(nuevo);
    }

    public void asignarReceta(String codigo, String numero, String paciente,
                               LocalDate emision, boolean aceptada) {
        exigirEditable();
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigo de producto es obligatorio.");
        }
        for (int i = 0; i < detalles.size(); i++) {
            DetalleVenta detalle = detalles.get(i);
            if (detalle.getCodigoProductoHistorico().equalsIgnoreCase(codigo.trim())) {
                detalles.set(i, detalle.conReceta(numero, paciente, emision, aceptada));
                return;
            }
        }
        throw new IllegalArgumentException("El producto no esta agregado a la venta.");
    }

    public void validarParaResumen(LocalDate fecha) {
        exigirEditable();
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de operacion es obligatoria.");
        }
        if (detalles.isEmpty()) {
            throw new IllegalStateException("La venta debe contener al menos un producto.");
        }
        for (DetalleVenta detalle : detalles) {
            if (!detalle.validarReceta(cliente, fecha)) {
                throw new IllegalStateException("Receta ausente o invalida para "
                        + detalle.getCodigoProductoHistorico() + ".");
            }
        }
    }

    public void cancelar() {
        exigirEditable();
        estado = CANCELADA;
    }

    private void exigirEditable() {
        if (!BORRADOR.equals(estado)) {
            throw new IllegalStateException("Solo se puede modificar un borrador activo.");
        }
    }
}

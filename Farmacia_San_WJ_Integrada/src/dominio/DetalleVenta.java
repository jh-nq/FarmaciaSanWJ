package dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

/** Valor inmutable: modificar una linea produce otra sin alterar el historial. */
public final class DetalleVenta {

    private final Producto producto;
    private final String codigoProductoHistorico;
    private final String nombreProductoHistorico;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final Receta receta;
    private final ArrayList<AsignacionLote> asignaciones;

    public DetalleVenta(Producto producto, int cantidad, Receta receta) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        validarCantidad(cantidad);
        this.producto = producto;
        this.codigoProductoHistorico = producto.getCodigo();
        this.nombreProductoHistorico = producto.getNombre();
        this.precioUnitario = producto.getPrecioVenta();
        this.cantidad = cantidad;
        this.receta = receta;
        this.asignaciones = new ArrayList<>();
    }

    private DetalleVenta(DetalleVenta original, int cantidad, Receta receta,
                         ArrayList<AsignacionLote> asignaciones) {
        validarCantidad(cantidad);
        this.producto = original.producto;
        this.codigoProductoHistorico = original.codigoProductoHistorico;
        this.nombreProductoHistorico = original.nombreProductoHistorico;
        this.precioUnitario = original.precioUnitario;
        this.cantidad = cantidad;
        this.receta = receta;
        this.asignaciones = new ArrayList<>(asignaciones);
        if (!this.asignaciones.isEmpty()) {
            validarAsignaciones();
        }
    }

    public Producto getProducto() { return producto; }
    public String getCodigoProductoHistorico() { return codigoProductoHistorico; }
    public String getNombreProductoHistorico() { return nombreProductoHistorico; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public Receta getReceta() { return receta; }

    public ArrayList<AsignacionLote> getAsignaciones() {
        return new ArrayList<>(asignaciones);
    }

    public DetalleVenta conUnidadesAgregadas(int unidades) {
        validarCantidad(unidades);
        long total = (long) cantidad + unidades;
        if (total > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("La cantidad acumulada supera el limite entero.");
        }
        return new DetalleVenta(this, (int) total, receta, new ArrayList<>());
    }

    public DetalleVenta conReceta(Receta nuevaReceta) {
        return new DetalleVenta(this, cantidad, nuevaReceta, new ArrayList<>());
    }

    public DetalleVenta conAsignaciones(ArrayList<AsignacionLote> nuevas) {
        if (nuevas == null || nuevas.isEmpty()) {
            throw new IllegalArgumentException("El detalle necesita asignaciones de lotes.");
        }
        return new DetalleVenta(this, cantidad, receta, nuevas);
    }

    public boolean tieneAsignaciones() {
        return !asignaciones.isEmpty();
    }

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public boolean validarReceta(Cliente cliente, LocalDate fecha) {
        if (cliente == null || fecha == null) {
            throw new IllegalArgumentException("Cliente y fecha son obligatorios.");
        }
        return !producto.requiereReceta()
                || (receta != null && receta.esValidaPara(cliente, producto, fecha));
    }

    public boolean mismasAsignacionesQue(DetalleVenta otro) {
        if (otro == null || producto != otro.producto || cantidad != otro.cantidad
                || precioUnitario.compareTo(otro.precioUnitario) != 0
                || asignaciones.size() != otro.asignaciones.size()) {
            return false;
        }
        for (int i = 0; i < asignaciones.size(); i++) {
            AsignacionLote anterior = asignaciones.get(i);
            AsignacionLote actual = otro.asignaciones.get(i);
            if (anterior.getLote() != actual.getLote()
                    || anterior.getCantidad() != actual.getCantidad()) {
                return false;
            }
        }
        return true;
    }

    private void validarAsignaciones() {
        long total = 0;
        for (AsignacionLote asignacion : asignaciones) {
            if (asignacion == null || asignacion.getLote().getProducto() != producto) {
                throw new IllegalArgumentException("Asignacion nula o de otro producto.");
            }
            total += asignacion.getCantidad();
        }
        if (total != cantidad) {
            throw new IllegalArgumentException("Las asignaciones deben cubrir exactamente la cantidad.");
        }
    }

    private static void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser un entero positivo.");
        }
    }
}

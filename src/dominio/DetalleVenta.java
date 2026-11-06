package dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;

/** Linea inmutable de una venta, con su receta y pares de lote y cantidad. */
public final class DetalleVenta {
    private final Producto producto;
    private final String codigoProductoHistorico;
    private final String nombreProductoHistorico;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final String numeroReceta;
    private final String identificacionPaciente;
    private final LocalDate fechaEmision;
    private final boolean recetaAceptada;
    private final ArrayList<SimpleImmutableEntry<Lote, Integer>> asignaciones;

    public DetalleVenta(Producto producto, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        validarCantidad(cantidad);
        this.producto = producto;
        this.codigoProductoHistorico = producto.getCodigo();
        this.nombreProductoHistorico = producto.getNombre();
        this.precioUnitario = producto.getPrecioVenta();
        this.cantidad = cantidad;
        this.numeroReceta = null;
        this.identificacionPaciente = null;
        this.fechaEmision = null;
        this.recetaAceptada = false;
        this.asignaciones = new ArrayList<>();
    }

    private DetalleVenta(DetalleVenta original, int cantidad, String numeroReceta,
            String identificacionPaciente, LocalDate fechaEmision, boolean recetaAceptada,
            ArrayList<SimpleImmutableEntry<Lote, Integer>> asignaciones) {
        validarCantidad(cantidad);
        this.producto = original.producto;
        this.codigoProductoHistorico = original.codigoProductoHistorico;
        this.nombreProductoHistorico = original.nombreProductoHistorico;
        this.precioUnitario = original.precioUnitario;
        this.cantidad = cantidad;
        this.numeroReceta = numeroReceta;
        this.identificacionPaciente = identificacionPaciente;
        this.fechaEmision = fechaEmision;
        this.recetaAceptada = recetaAceptada;
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
    public String getNumeroReceta() { return numeroReceta; }
    public String getIdentificacionPaciente() { return identificacionPaciente; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public boolean isRecetaAceptada() { return recetaAceptada; }

    public ArrayList<SimpleImmutableEntry<Lote, Integer>> getAsignaciones() {
        return new ArrayList<>(asignaciones);
    }

    public DetalleVenta conUnidadesAgregadas(int unidades) {
        validarCantidad(unidades);
        long total = (long) cantidad + unidades;
        if (total > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("La cantidad acumulada supera el limite entero.");
        }
        return new DetalleVenta(this, (int) total, numeroReceta, identificacionPaciente,
                fechaEmision, recetaAceptada, new ArrayList<>());
    }

    public DetalleVenta conReceta(String numero, String paciente,
                                  LocalDate emision, boolean aceptada) {
        String numeroValidado = validarTexto(numero, "El numero de receta");
        String pacienteValidado = validarTexto(paciente, "La identificacion del cliente");
        if (emision == null) {
            throw new IllegalArgumentException("La fecha de emision es obligatoria.");
        }
        return new DetalleVenta(this, cantidad, numeroValidado, pacienteValidado,
                emision, aceptada, new ArrayList<>());
    }

    public DetalleVenta conAsignaciones(ArrayList<SimpleImmutableEntry<Lote, Integer>> nuevas) {
        if (nuevas == null || nuevas.isEmpty()) {
            throw new IllegalArgumentException("El detalle necesita asignaciones de lotes.");
        }
        return new DetalleVenta(this, cantidad, numeroReceta, identificacionPaciente,
                fechaEmision, recetaAceptada, nuevas);
    }

    public boolean tieneAsignaciones() { return !asignaciones.isEmpty(); }

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public boolean validarReceta(Cliente cliente, LocalDate fecha) {
        if (cliente == null || fecha == null) {
            throw new IllegalArgumentException("Cliente y fecha son obligatorios.");
        }
        return !producto.requiereReceta()
                || (numeroReceta != null && recetaAceptada
                    && identificacionPaciente.equalsIgnoreCase(cliente.getIdentificacion())
                    && !fechaEmision.isAfter(fecha));
    }

    public boolean mismasAsignacionesQue(DetalleVenta otro) {
        if (otro == null || producto != otro.producto || cantidad != otro.cantidad
                || precioUnitario.compareTo(otro.precioUnitario) != 0
                || asignaciones.size() != otro.asignaciones.size()) {
            return false;
        }
        for (int i = 0; i < asignaciones.size(); i++) {
            SimpleImmutableEntry<Lote, Integer> anterior = asignaciones.get(i);
            SimpleImmutableEntry<Lote, Integer> actual = otro.asignaciones.get(i);
            if (anterior.getKey() != actual.getKey()
                    || !anterior.getValue().equals(actual.getValue())) {
                return false;
            }
        }
        return true;
    }

    private void validarAsignaciones() {
        long total = 0;
        for (SimpleImmutableEntry<Lote, Integer> asignacion : asignaciones) {
            if (asignacion == null || asignacion.getKey() == null
                    || asignacion.getValue() == null || asignacion.getValue() <= 0
                    || asignacion.getKey().getProducto() != producto) {
                throw new IllegalArgumentException("Asignacion nula, invalida o de otro producto.");
            }
            total += asignacion.getValue();
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

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.trim();
    }
}

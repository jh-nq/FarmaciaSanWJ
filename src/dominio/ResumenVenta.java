package dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

/** Cotizacion inmutable con los importes y lotes exactos que se revisan. */
public final class ResumenVenta {

    private final int idVenta;
    private final LocalDate fecha;
    private final String identificacionCliente;
    private final String nombreCliente;
    private final ArrayList<DetalleVenta> detalles;
    private final BigDecimal total;

    public ResumenVenta(int idVenta, LocalDate fecha, String identificacionCliente,
                        String nombreCliente, ArrayList<DetalleVenta> detalles) {
        if (idVenta <= 0 || fecha == null || identificacionCliente == null
                || identificacionCliente.trim().isEmpty() || nombreCliente == null
                || nombreCliente.trim().isEmpty() || detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("El resumen requiere venta, fecha, cliente y detalles.");
        }
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.identificacionCliente = identificacionCliente;
        this.nombreCliente = nombreCliente;
        this.detalles = copiarDetallesValidos(detalles);
        this.total = sumarSubtotales();
    }

    public int getIdVenta() { return idVenta; }
    public LocalDate getFecha() { return fecha; }
    public String getIdentificacionCliente() { return identificacionCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public BigDecimal getTotal() { return total; }

    public ArrayList<DetalleVenta> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public ArrayList<AsignacionLote> getAsignaciones() {
        ArrayList<AsignacionLote> salida = new ArrayList<>();
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

    private ArrayList<DetalleVenta> copiarDetallesValidos(ArrayList<DetalleVenta> origen) {
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
}

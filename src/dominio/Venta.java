package dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

/** Comprobante inmutable; su registro y descuento solo los coordina GestorVentas. */
public final class Venta {

    private final int id;
    private final LocalDateTime fechaHora;
    private final String identificacionClienteHistorica;
    private final String nombreClienteHistorico;
    private final ArrayList<DetalleVenta> detalles;
    private final BigDecimal total;

    public Venta(ResumenVenta resumen, LocalDateTime fechaHora) {
        if (resumen == null || fechaHora == null
                || !resumen.getFecha().equals(fechaHora.toLocalDate())) {
            throw new IllegalArgumentException("La venta requiere un resumen de la misma fecha.");
        }
        this.id = resumen.getIdVenta();
        this.fechaHora = fechaHora;
        this.identificacionClienteHistorica = resumen.getIdentificacionCliente();
        this.nombreClienteHistorico = resumen.getNombreCliente();
        this.detalles = resumen.getDetalles();
        this.total = resumen.getTotal();
    }

    public int getId() { return id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getIdentificacionClienteHistorica() { return identificacionClienteHistorica; }
    public String getNombreClienteHistorico() { return nombreClienteHistorico; }
    public EstadoVenta getEstado() { return EstadoVenta.CONFIRMADA; }
    public BigDecimal getTotal() { return total; }

    public ArrayList<DetalleVenta> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public BigDecimal calcularTotal() {
        return total;
    }
}

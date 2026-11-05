package consola;

import dominio.AsignacionLote;
import dominio.DetalleVenta;
import dominio.ResumenVenta;
import dominio.Venta;
import java.util.ArrayList;

/** Presentacion de datos de venta; no valida ni modifica el negocio. */
public final class FormateadorVentas {

    public String formatearResumen(ResumenVenta resumen) {
        StringBuilder texto = new StringBuilder();
        texto.append("PRE-RESUMEN DE VENTA ").append(resumen.getIdVenta())
                .append(" | Fecha: ").append(resumen.getFecha()).append('\n')
                .append("Cliente: ").append(resumen.getIdentificacionCliente())
                .append(" - ").append(resumen.getNombreCliente()).append('\n');
        agregarDetalles(texto, resumen.getDetalles());
        return texto.append("TOTAL: ").append(resumen.getTotal().toPlainString()).toString();
    }

    public String formatearVenta(Venta venta) {
        StringBuilder texto = new StringBuilder();
        texto.append("Venta ").append(venta.getId()).append(" | ").append(venta.getFechaHora())
                .append(" | Cliente: ").append(venta.getIdentificacionClienteHistorica())
                .append(" - ").append(venta.getNombreClienteHistorico())
                .append(" | Total: ").append(venta.getTotal().toPlainString()).append('\n');
        agregarDetalles(texto, venta.getDetalles());
        return texto.toString();
    }

    public String formatearHistorial(ArrayList<Venta> historial) {
        if (historial.isEmpty()) {
            return "No hay ventas confirmadas.";
        }
        StringBuilder texto = new StringBuilder();
        for (Venta venta : historial) {
            texto.append(formatearVenta(venta));
        }
        return texto.toString();
    }

    private void agregarDetalles(StringBuilder texto, ArrayList<DetalleVenta> detalles) {
        for (DetalleVenta detalle : detalles) {
            texto.append(detalle.getCodigoProductoHistorico()).append(" - ")
                    .append(detalle.getNombreProductoHistorico())
                    .append(" | Cantidad: ").append(detalle.getCantidad())
                    .append(" | Precio: ").append(detalle.getPrecioUnitario().toPlainString())
                    .append(" | Subtotal: ").append(detalle.calcularSubtotal().toPlainString())
                    .append('\n');
            agregarLotes(texto, detalle.getAsignaciones());
        }
    }

    private void agregarLotes(StringBuilder texto, ArrayList<AsignacionLote> asignaciones) {
        for (AsignacionLote asignacion : asignaciones) {
            texto.append("  Lote ").append(asignacion.getLote().getNumero())
                    .append(" | Unidades: ").append(asignacion.getCantidad())
                    .append(" | Caduca: ").append(asignacion.getLote().getFechaCaducidad())
                    .append('\n');
        }
    }
}

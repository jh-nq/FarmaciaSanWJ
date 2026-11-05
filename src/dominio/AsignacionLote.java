package dominio;

import java.time.LocalDate;

/** Propuesta de salida: crearla no reserva ni descuenta existencias. */
public final class AsignacionLote {

    private final Lote lote;
    private final int cantidad;

    public AsignacionLote(Lote lote, int cantidad) {
        if (lote == null) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad asignada debe ser un entero positivo.");
        }
        this.lote = lote;
        this.cantidad = cantidad;
    }

    public Lote getLote() {
        return lote;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean esAplicable(LocalDate fecha) {
        return lote.estaDisponible(fecha) && cantidad <= lote.getCantidadDisponible();
    }
}

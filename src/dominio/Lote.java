package dominio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Stock de un lote. Su producto, numero y caducidad son inmutables. */
public final class Lote {

    private final Producto producto;
    private final String numero;
    private int cantidadDisponible;
    private final LocalDate fechaCaducidad;

    public Lote(Producto producto, String numero, int cantidadDisponible,
                LocalDate fechaCaducidad) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("El numero de lote es obligatorio.");
        }
        if (cantidadDisponible <= 0) {
            throw new IllegalArgumentException("La cantidad inicial debe ser un entero positivo.");
        }
        validarFecha(fechaCaducidad);
        this.producto = producto;
        this.numero = numero.trim();
        this.cantidadDisponible = cantidadDisponible;
        this.fechaCaducidad = fechaCaducidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public String getNumero() {
        return numero;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public LocalDate getFechaCaducidad() {
        return fechaCaducidad;
    }

    /** Criterio aprobado: el lote se puede utilizar durante su fecha de caducidad. */
    public boolean estaCaducado(LocalDate fecha) {
        validarFecha(fecha);
        return fechaCaducidad.isBefore(fecha);
    }

    public boolean estaDisponible(LocalDate fecha) {
        validarFecha(fecha);
        return cantidadDisponible > 0 && !estaCaducado(fecha);
    }

    public boolean estaPorCaducar(LocalDate fecha, int dias) {
        validarFecha(fecha);
        if (dias < 0) {
            throw new IllegalArgumentException("El plazo no puede ser negativo.");
        }
        return estaDisponible(fecha)
                && ChronoUnit.DAYS.between(fecha, fechaCaducidad) <= dias;
    }

    /** Comprueba la salida sin modificar el stock. */
    public void validarSalida(int cantidad, LocalDate fecha) {
        validarFecha(fecha);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de salida debe ser un entero positivo.");
        }
        if (estaCaducado(fecha)) {
            throw new IllegalStateException("El lote " + numero + " esta caducado.");
        }
        if (cantidad > cantidadDisponible) {
            throw new IllegalStateException("Stock insuficiente en el lote " + numero + ".");
        }
    }

    public void descontar(int cantidad) {
        descontar(cantidad, LocalDate.now());
    }

    public void descontar(int cantidad, LocalDate fecha) {
        validarSalida(cantidad, fecha);
        cantidadDisponible -= cantidad;
    }

    private static void validarFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
    }

    @Override
    public String toString() {
        return producto.getCodigo() + " / lote " + numero
                + " / stock: " + cantidadDisponible + " / caduca: " + fechaCaducidad;
    }
}

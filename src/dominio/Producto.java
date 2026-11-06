package dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;

/** Producto de cuidado personal y base comun de los medicamentos. */
public class Producto {

    private final String codigo;
    private String nombre;
    private BigDecimal precioVenta;
    private final ArrayList<Lote> lotes;

    public Producto(String codigo, String nombre, BigDecimal precioVenta) {
        this.codigo = validarTexto(codigo, "El codigo");
        this.lotes = new ArrayList<>();
        actualizarDatos(nombre, precioVenta);
    }

    public final String getCodigo() {
        return codigo;
    }

    public final String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        this.nombre = validarTexto(nombre, "El nombre");
    }

    public final BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public final void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = validarPrecio(precioVenta);
    }

    /** Devuelve una copia de la coleccion; no permite reemplazarla. */
    public final ArrayList<Lote> getLotes() {
        return new ArrayList<>(lotes);
    }

    /** Valida ambos valores antes de modificar cualquiera de ellos. */
    public final void actualizarDatos(String nombre, BigDecimal precio) {
        String nombreValidado = validarTexto(nombre, "El nombre");
        BigDecimal precioValidado = validarPrecio(precio);
        this.nombre = nombreValidado;
        this.precioVenta = precioValidado;
    }

    public final void agregarLote(Lote lote) {
        if (lote == null) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }
        if (lote.getProducto() != this) {
            throw new IllegalArgumentException("El lote pertenece a otro producto.");
        }
        long total = lote.getCantidadDisponible();
        for (Lote existente : lotes) {
            if (existente.getNumero().equalsIgnoreCase(lote.getNumero())) {
                throw new IllegalArgumentException("El numero de lote ya existe para este producto.");
            }
            total += existente.getCantidadDisponible();
        }
        if (total > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("El stock total supera el limite de un entero.");
        }
        lotes.add(lote);
    }

    /** El stock disponible se calcula; no se almacena como un segundo saldo. */
    public final int calcularStockDisponible(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de consulta es obligatoria.");
        }
        int total = 0;
        for (Lote lote : lotes) {
            if (lote.estaDisponible(fecha)) {
                total = Math.addExact(total, lote.getCantidadDisponible());
            }
        }
        return total;
    }

    public boolean requiereReceta() { return false; }

    public String obtenerTipo() { return "Producto de cuidado personal"; }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.trim();
    }

    private static BigDecimal validarPrecio(BigDecimal precio) {
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        BigDecimal normalizado = precio.setScale(2, RoundingMode.HALF_UP);
        if (normalizado.signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser al menos 0.01.");
        }
        return normalizado;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " - " + obtenerTipo()
                + " - precio: " + precioVenta.toPlainString();
    }
}

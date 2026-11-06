package dominio;

import java.math.BigDecimal;

/** Medicamento de venta libre o con receta segun su configuracion. */
public final class Medicamento extends Producto {
    private final boolean conReceta;

    public Medicamento(String codigo, String nombre, BigDecimal precio, boolean conReceta) {
        super(codigo, nombre, precio);
        this.conReceta = conReceta;
    }

    @Override
    public boolean requiereReceta() {
        return conReceta;
    }

    @Override
    public String obtenerTipo() {
        return conReceta ? "Medicamento con receta" : "Medicamento de venta libre";
    }
}

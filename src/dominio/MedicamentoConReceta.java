package dominio;

import java.math.BigDecimal;

public class MedicamentoConReceta extends Producto {

    public MedicamentoConReceta(String codigo, String nombre, BigDecimal precioVenta) {
        super(codigo, nombre, precioVenta);
    }

    @Override
    public boolean requiereReceta() {
        return true;
    }

    @Override
    public String obtenerTipo() {
        return "Medicamento con receta";
    }
}

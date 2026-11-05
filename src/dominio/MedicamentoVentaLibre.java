package dominio;

import java.math.BigDecimal;

public class MedicamentoVentaLibre extends Producto {

    public MedicamentoVentaLibre(String codigo, String nombre, BigDecimal precioVenta) {
        super(codigo, nombre, precioVenta);
    }

    @Override
    public boolean requiereReceta() {
        return false;
    }

    @Override
    public String obtenerTipo() {
        return "Medicamento de venta libre";
    }
}

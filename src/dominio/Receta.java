package dominio;

import java.time.LocalDate;

/** Registro inmutable de la validacion basica de una receta por producto. */
public final class Receta {

    private final String numero;
    private final String identificacionCliente;
    private final String codigoProducto;
    private final LocalDate fechaEmision;
    private final boolean aceptada;

    public Receta(String numero, String identificacionCliente, String codigoProducto,
                  LocalDate fechaEmision, boolean aceptada) {
        this.numero = validarTexto(numero, "El numero de receta");
        this.identificacionCliente = validarTexto(identificacionCliente, "La identificacion del cliente");
        this.codigoProducto = validarTexto(codigoProducto, "El codigo del producto");
        if (fechaEmision == null) {
            throw new IllegalArgumentException("La fecha de emision es obligatoria.");
        }
        this.fechaEmision = fechaEmision;
        this.aceptada = aceptada;
    }

    public String getNumero() {
        return numero;
    }

    public String getIdentificacionCliente() {
        return identificacionCliente;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public boolean isAceptada() {
        return aceptada;
    }

    public boolean esValidaPara(Cliente cliente, Producto producto, LocalDate fecha) {
        if (cliente == null || producto == null || fecha == null) {
            throw new IllegalArgumentException("Cliente, producto y fecha son obligatorios.");
        }
        return aceptada
                && identificacionCliente.equalsIgnoreCase(cliente.getIdentificacion())
                && codigoProducto.equalsIgnoreCase(producto.getCodigo())
                && !fechaEmision.isAfter(fecha);
    }

    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return texto.trim();
    }
}

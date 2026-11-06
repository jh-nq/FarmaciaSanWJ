package dominio;

/** Identidad del comprador. La identificacion no cambia una vez creado. */
public final class Cliente {

    private final String identificacion;
    private String nombre;

    public Cliente(String identificacion, String nombre) {
        this.identificacion = validarTexto(identificacion, "La identificacion");
        setNombre(nombre);
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = validarTexto(nombre, "El nombre");
    }

    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return texto.trim();
    }

    @Override
    public String toString() {
        return identificacion + " - " + nombre;
    }
}

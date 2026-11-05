package dominio;

import java.time.LocalDate;
import java.util.ArrayList;

/** Contiene las ediciones de una venta que todavia no afecta el inventario. */
public final class BorradorVenta {

    private final int id;
    private final Cliente cliente;
    private final String identificacionCliente;
    private final String nombreCliente;
    private final ArrayList<DetalleVenta> detalles;
    private EstadoVenta estado;

    public BorradorVenta(int id, Cliente cliente) {
        if (id <= 0 || cliente == null) {
            throw new IllegalArgumentException("Se requiere identificador positivo y cliente.");
        }
        this.id = id;
        this.cliente = cliente;
        this.identificacionCliente = cliente.getIdentificacion();
        this.nombreCliente = cliente.getNombre();
        this.detalles = new ArrayList<>();
        this.estado = EstadoVenta.BORRADOR;
    }

    public int getId() { return id; }
    public String getIdentificacionCliente() { return identificacionCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public EstadoVenta getEstado() { return estado; }

    public ArrayList<DetalleVenta> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public void agregarProducto(Producto producto, int cantidad, Receta receta) {
        exigirEditable();
        DetalleVenta nuevo = new DetalleVenta(producto, cantidad, receta);
        for (int i = 0; i < detalles.size(); i++) {
            DetalleVenta existente = detalles.get(i);
            if (existente.getCodigoProductoHistorico().equalsIgnoreCase(producto.getCodigo())) {
                if (existente.getProducto() != producto) {
                    throw new IllegalArgumentException("Instancia de producto incompatible.");
                }
                DetalleVenta actualizado = existente.conUnidadesAgregadas(cantidad);
                if (receta != null) {
                    actualizado = actualizado.conReceta(receta);
                }
                detalles.set(i, actualizado);
                return;
            }
        }
        detalles.add(nuevo);
    }

    public void asignarReceta(String codigo, Receta receta) {
        exigirEditable();
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigo de producto es obligatorio.");
        }
        for (int i = 0; i < detalles.size(); i++) {
            DetalleVenta detalle = detalles.get(i);
            if (detalle.getCodigoProductoHistorico().equalsIgnoreCase(codigo.trim())) {
                detalles.set(i, detalle.conReceta(receta));
                return;
            }
        }
        throw new IllegalArgumentException("El producto no esta agregado a la venta.");
    }

    public void validarParaResumen(LocalDate fecha) {
        exigirEditable();
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de operacion es obligatoria.");
        }
        if (detalles.isEmpty()) {
            throw new IllegalStateException("La venta debe contener al menos un producto.");
        }
        for (DetalleVenta detalle : detalles) {
            if (!detalle.validarReceta(cliente, fecha)) {
                throw new IllegalStateException("Receta ausente o invalida para "
                        + detalle.getCodigoProductoHistorico() + ".");
            }
        }
    }

    public void cancelar() {
        exigirEditable();
        estado = EstadoVenta.CANCELADA;
    }

    private void exigirEditable() {
        if (estado != EstadoVenta.BORRADOR) {
            throw new IllegalStateException("Solo se puede modificar un borrador activo.");
        }
    }
}

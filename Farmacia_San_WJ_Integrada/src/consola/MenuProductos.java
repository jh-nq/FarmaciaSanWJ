
package consola;

import dominio.Lote;
import dominio.MedicamentoConReceta;
import dominio.MedicamentoVentaLibre;
import dominio.Producto;
import dominio.ProductoCuidadoPersonal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import servicios.GestorInventario;

/** Interaccion del modulo de productos; delega las operaciones al inventario. */
public final class MenuProductos {

    private final GestorInventario inventario;
    private final EntradaConsola entrada;

    public MenuProductos(GestorInventario inventario, EntradaConsola entrada) {
        if (inventario == null || entrada == null) {
            throw new IllegalArgumentException("Inventario y entrada son obligatorios.");
        }
        this.inventario = inventario;
        this.entrada = entrada;
    }

    public void mostrar() {
        while (true) {
            System.out.println("\n--- PRODUCTOS E INVENTARIO ---");
            System.out.println("1. Registrar producto");
            System.out.println("2. Listar productos y stock disponible");
            System.out.println("3. Actualizar nombre y precio");
            System.out.println("4. Registrar lote");
            System.out.println("5. Consultar lotes de un producto");
            System.out.println("0. Volver al menu principal");
            int opcion = entrada.leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1: registrarProducto(); break;
                    case 2: listarProductos(); break;
                    case 3: actualizarProducto(); break;
                    case 4: registrarLote(); break;
                    case 5: consultarLotes(); break;
                    case 0: return;
                    default: System.out.println("Opcion invalida. Intente nuevamente.");
                }
            } catch (IllegalArgumentException | IllegalStateException error) {
                System.out.println("Operacion rechazada: " + error.getMessage());
            }
        }
    }

    private void registrarProducto() {
        System.out.println("1. Medicamento de venta libre");
        System.out.println("2. Medicamento con receta");
        System.out.println("3. Producto de cuidado personal");
        int tipo = entrada.leerEntero("Tipo de producto: ");
        if (tipo < 1 || tipo > 3) {
            System.out.println("Tipo de producto invalido.");
            return;
        }
        String codigo = entrada.leerTexto("Codigo: ");
        String nombre = entrada.leerTexto("Nombre: ");
        BigDecimal precio = entrada.leerDecimal("Precio de venta: ");
        Producto producto;
        switch (tipo) {
            case 1: producto = new MedicamentoVentaLibre(codigo, nombre, precio); break;
            case 2: producto = new MedicamentoConReceta(codigo, nombre, precio); break;
            case 3: producto = new ProductoCuidadoPersonal(codigo, nombre, precio); break;
            default: throw new IllegalArgumentException("Tipo de producto invalido.");
        }
        inventario.registrarProducto(producto);
        System.out.println("Producto registrado.");
    }

    private void listarProductos() {
        ArrayList<Producto> productos = inventario.listarProductos();
        if (productos.isEmpty()) {
            System.out.println("No hay productos registrados.");
            return;
        }
        LocalDate hoy = LocalDate.now();
        for (Producto producto : productos) {
            System.out.println(producto + " | Stock disponible: "
                    + producto.calcularStockDisponible(hoy));
        }
    }

    private void actualizarProducto() {
        String codigo = entrada.leerTexto("Codigo del producto: ");
        String nombre = entrada.leerTexto("Nuevo nombre: ");
        BigDecimal precio = entrada.leerDecimal("Nuevo precio: ");
        inventario.actualizarProducto(codigo, nombre, precio);
        System.out.println("Producto actualizado.");
    }

    private void registrarLote() {
        String codigo = entrada.leerTexto("Codigo del producto: ");
        String numero = entrada.leerTexto("Numero de lote: ");
        int cantidad = entrada.leerEntero("Cantidad inicial: ");
        LocalDate caducidad = entrada.leerFecha("Caducidad (AAAA-MM-DD): ");
        inventario.registrarLote(codigo, numero, cantidad, caducidad);
        System.out.println("Lote registrado.");
    }

    private void consultarLotes() {
        Producto producto = inventario.buscarProducto(entrada.leerTexto("Codigo del producto: "));
        ArrayList<Lote> lotes = producto.getLotes();
        if (lotes.isEmpty()) {
            System.out.println("El producto no tiene lotes registrados.");
            return;
        }
        LocalDate hoy = LocalDate.now();
        for (Lote lote : lotes) {
            String estado = lote.estaDisponible(hoy) ? "DISPONIBLE" : "NO DISPONIBLE";
            System.out.println(lote + " | " + estado);
        }
        System.out.println("Stock disponible: " + producto.calcularStockDisponible(hoy));
    }
}

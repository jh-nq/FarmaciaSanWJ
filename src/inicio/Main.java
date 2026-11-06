package inicio;

import dominio.Cliente;
import dominio.DetalleVenta;
import dominio.Lote;
import dominio.Producto;
import dominio.Medicamento;
import dominio.Venta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import servicios.Farmacia;

/** Entrada, navegacion y presentacion. Las reglas se delegan a Farmacia. */
public final class Main {

    private final Farmacia farmacia;
    private final Scanner scanner;

    private Main(Scanner scanner) {
        this.farmacia = new Farmacia();
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new Main(scanner).mostrarPrincipal();
        } catch (NoSuchElementException error) {
            System.out.println("\nFin de entrada. Sistema finalizado.");
        }
    }

    private void mostrarPrincipal() {
        int opcion;
        do {
            System.out.println("\n=== FARMACIA SAN WJ ===");
            System.out.println("1. Productos e inventario");
            System.out.println("2. Clientes");
            System.out.println("3. Ventas y dispensacion");
            System.out.println("0. Salir del sistema");
            opcion = leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1: mostrarProductos(); break;
                case 2: mostrarClientes(); break;
                case 3: mostrarVentas(); break;
                case 0: System.out.println("Sistema finalizado."); break;
                default: System.out.println("Opcion invalida. Intente nuevamente.");
            }
        } while (opcion != 0);
    }

    // Menu de productos
    private void mostrarProductos() {
        while (true) {
            System.out.println("\n--- PRODUCTOS E INVENTARIO ---");
            System.out.println("1. Registrar producto");
            System.out.println("2. Listar productos y stock disponible");
            System.out.println("3. Actualizar nombre y precio");
            System.out.println("4. Registrar lote");
            System.out.println("5. Consultar lotes de un producto");
            System.out.println("0. Volver al menu principal");
            int opcion = leerEntero("Seleccione una opcion: ");
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
        int tipo = leerEntero("Tipo de producto: ");
        if (tipo < 1 || tipo > 3) {
            System.out.println("Tipo de producto invalido.");
            return;
        }
        String codigo = leerTexto("Codigo: ");
        String nombre = leerTexto("Nombre: ");
        BigDecimal precio = leerDecimal("Precio de venta: ");
        Producto producto;
        switch (tipo) {
            case 1: producto = new Medicamento(codigo, nombre, precio, false); break;
            case 2: producto = new Medicamento(codigo, nombre, precio, true); break;
            case 3: producto = new Producto(codigo, nombre, precio); break;
            default: throw new IllegalArgumentException("Tipo de producto invalido.");
        }
        farmacia.registrarProducto(producto);
        System.out.println("Producto registrado.");
    }

    private void listarProductos() {
        ArrayList<Producto> productos = farmacia.listarProductos();
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
        String codigo = leerTexto("Codigo del producto: ");
        String nombre = leerTexto("Nuevo nombre: ");
        BigDecimal precio = leerDecimal("Nuevo precio: ");
        farmacia.actualizarProducto(codigo, nombre, precio);
        System.out.println("Producto actualizado.");
    }

    private void registrarLote() {
        String codigo = leerTexto("Codigo del producto: ");
        String numero = leerTexto("Numero de lote: ");
        int cantidad = leerEntero("Cantidad inicial: ");
        LocalDate caducidad = leerFecha("Caducidad (AAAA-MM-DD): ");
        farmacia.registrarLote(codigo, numero, cantidad, caducidad);
        System.out.println("Lote registrado.");
    }

    private void consultarLotes() {
        Producto producto = farmacia.buscarProducto(leerTexto("Codigo del producto: "));
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

    // Menu de clientes
    private void mostrarClientes() {
        while (true) {
            System.out.println("\n--- CLIENTES ---");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Listar clientes");
            System.out.println("3. Buscar cliente");
            System.out.println("4. Actualizar nombre");
            System.out.println("0. Volver al menu principal");
            int opcion = leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1: registrarCliente(); break;
                    case 2: listarClientes(); break;
                    case 3:
                        System.out.println(farmacia.buscarCliente(
                                leerTexto("Identificacion: ")));
                        break;
                    case 4: actualizarCliente(); break;
                    case 0: return;
                    default: System.out.println("Opcion invalida. Intente nuevamente.");
                }
            } catch (IllegalArgumentException | IllegalStateException error) {
                System.out.println("Operacion rechazada: " + error.getMessage());
            }
        }
    }

    private void registrarCliente() {
        String identificacion = leerTexto("Identificacion: ");
        String nombre = leerTexto("Nombre: ");
        farmacia.registrarCliente(new Cliente(identificacion, nombre));
        System.out.println("Cliente registrado.");
    }

    private void listarClientes() {
        ArrayList<Cliente> registrados = farmacia.listarClientes();
        if (registrados.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }
        for (Cliente cliente : registrados) {
            System.out.println(cliente);
        }
    }

    private void actualizarCliente() {
        String identificacion = leerTexto("Identificacion: ");
        String nombre = leerTexto("Nuevo nombre: ");
        farmacia.actualizarCliente(identificacion, nombre);
        System.out.println("Cliente actualizado.");
    }

    // Menu de ventas
    private void mostrarVentas() {
        while (true) {
            mostrarOpciones();
            int opcion = leerEntero("Seleccione una opcion: ");
            if (opcion == 0) {
                return;
            }
            try {
                ejecutarOpcionVentas(opcion);
            } catch (IllegalArgumentException | IllegalStateException error) {
                System.out.println("Operacion rechazada: " + error.getMessage());
            }
        }
    }

    private void mostrarOpciones() {
        System.out.println("\n--- VENTAS Y DISPENSACION ---");
        System.out.println("Venta en preparacion: " + (farmacia.hayVentaEnCurso() ? "SI" : "NO"));
        System.out.println("1. Iniciar venta");
        System.out.println("2. Agregar producto");
        System.out.println("3. Registrar o corregir receta");
        System.out.println("4. Mostrar pre-resumen");
        System.out.println("5. Revisar resumen y confirmar venta");
        System.out.println("6. Cancelar venta");
        System.out.println("7. Consultar historial");
        System.out.println("0. Volver al menu principal");
    }

    private void ejecutarOpcionVentas(int opcion) {
        switch (opcion) {
            case 1: iniciarVenta(); break;
            case 2: agregarProducto(); break;
            case 3: registrarReceta(); break;
            case 4: mostrarResumen(); break;
            case 5: confirmarVenta(); break;
            case 6: cancelarVenta(); break;
            case 7:
                System.out.println(formatearHistorial(farmacia.listarVentas()));
                break;
            default: System.out.println("Opcion invalida. Intente nuevamente.");
        }
    }

    private void iniciarVenta() {
        farmacia.iniciarVenta(leerTexto("Identificacion del cliente: "));
        System.out.println("Venta iniciada.");
    }

    private void agregarProducto() {
        String codigo = leerTexto("Codigo del producto: ");
        int cantidad = leerEntero("Cantidad: ");
        if (farmacia.requiereReceta(codigo)
                && leerSiNo("Desea registrar la receta ahora? (S/N): ")) {
            String numero = leerTexto("Numero de receta: ");
            String paciente = leerTexto("Identificacion del paciente: ");
            LocalDate emision = leerFecha("Fecha de emision (AAAA-MM-DD): ");
            boolean aceptada = leerSiNo("La receta fue revisada y aceptada? (S/N): ");
            farmacia.agregarProducto(codigo, cantidad, numero, paciente, emision, aceptada);
        } else {
            farmacia.agregarProducto(codigo, cantidad);
        }
        System.out.println("Producto agregado a la venta.");
    }

    private void registrarReceta() {
        String codigo = leerTexto("Codigo del producto agregado: ");
        String numero = leerTexto("Numero de receta: ");
        String paciente = leerTexto("Identificacion del paciente: ");
        LocalDate emision = leerFecha("Fecha de emision (AAAA-MM-DD): ");
        boolean aceptada = leerSiNo("La receta fue revisada y aceptada? (S/N): ");
        farmacia.asignarReceta(codigo, numero, paciente, emision, aceptada);
        System.out.println("Receta registrada. Puede preparar otro resumen.");
    }

    private void mostrarResumen() {
        Venta resumen = farmacia.prepararResumen(LocalDate.now());
        System.out.println(formatearResumen(resumen));
    }

    private void confirmarVenta() {
        Venta resumen = farmacia.prepararResumen(LocalDate.now());
        System.out.println(formatearResumen(resumen));
        if (!leerSiNo("Confirmar esta venta? (S/N): ")) {
            System.out.println("La venta sigue en preparacion.");
            return;
        }
        Venta confirmada = farmacia.confirmarVenta(resumen, LocalDateTime.now());
        System.out.println("Venta confirmada. Numero: " + confirmada.getId()
                + " | Total: " + confirmada.getTotal().toPlainString());
    }

    private void cancelarVenta() {
        farmacia.cancelarVenta();
        System.out.println("Venta cancelada.");
    }



    // Presentacion de resumenes e historial
    private String formatearResumen(Venta resumen) {
        StringBuilder texto = new StringBuilder();
        texto.append("PRE-RESUMEN DE VENTA ").append(resumen.getId())
                .append(" | Fecha: ").append(resumen.getFecha()).append('\n')
                .append("Cliente: ").append(resumen.getIdentificacionCliente())
                .append(" - ").append(resumen.getNombreCliente()).append('\n');
        agregarDetalles(texto, resumen.getDetalles());
        return texto.append("TOTAL: ").append(resumen.getTotal().toPlainString()).toString();
    }

    private String formatearVenta(Venta venta) {
        StringBuilder texto = new StringBuilder();
        texto.append("Venta ").append(venta.getId()).append(" | ").append(venta.getFechaHora())
                .append(" | Cliente: ").append(venta.getIdentificacionCliente())
                .append(" - ").append(venta.getNombreCliente())
                .append(" | Total: ").append(venta.getTotal().toPlainString()).append('\n');
        agregarDetalles(texto, venta.getDetalles());
        return texto.toString();
    }

    private String formatearHistorial(ArrayList<Venta> historial) {
        if (historial.isEmpty()) {
            return "No hay ventas confirmadas.";
        }
        StringBuilder texto = new StringBuilder();
        for (Venta venta : historial) {
            texto.append(formatearVenta(venta));
        }
        return texto.toString();
    }

    private void agregarDetalles(StringBuilder texto, ArrayList<DetalleVenta> detalles) {
        for (DetalleVenta detalle : detalles) {
            texto.append(detalle.getCodigoProductoHistorico()).append(" - ")
                    .append(detalle.getNombreProductoHistorico())
                    .append(" | Cantidad: ").append(detalle.getCantidad())
                    .append(" | Precio: ").append(detalle.getPrecioUnitario().toPlainString())
                    .append(" | Subtotal: ").append(detalle.calcularSubtotal().toPlainString())
                    .append('\n');
            agregarLotes(texto, detalle.getAsignaciones());
        }
    }

    private void agregarLotes(StringBuilder texto, ArrayList<SimpleImmutableEntry<Lote, Integer>> asignaciones) {
        for (SimpleImmutableEntry<Lote, Integer> asignacion : asignaciones) {
            texto.append("  Lote ").append(asignacion.getKey().getNumero())
                    .append(" | Unidades: ").append(asignacion.getValue())
                    .append(" | Caduca: ").append(asignacion.getKey().getFechaCaducidad())
                    .append('\n');
        }
    }

    // Conversion de entradas de consola
    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException error) {
                System.out.println("Entrada invalida. Ingrese un numero entero.");
            }
        }
    }

    private BigDecimal leerDecimal(String mensaje) {
        while (true) {
            try {
                return new BigDecimal(leerTexto(mensaje).replace(',', '.'));
            } catch (NumberFormatException error) {
                System.out.println("Entrada invalida. Use un numero como 2.50 o 2,50.");
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(mensaje));
            } catch (DateTimeParseException error) {
                System.out.println("Fecha invalida. Use AAAA-MM-DD.");
            }
        }
    }

    private boolean leerSiNo(String mensaje) {
        while (true) {
            String respuesta = leerTexto(mensaje);
            if (respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("si")
                    || respuesta.equalsIgnoreCase("sí")) {
                return true;
            }
            if (respuesta.equalsIgnoreCase("n") || respuesta.equalsIgnoreCase("no")) {
                return false;
            }
            System.out.println("Ingrese S para si o N para no.");
        }
    }
}

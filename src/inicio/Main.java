package inicio;

import consola.EntradaConsola;
import consola.MenuClientes;
import consola.MenuProductos;
import consola.MenuVentas;
import java.util.NoSuchElementException;
import java.util.Scanner;
import servicios.GestorClientes;
import servicios.GestorInventario;
import servicios.GestorVentas;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        
        GestorInventario inventario = new GestorInventario();
        GestorClientes clientes = new GestorClientes();
        GestorVentas ventas = new GestorVentas(inventario, clientes);

        try (Scanner scanner = new Scanner(System.in)) {
            EntradaConsola entrada = new EntradaConsola(scanner);
            MenuProductos menuProductos = new MenuProductos(inventario, entrada);
            MenuClientes menuClientes = new MenuClientes(clientes, entrada);
            MenuVentas menuVentas = new MenuVentas(ventas, entrada);

            int opcion;
            do {
                System.out.println("\n=== FARMACIA SAN WJ ===");
                System.out.println("1. Productos e inventario");
                System.out.println("2. Clientes");
                System.out.println("3. Ventas y dispensacion");
                System.out.println("0. Salir del sistema");
                opcion = entrada.leerEntero("Seleccione una opcion: ");

                switch (opcion) {
                    case 1:
                        menuProductos.mostrar();
                        break;
                    case 2:
                        menuClientes.mostrar();
                        break;
                    case 3:
                        menuVentas.mostrar();
                        break;
                    case 0:
                        System.out.println("Sistema finalizado.");
                        break;
                    default:
                        System.out.println("Opcion invalida. Intente nuevamente.");
                }
            } while (opcion != 0);
        } catch (NoSuchElementException error) {
            System.out.println("\nFin de entrada. Sistema finalizado.");
        }
    }
}

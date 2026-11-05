package consola;

import dominio.Cliente;
import java.util.ArrayList;
import servicios.GestorClientes;

/** Interaccion del modulo de clientes; conserva la gestora recibida. */
public final class MenuClientes {

    private final GestorClientes clientes;
    private final EntradaConsola entrada;

    public MenuClientes(GestorClientes clientes, EntradaConsola entrada) {
        if (clientes == null || entrada == null) {
            throw new IllegalArgumentException("Gestor de clientes y entrada son obligatorios.");
        }
        this.clientes = clientes;
        this.entrada = entrada;
    }

    public void mostrar() {
        while (true) {
            System.out.println("\n--- CLIENTES ---");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Listar clientes");
            System.out.println("3. Buscar cliente");
            System.out.println("4. Actualizar nombre");
            System.out.println("0. Volver al menu principal");
            int opcion = entrada.leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1: registrarCliente(); break;
                    case 2: listarClientes(); break;
                    case 3:
                        System.out.println(clientes.buscarCliente(
                                entrada.leerTexto("Identificacion: ")));
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
        String identificacion = entrada.leerTexto("Identificacion: ");
        String nombre = entrada.leerTexto("Nombre: ");
        clientes.registrarCliente(new Cliente(identificacion, nombre));
        System.out.println("Cliente registrado.");
    }

    private void listarClientes() {
        ArrayList<Cliente> registrados = clientes.listarClientes();
        if (registrados.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }
        for (Cliente cliente : registrados) {
            System.out.println(cliente);
        }
    }

    private void actualizarCliente() {
        String identificacion = entrada.leerTexto("Identificacion: ");
        String nombre = entrada.leerTexto("Nuevo nombre: ");
        clientes.actualizarCliente(identificacion, nombre);
        System.out.println("Cliente actualizado.");
    }
}

package servicios;

import dominio.Cliente;
import java.util.ArrayList;

/** Registro de clientes exclusivamente en memoria. */
public final class GestorClientes {

    private final ArrayList<Cliente> clientes;

    public GestorClientes() {
        clientes = new ArrayList<>();
    }

    public ArrayList<Cliente> getClientes() {
        return new ArrayList<>(clientes);
    }

    public ArrayList<Cliente> listarClientes() {
        return getClientes();
    }

    public void registrarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        for (Cliente existente : clientes) {
            if (existente.getIdentificacion().equalsIgnoreCase(cliente.getIdentificacion())) {
                throw new IllegalArgumentException("La identificacion del cliente ya esta registrada.");
            }
        }
        clientes.add(cliente);
    }

    public Cliente buscarCliente(String identificacion) {
        if (identificacion == null || identificacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La identificacion es obligatoria.");
        }
        for (Cliente cliente : clientes) {
            if (cliente.getIdentificacion().equalsIgnoreCase(identificacion.trim())) {
                return cliente;
            }
        }
        throw new IllegalArgumentException("No existe el cliente con identificacion "
                + identificacion.trim() + ".");
    }

    public void actualizarCliente(String identificacion, String nombre) {
        buscarCliente(identificacion).setNombre(nombre);
    }
}

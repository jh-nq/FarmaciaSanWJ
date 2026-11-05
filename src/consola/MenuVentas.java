package consola;

import dominio.Receta;
import dominio.ResumenVenta;
import dominio.Venta;
import java.time.LocalDate;
import java.time.LocalDateTime;
import servicios.GestorVentas;

/** Solo navegacion, captura y presentacion; depende de una unica gestora. */
public final class MenuVentas {

    private final GestorVentas ventas;
    private final EntradaConsola entrada;
    private final FormateadorVentas formateador;

    public MenuVentas(GestorVentas ventas, EntradaConsola entrada) {
        if (ventas == null || entrada == null) {
            throw new IllegalArgumentException("Gestora de ventas y entrada son obligatorias.");
        }
        this.ventas = ventas;
        this.entrada = entrada;
        this.formateador = new FormateadorVentas();
    }

    public void mostrar() {
        while (true) {
            mostrarOpciones();
            int opcion = entrada.leerEntero("Seleccione una opcion: ");
            if (opcion == 0) {
                return;
            }
            try {
                ejecutarOpcion(opcion);
            } catch (IllegalArgumentException | IllegalStateException error) {
                System.out.println("Operacion rechazada: " + error.getMessage());
            }
        }
    }

    private void mostrarOpciones() {
        System.out.println("\n--- VENTAS Y DISPENSACION ---");
        System.out.println("Venta en preparacion: " + (ventas.hayVentaEnCurso() ? "SI" : "NO"));
        System.out.println("1. Iniciar venta");
        System.out.println("2. Agregar producto");
        System.out.println("3. Registrar o corregir receta");
        System.out.println("4. Mostrar pre-resumen");
        System.out.println("5. Revisar resumen y confirmar venta");
        System.out.println("6. Cancelar venta");
        System.out.println("7. Consultar historial");
        System.out.println("0. Volver al menu principal");
    }

    private void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1: iniciarVenta(); break;
            case 2: agregarProducto(); break;
            case 3: registrarReceta(); break;
            case 4: mostrarResumen(); break;
            case 5: confirmarVenta(); break;
            case 6: cancelarVenta(); break;
            case 7:
                System.out.println(formateador.formatearHistorial(ventas.listarVentas()));
                break;
            default: System.out.println("Opcion invalida. Intente nuevamente.");
        }
    }

    private void iniciarVenta() {
        ventas.iniciarVenta(entrada.leerTexto("Identificacion del cliente: "));
        System.out.println("Venta iniciada.");
    }

    private void agregarProducto() {
        String codigo = entrada.leerTexto("Codigo del producto: ");
        int cantidad = entrada.leerEntero("Cantidad: ");
        Receta receta = null;
        if (ventas.requiereReceta(codigo)
                && entrada.leerSiNo("Desea registrar la receta ahora? (S/N): ")) {
            receta = leerReceta(codigo);
        }
        ventas.agregarProducto(codigo, cantidad, receta);
        System.out.println("Producto agregado a la venta.");
    }

    private void registrarReceta() {
        String codigo = entrada.leerTexto("Codigo del producto agregado: ");
        ventas.asignarReceta(codigo, leerReceta(codigo));
        System.out.println("Receta registrada. Puede preparar otro resumen.");
    }

    private void mostrarResumen() {
        ResumenVenta resumen = ventas.prepararResumen(LocalDate.now());
        System.out.println(formateador.formatearResumen(resumen));
    }

    private void confirmarVenta() {
        ResumenVenta resumen = ventas.prepararResumen(LocalDate.now());
        System.out.println(formateador.formatearResumen(resumen));
        if (!entrada.leerSiNo("Confirmar esta venta? (S/N): ")) {
            System.out.println("La venta sigue en preparacion.");
            return;
        }
        Venta confirmada = ventas.confirmarVenta(resumen, LocalDateTime.now());
        System.out.println("Venta confirmada. Numero: " + confirmada.getId()
                + " | Total: " + confirmada.getTotal().toPlainString());
    }

    private void cancelarVenta() {
        ventas.cancelarVenta();
        System.out.println("Venta cancelada.");
    }

    private Receta leerReceta(String codigo) {
        String numero = entrada.leerTexto("Numero de receta: ");
        String identificacion = entrada.leerTexto("Identificacion del paciente: ");
        LocalDate emision = entrada.leerFecha("Fecha de emision (AAAA-MM-DD): ");
        boolean aceptada = entrada.leerSiNo("La receta fue revisada y aceptada? (S/N): ");
        return new Receta(numero, identificacion, codigo, emision, aceptada);
    }
}

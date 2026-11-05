package consola;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Conversion de entradas. Las reglas de negocio permanecen en las gestoras. */
public final class EntradaConsola {

    private final Scanner scanner;

    public EntradaConsola(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("El lector de consola es obligatorio.");
        }
        this.scanner = scanner;
    }

    public String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    public int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException error) {
                System.out.println("Entrada invalida. Ingrese un numero entero.");
            }
        }
    }

    public BigDecimal leerDecimal(String mensaje) {
        while (true) {
            try {
                return new BigDecimal(leerTexto(mensaje).replace(',', '.'));
            } catch (NumberFormatException error) {
                System.out.println("Entrada invalida. Use un numero como 2.50 o 2,50.");
            }
        }
    }

    public LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(mensaje));
            } catch (DateTimeParseException error) {
                System.out.println("Fecha invalida. Use AAAA-MM-DD.");
            }
        }
    }

    public boolean leerSiNo(String mensaje) {
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

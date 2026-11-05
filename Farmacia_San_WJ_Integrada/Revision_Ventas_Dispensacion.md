# Revision del proceso de Ventas y Dispensacion - Farmacia San WJ

## Conclusion de la revision

La version anterior cumplia el requerimiento en el flujo normal de la aplicacion:
mostraba cantidades, precios unitarios, subtotales y total, y preparar el resumen
no descontaba existencias. GestorVentas exigia un resumen preparado antes de
confirmar, invalidaba ese resumen al editar y volvia a verificar fecha, recetas,
lotes y stock.

La mejora necesaria era principalmente de diseno y claridad del flujo. La nueva
version conserva esas garantias, devuelve un resumen inmutable con datos y
muestra un resumen actualizado inmediatamente antes de solicitar S/N en la
opcion de confirmar. No se presenta como fallo funcional un descuento prematuro
que no se observo en el flujo anterior.

## Evidencia de la version anterior

Las referencias corresponden a las fuentes revisadas antes de modificarlas.

| Hallazgo | Evidencia | Evaluacion |
|---|---|---|
| Informacion completa del resumen | GestorVentas.construirResumen, lineas 179-200: cantidad, precio, subtotal y total. | Cumplia. |
| Preparacion sin descuento | GestorVentas.prepararResumen, lineas 72-86, planificaba asignaciones sin aplicar la salida. | Cumplia. |
| Confirmacion protegida | GestorVentas.confirmarVenta, lineas 89-125, exigia resumen, verificaba fecha y stock y despues aplicaba la salida. | Cumplia por la API de la gestora. |
| Flujo visible | MenuVentas, lineas 55-61: la opcion 4 imprimia el resumen y la 5 confirmaba. | Correcto, pero ambas acciones estaban separadas. |
| Servicio que tambien presenta | GestorVentas.construirResumen armaba texto mediante StringBuilder. | Responsabilidades mezcladas. |
| Menu acoplado al inventario | MenuVentas, lineas 17-27 y 77-86, recibia y consultaba GestorInventario directamente. | Dependencia evitable. |
| Metodo con varias tareas | GestorVentas.confirmarVenta tenia 37 lineas, combinando validacion, planes, descuento, cambio de estado e historial. | Convenia separar tareas por su responsabilidad, no solo por su longitud. |
| Navegacion y acciones mezcladas | MenuVentas.mostrar tenia 46 lineas con menu, switch, captura, formateo y operaciones. | Convenia extraer acciones con nombres explicitos. |
| Orden fragil para evolucionar | Se aplicaba el descuento y luego se llamaba a Venta.confirmar, que volvia a validar. | No se observo un fallo con las reglas existentes, ya verificadas antes. Era un riesgo de mantenimiento si se agregaban validaciones posteriores. |
| Objeto con dos responsabilidades | Venta representaba tanto el borrador editable como el comprobante confirmado. | Se mejoro su encapsulamiento separando ambos estados en tipos distintos. |

El metodo publico Venta.confirmar era un cambio de estado del documento, no la
operacion completa de dispensacion. El registro real se coordinaba desde
GestorVentas y ese borrador no se exponia al menu. La nueva API evita esa
ambiguedad: Venta es un comprobante inmutable y confirmarVenta es la operacion
de registro del sistema.

## Refactorizacion aplicada

| Antes | Ahora | Concepto de POO mejorado |
|---|---|---|
| String como resultado de prepararResumen | ResumenVenta con detalles y total inmutables. | Encapsulamiento y abstraccion: se transmite el significado de los datos. |
| StringBuilder dentro de GestorVentas | FormateadorVentas en consola. | Responsabilidad unica: negocio y presentacion cambian por separado. |
| MenuVentas conoce ventas e inventario | MenuVentas depende de GestorVentas para todas las operaciones y consultas del flujo. | Menor acoplamiento y mayor cohesion. |
| Venta editable y despues bloqueada | BorradorVenta editable, DetalleVenta inmutable y Venta como comprobante. | Encapsulamiento e inmutabilidad del historial. |
| Booleano y fecha independientes como permiso | Referencia al ResumenVenta vigente y validacion de su identidad y fecha. | Estado coherente: confirmar exige el resumen exacto emitido por la gestora. |
| Colecciones paralelas llamadas planes y detalles | Cada DetalleVenta contiene sus propias asignaciones. | Composicion: la relacion se expresa con objetos en lugar de posiciones de listas. |
| Descontar y luego volver a validar el documento | Construir y validar el comprobante antes de aplicar la salida. | Responsabilidades y orden de la operacion mas claros. |

Se mantiene el polimorfismo: DetalleVenta utiliza Producto.requiereReceta().
Las subclases responden segun el tipo de producto; el menu no necesita comprobar
tipos concretos.

La inmutabilidad se aplica a cantidades, precios, datos historicos y la
distribucion capturada. Las referencias a Producto y Lote enlazan entidades
vivas del inventario: no se congelan sus existencias, que se revalidan al
confirmar. Se protege la cotizacion sin duplicar el saldo del inventario.

Los nombres tambien expresan mejor su proposito: exigirVentaEnCurso,
validarResumenVigente, planificarDetalles, formatearResumen y BorradorVenta.
Se eliminaron los nombres genericos de colecciones paralelas. Los nombres
preexistentes que ya eran claros, como agregarProducto y cancelarVenta,
se conservaron.

## Tamano de los metodos

El conteo incluye declaracion, llaves y lineas en blanco. Reducir lineas no es
por si solo una mejora: aqui la extraccion separa responsabilidades concretas.

| Metodo | Antes | Despues |
|---|---:|---:|
| GestorVentas.confirmarVenta | 37 lineas | 20 lineas |
| MenuVentas.mostrar | 46 lineas | 14 lineas |

MenuVentas.ejecutarOpcion tiene 14 lineas y delega cada accion a su propio
metodo. MenuVentas.confirmarVenta tiene 11 lineas. El formato se encuentra en
FormateadorVentas; la planificacion permanece en la gestora.

## Confirmacion visible

La opcion 5 ejecuta este metodo:

```java
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
```

Antes de descontar se obtiene y presenta el resumen. La respuesta No no llama a
confirmarVenta. Si se responde Si, la gestora exige que ese objeto siga siendo
el resumen vigente, revalida los lotes y construye el comprobante. Finalmente,
el inventario valida la salida completa y descuenta sus lotes.

La logica de negocio no puede probar que una persona haya leido la pantalla;
eso pertenece a la interaccion. La API exige el objeto resumen y el menu asegura
la secuencia mostrar, preguntar y confirmar. Una llamada programatica a la
gestora debe respetar esa misma responsabilidad de presentacion.

## Garantias verificadas

- Preparar o mostrar el resumen no reserva ni descuenta stock ni agrega historial.
- Un resumen anterior, reemplazado, fabricado o perteneciente a otra venta se rechaza.
- Las cantidades repetidas se acumulan y se comprueba el total solicitado.
- Editar cantidades o recetas invalida el resumen emitido.
- Recetas incorrectas, lotes caducados y stock insuficiente bloquean la operacion.
- Un cambio de fecha o distribucion por lotes obliga a revisar otro resumen.
- La respuesta No conserva el borrador sin descontar ni registrar una venta.
- Cancelar tampoco descuenta ni agrega historial.
- Cada venta confirmada tiene su propio resumen y se registra una sola vez.
- Resumenes y comprobantes conservan cantidades, precios y nombres historicos.
- Los ArrayList se devuelven como copias de la estructura y sus lineas de venta
  son valores inmutables.
- Main solo inicializa instancias compartidas y mantiene la navegacion.

La garantia de ausencia de descuentos parciales corresponde a la ejecucion
monousuario y secuencial definida para este trabajo. No se agrega concurrencia
ni persistencia. Se conserva la politica anterior de precios capturados al
agregar el producto y del cliente capturado al iniciar el borrador.

## Entrega y compatibilidad

El ZIP incluye los 22 archivos Java y la documentacion. Se ejecuta inicio.Main.
Se debe actualizar la carpeta src completa porque prepararResumen ahora devuelve
ResumenVenta y confirmarVenta recibe ese resumen y la fecha y hora.

Resultado: compilacion Java 8 mediante --release 8, con -Xlint:all -Werror, en
JDK 17. Pasaron 80 comprobaciones de ventas, 72 de inventario y 65 comprobaciones
de consola distribuidas en 22 escenarios: 217 comprobaciones en total.

# Farmacia San WJ: version revisada de Ventas y Dispensacion

El paquete contiene 22 archivos Java: todos los modulos, el Main y los menus.
La aplicacion conserva el alcance de consola monousuario, con ArrayList en
memoria y sin base de datos ni persistencia de datos en archivos.

## Ejecutar en NetBeans

1. Crea un proyecto Java o abre el proyecto de la farmacia.
2. Usa completa la carpeta src de este paquete, con sus paquetes dominio,
   servicios, consola e inicio.
3. Selecciona inicio.Main como clase principal.
4. Compila y ejecuta con JDK 8 o posterior. No necesita librerias externas.

Esta refactorizacion cambia las firmas de Ventas. Actualiza juntas las clases
del paquete para evitar mezclar la API anterior con la nueva. Las gestoras de
inventario y clientes y sus reglas se conservan.

## Navegacion y venta

Main crea una sola instancia de GestorInventario, GestorClientes y GestorVentas.
Los menus comparten estas instancias y un unico Scanner. La opcion 0 de cada
submenu vuelve al principal; la opcion 0 del principal cierra el sistema.
Volver al principal conserva registros y cualquier venta en preparacion.

Para vender: registra productos, lotes y cliente; inicia una venta y agrega
sus productos. La opcion 4 permite consultar el resumen. La opcion 5 prepara
un resumen vigente, lo muestra y pregunta Confirmar esta venta? (S/N).
Solo S ejecuta la confirmacion. N mantiene el borrador sin descontar ni
registrar una venta. La opcion 6 permite cancelar el borrador.

Las cantidades se leen como enteros; los precios admiten punto o coma decimal
sin separadores de miles; las fechas usan AAAA-MM-DD. Se usa la fecha del equipo.

## Responsabilidades de las clases refactorizadas

| Clase | Responsabilidad |
|---|---|
| BorradorVenta | Mantener las ediciones previas al descuento. |
| DetalleVenta | Conservar una linea inmutable con cantidad, precio, receta y asignaciones. Las operaciones con... devuelven otro valor. |
| ResumenVenta | Mantener la cotizacion inmutable que se entrega para revision. |
| Venta | Conservar el comprobante confirmado sin mutadores de negocio. |
| GestorVentas | Coordinar validacion, planificacion por lotes, descuento e historial. |
| FormateadorVentas | Convertir resumenes y comprobantes en texto de consola. |
| MenuVentas | Leer opciones, mostrar resultados y solicitar la confirmacion explicita. |

MenuVentas ya no depende de GestorInventario. GestorVentas devuelve datos y
no conoce el formato ni la salida de consola. Main conserva solo inicializacion
y navegacion. El polimorfismo de Producto.requiereReceta() se mantiene.

## Cambios de API

- MenuVentas recibe GestorVentas y EntradaConsola.
- GestorVentas.prepararResumen(LocalDate) devuelve ResumenVenta.
- GestorVentas.confirmarVenta(ResumenVenta, LocalDateTime) exige exactamente el
  resumen vigente de la venta en curso.
- Venta representa el comprobante; las ediciones pertenecen a BorradorVenta.
- DetalleVenta es inmutable: conUnidadesAgregadas, conReceta y conAsignaciones
  crean nuevas lineas y no alteran resumenes ni comprobantes existentes.

La inmutabilidad protege las cantidades, precios y datos historicos capturados.
Producto y Lote siguen siendo entidades vivas referenciadas para validar y
trazar el inventario; sus cambios de existencias se revalidan al confirmar.

Editar cantidades o recetas invalida el resumen. Confirmar revalida fecha,
recetas, lotes y stock. Si la distribucion cambia o el resumen pertenece a otra
venta, la operacion se rechaza sin descuentos ni registro de historial.

El comprobante se valida y construye antes del descuento. Despues de la salida
completa solo se incorpora al historial y se libera el borrador, sin nuevas
decisiones de negocio. Esta garantia corresponde al alcance monousuario y
secuencial de la aplicacion.

Se conserva la politica anterior: el cliente se captura al iniciar el borrador
y el nombre y precio del producto al agregar su primera linea. La cotizacion
mantiene esos valores; modificar posteriormente el catalogo no altera lo ya
cotizado ni el historial.

## Documentacion y verificacion

- Revision_Ventas_Dispensacion.md explica la evaluacion y los conceptos de POO.
- PRUEBAS_REALIZADAS.md detalla las pruebas y sus resultados.

Las 22 fuentes compilaron con --release 8 -Xlint:all -Werror en JDK 17.
Se aprobaron 80 comprobaciones de Ventas, 72 de inventario y 65 de consola.
Las 65 comprobaciones de consola cubren 22 escenarios, incluyendo la respuesta
No y dos ventas consecutivas con un resumen propio antes de cada confirmacion.

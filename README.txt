FARMACIA SAN WJ CON 8 CLASES
Java Application with Ant en NetBeans

COMO EJECUTAR

1. Descomprime el ZIP en una carpeta de tu computadora.
2. Abre NetBeans y selecciona Archivo > Nuevo proyecto.
3. En Categorias elige Java with Ant y en Proyectos Java Application.
4. Escribe como nombre FarmaciaSanWJ y elige donde guardar el proyecto.
5. Desmarca Create Main Class o Crear clase principal y pulsa Finalizar.
6. Abre la carpeta del proyecto que acabas de crear en el explorador de archivos.
7. Copia alli la carpeta src de este paquete, integrandola con la carpeta src
   vacia que genero NetBeans. Dentro de src deben quedar directamente los
   paquetes dominio, inicio y servicios. Evita crear una segunda carpeta src
   dentro de la primera.
8. Regresa a NetBeans. Haz clic derecho en el proyecto > Propiedades > Ejecutar.
   En Clase principal selecciona o escribe inicio.Main y confirma con Aceptar.
   Si NetBeans solicita elegir una clase al ejecutar, selecciona inicio.Main.
9. Ejecuta con F6 o clic derecho en el proyecto > Run o Ejecutar.
10. Utiliza la ventana Salida u Output para escribir las opciones del menu.


FUNCIONES Y USO

- Registra los productos, sus lotes y un cliente antes de iniciar una venta.
- Las cantidades deben ser enteros positivos y los precios mayores que cero.
- Los precios admiten punto o coma decimal, sin separadores de miles.
- Las fechas se escriben como AAAA-MM-DD. Se utiliza la fecha del equipo.
- La venta comienza como borrador. Se agregan productos y, cuando corresponde,
  se registra una receta para el mismo cliente y el producto de esa linea.
- La receta se valida con la aceptacion del operador, la identificacion del
  paciente y una fecha de emision que no sea futura.
- El pre-resumen muestra cantidades, precios, subtotales, lotes y total.
- Al confirmar se vuelve a mostrar el resumen. N conserva el borrador y S
  confirma la venta y descuenta existencias.
- Los lotes caducados se excluyen del stock disponible. Primero se utilizan
  los lotes disponibles cuya fecha de caducidad es anterior (FEFO).
- El historial conserva nombres, cantidades y precios de la venta confirmada.
- Cancelar no descuenta ni registra otra venta. La opcion 0 de cada modulo
  vuelve al principal y la opcion 0 del principal cierra el sistema.

Los datos se mantienen exclusivamente durante la ejecucion y se pierden al
cerrar. La aplicacion es de consola, monousuario y sin base de datos ni
persistencia de datos en archivos. El metodo estaPorCaducar se conserva en
Lote; no hay un reporte especifico por caducar en el menu.

CAMBIOS EN EL DISENO

Producto ahora es una clase concreta. Medicamento hereda de ella y configura
si necesita receta. Se conservan encapsulamiento, herencia y polimorfismo.
Los datos de la receta se integran en DetalleVenta y quedan vinculados al
producto de esa linea. Las asignaciones se representan mediante pares
inmutables de lote y cantidad de la biblioteca estandar de Java
(SimpleImmutableEntry): getKey devuelve el lote y getValue la cantidad.
Las colecciones del sistema siguen siendo ArrayList.

VERIFICACION

Las 8 fuentes compilaron con compatibilidad Java 8 sobre JDK 17, sin
advertencias. Se aprobaron 79 comprobaciones de negocio y el recorrido de
consola de 122 entradas produjo la misma salida que la version anterior,
salvo el instante de confirmacion. Se comprobaron las fuentes por consola;
la interfaz de NetBeans no se ejecuto en este entorno.


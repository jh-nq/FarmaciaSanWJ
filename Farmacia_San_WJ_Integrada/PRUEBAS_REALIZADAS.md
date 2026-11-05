# Pruebas realizadas

Compilacion de las 22 fuentes: `--release 8 -encoding UTF-8 -Xlint:all -Werror` en JDK 17.

| Grupo | Comprobaciones aprobadas |
|---|---:|
| Ventas refactorizadas | 80 |
| Regresion del inventario | 72 |
| Consola, 22 escenarios | 65 |
| Total | 217 |

## Escenarios de consola

1. cancelacion.
2. clientes persisten.
3. confirmacion con resumen inmediato.
4. conversion de entradas.
5. dos ventas dos resumenes.
6. edicion antes de confirmar.
7. entrada principal invalida.
8. eof principal.
9. eof submenu.
10. fin entrada al confirmar.
11. listas vacias.
12. lote caducado.
13. navegacion repetida.
14. no confirmar no descuenta.
15. nuevo lote antes de confirmar.
16. receta rechazada y corregida.
17. reglas en gestoras.
18. salida.
19. submenus invalidos.
20. tres tipos productos.
21. venta entre menus.
22. volver de todos.

Se verifico que cantidades, precios unitarios, subtotales y total aparezcan antes de la pregunta S/N y del descuento. La respuesta No conserva stock e historial. Dos ventas consecutivas requieren dos resumenes propios.

Las pruebas de negocio comprueban tambien resumenes anteriores o fabricados, ediciones posteriores, cambios de fecha y lotes, recetas incorrectas, cantidades acumuladas y rechazos sin descuentos parciales.

Los ArrayList y la inmutabilidad de resumenes y comprobantes se verificaron mediante operaciones publicas y revision de atributos. La navegacion conserva gestoras y un solo Scanner.

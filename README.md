# Etiquetas-Palet

Aplicación de escritorio en Java para crear etiquetas GS1 en formato A5 para palets.

## Características
- Interfaz Swing para introducir Nº de pedido, cajas, lote y fecha de consumo preferente (dd/MM/yyyy).
- Selector de producto con EAN13, unidades por caja y peso unitario; cálculo automático de **PESO NETO PALET**.
- Visualización previa de la cadena GS1 generada con GTIN-14, peso (AI 3103), fecha de caducidad (AI 17) y lote (AI 10).
- Generación del código GS1 en Code 128 listo para imprimir en tamaño A5 mediante el diálogo de impresión del sistema.
- Configurador de productos para crear, editar o eliminar referencias; los datos se persisten en `~/.palet-labels/products.json`.

## Ejecución
1. Compila el proyecto con Maven y genera un JAR autoejecutable con todas las dependencias:
   ```bash
   mvn clean package
   ```
   > El artefacto `target/Etiquetas-Palet-1.0.0-with-dependencies.jar` se puede abrir directamente (doble clic en Windows) o ejecutar desde consola.

2. Ejecuta la aplicación manualmente si lo prefieres:
   ```bash
   java -jar target/Etiquetas-Palet-1.0.0-with-dependencies.jar
   ```

> Si es la primera ejecución, se crean algunos productos de ejemplo para empezar a imprimir.

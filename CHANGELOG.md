# Changelog
Todos los cambios relevantes de este proyecto se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/)
y este proyecto usa [Semantic Versioning](https://semver.org/lang/es/).

## [1.1.0] - 2025-12-18

### Added
- Soporte para productos de **peso variable (GS1 AI 02)**.
- Nuevo campo **“Peso variable”** en la gestión de productos.
- Persistencia del atributo de peso variable en datos de producto.
- La interfaz habilita la introducción manual del peso cuando el producto es de peso variable.
- Preparación del modelo para generar códigos GS1 con AI 01 o 02 según el tipo de producto.

### Changed
- Ajustes en la impresión de etiquetas para soportar pesos introducidos manualmente.
- Mejora en el renderizado del nombre de producto, dividiéndolo en varias líneas si no cabe en el área imprimible.

### Notes
- 

**Diseño de estructura de datos**

**Nombre de la Tabla**

| lasante-3d-catálogo |
| :---- |

**Claves**

| pk |
| :---- |

| sk |
| :---- |

**Tipos de entidad**

| `-UNIDAD_NEGOCIO -TRATAMIENTO -PRODUCTO -COLECCION -COLECCION_ITEM`  |
| :---- |

**1\. Unidad de negocio**

| `{ "pk": "UNIDAD#estetica", "sk": "META", "tipo": "UNIDAD_NEGOCIO", "unidadId": "estetica", "nombre": "Estética", "descripcion": "Servicios de belleza y bienestar", "estado": "ACTIVO", "orden": 1, "media": { "icono": "unidades-negocio/estetica/icono.png", "portada": "unidades-negocio/estetica/portada.jpg" }, "atributos": { "colorTema": "#D8C3A5" } }`  |
| :---- |

**Campos fijos**

| `-tipo -unidadId -nombre -descripción -estado -orden`  |
| :---- |

**Campos flexibles**

| `-atributos` |
| :---- |

**Tratamiento**

| `{ "pk": "UNIDAD#estetica", "sk": "TRATAMIENTO#limpieza-facial", "tipo": "TRATAMIENTO", "tratamientoId": "limpieza-facial", "unidadId": "estetica", "nombre": "Limpieza Facial", "descripcion": "Tratamiento facial profundo", "estado": "ACTIVO", "orden": 1, "media": { "icono": "tratamientos/estetica/limpieza-facial/icono.png", "portada": "tratamientos/estetica/limpieza-facial/portada.jpg" }, "atributos": { "duracion": "45 min", "frecuencia": "Semanal" } }`  |
| :---- |

**Campos fijos** 

| `-tratamiento -unidad -nombre -descripción -estado -orden` |
| :---- |

**Campos flexibles**

| `-atributos` |
| :---- |

**Producto**

| { "pk": "TRATAMIENTO\#limpieza-facial", "sk": "PRODUCTO\#serum-vitamina-c", "tipo": "PRODUCTO", "productoId": "serum-vitamina-c", "unidadId": "estetica", "tratamientoId": "limpieza-facial", "nombre": "Serum Vitamina C", "descripcion": "Antioxidante facial", "estado": "ACTIVO", "orden": 1, "media": { "imagenes2d": { "principal": "productos/estetica/limpieza-facial/serum-vitamina-c/2d/principal.jpg", "miniatura": "productos/estetica/limpieza-facial/serum-vitamina-c/2d/miniatura.jpg", "galería": \[ "productos/estetica/limpieza-facial/serum-vitamina-c/2d/galeria/01.jpg", "productos/estetica/limpieza-facial/serum-vitamina-c/2d/galeria/02.jpg" \] }, "modelo3d": { "glb": "productos/estetica/limpieza-facial/serum-vitamina-c/3d/modelo.glb", "usdz": "productos/estetica/limpieza-facial/serum-vitamina-c/3d/modelo.usdz", "vistaPrevia": "productos/estetica/limpieza-facial/serum-vitamina-c/3d/vista-previa.jpg" } }, "atributos": { "presentacion": "30 ml", "tipoPiel": "Mixta", "uso": "Diurno", "marca": "La Santé" } } |
| :---- |

**Campos fijos**

| `-productoId -unidadId -tratamientoId -nombre -descripción -estado -orden` |
| :---- |

**Campos flexibles**

| \- atributos |
| :---- |

**Media**

| `media.imagenes2d media.modelo3d`  |
| :---- |

**Colección de productos destacados**

| `{   "pk": "COLECCION#productos-destacados",   "sk": "META",   "tipo": "COLECCION",   "coleccionId": "productos-destacados",   "nombre": "Productos destacados",   "descripcion": "Colección usada por el carrusel principal",   "estado": "ACTIVO",   "maxItems": 16 }`  |
| :---- |

**Colección → Items destacados**

| `pk = COLECCION#productos-destacados sk = ITEM#...`  |
| :---- |

# **Criterio final del diseño**

## **Campos fijos**

Van arriba porque:

* siempre existen  
* son estructurales  
* se consultan mucho

## 

## 

## 

## **`atributos`**

Se usan para:

* propiedades variables  
* flexibilidad por entidad  
* crecer sin rediseñar la tabla

## **`media`**

Se usa para:

* agrupar imágenes, iconos y 3D  
* mantener orden  
* separar contenido multimedia del resto

# **Resumen final**

El diseño queda así:

* una sola tabla  
* campos estructurales arriba  
* `atributos` para campos flexibles  
* `media` para recursos visuales  
* colección aparte para destacados

APi Url: [**https://cz9s5ng4k8.execute-api.us-east-1.amazonaws.com/Stage/catalog**](https://cz9s5ng4k8.execute-api.us-east-1.amazonaws.com/Stage/catalog)

**APIs Propuestas – Microservicio de Catálogo (lasante-3d-gestion)**

**Unidades de Negocio**

| QTA | MÉTODO |                 ENDPOINT | DESCRIPCIÓN |
| :---- | :---- | :---- | :---- |
| 1 | GET | /unidades-negocio | Lista todas las unidades de negocio |
| 2 | GET | /unidades-negocio/{unidadId} | Obtiene el detalle de una unidad de negocio |
| 3 | POST | /unidades-negocio | Crea una nueva unidad de negocio |
| 4 | PUT | /unidades-negocio/{unidadId} | Actualiza una unidad de negocio |
| 5 | DELETE | /unidades-negocio/{unidadId} | Elimina (lógico) una unidad de negocio |

**Tratamientos**

| QTA | MÉTODO |                 ENDPOINT | DESCRIPCIÓN |
| :---- | :---- | :---- | :---- |
| 6 | GET | /tratamientos?unidadId={unidadId} | Lista tratamientos por unidad de negocio |
| 7 | GET | /tratamientos/{tratamientoId} | Obtiene el detalle de un tratamiento |
| 8 | POST | /tratamientos | Crea un nuevo tratamiento |
| 9 | PUT | /tratamientos/{tratamientoId} | Actualiza un tratamiento |
| 10 | DELETE | /tratamientos/{tratamientoId} | Elimina (lógico) un tratamiento |

**Productos** 

| QTA | MÉTODO |                 ENDPOINT | DESCRIPCIÓN |
| :---- | :---- | :---- | :---- |
| 11 | GET | /productos?tratamientoId={tratamientoId} | Lista productos por tratamiento |
| 12 | GET | /productos/{productoId} | Lista productos por tratamiento |
| 13 | POST | /productos | Crea un nuevo producto |
| 14 | PUT | /productos/{productoId} | Actualiza un producto |
| 15 | DELETE | /productos/{productoId} | Elimina (lógico) un producto |

**Productos Destacados (Carrusel)**

| QTA | MÉTODO |                 ENDPOINT | DESCRIPCIÓN |
| :---- | :---- | :---- | :---- |
| 16 | GET | /destacados | Lista productos destacados (ordenados) |
| 17 | POST | /destacados | Agrega un producto a destacados |
| 18 | DELETE | /destacados/{productoId} | Elimina un producto de destacados |
| 19 | PUT | /destacados/orden | Actualiza el orden de los productos destacados |
| 20 | PUT | /destacados/{productoId}/estado | Activa o desactiva un producto en el carrusel |


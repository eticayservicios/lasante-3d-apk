# Diseño de Funcionalidad - LaSanté 3D

## 1. Introducción – Pantallas del Sistema

El sistema se compone de un conjunto de pantallas interactivas diseñadas para ofrecer una experiencia de navegación intuitiva, visual y jerárquica sobre el catálogo de productos de La Santé.

Estas pantallas están orientadas a su uso en dispositivos táctiles (como pantallas tipo TV o kioscos digitales), permitiendo al usuario explorar de manera progresiva la información, partiendo desde una vista general hasta el detalle de cada producto.

### Tabla 1: Pantallas del Sistema

| QTA | Nombre de Pantalla (Funcional) | Nombre Software | Descripción |
|:---:|:---:|:---:|:---|
| 1 | Intro (Inicio) | pantalla_intro | Pantalla principal del sistema. Permite acceso a unidades de negocio, productos destacados y contenido institucional. |
| 2 | Unidades de Negocio | pantalla_unidad_negocio | Muestra las clases terapéuticas (tratamientos) asociadas a una unidad de negocio seleccionada. |
| 3 | Clase Terapéutica (Productos) | pantalla_clase_terapeutica | Muestra los productos asociados a un tratamiento, incluyendo búsqueda, filtros y visualización 3D. |

---

## 2. Elementos – Intro (Inicio)

Punto de entrada al sistema con elementos visuales atractivos.

### Tabla 2: Elementos de Pantalla Intro

| QTA | Elemento (Funcional) | Nombre Software | Descripción |
|:---:|:---:|:---:|:---|
| 1 | Carrusel de productos | seccion_carrusel_productos_destacados | Representación visual de unidades de negocio con rotación dinámica. |
| 2 | Vitrina unidades de negocio | seccion_vitrina_unidades_negocio | Muestra productos destacados seleccionados desde gestión. |
| 3 | Botón historia | componente_boton_historia | Permite reproducir el video institucional. |
| 4 | Footer redes sociales | seccion_footer_redes | Contiene accesos a redes sociales. |
| 5 | Rotación vitrina | comportamiento_rotacion_vitrina | Lógica que cambia la posición de los bloques al salir de inactividad o regresar a la pantalla. |

### Detalle de Funcionalidades - Intro

#### 1. Carrusel de productos
*   **Funcionalidad:** Muestra hasta 16 productos destacados, navegación horizontal con gestos táctiles o botones laterales. Selección abre vista 3D. Incluye sonido especial.
*   **Gestión:** Definidos desde el módulo de gestión (microservicio de catálogo).

#### 2. Vitrina de unidades de negocio
*   **Funcionalidad:** 5 bloques representando unidades de negocio. Al seleccionar, navega a tratamientos.
*   **Comportamiento:** Rotación circular de posiciones cada vez que se regresa a la pantalla o sale de inactividad.

#### 3. Botón Historia La Santé
*   **Funcionalidad:** Reproduce el video institucional de La Santé.
*   **Gestión:** El contenido de video es actualizable desde el sistema de gestión.

#### 4. Footer redes sociales
*   **Funcionalidad:** Enlaces fijos (IG, FB, IN, Catálogo) definidos directamente en la APK.

---

## 3. Elementos – Unidades de Negocio

Muestra las clases terapéuticas asociadas a la unidad seleccionada.

### Tabla 3: Elementos de Unidades de Negocio

| QTA | Elemento (Funcional) | Nombre Software | Descripción |
|:---:|:---:|:---:|:---|
| 1 | Botón retroceso | componente_boton_retroceso | Permite regresar a la pantalla anterior (Intro). |
| 2 | Nombre unidad | componente_titulo_unidad | Muestra el nombre de la unidad de negocio seleccionada. |
| 4 | Lista de tratamientos | lista_tratamientos | Lista vertical de clases terapéuticas (tratamientos). |
| 5 | Item tratamiento | item_tratamiento | Representa un tratamiento individual con información básica. |
| 6 | Botón ver tratamiento | componente_boton_ver_tratamiento | Navega a la pantalla de productos del tratamiento. |
| 7 | Scroll vertical | comportamiento_scroll_vertical | Permite navegar la lista cuando excede el área visible. |

---

## 4. Elementos – Clase Terapéutica (Productos)

Muestra el conjunto de productos con herramientas de búsqueda y filtrado.

### Tabla 4: Elementos de Pantalla de Productos

| QTA | Elemento (Funcional) | Nombre Software | Descripción |
|:---:|:---:|:---:|:---|
| 1 | Header | componente_header | Contenedor superior con navegación y control. |
| 2 | Botón retroceso | componente_boton_retroceso | Regresa a la pantalla de tratamientos. |
| 3 | Botón home | componente_boton_home | Regresa directamente a la pantalla Intro. |
| 4 | Buscador de productos | componente_buscador_productos | Búsqueda y filtrado en tiempo real. |
| 5 | Botón filtros | componente_boton_filtros | Permite aplicar filtros sobre los productos. |
| 6 | Botón ordenar | componente_boton_orden | Ordenamiento (ej. alfabético). |
| 7 | Grid de productos | grid_productos | Contenedor en formato cuadrícula visual uniforme. |
| 8 | Item producto | item_producto | Representa un producto individual con info básica. |
| 9 | Vista 3D (preview) | componente_producto_3d_preview | Vista previa 3D con rotación automática básica. |
| 10 | Descripción breve | componente_descripcion_producto | Información resumida del producto. |
| 11 | Scroll vertical | comportamiento_scroll_vertical | Navegación del listado de productos. |

---

## 5. Elementos – Visualización 3D del Producto

Ficha detallada interactiva del producto seleccionado.

### Tabla 5: Elementos de Detalle 3D

| QTA | Elemento (Funcional) | Nombre Software | Funcionalidad |
|:---:|:---:|:---:|:---|
| 1 | Header | componente_header | Estructura superior de la pantalla. |
| 2 | Botón retroceso | componente_boton_retroceso | Regresa a la pantalla anterior. |
| 3 | Botón home | componente_boton_home | Regresa a la pantalla Intro. |
| 4 | Visor 3D del producto | visor_producto_3d | Contenedor principal del modelo 3D. |
| 5 | Interacción 3D | comportamiento_interaccion_3d | Permite rotar el producto mediante gestos táctiles. |
| 6 | Botón producto anterior | componente_boton_producto_anterior | Navega al producto anterior de la lista. |
| 7 | Botón producto siguiente | componente_boton_producto_siguiente | Navega al producto siguiente de la lista. |
| 8 | Nombre del producto | componente_nombre_producto | Muestra el nombre del producto seleccionado. |
| 9 | Descripción del producto | componente_descripcion_producto | Muestra información breve del producto. |

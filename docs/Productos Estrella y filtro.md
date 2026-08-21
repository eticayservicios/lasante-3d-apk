# **Requerimientos funcionales – Productos Estrella y Filtros**

## **1\. Productos Estrella**

La funcionalidad **Productos Estrella** permitirá destacar los productos más relevantes de cada Unidad de Negocio y ofrecer dos niveles de visibilidad dentro de la aplicación.

### **1.1. Configuración desde el Back Admin**

**⚠️ REQUIERE DISEÑO – Pantalla de administración de Productos Estrella**

Se deberá diseñar dentro del Back Admin una interfaz que permita al administrador:

1. Seleccionar una **Unidad de Negocio**.  
2. Visualizar los productos pertenecientes a la Unidad de Negocio seleccionada.  
3. Seleccionar un máximo de **16 Productos Estrella**.  
4. Visualizar claramente la cantidad de productos seleccionados, por ejemplo: **8 de 16 seleccionados**.  
5. Realizar una segunda selección exclusivamente entre los Productos Estrella previamente seleccionados.  
6. Seleccionar en esta segunda ronda un máximo de **4 productos destacados**.  
7. Visualizar claramente la cantidad seleccionada en esta segunda ronda, por ejemplo: **3 de 4 seleccionados**.  
8. Guardar la configuración realizada.

El diseño deberá diferenciar claramente las dos etapas:

* **Primera selección:** hasta 16 Productos Estrella.  
* **Segunda selección:** hasta 4 productos destacados para mostrarse sobre la vitrina.

Los 4 productos de la segunda selección deberán formar parte de los Productos Estrella previamente seleccionados.

---

### **1.2. Productos destacados sobre la vitrina**

**✅ DISEÑO HOME – EXISTENTE**

Cuando una Unidad de Negocio se encuentre al frente de la vitrina, se mostrarán sobre ella los productos seleccionados en la segunda ronda, con un máximo de **4 productos**.

Estos productos corresponderán siempre a la Unidad de Negocio que se encuentre activa al frente de la vitrina.

Cuando cambie la Unidad de Negocio que está al frente, también deberán cambiar los productos destacados para mostrar los correspondientes a esa unidad.

---

### **1.3. Botón Productos Estrella**

**✅ DISEÑO HOME – EXISTENTE**

En el Home existirá **un único botón de Productos Estrella**, asociado a la vitrina.

El funcionamiento de este botón dependerá de la **Unidad de Negocio que se encuentre al frente de la vitrina en ese momento**.

Al seleccionar el botón:

* Se identificará la Unidad de Negocio que se encuentra al frente.  
* Se abrirá la pantalla de Productos Estrella correspondiente a esa Unidad de Negocio.  
* Se mostrarán exclusivamente los Productos Estrella configurados para dicha unidad.  
* Se podrán mostrar hasta un máximo de **16 productos**, dependiendo de la cantidad seleccionada desde el Back Admin.

Por ejemplo, si para una Unidad de Negocio se seleccionaron 10 Productos Estrella, la pantalla mostrará esos 10 productos.

---

## **2.1. Rediseño del filtro**

**✅ APK – Implementado (layout calibrado TV66 1280×720; escala multi-dispositivo pendiente)**

Dentro de la pantalla de una **Clase Terapéutica**, el usuario contará con la opción **Filtros** para reducir los productos mostrados según las opciones disponibles.

El contenido actual del filtro deberá ser rediseñado.

Para las presentaciones de producto se utilizará un **selector desplegable (Select) con casillas de selección (Checkbox)**.

Las opciones contempladas inicialmente dentro del Select serán:

* Comprimidos.  
* Cápsulas.  
* Suspensión.  
* Otros

Adicionalmente, la opción **Productos Estrella** deberá mostrarse **de forma independiente, en un box separado del selector de presentaciones**.

Cuando el usuario seleccione el box de **Productos Estrella**, se mostrarán únicamente los productos definidos como Productos Estrella que pertenezcan a la **Clase Terapéutica que se esté consultando**.

De esta forma, el filtro quedará visualmente dividido en:

* **Presentación del producto:** Select con Comprimidos, Cápsulas, Suspensión y Otros.  
* **Productos Estrella:** box independiente.  
* **Aplicar filtros.**  
* **Limpiar filtros.**

**Comportamiento APK (AND):** presentación y Productos Estrella se combinan con AND sobre el catálogo de la CT actual. El modal de filtros solo aparece en pantallas de clase terapéutica (no en modo estrellas / ver todos).

---

## **2.2. Productos Estrella dentro del filtro**

**✅ APK – Implementado**

La opción **Productos Estrella** permanecerá disponible dentro del filtro.

Cuando el usuario seleccione **Productos Estrella**, se mostrarán únicamente aquellos productos que hayan sido definidos previamente como Productos Estrella y que pertenezcan a la **Clase Terapéutica que el usuario está consultando**.

Por ejemplo, si el usuario se encuentra dentro de la Clase Terapéutica **Cardiovascular** y selecciona **Productos Estrella**, se mostrarán únicamente los Productos Estrella correspondientes a Cardiovascular.

---

## **2.3. Aplicar filtros**

**✅ APK – Implementado**

Una vez realizada la selección, el usuario deberá pulsar el botón **“Aplicar filtros”**.

Al hacerlo:

* Se procesará la selección realizada.  
* Se cerrará la ventana de filtros.  
* Se actualizarán los productos mostrados en pantalla.  
* Se visualizarán los productos correspondientes al filtro seleccionado.

---

## **2.4. Limpiar filtros**

**✅ APK – Implementado**

Se deberá mantener la opción **“Limpiar filtros”**.

Al seleccionarla:

* Se eliminarán los filtros seleccionados (en el modal y en el catálogo).  
* El modal **permanece abierto**.  
* Se restablecerá la visualización.  
* Se mostrarán nuevamente todos los productos correspondientes a la Clase Terapéutica actual.

---

## **2.5. Flujo del filtro**

El flujo esperado será:

**Clase Terapéutica → Filtros → Selección → Aplicar filtros → Visualización de productos**

La opción **Limpiar filtros** permitirá regresar a la visualización completa de los productos de la Clase Terapéutica (sin cerrar el modal).

---

# **3\. Resumen de necesidades de diseño**

| Elemento | Estado |
| ----- | ----- |
| Home / Vitrina | ✅ Diseño existente |
| Botón Productos Estrella del Home | ✅ Diseño existente |
| Visualización de los 4 productos sobre la vitrina | ✅ Diseño existente |
| **Administración de hasta 16 Productos Estrella** | ✅ Admin + APK |
| **Segunda selección de hasta 4 productos destacados** | ✅ Admin + APK (subset de estrellas) |
| **Nuevo filtro Select \+ Checkbox** | ✅ APK (escala multi-dispositivo pendiente) |
| **Acción Aplicar filtros** | ✅ APK |
| **Acción Limpiar filtros** | ✅ APK (limpia sin cerrar modal) |


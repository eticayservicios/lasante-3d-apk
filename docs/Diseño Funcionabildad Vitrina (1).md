# **Documento Técnico de Funcionamiento**

## Vitrina 3D Interactiva con Unidades de Negocio y Productos Destacados

## 1\. Objetivo general

\-Diseñar y desarrollar una vitrina digital 3D interactiva para presentar las unidades de negocio de La Santé y sus productos destacados (estrella)

\-La vitrina funcionará como un cilindro 3D giratorio, compuesto por 5 unidades de negocio. Cada unidad tendrá asociados 4 productos destacados (productos estrella)

\-La vitrina por defecto podrá iniciar en modo screen saver o inactividad. En este estado, la vitrina girará automáticamente como animación de atracción visual. Mientras esté girando, no se mostrarán productos destacados. Los productos aparecerán únicamente cuando la vitrina salga del modo screen saver, se detenga temporalmente, haga snap y una unidad de negocio quede completamente al frente.

\-Cada producto destacado será un subobjeto único e independiente, clickeable, administrable y asociado a un modal propio con información del producto, descripción e imagen o modelo 3D.

## 

## 

## 

## 

## 

## 

## 

## 

## **2\. Estructura general de la vitrina**

## La vitrina debe organizarse bajo una estructura jerárquica clara:

Vitrina 3D

├── **Unidad de negocio 1**

│ ├── Producto destacado 1

│ ├── Producto destacado 2

│ ├── Producto destacado 3

│ └── Producto destacado 4

├── **Unidad de negocio 2**

│ ├── Producto destacado 1

│ ├── Producto destacado 2

│ ├── Producto destacado 3

│ └── Producto destacado 4

├── **Unidad de negocio 3**

│ ├── Producto destacado 1

│ ├── Producto destacado 2

│ ├── Producto destacado 3

│ └── Producto destacado 4

├── **Unidad de negocio 4**

│ ├── Producto destacado 1

│ ├── Producto destacado 2

│ ├── Producto destacado 3

│ └── Producto destacado 4

└── **Unidad de negocio 5**

├── Producto destacado 1

├── Producto destacado 2

├── Producto destacado 3

└── Producto destacado 4 

## **3\. Concepto visual para el diseñador**

La vitrina no debe diseñarse como una imagen plana.  
 Debe diseñarse como un sistema compuesto por elementos independientes.

### A. Objeto principal

Vitrina 3D

Es el contenedor visual general. Debe permitir sensación de profundidad, rotación y cambio de cara o sección frontal.

### B. Objetos de primer nivel

**5 unidades de negocio**

Cada unidad debe funcionar como una sección independiente dentro de la vitrina.

**Unidad 1 → Cara / sección 1 de la vitrina**

**Unidad 2 → Cara / sección 2 de la vitrina**

**Unidad 3 → Cara / sección 3 de la vitrina**

**Unidad 4 → Cara / sección 4 de la vitrina**

**Unidad 5 → Cara / sección 5 de la vitrina**

### **C. Subobjetos**

4 productos destacados por cada unidad de negocio

Cada producto debe ser un elemento separado, no fusionado con el fondo ni con la vitrina.

Cada producto destacado debe poder identificarse individualmente para permitir interacción.

Ejemplo de nomenclatura:

        **U1\_P1**  
        **U1\_P2**  
        **U1\_P3**  
        **U1\_P4**

        **U2\_P1**  
        **U2\_P2**  
        **U2\_P3**  
        **U2\_P4**

        **U3\_P1**  
        **U3\_P2**  
        **U3\_P3**  
        **U3\_P4**

        **U4\_P1**  
        **U4\_P2**  
        **U4\_P3**  
        **U4\_P4**

        **U5\_P1**  
        **U5\_P2**  
        **U5\_P3**  
        **U5\_P4**

          
        **Donde:**   
        U \= Unidad de negocio  
        P \= Producto destacado

##      **4\. Comportamiento de rotación**

La vitrina tendrá 5 posiciones principales, una por cada unidad de negocio.

Como son 5 unidades, el giro completo de 360° se divide en 5 partes:

**360° / 5 \= 72°**

         Por lo tanto, cada unidad ocupará una posición principal de rotación. 

         Unidad 1 al frente → 0°  
         Unidad 2 al frente → 72°  
         Unidad 3 al frente → 144°  
         Unidad 4 al frente → 216°  
         Unidad 5 al frente → 288°

Cuando una unidad queda completamente al frente durante una pausa, se convierte en la       unidad activa. 

## **5\. Estado por defecto de la vitrina / modo screen saver**

La vitrina por defecto estará en modo screen saver o inactividad.

Esto significa que, al cargar la experiencia o cuando no exista interacción del usuario durante un tiempo definido, la vitrina girará automáticamente como animación de atracción visual.

Mientras la vitrina esté en modo screen saver:  
\- no se muestran productos destacados  
\- no hay productos clickeables  
\- no se pueden abrir modales  
\- no se permite navegación hacia Unidad de Tratamiento

Cuando el usuario toque la pantalla, haga swipe o arrastre la vitrina, el modo screen saver se desactiva y comienza el flujo interactivo normal.

## **6\. Cambio de productos destacados**

Los productos destacados deben actualizarse cuando la vitrina se detiene y una unidad de negocio queda completamente al frente.

El momento clave del cambio es el estado de detención / snap, porque allí el sistema confirma cuál es la unidad activa y carga los productos correspondientes.

### Comportamiento esperado

1\. La vitrina puede iniciar girando automáticamente y sutilmente.  
2\. Mientras está en screen saver o en rotación, no se muestran productos destacados.  
3\. Cuando el usuario toca, arrastra o hace swipe, se activa el flujo interactivo.  
4\. La vitrina se detiene temporalmente frente a una unidad de negocio.  
5\. Esa unidad se convierte en la unidad activa.  
6\. Se cargan los 4 productos destacados de esa unidad.  
7\. Los productos aparecen en sus slots correspondientes.  
8\. Los productos quedan clickeables.  
9\. Si el usuario hace click en un producto destacado, se abre el modal del producto.  
10\. Si el usuario hace click en la unidad de negocio activa, navega a la clase terapeutica correspondiente.  
11\. Cuando la vitrina vuelve a girar, los productos desaparecen.  
12\. Si no hay interacción durante un tiempo definido, puede volver al modo screen saver.

### **Regla principal**

Como la vitrina puede girar por defecto los productos destacados no estarán visibles durante la rotación.

## 7\. Comportamiento durante el giro

Mientras la vitrina está girando, los productos destacados no deben permanecer activos visual ni funcionalmente.

### 

### **Al iniciar el giro**

Productos actuales:  
\- hacen fade out  
\- desaparecen de la escena  
\- quedan sin interacción  
\- no pueden abrir modal

**Durante el giro**   
\- solo se muestra la vitrina rotando  
\- no se muestran productos destacados  
\- no existen productos clickeables  
\- no se permite abrir modal de producto

La actualización definitiva de productos ocurre cuando la vitrina se detiene, hace snap en la unidad activa y confirma qué unidad de negocio quedó al frente. 

## **8\. Interacción touch / drag**

Activación por touch

Cuando la vitrina esté en modo screen saver, el primer touch, swipe o drag del usuario debe sacar la experiencia del modo de inactividad.

A partir de ese momento, la vitrina inicia el flujo interactivo normal.

Durante este flujo:

\- la vitrina puede girar por acción del usuario

\- puede moverse mediante touch, swipe o drag

\- no se muestran productos destacados mientras el cilindro esté en movimiento

\- no se permiten clicks sobre productos mientras la vitrina esté girando

\- no se permite navegación hacia Unidad de Tratamiento mientras la vitrina esté girando

A. Movimiento manual por usuario

El usuario puede mover la vitrina con touch, drag o swipe.

Al iniciar el movimiento manual, los productos destacados deben desaparecer y quedar sin interacción.

Al soltar, la vitrina no debe quedar en una posición intermedia. Debe ajustarse automáticamente a la unidad de negocio más cercana a la posición frontal.

Este comportamiento se llama snap o autoajuste de posición frontal.

B. Autoajuste de posición frontal

Cuando el usuario suelta la vitrina, el sistema debe calcular cuál unidad de negocio está más próxima a la posición frontal.

Luego, el cilindro debe autoregular su posición y alinearse automáticamente hacia esa unidad.

Flujo esperado:

Usuario toca / arrastra / hace swipe sobre la vitrina

↓

El modo screen saver se desactiva

↓

La vitrina gira libremente por acción del usuario

↓

Los productos destacados desaparecen

↓

Usuario suelta la vitrina

↓

Sistema detecta la unidad de negocio más cercana a la posición frontal

↓

La vitrina se ajusta automáticamente hacia esa unidad

↓

Esa unidad queda posicionada al frente

↓

La base de la unidad se ilumina sutilmente

↓

Aparece el nombre de la unidad en la base o zona inferior

↓

La unidad queda disponible para touch hacia su clase terapéutica / Unidad de Tratamiento

C. Giro automático sutil

Después de la interacción del usuario, si no existe nueva actividad durante el tiempo definido, la vitrina puede volver a girar lentamente como al inicio.

Durante el giro automático sutil:

\- no se muestran productos destacados

\- no hay productos clickeables

\- no se abren modales

\- no se permite navegación hacia Unidad de Tratamiento

## **9\. Regla de unidad activa**

En todo momento debe existir una sola unidad activa.

La unidad activa es la unidad de negocio que queda completamente al frente de la vitrina después de que el cilindro se detiene, hace snap o autorregula su posición.

Cuando el usuario mueve la vitrina mediante touch, swipe o drag, el sistema debe calcular cuál unidad de negocio está más aproximada a la posición frontal. Al soltar la vitrina, el cilindro debe alinearse automáticamente hacia esa unidad.

Unidad activa \= unidad de negocio visible completamente al frente después del snap o autoajuste.

Los productos destacados visibles siempre deben corresponder únicamente a la unidad activa.

Si la Unidad 3 está al frente:  
solo se muestran productos destacados de la Unidad 3\.

Durante la rotación no debe considerarse ninguna unidad como completamente activa hasta que la vitrina se detenga, haga snap o finalice el autoajuste hacia la unidad más cercana.

Cuando una unidad queda activa al frente:  
\- se puede iluminar la base de esa unidad de forma sutil  
\- puede mostrarse el nombre de la unidad en la base o zona inferior  
\- la unidad queda disponible para touch hacia su clase terapéutica / Unidad de Tratamiento

## **10\. Slots de productos destacados**

Cada unidad de negocio debe tener **4 slots visuales** para productos destacados.

Estos slots son posiciones predefinidas dentro del diseño.

Ejemplo:

**Slot 1 → Producto destacado superior izquierdo**

**Slot 2 → Producto destacado superior derecho**

**Slot 3 → Producto destacado inferior izquierdo**

**Slot 4 → Producto destacado inferior derecho**

O según el diseño final: 

**Slot 1 → Producto principal frontal**  
**Slot 2 → Producto lateral izquierdo**  
**Slot 3 → Producto lateral derecho**  
**Slot 4 → Producto superior / flotante**

Lo importante es que los slots sean consistentes.

El administrador cambia los productos, pero no debería cambiar libremente la posición visual.  
La posición debe depender del slot.

## **11\. Click en producto destacado**

Cada producto destacado será clickeable.

Al hacer click sobre un producto, debe abrirse un modal con la información específica de ese producto.

### **El modal debe mostrar:**

\- Nombre del producto  
\- Imagen del producto  
\- Descripción corta  
\- Descripción larga, si aplica  
\- Imagen 3D o modelo 3D  
\- Unidad de negocio asociada  
\- Botón o acción adicional, si aplica

Cada subobjeto debe tener identidad propia para que el sistema sepa qué información cargar en el modal.

Durante la rotación, los productos no estarán visibles ni activos, por lo tanto no podrán abrir modales.

## **12\. Click en unidad de negocio**

Cada **unidad de negocio** también debe ser un objeto único, independiente e interactivo.

Cuando el usuario haga click sobre una unidad de negocio activa, el sistema debe llevarlo a una sección específica llamada **Unidad de Tratamiento**.

Debe existir una **Clase terapeutica por cada unidad de negocio**.

**Unidad de negocio 1 → Unidad de Tratamiento 1**

**Unidad de negocio 2 → Unidad de Tratamiento 2**

**Unidad de negocio 3 → Unidad de Tratamiento 3**

**Unidad de negocio 4 → Unidad de Tratamiento 4**

**Unidad de negocio 5 → Unidad de Tratamiento 5**

La unidad de negocio solo debe ser clickeable cuando esté completamente al frente, durante el estado de pausa o detención frontal de la vitrina.

Durante la rotación, la unidad de negocio no debe ejecutar navegación para evitar acciones accidentales.

**Comportamiento esperado** 

1\. La vitrina gira automáticamente.

2\. Mientras gira, no hay productos destacados visibles.

3\. La vitrina se detiene frente a una unidad de negocio.

4\. Esa unidad queda como unidad activa.

5\. Aparecen sus 4 productos destacados.

6\. Si el usuario hace click en un producto destacado:

   → se abre el modal del producto.

7\. Si el usuario hace click en la unidad de negocio:

   → se navega a la Unidad de Tratamiento correspondiente.

### Regla funcional

Al usuario estar posicionado en cada unidad de negocio, en la parte inferior aparecerá el nombre de la unidad de negocio (en la base) esta estará resaltada con un tono más resaltante de forma sutil (aparecerá y aparecerá de forma fade in) siendo un indicativo al usuario para dar touch y dirigirse a una clase terapéutica.

## **13\. Comportamiento del modal**

Cuando el usuario hace touch sobre un producto destacado, se abre un modal con la información específica del producto.

         El modal debe mostrarse únicamente cuando:

\- la vitrina esté detenida

\- exista una unidad activa al frente

\- los productos destacados estén visibles

\- el producto seleccionado esté habilitado para interacción

          Estado recomendado al abrir modal:

Producto destacado visible

↓

Usuario hace click

↓

Se abre modal del producto

↓

La vitrina permanece pausada o bloqueada

↓

Se desactiva la rotación automática

↓

El usuario revisa la información, imagen o modelo 3D del producto

↓

Al cerrar el modal, la vitrina puede continuar su flujo normal

Regla funcional del modal

Cuando el modal esté abierto, la vitrina debe permanecer pausada o bloquear la rotación automática hasta que el usuario cierre el modal.

Mientras el modal esté abierto:

\- no debe cambiar la unidad activa

\- no deben cambiar los productos destacados

\- no debe ejecutarse navegación hacia otra unidad

\- no debe activarse el modo screen saver

\- no debe reiniciarse el giro automático sutil

Al cerrar el modal:

\- se cierra el producto activo

\- se mantiene la unidad activa actual

\- se puede reanudar el conteo de inactividad

\- la vitrina puede continuar su ciclo según las reglas de temporización definidas 

## **14\. Reglas para el diseñador / vectorizador**

El diseñador debe entregar la vitrina separada por capas, objetos o componentes independientes.

La vitrina no debe entregarse como una sola imagen plana, porque debe permitir rotación, interacción, autoajuste, iluminación de unidad activa, productos destacados dinámicos y navegación hacia clases terapéuticas.

No entregar como una sola imagen plana

Debe evitarse:

\- una imagen única fusionada

\- productos pegados al fondo

\- productos no identificables

\- unidades de negocio mezcladas en una sola capa

\- productos sin identidad individual

\- bases de unidades no separadas

\- zonas clickeables sin nombre o sin identificación

\- elementos visuales que no puedan animarse por separado

Entregar separado por elementos

Debe entregarse algo similar a esto:

Vitrina\_Base

Cilindro\_3D\_Base

Base\_Interactiva\_General

Unidad\_1\_clickable

Unidad\_1\_base\_iluminable

Unidad\_1\_nombre\_base

Unidad\_2\_clickable

Unidad\_2\_base\_iluminable

Unidad\_2\_nombre\_base

Unidad\_3\_clickable

Unidad\_3\_base\_iluminable

Unidad\_3\_nombre\_base

Unidad\_4\_clickable

Unidad\_4\_base\_iluminable

Unidad\_4\_nombre\_base

Unidad\_5\_clickable

Unidad\_5\_base\_iluminable

Unidad\_5\_nombre\_base

U1\_P1\_clickable

U1\_P2\_clickable

U1\_P3\_clickable

U1\_P4\_clickable

U2\_P1\_clickable

U2\_P2\_clickable

U2\_P3\_clickable

U2\_P4\_clickable

U3\_P1\_clickable

U3\_P2\_clickable

U3\_P3\_clickable

U3\_P4\_clickable

U4\_P1\_clickable

U4\_P2\_clickable

U4\_P3\_clickable

U4\_P4\_clickable

U5\_P1\_clickable

U5\_P2\_clickable

U5\_P3\_clickable

U5\_P4\_clickable

Además de los productos destacados, cada unidad de negocio debe estar separada como objeto independiente, porque tendrá una acción propia: navegar hacia su respectiva clase terapéutica / Unidad de Tratamiento.

Cada unidad de negocio debe tener también una base o zona inferior separada, porque esa base será utilizada como indicador visual de selección. Cuando la unidad quede posicionada al frente, la base debe poder iluminarse con un tono resaltante y una animación sutil de encendido y apagado o fade in / fade out.

Por lo tanto, el diseñador debe entregar al menos estos tipos de objetos interactivos y animables:

\- Unidad de negocio clickeable.

\- Producto destacado clickeable.

\- Base iluminable por unidad de negocio.

\- Nombre de unidad visible en la base o zona inferior.

\- Slots visuales para productos destacados.

\- Elementos del cilindro o vitrina separados para permitir rotación.

Regla visual

La base iluminada no debe competir con los productos ni con la vitrina. Debe funcionar como una señal clara, elegante y sutil para indicar que la unidad está seleccionada y puede tocarse.

El nombre de la unidad debe ser legible, estar asociado visualmente a la base iluminada y servir como guía para que el usuario entienda que puede tocar esa unidad para ir a su clase terapéutica correspondiente. 

---

## **15\. Reglas visuales para productos destacados**

Cada producto destacado debe cumplir con reglas visuales y funcionales claras para garantizar buena legibilidad, correcta interacción y coherencia con su unidad de negocio.

Cada producto destacado debe tener:

\- posición definida por slot

\- tamaño proporcional respecto al espacio disponible

\- identidad visual clara

\- separación suficiente del fondo

\- área clickeable individual

\- relación visual directa con su unidad de negocio

\- jerarquía visual adecuada dentro de la composición

\- buena legibilidad si incluye nombre, etiqueta o información complementaria

Los productos destacados no deben verse como parte fija del fondo ni confundirse con elementos decorativos de la vitrina.

Reglas de visibilidad

Los productos destacados deben aparecer únicamente cuando su unidad de negocio esté activa al frente.

Mientras la vitrina esté girando:

\- los productos destacados no deben estar visibles

\- no deben ser clickeables

\- no deben abrir modal

\- no deben competir visualmente con el movimiento del cilindro

Al iniciar la rotación:

\- los productos destacados deben desaparecer de forma limpia

\- pueden hacerlo mediante fade out u otra transición visual suave

\- deben quedar sin interacción inmediatamente

Al finalizar la rotación o el autoajuste:

\- la unidad activa queda posicionada al frente

\- se pueden mostrar los productos destacados correspondientes a esa unidad

\- los productos aparecen en los slots asignados

\- los productos quedan habilitados para interacción

Relación con la unidad activa

Los productos destacados visibles siempre deben corresponder únicamente a la unidad activa.

Si la Unidad 3 está posicionada al frente:

\- solo se muestran productos de la Unidad 3

\- no deben mostrarse productos de ninguna otra unidad

Relación con la base iluminada

Cuando una unidad quede activa al frente, la base de esa unidad puede iluminarse de forma sutil y mostrar el nombre de la unidad.

Los productos destacados deben integrarse visualmente con esa unidad activa, sin impedir que la base siga funcionando como señal de orientación para el usuario.

Regla visual general

La aparición y desaparición de productos debe ser elegante, limpia y consistente con el movimiento de la vitrina.

Los productos destacados deben reforzar la comprensión de la unidad de negocio activa y facilitar la interacción del usuario, tanto para abrir modales de producto como para mantener coherencia visual dentro de la experiencia.

## **16\. Reglas para el programador**

El programador debe manejar la vitrina como un carrusel 3D interactivo con estados controlados.

La lógica debe contemplar:  
\- modo screen saver  
\- rotación automática sutil  
\- interacción touch / drag / swipe  
\- autoajuste hacia la unidad más cercana al frente  
\- unidad activa  
\- productos destacados dinámicos  
\- modal de producto  
\- navegación hacia clase terapéutica / Unidad de Tratamiento  
\- temporización de 2 minutos y 3 minutos

Variables funcionales principales

currentBusinessUnit  
activeBusinessUnit  
rotationAngle  
isRotating  
isPaused  
featuredProducts  
activeProduct  
activeSlot  
modalOpen  
businessUnitTargetSection  
isScreenSaver  
lastUserInteraction  
idleTimeout  
frontSnapTarget  
autoRotateTimeout  
screenSaverTimeout  
isAutoRotatingSubtle  
isUserDragging

Descripción de variables

currentBusinessUnit:  
Unidad de negocio actualmente asociada al estado visual de la vitrina.

activeBusinessUnit:  
Unidad de negocio que quedó completamente al frente después del snap o autoajuste.

rotationAngle:  
Ángulo actual de rotación del cilindro.

isRotating:  
Indica si la vitrina está girando.

isPaused:  
Indica si la vitrina está detenida temporalmente.

featuredProducts:  
Lista de productos destacados asociados a la unidad activa.

activeProduct:  
Producto seleccionado por el usuario para abrir modal.

activeSlot:  
Slot visual ocupado por cada producto destacado.

modalOpen:  
Indica si existe un modal de producto abierto.

businessUnitTargetSection:  
Sección destino o clase terapéutica asociada a la unidad de negocio activa.

isScreenSaver:  
Indica si la vitrina está en modo screen saver.

lastUserInteraction:  
Fecha/hora de la última interacción del usuario.

idleTimeout:  
Tiempo general de inactividad.

frontSnapTarget:  
Unidad de negocio más cercana a la posición frontal calculada al soltar la vitrina.

autoRotateTimeout:  
Tiempo de espera de 2 minutos para volver a rotación lenta y sutil.

screenSaverTimeout:  
Tiempo de espera de 3 minutos para volver automáticamente al modo screen saver.

isAutoRotatingSubtle:  
Indica si el cilindro está girando lentamente como animación sutil.

isUserDragging:  
Indica si el usuario está moviendo manualmente la vitrina.

Flujo funcional

1\. Iniciar la experiencia en modo screen saver si no hay interacción del usuario.  
2\. Mantener la vitrina girando automáticamente como animación de atracción visual.  
3\. Mantener productos destacados ocultos y clicks bloqueados en modo screen saver.  
4\. Bloquear modales y navegación hacia clase terapéutica durante el screen saver.  
5\. Al detectar touch, swipe o drag del usuario, salir del modo screen saver.  
6\. Iniciar el flujo interactivo normal.  
7\. Permitir que el usuario mueva la vitrina mediante touch, swipe o drag.  
8\. Mientras el usuario mueve la vitrina, ocultar productos destacados y bloquear clicks.  
9\. Al finalizar el movimiento del usuario, calcular la unidad de negocio más cercana a la posición frontal.  
10\. Definir esa unidad como frontSnapTarget.  
11\. Autoregular el cilindro hacia esa unidad.  
12\. Alinear la unidad seleccionada al frente.  
13\. Definir esa unidad como activeBusinessUnit.  
14\. Iluminar la base de la unidad activa.  
15\. Mostrar el nombre de la unidad activa en la base o zona inferior.  
16\. Consultar los productos destacados asociados a esa unidad.  
17\. Renderizar los productos destacados en los 4 slots correspondientes.  
18\. Activar clicks sobre los productos destacados.  
19\. Activar click sobre la unidad de negocio activa.  
20\. Si el usuario hace click en un producto destacado, abrir modal del producto.  
21\. Si el usuario hace click en la unidad de negocio activa, navegar hacia su clase terapéutica / Unidad de Tratamiento.  
22\. Si el modal está abierto, mantener la vitrina pausada y bloquear rotación.  
23\. Al cerrar el modal, permitir continuar el flujo normal.  
24\. Si pasan 2 minutos sin nueva interacción, reiniciar rotación lenta y sutil.  
25\. Durante la rotación lenta y sutil, ocultar productos destacados y bloquear clicks.  
26\. Si pasan 3 minutos sin ningún tipo de interacción en pantalla, activar nuevamente modo screen saver.  
27\. Repetir el ciclo.

Eventos funcionales principales

onTouchStart()  
→ salir del modo screen saver, si está activo  
→ registrar interacción del usuario  
→ bloquear productos destacados si la vitrina comienza a moverse

onDragStart()  
→ marcar isUserDragging \= true  
→ ocultar productos destacados  
→ desactivar clicks de productos y unidad

onDragEnd()  
→ marcar isUserDragging \= false  
→ calcular unidad más cercana al frente  
→ definir frontSnapTarget  
→ ejecutar autoajuste / snap  
→ activar unidad posicionada

onSnapComplete(unidad\_id)  
→ definir activeBusinessUnit  
→ iluminar base de la unidad  
→ mostrar nombre de la unidad en la base  
→ cargar productos destacados de la unidad  
→ activar clicks permitidos

onClickProduct(product\_id)  
→ validar que la vitrina esté detenida  
→ validar que exista unidad activa  
→ validar que el producto esté visible y habilitado  
→ abrir modal del producto

onClickBusinessUnit(unidad\_id)  
→ validar que la unidad esté activa al frente  
→ navegar / hacer scroll / abrir sección  
→ clase terapéutica / Unidad de Tratamiento correspondiente

onModalOpen(product\_id)  
→ pausar vitrina  
→ bloquear rotación automática  
→ bloquear cambio de unidad activa  
→ bloquear screen saver temporalmente

onModalClose()  
→ cerrar modal  
→ mantener unidad activa actual  
→ reanudar conteo de inactividad  
→ permitir continuidad del flujo

onAutoRotateTimeout()  
→ si pasan 2 minutos sin interacción  
→ ocultar productos destacados  
→ desactivar clicks  
→ iniciar rotación lenta y sutil

onScreenSaverTimeout()  
→ si pasan 3 minutos sin interacción total  
→ activar modo screen saver  
→ ocultar productos destacados  
→ bloquear clicks, modales y navegación 

**17\. Lógica simplificada**   
Si no hay interacción del usuario:  
activar modo screen saver  
girar vitrina automáticamente como animación de atracción  
ocultar productos destacados  
desactivar clicks de productos  
desactivar click sobre unidad de negocio  
impedir apertura de modal  
impedir navegación hacia clase terapéutica / Unidad de Tratamiento

Si usuario hace touch / swipe / drag:  
salir de modo screen saver  
registrar nueva interacción del usuario  
iniciar flujo interactivo normal

Si usuario comienza a mover la vitrina:  
ocultar productos destacados  
desactivar clicks de productos  
desactivar click sobre unidad de negocio  
impedir apertura de modal  
impedir navegación hacia clase terapéutica / Unidad de Tratamiento

Si el usuario suelta la vitrina después de moverla:  
calcular unidad de negocio más cercana a la posición frontal  
definir esa unidad como objetivo de snap / autoajuste  
autoregular posición del cilindro hacia esa unidad  
alinear la unidad más cercana al frente  
definir esa unidad como unidad activa  
iluminar la base de la unidad activa  
mostrar el nombre de la unidad activa en la base o zona inferior  
cargar productos destacados de esa unidad  
mostrar productos en sus slots  
activar clicks de productos  
activar click sobre la unidad de negocio activa

Si vitrina está girando:  
no mostrar productos destacados  
mantener clicks de productos desactivados  
bloquear navegación por click en unidad de negocio  
bloquear apertura de modal

Si vitrina se detiene y completa el snap:  
calcular / confirmar unidad activa  
iluminar la base de la unidad activa  
mostrar nombre de la unidad activa  
cargar productos destacados de esa unidad  
mostrar productos en sus slots  
activar clicks de productos  
activar click sobre unidad de negocio activa

Si usuario hace click en producto:  
validar que la vitrina esté detenida  
validar que exista unidad activa  
validar que el producto esté visible y habilitado  
abrir modal del producto  
pausar vitrina o bloquear rotación

Si usuario hace click en unidad de negocio activa:  
validar que la unidad esté al frente  
validar que la vitrina esté detenida  
navegar a la clase terapéutica / Unidad de Tratamiento correspondiente

Si usuario cierra modal:  
cerrar modal  
mantener unidad activa actual  
permitir que la vitrina continúe su ciclo  
reanudar conteo de inactividad

Si pasan 2 minutos sin nueva interacción:  
ocultar productos destacados  
desactivar clicks de productos  
desactivar click sobre unidad de negocio  
volver a rotación lenta y sutil

Si pasan 3 minutos sin ninguna interacción en pantalla:  
activar modo screen saver.

## **18\. Consideraciones administrativas**

Cada unidad de negocio tendrá asociados 4 productos destacados o productos estrella.

La ficha administrativa de cada producto ya existe, pero para este comportamiento se requiere que cada producto pueda asociarse correctamente a una unidad de negocio y a un slot visual específico dentro de la vitrina.

Cada producto destacado debe poder administrar al menos estos campos:

\- unidad de negocio asociada

\- slot visual asignado

\- orden de aparición

\- estado activo/inactivo

\- visibilidad en vitrina

\- imagen destacada

\- imagen o modelo 3D 

\- descripción corta para modal

\- descripción larga para modal, si aplica

\- enlace o acción adicional, si aplica

El administrador debe poder cambiar qué producto aparece en cada slot de cada unidad de negocio, sin modificar el diseño ni el código.

Ejemplo:

Unidad: Medicamentos

Slot 1 → Producto A

Slot 2 → Producto B

Slot 3 → Producto C

Slot 4 → Producto D

Cuando la vitrina cambie a otra unidad:

Unidad: Dermocosmética

Slot 1 → Producto E

Slot 2 → Producto F

Slot 3 → Producto G

Slot 4 → Producto H

Reglas administrativas

\- cada unidad de negocio debe tener hasta 4 productos destacados visibles

\- cada producto destacado debe pertenecer a una sola unidad de negocio dentro de la vitrina

\- cada producto destacado debe ocupar un slot definido

\- si un producto está inactivo, no debe mostrarse en la vitrina

\- si un slot no tiene producto asignado, debe permanecer vacío o usar un estado visual definido

\- el cambio de productos debe reflejarse cuando la unidad correspondiente quede activa al frente

\- los productos destacados no deben mostrarse durante la rotación, aunque estén activos en el administrador

Relación con la experiencia interactiva

Cuando una unidad de negocio queda activa al frente:

\- el sistema consulta los productos destacados asociados a esa unidad

\- ordena los productos según slot u orden definido

\- muestra los productos visibles en sus posiciones correspondientes

\- habilita el click de cada producto para abrir su modal

Cuando la vitrina vuelve a girar:

\- los productos destacados desaparecen

\- los clicks quedan bloqueados

\- no se permite abrir modal hasta que exista nuevamente una unidad activa al frente

Relación con clase terapéutica / Unidad de Tratamiento

Cada unidad de negocio debe estar asociada a una clase terapéutica o Unidad de Tratamiento correspondiente.

Esta asociación debe permitir que, al hacer click sobre la unidad de negocio activa, el usuario sea dirigido a la sección correcta.

Ejemplo:

Unidad de negocio 1 → Clase terapéutica / Unidad de Tratamiento 1

Unidad de negocio 2 → Clase terapéutica / Unidad de Tratamiento 2

Unidad de negocio 3 → Clase terapéutica / Unidad de Tratamiento 3

Unidad de negocio 4 → Clase terapéutica / Unidad de Tratamiento 4

Unidad de negocio 5 → Clase terapéutica / Unidad de Tratamiento 5 

## **19\. Estados funcionales de la vitrina**

La vitrina puede manejarse con seis estados principales:

Estado 0: Screen saver / inactividad

\- se activa al cargar la experiencia o cuando no hay interacción del usuario durante un tiempo definido  
\- la vitrina gira automáticamente como animación de atracción visual lento y sutilmente  
\- no se muestran productos destacados  
\- no hay productos clickeables  
\- no se abre modal  
\- no se permite navegación por click en unidad de negocio  
\- al primer touch, swipe o drag del usuario, se desactiva el modo screen saver  
\- luego comienza el flujo interactivo normal.

Estado 1: Rotación automática sutil

\- la vitrina gira de forma lenta y sutil  
\- puede activarse después de 2 minutos sin nueva interacción  
\- no se muestran productos destacados  
\- no hay productos clickeables  
\- no se abre modal  
\- no se permite navegación por click en unidad de negocio  
\- si no existe ninguna interacción durante 5 minutos, se activa el modo screen saver

Estado 2: Interacción manual / movimiento del usuario

\- el usuario mueve la vitrina mediante touch, swipe o drag hacia la derecha o hacia la izquierda  
\- los productos destacados desaparecen  
\- los productos no son clickeables  
\- no se permite abrir modal  
\- no se permite navegación hacia clase terapéutica / Unidad de Tratamiento  
\- al soltar la vitrina, el sistema calcula la unidad más cercana a la posición frontal  
\- la vitrina autoregula su posición hacia esa unidad

Estado 3: Espera interactiva / unidad posicionada

\- una unidad de negocio queda alineada al frente por snap o autoajuste  
\- esa unidad se convierte en la unidad activa  
\- la base de esa unidad se ilumina de forma sutil  
\- el nombre de la unidad aparece en la base o zona inferior  
\- se muestran los 4 productos destacados asociados a esa unidad, si están configurados y activos  
\- los productos son clickeables  
\- la unidad de negocio activa es clickeable  
\- click sobre producto destacado abre modal  
\- click sobre unidad activa lleva a su clase terapéutica / Unidad de Tratamiento  
\- si pasan 2 minutos sin interacción, la vitrina vuelve a rotación lenta y sutil  
\- si pasan 5 minutos sin interacción total en pantalla, vuelve al modo screen saver

Estado 4: Modal abierto

\- un producto fue seleccionado  
\- se muestra el modal del producto  
\- la vitrina permanece pausada o bloqueada  
\- no debe cambiar de unidad activa mientras el modal esté abierto  
\- no deben cambiar los productos destacados  
\- no debe ejecutarse navegación hacia otra unidad  
\- no debe activarse el modo screen saver  
\- al cerrar el modal, se reanuda el flujo normal y el conteo de inactividad

Estado 5: Reanudación de rotación

\- se cierra el modal, termina la pausa o se cumple el tiempo de espera  
\- los productos destacados desaparecen  
\- la vitrina vuelve a girar lentamente  
\- se desactivan los clicks de productos  
\- se desactiva el click sobre unidad de negocio  
\- se mantiene el control de inactividad para volver a screen saver si aplica 

## **20\. Resumen funcional**

La vitrina debe comportarse como un carrusel / cilindro 3D de 5 posiciones.

Cada posición corresponde a una unidad de negocio.

La experiencia debe contemplar un modo screen saver o inactividad. Cuando ningún usuario interactúa con la vitrina durante un tiempo definido, la vitrina gira automáticamente como animación de atracción visual, sin mostrar productos destacados ni permitir clicks. Al primer touch, swipe o drag del usuario, el modo screen saver se desactiva y comienza el flujo interactivo normal.

Durante el flujo interactivo, el usuario puede mover la vitrina mediante touch, swipe o drag. Mientras el cilindro esté en movimiento, no deben mostrarse productos destacados, no deben existir productos clickeables y tampoco debe ejecutarse navegación hacia clases terapéuticas o Unidades de Tratamiento.

Al soltar la vitrina, el sistema debe calcular cuál unidad de negocio está más aproximada a la posición frontal. Luego, el cilindro debe autoregular su posición, hacer snap y alinear esa unidad al frente.

Cuando una unidad queda posicionada al frente, se convierte en la unidad activa. La base de esa unidad debe iluminarse sutilmente y mostrar el nombre de la unidad en la base o zona inferior, indicando que puede tocarse para dirigir al usuario hacia su clase terapéutica o Unidad de Tratamiento.

Cuando la unidad activa queda confirmada, el sistema carga sus 4 productos destacados o productos estrella, siempre que estén configurados y activos en el administrador.

Cada producto destacado es un subobjeto único, clickeable e independiente. Al hacer click sobre un producto destacado, se abre un modal con la información del producto en modelo 3D acompañado por una ficha descriptiva con la descripción breve.

Además, cada unidad de negocio también debe ser un objeto clickeable independiente. Al hacer click sobre la unidad de negocio activa en la base iluminada, el usuario será llevado a la clase terapéutica / Unidad de Tratamiento correspondiente.

Si luego de posicionarse sobre una unidad no existe interacción durante 2 minutos, el cilindro volverá a girar como al inicio, de forma lenta y sutil. Durante esta rotación, los productos destacados desaparecen y quedan sin interacción.

Si no existe ningún tipo de interacción en la pantalla durante 3 minutos, el sistema volverá automáticamente al modo screen saver.

El diseño debe estar preparado por capas y objetos separados para permitir rotación, autoajuste, animación, iluminación de base, visualización del nombre de la unidad, interacción, administración, apertura de modales por producto y navegación hacia las clases terapéuticas o unidades de tratamiento.

## **21\. Frase técnica final para entregar al diseñador y programador**

La vitrina debe ser diseñada y construida como un sistema modular 3D compuesto por 5 unidades de negocio. Cada unidad representa una posición del carrusel / cilindro 3D y contiene 4 slots de productos destacados o productos estrella.

Adicionalmente, debe existir un modo screen saver para estados de inactividad. En este modo, la vitrina gira automáticamente como animación de atracción visual, sin mostrar productos destacados, sin habilitar clicks y sin permitir navegación hacia clases terapéuticas o Unidades de Tratamiento. Cuando el usuario toca, arrastra o interactúa con la vitrina, el modo screen saver se desactiva y comienza el flujo interactivo normal.

Durante el flujo interactivo, el usuario puede mover la vitrina mediante touch, swipe o drag. Mientras el cilindro esté en movimiento, no deben mostrarse productos destacados ni existir elementos clickeables asociados a productos. Tampoco debe ejecutarse navegación por click sobre unidades de negocio mientras la vitrina esté girando.

Cuando el usuario suelta la vitrina, el sistema debe calcular cuál unidad de negocio está más aproximada a la posición frontal. Luego, el cilindro debe autoregular su posición, hacer snap y alinear esa unidad al frente.

Cuando una unidad queda posicionada al frente, esa unidad se convierte en la unidad activa. La base de esa unidad debe iluminarse de forma sutil y mostrar el nombre de la unidad en la base o zona inferior, indicando que puede tocarse para dirigir al usuario hacia su clase terapéutica o Unidad de Tratamiento.

Cuando la unidad activa queda confirmada, aparecen sus 4 productos destacados, siempre que estén configurados y activos en el administrador. Cada producto debe ser un subobjeto único e independiente, clickeable, administrable y asociado a un modal propio.

Además, cada unidad de negocio activa debe ser clickeable como objeto independiente. Al hacer click sobre ella, el usuario será llevado a su clase terapéutica / Unidad de Tratamiento correspondiente.

Si no hay nueva interacción durante 2 minutos después de posicionarse sobre una unidad, el cilindro volverá a girar lentamente como al inicio, ocultando los productos destacados y bloqueando interacciones. Si no existe ningún tipo de interacción en la pantalla durante 3 minutos, se activará automáticamente el modo screen saver.


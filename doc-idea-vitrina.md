Lo haría como una vitrina 3D dinámica, donde el modelo .glb de la base giratoria es fijo, pero los productos se cargan desde el backend/admin como datos y assets 3D o imágenes, y luego Android los coloca en posiciones predefinidas dentro de la vitrina.

La idea correcta no es “dibujar productos manualmente” en Android, sino construir una arquitectura donde:

El admin registra productos.
El backend entrega una configuración de vitrina.
Android carga el .glb de la vitrina.
Android carga los productos activos.
El motor 3D posiciona cada producto en su slot correspondiente.
La base o vitrina puede rotar en 360°.
1. Arquitectura general propuesta
La vitrina debe funcionar como un escenario 3D.




2. Concepto clave: separar la vitrina de los productos
Tu modelo .glb de vitrina debería representar solo la estructura fija:

Base giratoria.
Estantes.
Cilindro o acrílico.
Separadores.
Ejes.
Materiales.
Luces o puntos decorativos, si aplica.
Los productos no deberían estar quemados dentro del .glb principal, salvo que sean decorativos fijos.

La vitrina sería el contenedor 3D, y los productos serían elementos dinámicos que se agregan encima.

text


vitrina.glb
│
├── Base giratoria
├── Acrílico / cápsula transparente
├── Estantes
├── Separador central
└── Slots vacíos donde se montan productos
Luego Android renderiza:

text


Producto 1 -> slot superior izquierdo
Producto 2 -> slot superior centro
Producto 3 -> slot superior derecho
Producto 4 -> slot medio izquierdo
Producto 5 -> slot medio derecho
3. Motor recomendado en Android
Para Android nativo, usaría Filament directamente o una librería encima de Filament.

Opciones recomendadas:

Opción	Uso recomendado
Filament	Máximo control 3D, producción seria.
SceneView / Filament wrapper	Más rápido de implementar en Android.
Unity como módulo	Si necesitas interacciones 3D complejas, animaciones avanzadas o equipo 3D dedicado.
WebView + Three.js	Útil si ya tienes experiencia web, pero menos nativo.
Mi recomendación para tu caso:

Android nativo + SceneView/Filament.

Porque tienes un .glb, necesitas cargar productos dinámicos, rotar una base y mantener buen rendimiento.

4. Modelo de datos del producto
Cada producto del backend debe traer suficiente información para saber:

Qué producto es.
Cómo se llama.
Qué categoría tiene.
Qué asset debe renderizarse.
Dónde va ubicado en la vitrina.
Qué escala, rotación y posición debe usar.
Si está activo o no.
Si se puede tocar.
Ejemplo de respuesta:

json


{
  "showcaseId": "main_showcase",
  "version": 12,
  "baseModelUrl": "[cdn.midominio.com](https://cdn.midominio.com/models/vitrina.glb)",
  "rotationEnabled": true,
  "products": [
    {
      "id": "prod_001",
      "name": "Producto Primary Care 1",
      "category": "PRIMARY_CARE",
      "assetType": "GLB",
      "assetUrl": "[cdn.midominio.com](https://cdn.midominio.com/products/prod_001.glb)",
      "slotId": "top_left_01",
      "position": {
        "x": -0.8,
        "y": 1.35,
        "z": 0.15
      },
      "rotation": {
        "x": 0,
        "y": 0,
        "z": 0
      },
      "scale": {
        "x": 0.25,
        "y": 0.25,
        "z": 0.25
      },
      "enabled": true
    }
  ]
}
Este JSON permite que la app renderice productos sin necesidad de actualizar la aplicación.

5. Dos enfoques para ubicar productos
Hay dos formas buenas de posicionar los productos.

6. Enfoque A: posiciones desde backend
En este enfoque, el backend envía las coordenadas exactas.

Ejemplo:

json


"position": {
  "x": -0.8,
  "y": 1.35,
  "z": 0.15
},
"rotation": {
  "x": 0,
  "y": 0,
  "z": 0
},
"scale": {
  "x": 0.25,
  "y": 0.25,
  "z": 0.25
}
Ventajas:

El admin tiene control total.
Puedes ajustar cada producto individualmente.
Sirve si los productos tienen tamaños muy distintos.
No necesitas publicar una nueva app para cambiar posiciones.
Desventajas:

El admin necesita una herramienta visual o mucha precisión.
Es más fácil cometer errores de ubicación.
Si no hay validación, un producto puede quedar flotando o atravesando la vitrina.
Este enfoque es potente, pero requiere un admin más avanzado.

7. Enfoque B: slots predefinidos en Android
En este enfoque, Android ya conoce los espacios disponibles de la vitrina.

Ejemplo:

kotlin


val showcaseSlots = mapOf(
    "top_left_01" to ShowcaseSlot(
        id = "top_left_01",
        position = Vector3(-0.8f, 1.35f, 0.15f),
        rotation = Vector3(0f, 0f, 0f),
        scale = Vector3(0.25f, 0.25f, 0.25f)
    ),
    "top_center_01" to ShowcaseSlot(
        id = "top_center_01",
        position = Vector3(0f, 1.35f, 0.15f),
        rotation = Vector3(0f, 0f, 0f),
        scale = Vector3(0.25f, 0.25f, 0.25f)
    ),
    "middle_right_01" to ShowcaseSlot(
        id = "middle_right_01",
        position = Vector3(0.75f, 0.65f, 0.15f),
        rotation = Vector3(0f, 0f, 0f),
        scale = Vector3(0.25f, 0.25f, 0.25f)
    )
)
El backend solo manda:

json


{
  "productId": "prod_001",
  "slotId": "top_left_01"
}
Ventajas:

Más seguro.
Más fácil de mantener.
Evita productos mal posicionados.
El admin solo elige un slot visual.
Ideal para una vitrina con espacios definidos.
Desventajas:

Menos flexible.
Si cambia el modelo .glb, posiblemente hay que actualizar slots.
Para tu caso, yo elegiría este enfoque inicialmente.

8. Enfoque recomendado: híbrido
La mejor solución es híbrida:

Android tiene slots base predefinidos.
El backend puede enviar ajustes finos opcionales.
Ejemplo:

json


{
  "id": "prod_001",
  "slotId": "top_left_01",
  "offset": {
    "x": 0.02,
    "y": 0,
    "z": -0.01
  },
  "scaleMultiplier": 1.1,
  "rotationOffset": {
    "x": 0,
    "y": 8,
    "z": 0
  }
}
Así tienes seguridad y flexibilidad.

La app toma el slot base:

kotlin


val baseSlot = slots[product.slotId]
Y luego aplica ajustes:

kotlin


finalPosition = baseSlot.position + product.offset
finalScale = baseSlot.scale * product.scaleMultiplier
finalRotation = baseSlot.rotation + product.rotationOffset
Este sería el diseño más profesional.

9. Estructura recomendada en backend
El backend debería tener estas entidades principales:

text


Product
Showcase
ShowcaseSlot
ShowcaseAssignment
Asset
Category
10. Entidad Product
Representa el producto comercial.

json


{
  "id": "prod_001",
  "name": "Dermacare",
  "category": "PRIMARY_CARE",
  "description": "Producto de cuidado primario",
  "active": true
}
Campos recomendados:

Campo	Tipo	Descripción
id	String	Identificador del producto.
name	String	Nombre comercial.
category	String	Línea del producto.
description	String	Descripción corta.
active	Boolean	Define si se muestra o no.
11. Entidad Asset
Representa el recurso visual del producto.

json


{
  "id": "asset_001",
  "productId": "prod_001",
  "type": "GLB",
  "url": "[cdn.midominio.com](https://cdn.midominio.com/products/dermacare.glb)",
  "thumbnailUrl": "[cdn.midominio.com](https://cdn.midominio.com/products/dermacare.png)",
  "sizeKb": 420
}
Campos recomendados:

Campo	Tipo	Descripción
type	String	GLB, IMAGE, CARD_2D, etc.
url	String	URL del modelo o imagen.
thumbnailUrl	String	Imagen para admin o fallback.
sizeKb	Number	Peso del asset.
12. Entidad Showcase
Representa la vitrina.

json


{
  "id": "showcase_home",
  "name": "Vitrina principal",
  "baseModelUrl": "[cdn.midominio.com](https://cdn.midominio.com/models/vitrina.glb)",
  "active": true,
  "version": 15
}
Campos recomendados:

Campo	Tipo	Descripción
id	String	Identificador de la vitrina.
name	String	Nombre administrativo.
baseModelUrl	String	URL del .glb de vitrina.
active	Boolean	Define si esta vitrina se usa actualmente.
version	Number	Permite cachear e invalidar datos.
13. Entidad ShowcaseSlot
Representa una posición disponible dentro de la vitrina.

json


{
  "id": "top_left_01",
  "showcaseId": "showcase_home",
  "label": "Superior izquierda 1",
  "position": {
    "x": -0.8,
    "y": 1.35,
    "z": 0.15
  },
  "rotation": {
    "x": 0,
    "y": 0,
    "z": 0
  },
  "scale": {
    "x": 0.25,
    "y": 0.25,
    "z": 0.25
  },
  "enabled": true
}
Esta entidad puede vivir en backend o en Android.

Si quieres que el admin pueda modificar layout sin actualizar app, debe vivir en backend.

14. Entidad ShowcaseAssignment
Representa qué producto va en qué slot.

json


{
  "id": "assignment_001",
  "showcaseId": "showcase_home",
  "productId": "prod_001",
  "slotId": "top_left_01",
  "offset": {
    "x": 0,
    "y": 0,
    "z": 0
  },
  "scaleMultiplier": 1,
  "rotationOffset": {
    "x": 0,
    "y": 0,
    "z": 0
  },
  "visible": true,
  "sortOrder": 1
}
Este modelo es muy útil porque separa el producto de su ubicación en la vitrina.

Un mismo producto puede existir en varias vitrinas o cambiar de posición sin alterar su información comercial.

15. API recomendada
Yo expondría una API específica para renderizar la vitrina:

http


GET /api/showcases/home
Respuesta:

json


{
  "id": "showcase_home",
  "name": "Vitrina principal",
  "version": 15,
  "baseModelUrl": "[cdn.midominio.com](https://cdn.midominio.com/models/vitrina.glb)",
  "settings": {
    "rotationEnabled": true,
    "autoRotate": true,
    "rotationSpeed": 0.35
  },
  "slots": [
    {
      "id": "top_left_01",
      "position": { "x": -0.8, "y": 1.35, "z": 0.15 },
      "rotation": { "x": 0, "y": 0, "z": 0 },
      "scale": { "x": 0.25, "y": 0.25, "z": 0.25 }
    }
  ],
  "items": [
    {
      "id": "assignment_001",
      "product": {
        "id": "prod_001",
        "name": "Dermacare",
        "category": "PRIMARY_CARE",
        "asset": {
          "type": "GLB",
          "url": "[cdn.midominio.com](https://cdn.midominio.com/products/dermacare.glb)",
          "thumbnailUrl": "[cdn.midominio.com](https://cdn.midominio.com/products/dermacare.png)"
        }
      },
      "slotId": "top_left_01",
      "offset": { "x": 0, "y": 0, "z": 0 },
      "scaleMultiplier": 1,
      "rotationOffset": { "x": 0, "y": 0, "z": 0 },
      "visible": true
    }
  ]
}
La app solo necesita consumir este endpoint para construir la escena completa.

16. Flujo en Android
El flujo interno sería:







Motor 3D
Backend API
ShowcaseViewModel
Android UI
Motor 3D
Backend API
ShowcaseViewModel
Android UI
Solicita vitrina
GET /api/showcases/home
Configuración + productos + slots
Estado Loading / Success
Cargar vitrina.glb
Cargar productos activos
Posicionar productos en slots
Activar rotación
17. Capas en Android
Lo organizaría así:

text


data/
├── remote/
│   ├── ShowcaseApi.kt
│   └── dto/
│       ├── ShowcaseResponseDto.kt
│       ├── ShowcaseSlotDto.kt
│       └── ShowcaseItemDto.kt
│
├── repository/
│   └── ShowcaseRepository.kt
│
domain/
├── model/
│   ├── Showcase.kt
│   ├── ShowcaseProduct.kt
│   ├── ShowcaseSlot.kt
│   └── Transform3D.kt
│
ui/
├── showcase/
│   ├── ShowcaseScreen.kt
│   ├── ShowcaseViewModel.kt
│   └── Showcase3DView.kt
Separaría claramente:

Capa	Responsabilidad
remote	Consume API.
repository	Maneja datos, cache y transformación.
domain	Modelos limpios usados por la app.
ui	Renderiza la pantalla.
Showcase3DView	Encapsula todo lo relacionado con 3D.
18. Modelos Kotlin sugeridos
kotlin


data class Showcase(
    val id: String,
    val version: Int,
    val baseModelUrl: String,
    val settings: ShowcaseSettings,
    val slots: List<ShowcaseSlot>,
    val items: List<ShowcaseItem>
)
data class ShowcaseSettings(
    val rotationEnabled: Boolean,
    val autoRotate: Boolean,
    val rotationSpeed: Float
)
data class ShowcaseSlot(
    val id: String,
    val position: Vector3Value,
    val rotation: Vector3Value,
    val scale: Vector3Value
)
data class ShowcaseItem(
    val id: String,
    val product: ShowcaseProduct,
    val slotId: String,
    val offset: Vector3Value,
    val scaleMultiplier: Float,
    val rotationOffset: Vector3Value,
    val visible: Boolean
)
data class ShowcaseProduct(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val asset: ProductAsset
)
data class ProductAsset(
    val type: AssetType,
    val url: String,
    val thumbnailUrl: String?
)
data class Vector3Value(
    val x: Float,
    val y: Float,
    val z: Float
)
enum class ProductCategory {
    PRIMARY_CARE,
    SPECIALTY_CARE
}
enum class AssetType {
    GLB,
    IMAGE,
    CARD_2D
}
19. ViewModel sugerido
kotlin


class ShowcaseViewModel(
    private val repository: ShowcaseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ShowcaseUiState>(ShowcaseUiState.Loading)
    val uiState: StateFlow<ShowcaseUiState> = _uiState
    fun loadShowcase(showcaseId: String) {
        viewModelScope.launch {
            _uiState.value = ShowcaseUiState.Loading
            runCatching {
                repository.getShowcase(showcaseId)
            }.onSuccess { showcase ->
                _uiState.value = ShowcaseUiState.Success(showcase)
            }.onFailure { error ->
                _uiState.value = ShowcaseUiState.Error(
                    message = error.message ?: "No se pudo cargar la vitrina"
                )
            }
        }
    }
}
sealed interface ShowcaseUiState {
    data object Loading : ShowcaseUiState
    data class Success(val showcase: Showcase) : ShowcaseUiState
    data class Error(val message: String) : ShowcaseUiState
}
20. Renderizado Compose de alto nivel
La pantalla Compose quedaría así:

kotlin


@Composable
fun ShowcaseScreen(
    viewModel: ShowcaseViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        ShowcaseUiState.Loading -> {
            ShowcaseLoading()
        }
        is ShowcaseUiState.Error -> {
            ShowcaseError(message = state.message)
        }
        is ShowcaseUiState.Success -> {
            Showcase3DView(
                showcase = state.showcase,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
La pantalla no debería saber cómo cargar .glb. Solo entrega el modelo Showcase al componente 3D.

21. Componente Showcase3DView
Este componente encapsularía el motor 3D.

Conceptualmente:

kotlin


@Composable
fun Showcase3DView(
    showcase: Showcase,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ShowcaseRendererView(context).apply {
                loadBaseModel(showcase.baseModelUrl)
                renderProducts(showcase)
                setAutoRotation(
                    enabled = showcase.settings.autoRotate,
                    speed = showcase.settings.rotationSpeed
                )
            }
        },
        update = { view ->
            view.updateShowcase(showcase)
        }
    )
}
Aquí ShowcaseRendererView sería una clase propia que maneja:

Escena 3D.
Cámara.
Luces.
Carga del .glb.
Carga de productos.
Posicionamiento.
Rotación.
Eventos táctiles.
22. Lógica para renderizar productos
La lógica principal sería:

kotlin


fun renderProducts(showcase: Showcase) {
    val slotsById = showcase.slots.associateBy { it.id }
    showcase.items
        .filter { it.visible }
        .forEach { item ->
            val slot = slotsById[item.slotId] ?: return@forEach
            val finalPosition = slot.position + item.offset
            val finalRotation = slot.rotation + item.rotationOffset
            val finalScale = slot.scale * item.scaleMultiplier
            loadProductAsset(
                asset = item.product.asset,
                position = finalPosition,
                rotation = finalRotation,
                scale = finalScale
            )
        }
}
Conceptualmente, cada producto se renderiza según esta fórmula:

text


transformación final = slot base + ajustes del assignment
23. Manejo de productos en formato GLB
Si cada producto viene como .glb, la carga sería:

text


producto.asset.url -> cargar modelo 3D -> posicionar -> agregar a escena
Ventajas:

Máxima calidad visual.
Permite volumen real.
Funciona mejor con rotación 360°.
Se ve más realista dentro de la vitrina.
Desventajas:

Mayor peso.
Mayor complejidad.
Hay que optimizar modelos.
Requiere control de escala.
Recomendación:

Cada producto .glb debería:

Estar centrado en su origen.
Tener escala consistente.
Estar orientado mirando hacia el frente.
Usar texturas comprimidas.
Tener pocos polígonos.
No incluir luces ni cámaras.
No incluir animaciones innecesarias.
24. Manejo de productos como imágenes 2D
Si no tienes modelos .glb para todos los productos, puedes usar imágenes planas en 3D, tipo billboard.

Esto significa que cargas una imagen PNG/WebP del producto y la colocas sobre un plano 3D.

Ventajas:

Mucho más liviano.
Más fácil para el admin.
Más rápido de producir.
Ideal si solo necesitas vista frontal.
Desventajas:

Menos realista en rotación.
Al girar mucho, se nota que es plano.
No tiene volumen real.
Podrías usar este tipo:

json


"asset": {
  "type": "IMAGE",
  "url": "[cdn.midominio.com](https://cdn.midominio.com/products/dermacare.webp)"
}
Y Android renderiza un plano con textura.

25. Estrategia recomendada de assets
Yo soportaría tres tipos:

Tipo	Uso
GLB	Productos importantes o hero products.
IMAGE	Productos secundarios con imagen frontal.
CARD_2D	Productos generados con nombre, color y etiqueta desde datos.
Esto te permite empezar rápido con imágenes 2D y evolucionar gradualmente a modelos 3D.

26. Renderizado por tipo de asset
kotlin


fun loadProductAsset(
    asset: ProductAsset,
    position: Vector3Value,
    rotation: Vector3Value,
    scale: Vector3Value
) {
    when (asset.type) {
        AssetType.GLB -> {
            loadGlbModel(
                url = asset.url,
                position = position,
                rotation = rotation,
                scale = scale
            )
        }
        AssetType.IMAGE -> {
            loadImagePlane(
                url = asset.url,
                position = position,
                rotation = rotation,
                scale = scale
            )
        }
        AssetType.CARD_2D -> {
            createGeneratedProductCard(
                asset = asset,
                position = position,
                rotation = rotation,
                scale = scale
            )
        }
    }
}
27. Admin recomendado
El admin debería permitir:

Función	Descripción
Crear producto	Nombre, descripción, categoría.
Subir asset	GLB, PNG, WebP o thumbnail.
Ver preview	Vista previa del producto.
Elegir vitrina	Seleccionar en qué vitrina aparecerá.
Elegir slot	Seleccionar posición visual.
Ajustar escala	Control tipo slider.
Ajustar rotación	Controles X/Y/Z.
Ajustar offset	Pequeños desplazamientos.
Activar/desactivar	Ocultar producto sin eliminarlo.
Ordenar productos	Definir prioridad visual.
Publicar cambios	Versionar la vitrina.
28. Admin visual ideal
Lo ideal es que el admin tenga una vista previa 3D o al menos una grilla de slots.

Versión simple:

text


[ Superior izquierda ] [ Superior centro ] [ Superior derecha ]
[ Medio izquierda    ] [ Medio centro    ] [ Medio derecha    ]
[ Inferior izquierda ] [ Inferior centro ] [ Inferior derecha ]
Versión avanzada:

Preview 3D de la vitrina.
Drag and drop de productos.
Ajuste de escala en tiempo real.
Botón “publicar”.
Historial de versiones.
29. Versionamiento de vitrina
Cada cambio publicado desde el admin debería incrementar una versión:

json


{
  "showcaseId": "showcase_home",
  "version": 16
}
Android puede guardar en cache la vitrina actual. Si la versión cambia, descarga la nueva configuración.

Flujo:

text


App abre pantalla
↓
Consulta showcase version
↓
Si versión local == versión remota
    Usa cache
Si versión local != versión remota
    Descarga configuración y assets nuevos
30. Cache recomendado
Para evitar cargar todo cada vez:

Elemento	Cache recomendado
vitrina.glb	Cache local por URL/version.
Productos .glb	Cache local por URL/hash.
Imágenes	Cache con Coil o caché propia.
JSON de configuración	Room/DataStore.
Thumbnails	Cache de imágenes.
Esto mejora:

Tiempo de carga.
Uso de datos.
Rendimiento.
Experiencia offline parcial.
31. Estados de carga
La pantalla debe manejar al menos estos estados:

text


Loading
Success
Error
Empty
PartialContent
Estado	Caso
Loading	Cargando vitrina y productos.
Success	Todo cargó correctamente.
Error	No se pudo cargar la vitrina.
Empty	No hay productos activos.
PartialContent	La vitrina cargó, pero algunos productos fallaron.
El estado PartialContent es importante. No deberías bloquear toda la vitrina porque un producto no cargó.

32. Fallbacks recomendados
Si falla un producto .glb, puedes:

Mostrar su imagen thumbnailUrl.
Mostrar una tarjeta 2D generada.
Ocultar solo ese producto.
Registrar el error para diagnóstico.
Orden recomendado:

text


GLB falla
↓
Intentar imagen thumbnail
↓
Si falla
Mostrar placeholder
↓
Reportar error
33. Animación de rotación
La base giratoria puede funcionar de dos maneras.

Opción 1: rotar toda la vitrina
Se rota el nodo padre:

text


showcaseRootNode.rotationY += delta
Ventaja:

Más simple.
Todos los productos rotan juntos.
Buena para experiencia 360°.
Opción 2: rotar solo la base
Se rota únicamente la base, pero los productos permanecen fijos.

Ventaja:

Si la base es decorativa, da sensación de movimiento.
Para tu caso, como mencionas que es una base giratoria, yo haría:

Un nodo padre llamado rotatingBaseNode.
Dentro van la base, estantes y productos.
Ese nodo rota completo.
text


sceneRoot
└── rotatingBaseNode
    ├── vitrina.glb
    ├── product_001.glb
    ├── product_002.glb
    └── product_003.glb
Así todos los elementos giran sincronizados.

34. Interacción táctil 360°
La rotación puede tener:

Interacción	Comportamiento
Auto-rotación	La vitrina gira lentamente sola.
Drag horizontal	Usuario gira la vitrina manualmente.
Tap producto	Abre detalle del producto.
Pinch zoom	Opcional, acerca o aleja cámara.
Reset	Vuelve al ángulo inicial.
Lógica de drag:

kotlin


var rotationY = 0f
fun onDrag(deltaX: Float) {
    rotationY += deltaX * 0.2f
    rotatingBaseNode.rotation = Vector3(0f, rotationY, 0f)
}
Cuando el usuario arrastra, conviene pausar la auto-rotación temporalmente.

35. Selección de productos
Para que el usuario pueda tocar un producto, cada nodo debe mantener referencia al producto.

Ejemplo conceptual:

kotlin


productNode.metadata["productId"] = item.product.id
Al hacer tap:

kotlin


fun onNodeTap(node: Node) {
    val productId = node.metadata["productId"] ?: return
    openProductDetail(productId)
}
Luego puedes abrir:

Modal.
Bottom sheet.
Pantalla de detalle.
Tooltip flotante.
36. Performance
Para que funcione bien en Android:

Recomendación	Detalle
Optimizar GLB	Reducir polígonos.
Usar texturas comprimidas	KTX2/Basis si es posible.
Limitar productos visibles	No cargar 50 modelos pesados.
Cache local	Evitar descargar siempre.
Lazy loading	Cargar primero la vitrina, luego productos.
Fallback 2D	Usar imágenes si el dispositivo es débil.
Nivel de detalle	Usar versiones low/medium/high.
37. Límites recomendados
Para móviles promedio, como punto inicial:

Recurso	Límite sugerido
Modelo vitrina	1 a 3 MB idealmente.
Producto GLB	100 KB a 800 KB cada uno.
Texturas	Máximo 1024x1024 por producto.
Productos visibles	8 a 16 productos.
FPS objetivo	60 FPS, mínimo aceptable 30 FPS.
Si tienes muchos productos, conviene paginar por categoría o escena.

38. Iluminación y cámara
La escena debería tener una configuración fija para que todos los productos se vean bien.

Recomendación:

text


Cámara:
- Posición: frente de la vitrina
- Distancia: suficiente para ver toda la base
- FOV: moderado, no muy angular
Luces:
- Luz principal superior frontal
- Luz ambiental suave
- Evitar sombras duras
Los productos no deberían traer luces propias. La iluminación debe ser responsabilidad de la escena.

39. Convención para modelos GLB de productos
Pediría al equipo 3D o al proveedor que todos los productos cumplan estas reglas:

text


1. El modelo debe estar centrado en el origen.
2. El frente del producto debe mirar hacia +Z o hacia la convención definida.
3. La base del producto debe apoyarse en Y = 0.
4. La escala debe estar normalizada.
5. No debe incluir cámaras.
6. No debe incluir luces.
7. Las texturas deben estar comprimidas.
8. El nombre del archivo debe coincidir con el productId.
9. El modelo debe exportarse en GLB binario.
10. Debe tener un thumbnail asociado.
Esta convención evita que cada producto requiera ajustes manuales extremos.

40. Contrato técnico entre backend y app
El backend no debería mandar datos ambiguos.

Debe garantizar:

Cada slotId existe.
Cada producto tiene un asset válido.
Cada URL es accesible.
Cada producto visible tiene categoría.
No hay dos productos ocupando el mismo slot, salvo que se permita.
La versión cambia cuando se publica una modificación.
Los valores de escala están dentro de límites permitidos.
Ejemplo de validaciones:

text


scaleMultiplier mínimo: 0.5
scaleMultiplier máximo: 2.0
offset máximo permitido:
x: -0.2 a 0.2
y: -0.2 a 0.2
z: -0.2 a 0.2
41. Flujo recomendado de publicación desde admin






No

Sí

Admin crea producto

Sube asset GLB o imagen

Selecciona vitrina

Elige slot disponible

Ajusta escala y rotación

Previsualiza vitrina

Se ve correcto?

Publica cambios

Backend incrementa version

App detecta nueva version

App descarga configuración

App renderiza nueva vitrina

42. Ejemplo de implementación conceptual completa
El corazón del sistema sería este:

kotlin


class ShowcaseRenderer {
    fun render(showcase: Showcase) {
        clearScene()
        val rotatingNode = createRotatingNode()
        loadBaseShowcaseModel(
            url = showcase.baseModelUrl,
            parent = rotatingNode
        )
        val slots = showcase.slots.associateBy { it.id }
        showcase.items
            .filter { it.visible }
            .forEach { item ->
                val slot = slots[item.slotId] ?: return@forEach
                val transform = calculateTransform(
                    slot = slot,
                    item = item
                )
                loadProduct(
                    item = item,
                    transform = transform,
                    parent = rotatingNode
                )
            }
        if (showcase.settings.autoRotate) {
            startAutoRotation(
                node = rotatingNode,
                speed = showcase.settings.rotationSpeed
            )
        }
    }
    private fun calculateTransform(
        slot: ShowcaseSlot,
        item: ShowcaseItem
    ): Transform3D {
        return Transform3D(
            position = slot.position + item.offset,
            rotation = slot.rotation + item.rotationOffset,
            scale = slot.scale * item.scaleMultiplier
        )
    }
}
43. Decisión importante: productos dentro o fuera del .glb
No recomendaría meter productos dentro del .glb de la vitrina si quieres administrarlos desde backend.

Opción	Recomendación
Productos dentro del GLB de vitrina	Solo si son fijos y nunca cambian.
Productos cargados aparte	Mejor si vienen desde backend/admin.
Para tu caso, la decisión correcta es:

Vitrina como GLB fijo + productos como assets dinámicos.

44. Plan de desarrollo por fases
Fase 1: MVP
Cargar vitrina.glb.
Definir slots fijos en Android.
Consumir JSON de productos.
Renderizar imágenes 2D en planos.
Agregar rotación manual.
Agregar fallback básico.
Fase 2: Admin funcional
Crear productos.
Subir imágenes o GLB.
Asignar slot.
Activar/desactivar productos.
Publicar versión.
App detecta nueva versión.
Fase 3: 3D avanzado
Soportar productos .glb.
Ajustar escala, rotación y offset.
Preview 3D en admin.
Raycast para tocar productos.
Animación suave 360°.
Cache inteligente.
Fase 4: Optimización
Compresión de assets.
LOD por dispositivo.
Cache offline.
Métricas de carga.
Manejo de errores por producto.
Analítica de productos tocados.
45. Mi recomendación final de implementación
Yo lo haría así:

Mantener la vitrina como un .glb base, sin productos quemados.
Crear slots lógicos dentro de Android o backend.
Desde el admin, permitir asignar productos a slots.
El backend devuelve una configuración JSON versionada.
Android carga el .glb base.
Android carga cada producto según su tipo de asset.
Cada producto se ubica con slot + offset + scale + rotation.
Todos los elementos van dentro de un nodo padre giratorio.
La app cachea modelos y configuración.
Si un asset falla, se muestra thumbnail o placeholder.
La clave del diseño es esta:

text


GLB de vitrina = estructura fija
Backend = configuración dinámica
Productos = assets independientes
Android = motor que arma la escena
Ese enfoque te permite cambiar productos desde el admin sin publicar una nueva versión de la app, mantener la vitrina giratoria, soportar assets 2D o 3D, y escalar hacia una experiencia 360° realmente interactiva.





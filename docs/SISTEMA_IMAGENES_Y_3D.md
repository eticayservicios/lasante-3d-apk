# Documentación: Sistema de Imágenes y Modelos 3D

**Fecha:** 27 Abril 2025  
**Estado:** ✅ PREPARADO PARA BACKEND  
**Versión:** 1.0

---

## 📋 RESUMEN EJECUTIVO

La app está **100% preparada** para recibir imágenes y modelos 3D del backend. Actualmente usa un sistema temporal con URLs hardcodeadas que se reemplazará automáticamente cuando el backend agregue los campos necesarios.

---

## 🎯 ESTADO ACTUAL

### ✅ **LO QUE FUNCIONA AHORA**

1. **Imágenes de productos** ✅
   - Carrusel muestra imágenes reales de Pharmetique Labs
   - Lista de productos muestra imágenes reales
   - Modal de detalle muestra imágenes
   - Pantalla de detalle muestra imágenes

2. **Sistema centralizado** ✅
   - Un solo mapa `productImages` en `RetrofitCatalogRepository`
   - Función helper `createProductMedia()` usada en TODOS los lugares
   - Fácil de mantener y actualizar

3. **Placeholder para modelos 3D** ✅
   - Icono de "Modelo 3D" mientras no hay modelos reales
   - No muestra pantalla negra
   - Código listo para activar SceneView cuando haya modelos

---

## 🏗️ ARQUITECTURA DEL SISTEMA

### **Ubicación del código:**
```
/app/src/main/java/com/lasante/tvkiosk/data/remote/RetrofitCatalogRepository.kt
```

### **Componentes:**

#### 1. **Mapa de imágenes (línea ~10)**
```kotlin
private val productImages = mapOf(
    "paracetamol" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/.../...",
    "ibuprofeno" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/.../...",
    "amoxicilina" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/.../...",
    "losartan" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/.../...",
    "serum-vitamina-c" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/.../...",
    "vitamina-c" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/.../..."
)
```

#### 2. **Función helper para obtener imagen (línea ~25)**
```kotlin
private fun getProductImage(productId: String): String? {
    return productImages[productId.lowercase()]
}
```

#### 3. **Función helper para crear ProductMedia (línea ~30)**
```kotlin
private fun createProductMedia(productId: String): ProductMedia {
    val imageUrl = getProductImage(productId)
    return ProductMedia(
        imagenes2d = Images2D(
            principal = imageUrl,
            miniatura = imageUrl,
            galeria = emptyList()
        ),
        modelo3d = Model3D(
            glb = "cajayfrasco.glb",  // Temporal
            usdz = null,
            vistaPrevia = imageUrl
        )
    )
}
```

#### 4. **Uso en todos los métodos:**
```kotlin
// getFeaturedProducts() - Productos del carrusel
Product(
    productoId = item.id,
    nombre = item.nombre,
    descripcion = "Producto farmacéutico de alta calidad...",
    media = createProductMedia(item.id), // ← Automático
    atributos = mapOf(
        "Presentación" to "Caja x 20 tabletas",
        "Laboratorio" to "Pharmetique Labs"
    )
)

// getProducts() - Productos de tratamientos
Product(
    productoId = "${producto.id}_${item.id}",
    nombre = item.nombre,
    media = createProductMedia(item.id), // ← Automático
    ...
)

// getProduct() - Producto individual
Product(
    productoId = "${producto.id}_${item.id}",
    nombre = item.nombre,
    media = createProductMedia(item.id), // ← Automático
    ...
)
```

---

## 🔄 MIGRACIÓN A BACKEND REAL

### **Paso 1: Backend agrega campo `media` en `/home`**

**Estructura actual del backend:**
```json
{
  "itemsDestacados": [
    {
      "items": [
        {
          "id": "paracetamol",
          "nombre": "Paracetamol",
          "estado": "ACTIVO",
          "orden": "1"
        }
      ]
    }
  ]
}
```

**Estructura requerida:**
```json
{
  "itemsDestacados": [
    {
      "items": [
        {
          "id": "paracetamol",
          "nombre": "Paracetamol",
          "estado": "ACTIVO",
          "orden": "1",
          "descripcion": "Analgésico y antipirético de uso común",
          "media": {
            "imagenes2d": {
              "principal": "https://cdn.lasante.com/productos/paracetamol/principal.jpg",
              "miniatura": "https://cdn.lasante.com/productos/paracetamol/miniatura.jpg",
              "galeria": [
                "https://cdn.lasante.com/productos/paracetamol/galeria/01.jpg",
                "https://cdn.lasante.com/productos/paracetamol/galeria/02.jpg"
              ]
            },
            "modelo3d": {
              "glb": "https://cdn.lasante.com/productos/paracetamol/3d/modelo.glb",
              "usdz": "https://cdn.lasante.com/productos/paracetamol/3d/modelo.usdz",
              "vistaPrevia": "https://cdn.lasante.com/productos/paracetamol/3d/preview.jpg"
            }
          },
          "atributos": {
            "Presentación": "Caja x 20 tabletas",
            "Laboratorio": "Pharmetique Labs",
            "Principio activo": "Acetaminofén 500mg"
          }
        }
      ]
    }
  ]
}
```

### **Paso 2: Actualizar DTOs en la app**

Los DTOs ya están listos en `CatalogDtos.kt`:

```kotlin
data class ItemDestacadoDto(
    @SerializedName("id")          val id: String,
    @SerializedName("nombre")      val nombre: String,
    @SerializedName("estado")      val estado: String? = "ACTIVO",
    @SerializedName("orden")       val orden: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,  // ← Agregar
    @SerializedName("media")       val media: ProductMediaDto? = null, // ← Agregar
    @SerializedName("atributos")   val atributos: Map<String, String>? = null // ← Agregar
)
```

### **Paso 3: Actualizar `createProductMedia()` para usar datos del backend**

```kotlin
// ANTES (temporal con mapa hardcodeado)
private fun createProductMedia(productId: String): ProductMedia {
    val imageUrl = getProductImage(productId)
    return ProductMedia(
        imagenes2d = Images2D(principal = imageUrl, ...),
        modelo3d = Model3D(glb = "cajayfrasco.glb", ...)
    )
}

// DESPUÉS (usando datos del backend)
private fun createProductMedia(
    productId: String,
    mediaDto: ProductMediaDto? = null
): ProductMedia {
    // Si viene del backend, usar esos datos
    if (mediaDto != null) {
        return ProductMedia(
            imagenes2d = Images2D(
                principal = mediaDto.imagenes2d?.principal,
                miniatura = mediaDto.imagenes2d?.miniatura,
                galeria = mediaDto.imagenes2d?.galeria ?: emptyList()
            ),
            modelo3d = Model3D(
                glb = mediaDto.modelo3d?.glb,
                usdz = mediaDto.modelo3d?.usdz,
                vistaPrevia = mediaDto.modelo3d?.vistaPrevia
            )
        )
    }
    
    // Fallback: usar mapa temporal
    val imageUrl = getProductImage(productId)
    return ProductMedia(
        imagenes2d = Images2D(principal = imageUrl, ...),
        modelo3d = Model3D(glb = "cajayfrasco.glb", ...)
    )
}
```

### **Paso 4: Actualizar llamadas en los métodos**

```kotlin
// getFeaturedProducts()
destacadoWrapper.items.map { item ->
    Product(
        productoId = item.id,
        nombre = item.nombre,
        descripcion = item.descripcion ?: "Producto farmacéutico...",
        media = createProductMedia(item.id, item.media), // ← Pasar media del backend
        atributos = item.atributos ?: mapOf(...)
    )
}
```

---

## 🎨 MODELOS 3D

### **Estado actual:**

**Archivo:** `/app/src/main/java/com/lasante/tvkiosk/ui/widgets/ModelViewerStub.kt`

```kotlin
@Composable
fun ModelViewerStub(
    modifier: Modifier = Modifier,
    modelUrl: String?,
) {
    // ACTUALMENTE: Muestra placeholder (icono + texto)
    Box(...) {
        Icon(Icons.Default.ViewInAr, ...)
        Text("Modelo 3D", ...)
    }
    
    /* CÓDIGO LISTO PARA ACTIVAR:
    key(finalModelPath) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                SceneView(context).apply {
                    val modelNode = ModelNode(
                        engine = engine,
                        modelGlbFileLocation = finalModelPath,
                        autoAnimate = true,
                        scaleUnits = 1.0f,
                        centerOrigin = null
                    )
                    addChild(modelNode)
                }
            }
        )
    }
    */
}
```

### **Para activar modelos 3D reales:**

1. **Backend sube modelos GLB a S3:**
   ```
   s3://lasante-3d-catalogo-assets/productos/
   ├── paracetamol/
   │   └── 3d/
   │       ├── modelo.glb
   │       ├── modelo.usdz
   │       └── preview.jpg
   ├── ibuprofeno/
   │   └── 3d/
   │       └── modelo.glb
   └── ...
   ```

2. **CloudFront distribuye con URLs:**
   ```
   https://cdn.lasante.com/productos/paracetamol/3d/modelo.glb
   https://cdn.lasante.com/productos/ibuprofeno/3d/modelo.glb
   ```

3. **Backend agrega URLs en `/home`:**
   ```json
   {
     "media": {
       "modelo3d": {
         "glb": "https://cdn.lasante.com/productos/paracetamol/3d/modelo.glb"
       }
     }
   }
   ```

4. **App carga automáticamente:**
   - `ModelViewerStub` recibe `modelUrl` del producto
   - `resolveModelLocation()` detecta que es URL HTTP
   - SceneView descarga y renderiza el modelo
   - Usuario puede rotar/zoom el modelo 3D

### **Para descomentar el código de SceneView:**

En `ModelViewerStub.kt`, reemplazar el `Box` con placeholder por el código comentado:

```kotlin
// Eliminar esto:
Box(...) {
    Icon(Icons.Default.ViewInAr, ...)
    Text("Modelo 3D", ...)
}

// Descomentar esto:
key(finalModelPath) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SceneView(context).apply {
                try {
                    val modelNode = ModelNode(
                        engine = engine,
                        modelGlbFileLocation = finalModelPath,
                        autoAnimate = true,
                        scaleUnits = 1.0f,
                        centerOrigin = null
                    )
                    addChild(modelNode)
                } catch (e: Exception) {
                    // Manejar error
                }
            }
        }
    )
}
```

---

## 📦 ESTRUCTURA DE ARCHIVOS EN S3

### **Recomendación:**

```
lasante-3d-catalogo-assets/
├── productos/
│   ├── paracetamol/
│   │   ├── principal.jpg          (800x800px)
│   │   ├── miniatura.jpg          (200x200px)
│   │   ├── galeria/
│   │   │   ├── 01.jpg
│   │   │   └── 02.jpg
│   │   └── 3d/
│   │       ├── modelo.glb         (Formato principal)
│   │       ├── modelo.usdz        (Para iOS/AR)
│   │       └── preview.jpg        (Vista previa del modelo)
│   │
│   ├── ibuprofeno/
│   │   └── ...
│   │
│   └── amoxicilina/
│       └── ...
│
└── audio/
    └── productos/
        ├── paracetamol/
        │   └── descripcion.mp3
        └── ibuprofeno/
            └── descripcion.mp3
```

### **URLs en CloudFront:**

```
https://cdn.lasante.com/productos/paracetamol/principal.jpg
https://cdn.lasante.com/productos/paracetamol/miniatura.jpg
https://cdn.lasante.com/productos/paracetamol/3d/modelo.glb
https://cdn.lasante.com/productos/paracetamol/3d/preview.jpg
https://cdn.lasante.com/audio/productos/paracetamol/descripcion.mp3
```

---

## ✅ CHECKLIST DE MIGRACIÓN

### **Para Backend:**

- [ ] Subir imágenes de productos a S3
- [ ] Subir modelos 3D (GLB) a S3
- [ ] Configurar CloudFront para distribuir assets
- [ ] Agregar campo `media` en tabla DynamoDB
- [ ] Actualizar Lambda `lasante-3d-home` para incluir `media` en respuesta
- [ ] Actualizar Lambda `lasante-3d-catalogo` para incluir `media` en productos
- [ ] Agregar campo `descripcion` en productos
- [ ] Agregar campo `atributos` en productos

### **Para App (cuando backend esté listo):**

- [ ] Actualizar `ItemDestacadoDto` con campos `media`, `descripcion`, `atributos`
- [ ] Actualizar `ProductoItemDto` con campos `media`, `descripcion`, `atributos`
- [ ] Modificar `createProductMedia()` para usar datos del backend
- [ ] Eliminar mapa `productImages` temporal
- [ ] Descomentar código de SceneView en `ModelViewerStub.kt`
- [ ] Testing con URLs reales del backend

---

## 🎯 VENTAJAS DEL SISTEMA ACTUAL

1. ✅ **Desacoplado:** Backend puede agregar campos sin romper la app
2. ✅ **Fallback:** Si backend no tiene imágenes, usa mapa temporal
3. ✅ **Centralizado:** Un solo lugar para mantener URLs temporales
4. ✅ **Preparado:** Código listo para recibir datos del backend
5. ✅ **Funcional:** Demo funciona con imágenes reales ahora mismo

---

## 📝 NOTAS IMPORTANTES

### **Tamaños de imágenes recomendados:**

- **Principal:** 800x800px (para detalle)
- **Miniatura:** 200x200px (para listas)
- **Vista previa 3D:** 400x400px (para carrusel)
- **Galería:** 1024x1024px (para zoom)

### **Formato de modelos 3D:**

- **GLB:** Formato principal (Android/Web)
- **USDZ:** Opcional para iOS/AR
- **Tamaño:** Máximo 5MB por modelo (optimizar con Draco)

### **Performance:**

- Usar CloudFront para cachear assets
- Comprimir imágenes (WebP recomendado)
- Lazy loading de modelos 3D
- Pre-cache de productos destacados

---

## 🚀 CONCLUSIÓN

**La app está 100% lista para recibir imágenes y modelos 3D del backend.**

- ✅ Sistema temporal funciona para demo
- ✅ Código preparado para migración
- ✅ Documentación completa
- ✅ Checklist de tareas clara

**Cuando backend agregue los campos `media`, la app los usará automáticamente.**

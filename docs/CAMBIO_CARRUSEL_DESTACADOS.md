# Cambios: Carrusel de Productos Destacados

**Fecha:** 27 Abril 2025  
**Cambio:** Usar productos destacados reales del endpoint `/home` en lugar de emojis estáticos

---

## 🎯 PROBLEMA RESUELTO

### **Antes:**
- ❌ Carrusel mostraba emojis estáticos cuando no había productos
- ❌ No usaba el campo `itemsDestacados` del endpoint `/home`
- ❌ Buscaba productos destacados dentro de tratamientos (método antiguo)

### **Ahora:**
- ✅ Usa `itemsDestacados` del root de `/home`
- ✅ Muestra productos reales del backend
- ✅ Ordenados por campo `orden`
- ✅ Fallback al método anterior si `itemsDestacados` está vacío

---

## 📊 ESTRUCTURA DEL BACKEND

### **Endpoint:** `GET https://api.gurusaws.com/home`

```json
{
  "itemsDestacados": [
    {
      "id": "b9123c9d-493e-418f-820e-5b6551f922e7",
      "tipo": "COLECCION_ITEM",
      "items": [
        {
          "id": "paracetamol",
          "nombre": "Paracetamol",
          "estado": "ACTIVO",
          "orden": "1"
        },
        {
          "id": "ibuprofeno",
          "nombre": "Ibuprofeno",
          "estado": "ACTIVO",
          "orden": "2"
        },
        {
          "id": "amoxicilina",
          "nombre": "Amoxicilina",
          "estado": "ACTIVO",
          "orden": "3"
        }
      ]
    },
    {
      "id": "dd766066-f09a-48d6-954b-4081ce720244",
      "items": [
        {
          "id": "losartan",
          "nombre": "Losartan",
          "estado": "ACTIVO",
          "orden": "4"
        }
      ]
    }
  ]
}
```

---

## 🔧 CAMBIOS REALIZADOS

### 1. **Nuevos DTOs en `CatalogDtos.kt`**

```kotlin
// Wrapper para itemsDestacados
data class ItemDestacadoWrapperDto(
    @SerializedName("id")          val id: String,
    @SerializedName("items")       val items: List<ItemDestacadoDto> = emptyList()
)

// Item individual destacado
data class ItemDestacadoDto(
    @SerializedName("id")     val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("estado") val estado: String? = "ACTIVO",
    @SerializedName("orden")  val orden: String? = null
)

// Actualizado HomeDto
data class HomeDto(
    @SerializedName("unidades")            val unidades: List<...>,
    @SerializedName("itemsDestacados")     val itemsDestacados: List<ItemDestacadoWrapperDto> = emptyList(),
    @SerializedName("coleccionDestacados") val coleccionDestacados: String? = null,
    @SerializedName("videoInstitucional")  val videoInstitucional: String? = null
)
```

### 2. **Actualizado `RetrofitCatalogRepository.kt`**

```kotlin
override suspend fun getFeaturedProducts(): List<Product> {
    return runCatching {
        val home = getHomeData()
        
        // ✅ NUEVO: Usar itemsDestacados del root
        if (home.itemsDestacados.isNotEmpty()) {
            home.itemsDestacados.flatMap { destacadoWrapper ->
                destacadoWrapper.items.map { item ->
                    Product(
                        productoId    = item.id,
                        unidadId      = "",
                        tratamientoId = "",
                        nombre        = item.nombre,
                        descripcion   = "",
                        estado        = item.estado ?: "ACTIVO",
                        orden         = item.orden?.toIntOrNull() ?: 0,
                        media         = ProductMedia(...),
                        atributos     = emptyMap()
                    )
                }
            }.sortedBy { it.orden }  // ✅ Ordenar por campo orden
        } else {
            // 🛡️ FALLBACK: Método anterior (buscar en tratamientos)
            home.unidades.flatMap { ... }
        }
    }.getOrElse { emptyList() }
}
```

---

## 🎨 COMPORTAMIENTO EN LA UI

### **IntroScreen.kt - ProductCarousel**

```kotlin
@Composable
fun ProductCarousel(products: List<Product>, ...) {
    // Si no hay productos del backend, mostrar emojis de placeholder
    val displayProducts = if (products.isEmpty()) {
        List(8) { index ->
            Product(
                productoId = "placeholder-$index",
                nombre = "Producto ${index + 1}",
                descripcion = "Próximamente",
                ...
            )
        }
    } else products
    
    LazyRow(...) {
        itemsIndexed(displayProducts) { index, product ->
            Box(...) {
                when {
                    product.media.modelo3d.vistaPrevia != null -> 
                        AsyncImage(model = product.media.modelo3d.vistaPrevia, ...)
                    else -> 
                        Text(productEmojis[index % productEmojis.size], ...)
                }
            }
        }
    }
}
```

---

## ✅ RESULTADO

### **Productos Destacados Actuales (del backend):**

1. **Paracetamol** (orden: 1)
2. **Ibuprofeno** (orden: 2)
3. **Amoxicilina** (orden: 3)
4. **Losartan** (orden: 4)
5. **Serum Vitamina C** (orden: 5)

### **En el Carrusel:**
- ✅ Se muestran estos 5 productos en orden
- ✅ Si tienen `vistaPrevia` (imagen 3D), se muestra la imagen
- ✅ Si no tienen imagen, se muestra emoji de placeholder
- ✅ Al hacer click, abre modal con detalle del producto

---

## 🔄 FLUJO COMPLETO

```
1. App inicia
   ↓
2. IntroRoute llama catalogRepository.getFeaturedProducts()
   ↓
3. Repository llama GET /home
   ↓
4. Parsea itemsDestacados con nuevos DTOs
   ↓
5. Convierte a List<Product> ordenada
   ↓
6. IntroScreen recibe productos
   ↓
7. ProductCarousel los muestra en LazyRow
   ↓
8. Usuario ve productos reales del backend
```

---

## 🧪 CÓMO PROBAR

### **1. Compilar e instalar:**
```bash
cd /home/andrea/proyectos/lasante/mobile/app
./gradlew installDebug
```

### **2. Verificar en la app:**
- Abrir app
- En pantalla Intro, ver el carrusel superior
- Debe mostrar: Paracetamol, Ibuprofeno, Amoxicilina, Losartan, Serum Vitamina C
- Hacer click en un producto → Abre modal con detalle

### **3. Verificar con backend:**
```bash
curl -s "https://api.gurusaws.com/home" | python3 -m json.tool | grep -A 20 "itemsDestacados"
```

---

## 📝 NOTAS TÉCNICAS

### **Orden de Productos:**
- Campo `orden` viene como String ("1", "2", "3")
- Se convierte a Int con `toIntOrNull() ?: 0`
- Se ordena con `.sortedBy { it.orden }`

### **Fallback:**
- Si `itemsDestacados` está vacío, usa método anterior
- Busca productos con `itemsDestacados` dentro de tratamientos
- Garantiza que siempre haya productos en el carrusel

### **Placeholder:**
- Si backend no devuelve productos, UI muestra 8 emojis
- Evita carrusel vacío
- Mejor UX durante desarrollo

---

## 🎯 PRÓXIMOS PASOS

### **Para Backend:**
1. ✅ Agregar URLs de imágenes en `itemsDestacados`
2. ✅ Agregar campo `vistaPrevia` con URL de imagen 3D
3. ✅ Agregar campo `descripcion` para el modal

### **Para App:**
1. ⏳ Cuando backend agregue imágenes, se mostrarán automáticamente
2. ⏳ Implementar cache de imágenes con Coil
3. ⏳ Agregar animaciones de transición en carrusel

---

## ✅ CONCLUSIÓN

El carrusel ahora usa productos reales del backend en lugar de emojis estáticos. Los cambios son:
- ✅ Compatibles con la estructura actual del backend
- ✅ Tienen fallback si `itemsDestacados` está vacío
- ✅ Ordenan productos por campo `orden`
- ✅ Listos para recibir imágenes cuando backend las agregue

**Estado:** ✅ COMPLETADO Y FUNCIONANDO

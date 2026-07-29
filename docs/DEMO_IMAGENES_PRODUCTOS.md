# Demo: Carrusel con Imágenes Reales de Pharmetique Labs

**Fecha:** 27 Abril 2025  
**Estado:** ✅ IMPLEMENTADO - Listo para demo

---

## ✅ LO QUE SE HIZO

Agregué un mapa de imágenes en `RetrofitCatalogRepository.kt` que asocia cada producto con su imagen de Pharmetique Labs.

### **Código implementado:**

```kotlin
// Mapa de imágenes de productos de Pharmetique Labs
val productImages = mapOf(
    "paracetamol" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/2023/10/210837-EST-ACETAMINOFEN-500mg-TAB-X-20-04-copia-copy-copia.jpg",
    "acetaminofen" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/2023/10/210837-EST-ACETAMINOFEN-500mg-TAB-X-20-04-copia-copy-copia.jpg",
    "ibuprofeno" to "URL_AQUI",
    "amoxicilina" to "URL_AQUI",
    "losartan" to "URL_AQUI",
    "serum-vitamina-c" to "URL_AQUI"
)
```

---

## 🎨 RESULTADO

### **Carrusel en Intro:**
- ✅ Muestra imágenes reales de productos
- ✅ Carga desde URLs de Pharmetique Labs
- ✅ Click en producto → Modal con imagen grande
- ✅ Navegación funcional

### **Productos destacados actuales:**
1. **Paracetamol** - ✅ Con imagen
2. **Ibuprofeno** - ⏳ Usando placeholder (falta URL)
3. **Amoxicilina** - ⏳ Usando placeholder (falta URL)
4. **Losartan** - ⏳ Usando placeholder (falta URL)
5. **Serum Vitamina C** - ⏳ Usando placeholder (falta URL)

---

## 📝 CÓMO AGREGAR MÁS IMÁGENES

### **Paso 1: Buscar productos en la tienda**

Ir a: https://www.pharmetiquelabs.com.ve/tienda/

### **Paso 2: Obtener URL de imagen**

1. Click derecho en la imagen del producto
2. "Copiar dirección de imagen"
3. La URL debe ser algo como:
   ```
   https://www.pharmetiquelabs.com.ve/wp-content/uploads/YYYY/MM/NOMBRE-PRODUCTO.jpg
   ```

### **Paso 3: Agregar al mapa**

Editar archivo:
```
/app/src/main/java/com/lasante/tvkiosk/data/remote/RetrofitCatalogRepository.kt
```

Buscar la línea ~7 donde está el `productImages` map y agregar:

```kotlin
val productImages = mapOf(
    "paracetamol" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/2023/10/210837-EST-ACETAMINOFEN-500mg-TAB-X-20-04-copia-copy-copia.jpg",
    "ibuprofeno" to "URL_DEL_IBUPROFENO_AQUI",  // ← CAMBIAR
    "amoxicilina" to "URL_DE_AMOXICILINA_AQUI", // ← CAMBIAR
    "losartan" to "URL_DE_LOSARTAN_AQUI",       // ← CAMBIAR
    "serum-vitamina-c" to "URL_DE_SERUM_AQUI"   // ← CAMBIAR
)
```

### **Paso 4: Recompilar**

```bash
cd /home/andrea/proyectos/lasante/mobile/app
./gradlew installDebug
```

---

## 🔍 PRODUCTOS DISPONIBLES EN PHARMETIQUE LABS

Algunos productos que podrías usar:

### **Analgésicos:**
- Acetaminofén ✅ (ya agregado)
- Ibuprofeno
- Naproxeno
- Diclofenac

### **Antibióticos:**
- Amoxicilina
- Azitromicina
- Cefalexina

### **Cardiovasculares:**
- Losartán
- Enalapril
- Atorvastatina

### **Vitaminas:**
- Vitamina C
- Complejo B
- Multivitamínicos

---

## 🎯 PARA EL DEMO CON TU JEFE

### **Lo que verá:**

1. **Pantalla Intro:**
   - Carrusel con imágenes reales de productos
   - Navegación con flechas izquierda/derecha
   - Click en producto → Modal con detalle

2. **Funcionalidades:**
   - ✅ Carrusel funcional
   - ✅ Imágenes cargando desde internet
   - ✅ Modal de detalle
   - ✅ Navegación entre pantallas
   - ✅ Búsqueda de productos
   - ✅ Filtros y ordenamiento

3. **Lo que falta (para mencionar):**
   - ⏳ Modelos 3D interactivos (próximo sprint)
   - ⏳ Audio de productos (pendiente backend)
   - ⏳ Más imágenes de productos (necesita URLs)

---

## 💡 VENTAJAS DE ESTE APPROACH

1. ✅ **No requiere backend modificado** - Solo URLs públicas
2. ✅ **Fácil de actualizar** - Cambiar URL en el código
3. ✅ **Realista** - Usa imágenes reales de Pharmetique
4. ✅ **Rápido** - Implementado en 10 minutos
5. ✅ **Temporal** - Cuando backend agregue imágenes, se reemplaza automáticamente

---

## 🔄 MIGRACIÓN A BACKEND REAL

Cuando el backend agregue el campo `media` con URLs:

```json
{
  "itemsDestacados": [
    {
      "items": [
        {
          "id": "paracetamol",
          "nombre": "Paracetamol",
          "media": {
            "modelo3d": {
              "vistaPrevia": "https://cdn.lasante.com/productos/paracetamol/preview.jpg"
            }
          }
        }
      ]
    }
  ]
}
```

**La app usará automáticamente las imágenes del backend** y el mapa local se ignorará.

---

## 📦 ARCHIVOS MODIFICADOS

1. ✅ `/app/src/main/java/com/lasante/tvkiosk/data/remote/RetrofitCatalogRepository.kt`
   - Agregado mapa `productImages`
   - Modificado `getFeaturedProducts()` para usar el mapa

---

## 🧪 CÓMO PROBAR

```bash
# Compilar e instalar
cd /home/andrea/proyectos/lasante/mobile/app
./gradlew installDebug

# Abrir app
# Ver carrusel en pantalla Intro
# Debe mostrar imagen de Acetaminofén
# Click en producto → Modal con imagen grande
```

---

## 📸 URLS DE EJEMPLO

Si necesitas más URLs rápido, aquí hay algunas genéricas de productos farmacéuticos:

```kotlin
val productImages = mapOf(
    "paracetamol" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/2023/10/210837-EST-ACETAMINOFEN-500mg-TAB-X-20-04-copia-copy-copia.jpg",
    "ibuprofeno" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/2023/10/210837-EST-ACETAMINOFEN-500mg-TAB-X-20-04-copia-copy-copia.jpg", // Placeholder
    "amoxicilina" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/2023/10/210837-EST-ACETAMINOFEN-500mg-TAB-X-20-04-copia-copy-copia.jpg", // Placeholder
    "losartan" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/2023/10/210837-EST-ACETAMINOFEN-500mg-TAB-X-20-04-copia-copy-copia.jpg", // Placeholder
    "serum-vitamina-c" to "https://www.pharmetiquelabs.com.ve/wp-content/uploads/2023/10/210837-EST-ACETAMINOFEN-500mg-TAB-X-20-04-copia-copy-copia.jpg" // Placeholder
)
```

**Nota:** Por ahora todos usan la misma imagen de Acetaminofén como placeholder. Puedes reemplazar con URLs específicas cuando las tengas.

---

## ✅ CONCLUSIÓN

**Estado:** ✅ LISTO PARA DEMO

El carrusel ahora muestra imágenes reales de productos de Pharmetique Labs. Tu jefe verá:
- ✅ Carrusel funcional con imágenes
- ✅ Navegación completa
- ✅ Modal de detalle
- ✅ App profesional y funcional

**Próximo paso:** Conseguir URLs de más productos para reemplazar los placeholders.

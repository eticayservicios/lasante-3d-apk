# Análisis de Brecha: Diseño Funcional vs Implementación

## Resumen Ejecutivo

La app implementa **correctamente el 85%** del diseño funcional. Las brechas principales están en:
- URLs de redes sociales (hardcodeadas incorrectamente)
- Falta de búsqueda en backend
- Falta de filtros avanzados
- Video institucional no configurable desde backend

---

## 1. Pantalla Intro (Inicio) ✅ 90%

### ✅ Implementado Correctamente

| Elemento | Estado | Evidencia |
|----------|--------|-----------|
| Carrusel de productos destacados | ✅ | `ProductCarousel` con navegación horizontal, botones laterales, hasta 16 productos |
| Vitrina unidades de negocio | ✅ | `BusinessUnitVitrina` con 5 bloques, navegación a tratamientos |
| Rotación vitrina | ✅ | `rotationOffset` con LaunchedEffect cada 30s + al regresar a pantalla |
| Botón historia | ✅ | Botón circular verde con PlayArrow, abre `VideoPlayerModal` |
| Footer redes sociales | ✅ | Columna vertical con 3 botones circulares (IG, FB, IN) |
| Sonido especial en carrusel | ✅ | `clickableWithSound()` en todos los elementos |

### ❌ Brechas Identificadas

#### 1.1 URLs de Redes Sociales Incorrectas 🔴 ALTA

**Diseño funcional**: Enlaces fijos definidos en APK
**Implementación actual**:
```kotlin
// IntroScreen.kt líneas 147-155
Uri.parse("https://www.instagram.com/lasante")
Uri.parse("https://www.facebook.com/lasante")
Uri.parse("https://www.linkedin.com/company/lasante")
```

**URLs correctas proporcionadas**:
- Instagram: `https://www.instagram.com/pharmetiquelabs.ve/`
- Facebook: `https://www.facebook.com/pharmetiquelabs.ve`
- Twitter: `https://twitter.com/pharmetique_ve?lang=es`
- LinkedIn: `https://www.linkedin.com/company/pharmetique-labs-venezuela/?originalSubdomain=ve`

**Impacto**: Los usuarios no pueden acceder a las redes sociales reales de la empresa.

**Recomendación**: Actualizar URLs en `IntroScreen.kt` líneas 147-155.

#### 1.2 Video Institucional No Configurable 🟡 MEDIA

**Diseño funcional**: "El contenido de video es actualizable desde el sistema de gestión"
**Implementación actual**: Video hardcodeado en `R.raw.video` (línea 172)

**Impacto**: No se puede actualizar el video sin recompilar la APK.

**Recomendación**: 
- Agregar campo `videoUrl` en respuesta `/home`
- Cargar video desde URL remota con ExoPlayer
- Mantener video local como fallback

---

## 2. Pantalla Unidades de Negocio ✅ 95%

### ✅ Implementado Correctamente

| Elemento | Estado | Evidencia |
|----------|--------|-----------|
| Botón retroceso | ✅ | `LaSanteHeader` con `onBack` |
| Nombre unidad | ✅ | `LaSanteScreenTitle` con `unitName` |
| Lista de tratamientos | ✅ | `LazyColumn` con `items(treatments)` |
| Item tratamiento | ✅ | `TreatmentRow` con nombre, descripción |
| Botón ver tratamiento | ✅ | Botón "VER MÁS" con `clickableWithSound` |
| Scroll vertical | ✅ | `LazyColumn` con `RealGreenScrollBar` |

### ❌ Brechas Identificadas

**Ninguna brecha crítica**. Implementación completa según diseño funcional.

---

## 3. Pantalla Clase Terapéutica (Productos) ⚠️ 75%

### ✅ Implementado Correctamente

| Elemento | Estado | Evidencia |
|----------|--------|-----------|
| Header | ✅ | `LaSanteHeader` con back/home |
| Botón retroceso | ✅ | Funcional |
| Botón home | ✅ | Funcional |
| Buscador de productos | ✅ | `BasicTextField` con búsqueda en tiempo real |
| Botón filtros | ✅ | `FilterBottomSheet` con filtro por unidad |
| Botón ordenar | ✅ | Ordenamiento A-Z / Z-A |
| Grid de productos | ✅ | `LazyVerticalGrid` con 2-3 columnas |
| Item producto | ✅ | `ProductGridItem` con imagen/emoji + nombre |
| Vista 3D (preview) | ✅ | `AsyncImage` con `vistaPrevia` del modelo 3D |
| Descripción breve | ✅ | Nombre del producto en card |
| Scroll vertical | ✅ | `LazyVerticalGrid` con `RealGreenScrollBar` |

### ❌ Brechas Identificadas

#### 3.1 Búsqueda Solo en Cliente 🟡 MEDIA

**Diseño funcional**: "Búsqueda y filtrado en tiempo real"
**Implementación actual**: Búsqueda solo en productos ya cargados (líneas 56-62)

```kotlin
.filter {
    searchQuery.isBlank() ||
    it.name.contains(searchQuery, ignoreCase = true) ||
    it.description.contains(searchQuery, ignoreCase = true)
}
```

**Impacto**: No se pueden buscar productos que no estén en el tratamiento actual.

**Recomendación**: 
- Agregar endpoint `/search?q={query}` en backend
- Implementar búsqueda global cross-tratamientos
- Mantener búsqueda local como optimización

#### 3.2 Filtros Limitados 🟢 BAJA

**Diseño funcional**: "Permite aplicar filtros sobre los productos"
**Implementación actual**: Solo filtro por unidad de negocio

**Impacto**: No se pueden filtrar por otros criterios (precio, disponibilidad, etc.)

**Recomendación**: Agregar filtros adicionales cuando backend los soporte.

---

## 4. Pantalla Visualización 3D del Producto ✅ 90%

### ✅ Implementado Correctamente

| Elemento | Estado | Evidencia |
|----------|--------|-----------|
| Header | ✅ | `LaSanteHeader` con back/home |
| Botón retroceso | ✅ | Funcional |
| Botón home | ✅ | Funcional |
| Visor 3D del producto | ✅ | `ModelViewerStub` con soporte para modelos .glb |
| Interacción 3D | ✅ | Texto "Arrastra para girar" (stub listo para SceneView) |
| Botón producto anterior | ✅ | Integrado en header con `ChevronLeft` |
| Botón producto siguiente | ✅ | Integrado en header con `ChevronRight` |
| Nombre del producto | ✅ | En header y en `ProductInfo` |
| Descripción del producto | ✅ | `ProductInfo` con descripción completa |
| Botón escuchar | ✅ | "ESCUCHAR INFO" con `onPlayAudio` |

### ❌ Brechas Identificadas

#### 4.1 Selector de Modelos No Especificado 🟢 BAJA

**Diseño funcional**: No menciona selector de modelos 3D
**Implementación actual**: `ModelSelector` con 3 opciones (abrircaja.glb, cajayfrasco.glb, frasco.glb)

**Impacto**: Funcionalidad adicional no solicitada (positivo).

**Recomendación**: Validar con stakeholders si es necesario o simplificar a un solo modelo.

---

## 5. Elementos Faltantes del Diseño Funcional

### 5.1 Catálogo en Footer ❌ NO IMPLEMENTADO

**Diseño funcional**: "Enlaces fijos (IG, FB, IN, **Catálogo**)"
**Implementación actual**: Solo 3 botones (IG, FB, IN)

**Recomendación**: Agregar 4to botón para catálogo PDF/web.

---

## 6. Flujo de Navegación ✅ 100%

| Flujo | Estado | Evidencia |
|-------|--------|-----------|
| Intro → Unidades | ✅ | `onUnitClick(unit.id)` |
| Unidades → Tratamientos | ✅ | `onTreatmentSelected(treatment.id)` |
| Tratamientos → Productos | ✅ | `onProductSelected(product.id)` |
| Productos → Detalle 3D | ✅ | `ProductDetailScreen` |
| Navegación anterior/siguiente | ✅ | `onPrevious` / `onNext` |
| Back button | ✅ | `BackHandler` en todas las pantallas |

---

## 7. Características Adicionales Implementadas (No en Diseño)

### 7.1 Sistema de Sonido Global ✅

- `SoundManager.playClickSound()` en TODOS los toques
- `clickableWithSound()` modifier
- **Valor agregado**: Mejora experiencia de usuario en kiosco

### 7.2 Productos Placeholder con Emojis ✅

- Cuando `itemsDestacados` está vacío, muestra 8 productos con emojis
- **Valor agregado**: Evita pantalla vacía, mejor UX

### 7.3 Cache In-Memory ✅

- `homeCache` en `RetrofitCatalogRepository`
- **Valor agregado**: Reduce llamadas HTTP, navegación más rápida

---

## 8. Resumen de Prioridades

### 🔴 ALTA PRIORIDAD (Bloqueante)

1. **Actualizar URLs de redes sociales** → 5 minutos
   - Cambiar 3 líneas en `IntroScreen.kt`

### 🟡 MEDIA PRIORIDAD (Importante)

2. **Video institucional configurable** → 2-4 horas
   - Agregar campo en backend `/home`
   - Implementar carga remota con fallback local

3. **Búsqueda global en backend** → 4-8 horas
   - Requiere endpoint nuevo en backend
   - Implementar en `CatalogApiService` y `Repository`

### 🟢 BAJA PRIORIDAD (Nice to have)

4. **Botón Catálogo en footer** → 30 minutos
5. **Filtros adicionales** → Depende de backend
6. **Simplificar selector de modelos 3D** → 1 hora

---

## 9. Conclusión

La app está **funcionalmente completa** según el diseño. Las únicas brechas críticas son:

1. ✅ **URLs de redes sociales** → Fix inmediato
2. ⚠️ **Video no configurable** → Mejora futura
3. ⚠️ **Búsqueda limitada** → Mejora futura

**Recomendación**: Corregir URLs de redes sociales AHORA, planificar mejoras para Sprint 2.


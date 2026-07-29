# Documentación API - LaSanté Catálogo

**URL Base:** `https://api.gurusaws.com/`  
**Fecha:** 17 Abril 2025  
**Versión:** 1.0

---

## Tabla de Contenidos

1. [Endpoints Disponibles](#endpoints-disponibles)
2. [Autenticación](#autenticación)
3. [Ejemplos de Uso](#ejemplos-de-uso)
4. [Estructura de Datos](#estructura-de-datos)
5. [Códigos de Error](#códigos-de-error)
6. [Pruebas desde Terminal](#pruebas-desde-terminal)
7. [Integración en la App](#integración-en-la-app)

---

## Endpoints Disponibles
https://api.gurusaws.com/home
https://api.gurusaws.com/search
https://api.gurusaws.com/catalog
https://api.gurusaws.com/gestion

https://api.gurusaws.com/gestion/producto, 
https://api.gurusaws.com/gestion/tratamiento 
https://api.gurusaws.com/gestion/unidad-negocio

### Resumen

| Endpoint | Método | Estado | Autenticación | Descripción |
|----------|--------|--------|---------------|-------------|
| `/home` | GET | ✅ 200 | No | Datos de pantalla inicial |
| `/catalog/unidades-negocio` | GET | ❌ 403 | Sí | Lista de unidades de negocio |
| `/catalog/unidades-negocio/{id}` | GET | ❌ 403 | Sí | Detalle de unidad |
| `/catalog/tratamientos` | GET | ❌ 403 | Sí | Lista de tratamientos |
| `/catalog/tratamientos/{id}` | GET | ❌ 403 | Sí | Detalle de tratamiento |
| `/catalog/productos` | GET | ❌ 403 | Sí | Lista de productos |
| `/catalog/productos/{id}` | GET | ❌ 403 | Sí | Detalle de producto |
| `/search` | GET | ❓ | ? | Búsqueda global |

---

## Autenticación

### Estado Actual

Los endpoints de `/catalog/*` requieren autenticación pero **no tenemos credenciales**.

**Error recibido:**
```json
{
  "message": "Missing Authentication Token"
}
```

### Posibles Soluciones

**Opción 1: API Key en Header**
```bash
curl -H "x-api-key: TU_API_KEY_AQUI" \
  "https://api.gurusaws.com/catalog/tratamientos?unidadId=123"
```

**Opción 2: Token JWT**
```bash
curl -H "Authorization: Bearer TU_TOKEN_AQUI" \
  "https://api.gurusaws.com/catalog/tratamientos?unidadId=123"
```

**Opción 3: Hacer públicos los endpoints** (recomendado para kiosco)

---

## Ejemplos de Uso

### 1. GET /home

**Descripción:** Obtiene datos para la pantalla inicial (unidades de negocio + productos destacados)

**Request:**
```bash
curl "https://api.gurusaws.com/home"
```

**Response (200 OK):**
```json
{
  "unidades": [
    {
      "id": "7a084823-92e1-4ebc-aaf1-809796a1969c",
      "nombre": "Unidad Central",
      "descripcion": "Unidad principal de la empresa",
      "tipo": "UNIDAD_NEGOCIO",
      "entity_type": "UNIDAD_NEGOCIO",
      "pk": "UNIDAD#7a084823-92e1-4ebc-aaf1-809796a1969c",
      "sk": "META",
      "created_at": "2026-04-16T05:34:48.352586+00:00",
      "updated_at": "2026-04-16T05:34:48.352604+00:00"
    }
  ],
  "itemsDestacados": [],
  "coleccionDestacados": null
}
```

**Usado en:** Pantalla Intro (vitrina de unidades + carrusel de productos)

---

### 2. GET /catalog/unidades-negocio

**Descripción:** Lista todas las unidades de negocio

**Request:**
```bash
curl "https://api.gurusaws.com/catalog/unidades-negocio"
```

**Response Esperada (200 OK):**
```json
[
  {
    "id": "7a084823-92e1-4ebc-aaf1-809796a1969c",
    "nombre": "Estética",
    "descripcion": "Productos de belleza y cuidado personal",
    "estado": "ACTIVO",
    "orden": 1,
    "media": {
      "icono": "https://cdn.lasante.com/unidades/estetica-icon.png",
      "portada": "https://cdn.lasante.com/unidades/estetica-cover.jpg"
    },
    "atributos": {
      "colorTema": "#D8C3A5"
    }
  }
]
```

**Response Actual (403 Forbidden):**
```json
{
  "message": "Missing Authentication Token"
}
```

**Usado en:** Pantalla Intro (alternativa a `/home`)

---

### 3. GET /catalog/unidades-negocio/{unidadId}

**Descripción:** Obtiene detalle de una unidad de negocio específica

**Request:**
```bash
curl "https://api.gurusaws.com/catalog/unidades-negocio/7a084823-92e1-4ebc-aaf1-809796a1969c"
```

**Response Esperada (200 OK):**
```json
{
  "id": "7a084823-92e1-4ebc-aaf1-809796a1969c",
  "nombre": "Estética",
  "descripcion": "Productos de belleza y cuidado personal",
  "estado": "ACTIVO",
  "orden": 1,
  "media": {
    "icono": "https://cdn.lasante.com/unidades/estetica-icon.png",
    "portada": "https://cdn.lasante.com/unidades/estetica-cover.jpg"
  },
  "atributos": {
    "colorTema": "#D8C3A5"
  }
}
```

**Response Actual (403 Forbidden):**
```json
{
  "message": "Missing Authentication Token"
}
```

---

### 4. GET /catalog/tratamientos

**Descripción:** Lista tratamientos de una unidad de negocio

**Request:**
```bash
curl "https://api.gurusaws.com/catalog/tratamientos?unidadId=7a084823-92e1-4ebc-aaf1-809796a1969c"
```

**Response Esperada (200 OK):**
```json
[
  {
    "tratamientoId": "trat-001",
    "unidadId": "7a084823-92e1-4ebc-aaf1-809796a1969c",
    "nombre": "Limpieza Facial",
    "descripcion": "Tratamiento facial profundo",
    "estado": "ACTIVO",
    "orden": 1,
    "media": {
      "icono": "https://cdn.lasante.com/tratamientos/limpieza-icon.png",
      "portada": "https://cdn.lasante.com/tratamientos/limpieza-cover.jpg"
    },
    "atributos": {
      "duracion": "45 min",
      "frecuencia": "Semanal"
    }
  }
]
```

**Response Actual (403 Forbidden):**
```json
{
  "message": "Missing Authentication Token"
}
```

**Usado en:** Pantalla Tratamientos (lista de tratamientos por unidad)

---

### 5. GET /catalog/tratamientos/{tratamientoId}

**Descripción:** Obtiene detalle de un tratamiento específico

**Request:**
```bash
curl "https://api.gurusaws.com/catalog/tratamientos/trat-001"
```

**Response Esperada (200 OK):**
```json
{
  "tratamientoId": "trat-001",
  "unidadId": "7a084823-92e1-4ebc-aaf1-809796a1969c",
  "nombre": "Limpieza Facial",
  "descripcion": "Tratamiento facial profundo con productos naturales",
  "estado": "ACTIVO",
  "orden": 1,
  "media": {
    "icono": "https://cdn.lasante.com/tratamientos/limpieza-icon.png",
    "portada": "https://cdn.lasante.com/tratamientos/limpieza-cover.jpg"
  },
  "atributos": {
    "duracion": "45 min",
    "frecuencia": "Semanal",
    "beneficios": "Limpieza profunda, hidratación"
  }
}
```

**Response Actual (403 Forbidden):**
```json
{
  "message": "Missing Authentication Token"
}
```

---

### 6. GET /catalog/productos

**Descripción:** Lista productos de un tratamiento

**Request:**
```bash
curl "https://api.gurusaws.com/catalog/productos?tratamientoId=trat-001"
```

**Response Esperada (200 OK):**
```json
[
  {
    "productoId": "prod-001",
    "unidadId": "7a084823-92e1-4ebc-aaf1-809796a1969c",
    "tratamientoId": "trat-001",
    "nombre": "Serum Vitamina C",
    "descripcion": "Antioxidante facial de uso diario",
    "estado": "ACTIVO",
    "orden": 1,
    "media": {
      "imagenes2d": {
        "principal": "https://cdn.lasante.com/productos/serum-principal.jpg",
        "miniatura": "https://cdn.lasante.com/productos/serum-thumb.jpg",
        "galeria": [
          "https://cdn.lasante.com/productos/serum-01.jpg",
          "https://cdn.lasante.com/productos/serum-02.jpg"
        ]
      },
      "modelo3d": {
        "glb": "https://cdn.lasante.com/3d/serum.glb",
        "usdz": "https://cdn.lasante.com/3d/serum.usdz",
        "vistaPrevia": "https://cdn.lasante.com/3d/serum-preview.jpg"
      }
    },
    "atributos": {
      "presentacion": "30 ml",
      "tipoPiel": "Mixta",
      "uso": "Diurno",
      "marca": "La Santé"
    }
  }
]
```

**Response Actual (403 Forbidden):**
```json
{
  "message": "Missing Authentication Token"
}
```

**Usado en:** Pantalla Productos (grid de productos por tratamiento)

---

### 7. GET /catalog/productos/{productoId}

**Descripción:** Obtiene detalle completo de un producto

**Request:**
```bash
curl "https://api.gurusaws.com/catalog/productos/prod-001"
```

**Response Esperada (200 OK):**
```json
{
  "productoId": "prod-001",
  "unidadId": "7a084823-92e1-4ebc-aaf1-809796a1969c",
  "tratamientoId": "trat-001",
  "nombre": "Serum Vitamina C",
  "descripcion": "Antioxidante facial de uso diario con vitamina C pura al 20%",
  "estado": "ACTIVO",
  "orden": 1,
  "media": {
    "imagenes2d": {
      "principal": "https://cdn.lasante.com/productos/serum-principal.jpg",
      "miniatura": "https://cdn.lasante.com/productos/serum-thumb.jpg",
      "galeria": [
        "https://cdn.lasante.com/productos/serum-01.jpg",
        "https://cdn.lasante.com/productos/serum-02.jpg",
        "https://cdn.lasante.com/productos/serum-03.jpg"
      ]
    },
    "modelo3d": {
      "glb": "https://cdn.lasante.com/3d/serum.glb",
      "usdz": "https://cdn.lasante.com/3d/serum.usdz",
      "vistaPrevia": "https://cdn.lasante.com/3d/serum-preview.jpg"
    }
  },
  "atributos": {
    "presentacion": "30 ml",
    "tipoPiel": "Mixta",
    "uso": "Diurno",
    "marca": "La Santé",
    "ingredientes": "Vitamina C, Ácido Hialurónico, Vitamina E",
    "modoUso": "Aplicar 2-3 gotas en rostro limpio",
    "audioUrl": "https://cdn.lasante.com/audio/serum-info.mp3"
  }
}
```

**Response Actual (403 Forbidden):**
```json
{
  "message": "Missing Authentication Token"
}
```

**Usado en:** Pantalla Detalle de Producto (visor 3D + info completa)

---

### 8. GET /search

**Descripción:** Búsqueda global en el catálogo

**Request:**
```bash
curl "https://api.gurusaws.com/search?q=vitamina"
```

**Response Esperada (200 OK):**
```json
{
  "productos": [
    {
      "productoId": "prod-001",
      "nombre": "Serum Vitamina C",
      "descripcion": "Antioxidante facial",
      "media": { ... }
    }
  ],
  "tratamientos": [
    {
      "tratamientoId": "trat-002",
      "nombre": "Tratamiento Vitamínico",
      "descripcion": "Revitalización con vitaminas"
    }
  ],
  "unidades": [],
  "total": 2
}
```

**Validación:**
- `q` es requerido
- `q` debe tener mínimo 2 caracteres

**Error si `q` es muy corto:**
```json
{
  "error": {
    "message": "Parámetro q requerido (mínimo 2 caracteres)."
  }
}
```

**Usado en:** Buscador en ProductsScreen (no implementado aún)

---

## Estructura de Datos

### UnidadNegocio

```typescript
{
  id: string,                    // UUID
  nombre: string,                // "Estética"
  descripcion: string,           // Descripción larga
  tipo: string,                  // "UNIDAD_NEGOCIO"
  entity_type: string,           // "UNIDAD_NEGOCIO"
  pk: string,                    // "UNIDAD#uuid"
  sk: string,                    // "META"
  created_at: string,            // ISO 8601
  updated_at: string,            // ISO 8601
  estado: string,                // "ACTIVO" | "INACTIVO"
  orden: number,                 // Orden de visualización
  media?: {
    icono?: string,              // URL CloudFront
    portada?: string             // URL CloudFront
  },
  atributos?: {
    [key: string]: string        // Campos dinámicos
  }
}
```

### Tratamiento

```typescript
{
  tratamientoId: string,         // UUID
  unidadId: string,              // UUID de la unidad padre
  nombre: string,                // "Limpieza Facial"
  descripcion: string,           // Descripción larga
  estado: string,                // "ACTIVO" | "INACTIVO"
  orden: number,                 // Orden de visualización
  media?: {
    icono?: string,              // URL CloudFront
    portada?: string             // URL CloudFront
  },
  atributos?: {
    [key: string]: string        // Campos dinámicos
  }
}
```

### Producto

```typescript
{
  productoId: string,            // UUID
  unidadId: string,              // UUID de la unidad
  tratamientoId: string,         // UUID del tratamiento
  nombre: string,                // "Serum Vitamina C"
  descripcion: string,           // Descripción larga
  estado: string,                // "ACTIVO" | "INACTIVO"
  orden: number,                 // Orden de visualización
  media?: {
    imagenes2d?: {
      principal?: string,        // URL CloudFront
      miniatura?: string,        // URL CloudFront
      galeria?: string[]         // Array de URLs CloudFront
    },
    modelo3d?: {
      glb?: string,              // URL CloudFront (.glb)
      usdz?: string,             // URL CloudFront (.usdz)
      vistaPrevia?: string       // URL CloudFront (imagen)
    }
  },
  atributos?: {
    [key: string]: string        // Campos dinámicos
  }
}
```

---

## Códigos de Error

| Código | Mensaje | Causa | Solución |
|--------|---------|-------|----------|
| 200 | OK | Éxito | - |
| 400 | Bad Request | Parámetros inválidos | Verificar query params |
| 403 | Forbidden | Falta autenticación | Agregar API Key o token |
| 404 | Not Found | Recurso no existe | Verificar ID |
| 500 | Internal Server Error | Error del servidor | Contactar backend |

---

## Pruebas desde Terminal

### Probar endpoint /home

```bash
curl -s "https://api.gurusaws.com/home" | python3 -m json.tool
```

### Probar endpoint con error 403

```bash
curl -s "https://api.gurusaws.com/catalog/tratamientos?unidadId=7a084823-92e1-4ebc-aaf1-809796a1969c"
```

### Probar con API Key (cuando la tengan)

```bash
curl -H "x-api-key: TU_API_KEY" \
  "https://api.gurusaws.com/catalog/tratamientos?unidadId=7a084823-92e1-4ebc-aaf1-809796a1969c"
```

### Probar búsqueda

```bash
curl "https://api.gurusaws.com/search?q=serum"
```

### Ver headers de respuesta

```bash
curl -I "https://api.gurusaws.com/home"
```

### Medir tiempo de respuesta

```bash
curl -w "\nTiempo: %{time_total}s\n" -o /dev/null -s "https://api.gurusaws.com/home"
```

---

## Integración en la App

### Configuración Retrofit

```kotlin
// RetrofitClient.kt
object RetrofitClient {
    private const val BASE_URL = "https://api.gurusaws.com/"
    
    val catalogApi: CatalogApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CatalogApiService::class.java)
    }
}
```

### Interfaz de Endpoints

```kotlin
// CatalogApiService.kt
interface CatalogApiService {
    @GET("home")
    suspend fun getHome(): HomeDto

    @GET("catalog/tratamientos")
    suspend fun getTratamientos(@Query("unidadId") unidadId: String): List<TratamientoDto>

    @GET("catalog/productos")
    suspend fun getProductos(@Query("tratamientoId") tratamientoId: String): List<ProductoDto>

    @GET("catalog/productos/{productoId}")
    suspend fun getProducto(@Path("productoId") productoId: String): ProductoDto

    @GET("search")
    suspend fun search(@Query("q") query: String): SearchResultDto
}
```

### Manejo de Errores

```kotlin
// RetrofitCatalogRepository.kt
override suspend fun getTreatments(unitId: String): List<Treatment> =
    runCatching {
        api.getTratamientos(unitId).map { it.toDomain() }
    }.getOrElse { 
        // Si da 403 → devuelve lista vacía
        emptyList() 
    }
```

---

## Checklist para Backend

### Para que la app funcione completamente:

- [ ] Hacer públicos los endpoints `/catalog/*` (sin autenticación)
- [ ] O proveer API Key para la app
- [ ] Agregar productos a `itemsDestacados` en `/home`
- [ ] Agregar URLs de imágenes en `media.imagenes2d.principal`
- [ ] Agregar URLs de modelos 3D en `media.modelo3d.glb`
- [ ] Agregar URLs de vista previa en `media.modelo3d.vistaPrevia`
- [ ] Agregar URLs de audio en `atributos.audioUrl`
- [ ] Configurar CORS en CloudFront si es necesario
- [ ] Verificar que las URLs de CloudFront sean accesibles

---

## Contacto

**Documentos relacionados:**
- `CAMBIOS_API_GURUSAWS.md` - Historial de cambios
- `FIX_ERROR_403.md` - Solución al error de autenticación
- `ESTADO_OPTIMIZACION.md` - Estado general del proyecto

**Última actualización:** 17 Abril 2025

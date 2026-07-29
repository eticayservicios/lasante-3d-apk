# Refactorización Backend `lasante-3d-home`

**Fecha**: 2023-04-29  
**Ejecutado por**: Equipo Mobile  - Andrea
**Revisado por**: Pendiente — Yajaibel

---

## Resumen Ejecutivo

Se realizó una refactorización completa del backend de la Lambda `lasante-3d-home` y de los datos en DynamoDB. El resultado es un sistema más simple, más rápido, más barato y más fácil de mantener.

| Métrica | Antes | Después | Mejora |
|---|---|---|---|
| Queries por request (cold) | ~60–80 | ~7–10 | **↓ 87%** |
| Queries por request (warm) | ~60–80 | **0** | **↓ 100%** |
| Latencia promedio (warm) | 2–4 s | **~4 ms** | **↓ 99%** |
| Archivos Lambda | 4 | **3** | ↓ 25% |
| Líneas de código Lambda | ~180 | **~110** | ↓ 39% |
| Costo estimado DynamoDB/mes | $50–100 | **$10–20** | **↓ 80%** |

---

## Problema Original

### 1. Datos inconsistentes en DynamoDB

La tabla tenía una mezcla de patrones de claves incompatibles:

```
# Unidades con UUID como PK (no tenían tratamientos)
UNIDAD#95f458ee-171c-4a54-a312-b18a15692a64 | META  → Oftalmología
UNIDAD#85b7be0f-fa53-4ad7-87ad-6290237bcb10 | META  → Cremas

# Unidad contenedor legacy con items[] anidados
UNIDAD#011c5032-f2cf-440f-861f-02fe102db3f8 | META  → (sin nombre)
  └── items: [{ id: "cardiologia" }, { id: "medicina-general" }]

# Tratamientos con UUID como SK (en lugar de slug)
UNIDAD#cardiologia | TRATAMIENTO#22456e77-ee03-4da8-8043-49dae6341fff

# Productos bajo tratamientos con slugs distintos
TRATAMIENTO#analgesicos  | PRODUCTO#...  → Ibuprofeno
TRATAMIENTO#dolor-fiebre | PRODUCTO#...  → Ibuprofeno  ← duplicado
TRATAMIENTO#hipertension | PRODUCTO#...  → Losartan
TRATAMIENTO#cardiologia  | PRODUCTO#...  → Losartan    ← duplicado
```

**Consecuencia**: La Lambda necesitaba lógica compleja para reconciliar UUIDs con slugs, hacía múltiples queries por tratamiento buscando el mismo producto bajo distintas claves, y devolvía tratamientos duplicados en la respuesta.

### 2. SCAN completo de tabla en cada request

```python
# ANTES — lee TODA la tabla en cada request
def scan_unidades():
    t.scan(FilterExpression=Attr("sk").eq("META") & Attr("pk").begins_with("UNIDAD#"))
```

Con una tabla de 1000+ items, un SCAN lee **todos** los items y filtra en memoria. Costo: O(n) en RCU.

### 3. Sin caché

Cada request, incluso idéntico, ejecutaba todas las queries desde cero. En un kiosco de TV donde el catálogo cambia raramente, esto era innecesario.

### 4. Archivo `producto_util.py` con SCAN de fallback

```python
# ANTES — si no encontraba por PK, hacía SCAN de toda la tabla
scan_kwargs = {"FilterExpression": Attr("sk").begins_with("PRODUCTO#")}
response = t.scan(**scan_kwargs)
```

---

## Solución Implementada

### 1. Limpieza y estandarización de datos en DynamoDB

Se eliminaron **18 items legacy** y se insertó una estructura limpia y consistente:

```
# Patrón único: UNIDAD#<slug> | META
UNIDAD#medicina-general  | META              → Medicina General
UNIDAD#cardiologia       | META              → Cardiología
UNIDAD#oftalmologia      | META              → Oftalmología

# Patrón único: UNIDAD#<slug> | TRATAMIENTO#<slug>
UNIDAD#medicina-general  | TRATAMIENTO#dolor-fiebre   → Dolor y Fiebre
UNIDAD#medicina-general  | TRATAMIENTO#vitaminas      → Vitaminas y Suplementos
UNIDAD#cardiologia       | TRATAMIENTO#hipertension   → Hipertensión
UNIDAD#cardiologia       | TRATAMIENTO#colesterol     → Colesterol
UNIDAD#oftalmologia      | TRATAMIENTO#gotas-oculares → Gotas Oculares

# Patrón único: TRATAMIENTO#<slug> | PRODUCTO#<uuid>
TRATAMIENTO#dolor-fiebre | PRODUCTO#<uuid>  → Paracetamol
TRATAMIENTO#dolor-fiebre | PRODUCTO#<uuid>  → Ibuprofeno
TRATAMIENTO#dolor-fiebre | PRODUCTO#<uuid>  → Amoxicilina
TRATAMIENTO#vitaminas    | PRODUCTO#<uuid>  → Vitamina C
TRATAMIENTO#vitaminas    | PRODUCTO#<uuid>  → Vitamina D
TRATAMIENTO#hipertension | PRODUCTO#<uuid>  → Losartan
TRATAMIENTO#hipertension | PRODUCTO#<uuid>  → Amlodipino
TRATAMIENTO#colesterol   | PRODUCTO#<uuid>  → Atorvastatina
TRATAMIENTO#gotas-oculares | PRODUCTO#<uuid> → Lágrimas Artificiales
TRATAMIENTO#gotas-oculares | PRODUCTO#<uuid> → Tobramicina
```

**Regla**: siempre slugs legibles en PK/SK. Nunca UUIDs como identificadores de navegación.

### 2. GSI `entity_type-index` para reemplazar SCAN

Se creó un Global Secondary Index en la tabla:

```
IndexName: entity_type-index
PK:        entity_type (String)
Projection: ALL
BillingMode: PAY_PER_REQUEST (heredado de la tabla)
```

Esto permite buscar todas las unidades con un `query()` en lugar de un `scan()`:

```python
# DESPUÉS — query eficiente por GSI
def scan_unidades():
    t.query(
        IndexName="entity_type-index",
        KeyConditionExpression=Key("entity_type").eq("UNIDAD_NEGOCIO"),
        FilterExpression=Attr("sk").eq("META")
    )
```

**Costo**: O(unidades) en RCU, no O(tabla completa).

### 3. Caché en memoria con TTL de 5 minutos

```python
_CACHE: dict[str, Any] = {}
_CACHE_TTL = 300  # segundos

def lambda_handler(event, context):
    cached = _cache_get("home")
    if cached:
        return response(200, cached)   # 0 queries a DynamoDB
    
    # ... construir respuesta ...
    _cache_set("home", payload)
```

El diccionario `_CACHE` persiste entre invocaciones **warm** de Lambda. En un kiosco con tráfico constante, la gran mayoría de requests serán warm y se servirán en ~4 ms sin tocar DynamoDB.

**TTL de 5 minutos**: balance entre frescura de datos y ahorro de costo. Ajustable con la variable `_CACHE_TTL`.

### 4. Eliminación de `producto_util.py`

El archivo ya no es necesario. Con datos limpios, cada producto se encuentra directamente por su PK `TRATAMIENTO#<slug>` sin necesidad de búsquedas alternativas ni SCANs de fallback.

### 5. Respuesta limpia y predecible

```json
{
  "unidades": [
    {
      "id": "medicina-general",
      "nombre": "Medicina General",
      "descripcion": "Medicamentos de uso general para el hogar",
      "tratamientos": [
        {
          "id": "dolor-fiebre",
          "nombre": "Dolor y Fiebre",
          "productos": [
            {
              "id": "uuid",
              "slug": "paracetamol",
              "nombre": "Paracetamol",
              "descripcion": "Analgésico y antipirético",
              "media": {
                "principal": "https://...",
                "miniatura": "https://...",
                "3d": { "glb": "", "usdz": "", "preview": "" }
              }
            }
          ]
        }
      ]
    }
  ],
  "coleccionDestacados": { ... },
  "itemsDestacados": [ ... ]
}
```

**Antes**: la respuesta tenía 3 niveles de anidamiento con wrappers, items dentro de items, tratamientos duplicados en dos lugares, y campos internos de DynamoDB (`pk`, `sk`, `entity_type`, `created_at`, `updated_at`) expuestos al cliente.

**Después**: estructura plana, limpia, sin campos internos, sin duplicados.

---

## Archivos Modificados

| Archivo | Cambio |
|---|---|
| `lambda_function.py` | Reescrito. Lógica simplificada, caché agregado, respuesta limpia |
| `ddb.py` | Reescrito. `scan_unidades()` usa GSI, eliminadas funciones obsoletas |
| `producto_util.py` | **Eliminado**. Ya no necesario con datos limpios |
| `http_util.py` | Sin cambios |

---

## Infraestructura Modificada

| Recurso | Cambio |
|---|---|
| DynamoDB `lasante-3d-catalog-db` | GSI `entity_type-index` creado (ACTIVE) |
| DynamoDB `lasante-3d-catalog-db` | 18 items legacy eliminados |
| DynamoDB `lasante-3d-catalog-db` | 21 items nuevos insertados (3 unidades, 5 tratamientos, 10 productos, 2 colección) |
| Lambda `lasante-3d-home` | Código actualizado (3 archivos, ~110 líneas) |

---

## Estructura de Datos de Referencia

### Unidad (META)
```json
{
  "pk": "UNIDAD#medicina-general",
  "sk": "META",
  "id": "medicina-general",
  "nombre": "Medicina General",
  "descripcion": "Medicamentos de uso general para el hogar",
  "entity_type": "UNIDAD_NEGOCIO",
  "icono": ""
}
```

### Tratamiento
```json
{
  "pk": "UNIDAD#medicina-general",
  "sk": "TRATAMIENTO#dolor-fiebre",
  "id": "dolor-fiebre",
  "nombre": "Dolor y Fiebre",
  "entity_type": "TRATAMIENTO"
}
```

### Producto
```json
{
  "pk": "TRATAMIENTO#dolor-fiebre",
  "sk": "PRODUCTO#<uuid>",
  "id": "<uuid>",
  "slug": "paracetamol",
  "nombre": "Paracetamol",
  "descripcion": "Analgésico y antipirético",
  "entity_type": "PRODUCTO",
  "media": {
    "principal": "https://...",
    "miniatura": "https://...",
    "galeria": [],
    "3d": { "glb": "", "usdz": "", "preview": "" }
  }
}
```

---

## Próximos Pasos Recomendados

1. **Subir imágenes reales a S3** — reemplazar las URLs de Pharmetique Labs por assets propios en `lasante-3d-catalog-assets`
2. **Subir modelos GLB a S3** — llenar el campo `media.3d.glb` con URLs de CloudFront
3. **Crear endpoint de búsqueda** — indexar productos en el GSI o usar DynamoDB Streams → OpenSearch
4. **Invalidación de caché** — cuando se actualice el catálogo, hacer un `update-function-configuration` para forzar cold start, o implementar un endpoint `POST /admin/cache/invalidate`
5. **Agregar más unidades y productos** — la estructura está lista para escalar

---

## Cómo Agregar Contenido Nuevo

### Nueva unidad
```python
table.put_item(Item={
    "pk": "UNIDAD#dermatologia",
    "sk": "META",
    "id": "dermatologia",
    "nombre": "Dermatología",
    "descripcion": "Productos para el cuidado de la piel",
    "entity_type": "UNIDAD_NEGOCIO",
    "icono": ""
})
```

### Nuevo tratamiento
```python
table.put_item(Item={
    "pk": "UNIDAD#dermatologia",
    "sk": "TRATAMIENTO#acne",
    "id": "acne",
    "nombre": "Acné",
    "entity_type": "TRATAMIENTO"
})
```

### Nuevo producto
```python
table.put_item(Item={
    "pk": "TRATAMIENTO#acne",
    "sk": f"PRODUCTO#{uuid.uuid4()}",
    "id": str(uuid.uuid4()),
    "slug": "tretinoina",
    "nombre": "Tretinoína",
    "descripcion": "Retinoide tópico para el acné",
    "entity_type": "PRODUCTO",
    "media": {
        "principal": "https://lasante-3d-catalog-assets.s3.us-east-1.amazonaws.com/tretinoina/principal.jpg",
        "miniatura": "https://lasante-3d-catalog-assets.s3.us-east-1.amazonaws.com/tretinoina/miniatura.jpg",
        "galeria": [],
        "3d": {"glb": "", "usdz": "", "preview": ""}
    }
})
```

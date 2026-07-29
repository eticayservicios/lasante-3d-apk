# Problemas Detectados en Lambda `lasante-3d-home`

**Fecha**: 2025-01-30  
**Revisado por**: Equipo Mobile  
**Archivo**: `backend/home/lambda_function.py`

---

## 🔴 Problemas Críticos (Alta Prioridad)

### 1. **Múltiples queries innecesarias a DynamoDB**
**Ubicación**: `lambda_function.py` líneas 44-51

**Problema**:
```python
# Buscar por UUID
if tratamiento_id_uuid:
    productos += list_productos(tratamiento_id_uuid)
# Buscar por cada id lógico
for id_logico in ids_logicos:
    productos += list_productos(id_logico)
```


**Impacto**:
- Si hay 10 tratamientos con 5 items cada uno = **50+ queries a DynamoDB**
- Alto costo en RCU (Read Capacity Units)
- Latencia elevada (cada query ~10-50ms)
- Timeout potencial en Lambda si el catálogo crece

**Solución recomendada**:
- Recolectar todos los `tratamiento_ids` primero
- Hacer queries en batch o usar `BatchGetItem`
- Reducir de 50+ queries a ~10 queries

---

### 2. **SCAN completo de tabla en búsqueda de productos**
**Ubicación**: `producto_util.py` líneas 18-35

**Problema**:
```python
# Buscar por campo 'id' en toda la tabl/home/andrea/proyectos/lasante/mobile/app/docs/PROBLEMAS_BACKEND.mda (scan)
scan_kwargs = {
    "FilterExpression": Attr("sk").begins_with("PRODUCTO#")
}
response = t.scan(**scan_kwargs)
```

**Impacto**:
- **SCAN lee TODA la tabla** (extremadamente costoso)
- Consumo masivo de RCU
- Latencia de segundos si la tabla tiene miles de registros
- No escala

**Solución recomendada**:
- Crear un **GSI (Global Secondary Index)** con:
  - PK: `id` (slug lógico como "paracetamol")
  - SK: `tipo` (PRODUCTO, TRATAMIENTO, etc)
- Usar `query()` en lugar de `scan()`

---

### 3. **Duplicación de tratamientos en respuesta**
**Ubicación**: `lambda_function.py` líneas 53-54

**Problema**:
```python
item["tratamientos"] = tratamientos_con_productos
tratamientos_total.extend(tratamientos_con_productos)
```

**Impacto**:
- Los tratamientos aparecen duplicados:
  - En `unidad.items[].tratamientos[]`
  - En `unidad.tratamientos[]`
- Payload de respuesta más grande (más datos transferidos)
- Confusión en el cliente sobre cuál usar

**Solución recomendada**:
- Decidir una sola ubicación para tratamientos
- Eliminar duplicados usando diccionario con key=tratamiento_id

---

### 4. **Enriquecimiento ineficiente de itemsDestacados**
**Ubicación**: `lambda_function.py` líneas 72-88

**Problema**:
```python
for coleccion in destacados:
    for item in coleccion.get("items", []):
        producto = get_producto_by_id(item["id"]) 
```

**Impacto**:
- Si hay 5 productos destacados = **5 SCANS completos de tabla**
- Latencia acumulada de varios segundos
- Consumo masivo de RCU

**Solución recomendada**:
- Recolectar todos los `producto_ids` primero
- Hacer batch get en una sola operación
- Usar GSI para búsqueda por id lógico

---

## 🟡 Problemas de Diseño (Media Prioridad)

### 5. **Estructura de datos inconsistente**

**Problema**:
- Algunos productos tienen `pk=TRATAMIENTO#hipertension` (slug)
- Otros tienen `pk=TRATAMIENTO#22456e77-ee03-4da8-8043-49dae6341fff` (UUID)
- Mezcla de identificadores dificulta búsquedas

**Impacto**:
- Lógica compleja para buscar productos
- Bugs potenciales al agregar nuevos productos
- Difícil de mantener

**Solución recomendada**:
- Estandarizar: usar **siempre UUID** en PK/SK
- Usar GSI para búsquedas por slug/id lógico
- Migrar datos existentes

---

### 6. **Sin caché**

**Problema**:
- Cada request hace todas las queries desde cero
- No aprovecha invocaciones "warm" de Lambda

**Impacto**:
- Latencia innecesaria en requests repetidos
- Costo elevado en DynamoDB

**Solución recomendada**:
- Implementar caché en memoria para invocaciones warm
- Considerar ElastiCache/Redis para caché distribuido
- TTL de 5-10 minutos

---

### 7. **Logs excesivos en producción**

**Problema**:
```python
log.info(f"Unidades encontradas: {unidades}")
log.info(f"Estructura final de unidades: {unidades_con_detalles}")
```

**Impacto**:
- CloudWatch Logs lleno de estructuras JSON gigantes
- Costo elevado en almacenamiento de logs
- Posible exposición de datos sensibles

**Solución recomendada**:
- Loguear solo contadores: `log.info(f"Unidades encontradas: {len(unidades)}")`
- Usar nivel DEBUG para estructuras completas
- Configurar retención de logs (7-30 días)

---

## 🟢 Mejoras Opcionales (Baja Prioridad)

### 8. **Sin paginación**

**Problema**:
- Retorna todo el catálogo en una sola respuesta
- No escala si el catálogo crece a 100+ productos

**Solución recomendada**:
- Implementar paginación con `limit` y `nextToken`
- Considerar si es necesario para caso de uso (TV kiosk)

---

### 9. **Sin manejo de concurrencia**

**Problema**:
- Si dos usuarios modifican productos simultáneamente, puede haber inconsistencias

**Solución recomendada**:
- Usar DynamoDB Transactions para escrituras
- Implementar optimistic locking con `version` field

---

## 📊 Estimación de Impacto

### Situación Actual:
- **Queries por request**: ~60-80
- **Latencia promedio**: 2-4 segundos
- **Costo mensual DynamoDB** (estimado): $50-100 USD

### Con Optimizaciones:
- **Queries por request**: ~10-15 (reducción 80%)
- **Latencia promedio**: 300-500ms (reducción 85%)
- **Costo mensual DynamoDB** (estimado): $10-20 USD (reducción 80%)

---

## 🎯 Plan de Acción Recomendado

### Sprint 1 (Crítico - 1 semana):
1. ✅ Crear GSI para búsqueda por id lógico
2. ✅ Reemplazar `scan()` por `query()` en `get_producto_by_id()`
3. ✅ Implementar batch queries para tratamientos
4. ✅ Eliminar duplicación de tratamientos

### Sprint 2 (Importante - 1 semana):
5. ✅ Implementar caché en memoria
6. ✅ Optimizar logs (solo contadores)
7. ✅ Estandarizar estructura de IDs (UUID vs slug)

### Sprint 3 (Opcional - 1 semana):
8. ✅ Implementar paginación (si es necesario)
9. ✅ Agregar DynamoDB Streams para invalidación de caché
10. ✅ Implementar monitoring con CloudWatch Metrics

---

## 📎 Archivos de Referencia

- **Código actual**: `backend/home/lambda_function.py`
- **Utilidades**: `backend/home/producto_util.py`, `backend/home/ddb.py`
- **Propuesta optimizada**: `backend/home/lambda_function_v2.py` (adjunto)
- **Batch get optimizado**: `backend/home/batch_get_productos.py` (adjunto)

---

## 🤝 Contacto

Para dudas o aclaraciones sobre este reporte, contactar al equipo mobile.

**Nota**: Los archivos `lambda_function_v2.py` y `batch_get_productos.py` contienen implementaciones de referencia con las optimizaciones propuestas.

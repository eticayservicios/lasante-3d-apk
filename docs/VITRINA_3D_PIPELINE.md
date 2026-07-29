# Pipeline 3D Vitrina — Estado actual

**Objetivo:** vitrina rotativa con productos ficticios decorativos integrados + productos destacados programables desde admin, encuadre estable en Infinix y TV 1080p.

**Última actualización:** 2026-07-06 (tarde — GLB Samuel recibido, calibración cilindro)

**Mockup visual Intro (Cursor Canvas):**  
`~/.cursor/projects/home-andrea-proyectos-lasante-mobile-app/canvases/intro-vitrina-expectations.canvas.tsx`

---

## 1. Dos tipos de producto (acuerdo diseño / jefe)

| Tipo | Origen | Ubicación en vitrina | ¿Admin? | ¿Clickeable? |
|------|--------|----------------------|---------|--------------|
| **Ficticios / decorativos** | Integrados en el GLB base (`tripo_node_*`) | Compartimentos (estantes inferiores/medios) | **No** | No |
| **Destacados / programables** | API / admin → URL GLB CloudFront | **Estante superior** — 4 por cara | **Sí** | **Sí** → modal 3D + QR |

**Regla de oro:** los ficticios **nunca** se cargan desde Kotlin. Los destacados **sí** (`VitrinaAssets`, `VitrinaAnchorResolver`).

---

## 2. Pantalla Intro — composición esperada

```
┌─────────────────────────────────────────────────────────────┐
│              PRODUCTOS ESTRELLA (título fondo)               │
│  [ig f in]                              [Nuestra historia]  │
│  scanea...                                                    │
│         ┌─────────────────────────────┐   [Selecciona y    │
│         │                             │    rota]           │
│         │     VITRINA 3D (protagonista)│                    │
│         │  · ficticios en compartimentos│                    │
│         │  · 4 destacados arriba (admin)│                   │
│         │  · 5 caras × 72° (drag bordes)│                   │
│         └─────────────────────────────┘                      │
│                                          [Logo La Santé]    │
└─────────────────────────────────────────────────────────────┘
```

| Elemento | Estado |
|----------|--------|
| Vitrina 3D centro (~85% área útil) | Objetivo |
| Redes sociales (izquierda) | ✅ Compose |
| Nuestra historia (arriba derecha) | ✅ |
| Selecciona y rota (centro derecha) | ✅ |
| Logo (abajo derecha) | ✅ |
| Cinta inferior (`lower_banner`) | ❌ **Eliminada** |
| Franja gris bajo vitrina | ❌ **Eliminada** |

---

## 3. Resumen ejecutivo — capas del sistema

| Capa | Archivo / código | Estado hoy |
|------|------------------|------------|
| Vitrina base + ficticios | **`bronx.glb`** (activo) | bbox ~22 u; calibrado |
| Export definitivo (Samuel) | `bronx.glb` o reemplazo | Solo actualizar `BASE_GLB_MAX_EXTENT` |
| Anclajes destacados | `featured_1…4` / `slot_1…4` | Resolver listo; bronx sin empties aún |
| Productos destacados 3D | GLB remoto / assets + tap Compose | ✅ Overlay ON + `FeaturedProductTapLayer` |
| Cámara / escala | `baseScaleToFill()` por perfil | ✅ phone_landscape / portrait / tv |
| Layout Compose | `IntroLayoutMetrics` | ✅ phone_landscape / tv_1080 |
| Admin | Slots + GLB producto | ✅ Existe; QA pendiente |

---

## 4. Archivos GLB en assets

| Archivo | Bbox max | Uso |
|---------|----------|-----|
| **`bronx.glb`** | ~228 u (`Cube.001` sin Apply Scale) | **BASE_GLB activo** |
| **`Sin_nombre.glb`** | ~3,9 u (Y-up) | Export Andrea sin `Plane`; listo para reemplazar bronx |
| `base_rotating.glb` | ~73 u | Iteración anterior + `slot_1…4` |
| `product_combo_acetaminofen.glb` | ~228 u (38 meshes) | **Roto** — lista negra en `VitrinaAssets` |
| `box.glb`, `bottle_liquid.glb`, `frasco.glb` | OK | Fallback destacados dev |

### Validación `Sin_nombre.glb` (2026-07-06 tarde)

| Check | Resultado |
|-------|-----------|
| `Plane` eliminado | ✅ |
| `Cube.001` bbox | ✅ ~2,7 u (Ctrl+A aplicado) |
| Bbox total | ✅ ~3,9 u en Y |
| Productos ficticios (tripo) | ✅ 26 nodos |
| `featured_1…4` | ❌ No exportados aún |
| Orientación glTF | Y-up estándar |

**Próximo paso:** añadir empties, reexportar y sustituir `bronx.glb` → quitar hacks Kotlin.

---

## 5. Lo hecho (2026-07-05 → 2026-07-06)

### Android / Kotlin ✅

- [x] `VitrinaAnchorResolver` — `featured_1…4` y `slot_1…4`
- [x] Destacados como hijos de `baseNode` (cuando overlay ON)
- [x] Fallback `VitrinaSlotConfig` (modo `LEGACY`)
- [x] Separación ficticios vs destacados
- [x] Cinta `lower_banner.glb` eliminada
- [x] Modal producto; tap temporal en vitrina si overlay OFF
- [x] `configureBronxForDisplay()` — oculta `Plane`, placeholders, tripo corrupto
- [x] `bronxScaleToUnits()` + cámara dinámica (bbox inflado)
- [x] Lista negra `product_combo_acetaminofen.glb`
- [x] Franja gris Compose eliminada
- [x] `BASE_GLB` = **`bronx.glb`** (restaurado tras prueba Sin_nombre)
- [x] Canvas expectativa Intro (`intro-vitrina-expectations.canvas.tsx`)

### Blender (Andrea) 🔄

- [x] `Plane` = piso → eliminar
- [x] `Cube.001` = estantes/separadores → conservar + Ctrl+A
- [x] Export `Sin_nombre.glb` validado (bbox OK)
- [ ] Empties `featured_1…4` en estante superior
- [ ] Merge a `bronx.glb` o renombrar export final
- [ ] Eliminar placeholders `Cylinder`–`003` en Blender

### Commits

- `142217f` — anclajes en `develop` (`base_rotating.glb`)
- Cambios 2026-07-06 — **sin commit** (bronx, cinta, overlay, escala, canvas)

---

## 6. Lo que esperamos de Samuel

### 6.1 GLB base final

```
vitrina_final.glb
├── Estructura (Cube.001–003, Cylinder.004–006)
├── tripo_node_*              → ficticios (decoración)
├── featured_1 … featured_4   → empties estante SUPERIOR
└── (opcional) cam_vitrina
```

Nombres alternativos aceptados: `slot_1…slot_4`.

### 6.2 Checklist Blender

| # | Tarea |
|---|-------|
| 1 | Borrar `Plane` |
| 2 | Conservar `Cube.001` (separadores) con **Ctrl+A** |
| 3 | Escala ~1,8–2 m; origen centrado |
| 4 | Empties `featured_1…4` arriba |
| 5 | Export **+Y Up**; verificar en [gltf-viewer](https://gltf-viewer.donmccurdy.com/) |
| 6 | Sin meshes que inflen bbox (>10 u basura) |

### 6.3 Productos admin (GLB sueltos)

- Solo la malla del producto (~0,2–0,4 m alto)
- URL HTTPS en `media.modelo3d.glb`
- **No** re-exportar la escena vitrina entera

### 6.4 Cuando llegue el GLB limpio — Kotlin

### Al recibir GLB de Samuel

1. Sustituir `app/src/main/assets/vitrina/models/bronx.glb`
2. Medir bbox max del export y actualizar en `VitrinaConstants.kt`:
   - `BASE_GLB_MAX_EXTENT` (ej. ~4 u si export limpio)
   - `BASE_GLB_VISIBLE_HEIGHT` (altura visible en Y)
3. Si el export incluye `featured_1…4`, el resolver los usará automáticamente
4. Revisar/vaciar `BASE_GLB_HIDDEN_MESH_NAMES` si ya no hay placeholders
5. Ajustar `baseScaleToFill(0.72f)` solo si el encuadre queda chico o grande

5. Cámara desde `VitrinaSlotConfig` normal

---

## 7. Nomenclatura Blender

| Objeto | Qué es | Acción |
|--------|--------|--------|
| `Plane` | Piso decorativo | Eliminar |
| `Cube.001` | Estantes / separadores | Conservar + Ctrl+A |
| `Cube.002`, `Cube.003` | Estructura | Conservar |
| `Cylinder.004`–`006` | Discos estantes | Conservar |
| `Cylinder`–`003` | Placeholders frascos | Eliminar u ocultar en app |
| `tripo_node_*` | Ficticios | Conservar |
| `Empty.002` | Padre colección | Ctrl+A antes de exportar |

> El mesh interno de `Cube.001` se llama `Cube.002` en el glTF — nombres cruzados del export.

---

## 8. Estado técnico Android

### Archivos clave

| Archivo | Rol |
|---------|-----|
| `VitrinaConstants.kt` | `BASE_GLB`, hidden meshes, hacks escala |
| `VitrinaGlbSetup.kt` | `configureBronxForDisplay()` |
| `VitrinaModelViewer.kt` | Escena 3D, cámara, rotación 72° |
| `VitrinaAnchorResolver.kt` | Empties GLB |
| `VitrinaSlotConfig.kt` | Fallback + perfiles dispositivo |
| `VitrinaAssets.kt` | GLB destacados + lista negra |
| `IntroLandscapeLayout.kt` | Overlays Intro |
| `IntroVitrina.kt` | Contenedor vitrina + drag |

### Constantes (`VitrinaConstants.kt`)

```kotlin
BASE_GLB = "vitrina/models/bronx.glb"
BRONX_SKIP_FEATURED_3D_OVERLAY = true
baseModelRotation = Rotation()          // sin corrección X=90°
BRONX_HIDDEN_MESH_NAMES = Plane, Cylinder.009, Cylinder…003, tripo corrupto
bronxScaleToUnits()                       // solo mientras bronx tenga bbox 228 u
bronxCameraDistance() / bronxCameraHeight()
```

### Flujo runtime

```
IntroScreen
  └── IntroResponsiveLayout (overlays)
        └── BusinessUnitVitrina
              └── VitrinaModelViewer
                    └── Node (rotación 72°)
                          └── Node (baseModelRotation)
                                └── ModelNode (bronx.glb)
                                      ├── configureBronxForDisplay()
                                      ├── bronxScaleToUnits + cámara alejada
                                      └── [overlay ON] × 4 destacados en anclas
```

---

## 9. Problemas conocidos

| Síntoma | Causa | Solución |
|---------|-------|----------|
| Vitrina invisible / minúscula | bbox 228 u + cámara | Usar `Sin_nombre.glb` limpio o Ctrl+A en bronx |
| Productos gigantes | `product_combo_*.glb` = escena entera | Lista negra; GLB suelto |
| Separadores ocultos | Se ocultaba `Cube.001` por error | Corregido |
| Export “de lado” en Blender | Z-up vs Y-up al reimportar | Verificar en gltf-viewer |
| Doble productos | bronx + overlay destacados | Overlay OFF hasta GLB final |
| Navegación unidades | Cinta eliminada | UX nueva pendiente |

---

## 10. Pendiente

### Alta 🔴

- [ ] `featured_1…4` en export Blender
- [ ] Reemplazar `bronx.glb` por export limpio (`Sin_nombre` o merge)
- [ ] Quitar hacks escala; reactivar overlay destacados
- [ ] Encuadre OK Infinix + TV 1080p
- [ ] Commit + capturas para jefe
- [ ] Arreglar / eliminar `product_combo_acetaminofen.glb`

### Media 🟡

- [ ] QA CloudFront en 4 slots
- [ ] Tap por slot (hoy: primer destacado si overlay OFF)
- [ ] Navegación tratamientos (ex-cinta)
- [ ] `cam_vitrina` empty
- [ ] Validación GLB en admin

### Baja 🟢

- [ ] Preview vitrina admin
- [ ] Normalizar escala server-side

---

## 11. Definition of Done

### Demo jefe (esta semana)

- [x] Vitrina 3D + ficticios integrados
- [x] Rotación 5 × 72°
- [x] UI Intro completa (sin cinta / franja gris)
- [x] Canvas expectativa visual
- [ ] Encuadre presentable Infinix + TV
- [ ] Capturas finales

### Producción

- [ ] `anchors=4/4` en logcat
- [ ] Destacados remotos clickeables por slot
- [ ] Sin hacks escala
- [ ] GLB producto admin validado

---

## 12. Comandos

```bash
cd mobile/app && ./gradlew :app:installDebug
adb logcat -s VitrinaAnchor VitrinaLayout VitrinaGlb
adb -s 08864252AD102210 exec-out screencap -p > /tmp/vitrina.png
```

Log anclas esperado (cuando existan empties):

```
VitrinaAnchor: mode=GLTF anchors=4/4 names=[featured_1, …] legacy=0
```

---

## 13. Contrato API — destacados

```json
{
  "productoId": "...",
  "unidadId": "genericos",
  "media": { "modelo3d": { "glb": "https://cdn.../producto.glb" } },
  "atributos": { "slot": "1" }
}
```

Slot `1–4` → índice visual `0–3`. Fallback dev: `box.glb`, `bottle_liquid.glb`, `frasco.glb`.

---

## 14. Historial de decisiones

| Fecha | Decisión |
|-------|----------|
| 2026-07-05 | Anclajes `slot_1…4` en `base_rotating.glb` |
| 2026-07-06 | Base interina `bronx.glb` + ficticios Tripo |
| 2026-07-06 | Eliminar cinta y franja gris Compose |
| 2026-07-06 | Ficticios en GLB; destacados desde admin arriba |
| 2026-07-06 | `Cube.001` = separadores; `Plane` = basura |
| 2026-07-06 | `Sin_nombre.glb` validado (bbox 3,9 u) pero app usa `bronx` hasta merge |
| 2026-07-06 | Canvas `intro-vitrina-expectations` para mockup jefe |
| 2026-07-06 | Overlay destacados OFF; hack escala solo para bronx viejo |

---

## 15. Referencias

- Doc funcional: `docs/Diseño Funcionabildad Vitrina (1).md`
- Canvas Intro: `canvases/intro-vitrina-expectations.canvas.tsx` (proyecto Cursor)
- Brecha: `docs/ANALISIS_BRECHA_FUNCIONAL.md`

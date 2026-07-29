# La Santé 3D — Estado funcional y pendientes

**Fecha:** 22 de julio de 2026  
**Proyecto:** APK kiosco TV (`lasante-3d-apk`) + Admin (`lasante-3d-devops`)  
**Último commit APK:** `e4262d6` en `develop` (push hecho)  
**Último commit Admin:** `fc30df7` en `dev` (push hecho)

---

## 1. Resumen ejecutivo

| Área | Estado | Comentario |
|:---|:---:|:---|
| Flujo principal (Intro → Tratamientos → Productos → modal 3D) | ✅ Operativo | Listo para uso en tienda |
| Vitrina 3D + admin (slots, tiempos, videos) | ✅ Operativo | Conectado a `GET /home` |
| Screen saver por inactividad | ✅ Configurado 3 min | API en vivo: `screenSaverAfterMs = 180000` |
| Reproducción de videos screen saver | 🟡 Pendiente QA | Fix de URLs aplicado en APK; falta confirmar en dispositivo |
| Caché al volver a Home | ✅ Mejorado | Intro permanece montada + catálogo en ViewModel |
| Pantalla apagada / app en segundo plano | ✅ Corregido | Pausa videos; no cuenta inactividad en background |
| Ocultar productos al girar vitrina | ❌ Revertido | Por decisión de producto: productos siempre visibles salvo screen saver |
| Funciones avanzadas (audio, videos de producto, etc.) | ⏸ Pendiente de decisión | Ver tablas §4 |

---

## 2. Lo que YA funciona

| # | Funcionalidad | Admin | APK | Notas |
|:---:|:---|:---:|:---:|:---|
| 1 | Vitrina 3D con 5 unidades y 4 productos por unidad | ✅ | ✅ | Slots desde Admin > Vitrina |
| 2 | Rotación manual (drag + botón Rota) | — | ✅ | Gestos corregidos (productos ya no bloquean drag) |
| 3 | Auto-rotación tras inactividad (2 min, configurable) | ✅ | ✅ | `autoRotateAfterMs = 120000` |
| 4 | Screen saver con playlist de videos (3 min, configurable) | ✅ | ✅ | DynamoDB + defaults en lambdas |
| 5 | Productos visibles mientras la vitrina gira | — | ✅ | Decisión actual de producto (no ocultar al rotar) |
| 6 | Blur de vitrina al abrir modal de producto | — | ✅ | `blur(20.dp)` restaurado |
| 7 | Click en producto destacado → modal con modelo 3D | ✅ | ✅ | GLB desde catálogo |
| 8 | Flag `modalEnabled` por slot | ✅ | ✅ | |
| 9 | Click en unidad activa → tratamientos | — | ✅ | |
| 10 | Botón “Nuestra Historia” → video (Video 1 playlist) | ✅ | ✅ | |
| 11 | Redes sociales → QR | — | ✅ | URLs fijas Pharmetique en APK |
| 12 | Tratamientos + “Ver todo” | ✅ | ✅ | |
| 13 | Grid productos, búsqueda, filtros, orden A–Z | ✅ | ✅ | |
| 14 | Modal producto en grilla con GLB | ✅ | ✅ | |
| 15 | Sonido de click en botones | — | ✅ | |
| 16 | Volver a Home sin loading de catálogo | — | ✅ | `IntroViewModel` + Intro montada en NavHost |

---

## 3. Correcciones recientes (build `e4262d6`)

| # | Problema | Solución |
|:---:|:---|:---|
| 1 | Screen saver no activaba a tiempo / timer reseteado por auto-rotación | Timer de inactividad separado de rotación cada 8 s |
| 2 | Videos no reproducían (URLs con espacios y acentos) | Codificación de path + `play()` + salto al siguiente si falla |
| 3 | App se cerraba en 2.º video screen saver | ExoPlayer: `pause()` + `release()` + `player = null` |
| 4 | Vitrina lenta al volver de Tratamientos | Intro 3D permanece en memoria; catálogo cacheado |
| 5 | Drag no rotaba (productos se ocultaban al arrastrar) | Productos siempre visibles; gestos activos durante drag |
| 6 | Fondo oscuro raro en modal de producto | Eliminado overlay negro; se mantiene blur original |
| 7 | Screen saver a 5 min en API | Backend actualizado a **3 min** (`180000 ms`) |

---

## 4. Pendientes funcionales

### 4.1 Confirmar en dispositivo (QA — bloqueante para cierre)

| # | Prueba | Estado | Notas |
|:---:|:---|:---:|:---|
| Q1 | Screen saver a los **3 min** con videos | ☐ Pendiente | Usuario probando |
| Q2 | Segundo ciclo de videos sin crash | ☐ Pendiente | |
| Q3 | Volver Home desde Tratamientos (vitrina instantánea) | ☐ Pendiente | |
| Q4 | Drag + botón Rota con productos visibles | ☐ Pendiente | |
| Q5 | Modal producto con blur de fondo | ☐ Pendiente | |

### 4.2 Deploy / repos

| # | Tarea | Estado | Notas |
|:---:|:---|:---:|:---|
| R1 | Push `main` APK (`e4262d6`) | 🟡 Local merge hecho | Falta `git push origin main` |
| R2 | Merge + push `main` admin (`fc30df7`) | ☐ Pendiente | Solo `dev` pusheado |
| R3 | Deploy lambdas SAM (home cache vitrina) | ☐ Pendiente | `_refresh_vitrina_config` en `lasante-3d-home` |

### 4.3 Decisiones de producto (sin implementar)

| # | Funcionalidad | Prioridad | Decisión |
|:---:|:---|:---:|:---:|
| A1 | Audio narración (“Escuchar”) | 🟡 | ☐ SÍ · ☐ NO · ☐ Después |
| A2 | Videos de producto en modal | 🟡 | ☐ SÍ · ☐ NO · ☐ Después |
| A3 | Rotación manual del GLB en modal | 🟢 | ☐ SÍ · ☐ NO · ☐ Después |
| B1 | Iniciar app en screen saver | 🟢 | ☐ SÍ · ☐ NO · ☐ Después |
| B2 | Video institucional separado de playlist | 🟡 | ☐ SÍ · ☐ NO · ☐ Después |
| B3 | Botón Catálogo en footer | 🟢 | ☐ SÍ · ☐ NO · ☐ Después |
| B4 | Redes sociales configurables desde admin | 🟢 | ☐ SÍ · ☐ NO · ☐ Después |
| C1 | Búsqueda remota con media completa | 🟡 | ☐ SÍ · ☐ NO · ☐ Después |
| D1 | Quitar descarga GLB al inicio (~26 MB) | 🔴 | ☐ SÍ · ☐ NO · ☐ Después |
| D3 | Modo kiosco (pantalla siempre encendida) | 🟡 | ☐ SÍ · ☐ NO · ☐ Después |

### 4.4 Tiempos configurados en API (22-jul-2026)

| Parámetro | Valor | Origen |
|:---|:---|:---|
| `autoRotateAfterMs` | 120000 (2 min) | DynamoDB |
| `screenSaverAfterMs` | **180000 (3 min)** | DynamoDB (actualizado) |
| Playlist screen saver | 5 videos, `enabled: true` | Admin > Vitrina > Videos |

---

## 5. Checklist QA en dispositivo

| # | Prueba | Resultado esperado | OK / FAIL |
|:---:|:---|:---|:---:|
| 1 | Abrir app → vitrina 3D | Carga sin error | ☐ |
| 2 | 2 min sin tocar | Gira sola; productos **siguen visibles** | ☐ |
| 3 | Drag horizontal en vitrina | Rota ±1 unidad | ☐ |
| 4 | Tap producto estrella | Modal 3D con **blur** de fondo | ☐ |
| 5 | Tap unidad (cintillo) | Navega a tratamientos | ☐ |
| 6 | Nuestra Historia | Reproduce video | ☐ |
| 7 | **3 min** sin tocar | Screen saver pantalla completa + videos | ☐ |
| 8 | Apagar pantalla | No suena video; al encender, vitrina normal | ☐ |
| 9 | 2.º ciclo screen saver | App no se cierra | ☐ |
| 10 | Tratamientos → Home | Vitrina **sin** espera de carga | ☐ |
| 11 | Tratamientos → productos → búsqueda | Flujo completo | ☐ |

---

## 6. Referencias

| Documento | Ubicación |
|:---|:---|
| Diseño funcional general | `docs/DISENO_FUNCIONAL.md` |
| Documento vitrina 3D | `docs/Diseño Funcionabildad Vitrina (1).md` |
| Auditoría rendimiento | `docs/AUDITORIA_RENDIMIENTO_APK.md` |
| Matriz admin ↔ APK | `admin/3d-admin/MATRIZ_VITRINA_APK_ADMIN.md` |

---

## 7. Firmas / aprobación

| Rol | Nombre | Fecha | Observaciones |
|:---|:---|:---|:---|
| Producto / Dirección | | | |
| Desarrollo | | | |
| QA | | | |

# LaSanté TV Kiosk (Android)

Proyecto base para kiosco en **televisores táctiles** con soporte opcional de **D-pad**.

## Stack (Sprint 1)

- Kotlin + Jetpack Compose
- Navigation Compose
- Media3/ExoPlayer (audio)
- 3D: stub listo para integrar SceneView
- Backend: **sin login** (kiosco), siempre online

## Cómo abrir y correr

Este repositorio no incluye Android SDK (en Linux a veces solo está `adb`, pero faltan `platforms/` y `build-tools/`).

1) Instala Android Studio.
2) Abre esta carpeta como proyecto Gradle.
3) En Android Studio → **SDK Manager**, instala:
	- Android SDK Platform (API 35)
	- Android SDK Build-Tools (34.x o superior)
4) Asegura que Gradle apunte a tu SDK:
	- Opción A: Android Studio lo crea solo.
	- Opción B: crea `local.properties` en la raíz con:
	  - `sdk.dir=/home/<tu_usuario>/Android/Sdk`
4) Ejecuta la configuración **app** en un emulador o dispositivo.

Notas:
- El permiso de internet ya está configurado.
- La app hoy usa datos mock; el objetivo es iterar UI/flujo sin bloquearse por backend.

## Flujo actual

Intro → Home → Tratamientos → Productos → Detalle

- Intro: reproduce el video de apertura (mute/autoplay) y permite continuar con tap o Enter.
	- URL actual: `https://mobile.lasante.com.ve/video.mp4` (sale de `mobile/3d/index.html` que usa `video.mp4`).
	- Config: `WebAssets.introVideoUrl`.

- Touch + D-pad: al tocar un card, ese card pide foco (patrón kiosco).
- Detalle: botón “Escuchar” reproduce un `audioUrl` (por ahora en mock está en null).
- 3D: `ModelViewerStub` muestra el estado; aquí se enchufa SceneView.

## Siguientes pasos

1) Completar `modelUrl` / `audioUrl` (CloudFront) por producto vía API.
2) Sustituir `ModelViewerStub` por el visor 3D de producción donde aún aplique.

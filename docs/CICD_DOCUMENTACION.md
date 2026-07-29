# CI/CD - LaSante TV Kiosk Android

**Fecha**: 2026-04-30  
---

## Resumen

Cada vez que se hace un push al repositorio GitHub, se compila automáticamente la app Android y el APK resultante se sube a un bucket S3. El backend puede consultar un archivo `manifest.json` en S3 para saber la URL del APK más reciente y ofrecérsela a los kioscos para descarga/actualización.

---

## Repositorio GitHub

| Campo | Valor |
|---|---|
| URL | https://github.com/eticayservicios/lasante-3d-apk |
| Rama producción | `main` |
| Rama staging | `develop` |
| Workflow | `.github/workflows/android-cicd.yml` |

---

## Infraestructura AWS

### Bucket S3

| Campo | Valor |
|---|---|
| Nombre | `lasante-apk-releases` |
| Región | `us-east-1` |
| URL base | `https://lasante-apk-releases.s3.us-east-1.amazonaws.com` |

**Estructura de carpetas en S3:**
```
lasante-apk-releases/
├── android/
│   ├── debug/
│   │   ├── lasante-tv-debug-<version>-<build>.apk   ← APK versionado
│   │   ├── lasante-tv-debug-latest.apk               ← siempre el más reciente
│   │   └── manifest.json                             ← metadata del último build
│   └── release/
│       ├── lasante-tv-release-<version>-<build>.apk
│       ├── lasante-tv-release-latest.apk
│       └── manifest.json
```

### IAM User (solo para GitHub Actions)

| Campo | Valor |
|---|---|
| Username | `lasante-github-actions` |
| Política | `lasante-apk-s3-policy` |
| Permisos | `s3:PutObject`, `s3:GetObject`, `s3:ListBucket` solo en `lasante-apk-releases` |
| Access Key ID | `ver secret AWS_ACCESS_KEY_ID en GitHub` |
| Secret Access Key | `ver secret AWS_SECRET_ACCESS_KEY en GitHub` |

> ⚠️ Estas credenciales están almacenadas como secrets en GitHub, no en el código.

---

## Secrets configurados en GitHub

Ubicación: `github.com/eticayservicios/lasante-3d-apk → Settings → Secrets and variables → Actions`

| Secret | Valor | Descripción |
|---|---|---|
| `AWS_ACCESS_KEY_ID` | *(ver en GitHub Secrets)* | IAM user para subir a S3 |
| `AWS_SECRET_ACCESS_KEY` | *(ver en GitHub Secrets)* | Secret del IAM user |
| `AWS_REGION` | `us-east-1` | Región del bucket |
| `S3_BUCKET` | `lasante-apk-releases` | Nombre del bucket |
| `KEYSTORE_BASE64` | *(base64 del archivo .jks)* | Keystore para firmar APK release |
| `KEYSTORE_PASSWORD` | `LaSante2024!` | Password del keystore |
| `KEY_ALIAS` | `lasante` | Alias de la key de firma |
| `KEY_PASSWORD` | `LaSante2024!` | Password de la key |

---

## Keystore (firma del APK)

El APK release debe estar firmado para instalarse en dispositivos Android. El keystore fue generado con:

| Campo | Valor |
|---|---|
| Archivo | `lasante-release.jks` |
| Alias | `lasante` |
| Password | `LaSante2024!` |
| Validez | 10.000 días (~27 años) |
| Organización | Pharmetique Labs |
| Algoritmo | RSA 2048 |

> ⚠️ El archivo `.jks` está guardado localmente en el equipo de desarrollo y NO está en el repositorio (está en `.gitignore`). Hacer backup de este archivo es crítico — si se pierde, no se pueden publicar actualizaciones firmadas.

**Ubicación local:** `/home/andrea/proyectos/lasante/mobile/app/lasante-release.jks`

---

## Cómo funciona el CI/CD

### Flujo completo

```
Developer hace push
        │
        ▼
GitHub Actions se dispara
        │
        ├── rama develop ──► assembleDebug ──► APK debug ──► S3/android/debug/
        │
        └── rama main ────► assembleRelease ──► APK release firmado ──► S3/android/release/
```

### Pasos del workflow

1. **Checkout** — clona el código
2. **Setup JDK 17** — configura Java
3. **Setup Android SDK** — instala herramientas Android
4. **Cache Gradle** — reutiliza dependencias para builds más rápidos
5. **Build APK** — compila debug o release según la rama
6. **Obtener versión** — lee `versionName` y `versionCode` del `build.gradle.kts`
7. **Deploy a S3** — sube el APK y genera `manifest.json`
8. **Artefacto GitHub** — guarda el APK en GitHub por 30 días

### Tiempo estimado por build

| Tipo | Primera vez | Builds siguientes (cache) |
|---|---|---|
| Debug | ~8-10 min | ~4-5 min |
| Release | ~10-12 min | ~5-6 min |

---

## manifest.json

Después de cada deploy exitoso, se genera/actualiza este archivo en S3. El backend debe leerlo para saber la URL del APK más reciente.

**URL del manifest (debug/staging):**
```
https://lasante-apk-releases.s3.us-east-1.amazonaws.com/android/debug/manifest.json
```

**URL del manifest (release/producción):**
```
https://lasante-apk-releases.s3.us-east-1.amazonaws.com/android/release/manifest.json
```

**Estructura del manifest.json:**
```json
{
  "version": "0.1.0",
  "build": "1",
  "branch": "main",
  "environment": "production",
  "filename": "lasante-tv-release-0.1.0-1.apk",
  "url": "https://lasante-apk-releases.s3.us-east-1.amazonaws.com/android/release/lasante-tv-release-0.1.0-1.apk",
  "latest_url": "https://lasante-apk-releases.s3.us-east-1.amazonaws.com/android/release/lasante-tv-release-latest.apk",
  "built_at": "2025-04-30T14:00:00Z",
  "commit": "abc123..."
}
```

**El backend puede:**
- Leer `latest_url` para siempre tener la URL del APK más reciente
- Leer `version` y `build` para mostrar qué versión está disponible
- Comparar `version` con la versión instalada en el kiosco para detectar actualizaciones

---

## Cómo hacer un nuevo release

### Deploy a staging (develop)
```bash
git checkout develop
# hacer cambios...
git add .
git commit -m "feat: descripción del cambio"
git push origin develop
# GitHub Actions compila y sube automáticamente
```

### Deploy a producción (main)
```bash
git checkout main
git merge develop
git push origin main
# GitHub Actions compila, firma y sube automáticamente
```

### Actualizar versión de la app

Editar `app/build.gradle.kts`:
```kotlin
versionCode = 2          // incrementar en 1 cada release
versionName = "0.2.0"   // versión semántica
```

---

## Permisos del bucket S3

Actualmente el bucket es **privado**. Si el backend necesita que los APKs sean descargables públicamente (sin autenticación), hay dos opciones:

**Opción A — Hacer el bucket público** (más simple):
```bash
aws s3api put-bucket-policy \
  --bucket lasante-apk-releases \
  --policy '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::lasante-apk-releases/*"
    }]
  }' \
  --profile desarrollador
```

**Opción B — Pre-signed URLs** (más seguro):
El backend genera URLs temporales de descarga con expiración. Requiere que el backend tenga credenciales AWS con permisos `s3:GetObject`.

---

## Verificar que funciona

### Ver el último APK en S3
```bash
aws s3 ls s3://lasante-apk-releases/android/debug/ --profile desarrollador
aws s3 ls s3://lasante-apk-releases/android/release/ --profile desarrollador
```

### Ver el manifest
```bash
aws s3 cp s3://lasante-apk-releases/android/debug/manifest.json - --profile desarrollador
```

### Ver historial de builds en GitHub
```
https://github.com/eticayservicios/lasante-3d-apk/actions
```

---

## Preguntas frecuentes

**¿Qué pasa si el build falla?**
El APK anterior en S3 no se sobreescribe. El `manifest.json` sigue apuntando al último build exitoso.

**¿Cómo sé qué versión está instalada en el kiosco?**
La app puede exponer su `versionName` y `versionCode` vía una API interna o comparar con el `manifest.json` al arrancar.

**¿Puedo hacer un build manual sin hacer push?**
Sí. En GitHub → Actions → Android CI/CD → Run workflow → seleccionar rama.

**¿Dónde están los logs de cada build?**
GitHub → Actions → click en el build → click en "Build APK".

**¿Qué pasa si se pierde el keystore?**
No se puede firmar con la misma key. Habría que desinstalar la app en todos los kioscos y reinstalar con la nueva firma. **Hacer backup del `.jks` es crítico.**

---
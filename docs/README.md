# Documentación - LaSanté TV Kiosk

Esta carpeta contiene la documentación técnica y funcional del proyecto.

> **Vitrina 3D (activo):** [`VITRINA_3D_PIPELINE.md`](VITRINA_3D_PIPELINE.md) — pipeline anclajes GLB, apartado **Diseñador 3D**, estado y pendientes (vitrina / cinta / productos).

---

## 📋 Documentos de Diseño (Base del Proyecto)

### 1. `DISENO_FUNCIONAL.md`
**Propósito:** Especificación funcional completa del sistema  
**Contenido:**
- Pantallas del sistema (Intro, Unidades, Productos, Detalle 3D)
- Elementos de cada pantalla con nombres de software
- Funcionalidades detalladas
- Flujos de navegación

**Audiencia:** Product Owners, Diseñadores, Desarrolladores

---

### 2. `DISENO_INFRAESTRUCTURA.md`
**Propósito:** Arquitectura de infraestructura AWS  
**Contenido:**
- Repositorios del proyecto (APK, Gestión, Backend, DevOps)
- Recursos AWS (API Gateway, Lambda, DynamoDB, S3, CloudFront)
- Estrategia de aprovisionamiento (AWS SAM)
- CI/CD con GitHub Actions

**Audiencia:** DevOps, Arquitectos, Backend Developers

---

### 3. `DISENO_SOFTWARE.md`
**Propósito:** Diseño de datos y APIs  
**Contenido:**
- Estructura de tabla DynamoDB (single-table design)
- Modelos de datos (Unidad, Tratamiento, Producto, Colección)
- Endpoints de API propuestos
- Criterios de diseño (campos fijos vs flexibles)

**Audiencia:** Backend Developers, Frontend Developers

---

### 4. `STACK_TECNICO.md`
**Propósito:** Decisiones técnicas y stack completo  
**Contenido:**
- Evaluación de frameworks (Jetpack Compose vs Flutter vs React)
- Stack frontend (Kotlin, Compose, Filament 3D, Media3)
- Stack backend (AWS Serverless)
- Arquitectura de la app (Clean Architecture)
- Flujos clave (3D, IA, QR)
- Plan de estudio del equipo

**Audiencia:** Tech Leads, Desarrolladores, Stakeholders técnicos

---

## 📊 Documentos de Estado Actual

### 5. `ESTADO_ACTUAL_FUNCIONAL.md`
**Propósito:** Estado de implementación vs diseño funcional  
**Contenido:**
- Checklist de funcionalidades implementadas (100%)
- Comparativa elemento por elemento
- Pendientes (dependencias de backend)
- Métricas de completitud
- Próximos pasos por sprint

**Audiencia:** Product Owners, QA, Stakeholders

**Última actualización:** 27 Abril 2025

---

### 6. `ANALISIS_BRECHA_FUNCIONAL.md`
**Propósito:** Análisis detallado de brechas funcionales  
**Contenido:**
- Comparativa diseño vs implementación
- Brechas identificadas con severidad
- Recomendaciones priorizadas
- Características adicionales implementadas

**Audiencia:** Product Owners, Tech Leads

**Última actualización:** 27 Abril 2025

---

## 🔧 Documentos Técnicos

### 7. `API_DOCUMENTACION.md`
**Propósito:** Referencia de APIs del backend  
**Contenido:**
- Endpoints disponibles
- Request/Response schemas
- Ejemplos de uso
- Códigos de error

**Audiencia:** Frontend Developers, QA

---

### 8. `GUIA_COMPILAR_INSTALAR.md`
**Propósito:** Guía de compilación y deployment  
**Contenido:**
- Compilar APK sin Android Studio
- Instalar en dispositivos
- Troubleshooting común
- Comandos útiles

**Audiencia:** DevOps, QA, Desarrolladores

---

### 9. `PROBAR_APK_TV_ANDROID_STUDIO.md`
**Propósito:** Probar la APK en emulador TV sin clonar el repo  
**Contenido:**
- Instalar `app-debug.apk` en emulador Android TV
- Simular perfiles TV 32" / 42" / 66" (`wm size` + `wm density`)
- Problemas frecuentes (APK vieja, fecha del emulador, SSL)

**Audiencia:** QA, jefes de proyecto, stakeholders sin acceso al código

---

## 📁 Estructura Recomendada de Lectura

### Para nuevos desarrolladores:
1. `DISENO_FUNCIONAL.md` - Entender qué hace la app
2. `STACK_TECNICO.md` - Entender cómo está construida
3. `DISENO_SOFTWARE.md` - Entender el modelo de datos
4. `ESTADO_ACTUAL_FUNCIONAL.md` - Ver qué está listo

### Para Product Owners:
1. `DISENO_FUNCIONAL.md` - Especificación base
2. `ESTADO_ACTUAL_FUNCIONAL.md` - Estado actual
3. `ANALISIS_BRECHA_FUNCIONAL.md` - Qué falta

### Para DevOps:
1. `DISENO_INFRAESTRUCTURA.md` - Arquitectura AWS
2. `GUIA_COMPILAR_INSTALAR.md` - Deployment
3. `STACK_TECNICO.md` - Stack completo

---

## 🗑️ Documentos Eliminados (Obsoletos)

Los siguientes documentos fueron removidos por ser temporales o ya aplicados:
- `ANALISIS_BRECHA.md` - Duplicado
- `APP_ACTUALIZADA_NUEVO_BACKEND.md` - Cambios ya aplicados
- `CAMBIO_BACKEND_HOME_COMPLETO.md` - Cambios ya aplicados
- `CAMBIOS_API_GURUSAWS.md` - Cambios ya aplicados
- `CAMBIOS_APLICADOS.md` - Temporal
- `CAMBIOS_BACKEND_LIMPIEZA.md` - Cambios ya aplicados
- `ESTADO_OPTIMIZACION.md` - Obsoleto
- `FIX_*.md` (15 archivos) - Fixes temporales ya aplicados
- `IMPLEMENTACION_BUSQUEDA_GLOBAL.md` - Ya implementado
- `INTEGRACION_*.md` (2 archivos) - Ya integrado

**Fecha de limpieza:** 27 Abril 2025

---

## 📝 Convenciones

### Actualización de documentos:
- Documentos de DISEÑO: Solo actualizar si hay cambios en requerimientos
- Documentos de ESTADO: Actualizar al final de cada sprint
- Documentos TÉCNICOS: Actualizar cuando cambie la implementación

### Nomenclatura:
- `DISENO_*.md` - Documentos de diseño base
- `ESTADO_*.md` - Documentos de estado actual
- `ANALISIS_*.md` - Análisis y comparativas
- `GUIA_*.md` - Guías prácticas
- `API_*.md` - Documentación de APIs

---

## 🔄 Historial de Cambios

### 27 Abril 2025
- ✅ Limpieza de 18 documentos obsoletos
- ✅ Creado `ESTADO_ACTUAL_FUNCIONAL.md`
- ✅ Actualizado `ANALISIS_BRECHA_FUNCIONAL.md`
- ✅ Creado este README

### Marzo 2025
- ✅ Documentos de diseño iniciales
- ✅ Stack técnico definido

---

## 📞 Contacto

Para preguntas sobre la documentación, contactar al equipo de desarrollo.

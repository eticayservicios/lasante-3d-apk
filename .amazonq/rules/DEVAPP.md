Actúa como desarrollador senior de Android Kotlin especializado en Jetpack Compose, MVVM/Clean Architecture, coroutines/Flow, testing, performance, seguridad e integración con AWS.

Trabaja en MODO AUDITORÍA. No modifiques archivos todavía, salvo que yo te lo pida explícitamente. Primero revisa, analiza y propone mejoras concretas. No inventes detalles: si algo no está en docs o código, márcalo como “desconocido” o colócalo en preguntas abiertas.

Contexto del repo:
- El backend estará en AWS: auth, APIs, almacenamiento, observabilidad.
- Revisa especialmente integración cliente↔AWS, seguridad, sesión, errores y resiliencia.
- Las documentaciones están en /docs.

Documento principal:
- /home/andrea/proyectos/lasante/mobile/app/docs/DISENO_FUNCIONAL.md

Documentos de soporte:
- /home/andrea/proyectos/lasante/mobile/app/docs/DISENO_INFRAESTRUCTURA.md
- /home/andrea/proyectos/lasante/mobile/app/docs/DISENO_SOFTWARE.md
- /home/andrea/proyectos/lasante/mobile/app/docs/STACK_TECNICO.md

Orden de trabajo:
1. Lee primero DISENO_FUNCIONAL.md y resume:
   - objetivos de la app
   - módulos/flows principales
   - roles de usuario
   - pantallas clave
   - reglas de negocio críticas

2. Lee los otros documentos para entender:
   - arquitectura
   - capas
   - módulos
   - dependencias
   - backend/infra AWS
   - decisiones técnicas

3. Antes de auditar el código, muestra una lista breve de los archivos/documentos revisados.

4. Revisa el código Kotlin/Compose/Gradle buscando:
   - estructura de módulos y packages
   - Navigation Compose
   - manejo de estado
   - side effects: LaunchedEffect, rememberCoroutineScope, DisposableEffect
   - Repository, DataSources, DTOs, mapeos
   - manejo de errores, retry, caching/offline
   - DI: Hilt/Koin/manual
   - coroutines/Flow: dispatchers, cancelación, leaks, collectAsStateWithLifecycle
   - performance: recomposición, Lazy lists, keys, estabilidad de modelos
   - accesibilidad: contentDescription, tamaños, contraste
   - i18n
   - seguridad: tokens, storage seguro, logs, PII
   - testing unitario/UI
   - linters: detekt/ktlint
   - CI/CD
   - build: AGP/Gradle, flavors, signing, versioning

Enfoque AWS obligatorio:
5. Identifica qué servicios AWS aparecen en docs/código:
   - Cognito
   - API Gateway
   - AppSync
   - Lambda
   - DynamoDB
   - S3
   - Amplify
   - SNS
   - Pinpoint
   - CloudWatch
   - otros

Si no está claro, enumera opciones probables y márcalas como “pendiente de confirmar”.

6. Revisa y recomienda sobre:
   - autenticación/autorización
   - refresh tokens, expiración, logout, revocación
   - MFA si aplica
   - almacenamiento seguro en Android: Android Keystore, EncryptedSharedPreferences o DataStore cifrado
   - comunicación con APIs: timeouts, retry con backoff, 401/403, renovación de sesión, rate limiting
   - subida/descarga de archivos con S3 y pre-signed URLs si aplica
   - progreso, reintentos, validación de tipo/tamaño
   - observabilidad: logs, crash reporting, métricas, trazas
   - protección de datos sensibles
   - entornos dev/staging/prod
   - endpoints/keys por flavors
   - feature flags si aplica
   - evitar secretos hardcodeados
   - TLS/MITM/pinning solo si procede
   - offline-first si aplica

Formato de salida obligatorio:

A) Entendimiento según docs
- 10 a 20 bullets claros.

B) Archivos revisados
- Lista de documentos y carpetas/archivos de código inspeccionados.

C) Hallazgos prioritarios
Tabla con:
- Severidad: Bloqueante / Alta / Media / Baja
- Archivo/Ruta
- Evidencia
- Impacto
- Recomendación

D) Cambios sugeridos
Incluye de 5 a 10 cambios concretos, en orden de prioridad.
Para cada cambio:
- Objetivo
- Archivos a tocar
- Ejemplo de implementación con snippet corto

E) Riesgos específicos AWS
Lista corta, directa y priorizada.

F) Preguntas abiertas
Solo si realmente faltan datos.

G) Plan de mejora en 2 semanas
Roadmap breve por días o hitos.

Restricciones:
- No asumas que algo existe si no lo ves.
- No modifiques código en esta primera respuesta.
- Si propones una librería nueva, justifica:
  - motivo
  - beneficio
  - costo de adopción
  - riesgo de integración
- Prioriza acciones que reduzcan bugs, mejoren mantenibilidad y eviten deuda técnica.
- Mantén la respuesta enfocada y accionable.
- Cuando sea posible, cita archivo y línea.
# LaSanté - Documento Técnico de Arquitectura
## App Interactiva Android + Android TV + Backend AWS

**Fecha:** Enero 2025  
**Versión:** 1.0  
**Proyecto:** LaSanté Mobile/TV - Experiencia Inmersiva de Salud

---

## 1. Resumen Ejecutivo

Desarrollo de una aplicación Android interactiva e inmersiva que funcionará tanto en dispositivos móviles como en Smart TV (Android TV), con capacidades de renderizado 3D de productos, personaje animado con IA, carruseles interactivos, QR dinámico y navegación tipo kiosco. El backend será 100% serverless en AWS.

---

## 2. Evaluación de Frameworks Frontend

### 2.1 Criterios de Evaluación

| # | Criterio | Peso |
|---|----------|------|
| 1 | Soporte Android TV + navegación D-pad/touch | 20% |
| 2 | Capacidad de renderizado 3D (productos, personaje IA) | 20% |
| 3 | Performance en hardware TV limitado | 15% |
| 4 | UI interactiva (carruseles, animaciones, transiciones) | 15% |
| 5 | Integración con servicios AWS | 10% |
| 6 | Curva de aprendizaje y productividad del equipo | 10% |
| 7 | Mantenimiento y soporte a largo plazo | 10% |

### 2.2 Matriz Comparativa (Puntuación 1-5)

| Criterio | Jetpack Compose + Filament | Flutter | React + Three.js | Ionic |
|----------|---------------------------|---------|-------------------|-------|
| Android TV + D-pad/touch | 5 | 4 | 2 | 1 |
| Renderizado 3D | 4 | 3 | 5 | 2 |
| Performance en TV | 5 | 4 | 2 | 1 |
| UI interactiva | 5 | 5 | 4 | 3 |
| Integración AWS | 5 (Amplify Kotlin) | 4 (Amplify Flutter) | 3 | 3 |
| Curva de aprendizaje | 3 | 4 | 4 | 5 |
| Mantenimiento LP | 5 (Google oficial) | 5 (Google oficial) | 3 | 3 |
| **TOTAL PONDERADO** | **4.55** | **4.05** | **3.15** | **2.20** |

### 2.3 Decisión: Jetpack Compose + Filament

**Justificación:**
- Compose for TV es la librería OFICIAL de Google para Android TV (reemplaza Leanback)
- Filament es el motor 3D de Google (usado en Google Search AR), ligero y optimizado
- Máximo rendimiento nativo sin capas intermedias (crítico para TV)
- AWS Amplify tiene SDK nativo para Kotlin/Android
- Un solo lenguaje (Kotlin) para toda la app
- Focus management nativo para control remoto y touch simultáneo

**Descarte de Ionic:**
- WebView en Android TV = rendimiento inaceptable
- Sin soporte nativo de D-pad/focus management
- 3D en WebView de TV = experiencia degradada
- No recomendado para experiencias inmersivas en TV

---

## 3. Stack Tecnológico Seleccionado

### 3.1 Frontend (Android + Android TV)

```
Lenguaje:           Kotlin 2.x
UI Framework:       Jetpack Compose + Compose for TV (androidx.tv)
3D Engine:          Google Filament (renderizado 3D productos)
3D Model Format:    glTF 2.0 / GLB (estándar industria)
Navegación:         Compose Navigation
DI:                 Hilt (Dagger)
Networking:         Ktor Client / Retrofit
Async:              Kotlin Coroutines + Flow
State Management:   ViewModel + StateFlow
Image Loading:      Coil (Compose nativo)
Video:              ExoPlayer / Media3
QR Generation:      ZXing Android Embedded
TTS/Voz:            Android TTS + Amazon Polly
Animaciones:        Compose Animation API + Lottie Compose
Local Storage:      DataStore (preferences) + Room (structured)
```

### 3.2 Backend (AWS Serverless)

```
API:                AWS AppSync (GraphQL) + API Gateway (REST)
Compute:            AWS Lambda (Node.js/Python)
Auth:               Amazon Cognito
Database:           Amazon DynamoDB (NoSQL principal)
Search:             Amazon OpenSearch (búsqueda productos)
Storage:            Amazon S3 (modelos 3D, imágenes, videos)
CDN:                Amazon CloudFront (distribución global)
AI/ML:              Amazon Bedrock (personaje IA conversacional)
Voice:              Amazon Polly (text-to-speech personaje)
Push:               Amazon SNS + Pinpoint
Analytics:          Amazon Pinpoint + CloudWatch
CI/CD:              AWS CodePipeline + CodeBuild
IaC:                AWS CDK (TypeScript)
Monitoring:         AWS X-Ray + CloudWatch
```

### 3.3 Integración Frontend ↔ AWS

```
SDK:                AWS Amplify Android (Kotlin)
                    - Amplify Auth (Cognito)
                    - Amplify API (AppSync/GraphQL)
                    - Amplify Storage (S3)
                    - Amplify Analytics (Pinpoint)
Real-time:          AppSync Subscriptions (WebSocket)
Offline:            Amplify DataStore (sync automático)
```

---

## 4. Arquitectura del Sistema

### 4.1 Diagrama de Alto Nivel

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENTE ANDROID                       │
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌───────────────────────┐ │
│  │ Compose  │  │ Compose  │  │  Filament 3D Engine   │ │
│  │ Mobile   │  │ for TV   │  │  (Productos/Personaje)│ │
│  └────┬─────┘  └────┬─────┘  └───────────┬───────────┘ │
│       │              │                    │             │
│  ┌────┴──────────────┴────────────────────┴───────────┐ │
│  │              Shared Business Logic                  │ │
│  │         ViewModels + Repositories + UseCases        │ │
│  └────────────────────┬───────────────────────────────┘ │
│                       │                                 │
│  ┌────────────────────┴───────────────────────────────┐ │
│  │              AWS Amplify SDK (Kotlin)               │ │
│  └────────────────────┬───────────────────────────────┘ │
└───────────────────────┼─────────────────────────────────┘
                        │ HTTPS / WSS
┌───────────────────────┼─────────────────────────────────┐
│                   AWS CLOUD                              │
│                       │                                 │
│  ┌────────────────────┴──────────────────────────────┐  │
│  │              Amazon CloudFront (CDN)               │  │
│  └──┬──────────────┬──────────────────┬──────────────┘  │
│     │              │                  │                  │
│  ┌──┴───┐   ┌──────┴──────┐   ┌──────┴──────┐          │
│  │  S3  │   │  AppSync    │   │ API Gateway │          │
│  │ 3D/  │   │  GraphQL    │   │    REST     │          │
│  │Media │   └──────┬──────┘   └──────┬──────┘          │
│  └──────┘          │                 │                  │
│              ┌─────┴─────┐    ┌──────┴──────┐          │
│              │  Lambda   │    │   Lambda    │          │
│              │ Resolvers │    │  Functions  │          │
│              └─────┬─────┘    └──────┬──────┘          │
│                    │                 │                  │
│  ┌─────────┬───────┴────┬────────────┴───┬──────────┐  │
│  │DynamoDB │ OpenSearch │   Bedrock      │  Polly   │  │
│  │         │            │   (IA Chat)    │  (Voz)   │  │
│  └─────────┴────────────┴────────────────┴──────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Cognito (Auth) │ Pinpoint (Analytics/Push)      │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 4.2 Arquitectura de la App (Clean Architecture)

```
app/
├── data/                    # Capa de datos
│   ├── remote/              # APIs AWS (Amplify, AppSync)
│   ├── local/               # Room, DataStore
│   ├── repository/          # Implementaciones de repositorios
│   └── model/               # DTOs y mappers
│
├── domain/                  # Capa de dominio (pura Kotlin)
│   ├── model/               # Entidades de negocio
│   ├── repository/          # Interfaces de repositorios
│   └── usecase/             # Casos de uso
│
├── presentation/            # Capa de presentación
│   ├── mobile/              # Screens específicas móvil
│   ├── tv/                  # Screens específicas TV
│   ├── shared/              # Componentes compartidos
│   │   ├── components/      # UI components reutilizables
│   │   ├── theme/           # Material Theme + TV Theme
│   │   └── navigation/      # Nav graphs
│   └── viewmodel/           # ViewModels compartidos
│
├── engine3d/                # Módulo Filament
│   ├── ProductViewer.kt     # Visor 3D de productos (rotación/zoom)
│   ├── CharacterEngine.kt   # Personaje IA animado
│   └── SceneManager.kt      # Gestión de escenas 3D
│
├── ai/                      # Módulo IA
│   ├── ChatService.kt       # Integración Amazon Bedrock
│   ├── VoiceService.kt      # Amazon Polly TTS
│   └── CharacterAI.kt       # Lógica del personaje IA
│
├── qr/                      # Módulo QR
│   └── DynamicQRGenerator.kt
│
└── di/                      # Inyección de dependencias (Hilt)
    └── AppModule.kt
```

---

## 5. Flujos Clave de la Aplicación

### 5.1 Visualización 3D de Producto

```
Usuario toca producto → ViewModel carga metadata desde AppSync
    → S3 descarga modelo GLB via CloudFront
    → Filament renderiza modelo 3D
    → Usuario rota/zoom con touch o D-pad
    → Interacción registrada en Pinpoint Analytics
```

### 5.2 Personaje IA Conversacional

```
Usuario activa personaje → CharacterEngine inicia animación (Filament/Lottie)
    → Usuario habla o escribe pregunta
    → Lambda envía prompt a Amazon Bedrock (Claude/Titan)
    → Respuesta texto → Amazon Polly genera audio
    → CharacterEngine sincroniza labios con audio
    → Respuesta mostrada + hablada por personaje
```

### 5.3 QR Dinámico

```
Producto seleccionado → Lambda genera URL única con tracking
    → QR generado localmente (ZXing)
    → Usuario escanea con móvil
    → Redirección a landing con info del producto
    → Evento trackeado en Pinpoint
```

---

## 6. Servicios AWS - Detalle de Uso

### 6.1 Amazon Bedrock (Personaje IA)

- **Modelo:** Claude 3 Sonnet/Haiku (conversacional, bajo costo)
- **Uso:** Responder preguntas sobre productos de salud
- **Knowledge Base:** RAG con documentos de productos en S3
- **Guardrails:** Filtros para mantener respuestas en dominio salud

### 6.2 Amazon Polly (Voz del Personaje)

- **Voz:** Neural voice en español (Lupe o Mia)
- **Formato:** MP3/OGG streaming
- **SSML:** Para controlar entonación y pausas naturales

### 6.3 Amazon DynamoDB (Base de Datos Principal)

```
Tablas principales:
├── Products          (PK: productId, SK: category)
├── Categories        (PK: categoryId)
├── UserSessions      (PK: sessionId, SK: timestamp)
├── AIConversations   (PK: sessionId, SK: messageId)
├── QRTracking        (PK: qrId, SK: scanTimestamp)
└── ContentBlog       (PK: postId, SK: publishDate)
```

### 6.4 Amazon S3 + CloudFront (Assets)

```
Buckets:
├── lasante-3d-models/       # Modelos GLB/glTF de productos
├── lasante-media/           # Imágenes, videos, Lottie animations
├── lasante-ai-knowledge/    # Documentos para RAG de Bedrock
└── lasante-qr-landings/     # Landing pages estáticas para QR
```

### 6.5 AWS AppSync (API GraphQL)

```graphql
type Product {
  id: ID!
  name: String!
  description: String!
  category: Category!
  model3dUrl: String!        # URL S3 del modelo GLB
  images: [String!]!
  price: Float
  features: [String!]
  blogPost: BlogPost
}

type AIResponse {
  message: String!
  audioUrl: String           # URL Polly audio
  suggestedProducts: [Product!]
}

type Query {
  getProduct(id: ID!): Product
  listProducts(category: String, limit: Int): [Product!]!
  searchProducts(query: String!): [Product!]!
}

type Mutation {
  askAI(sessionId: ID!, question: String!): AIResponse!
  generateQR(productId: ID!): String!
  trackInteraction(event: InteractionInput!): Boolean!
}

type Subscription {
  onAIResponse(sessionId: ID!): AIResponse
}
```

---

## 7. Adaptación Móvil vs TV

### 7.1 Estrategia de UI Adaptativa

```kotlin
// Detección de plataforma
val isTV = remember {
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
}

// Layouts adaptativos
@Composable
fun ProductScreen(product: Product) {
    if (isTV) {
        TVProductLayout(product)   // Optimizado para pantalla grande + D-pad
    } else {
        MobileProductLayout(product) // Optimizado para touch + scroll
    }
}
```

### 7.2 Diferencias Clave por Plataforma

| Aspecto | Móvil | TV / Kiosco |
|---------|-------|-------------|
| Input | Touch, gestos | D-pad, control remoto, touch (kiosco) |
| Layout | Vertical scroll | Grid horizontal, cards grandes |
| 3D Interaction | Pinch zoom, drag rotate | D-pad rotate, botones zoom |
| Texto | Tamaño normal | Texto grande, legible a distancia |
| Navegación | Bottom nav, drawer | Top nav, sidebar, focus visible |
| QR | Escanear QR | Mostrar QR para que móvil escanee |
| Personaje IA | Pequeño, esquina | Grande, prominente, pantalla completa |

---

## 8. Plan de Estudio y Alineación del Equipo

### Fase 1: Fundamentos (Semana 1-2)

| Tema | Recurso | Prioridad |
|------|---------|-----------|
| Kotlin Coroutines + Flow | kotlinlang.org/docs/coroutines | Alta |
| Jetpack Compose basics | developer.android.com/compose | Alta |
| Clean Architecture Android | Android Developers Guide | Alta |
| Hilt DI | developer.android.com/training/dependency-injection/hilt-android | Alta |

### Fase 2: Especialización (Semana 3-4)

| Tema | Recurso | Prioridad |
|------|---------|-----------|
| Compose for TV | developer.android.com/tv/compose | Alta |
| Google Filament 3D | github.com/google/filament | Alta |
| AWS Amplify Android | docs.amplify.aws/android | Alta |
| AppSync + GraphQL | docs.aws.amazon.com/appsync | Media |

### Fase 3: Integración (Semana 5-6)

| Tema | Recurso | Prioridad |
|------|---------|-----------|
| Amazon Bedrock integration | docs.aws.amazon.com/bedrock | Alta |
| Amazon Polly streaming | docs.aws.amazon.com/polly | Media |
| Filament + Compose integration | Filament Android samples | Alta |
| Performance profiling TV | Android TV best practices | Media |

---

## 9. Patrones de Implementación

### 9.1 Patrón Repository con Amplify

```kotlin
// domain/repository/ProductRepository.kt
interface ProductRepository {
    fun getProducts(category: String): Flow<List<Product>>
    suspend fun getProduct(id: String): Product
    suspend fun search(query: String): List<Product>
}

// data/repository/ProductRepositoryImpl.kt
class ProductRepositoryImpl @Inject constructor(
    private val appSyncClient: AWSAppSyncClient,
    private val localCache: ProductDao
) : ProductRepository {

    override fun getProducts(category: String): Flow<List<Product>> = flow {
        // Offline-first: emit cache, then fetch remote
        emit(localCache.getByCategory(category))
        val remote = appSyncClient.query(ListProductsQuery(category))
        localCache.insertAll(remote.toEntities())
        emit(remote.toDomain())
    }
}
```

### 9.2 Patrón ViewModel Compartido (Móvil + TV)

```kotlin
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val trackInteractionUseCase: TrackInteractionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    fun loadProducts(category: String) {
        viewModelScope.launch {
            getProductsUseCase(category)
                .catch { _uiState.value = ProductUiState.Error(it.message) }
                .collect { _uiState.value = ProductUiState.Success(it) }
        }
    }
}
```

---

## 10. Estimación de Costos AWS (Mensual Aproximado)

| Servicio | Uso Estimado | Costo Aprox. |
|----------|-------------|-------------|
| Lambda | 500K invocaciones | $5 - $15 |
| DynamoDB | 10GB + on-demand | $10 - $25 |
| AppSync | 500K queries | $5 - $10 |
| S3 | 50GB (modelos 3D, media) | $2 - $5 |
| CloudFront | 100GB transfer | $10 - $15 |
| Bedrock (Claude Haiku) | 10K conversaciones | $15 - $40 |
| Polly | 5M caracteres | $20 - $30 |
| Cognito | 1K MAU | $0 (free tier) |
| Pinpoint | Analytics básico | $0 - $5 |
| **TOTAL ESTIMADO** | | **$67 - $145/mes** |

> Nota: Usar https://calculator.aws para estimación precisa según uso real.

---

## 11. Riesgos y Mitigaciones

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Performance 3D en TV de gama baja | Alto | LOD (Level of Detail) en modelos, fallback a imágenes 360° |
| Latencia Bedrock en conversación IA | Medio | Streaming responses, cache de respuestas frecuentes |
| Tamaño de modelos 3D (descarga) | Medio | Compresión Draco, carga progresiva, pre-cache en kiosco |
| Fragmentación Android TV | Medio | Testing en múltiples dispositivos, min API 28 |
| Curva aprendizaje Filament | Medio | POC temprano, alternativa Lottie para personaje si 3D es excesivo |

---

## 12. Próximos Pasos

1. **Semana 1:** Setup proyecto base con Compose + Compose for TV + Hilt
2. **Semana 1:** Configurar AWS CDK para infraestructura (Cognito, AppSync, DynamoDB, S3)
3. **Semana 2:** POC Filament - renderizar un modelo GLB de producto
4. **Semana 2:** POC Bedrock + Polly - conversación básica con voz
5. **Semana 3:** Integrar Amplify Android con AppSync
6. **Semana 3:** Primer carrusel de productos funcional (móvil + TV)
7. **Semana 4:** QR dinámico + tracking de interacciones
8. **Semana 5-6:** Integración completa + testing en dispositivos TV reales

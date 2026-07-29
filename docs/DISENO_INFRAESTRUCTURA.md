# Diseño de Infraestructura para Proyecto 3D - LaSanté

## 1. Aprovisionamiento de Infraestructura y Gestión de Código

La infraestructura serverless del sistema será definida mediante **AWS Serverless Application Model (AWS SAM)**, permitiendo el aprovisionamiento automatizado de servicios como funciones Lambda, API Gateway, Amazon DynamoDB y Amazon S3.

Los servicios de distribución de contenido (Amazon CloudFront) y resolución de dominio (Amazon Route 53) serán configurados inicialmente de forma manual, permitiendo una implementación ágil en la fase inicial del proyecto. Posteriormente, estos componentes podrán ser incorporados dentro de la estrategia de infraestructura como código (IaC) para estandarizar su despliegue entre ambientes.

El código fuente del proyecto será gestionado mediante repositorios en GitHub, organizados por componente funcional (aplicación Android, módulo de gestión, backend serverless y automatización DevOps). Esta estructura permitirá un control de versiones eficiente y colaboración entre equipos.

Adicionalmente, se implementarán pipelines de integración y despliegue continuo (CI/CD) utilizando GitHub Actions, permitiendo automatizar procesos de construcción, validación y despliegue de los diferentes componentes del sistema.

---

## 2. Tabla de Repositorios – Proyecto Lasante 3D

| # | Repositorio | Tipo | Tecnologías | Descripción |
|---|---|---|---|---|
| 1 | `lasante-3d-apk` | Frontend (Android) | Kotlin / Jetpack Compose | Aplicación Android que consume el catálogo. Incluye pantallas (Intro, Unidades de negocio, Productos, Visualización 3D), navegación, integración con API Gateway y consumo de assets desde CloudFront. |
| 2 | `lasante-3d-gestion` | Frontend (Web) | React / Vue / HTML-CSS-JS | Módulo web de gestión administrativa. Permite realizar CRUD del catálogo, configurar la pantalla de inicio (home) y administrar assets (imágenes, modelos 3D y video institucional). |
| 3 | `lasante-3d-backend` | Backend (Serverless) | AWS SAM / Python | Backend serverless del sistema. Contiene las funciones Lambda (home, catalogo, search, gestion), lógica de negocio, integración con DynamoDB y S3, y definición de la API mediante AWS SAM. |
| 4 | `lasante-3d-devops` | CI/CD & Automatización | GitHub Actions / Bash | Repositorio de automatización. Contiene pipelines CI/CD, scripts de despliegue, configuraciones por ambiente (dev, prod) y lineamientos operativos del proyecto. |

---

## 3. Recursos AWS – Arquitectura Lasante 3D

| # | Recurso AWS | Nombre | Descripción |
|---|---|---|---|
| 1 | Amazon API Gateway | `lasante-3d-gw` | Punto de entrada único para la aplicación cliente (APK/TV) y el módulo de gestión. Expone los endpoints REST que invocan las funciones Lambda del sistema. |
| 2 | AWS Lambda | `lasante-3d-home` | Función encargada de construir la información de la pantalla de inicio (productos destacados, vitrina de unidades de negocio, video institucional y configuraciones visibles). |
| 3 | AWS Lambda | `lasante-3d-catalogo` | Función de consulta del catálogo. Permite obtener unidades de negocio, tratamientos, productos, detalle del producto y navegación entre productos. Solo lectura. |
| 4 | AWS Lambda | `lasante-3d-search` | Función encargada de la búsqueda, filtrado y ordenamiento de productos dentro del catálogo. Optimizada para consultas dinámicas. |
| 5 | AWS Lambda | `lasante-3d-gestion` | Microservicio administrativo que permite realizar operaciones CRUD sobre el catálogo, configuración de la aplicación y gestión de assets (carga, actualización y eliminación en S3). |
| 6 | Amazon DynamoDB | `lasante-3d-catalog-db` | Base de datos NoSQL que almacena toda la información del catálogo: unidades de negocio, tratamientos, productos, configuraciones de home y referencias a assets. |
| 7 | Amazon S3 | `lasante-3d-catalogo-assets` | Almacenamiento de activos digitales consumidos por la aplicación: imágenes, modelos 3D y video institucional. |
| 8 | Amazon S3 | `lasante-3d-gestion-assets` | Bucket utilizado por el módulo de gestión para la carga y administración de assets antes de su disponibilidad en el catálogo. |
| 9 | Amazon CloudFront | `lasante-3d-clf-assets` | Red de distribución de contenido (CDN) para la entrega eficiente de imágenes, modelos 3D y videos a la aplicación cliente. |
| 10| Amazon CloudFront | `lasante-3d-clf-gestion` | CDN que distribuye el acceso al módulo de gestión, permitiendo una experiencia segura y de baja latencia para los usuarios administrativos. |

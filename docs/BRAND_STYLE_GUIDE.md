# Guia de estilos La Sante - Gestion

Esta guia resume el lenguaje visual aplicado en el panel de Gestion para reutilizarlo en la APK.

## Identidad visual

- Estilo general: limpio, medico, suave y premium.
- Base visual: fondos claros, tarjetas gris claro, acentos verdes de marca y acciones con degradado cyan-azul.
- Tipografia: sistema nativo `-apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif`.
- Color de texto principal: `#071d3a`.
- Fondo base: `#f4f5f3`.
- Fondo comun: `/admin/fondo.jpg`, con overlay blanco translucido para no competir con el contenido.
- Marca de agua: texto grande `La Sante` en la parte inferior, muy tenue.
- Icono de Integraciones: usar solo `/admin/clip.png`.

## Paleta

### Primarios

- Azul oscuro texto/iconos: `#071d3a`.
- Verde marca: `#35a336`.
- Verde profundo: `#006d3d`.
- Verde medio: `#5db13d`.
- Cyan: `#10c5cc`, `#20d3d5`, `#32c2cc`.
- Azul accion: `#0054c9`, `#0051c8`, `#004ed0`.

### Neutros

- Fondo general: `#f4f5f3`.
- Panel/nav claro: `rgba(239, 240, 238, 0.86)`.
- Tarjetas: `rgba(234, 234, 233, 0.82)` o `#eaeae9`.
- Boton secundario: `#dfebf3`.
- Boton activo suave: `#f8e7e5`.
- Texto secundario: `#5d5d5d`, `#667287`, `#8a93a1`.

## Gradientes

### Accion principal

Usar para botones destacados como `+ Nuevo`, `Exportar configuracion`, `Subir recurso`.

```css
linear-gradient(90deg, #32c2cc 0%, #0054c9 100%)
```

### Menu activo y lineas azules

Usar para texto activo del menu y subrayado inferior.

```css
linear-gradient(90deg, #10c5cc, #004ed0)
```

### Header verde

```css
linear-gradient(135deg, #5db13d 0%, #218f32 46%, #006b42 100%)
```

### Boton cerrar sesion

```css
linear-gradient(90deg, #72bc42 0%, #006d3d 100%)
```

### Titulos verdes de tarjetas

```css
linear-gradient(90deg, #1f8f37 0%, #43b13f 58%, #68c447 100%)
```

### Numeros de estadisticas

```css
linear-gradient(90deg, #20d3d5 0%, #0097d6 55%, #0051c8 100%)
```

### Boton guardar

```css
linear-gradient(90deg, #b8f7cd 0%, #82b3ff 100%)
```

## Bordes y radios

- Pildoras/botones: `border-radius: 999px`.
- Tarjetas principales: `border-radius: 24px`.
- Modales y paneles grandes: `border-radius: 22px` a `32px`.
- Visor 3D: `border-radius: 31px`.
- Inputs tipo formulario: `border-radius: 999px`; textarea: `31px`.
- Listas: encabezado y filas con esquinas suaves, `14px` a `16px`.

## Sombras

Usar sombras suaves, nunca muy oscuras.

```css
box-shadow: 0 18px 30px rgba(0, 84, 201, 0.18);
box-shadow: 0 18px 34px rgba(7, 29, 58, 0.08);
box-shadow: 0 16px 28px rgba(7, 29, 58, 0.16);
```

## Botones

### Boton principal destacado

- Fondo: gradiente cyan-azul.
- Texto: blanco.
- Forma: pildora.
- Peso: `700`.
- Altura aproximada: `48px`.
- Sombra azul suave.

### Boton secundario

- Fondo: `#dfebf3`.
- Texto: `#071d3a`.
- Forma: pildora.
- Sin borde.

### Boton activo de vista

- Fondo: `#f8e7e5`.
- Texto: `#071d3a`.

## Tarjetas

- Fondo: gris claro translucido `rgba(234, 234, 233, 0.82)`.
- Radio: `24px`.
- Titulos: verde degradado.
- Texto: azul oscuro.
- Acciones: iconos azul oscuro `#071d3a`.
- Badge Manual en tarjetas: borde degradado cyan-azul, texto verde.
- Badge Manual en listas: borde igual, texto azul oscuro.

## Listas

- Encabezado: gradiente suave verde-cyan-azul.
- Filas: gris claro translucido.
- Separador inferior: linea fina degradada cyan-azul.
- Estadisticas: pildoras sin fondo visible fuerte, borde degradado.
- Acciones: iconos grandes, azul oscuro.

## Modales

- Overlay: `rgba(0, 0, 0, 0.5)`.
- Contenedor: blanco, radio `22px`, ancho maximo segun uso.
- Titulo: azul oscuro, peso alto.
- Separadores: linea cyan clara `#78d8df`.
- Secciones: marcador vertical con gradiente cyan-azul.
- Inputs: fondo degradado gris-blanco, sin borde visible duro.
- Panel informativo: blanco/gris muy suave con borde `#d8dfdf`.

## Integraciones

- Hero: clip grande `/admin/clip.png`, titulo verde, descripcion azul oscuro/gris.
- Tarjeta Android:
  - Pildora verde detras, visible solo al lado izquierdo.
  - Tarjeta encima, fondo `#eaeae9`.
  - Radio `24px`.
  - Toggle gris apagado, verde activo.
  - Texto "Android APP": negro con `APP` gris.

## Reglas responsive

- Contenido principal centrado con ancho maximo aproximado `1120px`.
- En desktop, tarjetas en 3 columnas.
- En tablet, tarjetas en 2 columnas.
- En movil, tarjetas en 1 columna y listas con scroll horizontal.
- Evitar texto demasiado grande dentro de paneles compactos.
- Mantener la misma sangria entre header, nav y contenido.

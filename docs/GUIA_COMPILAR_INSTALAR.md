# Guía: Compilar e Instalar sin Android Studio

**Fecha:** 17 Abril 2025  
**APK generado:** ✅ 123 MB

---

## Opción 1: Instalar APK Directo (Más Rápido)

cd /home/andrea/proyectos/lasante/mobile/app

# Opción 1: Instalar directo
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Opción 2: Compilar e instalar
./gradlew installDebug

adb exec-out screencap -p > ~/Escritorio/vitrina.png
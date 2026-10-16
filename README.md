## Voyager (Administrador de archivos mp3) 

Este es un proyecto multiplataforma para Android y Escritorio (JVM). Consiste de un administrador de archivos mp3.
Forma parte del proyecto 2 de la materia "Modelado y programación" impartido en la facultad de ciencias de la UNAM.

### Requisitos
- Kotlin
- Gradle
- OpenJDK 21

Instalación en Void Linux:

```
# xbps-install openjdk21 gradle kotlin-bin
```

### Instrucciones de uso

```
# xbps-install openjdk21 gradle kotlin-bin
$ git clone https://github.com/bsoto-fc/voyager.git
$ cd voyager/
```

- Android: `./gradlew :androidApp:assembleDebug`
- Escritorio:
  - Hot reload: `./gradlew :desktopApp:hotRun --auto`
  - Standard run: `./gradlew :desktopApp:run`

## Voyager (Administrador de archivos mp3) 

Este es un proyecto que consiste de un administrador de archivos mp3.
Forma parte del proyecto 2 de la materia "Modelado y programación" impartido en la facultad de ciencias de la UNAM.

<img width="1361" height="490" alt="image" src="https://github.com/user-attachments/assets/7b0b715a-c2d0-4e76-bd99-02d1b55ae4ec" />

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
$ ./gradlew run
```

Al correr el programa se crea un nuevo directorio en ~/.local/share/voyager

Para generar un archivo .jar,
```
$ ./gradlew build
$ java -jar app/build/libs/app-standalone.jar
```


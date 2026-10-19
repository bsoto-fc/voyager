package mx.unam.ciencias.myp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import mx.unam.ciencias.myp.ui.mainScreen

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Voyager") {
        mainScreen()
    }
}


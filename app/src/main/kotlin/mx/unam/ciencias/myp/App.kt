package mx.unam.ciencias.myp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import mx.unam.ciencias.myp.ui.mainScreen
import java.awt.Dimension

const val MIN_HEIGHT = 600
const val MIN_WIDTH = 800

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Voyager"
    ) {
        window.minimumSize = Dimension(MIN_WIDTH,MIN_HEIGHT)
        mainScreen()
    }
}


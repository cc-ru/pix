package totoro.pix

import javafx.application.Application
import tornadofx.App

fun main(args: Array<String>) {
    Application.launch(Pix::class.java, *args)
}

class Pix: App (PixView::class)

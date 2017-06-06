package totoro.pix

import javafx.application.Application
import tornadofx.App
import tornadofx.View
import tornadofx.hbox
import tornadofx.label

fun main(args: Array<String>) {
    Application.launch(Pix::class.java, *args)
}

class Pix: App (PixView::class) {
}

class PixView : View() {
    override val root = hbox {
        label("Pixin without limits!")
    }
}
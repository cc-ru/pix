package totoro.pix

import javafx.application.Application
import javafx.scene.control.Alert
import javafx.scene.image.Image
import tornadofx.*

fun main(args: Array<String>) {
    Application.launch(Pix::class.java, *args)
}

class Pix: App (PixView::class)

class PixView: View() {
    override val root = vbox {
        button("Open Image...") {
            action {
                alert(Alert.AlertType.INFORMATION, "Got ya!", "There is nothing to open here...")
            }
        }
        imageview(Image("/placeholder.png")) {
            style {
                padding = box(5.px)
            }
        }
    }
}
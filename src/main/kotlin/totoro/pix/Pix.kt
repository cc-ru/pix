package totoro.pix

import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import tornadofx.*

fun main(args: Array<String>) {
    Application.launch(Pix::class.java, *args)
}

class Pix: App (PixView::class)

class PixView: View() {
    val image = imageview(Image("/placeholder.png")) {
        fitWidth = 160.0
        fitHeight = 100.0
    }

    override val root = vbox {
        style {
            padding = box(10.px)
            spacing = 5.px
        }
        hbox {
            style {
                spacing = 5.px
            }
            button("Open Image...") {
                action {
                    val result = chooseFile("Select image file",
                            arrayOf(FileChooser.ExtensionFilter("Supported image format",
                                    listOf("*.png", "*.jpg", "*.jpeg", "*.bmp"))))
                    if (result.isNotEmpty()) {
                        val file = result.first()
                        image.image = Image("file:${file.absolutePath}")
                    }
                }
            }
            button("Export To...") {
                action {
                    alert(Alert.AlertType.ERROR, "Unimplemented", "This interface is under construction!")
                }
            }
        }
        hbox {
            style {
                padding = box(1.px)
                alignment = Pos.CENTER
                borderWidth += box(1.px)
                borderStyle += BorderStrokeStyle.SOLID
                borderColor += box(Color.DARKGRAY)
            }
            children += image
        }
    }

    init {
        title = "Pix Image Converter"
        primaryStage.isResizable = false
    }
}
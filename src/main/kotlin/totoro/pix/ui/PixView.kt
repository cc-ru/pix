package totoro.pix.ui

import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import tornadofx.*
import totoro.pix.converter.Converter
import java.nio.file.Files


class PixView: View() {
    private val openFilePicker = FilePicker()
    private val exportFilePicker = FilePicker()

    private val image = SimpleObjectProperty(Image("/placeholder.png"))
    private val preview = imageview() {
        viewport = Rectangle2D(0.0, 0.0, 160.0, 100.0)
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
                    val result = openFilePicker.chooseFile("Select image file",
                            arrayOf(FileChooser.ExtensionFilter("Supported image format",
                                    listOf("*.png", "*.jpg", "*.jpeg", "*.bmp"))))
                    if (result.isNotEmpty()) {
                        val file = result.first()
                        image.value = Image("file:${file.absolutePath}")
                    }
                }
            }
            button("Export To...") {
                action {
                    val files = exportFilePicker.chooseFile("Select file to save",
                            arrayOf(FileChooser.ExtensionFilter("Pix Image",
                                    listOf("*.pix"))), FileChooserMode.Save)
                    if (files.isNotEmpty()) {
                        runAsync {
                            val result = Converter.convert(image.value)
                            Files.write(files.first().toPath(), result)
                        } ui {
                            alert(Alert.AlertType.CONFIRMATION, "Converted successfully")
                        }
                    }
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
            children += preview
        }
    }

    init {
        title = "Pix Image Converter"
        primaryStage.isResizable = false
        preview.imageProperty().bind(image)
    }
}

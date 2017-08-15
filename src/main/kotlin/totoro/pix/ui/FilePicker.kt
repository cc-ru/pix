package totoro.pix.ui

import javafx.stage.FileChooser
import javafx.stage.Window
import tornadofx.FileChooserMode
import java.io.File

class FilePicker {
    private val chooser = FileChooser()
    private var directory: File? = null

    fun chooseFile(title: String? = null, filters: Array<FileChooser.ExtensionFilter>,
                   mode: FileChooserMode = FileChooserMode.Single, owner: Window? = null,
                   op: (FileChooser.() -> Unit)? = null): List<File> {
        if (directory != null) chooser.initialDirectory = directory
        if (title != null) chooser.title = title
        chooser.extensionFilters.clear()
        chooser.extensionFilters.addAll(filters)
        op?.invoke(chooser)

        val result = when (mode) {
            FileChooserMode.Single -> {
                val result = chooser.showOpenDialog(owner)
                if (result == null) emptyList() else listOf(result)
            }
            FileChooserMode.Multi -> chooser.showOpenMultipleDialog(owner) ?: emptyList()
            FileChooserMode.Save -> {
                val result = chooser.showSaveDialog(owner)
                if (result == null) emptyList() else listOf(result)
            }
            else -> emptyList()
        }

        if (result.isNotEmpty()) {
            val file = result.component1()
            directory = file.parentFile
        }

        return result
    }
}

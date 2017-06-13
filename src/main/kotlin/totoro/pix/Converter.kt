package totoro.pix

import javafx.scene.image.Image
import javafx.scene.paint.Color
import java.util.*
import kotlin.collections.HashMap

object Converter {
    fun convert(image: Image): ByteArray {
        val matrix = ArrayList<Byte>()
        val reader = image.pixelReader

        // encode width / height
        val width = Math.min(image.width.toInt(), 160)
        val height = Math.min(image.height.toInt(), 100)
        matrix.add(width.toByte())
        matrix.add((height / 2).toByte())
        println("Width: $width")
        println("Height: $height\n")

        // encode basic fore / back colors
        val pairs = HashMap<Pair<Color, Color>, Int>()
        for (x in 0 until width) {
            (0 until height / 2 step 2)
                    .map { Pair(reader.getColor(x, it), reader.getColor(x, it+1)) }
                    .forEach { pairs[it] = pairs[it]?.plus(1) ?: 1 }
        }
        val basic = pairs.toSortedMap(compareBy<Pair<Color, Color>> { pairs[it] }.reversed()).firstKey()
        encodeColor(matrix, basic.first)
        encodeColor(matrix, basic.second)
        println("Basic fore: ${basic.first.red}, ${basic.first.green}, ${basic.first.blue}")
        println("Basic back: ${basic.second.red}, ${basic.second.green}, ${basic.second.blue}\n")

        // encode the rest of matrix
        val list = LinkedList<Sequence>()
        var current: Sequence? = null
        for (x in 0 until width) {
            for (y in 0 until height / 2 step 2) {
                val upper = reader.getColor(x, y)
                val lower = reader.getColor(x, y+1)
                if (current == null || !current.add(upper, lower)) {
                    if (current != null) list.add(current)
                    current = Sequence(upper, lower, x+1, y/2+1)
                }
            }
        }
        val sorted = HashMap<Color, HashMap<Color, LinkedList<Sequence>>>()
        for (seq in list) {
            if (sorted[seq.fore] == null) sorted[seq.fore] = HashMap<Color, LinkedList<Sequence>>()
            if (sorted[seq.fore]?.get(seq.back) == null) sorted[seq.fore]?.set(seq.back, LinkedList<Sequence>())
            sorted[seq.fore]?.get(seq.back)?.add(seq)
        }
        encodeLen(matrix, sorted.size)
        println("Fore colors: ${sorted.size}")
        sorted.forEach { fore, sub ->
            encodeColor(matrix, fore)
            println("- ${fore.red}, ${fore.green}, ${fore.blue}")
            encodeLen(matrix, sub.size)
            println("- Back colors: ${sub.size}")
            sub.forEach { back, list ->
                encodeColor(matrix, back)
                println("- - ${back.red}, ${back.green}, ${back.blue}")
                encodeLen(matrix, list.size)
                println("- - Sequences: ${list.size}")
                list.forEach { seq ->
                    matrix.add(seq.x.toByte())
                    println("- - - x: ${seq.x}")
                    matrix.add(seq.y.toByte())
                    println("- - - y: ${seq.y}")
                    matrix.add(seq.str.length.toByte())
                    println("- - - len: ${seq.str.length}")
                    seq.str.forEach { char -> matrix.add(char.toByte()) }
                    println("- - - str: ${seq.str}")
                    println("- - - * * *")
                }
            }
        }

        return ByteArray(matrix.size, { matrix[it] })
    }

    private fun encodeColor(matrix: ArrayList<Byte>, color: Color) {
        matrix.add((color.red * 256).toByte())
        matrix.add((color.green * 256).toByte())
        matrix.add((color.blue * 256).toByte())
    }
    private fun encodeLen(matrix: ArrayList<Byte>, len : Int) {
        matrix.add((len / 256).toByte())
        matrix.add((len % 256).toByte())
    }
}

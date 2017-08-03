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
            (0 until height / 2)
                    .map { Pair(reader.getColor(x, it*2), reader.getColor(x, it*2+1)) }
                    .forEach { pairs[it] = pairs[it]?.plus(1) ?: 1 }
        }
        val basic = pairs.toSortedMap(compareBy<Pair<Color, Color>> { pairs[it] }.reversed()).firstKey()
        encodeColor(matrix, basic.first)
        encodeColor(matrix, basic.second)
        println("Basic fore: ${basic.first.red * 255}, ${basic.first.green * 255}, ${basic.first.blue * 255}")
        println("Basic back: ${basic.second.red * 255}, ${basic.second.green * 255}, ${basic.second.blue * 255}\n")

        // encode the rest of matrix
        val list = LinkedList<Sequence>()
        var current: Sequence? = null
        for (y in 0 until height / 2) {
            for (x in 0 until width) {
                val upper = inflate(deflate(reader.getColor(x, y*2)))
                val lower = inflate(deflate(reader.getColor(x, y*2+1)))
                if (current == null || current.str.size >= 255 ||
                        x == 0 || !current.add(upper, lower)) {
                    if (current != null) list.add(current)
                    current = Sequence(upper, lower, x+1, y+1)
                }
            }
        }
        if (current != null) list.add(current)

        val sorted = HashMap<Color, HashMap<Color, LinkedList<Sequence>>>()
        for (seq in list) {
            if (sorted[seq.fore] == null) sorted[seq.fore] = HashMap<Color, LinkedList<Sequence>>()
            if (sorted[seq.fore]?.get(seq.back) == null) sorted[seq.fore]?.set(seq.back, LinkedList<Sequence>())
            sorted[seq.fore]?.get(seq.back)?.add(seq)
        }

        print("Compressed char sequence: ")
        val raw = ArrayList<Byte>()
        var index = 0
        var byte = 0
        sorted.forEach { _, sub ->
            sub.forEach { _, l ->
                l.forEach { seq ->
                    seq.str.forEach { char ->
                        if (index % 4 == 0 && index > 0) {
                            raw.add(byte.toByte())
                            print("$byte.")
                            byte = 0
                        }
                        byte = byte * 4 + char
                        index++
                    }
                }
            }
        }
        if (index % 4 != 0) {
            while (index % 4 != 0) { byte *= 4; index++ }
            raw.add(byte.toByte())
        }
        raw.add(byte.toByte())
        print("$byte.\n")

        encodeLen(matrix, index)
        raw.forEach { matrix.add(it) }
        println("Total: $index symbols / ${raw.size} bytes\n")

        encodeLen(matrix, sorted.size)
        println("Fore colors: ${sorted.size}")
        sorted.forEach { fore, sub ->
            encodeColor(matrix, fore)
            println("- ${fore.red * 255}, ${fore.green * 255}, ${fore.blue * 255}")
            encodeLen(matrix, sub.size)
            println("- Back colors: ${sub.size}")
            sub.forEach { back, l ->
                encodeColor(matrix, back)
                println("- - ${back.red * 255}, ${back.green * 255}, ${back.blue * 255}")
                encodeLen(matrix, l.size)
                println("- - Sequences: ${l.size}")
                l.forEach { seq ->
                    matrix.add(seq.x.toByte())
                    println("- - - x: ${seq.x}")
                    matrix.add(seq.y.toByte())
                    println("- - - y: ${seq.y}")
                    matrix.add(seq.str.size.toByte())
                    println("- - - len: ${seq.str.size}")
                    println("- - - * * *")
                }
            }
        }

        return ByteArray(matrix.size, { matrix[it] })
    }

    private fun encodeColor(matrix: ArrayList<Byte>, color: Color) {
        matrix.add((color.red * 255).toByte())
        matrix.add((color.green * 255).toByte())
        matrix.add((color.blue * 255).toByte())
    }
    private fun encodeLen(matrix: ArrayList<Byte>, len : Int) {
        matrix.add((len / 256).toByte())
        matrix.add((len % 256).toByte())
    }

    private val reds = 6
    private val greens = 8
    private val blues = 5
    private val grays = arrayOf (
            0.05859375, 0.1171875, 0.17578125, 0.234375, 0.29296875, 0.3515625, 0.41015625, 0.46875,
            0.52734375, 0.5859375, 0.64453125, 0.703125, 0.76171875, 0.8203125, 0.87890625, 0.9375
    )
    private fun delta(a: Color, b: Color): Double {
        val dr = a.red - b.red
        val dg = a.green - b.green
        val db = a.blue - b.blue
        return 0.2126 * dr * dr + 0.7152 * dg * dg + 0.0722 * db * db
    }
    fun deflate(color: Color): Int {
        val idxR = (color.red * 255 * (reds - 1.0) / 0xFF + 0.5).toInt()
        val idxG = (color.green * 255 * (greens - 1.0) / 0xFF + 0.5).toInt()
        val idxB = (color.blue * 255 * (blues - 1.0) / 0xFF + 0.5).toInt()
        val compressed = 16 + idxR * greens * blues + idxG * blues + idxB
        return (0..15).fold(compressed, { acc, i -> if (delta(inflate(i), color) < delta(inflate(acc), color)) i else acc })
    }
    fun inflate(value: Int): Color {
        if (value < 16) {
            return Color.gray(grays[value])
        } else {
            val index = value - 16
            val idxB = index % blues
            val idxG = (index / blues) % greens
            val idxR = (index / blues / greens) % reds
            val r = (idxR * 0xFF / (reds - 1.0) + 0.5).toInt()
            val g = (idxG * 0xFF / (greens - 1.0) + 0.5).toInt()
            val b = (idxB * 0xFF / (blues - 1.0) + 0.5).toInt()
            return Color.rgb(r, g, b)
        }
    }
}

package totoro.pix

import javafx.scene.paint.Color

class Sequence(val fore: Color, val back: Color, val x: Int, val y: Int) {
    var str = ""

    fun add(upper: Color, lower: Color): Boolean {
        if (fits(upper, lower)) {
            str += when (upper) {
                fore -> when (lower) {
                    fore -> "█"
                    else -> "▀"
                }
                else -> when (lower) {
                    fore -> "▄"
                    else -> " "
                }
            }
            return true
        } else return false
    }

    fun fits(a: Color, b: Color): Boolean = (fore == a && back == b) || (fore == b && back == a)

    fun empty() = Sequence(Color.BLACK, Color.BLACK, 1, 1)
}

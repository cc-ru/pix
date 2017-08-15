package totoro.pix.converter

import javafx.scene.paint.Color
import java.util.*

class Sequence(val fore: Color, val back: Color, val x: Int, val y: Int) {
    var str = LinkedList<Int>().plus(Block.UPPER)

    fun add(upper: Color, lower: Color): Boolean {
        if (fits(upper, lower)) {
            str = str.plus(when (upper) {
                fore -> when (lower) {
                    fore -> Block.FULL
                    else -> Block.UPPER
                }
                else -> when (lower) {
                    fore -> Block.LOWER
                    else -> Block.EMPTY
                }
            })
            return true
        } else return false
    }

    private fun fits(a: Color, b: Color): Boolean = (fore == a && back == b) || (fore == b && back == a)
}

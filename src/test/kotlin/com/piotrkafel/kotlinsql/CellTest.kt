package com.piotrkafel.kotlinsql

import org.junit.jupiter.api.Test
import java.lang.Exception
import kotlin.test.fail

class CellTest {

    @Test
    fun shouldNotAllowConvertingFromStringToRandomInt() {
        try {
            val cell = Cell.ofString("Talking to myself")
            cell.asInt()
            fail("Conversion should fail")
        } catch (e: Exception) { }
    }

    @Test
    fun shouldAllowNullValues() {

    }
}
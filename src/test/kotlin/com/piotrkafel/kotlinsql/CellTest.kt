package com.piotrkafel.kotlinsql

import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.assertions.isFailure

class CellTest {

    @Test
    fun shouldNotAllowConvertingFromStringToRandomInt() {
        val cell = Cell.ofString("Talking to myself")
        expectCatching { cell.asInt() }.isFailure()
    }
}
package com.piotrkafel.kotlinsql

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {

    @Test
    fun testParsingSelectQuery() {
        val parser = Parser(SqlLexer())

        val result = parser.parse("SELECT name from users;")

        assertTrue(result.isNotEmpty())
        assertEquals(1, result.size)

        val selectStatement = result[0]
        assertTrue(selectStatement is Statement.SelectStatement)
        assertEquals(listOf(Literal.IdentifierLiteral("name")), selectStatement.columns)
        assertEquals("users", selectStatement.tableName)
    }
}
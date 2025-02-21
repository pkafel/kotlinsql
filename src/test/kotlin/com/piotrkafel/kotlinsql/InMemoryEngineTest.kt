package com.piotrkafel.kotlinsql


import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class InMemoryEngineTest {

    @Test
    fun testStatementsInMemoryEngine() {
        val engine = InMemoryEngine()

        val createTableStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "age", type = ColumnType.INT))
        )
        val error = engine.createTable(createTableStatement)

        assertNull(error)

        val insertStatement = Statement.InsertStatement(tableName = "user", values = listOf(Literal.IntLiteral(13)))
        engine.insertData(insertStatement)

        val selectStatement = Statement.SelectStatement(tableName = "user", columns = listOf(Literal.IdentifierLiteral("age")))
        val queryResult = engine.selectData(selectStatement)

        if(queryResult !is QueryResult.Success) fail("Error while running select query")
        assertEquals(1, queryResult.rows.size)
        assertEquals(1, queryResult.rows[0].size)
        assertEquals(13, queryResult.rows[0][0].asInt())
    }


}
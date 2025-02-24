package com.piotrkafel.kotlinsql

import org.junit.jupiter.api.Test
import java.lang.Exception
import kotlin.test.*

class InMemoryEngineTest {

    @Test
    fun shouldReadYourWrites() {
        val engine = InMemoryEngine()

        val createTableStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "age", type = ColumnType.INT))
        )
        val error = engine.createTable(createTableStatement)

        assertNull(error)

        val insertStatement = Statement.InsertStatement(tableName = "user", values = listOf(Literal.IntLiteral(13)))
        engine.insertData(insertStatement)

        val selectStatement =
            Statement.SelectStatement(tableName = "user", columns = listOf(Literal.IdentifierLiteral("age")))
        val queryResult = engine.selectData(selectStatement)

        if (queryResult !is QueryResult.Success) fail("Error while running select query")
        assertEquals(1, queryResult.getResultSize())
        assertEquals(13, queryResult.getInt(0, "age"))
    }

    @Test
    fun shouldNotAllowToCreateTableWithTheSameNameTwice() {
        val engine = InMemoryEngine()

        val createTableStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "age", type = ColumnType.INT))
        )
        val error1 = engine.createTable(createTableStatement)

        assertNull(error1)

        val createTableWithTheSameNameStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "name", type = ColumnType.TEXT))
        )
        val error2 = engine.createTable(createTableWithTheSameNameStatement)

        assertNotNull(error2)
        assertTrue(error2 is StatementExecutionError.TableAlreadyExistsError)
    }

    @Test
    fun shouldNotAllowInsertingDataIntoNonExistingTable() {
        val engine = InMemoryEngine()

        val insertStatement = Statement.InsertStatement(tableName = "user", values = listOf(Literal.IntLiteral(13)))
        val error = engine.insertData(insertStatement)

        assertNotNull(error)
        assertTrue(error is StatementExecutionError.TableDoesNotExistError)
    }

    @Test
    fun shouldNotAllowReadingNonExistingColumns() {
        val engine = InMemoryEngine()

        val createTableStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "age", type = ColumnType.INT))
        )
        val error = engine.createTable(createTableStatement)

        assertNull(error)

        val selectStatement = Statement.SelectStatement(
            tableName = "user", columns = listOf(Literal.IdentifierLiteral("name"))
        )
        val queryResult = engine.selectData(selectStatement)

        assertEquals(QueryResult.Failure(StatementExecutionError.ColumnDoesNotExistError), queryResult)
    }

    @Test
    fun shouldThrowExceptionOnWrongTypeConversion() {
        val engine = InMemoryEngine()

        val createTableStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "name", type = ColumnType.TEXT))
        )
        val error = engine.createTable(createTableStatement)

        assertNull(error)

        val insertStatement = Statement.InsertStatement(tableName = "user", values = listOf(Literal.StringLiteral("John")))
        engine.insertData(insertStatement)

        val selectStatement =
            Statement.SelectStatement(tableName = "user", columns = listOf(Literal.IdentifierLiteral("name")))
        val queryResult = engine.selectData(selectStatement)

        if (queryResult !is QueryResult.Success) fail("Error while running select query")
        assertEquals(1, queryResult.getResultSize())

        try {
            queryResult.getInt(0, "name")
            fail("Should throw exception on unsupported conversion")
        } catch (e: Exception) { }
    }
}
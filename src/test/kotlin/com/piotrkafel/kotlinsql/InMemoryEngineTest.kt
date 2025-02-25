package com.piotrkafel.kotlinsql

import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.test.fail

class InMemoryEngineTest {

    @Test
    fun shouldReadYourWrites() {
        val engine = InMemoryEngine()

        val createTableStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "age", type = ColumnType.INT))
        )
        val error = engine.createTable(createTableStatement)

        expectThat(error).isNull()

        val insertStatement = Statement.InsertStatement(tableName = "user", values = listOf(Literal.IntLiteral(13)))
        engine.insertData(insertStatement)

        val selectStatement =
            Statement.SelectStatement(tableName = "user", columns = listOf(Literal.IdentifierLiteral("age")))
        val queryResult = engine.selectData(selectStatement)

        expectThat(queryResult)
            .isA<QueryResult.Success>()
            .and {
                get { getResultSize() }.isEqualTo(1)
                get { getInt(0, "age") }.isEqualTo(13)
            }
    }

    @Test
    fun shouldNotAllowToCreateTableWithTheSameNameTwice() {
        val engine = InMemoryEngine()

        val createTableStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "age", type = ColumnType.INT))
        )
        val error1 = engine.createTable(createTableStatement)

        expectThat(error1).isNull()

        val createTableWithTheSameNameStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "name", type = ColumnType.TEXT))
        )
        val error2 = engine.createTable(createTableWithTheSameNameStatement)

        expectThat(error2).isNotNull().and {
            get { error2 }.isA<StatementExecutionError.TableAlreadyExistsError>()
        }
    }

    @Test
    fun shouldNotAllowInsertingDataIntoNonExistingTable() {
        val engine = InMemoryEngine()

        val insertStatement = Statement.InsertStatement(tableName = "user", values = listOf(Literal.IntLiteral(13)))
        val error = engine.insertData(insertStatement)

        expectThat(error).isNotNull().and {
            get { error }.isA<StatementExecutionError.TableDoesNotExistError>()
        }
    }

    @Test
    fun shouldNotAllowReadingNonExistingColumns() {
        val engine = InMemoryEngine()

        val createTableStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "age", type = ColumnType.INT))
        )
        val error = engine.createTable(createTableStatement)

        expectThat(error).isNull()

        val selectStatement = Statement.SelectStatement(
            tableName = "user", columns = listOf(Literal.IdentifierLiteral("name"))
        )
        val queryResult = engine.selectData(selectStatement)

        expectThat(queryResult).isEqualTo(QueryResult.Failure(StatementExecutionError.ColumnDoesNotExistError))
    }

    @Test
    fun shouldThrowExceptionOnWrongTypeConversion() {
        val engine = InMemoryEngine()

        val createTableStatement = Statement.CreateTableStatement(
            name = "user",
            columns = listOf(ColumnDefinition(name = "name", type = ColumnType.TEXT))
        )
        val error = engine.createTable(createTableStatement)

        expectThat(error).isNull()

        val insertStatement = Statement.InsertStatement(tableName = "user", values = listOf(Literal.StringLiteral("John")))
        engine.insertData(insertStatement)

        val selectStatement =
            Statement.SelectStatement(tableName = "user", columns = listOf(Literal.IdentifierLiteral("name")))
        val queryResult = engine.selectData(selectStatement)

        if (queryResult !is QueryResult.Success) fail("Error while running select query")
        expectThat(queryResult.getResultSize()).isEqualTo(1)

        expectCatching {  queryResult.getInt(0, "name") }.isFailure()
    }
}
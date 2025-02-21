package com.piotrkafel.kotlinsql

import java.nio.ByteBuffer
import java.nio.ByteOrder

interface Engine {

    fun createTable(createTableStatement: Statement.CreateTableStatement): StatementExecutionError?

    fun insertData(insertStatement: Statement.InsertStatement): StatementExecutionError?

    fun selectData(selectStatement: Statement.SelectStatement): QueryResult
}

sealed class StatementExecutionError {
    data object TableDoesNotExistError : StatementExecutionError()
    data object ColumnDoesNotExistError : StatementExecutionError()
    data object NumberOfValuesDoesNotMatchNumberOfColumns : StatementExecutionError()
    data object ValueTypeDoesNotMatchColumnType : StatementExecutionError()
}

sealed class QueryResult {
    // todo for results we need as well type of the column
    data class Success(val rows: List<List<Cell>>) : QueryResult()

    data class Failure(val error: StatementExecutionError) : QueryResult()
}

typealias Cell = ByteArray

fun Cell.asInt(): Int = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN).int

fun Cell.asText(): String = String(this)

fun Int.toCell(): Cell = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()

fun String.toCell(): Cell = this.encodeToByteArray()

package com.piotrkafel.kotlinsql

interface Engine {

    fun createTable(createTableStatement: Statement.CreateTableStatement): StatementExecutionError?

    fun insertData(insertStatement: Statement.InsertStatement): StatementExecutionError?

    fun selectData(selectStatement: Statement.SelectStatement): QueryResult
}

sealed class StatementExecutionError {
    data object TableAlreadyExistsError : StatementExecutionError()
    data object TableDoesNotExistError : StatementExecutionError()
    data object ColumnDoesNotExistError : StatementExecutionError()
    data object NumberOfValuesDoesNotMatchNumberOfColumns : StatementExecutionError()
    data object ValueTypeDoesNotMatchColumnType : StatementExecutionError()
}

sealed class QueryResult {

    data class Success(private val rows: List<Map<String, Cell>>) : QueryResult() {

        fun getInt(rowNumber: Int, fieldName: String): Int {
            return rows[rowNumber][fieldName]!!.asInt()
        }

        fun getString(rowNumber: Int, fieldName: String): String {
            return rows[rowNumber][fieldName]!!.asText()
        }

        fun getResultSize(): Int = rows.size
    }

    data class Failure(val error: StatementExecutionError) : QueryResult()
}
class Cell(private val value: ByteArray) {

    fun asInt(): Int = String(value).toInt()

    fun asText(): String = String(value)

    companion object {

        fun ofInt(value: Int): Cell = Cell(value.toString().encodeToByteArray())

        fun ofString(value: String): Cell = Cell(value.encodeToByteArray())
    }
}

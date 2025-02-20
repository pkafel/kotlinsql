package com.piotrkafel.kotlinsql

interface Engine {

    fun createTable(createTableStatement: Statement.CreateTableStatement): StatementExecutionError?

    fun insertData(insertStatement: Statement.InsertStatement): StatementExecutionError?

    fun selectData(selectStatement: Statement.SelectStatement): QueryResult
}

sealed class StatementExecutionError {
    data object TableDoesNotExistError : StatementExecutionError()
    data object ColumnDoesNotExistError : StatementExecutionError()
}

sealed class QueryResult {
    data class Success(val rows: Array<Array<Cell>>) : QueryResult() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            return rows.contentDeepEquals(other.rows)
        }

        override fun hashCode(): Int {
            return rows.contentDeepHashCode()
        }
    }

    data object Failure : QueryResult()
}

interface Cell{
    fun asText(): String
    fun asInt(): Int
}
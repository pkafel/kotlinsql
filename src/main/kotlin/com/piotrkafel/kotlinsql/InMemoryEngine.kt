package com.piotrkafel.kotlinsql

import java.nio.ByteBuffer
import java.nio.ByteOrder

class InMemoryEngine(private val tables: MutableMap<String, Table> = mutableMapOf()): Engine {

    override fun createTable(createTableStatement: Statement.CreateTableStatement): StatementExecutionError? {
        tables[createTableStatement.name] = Table(columns = createTableStatement.columns, mutableListOf())
        return null
    }

    override fun insertData(insertStatement: Statement.InsertStatement): StatementExecutionError? {
        if(!tables.containsKey(insertStatement.tableName)) return StatementExecutionError.TableDoesNotExistError

        val table = tables[insertStatement.tableName]

        // TODO implement the insertion part

        return null
    }

    override fun selectData(selectStatement: Statement.SelectStatement): QueryResult {
        TODO("Not yet implemented")
    }
}

typealias MemoryCell = ByteArray

fun MemoryCell.asInt(): Int {
    return ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN).int
}

fun MemoryCell.asText(): String {
    return String(this)
}

class Table(columns: List<ColumnDefinition>, rows: List<List<MemoryCell>>)
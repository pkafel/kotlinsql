package com.piotrkafel.kotlinsql

import java.nio.ByteBuffer
import java.nio.ByteOrder

class InMemoryEngine(val tables: MutableMap<String, Table> = mutableMapOf()): Engine {

    override fun createTable(createTableStatement: Statement.CreateTableStatement): StatementExecutionError? {
        TODO("Not yet implemented")
    }

    override fun insertData(insertStatement: Statement.InsertStatement): StatementExecutionError? {
        TODO("Not yet implemented")
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
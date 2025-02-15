package com.piotrkafel.kotlinsql

sealed interface Statement {

    data class CreateTableStatement(
        val name: String,
        val columns: List<ColumnDefinition>
    ) : Statement

    data class InsertStatement(
        val tableName: String,
        val values: List<Literal>
    ) : Statement

    data class SelectStatement(
        val columns: List<Literal>,
        val tableName: String
    ) : Statement
}

data class ColumnDefinition(
    val name: String,
    val type: ColumnType
)

enum class ColumnType {
    INT, TEXT
}

sealed interface Literal {

    data class IntLiteral(val value: Int) : Literal

    data class StringLiteral(val value: String) : Literal

    data class IdentifierLiteral(val name: String): Literal
}

package com.piotrkafel.kotlinsql

class InMemoryEngine(private val tables: MutableMap<String, Table> = mutableMapOf()) : Engine {

    override fun createTable(createTableStatement: Statement.CreateTableStatement): StatementExecutionError? {
        tables[createTableStatement.name] = Table(columns = createTableStatement.columns, mutableListOf())
        return null
    }

    override fun insertData(insertStatement: Statement.InsertStatement): StatementExecutionError? {
        if (!tables.containsKey(insertStatement.tableName)) return StatementExecutionError.TableDoesNotExistError

        val table = tables.getValue(insertStatement.tableName)

        if (table.columns.size != insertStatement.values.size) return StatementExecutionError.NumberOfValuesDoesNotMatchNumberOfColumns

        // for simplicity, we assume the order of the values is the same as the order of columns in the table
        val row = mutableListOf<Cell>()
        for (index in insertStatement.values.indices) {
            val valueToInsert = insertStatement.values[index]
            val columnDefinition = table.columns[index]

            when (valueToInsert) {
                is Literal.IntLiteral -> if (columnDefinition.type == ColumnType.INT) row.add(valueToInsert.value.toCell()) else return StatementExecutionError.ValueTypeDoesNotMatchColumnType
                is Literal.StringLiteral -> if (columnDefinition.type == ColumnType.TEXT) row.add(valueToInsert.value.toCell()) else return StatementExecutionError.ValueTypeDoesNotMatchColumnType
                else -> throw IllegalStateException("Unsupported type in INSERT statement")
            }
        }

        table.rows.add(row)
        return null
    }

    override fun selectData(selectStatement: Statement.SelectStatement): QueryResult {
        val table = tables[selectStatement.tableName]
            ?: return QueryResult.Failure(error = StatementExecutionError.TableDoesNotExistError)

        val identifiersColumns = selectStatement.columns
            .filterIsInstance<Literal.IdentifierLiteral>()
            .associateBy(keySelector = {it.name}, valueTransform = {literal -> table.columns.indexOfFirst { it.name == literal.name }})

        val result = mutableListOf<MutableList<Cell>>()
        for(rowIndex in table.rows.indices) {
            val row = table.rows[rowIndex]
            val rowResult = mutableListOf<Cell>()
            for(column in selectStatement.columns) {
                when(column) {
                    is Literal.IntLiteral -> rowResult.add(column.value.toCell())
                    is Literal.StringLiteral -> rowResult.add(column.value.toCell())
                    is Literal.IdentifierLiteral -> rowResult.add(row[identifiersColumns[column.name]!!])
                }
            }
            result.add(rowResult)
        }

        return QueryResult.Success(rows = result)
    }
}

class Table(val columns: List<ColumnDefinition>, val rows: MutableList<List<Cell>>)

package com.piotrkafel.kotlinsql

class InMemoryEngine(private val tables: MutableMap<String, Table> = mutableMapOf()) : Engine {

    override fun createTable(createTableStatement: Statement.CreateTableStatement): StatementExecutionError? {
        if(tables.contains(createTableStatement.name)) return StatementExecutionError.TableAlreadyExistsError
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
                is Literal.IntLiteral -> if (columnDefinition.type == ColumnType.INT) row.add(Cell.ofInt(valueToInsert.value)) else return StatementExecutionError.ValueTypeDoesNotMatchColumnType
                is Literal.StringLiteral -> if (columnDefinition.type == ColumnType.TEXT) row.add(Cell.ofString(valueToInsert.value)) else return StatementExecutionError.ValueTypeDoesNotMatchColumnType
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
            .associateBy(
                keySelector = { it.name },
                valueTransform = { literal -> table.columns.indexOfFirst { it.name == literal.name } }
            )

        if (identifiersColumns.values.any { it < 0 }) {
            return QueryResult.Failure(error = StatementExecutionError.ColumnDoesNotExistError)
        }

        val result = mutableListOf<Map<String, Cell>>()
        for (rowIndex in table.rows.indices) {
            val row = table.rows[rowIndex]
            val rowResult = mutableMapOf<String, Cell>()
            for (column in selectStatement.columns) {
                when (column) {
                    // let's assume here the column name will be the same as the value
                    is Literal.IntLiteral -> rowResult[column.value.toString()] = Cell.ofInt(column.value)
                    is Literal.StringLiteral -> rowResult[column.value] = Cell.ofString(column.value)
                    is Literal.IdentifierLiteral -> rowResult[column.name] = row[identifiersColumns[column.name]!!]
                }
            }
            result.add(rowResult.toMap())
        }

        return QueryResult.Success(rows = result.toList())
    }
}

class Table(val columns: List<ColumnDefinition>, val rows: MutableList<List<Cell>>)

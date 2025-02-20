package com.piotrkafel.kotlinsql

class Parser(private val sqlLexer: SqlLexer) {

    fun parse(input: String): List<Statement> {
        val tokens = sqlLexer.lex(input)

        val result = mutableListOf<Statement>()
        var cursor = 0
        while (cursor < tokens.size) {
            when (val statement = parseStatement(tokens, cursor)) {
                is ParsingStatementResult.Success -> {
                    cursor = statement.cursor
                    result.add(statement.statement)

                    var atLeastOneSemicolon = false
                    while (expectSemicolon(tokens, cursor)) {
                        atLeastOneSemicolon = true
                        cursor++
                    }

                    if (!atLeastOneSemicolon) return listOf()
                }

                is ParsingStatementResult.Failure -> return listOf()
            }
        }

        return result
    }

    // TODO extract parsers to separate classes so we can iterate over them
    private fun parseStatement(tokens: List<Token>, initialCursor: Int): ParsingStatementResult {
        val selectStatement = parseSelectStatement(tokens, initialCursor)
        if (selectStatement is ParsingStatementResult.Success) {
            return selectStatement
        }

        val insertStatement = parseInsertStatement(tokens, initialCursor)
        if (insertStatement is ParsingStatementResult.Success) {
            return insertStatement
        }

        val createStatement = parseCreateStatement(tokens, initialCursor)
        if (createStatement is ParsingStatementResult.Success) {
            return createStatement
        }

        return ParsingStatementResult.Failure
    }

    private fun parseSelectStatement(tokens: List<Token>, initialCursor: Int): ParsingStatementResult {
        if (!expectKeyword(tokens, initialCursor, Keyword.SELECT)) return ParsingStatementResult.Failure

        var cursor = initialCursor + 1
        var columns = listOf<Literal>()

        // parse literals inside select query
        when (val literalsResult = parseComaSeparatedLiterals(
            tokens,
            cursor,
            true,
            Token(value = Keyword.FROM.value, kind = TokenKind.KEYWORD, loc = Location(0u))
        )) {
            is ParsingLiteralsResult.Success -> {
                cursor = literalsResult.cursor
                columns = literalsResult.literals
            }

            is ParsingLiteralsResult.Failure -> return ParsingStatementResult.Failure
        }

        if (!expectKeyword(tokens, cursor, Keyword.FROM)) return ParsingStatementResult.Failure
        cursor++

        if (!expectIdentifier(tokens, cursor)) return ParsingStatementResult.Failure
        val tableName = tokens[cursor].value
        cursor++

        return ParsingStatementResult.Success(
            statement = Statement.SelectStatement(columns = columns, tableName = tableName), cursor = cursor
        )
    }

    private fun parseInsertStatement(tokens: List<Token>, initialCursor: Int): ParsingStatementResult {
        if (!expectKeyword(tokens, initialCursor, Keyword.INSERT)) return ParsingStatementResult.Failure
        var cursor = initialCursor + 1

        if (!expectKeyword(tokens, cursor, Keyword.INTO)) return ParsingStatementResult.Failure
        cursor++

        if (!expectIdentifier(tokens, cursor)) return ParsingStatementResult.Failure
        val tableName = tokens[cursor].value
        cursor++

        if (!expectKeyword(tokens, cursor, Keyword.VALUES)) return ParsingStatementResult.Failure
        cursor++

        if (!expectSymbol(tokens, cursor, Symbol.LEFT_PAREN)) return ParsingStatementResult.Failure
        cursor++

        // parse values we want to insert into table
        var literals = listOf<Literal>()
        when (val literalsResult = parseComaSeparatedLiterals(
            tokens,
            cursor,
            false,
            Token(value = Symbol.RIGHT_PAREN.value, kind = TokenKind.SYMBOL, loc = Location(0u))
        )) {
            is ParsingLiteralsResult.Success -> {
                cursor = literalsResult.cursor
                literals = literalsResult.literals
            }

            is ParsingLiteralsResult.Failure -> return ParsingStatementResult.Failure
        }

        if (!expectSymbol(tokens, cursor, Symbol.RIGHT_PAREN)) return ParsingStatementResult.Failure
        cursor++

        return ParsingStatementResult.Success(
            statement = Statement.InsertStatement(tableName = tableName, values = literals), cursor = cursor
        )
    }

    private fun parseCreateStatement(tokens: List<Token>, initialCursor: Int): ParsingStatementResult {
        if (!expectKeyword(tokens, initialCursor, Keyword.CREATE)) return ParsingStatementResult.Failure
        var cursor = initialCursor + 1

        if (!expectKeyword(tokens, cursor, Keyword.TABLE)) return ParsingStatementResult.Failure
        cursor++

        if (!expectIdentifier(tokens, cursor)) return ParsingStatementResult.Failure
        val tableName = tokens[cursor].value
        cursor++

        if (!expectSymbol(tokens, cursor, Symbol.LEFT_PAREN)) return ParsingStatementResult.Failure
        cursor++

        var columns = emptyList<ColumnDefinition>()
        when(val columnsParsingResult = parseTableColumns(tokens, cursor)) {
            is ParsingColumnsDefinition.Failure -> return ParsingStatementResult.Failure
            is ParsingColumnsDefinition.Success -> {
                cursor = columnsParsingResult.cursor
                columns = columnsParsingResult.columns

            }
        }

        if (!expectSymbol(tokens, cursor, Symbol.RIGHT_PAREN)) return ParsingStatementResult.Failure
        cursor++

        return ParsingStatementResult.Success(
            statement = Statement.CreateTableStatement(
                name = tableName,
                columns = columns,
            ), cursor = cursor
        )
    }

    private fun parseComaSeparatedLiterals(
        tokens: List<Token>, initialCursor: Int, allowIdentifiers: Boolean, endDelimiter: Token
    ): ParsingLiteralsResult {
        if (initialCursor >= tokens.size) return ParsingLiteralsResult.Failure("Unexpected end of tokens")

        var cursor = initialCursor
        val result = mutableListOf<Literal>()
        while (cursor < tokens.size && tokens[cursor] != endDelimiter) {
            val currentToken = tokens[cursor]

            when (currentToken.kind) {
                TokenKind.STRING -> result.add(Literal.StringLiteral(currentToken.value))
                TokenKind.NUMERIC -> result.add(Literal.IntLiteral(currentToken.value.toInt()))
                TokenKind.IDENTIFIER -> {
                    if (allowIdentifiers) result.add(Literal.IdentifierLiteral(currentToken.value))
                    else return ParsingLiteralsResult.Failure("Unexpected token: $currentToken")
                }

                else -> return ParsingLiteralsResult.Failure("Unexpected token: $currentToken")
            }
            cursor++

            if (expectSymbol(tokens, cursor, Symbol.COMMA)) {
                cursor++
            }
        }

        return ParsingLiteralsResult.Success(literals = result, cursor = cursor)
    }

    private fun parseTableColumns(tokens: List<Token>, initialCursor: Int): ParsingColumnsDefinition {
        if (initialCursor >= tokens.size) return ParsingColumnsDefinition.Failure("Unexpected end of tokens")

        var cursor = initialCursor
        val columns = mutableListOf<ColumnDefinition>()
        while (cursor < tokens.size) {
            val potentialIdentifierToken = tokens[cursor]

            when (potentialIdentifierToken.kind) {
                TokenKind.IDENTIFIER -> {
                    val columnName = potentialIdentifierToken.value
                    cursor++
                    if (cursor >= tokens.size) return ParsingColumnsDefinition.Failure("Unexpected end of tokens")
                    val potentialTypeToken = tokens[cursor]

                    when(potentialTypeToken.kind) {
                        TokenKind.KEYWORD -> {
                            when(potentialTypeToken.value) {
                                Keyword.INT.value -> columns.add(ColumnDefinition(name = columnName, type = ColumnType.INT))
                                Keyword.TEXT.value -> columns.add(ColumnDefinition(name = columnName, type = ColumnType.TEXT))
                                else -> ParsingColumnsDefinition.Failure("Expected column type but got ${potentialIdentifierToken.value}")
                            }
                        }
                        else -> return ParsingColumnsDefinition.Failure("Expected column type but got ${potentialIdentifierToken.value}")
                    }

                }
                else -> return ParsingColumnsDefinition.Failure("Expected column type but got ${potentialIdentifierToken.value}")
            }
            cursor++

            if (expectSymbol(tokens, cursor, Symbol.COMMA)) {
                cursor++
            } else break
        }

        return ParsingColumnsDefinition.Success(columns = columns, cursor = cursor)
    }

    private fun expectSemicolon(tokens: List<Token>, cursor: Int): Boolean =
        expectSymbol(tokens, cursor, Symbol.SEMICOLON)

    private fun expectSymbol(tokens: List<Token>, cursor: Int, expectedSymbol: Symbol): Boolean {
        if (cursor >= tokens.size) return false

        val token = tokens[cursor]
        return token.kind == TokenKind.SYMBOL && token.value == expectedSymbol.value
    }

    private fun expectKeyword(tokens: List<Token>, cursor: Int, expectedKeyword: Keyword): Boolean {
        if (cursor >= tokens.size) return false

        val token = tokens[cursor]
        return token.kind == TokenKind.KEYWORD && Keyword.valueOf(token.value.uppercase()) == expectedKeyword
    }

    private fun expectIdentifier(tokens: List<Token>, cursor: Int): Boolean {
        if (cursor >= tokens.size) return false

        val token = tokens[cursor]
        return token.kind == TokenKind.IDENTIFIER
    }

    sealed class ParsingColumnsDefinition {
        class Success(val columns: List<ColumnDefinition>, val cursor: Int) : ParsingColumnsDefinition()
        class Failure(val errorMessage: String) : ParsingColumnsDefinition()
    }

    sealed class ParsingLiteralsResult {
        class Success(val literals: List<Literal>, val cursor: Int) : ParsingLiteralsResult()
        class Failure(val errorMessage: String) : ParsingLiteralsResult()
    }

    sealed class ParsingStatementResult {
        class Success(val statement: Statement, val cursor: Int) : ParsingStatementResult()
        data object Failure : ParsingStatementResult()
    }
}
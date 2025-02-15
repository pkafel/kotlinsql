package com.piotrkafel.kotlinsql

class Parser(private val sqlLexer: SqlLexer) {

    fun parse(input: String): List<Statement> {
        val tokens = sqlLexer.lex(input)

        val result = mutableListOf<Statement>()
        var cursor = 0
        while(cursor < tokens.size) {
            when(val statement = parseStatement(tokens, cursor)) {
                is ParsingResult.Success -> {
                    cursor = statement.cursor
                    result.add(statement.statement)

                    var atLeastOneSemicolon = false
                    while(expectSemicolon(tokens, cursor)) {
                        atLeastOneSemicolon = true
                        cursor++
                    }

                    if (!atLeastOneSemicolon) return listOf()
                }
                is ParsingResult.Failure -> return listOf()
            }
        }

        return result
    }

    // TODO extract parsers to separate classes so we can iterate over them
    private fun parseStatement(tokens: List<Token>, initialCursor: Int): ParsingResult {
        val selectStatement = parseSelectStatement(tokens, initialCursor)
        if(selectStatement is ParsingResult.Success) {
            return selectStatement
        }

        val insertStatement = parseInsertStatement(tokens, initialCursor)
        if(insertStatement is ParsingResult.Success) {
            return insertStatement
        }

        val createStatement = parseCreateStatement(tokens, initialCursor)
        if(createStatement is ParsingResult.Success) {
            return createStatement
        }

        return ParsingResult.Failure
    }

    private fun parseSelectStatement(tokens: List<Token>, initialCursor: Int): ParsingResult {
        if(!expectKeyword(tokens, initialCursor, Keyword.SELECT)) return ParsingResult.Failure

        var cursor = initialCursor + 1
        val columns = mutableListOf<Literal>()

        while(cursor < tokens.size) {
            val currentToken = tokens[cursor]
            when (currentToken.kind) {
                TokenKind.IDENTIFIER -> columns.add(Literal.IdentifierLiteral(currentToken.value))
                TokenKind.STRING -> columns.add(Literal.StringLiteral(currentToken.value))
                TokenKind.NUMERIC -> columns.add(Literal.IntLiteral(currentToken.value.toInt()))
                else -> {
                    println("Expected identifier, string or numeric inside SELECT statement")
                    return ParsingResult.Failure
                }
            }

            cursor++
            if(!expectSymbol(tokens, cursor, Symbol.COMMA)) break
            cursor++
        }

        if(!expectKeyword(tokens, cursor, Keyword.FROM)) return ParsingResult.Failure
        cursor++

        if(!expectIdentifier(tokens, cursor)) return ParsingResult.Failure
        val tableName = tokens[cursor].value
        cursor++

        return ParsingResult.Success(
            statement = Statement.SelectStatement(columns = columns, tableName = tableName),
            cursor = cursor
        )
    }

    private fun parseInsertStatement(tokens: List<Token>, cursor: Int): ParsingResult {
        return ParsingResult.Failure
    }

    private fun parseCreateStatement(tokens: List<Token>, cursor: Int): ParsingResult {
        return ParsingResult.Failure
    }

    private fun expectSemicolon(tokens: List<Token>, cursor: Int): Boolean = expectSymbol(tokens, cursor, Symbol.SEMICOLON)

    private fun expectSymbol(tokens: List<Token>, cursor: Int, expectedSymbol: Symbol): Boolean {
        if(cursor >= tokens.size) return false

        val token = tokens[cursor]
        return token.kind == TokenKind.SYMBOL && token.value == expectedSymbol.value
    }

    private fun expectKeyword(tokens: List<Token>, cursor: Int, expectedKeyword: Keyword): Boolean {
        if(cursor >= tokens.size) return false

        val token = tokens[cursor]
        return token.kind == TokenKind.KEYWORD && Keyword.valueOf(token.value.uppercase()) == expectedKeyword
    }

    private fun expectIdentifier(tokens: List<Token>, cursor: Int): Boolean {
        if(cursor >= tokens.size) return false

        val token = tokens[cursor]
        return token.kind == TokenKind.IDENTIFIER
    }

    sealed class ParsingResult {

        class Success(val statement: Statement, val cursor: Int): ParsingResult()

        data object Failure : ParsingResult()
    }
}
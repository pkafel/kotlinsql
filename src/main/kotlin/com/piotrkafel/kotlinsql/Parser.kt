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

        return ParsingResult.Success(statement = Statement.SelectStatement(listOf(), ""), cursor = cursor)
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
        return token.kind == TokenKind.SYMBOL && Symbol.valueOf(token.value) == expectedSymbol
    }

    private fun expectKeyword(tokens: List<Token>, cursor: Int, expectedKeyword: Keyword): Boolean {
        if(cursor >= tokens.size) return false

        val token = tokens[cursor]
        return token.kind == TokenKind.SYMBOL && Keyword.valueOf(token.value) == expectedKeyword
    }

    sealed class ParsingResult {

        class Success(val statement: Statement, val cursor: Int): ParsingResult()

        data object Failure : ParsingResult()
    }
}
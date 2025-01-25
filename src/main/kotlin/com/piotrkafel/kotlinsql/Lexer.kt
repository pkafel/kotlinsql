package com.piotrkafel.kotlinsql

import com.piotrkafel.kotlinsql.LexerResult.Success

data class Location(
    val col: UInt
)

enum class Keyword(val value: String) {
    SELECT("select"),
    FROM("from"),
    AS("as"),
    TABLE("table"),
    CREATE("create"),
    INSERT("insert"),
    INTO("into"),
    VALUES("values"),
    INT("int"),
    TEXT("text")
}

enum class Symbol(val value: String) {
    SEMICOLON(";"),
    ASTERISK("*"),
    COMMA(","),
    LEFT_PAREN("("),
    RIGHT_PAREN(")")
}

enum class TokenKind {
    KEYWORD,
    SYMBOL,
    IDENTIFIER,
    STRING,
    NUMERIC
}

data class Token(
    val value: String,
    val kind: TokenKind,
    val loc: Location
) {
    fun equals(other: Token): Boolean {
        return value == other.value && kind == other.kind
    }
}

data class Cursor(
    val pointer: UInt,
    val loc: Location
)

sealed class LexerResult(cursor: Cursor) {
    data class Success(val token: Token, val cursor: Cursor): LexerResult(cursor)
    data class Failure(val cursor: Cursor) : LexerResult(cursor)
}

interface Lexer {
    fun lex(input: String, cursor: Cursor): LexerResult
}

class SqlLexer {

    private val lexers = arrayOf(KeywordLexer(), StringLexer(), SymbolLexer())

    fun lex(input: String): List<Token> {
        var cursor = Cursor(0u, Location(0u))
        val result = mutableListOf<Token>()

        while(cursor.pointer < input.length.toUInt()) {
            var movedPointer = false
            for(lexer in lexers) {
                val lexingResult = lexer.lex(input, cursor)

                if(lexingResult is Success) {
                    result.add(lexingResult.token)
                    cursor = lexingResult.cursor
                    movedPointer = true
                }
            }

            if(!movedPointer) {
                // TODO handle cases when no lexer could move the pointer
            }
        }

        return result
    }
}

class KeywordLexer: Lexer {

    private val keywords = Keyword.entries.map { it.value.lowercase() }

    override fun lex(input: String, cursor: Cursor): LexerResult {
        val match = longestMatching(input, cursor)

        if(match == "") return LexerResult.Failure(cursor)

        return Success(
            token = Token(
                value = match,
                kind = TokenKind.KEYWORD,
                loc = Location(
                    col = cursor.loc.col,
                )
            ),
            cursor = cursor.copy(
                pointer = cursor.pointer + match.length.toUInt(),
                loc = Location(
                    col = cursor.loc.col + match.length.toUInt()
                )
            )
        )
    }

    private fun longestMatching(input: String, inputCursor: Cursor): String {
        val initialPointer = inputCursor.pointer.toInt()
        var cur = inputCursor
        var match = ""

        // Iterate through the source string from the cursor's position
        while (cur.pointer < input.length.toUInt()) {
            val value = input.substring(initialPointer, cur.pointer.toInt() + 1).lowercase()

            val matchingOptions = keywords.filter { it.startsWith(value) }

            // If no options match the current prefix, stop processing
            if (matchingOptions.isEmpty()) break

            // Update the longest match if the exact match is found
            // This handles cases like INT vs INTO
            matchingOptions.forEach { option ->
                if (option == value && option.length > match.length) {
                    match = option
                }
            }

            cur = cur.copy(pointer = cur.pointer + 1u)
        }

        return match
    }
}

class StringLexer: Lexer {

    private val delimiter =  '\''

    override fun lex(input: String, cursor: Cursor): LexerResult {
        // Check if there's nothing left to parse
        if (cursor.pointer.toInt() >= input.length) {
            return LexerResult.Failure(cursor)
        }

        // Check if the current char is start of a string
        if(input[cursor.pointer.toInt()] != delimiter) {
            return LexerResult.Failure(cursor)
        }

        val value = StringBuilder()
        var movingPointer = cursor.pointer.toInt().inc()

        while (movingPointer < input.length) {
            val nextCharacter = input[movingPointer]

            if(nextCharacter == delimiter) {
                // if end of input or next character is not delimiter (in SQL we use double quote to escape quote)
                if(movingPointer + 1 == input.length || input[movingPointer + 1] != delimiter) {
                    // increase pointer so it points to next character that we will need to consider
                    movingPointer++
                    return Success(
                        Token(
                            value = value.toString(),
                            kind = TokenKind.STRING,
                            loc = cursor.loc.copy(col = cursor.loc.col)
                        ),
                        cursor = cursor.copy(
                            pointer = movingPointer.toUInt(),
                            loc = cursor.loc.copy(
                                col = movingPointer.toUInt()
                            )
                        ),
                    )
                } else {
                    value.append(delimiter)
                    movingPointer++
                }
            } else {
                value.append(nextCharacter)
                movingPointer++
            }
        }

        return LexerResult.Failure(cursor)
    }
}

class SymbolLexer: Lexer {

    override fun lex(input: String, cursor: Cursor): LexerResult {
        val currentChar = input[cursor.pointer.toInt()]
        return LexerResult.Failure(cursor)
    }
}
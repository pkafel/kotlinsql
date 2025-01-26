package com.piotrkafel.kotlinsql

import com.piotrkafel.kotlinsql.LexerResult.Failure
import com.piotrkafel.kotlinsql.LexerResult.Success
import java.lang.RuntimeException

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
)

data class Cursor(
    val pointer: UInt,
    val loc: Location
)

sealed class LexerResult() {
    data class Success(val token: Token, val cursor: Cursor): LexerResult()
    data class Failure(val cursor: Cursor) : LexerResult()
}

interface Lexer {
    fun lex(input: String, cursor: Cursor): LexerResult
}

class SqlLexer {

    // order of lexers matter - for example keyword lexer needs to be before identifier lexer
    private val lexers = arrayOf(KeywordLexer(), SymbolLexer(), StringLexer(), NumericLexer(), IdentifierLexer())

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
                    break
                }

                if(lexingResult is Failure && lexingResult.cursor != cursor) {
                    movedPointer = true
                    cursor = lexingResult.cursor
                    break
                }
            }

            if(!movedPointer) {
                throw RuntimeException("Could not process input. Problem at ${cursor.pointer} position")
            }
        }

        return result
    }
}

class KeywordLexer: Lexer {

    private val keywords = Keyword.entries.map { it.value.lowercase() }

    override fun lex(input: String, cursor: Cursor): LexerResult {
        val match = longestMatching(input, cursor)

        if(match == "") return Failure(cursor)

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
            return Failure(cursor)
        }

        // Check if the current char is start of a string
        if(input[cursor.pointer.toInt()] != delimiter) {
            return Failure(cursor)
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

        return Failure(cursor)
    }
}

class SymbolLexer: Lexer {

    private val symbols = Symbol.entries.associateBy { it.value.first() }

    override fun lex(input: String, cursor: Cursor): LexerResult {
        val currentChar = input[cursor.pointer.toInt()]

        // whitespaces could be handled in a separate lexer
        // for now they can stay here, and we will recognize them based on moved cursor
        if(currentChar == ' ' || currentChar == '\t') {
            return Failure(
                Cursor(
                    pointer = cursor.pointer + 1u,
                    loc = Location(cursor.pointer + 1u)
                )
            )
        }

        if(symbols.contains(currentChar)) {
            return Success(
                token =  Token(
                    value = currentChar.toString(),
                    kind = TokenKind.SYMBOL,
                    loc = Location(col = cursor.pointer)
                ),
                cursor = Cursor(
                    pointer = cursor.pointer + 1u,
                    loc = Location(col = cursor.pointer + 1u)
                )
            )
        }

        return Failure(cursor)
    }
}

class NumericLexer: Lexer {

    override fun lex(input: String, cursor: Cursor): LexerResult {
        var movingPointer = cursor.pointer

        var result = ""
        val firstChar = input[movingPointer.toInt()]

        if(firstChar.isDigit() || firstChar == '-'){
            result += firstChar
            movingPointer++
        }

        var foundPointer = false
        while (movingPointer < input.length.toUInt()) {
            val currentChar = input[movingPointer.toInt()]

            if(currentChar.isDigit()) {
                result += currentChar
                movingPointer++
                continue
            } else if(currentChar == '.') {
                if(foundPointer) return Failure(cursor)
                foundPointer = true
                result += currentChar
                movingPointer++
                continue
            } else {
                break
            }
        }
        val doubleResult = result.toDoubleOrNull()
        return if(doubleResult == null) Failure(cursor)
        else Success(
            token = Token(
                value = result,
                kind = TokenKind.NUMERIC,
                loc = Location(col = cursor.pointer)
            ),
            cursor = Cursor(movingPointer, loc = Location(col = movingPointer))
        )

    }
}

class IdentifierLexer: Lexer {

    override fun lex(input: String, cursor: Cursor): LexerResult {
        val firstChar = input[cursor.pointer.toInt()]

        if(!firstChar.isLetter()) return Failure(cursor)

        var result = "" + firstChar
        var movingPointer = cursor.pointer + 1u

        while(movingPointer < input.length.toUInt()) {
            val currentChar = input[movingPointer.toInt()]

            if(currentChar.isLetterOrDigit()) {
                result += currentChar
                movingPointer++
            } else {
                break
            }
        }

        return Success(
            token = Token(
                value = result,
                kind = TokenKind.IDENTIFIER,
                loc = Location(cursor.pointer)
            ),
            cursor = Cursor(
                pointer = movingPointer,
                loc = Location(col = movingPointer)
            )
        )
    }
}
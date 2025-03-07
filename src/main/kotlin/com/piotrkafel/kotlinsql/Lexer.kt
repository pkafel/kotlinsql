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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Token) return false
        return value == other.value && kind == other.kind
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + kind.hashCode()
        return result
    }
}

data class Cursor(
    val pointer: UInt,
    val loc: Location
)

sealed class LexerResult {
    data class Success(val tokens: List<Token>, val cursor: Cursor): LexerResult()
    data class Failure(val cursor: Cursor) : LexerResult()
}

interface Lexer {
    fun lex(input: String, cursor: Cursor = Cursor(pointer = 0u, loc = Location(col = 0u))): LexerResult
}

class SqlLexer: Lexer {

    // order of lexers matter - for example keyword lexer needs to be before identifier lexer
    private val lexers = arrayOf(KeywordLexer(), SymbolLexer(), StringLexer(), NumericLexer(), IdentifierLexer())

    override fun lex(input: String, initialCursor: Cursor): LexerResult {
        val tokens = mutableListOf<Token>()
        var cursor = initialCursor

        while(cursor.pointer < input.length.toUInt()) {
            var movedPointer = false
            for(lexer in lexers) {
                val lexingResult = lexer.lex(input, cursor)

                if(lexingResult is Success) {
                    tokens.addAll(lexingResult.tokens)
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

        return Success(tokens = tokens, cursor = cursor)
    }
}

/**
 * Any input that matches in lowercase Keyword enum.
 */
class KeywordLexer: Lexer {

    private val keywords = Keyword.entries.map { it.value.lowercase() }

    override fun lex(input: String, cursor: Cursor): LexerResult {
        val match = longestMatching(input, cursor)

        if(match == "") return Failure(cursor)

        return Success(
            tokens = listOf(Token(
                value = match,
                kind = TokenKind.KEYWORD,
                loc = Location(
                    col = cursor.loc.col,
                )
            )),
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

/**
 * Any input wrapped between two single quotes is considered string.
 */
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
                        listOf(Token(
                            value = value.toString(),
                            kind = TokenKind.STRING,
                            loc = cursor.loc.copy(col = cursor.loc.col)
                        )),
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

/**
 * Any input that matches in Symbol enum.
 */
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
                tokens =  listOf(Token(
                    value = currentChar.toString(),
                    kind = TokenKind.SYMBOL,
                    loc = Location(col = cursor.pointer)
                )),
                cursor = Cursor(
                    pointer = cursor.pointer + 1u,
                    loc = Location(col = cursor.pointer + 1u)
                )
            )
        }

        return Failure(cursor)
    }
}

/**
 * A valid numeric input must follow these rules:
 * 1. It may start with an optional '-' (negative sign).
 * 2. It can begin with a digit or a '.' (decimal point).
 * 3. If it starts with a '.', it must be followed by at least one digit.
 * 4. Digits may optionally be followed by a single '.' (decimal point) for floating-point numbers.
 * 5. The decimal point can only appear once in the numeric sequence.
 * 6. No additional characters (e.g., letters or symbols) are allowed in the numeric sequence.
 *
 * Examples of valid inputs:
 * - "123"
 * - "-456"
 * - "78.90"
 * - "-0.123"
 * - ".123"
 */
class NumericLexer: Lexer {

    override fun lex(input: String, cursor: Cursor): LexerResult {
        var movingPointer = cursor.pointer

        var result = ""
        val firstChar = input[movingPointer.toInt()]

        if(firstChar == '-'){
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
            tokens = listOf(Token(
                value = result,
                kind = TokenKind.NUMERIC,
                loc = Location(col = cursor.pointer)
            )),
            cursor = Cursor(movingPointer, loc = Location(col = movingPointer))
        )
    }
}

/**
 * By identifier, we understand anything that starts with letter and thats followed by letters, numbers or _.
 */
class IdentifierLexer: Lexer {

    override fun lex(input: String, cursor: Cursor): LexerResult {
        val firstChar = input[cursor.pointer.toInt()]

        if(!firstChar.isLetter()) return Failure(cursor)

        var result = "" + firstChar
        var movingPointer = cursor.pointer + 1u

        while(movingPointer < input.length.toUInt()) {
            val currentChar = input[movingPointer.toInt()]

            if(currentChar.isLetterOrDigit() || currentChar == '_') {
                result += currentChar
                movingPointer++
            } else {
                break
            }
        }

        return Success(
            tokens = listOf(Token(
                value = result,
                kind = TokenKind.IDENTIFIER,
                loc = Location(cursor.pointer)
            )),
            cursor = Cursor(
                pointer = movingPointer,
                loc = Location(col = movingPointer)
            )
        )
    }
}
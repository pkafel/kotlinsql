package com.piotrkafel.kotlinsql

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals


class LexerTest {

    private val cursor = Cursor(0u, Location(0u))

    @ParameterizedTest
    @MethodSource("sqlTestData")
    fun testSqlLexer(input: String, expectedResult: LexerResult) {
        val result = SqlLexer().lex(input)

        assertEquals(expectedResult, result)
    }

    @ParameterizedTest
    @MethodSource("keywordsTestData")
    fun testBasicKeywords(input: String, expectedResult: LexerResult) {
        val result = KeywordLexer().lex(input, cursor)

        assertEquals(expectedResult, result)
        assertEquals(Cursor(0u, Location(0u)), cursor)
    }

    @ParameterizedTest
    @MethodSource("stringLiteralsTestData")
    fun testStringLiterals(input: String, expectedResult: LexerResult) {
        val result = StringLexer().lex(input, cursor)

        assertEquals(expectedResult, result)
        assertEquals(Cursor(0u, Location(0u)), cursor)
    }

    @ParameterizedTest
    @MethodSource("symbolsTestData")
    fun testSymbols(input: String, expectedResult: LexerResult) {
        val result = SymbolLexer().lex(input, cursor)

        assertEquals(expectedResult, result)
        assertEquals(Cursor(0u, Location(0u)), cursor)
    }

    @ParameterizedTest
    @MethodSource("identifiersTestData")
    fun testIdentifiers(input: String, expectedResult: LexerResult) {
        val result = IdentifierLexer().lex(input, cursor)

        assertEquals(expectedResult, result)
        assertEquals(Cursor(0u, Location(0u)), cursor)
    }


    @ParameterizedTest
    @MethodSource("numericsTestData")
    fun testNumericLiterals(input: String, expectedResult: LexerResult) {
        val result = NumericLexer().lex(input, cursor)

        assertEquals(expectedResult, result)
        assertEquals(Cursor(0u, Location(0u)), cursor)
    }

    companion object {

        @JvmStatic
        fun sqlTestData() = listOf(
            arrayOf(
                "select * from myTable", LexerResult.Success(
                    tokens = listOf(
                        Token(
                            value = "select",
                            kind = TokenKind.KEYWORD,
                            loc = Location(col = 0u)
                        ),
                        Token(
                            value = "*",
                            kind = TokenKind.SYMBOL,
                            loc = Location(col = 7u)
                        ),
                        Token(
                            value = "from",
                            kind = TokenKind.KEYWORD,
                            loc = Location(col = 9u)
                        ),
                        Token(
                            value = "myTable",
                            kind = TokenKind.IDENTIFIER,
                            loc = Location(col = 14u)
                        )
                    ),
                    cursor = Cursor(pointer = 21u, loc = Location(21u))
                ),
                arrayOf(
                    "INSERT into myTable VaLuEs ('what a value')", LexerResult.Success(
                        tokens = listOf(
                            Token(
                                value = "insert",
                                kind = TokenKind.KEYWORD,
                                loc = Location(col = 0u)
                            ),
                            Token(
                                value = "into",
                                kind = TokenKind.KEYWORD,
                                loc = Location(col = 7u)
                            ),
                            Token(
                                value = "myTable",
                                kind = TokenKind.IDENTIFIER,
                                loc = Location(col = 12u)
                            ),
                            Token(
                                value = "values",
                                kind = TokenKind.KEYWORD,
                                loc = Location(col = 20u)
                            ),
                            Token(
                                value = "(",
                                kind = TokenKind.SYMBOL,
                                loc = Location(col = 27u)
                            ),
                            Token(
                                value = "what a value",
                                kind = TokenKind.STRING,
                                loc = Location(col = 28u)
                            ),
                            Token(
                                value = ")",
                                kind = TokenKind.SYMBOL,
                                loc = Location(col = 42u)
                            )
                        ),
                        cursor = Cursor(pointer = 43u, loc = Location(43u))
                    )
                )
            )
        )

        @JvmStatic
        fun identifiersTestData() = listOf(
            arrayOf(
                "myIdentifier", LexerResult.Success(
                    tokens = listOf(Token(value = "myIdentifier", kind = TokenKind.IDENTIFIER, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 12u,
                        loc = Location(
                            col = 12u
                        )
                    )
                )
            ),
            arrayOf(
                "my1dent1f1er", LexerResult.Success(
                    tokens = listOf(Token(value = "my1dent1f1er", kind = TokenKind.IDENTIFIER, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 12u,
                        loc = Location(
                            col = 12u
                        )
                    )
                )
            ),
            arrayOf(
                "1dent1f1er", LexerResult.Failure(
                    cursor = Cursor(
                        pointer = 0u,
                        loc = Location(
                            col = 0u
                        )
                    )
                )
            ),
            arrayOf(
                "my_identifier", LexerResult.Success(
                    tokens = listOf(Token(value = "my_identifier", kind = TokenKind.IDENTIFIER, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 13u,
                        loc = Location(
                            col = 13u
                        )
                    )
                )
            ),
        )

        @JvmStatic
        fun numericsTestData() = listOf(
            arrayOf(
                "1234", LexerResult.Success(
                    tokens = listOf(Token(value = "1234", kind = TokenKind.NUMERIC, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 4u,
                        loc = Location(
                            col = 4u
                        )
                    )
                )
            ),
            arrayOf(
                "-1234", LexerResult.Success(
                    tokens = listOf(Token(value = "-1234", kind = TokenKind.NUMERIC, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 5u,
                        loc = Location(
                            col = 5u
                        )
                    )
                )
            ),
            arrayOf(
                "1.234", LexerResult.Success(
                    tokens = listOf(Token(value = "1.234", kind = TokenKind.NUMERIC, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 5u,
                        loc = Location(
                            col = 5u
                        )
                    )
                )
            ),
            arrayOf(
                "-1.234", LexerResult.Success(
                    tokens = listOf(Token(value = "-1.234", kind = TokenKind.NUMERIC, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 6u,
                        loc = Location(
                            col = 6u
                        )
                    )
                )
            ),
            arrayOf(
                ".234", LexerResult.Success(
                    tokens = listOf(Token(value = ".234", kind = TokenKind.NUMERIC, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 4u,
                        loc = Location(
                            col = 4u
                        )
                    )
                )
            ),
            arrayOf(
                "-.234", LexerResult.Success(
                    tokens = listOf(Token(value = "-.234", kind = TokenKind.NUMERIC, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 5u,
                        loc = Location(
                            col = 5u
                        )
                    )
                )
            ),
            arrayOf(
                "-.23.4", LexerResult.Failure(
                    cursor = Cursor(
                        pointer = 0u,
                        loc = Location(
                            col = 0u
                        )
                    )
                )
            ),
        )

        @JvmStatic
        fun keywordsTestData() = listOf(
            arrayOf(
                "select", LexerResult.Success(
                    tokens = listOf(Token(value = "select", kind = TokenKind.KEYWORD, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 6u,
                        loc = Location(
                            col = 6u
                        )
                    )
                )
            ),
            arrayOf(
                "FROM", LexerResult.Success(
                    tokens = listOf(Token(value = "from", kind = TokenKind.KEYWORD, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 4u,
                        loc = Location(
                            col = 4u
                        )
                    )
                )
            ),
            arrayOf("Shenanigans", LexerResult.Failure(Cursor(pointer = 0u, loc = Location(col = 0u))))
        )

        @JvmStatic
        fun stringLiteralsTestData() = listOf(
            arrayOf(
                "'hello world'", LexerResult.Success(
                    tokens = listOf(Token(value = "hello world", kind = TokenKind.STRING, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 13u,
                        loc = Location(
                            col = 13u
                        )
                    )
                )
            )
        )

        @JvmStatic
        fun symbolsTestData() = listOf(
            arrayOf(
                "(", LexerResult.Success(
                    tokens = listOf(Token(value = "(", kind = TokenKind.SYMBOL, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 1u,
                        loc = Location(
                            col = 1u
                        )
                    )
                )
            ),
            arrayOf(
                ")", LexerResult.Success(
                    tokens = listOf(Token(value = ")", kind = TokenKind.SYMBOL, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 1u,
                        loc = Location(
                            col = 1u
                        )
                    )
                )
            ),
            arrayOf(
                "*", LexerResult.Success(
                    tokens = listOf(Token(value = "*", kind = TokenKind.SYMBOL, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 1u,
                        loc = Location(
                            col = 1u
                        )
                    )
                )
            ),
            arrayOf(
                ",", LexerResult.Success(
                    tokens = listOf(Token(value = ",", kind = TokenKind.SYMBOL, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 1u,
                        loc = Location(
                            col = 1u
                        )
                    )
                )
            ),
            arrayOf(
                ";", LexerResult.Success(
                    tokens = listOf(Token(value = ";", kind = TokenKind.SYMBOL, loc = Location(col = 0u))),
                    cursor = Cursor(
                        pointer = 1u,
                        loc = Location(
                            col = 1u
                        )
                    )
                )
            ),
            arrayOf(
                " ", LexerResult.Failure(
                    cursor = Cursor(
                        pointer = 1u,
                        loc = Location(
                            col = 1u
                        )
                    )
                )
            ),
            arrayOf(
                "\t", LexerResult.Failure(
                    cursor = Cursor(
                        pointer = 1u,
                        loc = Location(
                            col = 1u
                        )
                    )
                )
            )
        )
    }
}

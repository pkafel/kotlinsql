package com.piotrkafel.kotlinsql

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals


class LexerTest {

    private val cursor = Cursor(0u, Location(0u))

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

//    @Test
//    fun testMixedInput() {
//        val input = "insert into my_table values (1, 'test');"
//        val expectedTokens = listOf(
//            Token("insert", TokenKind.KEYWORD, Location(1u, 1u)),
//            Token("into", TokenKind.KEYWORD, Location(1u, 8u)),
//            Token("my_table", TokenKind.IDENTIFIER, Location(1u, 13u)),
//            Token("values", TokenKind.KEYWORD, Location(1u, 22u)),
//            Token("(", TokenKind.SYMBOL, Location(1u, 29u)),
//            Token("1", TokenKind.NUMERIC, Location(1u, 30u)),
//            Token(",", TokenKind.SYMBOL, Location(1u, 31u)),
//            Token("test", TokenKind.STRING, Location(1u, 33u)),
//            Token(")", TokenKind.SYMBOL, Location(1u, 39u)),
//            Token(";", TokenKind.SYMBOL, Location(1u, 40u))
//        )
//        testLexerInput(input, expectedTokens)
//    }

    companion object {

        @JvmStatic
        fun identifiersTestData() = listOf(
            arrayOf("myIdentifier", LexerResult.Success(
                token = Token(value = "myIdentifier", kind = TokenKind.IDENTIFIER, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 12u,
                    loc = Location(
                        col = 12u
                    )
                )
            )),
            arrayOf("my1dent1f1er", LexerResult.Success(
                token = Token(value = "my1dent1f1er", kind = TokenKind.IDENTIFIER, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 12u,
                    loc = Location(
                        col = 12u
                    )
                )
            )),
            arrayOf("1dent1f1er", LexerResult.Failure(
                cursor = Cursor(
                    pointer = 0u,
                    loc = Location(
                        col = 0u
                    )
                )
            )),
        )

        @JvmStatic
        fun numericsTestData() = listOf(
            arrayOf(
                "1234", LexerResult.Success(
                    token = Token(value = "1234", kind = TokenKind.NUMERIC, loc = Location(col = 0u)),
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
                    token = Token(value = "-1234", kind = TokenKind.NUMERIC, loc = Location(col = 0u)),
                    cursor = Cursor(
                        pointer = 5u,
                        loc = Location(
                            col = 5u
                        )
                    )
                )
            ),
            arrayOf("1.234", LexerResult.Success(
                token = Token(value = "1.234", kind = TokenKind.NUMERIC, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 5u,
                    loc = Location(
                        col = 5u
                    )
                )
            )),
            arrayOf("-1.234", LexerResult.Success(
                token = Token(value = "-1.234", kind = TokenKind.NUMERIC, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 6u,
                    loc = Location(
                        col = 6u
                    )
                )
            )),
            arrayOf(".234", LexerResult.Success(
                token = Token(value = ".234", kind = TokenKind.NUMERIC, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 4u,
                    loc = Location(
                        col = 4u
                    )
                )
            )),
            arrayOf("-.234", LexerResult.Success(
                token = Token(value = "-.234", kind = TokenKind.NUMERIC, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 5u,
                    loc = Location(
                        col = 5u
                    )
                )
            )),
            arrayOf("-.23.4", LexerResult.Failure(
                cursor = Cursor(
                    pointer = 0u,
                    loc = Location(
                        col = 0u
                    )
                )
            )),
        )

        @JvmStatic
        fun keywordsTestData() = listOf(
            arrayOf(
                "select", LexerResult.Success(
                    token = Token(value = "select", kind = TokenKind.KEYWORD, loc = Location(col = 0u)),
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
                    token = Token(value = "from", kind = TokenKind.KEYWORD, loc = Location(col = 0u)),
                    cursor = Cursor(
                        pointer = 4u,
                        loc = Location(
                            col = 4u
                        )
                    )
                )
            ),
            arrayOf("Shenanigans", LexerResult.Failure(Cursor(pointer = 0u, loc = Location(col = 0u))))
//            arrayOf("INTROVERT", LexerResult.Failure),   how to handle this?
        )

        @JvmStatic
        fun stringLiteralsTestData() = listOf(
            arrayOf("'hello world'", LexerResult.Success(
                token = Token(value = "hello world", kind = TokenKind.STRING, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 13u,
                    loc = Location(
                        col = 13u
                    )
                )
            ))
        )

        @JvmStatic
        fun symbolsTestData() = listOf(
            arrayOf("(", LexerResult.Success(
                token = Token(value = "(", kind = TokenKind.SYMBOL, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 1u,
                    loc = Location(
                        col = 1u
                    )
                )
            )),
            arrayOf(")", LexerResult.Success(
                token = Token(value = ")", kind = TokenKind.SYMBOL, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 1u,
                    loc = Location(
                        col = 1u
                    )
                )
            )),
            arrayOf("*", LexerResult.Success(
                token = Token(value = "*", kind = TokenKind.SYMBOL, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 1u,
                    loc = Location(
                        col = 1u
                    )
                )
            )),
            arrayOf(",", LexerResult.Success(
                token = Token(value = ",", kind = TokenKind.SYMBOL, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 1u,
                    loc = Location(
                        col = 1u
                    )
                )
            )),
            arrayOf(";", LexerResult.Success(
                token = Token(value = ";", kind = TokenKind.SYMBOL, loc = Location(col = 0u)),
                cursor = Cursor(
                    pointer = 1u,
                    loc = Location(
                        col = 1u
                    )
                )
            )),
            arrayOf(" ", LexerResult.Failure(
                cursor = Cursor(
                    pointer = 1u,
                    loc = Location(
                        col = 1u
                    )
                )
            )),
            arrayOf("\t", LexerResult.Failure(
                cursor = Cursor(
                    pointer = 1u,
                    loc = Location(
                        col = 1u
                    )
                )
            ))
        )
    }
}

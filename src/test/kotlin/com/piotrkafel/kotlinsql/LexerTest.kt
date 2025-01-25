package com.piotrkafel.kotlinsql

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals


class LexerTest {

    private val cursor = Cursor(0u, Location(0u, 0u))

    @ParameterizedTest
    @MethodSource("keywordsTestData")
    fun testBasicKeywords(input: String, expectedResult: LexerResult) {
        val result = KeywordLexer().lex(input, cursor)

        assertEquals(expectedResult, result)
        assertEquals(Cursor(0u, Location(0u, 0u)), cursor)
    }

    @ParameterizedTest
    @MethodSource("stringLiteralsTestData")
    fun testStringLiterals(input: String, expectedResult: LexerResult) {
        val result = StringLexer().lex(input, cursor)

        assertEquals(expectedResult, result)
        assertEquals(Cursor(0u, Location(0u, 0u)), cursor)
    }



//    @Test
//    fun testSymbols() {
//        val input = "(*),;"
//        val expectedTokens = listOf(
//            Token("(", TokenKind.SYMBOL, Location(1u, 1u)),
//            Token("*", TokenKind.SYMBOL, Location(1u, 2u)),
//            Token(")", TokenKind.SYMBOL, Location(1u, 3u)),
//            Token(",", TokenKind.SYMBOL, Location(1u, 4u)),
//            Token(";", TokenKind.SYMBOL, Location(1u, 5u))
//        )
//        testLexerInput(input, expectedTokens)
//    }
//
//    @Test
//    fun testIdentifiers() {
//        val input = "my_table column1 column2"
//        val expectedTokens = listOf(
//            Token("my_table", TokenKind.IDENTIFIER, Location(1u, 1u)),
//            Token("column1", TokenKind.IDENTIFIER, Location(1u, 10u)),
//            Token("column2", TokenKind.IDENTIFIER, Location(1u, 18u))
//        )
//        testLexerInput(input, expectedTokens)
//    }
//
//
//    @Test
//    fun testNumericLiterals() {
//        val input = "123 456.78"
//        val expectedTokens = listOf(
//            Token("123", TokenKind.NUMERIC, Location(1u, 1u)),
//            Token("456.78", TokenKind.NUMERIC, Location(1u, 5u))
//        )
//        testLexerInput(input, expectedTokens)
//    }
//
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
        fun keywordsTestData() = listOf(
            arrayOf(
                "select", LexerResult.Success(
                    token = Token(value = "select", kind = TokenKind.KEYWORD, loc = Location(line = 0u, col = 0u)),
                    cursor = Cursor(
                        pointer = 6u,
                        loc = Location(
                            line = 0u,
                            col = 6u
                        )
                    )
                )
            ),
            arrayOf(
                "FROM", LexerResult.Success(
                    token = Token(value = "from", kind = TokenKind.KEYWORD, loc = Location(line = 0u, col = 0u)),
                    cursor = Cursor(
                        pointer = 4u,
                        loc = Location(
                            line = 0u,
                            col = 4u
                        )
                    )
                )
            ),
            arrayOf("Shenanigans", LexerResult.Failure(Cursor(pointer = 0u, loc = Location(line = 0u, col = 0u))))
//            arrayOf("INTROVERT", LexerResult.Failure),   how to handle this?
        )

        @JvmStatic
        fun stringLiteralsTestData() = listOf(
            arrayOf("'hello world'", LexerResult.Success(
                token = Token(value = "hello world", kind = TokenKind.STRING, loc = Location(line = 0u, col = 0u)),
                cursor = Cursor(
                    pointer = 13u,
                    loc = Location(
                        line = 0u,
                        col = 13u
                    )
                )
            ))
        )
    }
}

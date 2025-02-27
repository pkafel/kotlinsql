package com.piotrkafel.kotlinsql

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class ParserTest {

    private val parser: Parser = SqlParser()
    private val lexer: Lexer = SqlLexer()

    @ParameterizedTest
    @MethodSource("sqlSelectParsingTestData", "sqlInsertParsingTestData", "sqlCreateTableParsingTestData")
    fun testParsingSelectQuery(sql: String, expectedResult: List<Statement>) {
        val lexingResult = lexer.lex(sql)

        if(lexingResult is LexerResult.Success) {
            val result = parser.parse(lexingResult.tokens)
            assertTrue(result.isNotEmpty())
            assertEquals(expectedResult, result)
        } else {
            fail("Lexer could not split into tokens the following sql: $sql")
        }
    }

    companion object {

        @JvmStatic
        fun sqlSelectParsingTestData() = listOf(
            arrayOf(
                "SELECT name FROM users;",
                listOf(
                    Statement.SelectStatement(
                        columns = listOf(Literal.IdentifierLiteral("name")),
                        tableName = "users"
                    )
                )
            ),
            arrayOf(
                "SELECT name, age, country FROM users;",
                listOf(
                    Statement.SelectStatement(
                        columns = listOf(
                            Literal.IdentifierLiteral("name"),
                            Literal.IdentifierLiteral("age"),
                            Literal.IdentifierLiteral("country")
                        ),
                        tableName = "users"
                    )
                )
            ),
            arrayOf(
                "SelECT age frOm users;",
                listOf(
                    Statement.SelectStatement(
                        columns = listOf(
                            Literal.IdentifierLiteral("age"),
                        ),
                        tableName = "users"
                    )
                )
            ),
            arrayOf(
                "SELECT 11, 'just a value' FROM example_table;",
                listOf(
                    Statement.SelectStatement(
                        columns = listOf(
                            Literal.IntLiteral(11),
                            Literal.StringLiteral("just a value")
                        ),
                        tableName = "example_table"
                    )
                )
            ),
        )

        @JvmStatic
        fun sqlInsertParsingTestData() = listOf(
            arrayOf(
                "INSERT INTO users VALUES ('James Dean', 41);",
                listOf(
                    Statement.InsertStatement(
                        tableName = "users",
                        values = listOf(Literal.StringLiteral("James Dean"), Literal.IntLiteral(41))
                    )
                )
            )
        )

        @JvmStatic
        fun sqlCreateTableParsingTestData() = listOf(
            arrayOf(
                "CREATE TABLE users (id INT, name TEXT);",
                listOf(
                    Statement.CreateTableStatement(
                        name = "users",
                        columns = listOf(
                            ColumnDefinition("id", ColumnType.INT),
                            ColumnDefinition("name", type = ColumnType.TEXT)
                        )
                    )
                )
            )
        )
    }
}

# Kotlinsql

A relational database implemented in Kotlin for educational purposes.

This project is inspired by [gosql](https://github.com/eatonphil/gosql), created by [Phil Eaton](https://github.com/eatonphil). Unlike gosql, which aims to reimplement early versions of PostgreSQL, this project simplifies the design to demonstrate how relational databases can be built.

The project is also an answer to author's frustration about lack of JVM based educational content regarding database design.

## Supported Features
As an educational tool, Kotlinsql focuses on implementing fundamental features of an SQL database, including:
* In-Memory Storage: Utilizes in-memory data structures to manage tables, facilitating quick data retrieval and manipulation.
* Basic SQL Query Support: Supports a subset of SQL commands, such as SELECT and INSERT.
* Schema Management: Allows the creation of simple table schemas, including defining columns and their data types.

Please note that Kotlinsql is a work in progress. Many core features of relational database are missing. Some of them will be added in the future.

## Structure

Kotlinsql is structured into several key components that implement core database functionalities:

- **Lexer**: Responsible for tokenizing raw SQL input into meaningful components.
- **Parser**: Converts tokens into an abstract syntax tree (AST), representing the structure of SQL queries.
- **AST (Abstract Syntax Tree)**: A hierarchical representation of SQL queries used by the execution engine.
- **Engine**: Interface and types responsible for processing of parsed queries.
- **InMemoryEngine**: Implements an in-memory storage mechanism to handle tables and query execution without persistent storage.

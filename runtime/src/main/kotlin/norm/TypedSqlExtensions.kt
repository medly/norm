package norm

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Enable Type Safe queries and commands
 *
 */

interface RowMapper<T> {
    fun map(rs: ResultSet): T
}

interface ParamSetter<R> {
    fun map(ps: PreparedStatement, params: R): Unit
}

interface Command<P> {
    val sql: String
    val paramSetter: ParamSetter<P>
}

interface Query<P, R> {
    val sql: String
    val mapper: RowMapper<R>
    val paramSetter: ParamSetter<P>
}

data class CommandResult(val updatedRecordsCount: Int)

fun <R : Any> ResultSet.toList(mapper: RowMapper<R>): List<R> =
        this.use { generateSequence { if (this.next()) mapper.map(this) else null }.toList() }

fun <P, R : Any> Query<P, R>.query(connection: Connection, params: P): List<R> =
        connection
                .prepareStatement(sql)
                .also { paramSetter.map(it, params) }
                .executeQuery()
                .toList(mapper)

fun <P : Any> Command<P>.command(connection: Connection, params: P): CommandResult =
        connection
                .prepareStatement(sql)
                .also { paramSetter.map(it, params) }
                .executeUpdate()
                .let { CommandResult(it) }


package norm

import javax.sql.DataSource

fun <T> DataSource.map(sql: String, vararg params: Any, mapper: (Map<String, Any?>) -> T): List<T> =
        this.query(sql, *params).map(mapper)

fun DataSource.query(sql: String, vararg params: Any) =
        this.connection.use { it.executeQuery(sql, params.toList()).toList() }

fun DataSource.command(sql: String, vararg params: Any) =
        this.connection.use { it.executeCommand(sql, params.toList()) }

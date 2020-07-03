package norm.util

import org.postgresql.ds.PGSimpleDataSource
import java.sql.Connection

/**
 * provides connection to the callback and closes it when done, this way code block does not need to manage connection
 */
fun withPGConnection(url: String, username: String, password: String, fn: (Connection) -> Unit) =
        PGSimpleDataSource().also {
            it.setUrl(url) // url is not a property
            it.user = username
            it.password = password
        }.connection.use(fn)

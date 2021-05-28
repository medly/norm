package norm.transaction

import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Executes queries specified by [block] in the transaction.
 *
 * @param block Lambda block with connection on which transaction queries will be executed.
 * @return The transaction result.
 *
 * Example:
 *
 * ```
 *     val result = dataSource.executeTransaction {
 *         val user = FindUserByIdQuery().query(it, FindUserByIdParams(id))
 *         ReportUserViewedMessageCommand().command(it, ReportUserViewedMessageParams(messageId, user.id))
 *         GetMessageViewCountQuery().query(it, GetMessageViewCountParams(messageId)).first()
 *     }
 *
 *     // Check whether transaction is successful
 *     val isSuccessful = result.isSuccessful
 *
 *     // Retrieve transaction result
 *     val viewCount = result.get() // or `result.getOrNull()` to retrieve safely.
 *
 *     // Retrieve exception (if it's failed)
 *     val error = result.errorOrNull()
 * ```
 */
fun <R> DataSource.executeTransaction(block: (Connection) -> R): TransactionResult<R> {
    return connection.use { connection ->
        connection.autoCommit = false
        try {
            block(connection)
                .also { connection.commit() }
                .let { result -> TransactionResult.Success(result) }
        } catch (exception: SQLException) {
            exception.printStackTrace()
            val rollbackError = runCatching { connection.rollback() }.exceptionOrNull()
            TransactionResult.Error(rollbackError ?: exception)
        }
    }
}

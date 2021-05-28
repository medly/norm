package norm.transaction

/**
 * A result model which will be returned on execution on [transaction].
 */
sealed class TransactionResult<R> {
    data class Success<R>(val data: R) : TransactionResult<R>()
    data class Error<R>(val error: Throwable) : TransactionResult<R>()

    /**
     * Returns `true` if transaction is successful. Else `false`.
     */
    val isSuccessful: Boolean = this is Success

    /**
     * Forcefully tries to return a successful result.
     * If transaction is not successful, it throws [IllegalStateException].
     */
    fun get(): R = runCatching { (this as Success).data }
        .getOrElse { throw IllegalStateException("Transaction is not successful") }

    /**
     * @return a result if transaction is successful otherwise null.
     */
    fun getOrNull(): R? = if (this is Success) data else null

    /**
     * @return an error if transaction is failed otherwise null.
     */
    fun errorOrNull(): Throwable? = if (this is Error) error else null
}

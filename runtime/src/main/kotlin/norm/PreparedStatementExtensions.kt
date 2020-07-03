package norm

import java.sql.PreparedStatement

// TODO - handle prepareStatements Failure as well

fun PreparedStatement.withParams(params: List<Any?> = listOf()): PreparedStatement =
        this.also { self ->
            params.forEachIndexed { index, param -> self.setObject(index + 1, param) }
        }

fun PreparedStatement.withBatches(batchedParams: List<List<Any?>> = listOf()) =
        this.also { ps ->
            batchedParams.forEach { params ->
                ps.withParams(params).addBatch()
            }
        }

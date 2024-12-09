package her.gradle.plugin.doctools.components


import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

import java.sql.PreparedStatement


class PreparedStatementInvocationHandler(private val ps: PreparedStatement, private val batchSize: Int) : InvocationHandler {

    var currentBatchSize = 0L

    override fun invoke(obj: Any, method: Method, args: Array<Any?>?): Any? {

        val result = if (args == null) {
            method.invoke(ps)
        } else {
            method.invoke(ps, *args)
        }

        if (method.name == "addBatch") {

            if (++currentBatchSize >= batchSize) {
                ps.executeBatch()
                ps.clearBatch()
                currentBatchSize = 0L
            }
        }

        return result
    }

    fun flush() {
        if (currentBatchSize > 0) {
            ps.executeBatch()
            ps.clearBatch()
        }
    }

}

package her.gradle.plugin.doctools.components

import java.lang.reflect.Proxy
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet


class Sql(private val connection: Connection) {

    fun <R> query(sql: String, action: (ResultSet) -> R?): R? {
        return connection.createStatement().use {statement->
            statement.executeQuery(sql).use { action(it) }
        } 
    }

    fun <R> rows(sql: String, mapping: (ResultSet) -> R): List<R> {
        return query(sql) {rs->
            val result = mutableListOf<R>()

            while(rs.next()) {
                result.add(mapping(rs))
            }

            result
        }!!
    }

    fun insert(sql: String, returnGeneratedKeys: Boolean = false, action: (PreparedStatement) -> Unit): Long? {
        val preparedStatement = if (returnGeneratedKeys) connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS) else connection.prepareStatement(sql)
        return preparedStatement.use { ps ->
            action(ps)
            ps.executeUpdate()
            if (returnGeneratedKeys) {
                val rs = ps.generatedKeys
                if (rs.next()) {
                    rs.getLong(1)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    fun withBatch(batchSize: Int, sql: String, action: (PreparedStatement) -> Unit) {
        connection.prepareStatement(sql).use { ps ->
            val invocationHandler = PreparedStatementInvocationHandler(ps, batchSize)
            val psProxy = Proxy.newProxyInstance(
                ps.javaClass.classLoader,
                arrayOf(PreparedStatement::class.java),
                invocationHandler
            ) as PreparedStatement

            try {
                action(psProxy)
            } finally {
                invocationHandler.flush()
            }
        }
    }

}


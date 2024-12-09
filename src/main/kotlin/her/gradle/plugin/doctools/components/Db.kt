package her.gradle.plugin.doctools.components

import java.lang.reflect.Proxy

import java.sql.DriverManager
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet


class Db(
    private val driver: String,
    private val url: String,
    private val user: String,
    private val password: String
) {
    companion object {
        const val DEFAULT_NAME = "default"

        val DBS = mutableMapOf<String, Db>()

        fun regist(name: String, driver: String, url: String, user: String, password: String) {
            DBS[name] = Db(driver, url, user, password)
        }

        fun use(name: String): Db = DBS[name]!!

        fun <R> withInstance(action: (Sql) -> R?): R? = use(DEFAULT_NAME).withInstance(action)

        fun <R> withConnection(doInConnection: (Connection) -> R?): R? = use(DEFAULT_NAME).withConnection(doInConnection)

        fun <R> withResultSet(sql: String, doInResultSet: (ResultSet) -> R?): R? = use(DEFAULT_NAME).withResultSet(sql, doInResultSet)

        fun <R> withResult(sql: String, doInResult: (ResultSet) -> R): List<R> = use(DEFAULT_NAME).withResult(sql, doInResult)

        fun withPs(sql: String, batchSize: Int, doInPs: (PreparedStatement) -> Unit) = use(DEFAULT_NAME).withPs(sql, batchSize, doInPs)

    }

    init {
        DBS.values.firstOrNull {it.driver == driver} ?: Class.forName(driver)
    }

    fun <R> withConnection(doInConnection: (Connection) -> R): R? {
        return DriverManager.getConnection(url, user, password).use {connection->
            doInConnection(connection)
        }
    }

    fun <R> withInstance(action: (Sql) -> R?): R? {
        return withConnection {connection ->
            action(Sql(connection))
        }
    }

    fun <R> withResultSet(sql: String, doInResultSet: (ResultSet) -> R): R {
        return DriverManager.getConnection(url, user, password).use {connection->
            connection.createStatement().use {statement->
                statement.executeQuery(sql).use {rs -> doInResultSet(rs)}
            }
        }
    }

    fun <R> withResult(sql: String, doInResult: (ResultSet) -> R): List<R> {

        return withResultSet(sql) {rs->
            val results = mutableListOf<R>()
            
            while(rs.next()) {
                results.add(doInResult(rs))
            }

            results
        }
    }

    fun withPs(sql: String, batchSize: Int, doInPs: (PreparedStatement) -> Unit) {
        withConnection {connection->
            connection.prepareStatement(sql).use {ps->
                val psProxy = Proxy.newProxyInstance(
                    ps.javaClass.classLoader,
                    arrayOf(PreparedStatement::class.java),
                    PreparedStatementInvocationHandler(ps, batchSize)
                ) as PreparedStatement

                doInPs(psProxy)
            }
        }
    }

}

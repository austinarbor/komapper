package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.option.ScriptExecuteOption

interface ScriptExecuteQuery : Query<Unit> {
    fun option(configurator: QueryOptionConfigurator<ScriptExecuteOption>): ScriptExecuteQuery
}

internal data class ScriptExecuteQueryImpl(
    val sql: String,
    val option: ScriptExecuteOption = ScriptExecuteOption()
) :
    ScriptExecuteQuery {
    private val statement = Statement(sql, emptyList(), sql)

    override fun option(configurator: QueryOptionConfigurator<ScriptExecuteOption>): ScriptExecuteQueryImpl {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig) {
        val executor = JdbcExecutor(config, option.asJdbcOption())
        return executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return statement.sql
    }
}

package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityUpdateSingleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : Runner {
    private val support: EntityUpdateRunnerSupport<ENTITY, ID, META> =
        EntityUpdateRunnerSupport(context)

    override fun check(config: DatabaseConfig) = Unit

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val statement = buildStatement(config, entity)
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }

    fun preUpdate(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpdate(config, entity)
    }

    fun postUpdate(entity: ENTITY, count: Long): ENTITY {
        return support.postUpdate(entity, count)
    }
}

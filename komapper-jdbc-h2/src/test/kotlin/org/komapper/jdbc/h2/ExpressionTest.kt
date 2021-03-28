package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.concat
import org.komapper.core.dsl.div
import org.komapper.core.dsl.minus
import org.komapper.core.dsl.plus
import org.komapper.core.dsl.rem

@ExtendWith(Env::class)
class ExpressionTest(private val db: Database) {

    @Test
    fun plus() {
        val a = Address_()
        val result = db.execute {
            SqlQuery.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(1 + a.addressId + 1)
                .also {
                    println(it.toStatement())
                }.first()
        }
        assertEquals(12, result)
    }

    @Test
    fun minus() {
        val a = Address_()
        val result = db.execute {
            SqlQuery.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(50 - a.addressId - 40)
                .also {
                    println(it.toStatement())
                }.first()
        }
        assertEquals(0, result)
    }

    @Test
    fun div() {
        val a = Address_()
        val result = db.execute {
            SqlQuery.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(100 / a.addressId / 2)
                .also {
                    println(it.toStatement())
                }.first()
        }
        assertEquals(5, result)
    }

    @Test
    fun rem() {
        val a = Address_()
        val result = db.execute {
            SqlQuery.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(15 % a.addressId % 2)
                .also {
                    println(it.toStatement())
                }.first()
        }
        assertEquals(1, result)
    }

    @Test
    fun concat() {
        val a = Address_()
        val result = db.execute {
            SqlQuery.from(a)
                .where {
                    a.addressId eq 10
                }
                .select("[" concat a.street concat "]")
                .also {
                    println(it.toStatement())
                }.first()
        }
        assertEquals("[STREET 10]", result)
    }
}

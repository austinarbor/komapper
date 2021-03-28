package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.DatabaseConfig
import org.komapper.core.UniqueConstraintException
import org.komapper.core.config.ClockProvider
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(Env::class)
class EntityInsertQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.metamodel()
        val address = Address(16, "STREET 16", 0)
        db.insert(a, address)
        val address2 = db.find(a) { a.addressId eq 16 }
        assertEquals(address, address2)
    }

    @Test
    fun createdAt_localDateTime() {
        val p = Person.metamodel()
        val person1 = Person(1, "ABC")
        val person2 = db.insert(p, person1)
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.find(p) { p.personId to 1 }
        assertEquals(person2, person3)
    }

    @Test
    fun createdAt_offsetDateTime() {
        val h = Human.metamodel()
        val human1 = Human(1, "ABC")
        val human2 = db.insert(h, human1)
        assertNotNull(human2.createdAt)
        assertNotNull(human2.updatedAt)
        assertEquals(human2.createdAt, human2.updatedAt)
        val human3 = db.find(h) { h.humanId to 1 }
        assertEquals(human2, human3)
    }

    @Test
    fun createdAt_customize() {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Person.metamodel()
        val config = object : DatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = Database(config)
        val person1 = Person(1, "ABC")
        val person2 = myDb.insert(p, person1)
        val person3 = db.find(p) { p.personId to 1 }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        assertEquals(person3, person2)
        assertEquals(LocalDateTime.ofInstant(instant, zoneId), person2.createdAt)
    }

    /*
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    @Test
    fun globalEntityListener() {
        val db = Db(object : DbConfig() {
            override val dataSource = db.config.dataSource
            override val dialect = db.config.dialect
            override val entityMetaResolver = db.config.entityMetaResolver
            override val listener = object : GlobalEntityListener {
                override fun <T : Any> preInsert(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T : Any> postInsert(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }
        })

        val address = Address(16, "STREET 16", 0)
        val address2 = db.insert(address)
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16*",
                0
            ), address2
        )
        val t = template<Address>("select * from address where address_id = 16")
        val address3 = db.select(t).firstOrNull()
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16",
                0
            ), address3
        )
    }

    @Test
    fun entityListener() {
        val db = Db(
            AddressListenerConfig(
                db.config,
                object :
                    EntityListener<Address> {
                    override fun preInsert(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "*${entity.street}")
                    }

                    override fun postInsert(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "${entity.street}*")
                    }
                })
        )

        val address = Address(16, "STREET 16", 0)
        val address2 = db.insert(address)
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16*",
                0
            ), address2
        )
        val t = template<Address>("select * from address where address_id = 16")
        val address3 = db.select(t).firstOrNull()
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16",
                0
            ), address3
        )
    }

    */

    @Test
    fun uniqueConstraintException() {
        val a = Address.metamodel()
        val address = Address(1, "STREET 1", 0)
        assertThrows<UniqueConstraintException> {
            db.insert(a, address)
        }
    }

    @Test
    fun identityGenerator() {
        for (i in 1..201) {
            val m = IdentityStrategy.metamodel()
            val strategy = IdentityStrategy(0, "test")
            val newStrategy = db.insert(m, strategy)
            assertEquals(i, newStrategy.id)
        }
    }

    @Test
    fun sequenceGenerator() {
        for (i in 1..201) {
            val m = SequenceStrategy.metamodel()
            val strategy = SequenceStrategy(0, "test")
            val newStrategy = db.insert(m, strategy)
            assertEquals(i, newStrategy.id)
        }
    }
}

package integration.r2dbc

import integration.core.Address
import integration.core.address
import integration.core.employee
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.int
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.core.TransactionAttribute
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(R2dbcEnv::class)
class R2dbcFlowTest(val db: R2dbcDatabase) {
    @Test
    fun singleEntity(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2) }.orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it.addressId })
    }

    @Test
    fun singleEntity_union(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId eq 1 }.union(
                QueryDsl.from(a).where { a.addressId eq 2 },
            ).orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it.addressId })
    }

    @Test
    fun singleColumn(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun singleNotNullColumn(info: TestInfo) = inTransaction(db, info) {
        val flow: Flow<Int> = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun singleColumn_union(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId).union(
                    QueryDsl.from(Meta.address)
                        .where { a.addressId eq 2 }
                        .select(a.addressId),
                ).orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun pairColumns(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street)
        }
        assertEquals(
            listOf(
                1 to "STREET 1",
                2 to "STREET 2",
            ),
            flow.toList(),
        )
    }

    @Test
    fun pairNotNullColumns(info: TestInfo) = inTransaction(db, info) {
        val flow: Flow<Pair<Int, String>> = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId, a.street)
        }
        assertEquals(
            listOf(
                1 to "STREET 1",
                2 to "STREET 2",
            ),
            flow.toList(),
        )
    }

    @Test
    fun pairColumns_union(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId, a.street).union(
                    QueryDsl.from(Meta.address)
                        .where { a.addressId eq 2 }
                        .select(a.addressId, a.street),
                ).orderBy(a.addressId)
        }
        assertEquals(
            listOf(
                1 to "STREET 1",
                2 to "STREET 2",
            ),
            flow.toList(),
        )
    }

    @Test
    fun tripleColumns(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1),
            ),
            flow.toList(),
        )
    }

    @Test
    fun tripleNotNullColumns(info: TestInfo) = inTransaction(db, info) {
        val flow: Flow<Triple<Int, String, Int>> = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1),
            ),
            flow.toList(),
        )
    }

    @Test
    fun tripleColumns_union(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId, a.street, a.version).union(
                    QueryDsl.from(a)
                        .where { a.addressId eq 2 }
                        .select(a.addressId, a.street, a.version),
                ).orderBy(a.addressId)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1),
            ),
            flow.toList(),
        )
    }

    @Test
    fun multipleColumns(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val flow = db.flowQuery {
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version, a.addressId)
        }
        val list = flow.toList()
        assertEquals(2, list.size)
        assertEquals(1, list[0][a.addressId])
        assertEquals(2, list[1][a.addressId])
    }

    @Test
    fun multipleColumns_union(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val flow = db.flowQuery {
            QueryDsl.from(e)
                .where { e.employeeId eq 1 }
                .select(e.employeeId, e.employeeNo, e.employeeName, e.salary).union(
                    QueryDsl.from(e)
                        .where { e.employeeId eq 2 }
                        .select(e.employeeId, e.employeeNo, e.employeeName, e.salary),
                ).orderBy(e.employeeId)
        }
        val list = flow.toList()
        assertEquals(2, list.size)
        assertEquals(1, list[0][e.employeeId])
        assertEquals(2, list[1][e.employeeId])
    }

    @Test
    fun template(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            QueryDsl.fromTemplate("select address_id from address order by address_id")
                .select { it.int("address_id") }
        }
        assertEquals((1..15).toList(), flow.toList())
    }

    @Test
    fun flowTransaction(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }.first()
        val flow: Flow<Address> = db.flowTransaction {
            val address = db.runQuery(query)
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
            val addressFlow = db.flowQuery { QueryDsl.from(a).orderBy(a.addressId) }
            emitAll(addressFlow)
        }
        val list = flow.toList()
        assertEquals(15, list.size)
        assertEquals(Address(15, "TOKYO", 2), list.last())
        val address = db.runQuery(query)
        assertEquals(Address(15, "TOKYO", 2), address)
    }

    @Test
    fun flowTransaction_setRollbackOnly(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }.first()
        val flow: Flow<Address> = db.flowTransaction(TransactionAttribute.REQUIRES_NEW) { tx ->
            val address = db.runQuery(query)
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
            tx.setRollbackOnly()
            val addressFlow = db.flowQuery { QueryDsl.from(a).orderBy(a.addressId) }
            emitAll(addressFlow)
        }
        val list = flow.toList()
        assertEquals(15, list.size)
        assertEquals(Address(15, "TOKYO", 2), list.last())
        val address = db.runQuery(query)
        assertEquals(Address(15, "STREET 15", 1), address)
    }
}

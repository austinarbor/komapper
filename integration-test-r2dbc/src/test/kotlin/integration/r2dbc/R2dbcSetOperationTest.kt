package integration.r2dbc

import integration.core.Dbms
import integration.core.Run
import integration.core.address
import integration.core.department
import integration.core.employee
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.alias
import org.komapper.core.dsl.operator.desc
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(R2dbcEnv::class)
class R2dbcSetOperationTest(private val db: R2dbcDatabase) {
    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5])
    @Test
    fun except_entity(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val q1 = QueryDsl.from(e).where { e.employeeId inList listOf(1, 2, 3, 4, 5) }
        val q2 = QueryDsl.from(e).where { e.employeeId inList listOf(2, 4, 6, 8) }
        val query = (q1 except q2).orderBy(e.employeeId)
        val list = db.runQuery { query }
        assertEquals(3, list.size)
        val e1 = list[0]
        val e2 = list[1]
        val e3 = list[2]
        assertEquals(1, e1.employeeId)
        assertEquals(3, e2.employeeId)
        assertEquals(5, e3.employeeId)
    }

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5])
    @Test
    fun intersect_entity(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val q1 = QueryDsl.from(e).where { e.employeeId inList listOf(1, 2, 3, 4, 5) }
        val q2 = QueryDsl.from(e).where { e.employeeId inList listOf(2, 4, 6, 8) }
        val query = (q1 intersect q2).orderBy(e.employeeId)
        val list = db.runQuery { query }
        assertEquals(2, list.size)
        val e1 = list[0]
        val e2 = list[1]
        assertEquals(2, e1.employeeId)
        assertEquals(4, e2.employeeId)
    }

    @Test
    fun union_entity(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val q1 = QueryDsl.from(e).where { e.employeeId eq 1 }
        val q2 = QueryDsl.from(e).where { e.employeeId eq 1 }
        val q3 = QueryDsl.from(e).where { e.employeeId eq 5 }
        val query = (q1 union q2 union q3).orderBy(e.employeeId.desc())
        val list = db.runQuery { query }
        assertEquals(2, list.size)
        val e1 = list[0]
        val e2 = list[1]
        assertEquals(5, e1.employeeId)
        assertEquals(1, e2.employeeId)
    }

    @Run(unless = [Dbms.MYSQL_5])
    @Test
    fun union_subquery(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val q1 = QueryDsl.from(e).where { e.employeeId eq 1 }.select(e.employeeId)
        val q2 = QueryDsl.from(e).where { e.employeeId eq 6 }.select(e.employeeId)
        val subquery = q1 union q2
        val query = QueryDsl.from(e).where {
            e.managerId inList { subquery }
        }
        val list = db.runQuery { query }
        assertEquals(5, list.size)
    }

    @Test
    fun union_columns(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val a = Meta.address
        val d = Meta.department
        val q1 =
            QueryDsl.from(e).where { e.employeeId eq 1 }
                .select(e.employeeId alias "ID", e.employeeName alias "NAME")
        val q2 = QueryDsl.from(a).where { a.addressId eq 2 }
            .select(a.addressId alias "ID", a.street alias "NAME")
        val q3 = QueryDsl.from(d).where { d.departmentId eq 3 }
            .select(d.departmentId alias "ID", d.departmentName alias "NAME")
        val query = (q1 union q2 union q3).orderBy("ID", desc("NAME"))
        val list = db.runQuery { query }
        assertEquals(3, list.size)
        assertEquals(1 to "SMITH", list[0])
        assertEquals(2 to "STREET 2", list[1])
        assertEquals(3 to "SALES", list[2])
    }

    @Test
    fun unionAll_entity(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val q1 = QueryDsl.from(e).where { e.employeeId eq 1 }
        val q2 = QueryDsl.from(e).where { e.employeeId eq 1 }
        val q3 = QueryDsl.from(e).where { e.employeeId eq 5 }
        val query = (q1 unionAll q2 unionAll q3).orderBy(e.employeeId.desc())
        val list = db.runQuery { query }
        assertEquals(3, list.size)
        val e1 = list[0]
        val e2 = list[1]
        val e3 = list[2]
        assertEquals(5, e1.employeeId)
        assertEquals(1, e2.employeeId)
        assertEquals(1, e3.employeeId)
    }

    @Test
    fun missingWhereClause_top_level_allowEmptyWhereClause_option_is_ignored(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val q1 = QueryDsl.from(e).where { e.employeeId eq 1 }
        val q2 = QueryDsl.from(e)
        val query = (q1 union q2).options { it.copy(allowMissingWhereClause = false) }
        db.runQuery { query }
    }

    @Test
    fun missingWhereClause(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val q1 = QueryDsl.from(e).where { e.employeeId eq 1 }
        val q2 = QueryDsl.from(e).options { it.copy(allowMissingWhereClause = false) }
        val query = (q1 union q2)
        val ex = assertFailsWith<IllegalStateException> {
            db.runQuery { query }.let { }
        }
        println(ex)
    }
}

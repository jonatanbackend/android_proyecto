package com.example.myapplication.db

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.SQLException
import java.sql.Date
import java.sql.Time

data class Empresa(val id: Int, val nombre: String, val fechaInicioContrato: Date?)
data class OrdenInfo(
    val idOrden: String,
    val cedula: String,
    val estado: String,
    val empresa: String
)

class LegalizacionDatabaseHelper {

    fun ejecutarFuncionLegalizacion(
        pid: Int,
        pfecha: Date,
        ptecnico: String,
        pplaca: String,
        porden: String,
        paviso: String,
        phora_inicial: Time,
        phora_final: Time,
        psublinea: Int,
        pobservacion: String,
        pacta: String,
        pusuario: String,
        pempresa: String,
        ptarifa: Int,
        pnic: String,
        pnis: String,
        pnombre: String,
        pdireccion: String,
        pbarrio: String,
        pmunicipio: Int,
        psector: Int,
        ptrafo: String,
        pnodo: String,
        pcircuito: String,
        pestrato: Int,
        ptorden: Int,
        ptacta: Int,
        pefectiva: Int,
        pzona: Int,
        pid_orden: Int,
        pcausal: Int,
        pnombreatiende: String,
        pdoc_atiende: String
    ): String? {
        val sql = "{ ? = call fx_pr_legalizacion_disico(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }"

        val connection: Connection = PostgresHelper.connect()
            ?: throw SQLException("No se pudo establecer conexión con la base de datos.")
        val statement: CallableStatement = connection.prepareCall(sql)

        try {
            statement.registerOutParameter(1, java.sql.Types.VARCHAR)
            statement.setInt(2, pid)
            statement.setDate(3, pfecha)
            statement.setString(4, ptecnico)
            statement.setString(5, pplaca)
            statement.setString(6, porden)
            statement.setString(7, paviso)
            statement.setTime(8, phora_inicial)
            statement.setTime(9, phora_final)
            statement.setInt(10, psublinea)
            statement.setString(11, pobservacion)
            statement.setString(12, pacta)
            statement.setString(13, pusuario)
            statement.setString(14, pempresa)
            statement.setInt(15, ptarifa)
            statement.setString(16, pnic)
            statement.setString(17, pnis)
            statement.setString(18, pnombre)
            statement.setString(19, pdireccion)
            statement.setString(20, pbarrio)
            statement.setInt(21, pmunicipio)
            statement.setInt(22, psector)
            statement.setString(23, ptrafo)
            statement.setString(24, pnodo)
            statement.setString(25, pcircuito)
            statement.setInt(26, pestrato)
            statement.setInt(27, ptorden)
            statement.setInt(28, ptacta)
            statement.setInt(29, pefectiva)
            statement.setInt(30, pzona)
            statement.setInt(31, pid_orden)
            statement.setInt(32, pcausal)
            statement.setString(33, pnombreatiende)
            statement.setString(34, pdoc_atiende)

            statement.execute()
            return statement.getString(1)
        } finally {
            statement.close()
            connection.close()
        }
    }

    fun fxPrLegalizacionSello(
        pid: Int,
        pidlegaliza: Int,
        pestado: Int,
        pubicacion: Int,
        pidmaterial: Int,
        pmarca: String,
        pserie: String,
        pusuario: String,
        pempresa: String
    ): String? {
        val sql = "{ ? = call fx_pr_legalizacion_sello(?, ?, ?, ?, ?, ?, ?, ?, ?) }"
        val connection: Connection = PostgresHelper.connect()
            ?: throw SQLException("No se pudo establecer conexión con la base de datos.")
        val statement: CallableStatement = connection.prepareCall(sql)
        try {
            statement.registerOutParameter(1, java.sql.Types.VARCHAR)
            statement.setInt(2, pid)
            statement.setInt(3, pidlegaliza)
            statement.setInt(4, pestado)
            statement.setInt(5, pubicacion)
            statement.setInt(6, pidmaterial)
            statement.setString(7, pmarca)
            statement.setString(8, pserie)
            statement.setString(9, pusuario)
            statement.setString(10, pempresa)
            statement.execute()
            return statement.getString(1)
        } finally {
            statement.close()
            connection.close()
        }
    }

    fun obtenerDatosLegalizacionSello(): Map<String, String>? {
        val connection = PostgresHelper.connect()
        connection?.let {
            try {
                val callableStatement = it.prepareCall("{ call geros_fx_pr_legalizacion_sello() }")
                val resultSet = callableStatement.executeQuery()

                if (resultSet.next()) {
                    return mapOf(
                        "numeroActa" to resultSet.getString("numero_acta"),
                        "numeroOrden" to resultSet.getString("numero_orden"),
                        "tecnico" to resultSet.getString("tecnico"),
                        "estado" to resultSet.getString("estado"),
                        "motivoRetiro" to resultSet.getString("motivo_retiro"),
                        "clase" to resultSet.getString("clase"),
                        "medidor" to resultSet.getString("medidor"),
                        "marca" to resultSet.getString("marca"),
                        "serie" to resultSet.getString("serie")
                    )
                }
            } catch (e: SQLException) {
                println("Error al ejecutar la función: ${e.message}")
            } finally {
                connection.close()
            }
        }
        return null
    }

    fun obtenerEmpresasConFechaInicio(): List<Triple<Int, String, Date?>> {
        val empresas = mutableListOf<Triple<Int, String, Date?>>()
        val connection = PostgresHelper.connect() ?: return empresas
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT em_idempresa, em_nombreempresa, em_fecha_inicio FROM geros_empresa")
        while (resultSet.next()) {
            empresas.add(
                Triple(
                    resultSet.getInt("em_idempresa"),
                    resultSet.getString("em_nombreempresa"),
                    resultSet.getDate("em_fecha_inicio")
                )
            )
        }
        resultSet.close()
        statement.close()
        connection.close()
        return empresas
    }

    fun obtenerEmpresasPorCedula(cedula: String): List<Triple<Int, String, Date?>> {
        val empresas = mutableListOf<Triple<Int, String, Date?>>()
        val connection = PostgresHelper.connect() ?: return empresas
        val statement = connection.prepareStatement(
            """
            SELECT e.em_idempresa, e.em_nombreempresa, e.em_fecha_inicio
            FROM geros_empresa e
            JOIN geros_usuarios u ON u.empresa = e.em_idempresa
            WHERE u.cedula = ?
            """.trimIndent()
        )
        statement.setString(1, cedula)
        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
            empresas.add(
                Triple(
                    resultSet.getInt("em_idempresa"),
                    resultSet.getString("em_nombreempresa"),
                    resultSet.getDate("em_fecha_inicio")
                )
            )
        }
        resultSet.close()
        statement.close()
        connection.close()
        return empresas
    }

    fun debugEmpresasPorCedula(cedula: String): List<Triple<Int, String, Date?>> {
        val empresas = mutableListOf<Triple<Int, String, Date?>>()
        val connection = PostgresHelper.connect() ?: return empresas
        val statement = connection.prepareStatement(
            """
            SELECT e.em_idempresa, e.em_nombreempresa, e.em_fecha_inicio
            FROM geros_empresa e
            JOIN geros_usuarios u ON u.empresa = e.em_idempresa
            WHERE u.cedula = ?
            """.trimIndent()
        )
        statement.setString(1, cedula)
        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
            val id = resultSet.getInt("em_idempresa")
            val nombre = resultSet.getString("em_nombreempresa")
            val fecha = resultSet.getDate("em_fecha_inicio")
            println("Empresa encontrada: id=$id, nombre=$nombre, fecha_inicio=$fecha")
            empresas.add(Triple(id, nombre, fecha))
        }
        resultSet.close()
        statement.close()
        connection.close()
        return empresas
    }

    fun obtenerOrdenesAsignadas(sessionUser: String, sessionEmpresa: String): List<String> {
        val ordenes = mutableListOf<String>()
        val connection = PostgresHelper.connect() ?: return ordenes
        val sql = """
            SELECT x.id AS qid, x.id_orden as qidorden, x.id_orden||' -//- '||x.estado AS qorden
            FROM pr_programacion_disico_open x
            INNER JOIN pr_asignacion_disico y ON y.id_orden::int = x.id
            LEFT JOIN geros_usuarios u ON u.login = ? AND u.cedula = y.cuadrilla
            LEFT JOIN pr_cuadrillas_tecnicos cu ON cu.pri_empresa = x.empresa AND cu.pri_tecnico = u.cedula
            LEFT JOIN pr_cuadrillas cua ON cua.pr_empresa = x.empresa AND cu.pri_id_cuadrilla = cua.pr_id
            WHERE x.estado = 'ASIGNADO'  AND x.empresa = ?
            ORDER BY 2
        """.trimIndent()
        val statement = connection.prepareStatement(sql)
        statement.setString(1, sessionUser)
        statement.setString(2, sessionEmpresa)
        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
            ordenes.add(resultSet.getString("qorden"))
        }
        resultSet.close()
        statement.close()
        connection.close()
        return ordenes
    }

    fun obtenerEmpresaYOrdenesPorCedula(cedula: String): Pair<Empresa, List<String>>? {
        val connection = PostgresHelper.connect() ?: return null
        try {
            val empresaStmt = connection.prepareStatement(
                "SELECT e.em_idempresa, e.em_nombreempresa, e.em_fecha_inicio " +
                "FROM geros_empresa e " +
                "JOIN geros_usuarios u ON u.empresa = e.em_idempresa " +
                "WHERE u.cedula = ? LIMIT 1"
            )
            empresaStmt.setString(1, cedula)
            val empresaRs = empresaStmt.executeQuery()
            if (!empresaRs.next()) {
                empresaRs.close()
                empresaStmt.close()
                return null
            }
            val empresa = Empresa(
                empresaRs.getInt("em_idempresa"),
                empresaRs.getString("em_nombreempresa"),
                empresaRs.getDate("em_fecha_inicio")
            )
            empresaRs.close()
            empresaStmt.close()

            val ordenes = mutableListOf<String>()
            val ordenStmt = connection.prepareStatement(
                "SELECT x.id_orden||' -//- '||x.estado AS qorden " +
                "FROM pr_programacion_disico_open x " +
                "INNER JOIN pr_asignacion_disico y ON y.id_orden::int = x.id " +
                "INNER JOIN pr_cuadrillas_tecnicos ct ON ct.pri_id_cuadrilla = y.cuadrilla::bigint " +
                "WHERE x.estado = 'ASIGNADO' AND x.empresa = ? AND ct.pri_tecnico = ?"
            )
            ordenStmt.setString(1, empresa.id.toString())
            ordenStmt.setString(2, cedula)

            val ordenRs = ordenStmt.executeQuery()
            while (ordenRs.next()) {
                ordenes.add(ordenRs.getString("qorden"))
            }
            ordenRs.close()
            ordenStmt.close()

            return Pair(empresa, ordenes)
        } finally {
            connection.close()
        }
    }

    fun obtenerOrdenesPorCedula(cedula: String): List<OrdenInfo> {
        val ordenes = mutableListOf<OrdenInfo>()
        val connection = PostgresHelper.connect() ?: return ordenes
        val statement = connection.prepareStatement(
            """
            SELECT TRIM(id_orden) AS id_orden, TRIM(cedula) AS cedula, estado, empresa
            FROM pr_programacion_disico_open
            WHERE cedula = ? AND id_orden IS NOT NULL AND TRIM(id_orden) <> ''
            ORDER BY id_orden
            """.trimIndent()
        )
        statement.setString(1, cedula)
        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
            ordenes.add(
                OrdenInfo(
                    resultSet.getString("id_orden"),
                    resultSet.getString("cedula"),
                    resultSet.getString("estado"),
                    resultSet.getString("empresa")
                )
            )
        }
        resultSet.close()
        statement.close()
        connection.close()
        return ordenes
    }

    fun obtenerOrdenesPorLogin(login: String): List<OrdenInfo> {
        val ordenes = mutableListOf<OrdenInfo>()
        val connection = PostgresHelper.connect() ?: return ordenes

        val cedulaStmt = connection.prepareStatement(
            "SELECT cedula FROM geros_usuarios WHERE login = ? LIMIT 1"
        )
        cedulaStmt.setString(1, login)
        val cedulaRs = cedulaStmt.executeQuery()
        if (!cedulaRs.next()) {
            cedulaRs.close()
            cedulaStmt.close()
            connection.close()
            return ordenes
        }
        val cedula = cedulaRs.getString("cedula")
        cedulaRs.close()
        cedulaStmt.close()

        val ordenStmt = connection.prepareStatement(
            """
            SELECT TRIM(id_orden) AS id_orden, TRIM(cedula) AS cedula, estado, empresa
            FROM pr_programacion_disico_open
            WHERE cedula = ? AND id_orden IS NOT NULL AND TRIM(id_orden) <> ''
            ORDER BY id_orden
            """.trimIndent()
        )
        ordenStmt.setString(1, cedula)
        val ordenRs = ordenStmt.executeQuery()
        while (ordenRs.next()) {
            ordenes.add(
                OrdenInfo(
                    ordenRs.getString("id_orden"),
                    ordenRs.getString("cedula"),
                    ordenRs.getString("estado"),
                    ordenRs.getString("empresa")
                )
            )
        }
        ordenRs.close()
        ordenStmt.close()
        connection.close()
        return ordenes
    }

    fun obtenerOrdenesQuemadas(): List<OrdenInfo> {
        val ordenes = mutableListOf<OrdenInfo>()
        val connection = PostgresHelper.connect() ?: return ordenes
        val statement = connection.prepareStatement(
            """
            SELECT id_orden, cedula, estado, empresa
            FROM pr_programacion_disico_open
            WHERE cedula = '1193265663'
            ORDER BY id_orden
            """.trimIndent()
        )
        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
            ordenes.add(
                OrdenInfo(
                    resultSet.getString("id_orden"),
                    resultSet.getString("cedula"),
                    resultSet.getString("estado"),
                    resultSet.getString("empresa")
                )
            )
        }
        resultSet.close()
        statement.close()
        connection.close()
        return ordenes
    }

    fun obtenerIdLegalizacion(numeroOrden: String, numeroActa: String): Long? {
        val connection = PostgresHelper.connect() ?: return null
        val sql = "SELECT l_id FROM pr_legalizacion WHERE l_orden = ? AND l_acta = ? LIMIT 1"
        val statement = connection.prepareStatement(sql)
        statement.setString(1, numeroOrden)
        statement.setString(2, numeroActa)
        val resultSet = statement.executeQuery()
        val id = if (resultSet.next()) resultSet.getLong("l_id") else null
        resultSet.close()
        statement.close()
        connection.close()
        return id
    }

    fun obtenerProductos(): List<Pair<Long, String>> {
        val productos = mutableListOf<Pair<Long, String>>()
        val connection = PostgresHelper.connect() ?: return productos
        val statement = connection.prepareStatement("SELECT prid, prdescripcion FROM al_tcproducto ORDER BY prdescripcion")
        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
            productos.add(
                Pair(
                    resultSet.getLong("prid"),
                    resultSet.getString("prdescripcion") ?: ""
                )
            )
        }
        resultSet.close()
        statement.close()
        connection.close()
        return productos
    }

    fun existeLegalizacion(empresaId: Int, numeroActa: String): Boolean {
        val connection = PostgresHelper.connect() ?: return false
        val sql = "SELECT 1 FROM pr_legalizacion WHERE l_empresa = ? AND TRIM(l_acta) = ? LIMIT 1"
        try {
            val statement = connection.prepareStatement(sql)
            statement.setString(1, empresaId.toString())
            statement.setString(2, numeroActa.trim())
            val resultSet = statement.executeQuery()
            val exists = resultSet.next()
            resultSet.close()
            statement.close()
            connection.close()
            return exists
        } catch (e: Exception) {
            e.printStackTrace()
            connection.close()
            return false
        }
    }
}
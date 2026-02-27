package com.example.waterdropapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTablePlantas = """
            CREATE TABLE $TABLE_NAME_PLANTAS (
                planta_id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                dias_max_sin_riego INTEGER NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1,
                imagen_path TEXT
            )
        """.trimIndent()

        val createTableRiegos = """
            CREATE TABLE $TABLE_NAME_RIEGOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                planta_id INTEGER,
                fecha TEXT NOT NULL,
                FOREIGN KEY(planta_id) REFERENCES plantas(planta_id)
            )
        """.trimIndent()

        val createTableGrupos = """
            CREATE TABLE $TABLE_NAME_GRUPOS (
                grupo_id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()

        val createTableGruposMany = """
            CREATE TABLE $TABLE_NAME_GRUPOS_MANY (
                planta_id INTEGER NOT NULL,
                grupo_id INTEGER NOT NULL,
                PRIMARY KEY (planta_id, grupo_id),
                FOREIGN KEY(planta_id) REFERENCES plantas(planta_id),
                FOREIGN KEY(grupo_id) REFERENCES grupos(grupo_id)
            )
        """.trimIndent()

        db.execSQL(createTableGrupos)
        db.execSQL(createTablePlantas)
        db.execSQL(createTableRiegos)
        db.execSQL(createTableGruposMany)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_GRUPOS_MANY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_RIEGOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PLANTAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_GRUPOS")
        onCreate(db)
    }

    // metodos plantas

    fun putPlantas(nombre: String , dias: Int , imagenPath: String?): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("dias_max_sin_riego", dias)
            put("imagen_path", imagenPath)
        }
        return db.insert(TABLE_NAME_PLANTAS, null, values)
    }

    fun eliminarPlantas(planta_id:Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("activo" , 0)
        }
        return db.update(
            TABLE_NAME_PLANTAS,
            values,
            "planta_id = ?",                  // WHERE
            arrayOf(planta_id.toString()) )
    }

    fun actualizarPlantas(planta_id : Int,nombre: String , dias: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("dias_max_sin_riego", dias)
        }
        return db.update(
            TABLE_NAME_PLANTAS,
            values,
            "planta_id = ?",                  // WHERE
            arrayOf(planta_id.toString()))
    }

    fun getPlantas(): List<Pair<Int, String>> {
        val lista = mutableListOf<Pair<Int, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT planta_id, nombre FROM $TABLE_NAME_PLANTAS WHERE activo = 1", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                lista.add(Pair(id, nombre))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    fun obtenerEstadoPlantas(): List<EstadoPlantasDTO> {
        val lista = mutableListOf<EstadoPlantasDTO>()
        val db = readableDatabase

        val query = """
        SELECT 
            p.planta_id,
            p.nombre,
            p.dias_max_sin_riego,
            imagen_path,
            -- Último riego calculado aparte
            (
                SELECT MAX(r.fecha)
                FROM riegos r
                WHERE r.planta_id = p.planta_id
            ) AS ultimo_riego,
        
            -- Grupos calculados aparte
            (
                SELECT GROUP_CONCAT(g.nombre, ', ')
                FROM grupos_plantas gp
                JOIN grupos g ON g.grupo_id = gp.grupo_id
                WHERE gp.planta_id = p.planta_id
            ) AS nombre_grupos
        
        FROM plantas p
        WHERE p.activo = 1
        ORDER BY p.nombre
        """

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("planta_id"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val nombreGrupos = cursor.getString(cursor.getColumnIndexOrThrow("nombre_grupos"))
            val maxDias = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego"))
            val ultimo = cursor.getString(cursor.getColumnIndexOrThrow("ultimo_riego"))
            val imagenPath = cursor.getString(cursor.getColumnIndexOrThrow("imagen_path"))

            val diasSinRegar = calcularDias(ultimo)
            val necesita = diasSinRegar >= maxDias

            lista.add(
                EstadoPlantasDTO(
                    plantaId = id,
                    nombre = nombre,
                    ultimoRiego = ultimo,
                    diasSinRegar = diasSinRegar,
                    necesitaRiego = necesita,
                    nombreGrupos = nombreGrupos,
                    imagen_path = imagenPath
                )
            )
        }

        cursor.close()
        return lista
    }

    fun obtenerEstadoPlantasPorGrupo(grupoId: Int): List<EstadoPlantasDTO> {
        val lista = mutableListOf<EstadoPlantasDTO>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT            
                p.planta_id,
                p.nombre,
                p.dias_max_sin_riego,
                imagen_path,
                -- Último riego
                (
                    SELECT MAX(r.fecha)
                    FROM riegos r
                    WHERE r.planta_id = p.planta_id
                ) AS ultimo_riego,
            
                -- Todos los grupos de la planta
                (
                    SELECT GROUP_CONCAT(g.nombre, ', ')
                    FROM grupos_plantas gp2
                    JOIN grupos g ON g.grupo_id = gp2.grupo_id
                    WHERE gp2.planta_id = p.planta_id
                ) AS nombre_grupos
            
            FROM plantas p
            
            WHERE p.activo = 1
            AND EXISTS (
                SELECT 1
                FROM grupos_plantas gp
                WHERE gp.planta_id = p.planta_id
                AND gp.grupo_id = ?
            )
            
            ORDER BY p.nombre
        """,
            arrayOf(grupoId.toString())
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("planta_id"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val nombreGrupos = cursor.getString(cursor.getColumnIndexOrThrow("nombre_grupos"))
            val maxDias = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego"))
            val ultimo = cursor.getString(cursor.getColumnIndexOrThrow("ultimo_riego"))
            val imagenPath = cursor.getString(cursor.getColumnIndexOrThrow("imagen_path"))
            val diasSinRegar = calcularDias(ultimo)
            val necesita = diasSinRegar >= maxDias

            lista.add(
                EstadoPlantasDTO(
                    plantaId = id,
                    nombre = nombre,
                    ultimoRiego = ultimo,
                    diasSinRegar = diasSinRegar,
                    necesitaRiego = necesita,
                    nombreGrupos = nombreGrupos,
                    imagen_path = imagenPath
                )
            )
        }

        cursor.close()
        return lista
    }

    private fun calcularDias(fecha: String?): Int {
        if (fecha == null) return 0//Int.MAX_VALUE

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaRiego = formatter.parse(fecha) ?: return Int.MAX_VALUE
        val hoy = Date()

        val diff = hoy.time - fechaRiego.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    // metodos riegos

    fun putRiegos(planta_id: Int, fecha:String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("planta_id", planta_id)
            put("fecha", fecha)
        }
        return db.insert(TABLE_NAME_RIEGOS, null, values)
    }

    fun putRiegoPorGrupo(grupoId: Int, fecha: String) {
        val db = writableDatabase
        val plantasIds = obtenerEstadoPlantasPorGrupo(grupoId)

        db.beginTransaction()
        try {
            plantasIds.forEach { planta ->
                val values = ContentValues().apply {
                    put("planta_id", planta.plantaId)
                    put("fecha", fecha)
                }
                db.insert(TABLE_NAME_RIEGOS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }


    // metodos grupos

    fun putGrupos(nombre: String):Long{
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre" , nombre)
        }
        return db.insert(TABLE_NAME_GRUPOS,null,values)
    }

    fun eliminarGrupos(grupoId: Int): Int{
        val db = writableDatabase
        val values = ContentValues().apply {
            put("activo" , 0)
        }
        return db.update(
            TABLE_NAME_GRUPOS,
            values,
            "grupo_id = ?",                  // WHERE
            arrayOf(grupoId.toString()) )
    }

    fun actualizarGrupos(grupoId: Int, nombre:String):Int{
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre" , nombre)
        }
        return db.update(
            TABLE_NAME_GRUPOS,
            values,
            "grupo_id = ?",                  // WHERE
            arrayOf(grupoId.toString()))
    }

    fun getGrupos(): List<Grupos> {
        val lista = mutableListOf<Grupos>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT grupo_id, nombre FROM $TABLE_NAME_GRUPOS WHERE activo = 1", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("grupo_id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                lista.add(
                    Grupos(
                        grupo_id = id,
                        nombre = nombre
                    )

                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }
    fun getEstadosGrupos(): List<EstadoGruposDTO> {
        val lista = mutableListOf<EstadoGruposDTO>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT grupo_id, nombre, count(*) as cantPlantasGrupo  FROM $TABLE_NAME_GRUPOS WHERE activo=1 GROUP BY grupo_id , nombre", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("grupo_id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantPlantasGrupo"))
                lista.add(
                    EstadoGruposDTO(
                        grupoId = id,
                        nombreGrupo = nombre,
                        cantPlantasGrupo = cantidad
                    )

                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }


    // metodos grupos plantas
    fun putGruposPlantas(planta_id: Int, grupo_id:Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("planta_id", planta_id)
            put("grupo_id", grupo_id)
        }
        return db.insert(TABLE_NAME_GRUPOS_MANY, null, values)
    }

    // metricas y consultas particulares

    fun getUltimosRiegos(): List<UltimoRiego> {
        val lista = mutableListOf<UltimoRiego>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT p.nombre, MAX(r.fecha)\n"+
                "            FROM riegos r\n"+
                "            LEFT JOIN plantas p ON p.planta_id = r.planta_id\n"+
                "            WHERE p.activo = 1 GROUP BY p.planta_id", null)


        if (cursor.moveToFirst()) {
            do {
                val ur = UltimoRiego()
                ur.name = cursor.getString(0)
                ur.fecha = cursor.getString(1)
                lista.add(ur)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    fun obtenerHistorialRiegoxPlanta(planta_id: Int): List<RiegoHistorialDTO> {
        val lista = mutableListOf<RiegoHistorialDTO>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT p.nombre AS nombre_planta,
               r.fecha   AS fecha_riego
        FROM riegos r
        INNER JOIN plantas p ON p.planta_id = r.planta_id
        WHERE r.planta_id = ?
        AND p.activo = 1
        ORDER BY r.fecha ASC
        LIMIT 4
        """,
            arrayOf(planta_id.toString())
        )

        var fechaAnterior: Date? = null


        while (cursor.moveToNext()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre_planta"))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha_riego"))

            val fechaActual = parseFecha(fecha)

            val diasDesdeUltimo = fechaAnterior?.let { ((fechaActual.time - it.time ) / (1000 * 60 * 60 * 24)).toInt()}

            lista.add(
                RiegoHistorialDTO(
                    nombrePlanta = nombre,
                    fechaRiego = fecha,
                    diasDesdeUltimo = diasDesdeUltimo
                )
            )
            fechaAnterior = fechaActual
        }

        cursor.close()
        return lista
    }

    private fun parseFecha(fecha: String): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.isLenient = false
        return sdf.parse(fecha)!!
    }


    companion object {
        const val DATABASE_NAME = "plantas.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME_PLANTAS = "plantas"
        const val TABLE_NAME_RIEGOS = "riegos"
        const val TABLE_NAME_GRUPOS = "grupos"
        const val TABLE_NAME_GRUPOS_MANY = "grupos_plantas"
    }
}
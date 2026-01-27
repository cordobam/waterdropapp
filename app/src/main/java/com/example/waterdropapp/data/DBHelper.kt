package com.example.waterdropapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTablePlantas = """
            CREATE TABLE $TABLE_NAME_PLANTAS (
                planta_id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL
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
                nombre TEXT NOT NULL
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

    // metodos platas

    fun putPlantas(nombre: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
        }
        return db.insert(TABLE_NAME_PLANTAS, null, values)
    }

    fun getPlantas(): List<Pair<Int, String>> {
        val lista = mutableListOf<Pair<Int, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT planta_id, nombre FROM $TABLE_NAME_PLANTAS", null)

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


    // metodos riegos

    fun putRiegos(planta_id: Int, fecha:String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("planta_id", planta_id)
            put("fecha", fecha)
        }
        return db.insert(TABLE_NAME_RIEGOS, null, values)
    }

    // metodos grupos

    // metricas y consultas particulares
    fun getUltimosRiegos(): List<UltimoRiego> {
        val lista = mutableListOf<UltimoRiego>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT p.nombre, MAX(r.fecha)\n"+
                "            FROM riegos r\n"+
                "            LEFT JOIN plantas p ON p.planta_id = r.planta_id\n"+
                "            GROUP BY p.planta_id", null)


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
    companion object {
        const val DATABASE_NAME = "plantas.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME_PLANTAS = "plantas"
        const val TABLE_NAME_RIEGOS = "riegos"
        const val TABLE_NAME_GRUPOS = "grupos"
        const val TABLE_NAME_GRUPOS_MANY = "grupos_plantas"
    }
}
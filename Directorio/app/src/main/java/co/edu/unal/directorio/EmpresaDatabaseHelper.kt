package co.edu.unal.directorio

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class EmpresaDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "DirectorioEmpresas.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_EMPRESAS = "Empresas"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_URL = "url"
        private const val COLUMN_TELEFONO = "telefono"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PRODUCTOS = "productosServicios"
        private const val COLUMN_CLASIFICACION = "clasificacion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_EMPRESAS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_URL TEXT,
                $COLUMN_TELEFONO TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PRODUCTOS TEXT,
                $COLUMN_CLASIFICACION TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EMPRESAS")
        onCreate(db)
    }

    // CRUD OPERATIONS

    fun addEmpresa(empresa: Empresa): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, empresa.nombre)
            put(COLUMN_URL, empresa.url)
            put(COLUMN_TELEFONO, empresa.telefono)
            put(COLUMN_EMAIL, empresa.email)
            put(COLUMN_PRODUCTOS, empresa.productosServicios)
            put(COLUMN_CLASIFICACION, empresa.clasificacion)
        }
        return db.insert(TABLE_EMPRESAS, null, values)
    }

    fun getEmpresas(): List<Empresa> {
        val empresas = mutableListOf<Empresa>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EMPRESAS", null)
        with(cursor) {
            while (moveToNext()) {
                val empresa = Empresa(
                    id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
                    nombre = getString(getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    url = getString(getColumnIndexOrThrow(COLUMN_URL)),
                    telefono = getString(getColumnIndexOrThrow(COLUMN_TELEFONO)),
                    email = getString(getColumnIndexOrThrow(COLUMN_EMAIL)),
                    productosServicios = getString(getColumnIndexOrThrow(COLUMN_PRODUCTOS)),
                    clasificacion = getString(getColumnIndexOrThrow(COLUMN_CLASIFICACION))
                )
                empresas.add(empresa)
            }
        }
        cursor.close()
        return empresas
    }

    fun getEmpresaById(id: Long): Empresa? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EMPRESAS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        return if (cursor.moveToFirst()) {
            val empresa = Empresa(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TELEFONO)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                productosServicios = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTOS)),
                clasificacion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASIFICACION))
            )
            cursor.close()
            empresa
        } else {
            cursor.close()
            null
        }
    }

    fun updateEmpresa(empresa: Empresa): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, empresa.nombre)
            put(COLUMN_URL, empresa.url)
            put(COLUMN_TELEFONO, empresa.telefono)
            put(COLUMN_EMAIL, empresa.email)
            put(COLUMN_PRODUCTOS, empresa.productosServicios)
            put(COLUMN_CLASIFICACION, empresa.clasificacion)
        }
        return db.update(TABLE_EMPRESAS, values, "$COLUMN_ID = ?", arrayOf(empresa.id.toString()))
    }

    fun deleteEmpresa(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_EMPRESAS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}

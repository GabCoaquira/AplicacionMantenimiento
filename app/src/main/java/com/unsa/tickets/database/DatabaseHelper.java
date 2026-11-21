package com.unsa.tickets.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tickets_unsa.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLA_FACULTADES = "facultades";
    public static final String TABLA_PROGRAMAS = "programas";
    public static final String TABLA_ESTUDIANTES = "estudiantes";
    public static final String TABLA_TICKETS = "tickets";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_FACULTADES + " (" +
                "codigo TEXT PRIMARY KEY, " +
                "nombre TEXT NOT NULL, " +
                "estado TEXT DEFAULT 'A')");

        db.execSQL("CREATE TABLE " + TABLA_PROGRAMAS + " (" +
                "codigo TEXT PRIMARY KEY, " +
                "nombre TEXT NOT NULL, " +
                "estado TEXT DEFAULT 'A')");

        db.execSQL("CREATE TABLE " + TABLA_ESTUDIANTES + " (" +
                "codigo TEXT PRIMARY KEY, " +
                "nombre TEXT NOT NULL, " +
                "estado TEXT DEFAULT 'A')");

        db.execSQL("CREATE TABLE " + TABLA_TICKETS + " (" +
                "codigo TEXT PRIMARY KEY, " +
                "fecha TEXT NOT NULL, " +
                "cod_estudiante TEXT NOT NULL, " +
                "cod_facultad TEXT NOT NULL, " +
                "cod_programa TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "estado TEXT DEFAULT 'A', " +
                "FOREIGN KEY(cod_estudiante) REFERENCES " + TABLA_ESTUDIANTES + "(codigo), " +
                "FOREIGN KEY(cod_facultad) REFERENCES " + TABLA_FACULTADES + "(codigo), " +
                "FOREIGN KEY(cod_programa) REFERENCES " + TABLA_PROGRAMAS + "(codigo))");

        cargarDatosIniciales(db);
    }

    private void cargarDatosIniciales(SQLiteDatabase db) {
        // Facultades reales de la UNSA (P3)
        cargarFacultades(db);

        // Programas profesionales de prueba
        db.execSQL("INSERT INTO " + TABLA_PROGRAMAS + " VALUES ('071', 'Ingeniería de Sistemas', 'A')");
        db.execSQL("INSERT INTO " + TABLA_PROGRAMAS + " VALUES ('072', 'Ingeniería Electrónica', 'A')");
        db.execSQL("INSERT INTO " + TABLA_PROGRAMAS + " VALUES ('070', 'Ingeniería Industrial', 'A')");

        // Estudiantes de prueba
        db.execSQL("INSERT INTO " + TABLA_ESTUDIANTES + " VALUES ('101', 'Coaquira Suyo, Gabriela Dayana', 'A')");
        db.execSQL("INSERT INTO " + TABLA_ESTUDIANTES + " VALUES ('102', 'Llacma Quispe, Kevin Andree', 'A')");
        db.execSQL("INSERT INTO " + TABLA_ESTUDIANTES + " VALUES ('103', 'Velarde Saldaña, Jhossep Fabritzio', 'A')");
        db.execSQL("INSERT INTO " + TABLA_ESTUDIANTES + " VALUES ('104', 'Zeballos Perez, Juan Sergio', 'A')");

        // Tickets de prueba
        db.execSQL("INSERT INTO " + TABLA_TICKETS + " VALUES ('501', '20261118', '101', '017', '071', 'Entrega de constancia de matrícula', 'A')");
        db.execSQL("INSERT INTO " + TABLA_TICKETS + " VALUES ('502', '20261118', '102', '017', '072', 'Solicitud de certificado de estudios', 'A')");
    }

    /**
     * P3 - Facultades reales de la Universidad Nacional de San Agustín de Arequipa.
     * Códigos de 3 dígitos NO secuenciales. Se respetan 017, 018 y 020 porque
     * los tickets de prueba hacen referencia a ellos.
     */
    private void cargarFacultades(SQLiteDatabase db) {
        String[][] facultades = {
                {"011", "Administración", "A"},
                {"012", "Agronomía", "A"},
                {"013", "Arquitectura y Urbanismo", "A"},
                {"014", "Ciencias Biológicas", "A"},
                {"015", "Ciencias Contables y Financieras", "A"},
                {"016", "Ciencias Histórico Sociales", "A"},
                {"017", "Ingeniería de Producción y Servicios", "A"},
                {"018", "Ciencias de la Educación", "A"},
                {"020", "Ciencias Naturales y Formales", "I"},
                {"021", "Derecho", "A"},
                {"022", "Medicina", "A"},
                {"024", "Economía", "A"},
                {"025", "Enfermería", "A"},
                {"027", "Filosofía y Humanidades", "A"},
                {"030", "Geología, Geofísica y Minas", "A"},
                {"032", "Ingeniería Civil", "A"},
                {"035", "Ingeniería de Procesos", "A"},
                {"038", "Psicología, Relaciones Industriales y Ciencias de la Comunicación", "A"},
        };
        for (String[] f : facultades) {
            db.execSQL("INSERT INTO " + TABLA_FACULTADES + " (codigo, nombre, estado) VALUES (?, ?, ?)", f);
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_TICKETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_ESTUDIANTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_PROGRAMAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_FACULTADES);
        onCreate(db);
    }

    public boolean existeCodigo(String nombreTabla, String codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + nombreTabla + " WHERE codigo = ?", new String[]{codigo});
        boolean existe = cursor.moveToFirst();
        cursor.close();
        return existe;
    }

    public boolean cambiarEstadoRegistro(String nombreTabla, String codigo, String nuevoEstado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("estado", nuevoEstado);
        int filas = db.update(nombreTabla, values, "codigo = ?", new String[]{codigo});
        return filas > 0;
    }
}

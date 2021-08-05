package com.example.guru2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import java.lang.Exception

class DBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int)
    : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private val DATABASE_NAME = "tempDB.db"
        private val DATABASE_VERSION = 1

        val SUBJECT_TABLE_NAME = "Subjects"
        val COLUMN_SUBJECTID = "subjectId"
        val COLUMN_SUBJECTNAME = "subjectName"
        // val COLUMN_SUBJECTTIME = "subjectTime" // 공부 시간
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_SUBJECTS_TABLE: String = ("CREATE TABLE $SUBJECT_TABLE_NAME (" +
                "$COLUMN_SUBJECTID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_SUBJECTNAME TEXT)")
        // "$COLUMN_SUBJECTTIME TEXT)
        db?.execSQL(CREATE_SUBJECTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    // 과목 조회
    fun getSubjects(mCtx: Context): ArrayList<Subject> {
        val qry = "Select * From $SUBJECT_TABLE_NAME"
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(qry, null)
        val subjects = ArrayList<Subject>()

        if (cursor.count == 0)
            Toast.makeText(mCtx, "추가한 과목이 없습니다.", Toast.LENGTH_SHORT).show()
        else {
            while (cursor.moveToNext()) {
                val subject = Subject()
                subject.subjectID = cursor.getInt(cursor.getColumnIndex(COLUMN_SUBJECTID))
                subject.subjectName = cursor.getString(cursor.getColumnIndex(COLUMN_SUBJECTNAME))
                subjects.add(subject)
            }
        }

        cursor.close()
        db.close()
        return subjects
    }

    // 과목 추가
    fun addSubjects(mCtx: Context, subject: Subject) {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUBJECTNAME, subject.subjectName)
        try {
            db.insert(SUBJECT_TABLE_NAME, null, contentValues)
            Toast.makeText(mCtx, "${subject.subjectName} 과목이 추가되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(mCtx, e.message, Toast.LENGTH_SHORT).show()
        }
        db.close()
    }

    // 과목 수정
    fun updateSubject(id: String, subjectName: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        var result = false
        contentValues.put(COLUMN_SUBJECTNAME, subjectName)
        try {
            db.update(SUBJECT_TABLE_NAME, contentValues, "$COLUMN_SUBJECTID = ?", arrayOf(id))
            result = true
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Update Error")
        }
        db.close()
        return result
    }

    // 과목 삭제
    fun deleteSubject(subjectID: Int): Boolean {
        val qry = "Delete From $SUBJECT_TABLE_NAME where $COLUMN_SUBJECTID = $subjectID"
        val db: SQLiteDatabase = this.writableDatabase
        var result = false
        try {
            db.execSQL(qry)
            result = true
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Delete Error")
        }
        db.close()
        return result
    }

}
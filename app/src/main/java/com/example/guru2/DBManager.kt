package com.example.guru2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.strictmode.SqliteObjectLeakedViolation
import android.util.Log
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Text
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class DBManager(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        ///////////////////////////////////////////////////
        db!!.execSQL("CREATE TABLE subjects (subj_name text, subj_studyTimeHr INTEGER, subj_studyTimeMin INTEGER, totalsec INTEGER, subj_studyTimeSec INTEGER, year INTEGER, month INTEGER, day INTEGER)")
        db!!.execSQL("CREATE TABLE calendar (study_Yr INTEGER , study_Mth INTEGER , study_Date INTEGER )")
        //////////////////////////////////////////////////////////////
        db!!.execSQL("CREATE TABLE todoMVA (fk_subj_name text , check_list text PRIMARY KEY)")
        //db!!.execSQL("CREATE TABLE totalTime (name text PRIMARY KEY, total_hour text , total_min text, total_sec text)")
        //db!!.execSQL("CREATE TABLE totalTime (total text PRIMARY KEY)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun totalTime(mCtx: Context, subj_name : String): ArrayList<Int> {
        val totaltime = ArrayList<Int>()
        val db = this.readableDatabase

        var cursor : Cursor
        cursor = db.rawQuery("SELECT totalsec FROM subjects WHERE subj_name = '" + subj_name + "';",null)

        if(cursor.moveToFirst()) {  //데이터 존재 시
            do {
                var subjecttime : Int
                subjecttime = cursor.getInt(cursor.getColumnIndex("totalsec"))
                totaltime.add(subjecttime)
            } while (cursor.moveToNext())
        }
        else {  //데이터 없을 때
            Toast.makeText(mCtx, "오늘의 공부기록은 없습니다.", Toast.LENGTH_SHORT).show()
        }

        cursor.close()
        db.close()
        return totaltime
    }
/*
    fun minTime(mCtx: Context, subj_name : String): ArrayList<Int> {
        val minTime = ArrayList<Int>()
        val db = this.readableDatabase

        var cursor : Cursor
        cursor = db.rawQuery("SELECT subj_studyTimeMin FROM subjects WHERE subj_name = '" + subj_name + "';",null)

        if(cursor.moveToFirst()) {  //데이터 존재 시
            do {
                var subjectMin: Int
                subjectMin = cursor.getInt(cursor.getColumnIndex("subj_studyTimeMin"))
                minTime.add(subjectMin)
            } while (cursor.moveToNext())
        }
        else {  //데이터 없을 때
            Toast.makeText(mCtx, "오늘의 공부기록은 없습니다.", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
        db.close()
        return minTime
    }

    fun secTime(mCtx: Context, subj_name : String): ArrayList<Int> {
        val secTime = ArrayList<Int>()
        val db = this.readableDatabase

        var cursor : Cursor
        cursor = db.rawQuery("SELECT subj_studyTimeSec FROM subjects WHERE subj_name = '" + subj_name + "';",null)

        if(cursor.moveToFirst()) {
            do {
                var subjectSec : Int
                subjectSec = cursor.getInt(cursor.getColumnIndex("subj_studyTimeSec"))
                secTime.add(subjectSec)
            } while (cursor.moveToNext())
        }
        else {  //데이터 없을 때
            Toast.makeText(mCtx, "오늘의 공부기록은 없습니다.", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
        db.close()
        return secTime
    }

 */

    fun getTodos(mCtx : Context, subj_name : String) : ArrayList<TextView> {
        val checkLists = ArrayList<TextView>()
        val db = this.readableDatabase

        var cursor : Cursor
        cursor = db.rawQuery("SELECT check_list FROM todoMVA WHERE fk_subj_name = '" + subj_name + "';",null)
        if (cursor.count == 0)
            Toast.makeText(mCtx, "등록한 일정이 없습니다.", Toast.LENGTH_SHORT).show()
        else {

            while (cursor.moveToNext()) {
                var todos : TextView = TextView(mCtx)
                todos.text = cursor.getString(cursor.getColumnIndex("check_list"))
                checkLists.add(todos)
            }
        }

        cursor.close()
        db.close()
        this.close()
        return checkLists
    }

    fun addTodos(mCtx: Context, subj_name : String ,todos : String) : Boolean{
        var result = false
        val db: SQLiteDatabase = this.writableDatabase
        try {
            db.execSQL("INSERT INTO todoMVA VALUES ('" + subj_name + "','" + todos + "')")
            result = !result
        } catch (e: Exception) {
            Toast.makeText(mCtx, e.message, Toast.LENGTH_SHORT).show()
        }

        db.close()
        this.close()
        return result
    }

    fun deleteTodos(todoText : String) : Boolean{
        var result = false
        val db : SQLiteDatabase = this.writableDatabase
        try{
            db.execSQL("DELETE FROM todoMVA WHERE check_list = '"+todoText+"'")
            result = !result
        } catch (e: Exception){
            Log.e("DELETE ERR", "Delete Error")
        }

        db.close()
        this.close()
        return result
    }

    fun getSelectAllCount(subj_name: String) : Int{
        val db : SQLiteDatabase = this.readableDatabase
        val cursor : Cursor
        cursor = db.rawQuery("SELECT * FROM todoMVA WHERE fk_subj_name = '"+subj_name+"';",null)

        val size = cursor.getCount()
        cursor.close()

        return size
    }

    private var isSaved = true
    fun checkTodaysDate() : Boolean{
        // date 객체
        val date : Dates = Dates()
        date.setTodayDate()
        val db : SQLiteDatabase = this.writableDatabase
        val cursor : Cursor
        cursor =db.rawQuery("SELECT * FROM calendar;",null)
        if(cursor.count == 0){
            db.execSQL("INSERT INTO calendar VALUES(" + date.getYear() + ", " + date.getMonth() + ", " + date.getDay() + ") ")
            isSaved = true
            Log.i("calendar", "입력함")
        }
        else {
            var saved_year = 0
            var saved_month = 0
            var saved_day = 0
            // 커서를 움직이면서 불러온 날짜가 데이터베이스에 저장되었는지 체크
            while (cursor.moveToNext()) {
                saved_year = cursor.getInt(cursor.getColumnIndex("study_Yr"))
                saved_month = cursor.getInt(cursor.getColumnIndex("study_Mth"))
                saved_day = cursor.getInt(cursor.getColumnIndex("study_Date"))
            }
            if (saved_day == date.getDay() && saved_month == date.getMonth() && saved_year == date.getYear()) {
                isSaved = false     // 있다면 false 반환 - 저장할 필요 없다
                Log.i("calendar", "필요가 업따")
            }
            else {
                db.execSQL("UPDATE calendar SET (study_Yr, study_Mth, study_Date) = (" + date.getYear() + ", " + date.getMonth() + ", " + date.getDay() + ") ")
                isSaved = true
                Log.i("calendar", "바꿧따")
            }
        }
        db.close()
        cursor.close()
        return isSaved
    }

}
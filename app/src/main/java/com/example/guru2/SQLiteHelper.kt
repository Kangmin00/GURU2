package com.example.guru2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import java.lang.Exception
import javax.security.auth.Subject

class SQLiteHelper(context:Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "place.db"

        val PLACE_TABLE_NAME = "Places"
        val COLUMN_PLACEID = "placeId"
        val COLUMN_PLACENAME = "placeName"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_PLACES_TABLE: String = ("CREATE TABLE $PLACE_TABLE_NAME (" +
                "$COLUMN_PLACEID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_PLACENAME TEXT)")
        db?.execSQL(CREATE_PLACES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

     //장소 조회
        fun getPlaces(mCtx: Context): ArrayList<Place>{
         val qry = "Select * From $PLACE_TABLE_NAME"
         val db: SQLiteDatabase = this.readableDatabase
         val cursor = db.rawQuery(qry, null)
         val places = ArrayList<Place>()

         if (cursor.count == 0)
             Toast.makeText(mCtx, "추가한 장소가 없습니다.", Toast.LENGTH_SHORT).show()
         else {
             while (cursor.moveToNext()) {
                 val place = Place()
                 place.placeID = cursor.getInt(cursor.getColumnIndex(COLUMN_PLACEID))
                 place.placeName = cursor.getString(cursor.getColumnIndex(COLUMN_PLACENAME))
                 places.add(place)
             }
        }

         cursor.close()
         db.close()
         return places
    }


    //장소 추가
    fun addPlaces(mCtx: Context, place: Place) {
            val db: SQLiteDatabase = this.writableDatabase
            val contentValues = ContentValues()
            contentValues.put(COLUMN_PLACENAME, place.placeName)
            try {
                db.insert(PLACE_TABLE_NAME, null, contentValues)
                Toast.makeText(mCtx, "${place.placeName}  추가되었습니다.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(mCtx, e.message, Toast.LENGTH_SHORT).show()
            }
            db.close()
        }

        //장소 수정
        fun updatePlace(id: String, placeName: String): Boolean {
            val db: SQLiteDatabase = this.writableDatabase
            val contentValues = ContentValues()
            var result = false
            contentValues.put(COLUMN_PLACENAME, placeName)
            try {
                db.update(PLACE_TABLE_NAME, contentValues, "$COLUMN_PLACEID = ?", arrayOf(id))
                result = true
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Update Error")
            }
            db.close()
            return result
        }



        //장소 삭제
        fun deletePlace(placeID: Int): Boolean {
            val qry = "Delete From $PLACE_TABLE_NAME where $COLUMN_PLACEID = $placeID"
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
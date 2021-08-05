package com.example.guru2

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StudyCalendar_layout : AppCompatActivity() {

    lateinit var dbManager : DBManager
    lateinit var sqlitedb : SQLiteDatabase
    lateinit var layout1 : LinearLayout
    val date : Dates = Dates()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_layout)

        // 날짜 설정
        date.setTodayDate()

        //데이터베이스 열기 - 읽어오기
        dbManager = DBManager(this, "studyDB", null, 1)
        sqlitedb = dbManager.readableDatabase

        layout1 = findViewById(R.id.calendar)

        // 과목, 시간 읽어오기
        var cursor : Cursor
        //cursor = sqlitedb.rawQuery("SELECT * FROM subjects WHERE YYMMDD = '"+date.getYear()+""+date.getMonth()+""+date.getYear()+"';",null)
        cursor = sqlitedb.rawQuery("SELECT * FROM subjects WHERE year = " + date.getYear() + " AND month = " + date.getMonth() + " AND day = " + date.getDay() + ";",null)

        var num : Int = 0
        if(cursor.count != 0) {
            while (cursor.moveToNext()) {
                var str_subject = cursor.getString((cursor.getColumnIndex("subj_name")))
                var str_timeHour  = cursor.getInt((cursor.getColumnIndex("subj_studyTimeHr")))
                var str_timeMin  = cursor.getInt((cursor.getColumnIndex("subj_studyTimeMin")))
                var str_timeSec = cursor.getInt((cursor.getColumnIndex("subj_studyTimeSec")))

                var layout_item : LinearLayout = LinearLayout(this)
                layout_item.orientation = LinearLayout.VERTICAL
                layout_item.setPadding(20,30,20,30)
                layout_item.id = num
                layout_item.setTag(str_subject)

                var tvSubject : TextView = TextView(this)
                tvSubject.text = str_subject
                tvSubject.textSize = 15F
                layout_item.addView(tvSubject)

                var tvTime : TextView = TextView(this)
                tvTime.text = timerFormat(str_timeHour) + ":" + timerFormat(str_timeMin) + ":" + timerFormat(str_timeSec)
                tvTime.textSize = 15F
                layout_item.addView(tvTime)

                layout_item.setOnClickListener {
                    val intent = Intent(this, StudyCalendar_button::class.java)
                    intent.putExtra("subj_name", str_subject)
                    startActivity(intent)
                }
                layout1.addView(layout_item)
                num++
            }
        }
        cursor.close()
        sqlitedb.close()
        dbManager.close()
    }

    //타이머 두 자리수 맞춰주는 함수
    private fun timerFormat(time : Int) : String{
        var timeString = ""
        if(time < 10){
            timeString = "0"+ "$time"
        } else {
            timeString = "$time"
        }

        return timeString
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_study_calendar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.action_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_subject -> {
                val intent = Intent(this, StudySubjects::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_map -> {
                val intent = Intent(this, StudyMap::class.java)
                startActivity(intent)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

}
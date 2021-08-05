package com.example.guru2

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StudyCalendar_button: AppCompatActivity() {

    lateinit var dbManager : DBManager
    lateinit var sqlitedb : SQLiteDatabase

    lateinit var subjTextView : TextView
    lateinit var tvTimeHour : TextView
    lateinit var tvTimeMin : TextView
    lateinit var tvTimeSec : TextView

    //저장 변수
    private var subjName = ""
    private var hour = 0
    private var min = 0
    private var sec = 0
    var date : Dates = Dates()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_info)

        //공부과목, 공부시간 아이디와 연결
        subjTextView = findViewById(R.id.subject)
        tvTimeHour = findViewById(R.id.totalTextView)
        tvTimeMin = findViewById(R.id.min_Textview)
        tvTimeSec = findViewById(R.id.sec_TextView)

        // 어댑터에서 과목 이름 받아오기
        val intent = intent
        subjTextView.text = intent.getStringExtra("subjects").toString()

        // 데이터베이스 열기 - 기록 읽어오기
        dbManager = DBManager(this, "studyDB", null, 1)
        sqlitedb = dbManager.readableDatabase

        //공부과목, 공부시간 가져오기
        var cursor : Cursor
        //cursor = sqlitedb.rawQuery("SELECT * FROM subjects WHERE subj_name = '"+ subjTextView.text.toString() +"';",null)
        cursor = sqlitedb.rawQuery("SELECT * FROM subjects WHERE subj_name = '"+ subjTextView.text.toString() +"' AND year = "
                + date.getYear() + " AND month = " + date.getMonth() + " AND day = " + date.getDay() +";",null)


        if(cursor.moveToFirst()) {
            do {
                subjName = cursor.getString((cursor.getColumnIndex("subj_name")))
                hour = cursor.getInt((cursor.getColumnIndex("subj_studyTimeHr")))
                min = cursor.getInt((cursor.getColumnIndex("subj_studyTimeMin")))
                sec = cursor.getInt((cursor.getColumnIndex("subj_studyTimeSec")))
            } while(cursor.moveToNext())
        }

        if(cursor.count != 0) {
            subjTextView.text = subjName
            tvTimeHour.text = timerFormat(hour)
            tvTimeMin.text = timerFormat(min)
            tvTimeSec.text = timerFormat(sec)
        }
        cursor.close()
        dbManager.close()
        sqlitedb.close()
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

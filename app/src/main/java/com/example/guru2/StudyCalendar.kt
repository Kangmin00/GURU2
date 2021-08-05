package com.example.guru2

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StudyCalendar : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var fname: String  //저장할 파일 이름
    lateinit var sqlitedb : SQLiteDatabase
    lateinit var dbManager: DBManager

    //총 시간에 관한 변수
    lateinit var total : TextView
    var total_hour : String = ""
    var total_min : String = ""
    var total_sec : String = ""

    //네비게이션 뷰
    lateinit var layout_drawer : DrawerLayout
    lateinit var btn_navi : ImageView
    lateinit var naviView : NavigationView

    lateinit var calendarView: CalendarView  //달력
    lateinit var dateTextView: TextView //날짜 나타남
    lateinit var showTextView: TextView  //총 공부시간, 과목별 공부시간 나타남
    lateinit var title: TextView  //제목
    lateinit var textButton: Button  //공부 기록 조회

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_calendar)

        //생성
        calendarView = findViewById(R.id.calendarView)
        dateTextView = findViewById(R.id.dateTextView)
        showTextView = findViewById(R.id.showTextView)
        title = findViewById(R.id.title)
        textButton = findViewById(R.id.textButton)
        total = findViewById(R.id.total)

        layout_drawer = findViewById<DrawerLayout>(R.id.layout_drawer)
        btn_navi = findViewById(R.id.btn_navi)
        naviView = findViewById(R.id.naviView)

        btn_navi.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)  //START: left, END: right
        }

        naviView.setNavigationItemSelectedListener(this)  //네비게이션 메뉴 아이템에 클릭 속성 부여

        //오늘의 공부 조회 버튼 누르면 과목과 시간 나오게 하는 메소드
        textButton.setOnClickListener {
            //var str_subject = textButton.text.toString()
            var intent1 = Intent(this, StudyCalendar_layout::class.java)
            startActivity(intent1)
        }

        var saved_year: Int = 0
        var saved_month: Int = 0
        var saved_day: Int = 0

        //달력 날짜가 선택되면
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            dateTextView.visibility = View.VISIBLE  //해당 날짜가 뜨는 textView가 VISIBLE
            showTextView.visibility = View.VISIBLE
            dateTextView.text = String.format("%d / %d / %d", year, month + 1, dayOfMonth)
            showTextView.setText("")

            //checkDay 메소드 호출
            checkDay(year, month, dayOfMonth)

            // 데이터베이스 열기 - 기록 읽어오기
            dbManager = DBManager(this, "studyDB", null, 1)
            sqlitedb = dbManager.readableDatabase

            var cursor: Cursor
            cursor = sqlitedb.rawQuery("SELECT * FROM calendar;", null)

            while (cursor.moveToNext()) {
                saved_year = cursor.getInt(cursor.getColumnIndex("study_Yr"))
                saved_month = cursor.getInt(cursor.getColumnIndex("study_Mth"))
                saved_day = cursor.getInt(cursor.getColumnIndex("study_Date"))
            }

            if (cursor.count != 0) {
                //공부기록 조회 버튼, 총 공부시간 보이게
                textButton.visibility = View.VISIBLE
                total.visibility = View.VISIBLE

                //총 공부시간 보여줌
                total(this)
            }
        }
    }

    fun total(mCtx : Context): CharSequence? {
        var subj_name = ""
        var time = 0

        //공부과목 가져오기
        var cursor : Cursor
        cursor = sqlitedb.rawQuery("SELECT * FROM subjects;",null)

        if(cursor.moveToNext()) {  //데이터가 있다면
            subj_name = cursor.getString(cursor.getColumnIndex("subj_name"))
            //time = cursor.getInt(cursor.getColumnIndex("totalsec"))
        }
        else {
            Toast.makeText(mCtx,"오늘의 공부기록은 없습니다.", Toast.LENGTH_SHORT).show()
        }

        //공부시간 초, 분, 시 결과 변수
        var resultSec : Int = 0
        var resultMin : Int = 0
        var resultHour : Int = 0

        var numtotal : Int = 0
        val totaltimelist : ArrayList<Int> = dbManager.totalTime(this, subj_name)

        if(cursor.moveToFirst()) {
            do {
                while(numtotal < totaltimelist.size) {
                    time += totaltimelist[numtotal]
                    numtotal++
                }

                if((time % 60) > 60) {
                    resultMin = time / 60 % 60
                }
                else if((time % 60) < 60) {
                    resultSec = time % 60
                }
                else if((time/60/60 > 60) || (time/60/60) <= 60) {
                    resultHour = time / 60 / 60
                }
            } while(cursor.moveToNext())
        }

        total_hour = timerFormat(resultHour)
        total_min = timerFormat(resultMin)
        total_sec = timerFormat(resultSec)

        total.text = "#오늘의 공부 시간 #" + "" + total_hour + ":" + total_min + ":" +  total_sec

        sqlitedb.close()
        dbManager.close()

        return total.text
    }


    //달력 조회
    fun checkDay(cYear: Int, cMonth: Int, cDay: Int) {
        //저장할 파일 이름 설정하기(ex.2021/07/01)
        fname = "" + cYear + "/" + (cMonth + 1) + "" + "/" + cDay + ".txt"
        var fileInputStream: FileInputStream  //변수 설정
        try {
            fileInputStream = openFileInput(fname)  //fname 파일 오픈
            val fileData = ByteArray(fileInputStream.available())  //fileData에 파이트 형식으로 저장
            fileInputStream.read(fileData)  //fileData 읽음
            fileInputStream.close()

            showTextView.visibility = View.INVISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_subject -> {
                val intent = Intent(this, StudySubjects::class.java)
                startActivity(intent)
                finish()
            }
            R.id.action_map -> {
                val intent = Intent(this, StudyMap::class.java)
                startActivity(intent)
                finish()
            }
            R.id.action_calendar -> {
                val intent = Intent(this, StudyCalendar::class.java)
                startActivity(intent)
                finish()
            }
        }
        layout_drawer.closeDrawers()
        return false
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

    private var pressed : Long = 0
    override fun onBackPressed() {
        if (System.currentTimeMillis() - pressed < 2000) {
            finish()
            return
        }
        Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 \n 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        pressed = System.currentTimeMillis()
    }
}
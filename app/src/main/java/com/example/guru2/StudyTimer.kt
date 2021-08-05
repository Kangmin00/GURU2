package com.example.guru2

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class StudyTimer : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var dbManager: DBManager
    lateinit var dbHandler : DBHandler
    lateinit var sqlitedb : SQLiteDatabase

    private var time = 0
    private var isRunning = false
    private var timerTask : Timer?= null
    private var isClicked = false
    var date : Dates = Dates()

    // 시간 저장 변수
    private var db_subjName = ""
    private var db_hour = 0
    private var db_min = 0
    private var db_sec = 0

    // 아이디 변수 - 사이드뷰
    lateinit var layout_drawer : DrawerLayout
    lateinit var btn_navi : ImageView
    lateinit var naviView : NavigationView
    lateinit var subjTextView : TextView

    // 아이디 변수 - 스톱워치, 투두리스트
    lateinit var hour_tv : TextView
    lateinit var min_tv : TextView
    lateinit var sec_tv : TextView
    lateinit var btnPlay : FloatingActionButton

    //프로그래스바 , 일정 추가 레이아웃
    lateinit var studyGraph : com.dinuscxj.progressbar.CircleProgressBar
    lateinit var btn_addList : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_timer)

        // 아이디와 코드 연결
        layout_drawer = findViewById<DrawerLayout>(R.id.layout_drawer)
        btn_navi = findViewById(R.id.btn_back)
        naviView = findViewById(R.id.naviView)
        subjTextView = findViewById(R.id.subjectTextView)

        studyGraph = findViewById(R.id.study_graph)
        studyGraph.max = 8640 // 24시간 - 목표시간으로 설정?

        // 스톱워치, 투두 리스트 연결
        btnPlay = findViewById(R.id.FABPlay)
        btn_addList = findViewById(R.id.btn_addList)

        hour_tv =findViewById(R.id.totalTextView)
        min_tv = findViewById(R.id.min_Textview)
        sec_tv = findViewById(R.id.sec_TextView)

        // 사이드뷰 메뉴 버튼 클릭 활성화
        btn_navi.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)
        }

        naviView.setNavigationItemSelectedListener(this)
        // 어댑터에서 과목 이름 받아오기
        val intent = intent
        subjTextView.text = intent.getStringExtra("subject_name").toString()

        // 데이터베이스 열기 - 기록 읽어오기
        dbManager = DBManager(this, "studyDB", null, 1)
        date.setTodayDate() // 날짜 오늘로 맞추기
        /*
        if(dbManager.checkTodaysDate()){ // 날짜가 다르다면
            sqlitedb = dbManager.writableDatabase
            sqlitedb.execSQL("INSERT INTO subjects VALUES ('" + subjTextView.text.toString() + "',0,0,0,0, " +
                    "'" + date.getYear() + "" + date.getMonth() + "" + date.getDay() + "')") // 날짜 추가
            Log.i("insert","너어따")
        }
        */
        sqlitedb = dbManager.readableDatabase
        // 시간 읽어오기
        var cursor : Cursor
        cursor = sqlitedb.rawQuery("SELECT * FROM subjects WHERE subj_name = '"+ subjTextView.text.toString() +"' AND year = "
                + date.getYear() + " AND month = " + date.getMonth() + " AND day = " + date.getDay() +";",null)
        //val size = cursor.getCount()

        if(cursor.moveToNext()) {
            db_subjName = cursor.getString((cursor.getColumnIndex("subj_name")))
            db_hour = cursor.getInt((cursor.getColumnIndex("subj_studyTimeHr")))
            db_min = cursor.getInt((cursor.getColumnIndex("subj_studyTimeMin")))
            db_sec = cursor.getInt((cursor.getColumnIndex("subj_studyTimeSec")))
            time = cursor.getInt((cursor.getColumnIndex("totalsec")))
        }

        cursor.close()
        sqlitedb.close()
        dbManager.close()

        // 날짜 체크
        dbManager.checkTodaysDate()

        //subjTextView.text = db_subjName
        hour_tv.text = timerFormat(db_hour)
        min_tv.text = timerFormat(db_min)
        sec_tv.text = timerFormat(db_sec)
        studyGraph.setProgress(time)

        viewTodos()

        // 투두 리스트 추가버튼
        btn_addList.setOnClickListener {
            val dlg = AlertDialog.Builder(this)
            val dlgAddListView = layoutInflater.inflate(R.layout.dlg_add_list, null)
            val dlgEditText = dlgAddListView.findViewById<EditText>(R.id.dlglist_tv)

            //sqlitedb = dbManager.writableDatabase
            // 다이얼로그 생성
            dlg.setView(dlgAddListView)
            dlg.setPositiveButton("저장"){ dialog, which ->
                dbManager = DBManager(this, "todoListdb", null, 1)
                isClicked = !isClicked
                val checklist = TextView(this)
                checklist.text = dlgEditText.text.toString()
                checklist.textSize = 20f
                // 체크리스트 저장
                dbManager.addTodos(this,subjTextView.text.toString(), checklist.text.toString())
                viewTodos()
            }
            dlg.setNegativeButton("취소",null)
            dlg.show()

        }

        // 타이머 시작 버튼
        btnPlay.setOnClickListener {
            isRunning = !isRunning

            if(isRunning){
                timer_start()
            } else {
                timer_pause()
            }
        }

    }

    // 메뉴 아이템
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_subject ->{
                val intent = Intent(this, StudySubjects::class.java)
                startActivity(intent)
                Toast.makeText(this,"공부 종료: 과목을 선택해주세요", Toast.LENGTH_LONG).show()
                saveTime()
                finish()
            }
            R.id.action_map ->{
                val intent = Intent(this, StudyMap::class.java)
                startActivity(intent)
                Toast.makeText(this,"공부 종료: 과목을 선택해주세요", Toast.LENGTH_LONG).show()
                saveTime()
                finish()
            }
            R.id.action_calendar ->{
                val intent = Intent(this, StudyCalendar::class.java)
                startActivity(intent)
                Toast.makeText(this,"공부 종료: 과목을 선택해주세요", Toast.LENGTH_LONG).show()
                saveTime()
                finish()
            }
        }
        layout_drawer.closeDrawers()
        return false
    }

    // 타이머 시작 , 정지
    private fun timer_start(){
        btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)

        timerTask = timer(period = 1000){
            time++
            val progress = time
            val sec = time % 60
            val min = time / 60 % 60
            val hour =time / 3600
            runOnUiThread{
                studyGraph.setProgress(progress)
                // 초
                sec_tv.text = timerFormat(sec)
                // 분
                min_tv.text = timerFormat(min)
                // 시
                hour_tv.text = timerFormat(hour)
            }
        }
        Toast.makeText(this,"공부 시작!", Toast.LENGTH_SHORT).show()
    }

    private fun viewTodos(){
        dbManager = DBManager(this, "todoListdb", null, 1)
        val todoLists : ArrayList<TextView> = dbManager.getTodos(this, db_subjName)
        val adapter = ListAdapter(this, todoLists)
        val rv : RecyclerView = findViewById(R.id.recyclerV)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter
    }
    override fun onResume() {
        viewTodos()
        super.onResume()
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
    // 시간 저장
    private fun saveTime(){
        dbManager = DBManager(this, "studyDB", null, 1)
        dbHandler = DBHandler(this, null, null, 1)

        sqlitedb = dbManager.writableDatabase
        ////////////////////////////////////////////////////////////////////////////////////
        sqlitedb.execSQL("UPDATE subjects SET (subj_studyTimeHr, subj_studyTimeMin, subj_studyTimeSec, totalsec) = ("+
                hour_tv.text.toString().toInt()+", " +min_tv.text.toString().toInt()+", "+sec_tv.text.toString().toInt()+
                ", "+time+") WHERE subj_name = '"+subjTextView.text.toString()+"' AND year = " + date.getYear() + " AND month = " + date.getMonth() + " AND day = " + date.getDay() + ";")
//////////////////////////////////////////////////////////////////////////////////////////////////////////
        sqlitedb.close()
        dbManager.close()
    }

    private fun timer_pause(){
        btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        timerTask?.cancel()

        Toast.makeText(this,"일시 정지", Toast.LENGTH_SHORT).show()
        saveTime()
    }

    // 종료
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
package com.example.guru2

import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.lang.Error
import java.util.*
import kotlin.collections.ArrayList

class StudySubjects : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var dbManager: DBManager
    lateinit var sqlitedb : SQLiteDatabase

    var date : Dates = Dates()

    companion object {
        lateinit var dbHandler: DBHandler
    }

    // 아이디와 코드 연결
    lateinit var layout_drawer : DrawerLayout
    lateinit var btn_navi : ImageView
    lateinit var naviView : NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_subjects)

        layout_drawer = findViewById<DrawerLayout>(R.id.layout_drawer)
        btn_navi = findViewById(R.id.btn_navi)
        naviView = findViewById(R.id.naviView)

        dbHandler = DBHandler(this, null, null, 1)

        // 사이드뷰 메뉴 버튼 클릭 활성화
        btn_navi.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)
        }

        naviView.setNavigationItemSelectedListener(this)

        date.setTodayDate()
        viewSubjects()

        var fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val v = LayoutInflater.from(this).inflate(R.layout.lo_input_subject, null)
            val name = v.findViewById<EditText>(R.id.editSubjectName)
            AlertDialog.Builder(this)
                .setTitle("과목 추가")
                .setView(v)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                    if (name.text.isEmpty()) {
                        Toast.makeText(this, "과목 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        val subject = Subject()
                        subject.subjectName = name.text.toString()
                        dbHandler.addSubjects(this, subject)
                        //////////////////////////////////////////////////////////////////////////////////////////////////////////
                        // DBManager에 개별 저장
                        dbManager = DBManager(this, "studyDB", null, 1)
                        sqlitedb = dbManager.writableDatabase
                        //sqlitedb.execSQL("INSERT INTO subjects VALUES ('" + subject.subjectName + "',0,0,0,0, " + "'" + date.getYear() + "" + date.getMonth() + "" + date.getDay() + "')") // 날짜 추가
                        sqlitedb.execSQL("INSERT INTO subjects VALUES ('" + subject.subjectName +"',0,0,0,0, " + date.getYear() + ", "+ date.getMonth() +", "+ date.getDay() +")")

                        //Log.e("ERROR", "insert error")
                        sqlitedb.close()
                        dbManager.close()
                        //////////////////////////////////////////////////////////////////////////////////////////////////////////
                        name.text.clear()
                        dialog.dismiss()
                        viewSubjects()
                    }
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                    name.text.clear()
                    dialog.dismiss()
                })
                .create()
                .show()
        }
    }

    private fun viewSubjects() {
        val subjectsList: ArrayList<Subject> = dbHandler.getSubjects(this)
        val adapter = SubjectAdapter(this, subjectsList)
        val rv: RecyclerView = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter
    }

    override fun onResume() {
        viewSubjects()
        super.onResume()
    }

    // 메뉴 아이템
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_subject ->{
                val intent = Intent(this, StudySubjects::class.java)
                startActivity(intent)
                finish()
            }
            R.id.action_map ->{
                val intent = Intent(this, StudyMap::class.java)
                startActivity(intent)
                finish()
            }
            R.id.action_calendar ->{
                val intent = Intent(this, StudyCalendar::class.java)
                startActivity(intent)
                finish()
            }
        }

        layout_drawer.closeDrawers()
        return false
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
package com.example.guru2

import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.service.autofill.UserData
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
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
import javax.security.auth.Subject

class StudyMap : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        lateinit var SQLiteHelper: SQLiteHelper
    }

    // 아이디 변수 - 사이드뷰
    lateinit var layout_drawer : DrawerLayout
    lateinit var btn_navi : ImageView
    lateinit var naviView : NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_map)

        setFrag() //지도 fragment 연결

        SQLiteHelper = SQLiteHelper(this, null, null, 1)

        // 아이디와 코드 연결
        layout_drawer = findViewById<DrawerLayout>(R.id.layout_drawer)
        btn_navi = findViewById(R.id.btn_navi)
        naviView = findViewById(R.id.naviView)

        // 사이드뷰 메뉴 버튼 클릭 활성화
        btn_navi.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)
        }

        naviView.setNavigationItemSelectedListener(this)

        viewPlaces()

        //자주 가는 장소 추가
        var addBtn = findViewById<FloatingActionButton>(R.id.addBtn)
        addBtn.setOnClickListener {
            val v = LayoutInflater.from(this).inflate(R.layout.input_place, null)
            val name = v.findViewById<EditText>(R.id.editPlaceName)
            AlertDialog.Builder(this)
                .setTitle("자주 가는 장소")
                .setView(v)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                    if (name.text.isEmpty()) {
                        Toast.makeText(this, "장소 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        val place = Place()
                        place.placeName = name.text.toString()
                        SQLiteHelper.addPlaces(this, place)
                        name.text.clear()
                        dialog.dismiss()
                        viewPlaces()
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


    //지도 fragment 연결
    private fun setFrag() {

        val ft = supportFragmentManager.beginTransaction()

        ft.replace(R.id.mapfrag, frag_map()).commit()

    }


    private fun viewPlaces() {
        val placesList: ArrayList<Place> = SQLiteHelper.getPlaces(this)
        val adapter = PlaceAdapter(this, placesList)
        val rv: RecyclerView = findViewById(R.id.Recycler)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter
    }

    override fun onResume() {
        viewPlaces()
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
package com.example.guru2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var layout_drawer : DrawerLayout
    lateinit var btn_navi : ImageView
    lateinit var naviView : NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layout_drawer = findViewById<DrawerLayout>(R.id.layout_drawer)
        btn_navi = findViewById(R.id.btn_navi)
        naviView = findViewById(R.id.naviView)

        btn_navi.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)
        }

        naviView.setNavigationItemSelectedListener(this)
    }

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
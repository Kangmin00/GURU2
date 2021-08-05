package com.example.guru2

import java.util.*

class Dates{
    private var today_day =0
    private var today_month =0
    private var today_year = 0

    fun Dates(day : Int, month : Int, year: Int){
        this.today_day = day
        this.today_month = month
        this.today_year = year
    }

    fun getDay(): Int {
        return today_day
    }
    fun getMonth(): Int{
        return today_month
    }
    fun getYear(): Int{
        return today_year
    }

    fun setTodayDate(){
        val instance = Calendar.getInstance()
        val day = instance.get(Calendar.DATE)
        val month = instance.get(Calendar.MONTH) + 1
        val year = instance.get(Calendar.YEAR)

        this.today_day = day
        this.today_month = month
        this.today_year = year
    }
}
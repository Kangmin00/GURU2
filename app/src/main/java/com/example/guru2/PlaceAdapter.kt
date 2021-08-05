package com.example.guru2

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
//import noman.googleplaces.Place
import javax.security.auth.Subject


class PlaceAdapter(val mCtx: Context, val places: ArrayList<com.example.guru2.Place>): RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var txtPlaceName: TextView
        var btnOptions: ImageView

        init {
            txtPlaceName = itemView.findViewById(R.id.txtPlaceName)
            btnOptions = itemView.findViewById(R.id.btnOpt)
            btnOptions.setOnClickListener { popupOptions(it) }
        }

        private fun popupOptions(itemView: View) {
            val place = places[adapterPosition]
            val popupMenus = PopupMenu(mCtx, itemView)
            popupMenus.inflate(R.menu.place_options)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId) {
                    //수정
                    R.id.btnUp -> {
                        val v = LayoutInflater.from(mCtx).inflate(R.layout.input_place, null)
                        val name = v.findViewById<EditText>(R.id.editPlaceName)
                        v.findViewById<TextView>(R.id.txtPlace).text = "수정할 장소"
                        name.setText(place.placeName)
                        name.setSelection(name.length())
                        AlertDialog.Builder(mCtx)
                                .setTitle("장소 수정")
                                .setView(v)
                                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                                    val isUpdate = StudyMap.SQLiteHelper.updatePlace(place.placeID.toString(), name.text.toString())
                                    if (isUpdate) {
                                        place.placeName = name.text.toString()
                                        notifyDataSetChanged()
                                        Toast.makeText(mCtx, "이름이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(mCtx, "수정 오류", Toast.LENGTH_SHORT).show()
                                    }
                                    dialog.dismiss()
                                })
                                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                                .create()
                                .show()
                        true
                    }
                    //삭제
                    R.id.btnDel -> {
                        AlertDialog.Builder(mCtx)
                                .setTitle("장소 삭제")
                                .setMessage("${place.placeName} 삭제하시겠습니까?")
                                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                                    if (StudyMap.SQLiteHelper.deletePlace(place.placeID)) {
                                        places.removeAt(adapterPosition)
                                        notifyItemRemoved(adapterPosition)
                                        notifyItemRangeChanged(adapterPosition, places.size)
                                        Toast.makeText(mCtx, "${place.placeName} 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(mCtx, "장소 삭제 오류", Toast.LENGTH_SHORT).show()
                                    }
                                    dialog.dismiss()
                                })
                                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                                .create()
                                .show()
                        true
                    }
                    else -> true
                }
            }
            popupMenus.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.place_items, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: PlaceAdapter.ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return places.size
    }

    override fun onBindViewHolder(holder: PlaceAdapter.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val place: com.example.guru2.Place = places[position]
        holder.txtPlaceName.text = place.placeName
    }
}


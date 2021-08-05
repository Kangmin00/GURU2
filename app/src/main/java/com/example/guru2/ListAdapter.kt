package com.example.guru2

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class ListAdapter (val mCtx: Context, val todos: ArrayList<TextView>): RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    lateinit var dbManager: DBManager

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var checkList : CheckBox
        var btn_delete : ImageView
        var checkTextView : TextView

        init{
            checkList = itemView.findViewById(R.id.checkList)
            btn_delete = itemView.findViewById(R.id.btn_delete)
            checkTextView = itemView.findViewById(R.id.checkTextView)

            checkList.setOnClickListener{
                // 체크시 완료선
                if(checkList.isChecked){
                    checkTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG)
                }
                else{
                    checkTextView.setPaintFlags(0)
                }
            }

            btn_delete.setOnClickListener {
                // 투두리스트 삭제
                dbManager = DBManager(mCtx, "todoListdb", null, 1)
                var result = dbManager.deleteTodos(checkTextView.text.toString())
                if(result){
                    todos.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    notifyItemRangeChanged(adapterPosition, todos.size)
                    Toast.makeText(mCtx,"삭제 완료",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.checklist_layout, parent, false)    // 리사이클러 뷰에서 아이템 뷰로 사용할 레이아웃을 객체화
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ListAdapter.ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    override fun onBindViewHolder(holder: ListAdapter.ViewHolder, position: Int, payloads:MutableList<Any>) {
        val todoList : TextView = todos[position]
        holder.checkTextView.text = todoList.text.toString()
    }
}
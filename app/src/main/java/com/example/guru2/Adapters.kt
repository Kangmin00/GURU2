package com.example.guru2

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import javax.security.auth.Subject

class SubjectAdapter(val mCtx: Context, val subjects: ArrayList<com.example.guru2.Subject>): RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {

    lateinit var dbManager: DBManager
    lateinit var sqlitedb : SQLiteDatabase


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var txtSubjectName: TextView
        var btnOptions: ImageView

        init {
            txtSubjectName = itemView.findViewById(R.id.txtSubjectName)
            txtSubjectName.setOnClickListener{
                var str_subjName = txtSubjectName.text.toString()
                // 과목 이름 인텐트로 넘기기
                val intent = Intent(mCtx, StudyTimer::class.java)
                intent.putExtra("subject_name", txtSubjectName.text.toString())
                mCtx.startActivity(intent)
            }
            btnOptions = itemView.findViewById(R.id.btnOptions)
            btnOptions.setOnClickListener { popupOptions(it) }
        }

        private fun popupOptions(itemView: View) {
            val subject = subjects[adapterPosition]
            val popupMenus = PopupMenu(mCtx, itemView)
            popupMenus.inflate(R.menu.subject_options)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.btnUpdate -> {
                        val v = LayoutInflater.from(mCtx).inflate(R.layout.lo_input_subject, null)
                        val name = v.findViewById<EditText>(R.id.editSubjectName)
                        v.findViewById<TextView>(R.id.txtDesc).text = "수정할 과목"
                        name.setText(subject.subjectName)
                        name.setSelection(name.length())
                        AlertDialog.Builder(mCtx)
                            .setTitle("과목 수정")
                            .setView(v)
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                                // DBManager에 개별 저장
                                dbManager = DBManager(mCtx, "studyDB", null, 1)
                                sqlitedb = dbManager.writableDatabase

                                val isUpdate = StudySubjects.dbHandler.updateSubject(subject.subjectID.toString(), name.text.toString())
                                if (isUpdate) {
                                    sqlitedb.execSQL("UPDATE subjects SET subj_name = '" + name.text.toString() + "' WHERE subj_name = '"+subject.subjectName+"'")

                                    dbManager = DBManager(mCtx, "todoListdb", null,1)
                                    sqlitedb = dbManager.writableDatabase
                                    val listSize = dbManager.getSelectAllCount(subject.subjectName)    // 과목 이름과 맞는 레코드 개수를 커서값으로 반환 후 반복 변경
                                    for (i in 1..listSize){
                                        sqlitedb.execSQL("UPDATE todoMVA SET fk_subj_name = '"+ name.text.toString() +"' WHERE fk_subj_name ='"+subject.subjectName+"'")
                                    }

                                    subject.subjectName = name.text.toString()
                                    notifyDataSetChanged()
                                    Toast.makeText(mCtx, "과목 이름이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(mCtx, "과목 수정 오류", Toast.LENGTH_SHORT).show()
                                }
                                sqlitedb.close()
                                dbManager.close()

                                dialog.dismiss()
                            })
                            .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                            .create()
                            .show()
                        true
                    }
                    R.id.btnDelete -> {
                        AlertDialog.Builder(mCtx)
                            .setTitle("과목 삭제")
                            .setMessage("${subject.subjectName} 과목을 삭제하시겠습니까?")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                                // DBManager에 개별 저장
                                dbManager = DBManager(mCtx, "studyDB", null, 1)
                                sqlitedb = dbManager.writableDatabase

                                if (StudySubjects.dbHandler.deleteSubject(subject.subjectID)) {
                                    sqlitedb.execSQL("DELETE FROM subjects WHERE subj_name = '"+subject.subjectName+"'")
                                    // subjects 테이블에서 과목 삭제 후, 관련 투두리스트도 전체 삭제
                                    dbManager = DBManager(mCtx, "todoListdb", null,1)
                                    sqlitedb = dbManager.writableDatabase
                                    val listSize = dbManager.getSelectAllCount(subject.subjectName)    // 과목 이름과 맞는 레코드 개수를 커서값으로 반환 후 반복삭제
                                    for (i in 1..listSize){
                                        sqlitedb.execSQL("DELETE FROM todoMVA WHERE fk_subj_name = '"+subject.subjectName+"'")
                                    }

                                    subjects.removeAt(adapterPosition)
                                    notifyItemRemoved(adapterPosition)
                                    notifyItemRangeChanged(adapterPosition, subjects.size)
                                    Toast.makeText(mCtx, "${subject.subjectName} 과목이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(mCtx, "과목 삭제 오류", Toast.LENGTH_SHORT).show()
                                }
                                sqlitedb.close()
                                dbManager.close()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.lo_subjects, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: SubjectAdapter.ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return subjects.size
    }

    override fun onBindViewHolder(holder: SubjectAdapter.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val subject: com.example.guru2.Subject = subjects[position]
        holder.txtSubjectName.text = subject.subjectName
    }
}
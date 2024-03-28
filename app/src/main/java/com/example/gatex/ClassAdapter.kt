package com.example.gatex



import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class CustomAdapter(private val dataList: List<Visitor>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bindData(data)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val phoneTextView: TextView = itemView.findViewById(R.id.phoneTextView)
        private val purposeTextView: TextView = itemView.findViewById(R.id.purposeTextView)
        private val outButton: Button = itemView.findViewById(R.id.outButton)

        fun bindData(data: Visitor) {
            nameTextView.text = data.name
            phoneTextView.text = data.phoneNumber
            purposeTextView.text = data.purpose
            val userId: String? = data.id
            outButton.setOnClickListener {

                val reference = FirebaseDatabase.getInstance().reference.child("admin").child("security_guards").child(data.securityId.toString())
                val dataMap = HashMap<String, Any>()

                val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val currentTime = sdfTime.format(System.currentTimeMillis())

                dataMap["outTime"] = currentTime

                reference.child(userId.toString()).updateChildren(dataMap)
                    .addOnSuccessListener {
                       // Toast.makeText(, "Out time updated successfully", Toast.LENGTH_SHORT).show()
                        Log.d("dfghjk","sdfghjk")
                    }
                    .addOnFailureListener { e ->
                        //Toast.makeText(requireContext(), "Failed to update out time: ${e.message}", Toast.LENGTH_SHORT).show()
                    }


            }
        }
    }
}

package com.example.gatex

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class TableAdapter(private val visitors: List<Visitor>) :
    RecyclerView.Adapter<TableAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val visitorImageUrl: ImageView = itemView.findViewById(R.id.imageView)
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textPhoneNumber: TextView = itemView.findViewById(R.id.textPhoneNumber)
        val textPurpose: TextView = itemView.findViewById(R.id.textPurposeVal)
        val inTime:TextView = itemView.findViewById(R.id.inTime)
        val outTime:TextView = itemView.findViewById(R.id.outTime)
        val security:TextView = itemView.findViewById(R.id.nameSecurity)
        val dateEntery:TextView = itemView.findViewById(R.id.dateEntery)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_table_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visitor = visitors[position]
        val currentUser = FirebaseAuth.getInstance().currentUser
        holder.textName.text = visitor.name
        holder.textPhoneNumber.text = visitor.phoneNumber
        holder.textPurpose.text = visitor.purpose
        holder.inTime.text = visitor.inTime
        holder.outTime.text = visitor.outTime
        holder.security.text =  visitor.security
        holder.dateEntery.text = visitor.dateEntery
        if (currentUser != null && visitor.visitorImageUrl != "") {
            Log.d("image url",visitor.visitorImageUrl.toString())
            Picasso.get().load(visitor.visitorImageUrl).into(holder.visitorImageUrl, object : Callback {
                override fun onSuccess() {

                }

                override fun onError(e: Exception?) {
                    // Log the error
                    Log.e("Picasso", "Error loading image", e)
                }
            })
        }

    }

    override fun getItemCount(): Int {
        return visitors.size
    }
}

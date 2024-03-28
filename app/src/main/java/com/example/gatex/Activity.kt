package com.example.gatex

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Activity : Fragment(), SecurityDataListener {

    private val auth = FirebaseAuth.getInstance()
    private val entryList = mutableListOf<Visitor>()
    private lateinit var adapter: TableAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activity, container, false)

        // Setup RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(context)
        adapter = TableAdapter(entryList)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        ServiceUser.registerListener(this)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ServiceUser.unregisterListener(this)
    }

    override fun onDataChanged(security: Security) {
        checkAdminPermission(security)
    }

    private fun checkAdminPermission(security: Security) {

        getDataForUser()
        if(security.role == "admin"){
            getDataForAdmin()
        }else{
            getDataForUser()
        }

    }

    private fun getDataForUser() {

        val uidReference = FirebaseDatabase.getInstance().getReference("admin/security_guards/${auth.uid}")

        uidReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                entryList.clear()
                for (snapshot in dataSnapshot.children) {
                    val visitor = snapshot.getValue(Visitor::class.java)
                    visitor?.let {
                        entryList.add(it)
                    }
                }
                view?.findViewById<ProgressBar>(R.id.loadingSpin)?.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("TAG", "No Data is found")
            }
        })
    }

    private fun getDataForAdmin() {

        val uidReference = FirebaseDatabase.getInstance().reference
            .child("admin")
            .child("security_guards")

        uidReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    entryList.clear()
                    for (childSnapshot in dataSnapshot.children) {
                        for (user in childSnapshot.children){
                            val visitor = user.getValue(Visitor::class.java)

                            if (visitor != null) {
                                entryList.add(visitor)
                            }
                        }
                    }
                    view?.findViewById<ProgressBar>(R.id.loadingSpin)?.visibility = View.GONE
                    adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("TAG", "Database error: ${databaseError.message}")
            }
        })
    }

    private fun openAnotherFragment() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val newFragment = Home()
        transaction.replace(R.id.frame_layout, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}

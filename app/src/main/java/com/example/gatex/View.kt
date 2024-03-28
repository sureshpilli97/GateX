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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class View : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = "abcd"
            param2 = "Wxyz"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_view, container, false)

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dataList = mutableListOf<Visitor>()
        val adapter = CustomAdapter(dataList)

        recyclerView.adapter = adapter

        // Get FirebaseAuth instance and check if user is authenticated
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is authenticated, fetch data
            val uidReference = FirebaseDatabase.getInstance().getReference("admin/security_guards/${currentUser.uid}")
            uidReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataList.clear() // Clear existing data
                    for (snapshot in dataSnapshot.children) {
                        val visitor = snapshot.getValue(Visitor::class.java)
                        visitor?.let {
                            if(visitor.outTime == ""){
                                dataList.add(it)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("TAG", "No Data is found")
                }
            })
        } else {
            // User is not authenticated, handle accordingly
            Log.e("TAG", "User is not authenticated")
        }

        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            View().apply {
                arguments = Bundle().apply {
                }
            }
    }
}

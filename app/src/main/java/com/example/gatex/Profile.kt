package com.example.gatex

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class Profile : Fragment(), SecurityDataListener {
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userPhone: TextView
    private lateinit var userRole: TextView
    private lateinit var userImg: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userName = view.findViewById(R.id.textView4)
        userEmail = view.findViewById(R.id.textView5)
        userPhone = view.findViewById(R.id.textPhoneNumber)
        userRole = view.findViewById(R.id.textRole)
        userImg = view.findViewById(R.id.imageView2)

        val logOutBtn = view.findViewById<Button>(R.id.logOutButton)
        logOutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
        }

        ServiceUser.registerListener(this)

        val admin = view.findViewById<Button>(R.id.viewAdmin)
        admin.setOnClickListener {
            replaceFragment(Admin())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ServiceUser.unregisterListener(this)
    }

    override fun onDataChanged(security: Security) {
        setUserData(security)
        if(security.role == "admin"){
            view?.findViewById<Button>(R.id.viewAdmin)?.visibility = View.VISIBLE
        }
    }

    private fun setUserData(userData: Security) {
        userName.text = userData.name
        userEmail.text = userData.email
        userPhone.text = userData.phoneNumber
        userRole.text = userData.designation
        val img: String? = userData.img
        Log.d("user data",userData.toString())
        if (img != "") {
            try{
                Picasso.get().load(img).into(userImg, object : Callback {
                    override fun onSuccess() {
                        // Image loaded successfully
                    }

                    override fun onError(e: Exception?) {
                        // Handle error
                    }
                })
            }catch(e:Exception){
                Log.d("photo","high quality")

            }
        }
    }

    private fun replaceFragment(fragment : Fragment){

        val fragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

    }
}

package com.example.gatex

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Admin.newInstance] factory method to
 * create an instance of this fragment.
 */
class Admin : Fragment(),SecurityDataListener {

    private var user: String = ""
    private lateinit var btnTake: Button
    private lateinit var sendDbButton: Button
    private var userUri: Uri = Uri.EMPTY
    private lateinit var previewImageView:ImageView

    private var name: String = ""
    private var phoneNumber: String = ""
    private var email: String = ""
    private var designation: String = ""
    private var role: String = ""

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_admin, container, false)

        val spinnerRole = view.findViewById<Spinner>(R.id.spinnerRole)
        val roles = arrayOf("admin", "user")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        btnTake = view.findViewById(R.id.btnTakePicture)
        previewImageView = view.findViewById(R.id.previewImageView)

        sendDbButton = view.findViewById(R.id.btnAdd)

        btnTake.setOnClickListener{
            captureImage()
            val idImageFile = File(requireContext().getExternalFilesDir(null), "my_images/user.png")
            val idImageUri = FileProvider.getUriForFile(requireContext(), "com.example.gatex.fileProvider", idImageFile)
            previewImageView.setImageURI(idImageUri)
            userUri = idImageUri
            previewImageView.visibility = View.VISIBLE
            btnTake.visibility = View.GONE
        }

        sendDbButton.setOnClickListener {
            val nameEditText = view?.findViewById<EditText>(R.id.editTextName)
            val phoneNumberEditText = view?.findViewById<EditText>(R.id.editTextPhoneNumber)
            val roleSpinner = view?.findViewById<Spinner>(R.id.spinnerRole)
            val des = view?.findViewById<EditText>(R.id.editTextRole)
            val eText = view?.findViewById<EditText>(R.id.editTextEmail)

            name = nameEditText?.text.toString()
            phoneNumber = phoneNumberEditText?.text.toString()
            role = roleSpinner?.selectedItem.toString()
            email = eText?.text.toString()
            designation = des?.text.toString()
            view?.findViewById<ProgressBar>(R.id.loadingSpinner)?.visibility = View.VISIBLE
            ServiceUser.registerListener(this)
        }

        return view
    }
    fun sendImageToStorage(uri: Uri, callback: (String) -> Unit) {
        if (uri == Uri.EMPTY) {
            callback("")
            return
        }

        val imageRef = storageReference.child("users/${user}.jpg")

        imageRef.putFile(uri)
            .addOnSuccessListener { _ ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imgUri = uri.toString()
                    callback(imgUri)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Image upload failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun captureImage() {
        val imageUri = createImageUri()
        if (imageUri != null) {
            takePictureLauncher.launch(imageUri)
        } else {
            Toast.makeText(requireContext(), "Failed to create image URI", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccessful ->
            if (isSuccessful) {
                //
            } else {
                // Handle the failure or cancellation
            }
        }

    private fun createImageUri(): Uri? {
        val imageFileName = "user.png"
        val storageDir = File(requireContext().getExternalFilesDir(null), "my_images")

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val image = File(storageDir, imageFileName)

        return FileProvider.getUriForFile(
            requireContext(),
            "com.example.gatex.fileProvider",
            image
        )
    }

    fun createUser(){
        sendImageToStorage(userUri) { userUrl ->


            if (name.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || role.isEmpty() || designation.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            }else {

                val reference = database.reference.child("admin").child("security")

                val dataMap = Security(
                    id = user,
                    name = name,
                    phoneNumber = phoneNumber,
                    email = email,
                    img = userUrl,
                    designation = designation,
                    role = role
                )

                reference.child(user).setValue(dataMap)
                    .addOnSuccessListener {
                        view?.findViewById<ProgressBar>(R.id.loadingSpinner)?.visibility = View.GONE

                        previewImageView.visibility = View.GONE
                        btnTake.visibility = View.VISIBLE

                        userUri = Uri.EMPTY
                        user = ""

                        Toast.makeText(requireContext(), "Sign up successful", Toast.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener {
                        view?.findViewById<ProgressBar>(R.id.loadingSpinner)?.visibility = View.GONE

                        previewImageView.visibility = View.GONE
                        btnTake.visibility = View.VISIBLE

                        userUri = Uri.EMPTY
                        user = ""

                        Toast.makeText(
                            requireContext(),
                            "Please register again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun signUp(password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("asdfgh",firebaseAuth.uid.toString())
                    user = firebaseAuth.uid.toString()
                    createUser()
                } else {

                    Log.e("SignUp", "User creation failed: ${task.exception?.message}")
                    view?.findViewById<ProgressBar>(R.id.loadingSpinner)?.visibility = View.GONE

                    previewImageView.visibility = View.GONE
                    btnTake.visibility = View.VISIBLE

                    userUri = Uri.EMPTY
                    Toast.makeText(
                        requireContext(),
                        "Please register again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ServiceUser.unregisterListener(this)
    }

    override fun onDataChanged(security: Security) {
        signUp("SVEC1234")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Admin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Admin().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
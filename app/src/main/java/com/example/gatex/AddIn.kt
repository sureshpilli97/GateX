package com.example.gatex

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddIn : Fragment(), SecurityDataListener {

    private lateinit var sendBtn: Button
    private lateinit var idBtn: Button
    private lateinit var visBtn: Button
    private lateinit var btnVehicle:Button


    private lateinit var security: Security

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var name:String
    private lateinit var phone:String
    private lateinit var purpose:String

    private var count = 1
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storageReference = FirebaseStorage.getInstance().reference

    private val user = firebaseAuth.currentUser
    private val uid = user?.uid

    private var visitorUri: Uri = Uri.EMPTY
    private var vechileUri: Uri = Uri.EMPTY
    private var idUri: Uri = Uri.EMPTY

    private lateinit var previewVisitorImageView: ImageView
    private lateinit var previewIdImageView: ImageView
    private lateinit var previewVehicleImageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_in, container, false)

        previewVisitorImageView = view.findViewById(R.id.previewVisitorImageView)
        previewIdImageView = view.findViewById(R.id.previewIdImageView)
        previewVehicleImageView = view.findViewById(R.id.previewVehicleImageView)

        visBtn = view.findViewById(R.id.btnVisitor)
        sendBtn = view.findViewById(R.id.sendDb)
        idBtn = view.findViewById(R.id.btnId)
        val checkBox = view.findViewById<CheckBox>(R.id.checkYes)
        btnVehicle = view.findViewById(R.id.btnVechicle)

        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                view?.findViewById<LinearLayout>(R.id.vehicleLayout)?.visibility = View.VISIBLE
            } else {
                view?.findViewById<LinearLayout>(R.id.vehicleLayout)?.visibility = View.GONE
                count = 2
            }
        }
        ServiceUser.registerListener(this)

        visBtn.setOnClickListener {
            captureImage()
            val visitorImageFile = File(requireContext().getExternalFilesDir(null), "my_images/camera_photo$count.png")
            val visitorImageUri = FileProvider.getUriForFile(requireContext(), "com.example.gatex.fileProvider", visitorImageFile)
            //previewVisitorImageView.setImageURI(visitorImageUri)
            visitorUri = visitorImageUri
            visBtn.visibility = View.GONE
            count++//3
            previewVisitorImageView.visibility = View.VISIBLE
        }

        idBtn.setOnClickListener {
            captureImage()
            val idImageFile = File(requireContext().getExternalFilesDir(null), "my_images/camera_photo$count.png")
            val idImageUri = FileProvider.getUriForFile(requireContext(), "com.example.gatex.fileProvider", idImageFile)
            //previewIdImageView.setImageURI(idImageUri)
            idUri = idImageUri
            idBtn.visibility = View.GONE
            count = 1//1
            previewIdImageView.visibility = View.VISIBLE
        }

        btnVehicle.setOnClickListener {
            captureImage()
            val vehicleImageFile = File(requireContext().getExternalFilesDir(null), "my_images/camera_photo$count.png")
            val vehicleImageUri = FileProvider.getUriForFile(requireContext(), "com.example.gatex.fileProvider", vehicleImageFile)
            //previewVehicleImageView.setImageURI(vehicleImageUri)
            vechileUri = vehicleImageUri
            btnVehicle.visibility = View.GONE
            count++//2
            previewVehicleImageView.visibility = View.VISIBLE
        }

        sendBtn.setOnClickListener {
            val nameEditText = view.findViewById<EditText>(R.id.editTextName)
            val phoneEditText = view.findViewById<EditText>(R.id.editTextPhoneNumber)
            val purposeEditText = view.findViewById<EditText>(R.id.editTextPurpose)

            name = nameEditText.text.toString()
            phone = phoneEditText.text.toString()
            purpose = purposeEditText.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty() && purpose.isNotEmpty()) {
                sendDataToFirebase()
                sendBtn.visibility = View.GONE
                view?.findViewById<ProgressBar>(R.id.loadingSpinner)?.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ServiceUser.unregisterListener(this)
    }

    override fun onDataChanged(security: Security) {
        this.security = security
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
                
            } else {
                // Handle the failure or cancellation
            }
        }

    private fun createImageUri(): Uri? {
        val imageFileName = "camera_photo$count.png"
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

    fun sendImageToStorage(uri: Uri, callback: (String) -> Unit) {
        if (uri == Uri.EMPTY) {
            callback("")
            return
        }

        val imageRef = storageReference.child("images/$uid/${System.currentTimeMillis()}_image.jpg")

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

    private fun sendDataToFirebase() {
        sendImageToStorage(visitorUri) { visitorUrl ->
            sendImageToStorage(idUri) { idUrl ->
                sendImageToStorage(vechileUri) { vehicleUrl ->
                    val sdfDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val currentDate = sdfDate.format(System.currentTimeMillis())
                    val currentTime = sdfTime.format(System.currentTimeMillis())
                    val reference = database.reference.child("admin").child("security_guards").child(uid.orEmpty())

                    val entryKey = reference.push().key.orEmpty()
                    val dataMap = Visitor(
                        id = entryKey,
                        name = name,
                        securityId = uid,
                        phoneNumber = phone,
                        purpose = purpose,
                        vehicleImageUrl = vehicleUrl,
                        visitorImageUrl = visitorUrl,
                        visitorIdImageUrl = idUrl,
                        inTime = currentTime,
                        dateEntery = currentDate,
                        security = security.name,
                        outTime = ""
                    )

                    reference.child(entryKey).setValue(dataMap)
                        .addOnSuccessListener {
                            view?.findViewById<ProgressBar>(R.id.loadingSpinner)?.visibility = View.GONE
                            val intent = Intent(requireContext(), PdfGeneration::class.java).apply {
                                putExtra("name", name)
                                putExtra("phone", phone)
                                putExtra("purpose", purpose)
                                putExtra("visitorUri", visitorUri)
                                putExtra("currentTime", currentTime)
                                putExtra("currentDate", currentDate)
                            }
                            vechileUri = Uri.EMPTY
                            visitorUri = Uri.EMPTY
                            idUri = Uri.EMPTY
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            view?.findViewById<ProgressBar>(R.id.loadingSpinner)?.visibility = View.GONE
                            sendBtn.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Please register again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddIn().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

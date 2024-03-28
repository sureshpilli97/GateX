package com.example.gatex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import java.io.File
import android.os.Environment
import android.widget.Toast
import android.Manifest
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.*
import java.io.FileOutputStream
import java.io.IOException

class PdfGeneration() : AppCompatActivity() {
    private val REQUESTCODE = 1232
    private lateinit var btnXMLtoPDF: Button
    private lateinit var phone:String

    private val  firebaseAuth = FirebaseAuth.getInstance()
    private val storageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_generator)
        askPermissions()

        var name = intent.getStringExtra("name") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
        var purpose = intent.getStringExtra("purpose") ?: ""
        val visitorUri: Uri = intent.getParcelableExtra<Uri>("visitorUri")!!
        var currentTime = intent.getStringExtra("currentTime") ?: ""
        var currentDate = intent.getStringExtra("currentDate") ?: ""

        val textName = findViewById<TextView>(R.id.textName)
        val textPhoneNumber = findViewById<TextView>(R.id.textPhoneNumber)
        val textPurpose = findViewById<TextView>(R.id.textPurpose)
        val textIntime = findViewById<TextView>(R.id.textIntime)
        val textOuttime = findViewById<TextView>(R.id.textOuttime)
        val textDate = findViewById<TextView>(R.id.textDate)
        val imgVisitor = findViewById<ImageView>(R.id.imageVisitor)

        textName.text = name
        textPhoneNumber.text = phone
        textPurpose.text = purpose
        textIntime.text = currentTime
        imgVisitor.setImageURI(visitorUri)


        val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val parsedTime = sdfTime.parse(currentTime)
        val cal = Calendar.getInstance()
        cal.time = parsedTime
        cal.add(Calendar.HOUR_OF_DAY, 3)
        val outTime = sdfTime.format(cal.time)
        textOuttime.text = outTime

        textDate.text = currentDate



        btnXMLtoPDF = findViewById(R.id.btnSend)
        btnXMLtoPDF.setOnClickListener {
            findViewById<ProgressBar>(R.id.loadingSpinner)?.visibility = View.VISIBLE
            convertXmlToPdf()
        }
    }

    private fun askPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUESTCODE
        )
        Log.d("permission","sucess")
    }

    private fun convertXmlToPdf() {
        val linearLayout: LinearLayout = findViewById(R.id.pdfContent)

        val document = PdfDocument()

        val viewWidth = linearLayout.width
        val viewHeight = linearLayout.height

        val pageInfo = PdfDocument.PageInfo.Builder(viewWidth, viewHeight, 1).create()

        val page = document.startPage(pageInfo)

        val canvas: Canvas = page.canvas

        val paint = Paint()
        paint.color = Color.WHITE

        linearLayout.draw(canvas)
        document.finishPage(page)

        savePdfDocument(document)
    }

    private fun savePdfDocument(document: PdfDocument) {
        val downloadsDir: File? = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        downloadsDir?.let { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileName = "example.pdf"
            val filePath = File(dir, fileName)
            try {
                val fos = FileOutputStream(filePath)
                document.writeTo(fos)
                document.close()
                fos.close()
                val pdfUri = Uri.fromFile(filePath)
                Log.d("vhjk",pdfUri.toString())

                sendTOStorage(pdfUri) { pdfUrl ->
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                            WhatsApp.sendWhatsApp(phone, pdfUrl)
                            findViewById<ProgressBar>(R.id.loadingSpinner)?.visibility = View.GONE
                            Toast.makeText(this@PdfGeneration, "Sent Successfully", Toast.LENGTH_LONG).show()

                            // Optionally, you can navigate to another activity
                            val intent = Intent(this@PdfGeneration, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }



    private fun sendTOStorage(uri: Uri, callback: (String) -> Unit) {

        val uid = firebaseAuth.currentUser?.uid

        if (uri == Uri.EMPTY) {
            callback("")
        }

        val pdfRef = storageReference.child("pdfs/$uid.pdf")

        pdfRef.putFile(uri)
            .addOnSuccessListener { _ ->
                pdfRef.downloadUrl.addOnSuccessListener { uri ->
                    val pdfUri = uri.toString()
                    callback(pdfUri)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "file upload failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException


object WhatsApp {

    fun sendWhatsApp(phone: String, pdfUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val body: RequestBody = FormBody.Builder()
                .add("token", "zb8zj5zujv4zpjnj")
                .add("to", "+91$phone")
                .add("body", pdfUrl)
                .build()

            val request: Request = Request.Builder()
                .url("https://api.ultramsg.com/instance78680/messages/chat")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build()

            try {
                val response: Response = client.newCall(request).execute()
                // Handle response here if needed
            } catch (e: IOException) {
                // Handle error here
                e.printStackTrace()
            }
        }
    }
}

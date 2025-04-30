package com.example.pert8fix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)  // Pastikan layout ini memiliki button dan EditText

        val editTextTweet = findViewById<EditText>(R.id.editTextTweet)  // EditText untuk isi postingan
        val buttonPost = findViewById<Button>(R.id.buttonPost)  // Tombol untuk post

        buttonPost.setOnClickListener {
            val text = editTextTweet.text.toString()

            if (text.isNotBlank()) {
                // Menyimpan postingan ke database
                val userId = 1 // Misal ID user aktif
                simpanPostKeDatabase(userId, text) {
                    // Menampilkan toast sukses
                    Toast.makeText(this, "Post terkirim: $text", Toast.LENGTH_SHORT).show()

                    // Pindah ke ProfileActivity setelah berhasil menyimpan
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("new_post", text)  // Kirim postingan baru ke ProfileActivity
                    startActivity(intent)
                    finish()  // Menutup PostActivity
                }
            } else {
                Toast.makeText(this, "Belum ada tulisannya tuh", Toast.LENGTH_SHORT).show()
            }
        }

        supportActionBar?.title = "Post"
    }

    private fun simpanPostKeDatabase(idPengguna: Int, teks: String, onSuccess: () -> Unit) {
        GlobalScope.launch {
            try {
                val conn = koneksi.connection()  // Pastikan koneksi ini benar
                val sql = "INSERT INTO post (id_pengguna, content) VALUES (?, ?)"
                val stmt = conn?.prepareStatement(sql)
                stmt?.setInt(1, idPengguna)     // ID user
                stmt?.setString(2, teks)        // Isi post
                stmt?.executeUpdate()
                conn?.close()

                // Panggil onSuccess setelah selesai
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

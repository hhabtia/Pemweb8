package com.example.pert8fix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FollowingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_following)

        supportActionBar?.title = "Following"
    }
}

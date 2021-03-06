package com.example.instagramphotovideodownloader

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.instagramphotovideodownloader.adapter.ViewpagerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewpager.adapter = ViewpagerAdapter(supportFragmentManager)
        tab_layout.setupWithViewPager(viewpager)
    }
}
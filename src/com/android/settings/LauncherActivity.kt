package com.android.settings;

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {
    var context: Context ? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this ;
        val intent = Intent(context,Settings::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finish();
    }    

}
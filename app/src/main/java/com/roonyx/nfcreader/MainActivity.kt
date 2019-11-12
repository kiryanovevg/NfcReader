package com.roonyx.nfcreader

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val nfcHelper = NfcHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var count = 0

        nfcHelper.apply {
            initialize(this@MainActivity)
            uidLiveData.observe(this@MainActivity, Observer {
                val str = "count: ${++count}\nrfid: $it"
                textView.text = str
            })
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.also { nfcHelper.handleIntent(intent) }
    }
}
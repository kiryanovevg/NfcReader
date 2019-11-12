package com.roonyx.nfcreader

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*

class NfcHelper {

    private val uid = MutableLiveData<String>()
    val uidLiveData: LiveData<String> get() = uid

    fun initialize(fragmentActivity: FragmentActivity) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(fragmentActivity)
        val techLists = arrayOf(arrayOf(Ndef::class.java.name))
        val pendingIntent = PendingIntent.getActivity(
            fragmentActivity, 0,
            Intent(fragmentActivity, fragmentActivity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            0
        )

        val intentFilter = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)

        try {
            intentFilter.addDataType(MIME_TYPE_NFC)
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("Error while adding data type", e)
        }

        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        val filters = arrayOf(intentFilter)

        fragmentActivity.lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                nfcAdapter.enableForegroundDispatch(
                    fragmentActivity, pendingIntent, filters, techLists
                )
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                nfcAdapter.disableForegroundDispatch(fragmentActivity)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                fragmentActivity.lifecycle.removeObserver(this)
            }
        })
    }

    fun handleIntent(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            uid.value = byteArrayToHexString(tag.id)
        }
    }

    private fun byteArrayToHexString(array: ByteArray) = StringBuilder().apply {
        val hex = arrayOf("0", "1", "2", "3", "4", "5", "6",
            "7", "8", "9", "A", "B", "C", "D", "E", "F")

        repeat(array.size) { i ->
            if (isNotEmpty()) append(":")
            val input = array[i].toInt() and 0xff
            var j = input shr 4 and 0x0f
            append(hex[j])
            j = input and 0x0f
            append(hex[j])
        }
    }.toString()

    companion object {
        const val MIME_TYPE_NFC = "application/com.roonyx.nfcreader.nfc"
    }
}
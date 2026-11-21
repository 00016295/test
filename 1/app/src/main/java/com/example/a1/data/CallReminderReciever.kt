package com.example.a1.data
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallReminderReciever : BroadcastReceiver() {
    companion object{
        const val  EXTRA_CONTACT_ID = "contact_id"
        fun createIntent(context: Context, contactId: Long): Intent{
            return Intent(context, CallReminderReciever::class.java).apply{
                action = "${context.packageName}.ACTION_REMIND"
                putExtra(EXTRA_CONTACT_ID, contactId)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        val contactId = intent.getLongExtra(EXTRA_CONTACT_ID, -1L)
        if (contactId != -1L) {
            val serviceIntent = Intent(context , CallReminderService::class.java).apply {
                putExtra(EXTRA_CONTACT_ID,contactId)
            }
            context?.startService(serviceIntent)
        }
    }
}
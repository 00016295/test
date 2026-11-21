package com.example.a1.data
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import  android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
class CallReminderService : Service(){
    companion object{
        private const val TAG = "CallReminderService"
        private const val CHANNEL_ID = "call_reminder_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"onStartCommand")
        val contactId = intent?.getLongExtra(CallReminderReciever.EXTRA_CONTACT_ID , -1L)?:-1L
        val contact = ContactsRepository.getById(contactId)

        if(contact == null){
            Log.d(TAG,"Contact with id=$contactId not found")
            stopSelf(startId)
            return START_NOT_STICKY
        }
        createNotificationChannel()

        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${contact.phone}")
        }

        val flagsPending = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }
        else{
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            contactId.toInt(),
            dialIntent,
            flagsPending
        )

        val notification = NotificationCompat.Builder(this,CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Call:${contact.name}")
            .setContentText(contact.phone)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)

        notificationManager?.notify(contactId.toInt(), notification)

        stopSelf(startId)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

}
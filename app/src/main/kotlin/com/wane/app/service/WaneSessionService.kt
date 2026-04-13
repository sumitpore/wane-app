package com.wane.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class WaneSessionService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        TODO("Not yet implemented")
    }
}

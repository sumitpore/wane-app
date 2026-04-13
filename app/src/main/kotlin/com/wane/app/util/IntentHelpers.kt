package com.wane.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast

object IntentHelpers {

    fun openDialer(context: Context, number: String? = null) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            if (!number.isNullOrBlank()) {
                data = Uri.parse("tel:${Uri.encode(number)}")
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.safeStartActivity(intent)
    }

    fun openContacts(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = ContactsContract.Contacts.CONTENT_URI
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.safeStartActivity(intent)
    }

    fun openSms(context: Context, number: String? = null) {
        val uri = if (!number.isNullOrBlank()) {
            Uri.parse("smsto:${Uri.encode(number)}")
        } else {
            Uri.parse("smsto:")
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.safeStartActivity(intent)
    }

    private fun Context.safeStartActivity(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No app found", Toast.LENGTH_SHORT).show()
        }
    }
}

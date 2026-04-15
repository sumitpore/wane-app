package com.wane.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast

object IntentHelpers {
    fun openDialer(context: Context) {
        val intent =
            Intent(Intent.ACTION_DIAL).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.safeStartActivity(intent)
    }

    fun openContacts(context: Context) {
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                data = ContactsContract.Contacts.CONTENT_URI
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.safeStartActivity(intent)
    }

    fun openSms(
        context: Context,
        number: String? = null,
    ) {
        if (!number.isNullOrBlank()) {
            val intent =
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${Uri.encode(number)}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.safeStartActivity(intent)
            return
        }

        // No number — open the conversation list rather than the compose screen.
        // CATEGORY_APP_MESSAGING launches the SMS app's main activity, giving a
        // more natural entry point that avoids auto-focusing the text field (and
        // thus avoids the keyboard from appearing immediately).
        val messagingIntent =
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MESSAGING)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        if (!context.tryStartActivity(messagingIntent)) {
            val fallbackIntent =
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.safeStartActivity(fallbackIntent)
        }
    }

    private fun Context.tryStartActivity(intent: Intent): Boolean =
        try {
            startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }

    private fun Context.safeStartActivity(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No app found", Toast.LENGTH_SHORT).show()
        }
    }
}

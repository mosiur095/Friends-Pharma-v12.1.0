package com.friendspharma.app.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import java.time.LocalDateTime

object Utils {

    private val bengaliDayName: HashMap<Int, String> = hashMapOf(
        6 to "শনিবার", 7 to "রবিবার", 1 to "সোমবার", 2 to "মঙ্গলবার", 3 to "বুধবার",
        4 to "বৃহস্পতিবার", 5 to "শুক্রবার"
    )

    private val bengaliMonthName: HashMap<Int, String> = hashMapOf(
        1 to "জানুয়ারি", 2 to "ফেব্রুয়ারি", 3 to "মার্চ", 4 to "এপ্রিল", 5 to "মে",
        6 to "জুন", 7 to "জুলাই", 8 to "আগস্ট", 9 to "সেপ্টেম্বর", 10 to "অক্টোবর",
        11 to "নভেম্বর", 12 to "ডিসেম্বর"
    )

    private val bengaliNumberChar: HashMap<Char, String> = hashMapOf(
        '1' to "১", '2' to "২", '3' to "৩", '4' to "৪", '5' to "৫",
        '6' to "৬", '7' to "৭", '8' to "৮", '9' to "৯", '0' to "০"
    )

    private fun bengaliNumberString(number: String): String {
        var bangla = ""
        for (ch in number) {
            bangla += bengaliNumberChar[ch]
        }
        return bangla
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBengaliDate(diff: Int = 0): String {
        val now = LocalDateTime.now().plusDays(diff.toLong())
        return bengaliDayName[now.dayOfWeek.value] + ", " + bengaliMonthName[now.monthValue] + " " + bengaliNumberString(
            now.dayOfMonth.toString()
        ) + ", " + bengaliNumberString(now.year.toString())
    }

    fun openWhatsAppChat(context: Context, phoneNumber: String, message: String = "") {
        try {
            // Construct the WhatsApp URI with the phone number and optional message
            val packageName = "com.whatsapp"
            val uri =
                "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}".toUri()

            // Create an Intent with ACTION_VIEW to open the URI
            val intent = Intent(Intent.ACTION_VIEW, uri)

            // Set the package to ensure WhatsApp is opened specifically
            intent.setPackage(packageName)

            // Check if WhatsApp is installed before starting the activity
            if (isAppInstalled(context, packageName)) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "WhatsApp is not installed.", Toast.LENGTH_LONG).show()

                try {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "market://details?id=$packageName".toUri()
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://play.google.com/store/apps/details?id=$packageName".toUri()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error opening WhatsApp: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    // Helper function to check if an app is installed
    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

}
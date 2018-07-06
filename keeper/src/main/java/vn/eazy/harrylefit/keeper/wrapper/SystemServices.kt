package vn.eazy.harrylefit.keeper.wrapper

import android.content.Context
import android.os.Build
import android.provider.Settings


object SystemServices {
    fun hasMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun getModel(): String {
        return Build.MODEL
    }

    fun getManufactory(): String {
        return Build.MANUFACTURER
    }

    fun getOS(): String {
        return Build.VERSION.RELEASE
    }

    fun getOsVersion(): String {
        return Build.VERSION.SDK_INT.toString()
    }

    fun getSecureId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ANDROID_ID)
    }
}
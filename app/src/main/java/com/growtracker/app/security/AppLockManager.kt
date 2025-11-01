package com.growtracker.app.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest
import java.security.SecureRandom

object AppLockManager {
    private const val PREF_NAME = "app_lock_prefs"
    private const val KEY_LOCK_ENABLED = "lock_enabled"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_PIN_SALT = "pin_salt"
    private const val KEY_PIN_HASH = "pin_hash"

    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked

    private fun prefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            @Suppress("DEPRECATION")
            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("AppLock", "Falling back to unencrypted SharedPreferences: ${e.message}")
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun isLockEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_LOCK_ENABLED, false)
    fun isBiometricEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setLockEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_LOCK_ENABLED, enabled).apply()
        if (!enabled) _locked.value = false
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun hasPin(context: Context): Boolean = !prefs(context).getString(KEY_PIN_HASH, null).isNullOrEmpty()

    fun setPin(context: Context, pin: String) {
        val normalized = pin.filter { it.isDigit() }.take(4)
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hash = sha256(salt, normalized)
        prefs(context).edit()
            .putString(KEY_PIN_SALT, saltB64)
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val p = prefs(context)
        val saltB64 = p.getString(KEY_PIN_SALT, null) ?: return false
        val hashStored = p.getString(KEY_PIN_HASH, null) ?: return false
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val hash = sha256(salt, pin)
        val ok = hash.equals(hashStored, ignoreCase = true)
        if (ok) _locked.value = false
        return ok
    }

    private fun sha256(salt: ByteArray, pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        md.update(pin.toByteArray())
        val bytes = md.digest()
        return bytes.joinToString("") { ((it.toInt() and 0xFF).toString(16)).padStart(2, '0') }
    }

    fun canUseBiometric(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        val res = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        return res == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun onForeground(context: Context) {
        if (isLockEnabled(context)) {
            // Only lock if there's at least one method
            if (hasPin(context) || isBiometricEnabled(context)) {
                _locked.value = true
            } else {
                _locked.value = false
            }
        } else {
            _locked.value = false
        }
    }

    fun unlock() { _locked.value = false }
}

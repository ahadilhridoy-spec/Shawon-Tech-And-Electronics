package com.example.data.remote

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.lang.ref.WeakReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

class FirebaseAuthManager(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn = _isSigningIn.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var activeActivityRef: WeakReference<Activity?> = WeakReference(null)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
        val app = (context.applicationContext as? Application) ?: (context as? Application)
        app?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                activeActivityRef = WeakReference(activity)
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activeActivityRef = WeakReference(activity)
            }
            override fun onActivityStarted(activity: Activity) {
                activeActivityRef = WeakReference(activity)
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (activeActivityRef.get() === activity) {
                    activeActivityRef.clear()
                }
            }
        })
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    suspend fun signInWithGoogle(callingContext: Context? = null) {
        _isSigningIn.value = true
        _errorMessage.value = null
        try {
            val serverClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                ""
            }

            if (serverClientId.isBlank()) {
                _errorMessage.value = "Google Web Client ID পাওয়া যায়নি। অনুগ্রহ করে কনফিগারেশন চেক করুন।"
                return
            }

            // Credential Manager requires an Activity context to display the account picker interface
            val resolvedActivity: Activity? = callingContext?.findActivity()
                ?: activeActivityRef.get()
                ?: (context as? Activity)
                ?: context.findActivity()

            val targetContext: Context = resolvedActivity ?: context

            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = credentialManager.getCredential(targetContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                _currentUser.value = authResult.user
                _errorMessage.value = null
            } else {
                Log.w("FirebaseAuthManager", "Unexpected credential type received: ${credential::class.java.name}")
                _errorMessage.value = "অপ্রত্যাশিত ক্রেডেনশিয়াল তথ্য পাওয়া গেছে। অনুগ্রহ করে আবার চেষ্টা করুন।"
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i("FirebaseAuthManager", "User cancelled Google Sign-in flow")
            _errorMessage.value = null
        } catch (e: GetCredentialException) {
            Log.e("FirebaseAuthManager", "Credential Manager error: ${e.type}", e)
            val message = when {
                e is NoCredentialException || e.message?.contains("28433") == true || e.message?.contains("No credentials") == true ->
                    "ডিভাইসে কোনো লিঙ্কড গুগল একাউন্ট নেই। অনুগ্রহ করে ডিভাইসের Settings > Accounts থেকে Google Account যোগ করুন।"
                e.message?.contains("28436") == true || e.message?.contains("Google Play Services") == true ->
                    "গুগল প্লে সার্ভিসেস আপডেট বা অ্যাক্টিভ নেই। অনুগ্রহ করে প্লে সার্ভিসেস আপডেট করুন।"
                e.message?.contains("16") == true || e.message?.contains("cancelled") == true ->
                    null
                else ->
                    "গুগল সাইন-ইন ইন্টারফেসে সমস্যা হয়েছে: ${e.localizedMessage ?: "একউন্ট নির্বাচন সম্পন্ন হয়নি"}"
            }
            _errorMessage.value = message
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Sign-in exception", e)
            _errorMessage.value = "সাইন-ইন ব্যর্থ হয়েছে: ${e.localizedMessage ?: "সংযোগ সমস্যা"}"
        } finally {
            _isSigningIn.value = false
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _errorMessage.value = null
    }
}

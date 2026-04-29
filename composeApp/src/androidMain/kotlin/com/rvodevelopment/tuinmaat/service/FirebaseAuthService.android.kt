package com.rvodevelopment.tuinmaat.service

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.rvodevelopment.tuinmaat.util.ActivityProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await

actual suspend fun getGoogleIdToken(): String? {
    val activity = ActivityProvider.getCurrentActivity() ?: return null
    
    val resId = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)
    if (resId == 0) return null
    
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(activity.getString(resId))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(activity, gso)
    
    // Probeer eerst stil in te loggen
    return try {
        val account = try {
            googleSignInClient.silentSignIn().await()
        } catch (e: Exception) {
            null
        }
        
        if (account?.idToken != null) {
            account.idToken
        } else {
            // Interactieve login
            val deferred = CompletableDeferred<String?>()
            val requestCode = 9001
            
            ActivityProvider.setActivityResultCallback { req, res, data ->
                if (req == requestCode) {
                    try {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        val interactiveAccount = task.getResult(ApiException::class.java)
                        deferred.complete(interactiveAccount?.idToken)
                    } catch (e: Exception) {
                        deferred.complete(null)
                    } finally {
                        ActivityProvider.setActivityResultCallback(null)
                    }
                }
            }
            
            activity.startActivityForResult(googleSignInClient.signInIntent, requestCode)
            deferred.await()
        }
    } catch (e: Exception) {
        null
    }
}

package com.example.personalfinance.firebase

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

class FirebaseAuthHelper {

    companion object {
        private const val TAG = "FirebaseAuthHelper"
        const val RC_GOOGLE_SIGN_IN = 9001
    }

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null
    private var facebookCallbackManager: CallbackManager? = null

    val currentUser: FirebaseUser? get() = mAuth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null

    fun signOut() {
        mAuth.signOut()
        googleSignInClient?.signOut()
        LoginManager.getInstance().logOut()
    }

    // ============ EMAIL/PASSWORD ============

    fun signIn(email: String, password: String, callback: FirebaseAuthCallback) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                val user = mAuth.currentUser
                if (task.isSuccessful && user != null) {
                    callback.onSuccess(user)
                } else {
                    callback.onFailure(task.exception ?: Exception("Đăng nhập thất bại"))
                }
            }
    }

    fun signUp(email: String, password: String, fullName: String, callback: FirebaseAuthCallback) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                val user = mAuth.currentUser
                if (task.isSuccessful && user != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    // Proactively proceed even if profile display name update has soft errors
                    user.updateProfile(profileUpdates).addOnCompleteListener {
                        callback.onSuccess(user)
                    }
                } else {
                    callback.onFailure(task.exception ?: Exception("Đăng ký thất bại"))
                }
            }
    }

    // ============ GOOGLE SIGN-IN ============

    /**
     * Initialize Google Sign-In client. Must be called before signInWithGoogle().
     * @param activity The Activity context
     * @param webClientId The Web Client ID from google-services.json (client_type: 3)
     */
    fun initGoogleSignIn(activity: Activity, webClientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    /**
     * Launch the Google Sign-In intent. Handle the result in onActivityResult().
     */
    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient?.signInIntent
            ?: throw IllegalStateException("Google Sign-In not initialized. Call initGoogleSignIn() first.")
    }

    /**
     * Handle Google Sign-In result from onActivityResult().
     * @param data The Intent data from onActivityResult
     * @param callback Callback for success/failure
     */
    fun handleGoogleSignInResult(data: Intent?, callback: FirebaseAuthCallback) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                Log.d(TAG, "Google Sign-In successful, authenticating with Firebase...")
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuthWithCredential(credential, callback)
            } else {
                callback.onFailure(Exception("Không lấy được token từ Google"))
            }
        } catch (e: ApiException) {
            Log.w(TAG, "Google Sign-In failed, statusCode=${e.statusCode}", e)
            val errorMsg = when (e.statusCode) {
                CommonStatusCodes.NETWORK_ERROR -> "Không kết nối được tới Google. Kiểm tra Internet, Google Play Services hoặc thử lại sau."
                12501 -> "Bạn đã hủy đăng nhập Google"
                12500 -> "Đăng nhập Google thất bại. Kiểm tra SHA-1 fingerprint trên Firebase Console."
                10 -> "Lỗi cấu hình: Kiểm tra SHA-1 và Web Client ID trên Firebase Console."
                else -> "Đăng nhập Google thất bại (mã lỗi: ${e.statusCode})"
            }
            callback.onFailure(Exception(errorMsg))
        }
    }

    // ============ FACEBOOK LOGIN ============

    /**
     * Initialize Facebook Login callback manager.
     * @return CallbackManager to be used in onActivityResult
     */
    fun initFacebookLogin(callback: FirebaseAuthCallback): CallbackManager {
        val manager = CallbackManager.Factory.create()
        facebookCallbackManager = manager

        LoginManager.getInstance().registerCallback(manager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d(TAG, "Facebook Login successful, authenticating with Firebase...")
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                firebaseAuthWithCredential(credential, callback)
            }

            override fun onCancel() {
                Log.d(TAG, "Facebook Login cancelled")
                callback.onFailure(Exception("Bạn đã hủy đăng nhập Facebook"))
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "Facebook Login error", error)
                callback.onFailure(Exception("Đăng nhập Facebook thất bại: ${error.message}"))
            }
        })

        return manager
    }

    /**
     * Launch Facebook Login flow.
     * @param activity The Activity to launch from
     */
    fun signInWithFacebook(activity: Activity) {
        LoginManager.getInstance().logInWithReadPermissions(activity, listOf("email", "public_profile"))
    }

    fun getFacebookCallbackManager(): CallbackManager? = facebookCallbackManager

    // ============ SHARED CREDENTIAL AUTH ============

    /**
     * Authenticate with Firebase using an AuthCredential (Google/Facebook).
     */
    private fun firebaseAuthWithCredential(
        credential: com.google.firebase.auth.AuthCredential,
        callback: FirebaseAuthCallback
    ) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                val user = mAuth.currentUser
                if (task.isSuccessful && user != null) {
                    Log.d(TAG, "Firebase credential auth successful")
                    callback.onSuccess(user)
                } else {
                    Log.w(TAG, "Firebase credential auth failed", task.exception)
                    callback.onFailure(task.exception ?: Exception("Xác thực Firebase thất bại"))
                }
            }
    }
}

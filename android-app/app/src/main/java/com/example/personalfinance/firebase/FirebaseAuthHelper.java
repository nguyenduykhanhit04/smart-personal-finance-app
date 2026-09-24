package com.example.personalfinance.firebase;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Arrays;

public class FirebaseAuthHelper {

    private static final String TAG = "FirebaseAuthHelper";
    public static final int RC_GOOGLE_SIGN_IN = 9001;

    private final FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager facebookCallbackManager;

    public FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public void signOut() {
        mAuth.signOut();
        // Also sign out from Google if configured
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
        // Also sign out from Facebook
        LoginManager.getInstance().logOut();
    }

    // ============ EMAIL/PASSWORD ============

    public void signIn(String email, String password, FirebaseAuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException() != null ? 
                                task.getException() : new Exception("Đăng nhập thất bại"));
                    }
                });
    }

    public void signUp(String email, String password, String fullName, FirebaseAuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Update display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build();
                        
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    // Proactively proceed even if profile display name update has soft errors
                                    callback.onSuccess(user);
                                });
                    } else {
                        callback.onFailure(task.getException() != null ? 
                                task.getException() : new Exception("Đăng ký thất bại"));
                    }
                });
    }

    // ============ GOOGLE SIGN-IN ============

    /**
     * Initialize Google Sign-In client. Must be called before signInWithGoogle().
     * @param activity The Activity context
     * @param webClientId The Web Client ID from google-services.json (client_type: 3)
     */
    public void initGoogleSignIn(Activity activity, String webClientId) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    /**
     * Launch the Google Sign-In intent. Handle the result in onActivityResult().
     * @param activity The Activity to launch from
     */
    public Intent getGoogleSignInIntent() {
        if (googleSignInClient == null) {
            throw new IllegalStateException("Google Sign-In not initialized. Call initGoogleSignIn() first.");
        }
        return googleSignInClient.getSignInIntent();
    }

    /**
     * Handle Google Sign-In result from onActivityResult().
     * @param data The Intent data from onActivityResult
     * @param callback Callback for success/failure
     */
    public void handleGoogleSignInResult(Intent data, FirebaseAuthCallback callback) {
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult(ApiException.class);

            if (account != null && account.getIdToken() != null) {
                Log.d(TAG, "Google Sign-In successful, authenticating with Firebase...");
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                firebaseAuthWithCredential(credential, callback);
            } else {
                callback.onFailure(new Exception("Không lấy được token từ Google"));
            }
        } catch (ApiException e) {
            Log.w(TAG, "Google Sign-In failed, statusCode=" + e.getStatusCode(), e);
            String errorMsg;
            switch (e.getStatusCode()) {
                case CommonStatusCodes.NETWORK_ERROR:
                    errorMsg = "Không kết nối được tới Google. Kiểm tra Internet, Google Play Services hoặc thử lại sau.";
                    break;
                case 12501:
                    errorMsg = "Bạn đã hủy đăng nhập Google";
                    break;
                case 12500:
                    errorMsg = "Đăng nhập Google thất bại. Kiểm tra SHA-1 fingerprint trên Firebase Console.";
                    break;
                case 10:
                    errorMsg = "Lỗi cấu hình: Kiểm tra SHA-1 và Web Client ID trên Firebase Console.";
                    break;
                default:
                    errorMsg = "Đăng nhập Google thất bại (mã lỗi: " + e.getStatusCode() + ")";
            }
            callback.onFailure(new Exception(errorMsg));
        }
    }

    // ============ FACEBOOK LOGIN ============

    /**
     * Initialize Facebook Login callback manager.
     * @return CallbackManager to be used in onActivityResult
     */
    public CallbackManager initFacebookLogin(FirebaseAuthCallback callback) {
        facebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "Facebook Login successful, authenticating with Firebase...");
                        AccessToken token = loginResult.getAccessToken();
                        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
                        firebaseAuthWithCredential(credential, callback);
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Facebook Login cancelled");
                        callback.onFailure(new Exception("Bạn đã hủy đăng nhập Facebook"));
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.e(TAG, "Facebook Login error", error);
                        callback.onFailure(new Exception("Đăng nhập Facebook thất bại: " + error.getMessage()));
                    }
                });

        return facebookCallbackManager;
    }

    /**
     * Launch Facebook Login flow.
     * @param activity The Activity to launch from
     */
    public void signInWithFacebook(Activity activity) {
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("email", "public_profile"));
    }

    /**
     * Get the Facebook CallbackManager for use in onActivityResult.
     */
    public CallbackManager getFacebookCallbackManager() {
        return facebookCallbackManager;
    }

    // ============ SHARED CREDENTIAL AUTH ============

    /**
     * Authenticate with Firebase using an AuthCredential (Google/Facebook).
     */
    private void firebaseAuthWithCredential(AuthCredential credential, FirebaseAuthCallback callback) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        Log.d(TAG, "Firebase credential auth successful");
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        Log.w(TAG, "Firebase credential auth failed", task.getException());
                        callback.onFailure(task.getException() != null ?
                                task.getException() : new Exception("Xác thực Firebase thất bại"));
                    }
                });
    }
}

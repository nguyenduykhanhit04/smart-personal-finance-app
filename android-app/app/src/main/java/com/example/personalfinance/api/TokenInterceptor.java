package com.example.personalfinance.api;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Add ngrok bypass header to prevent browser warning on dynamic/API hosts
        Request.Builder requestBuilder = originalRequest.newBuilder()
                .header("ngrok-skip-browser-warning", "true");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return chain.proceed(requestBuilder.build());
        }
        
        try {
            // Synchronously block until the Firebase ID Token task resolves (runs on OkHttp background thread)
            Task<GetTokenResult> tokenTask = user.getIdToken(false);
            GetTokenResult result = Tasks.await(tokenTask);
            String token = result.getToken();
            
            if (token != null) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        
        return chain.proceed(requestBuilder.build());
    }
}

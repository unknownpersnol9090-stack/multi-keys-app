package app.lovable.multihackskey;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.PermissionRequest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends BridgeActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure WebView settings for optimal performance
        configureWebView();
        
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }
        
        // Initialize FCM token
        initializeFCM();
    }

    private void configureWebView() {
        // Get the WebView from the bridge
        webView = getBridge().getWebView();
        
        if (webView != null) {
            // Enable JavaScript
            webView.getSettings().setJavaScriptEnabled(true);
            
            // Enable DOM storage for better performance
            webView.getSettings().setDomStorageEnabled(true);
            
            // Allow file access for uploads
            webView.getSettings().setAllowFileAccess(true);
            
            // Enable caching for faster load times
            webView.getSettings().setAppCacheEnabled(true);
            webView.getSettings().setDatabaseEnabled(true);
            
            // Improve performance
            webView.getSettings().setRenderPriority(WebView.RENDERER_PRIORITY_HIGH);
            webView.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER);
            
            // Set up WebChromeClient for file uploads and permissions
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onPermissionRequest(PermissionRequest request) {
                    String[] resources = request.getResources();
                    for (String resource : resources) {
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) {
                            if (ContextCompat.checkSelfPermission(MainActivity.this, 
                                    android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                request.grant(new String[]{resource});
                            } else {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.RECORD_AUDIO},
                                        PERMISSION_REQUEST_CODE);
                            }
                        } else if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) {
                            if (ContextCompat.checkSelfPermission(MainActivity.this, 
                                    android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                request.grant(new String[]{resource});
                            }
                        }
                    }
                }

                @Override
                public boolean onShowFileChooser(WebView webView, 
                        android.webkit.ValueCallback<android.net.Uri[]> filePathCallback,
                        WebChromeClient.FileChooserParams fileChooserParams) {
                    // Handle file uploads
                    return true;
                }
            });
            
            // Register AndroidBridge for FCM communication
            webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        }
    }

    private void initializeFCM() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    if (webView != null) {
                        webView.evaluateJavascript(
                            "window.setFCMToken('" + token + "')",
                            null
                        );
                    }
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                }
            }
        }
    }

    /**
     * JavaScript interface for FCM token management
     */
    public class AndroidBridge {
        @android.webkit.JavascriptInterface
        public String getFCMToken() {
            // This would need to be cached from initialization
            // For now, return null - token is set via setFCMToken
            return null;
        }

        @android.webkit.JavascriptInterface
        public void requestFCMToken() {
            FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && webView != null) {
                        String token = task.getResult();
                        webView.evaluateJavascript(
                            "window.setFCMToken('" + token + "')",
                            null
                        );
                    }
                });
        }
    }
}

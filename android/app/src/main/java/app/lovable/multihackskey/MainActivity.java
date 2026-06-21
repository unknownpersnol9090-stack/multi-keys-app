package app.lovable.multihackskey;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends BridgeActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureWebView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE
                );
            }
        }

        initializeFCM();
    }

    private void configureWebView() {

        webView = getBridge().getWebView();

        if (webView == null) {
            return;
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDatabaseEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(PermissionRequest request) {

                String[] resources = request.getResources();

                for (String resource : resources) {

                    if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) {

                        if (ContextCompat.checkSelfPermission(
                                MainActivity.this,
                                android.Manifest.permission.RECORD_AUDIO)
                                == PackageManager.PERMISSION_GRANTED) {

                            request.grant(new String[]{resource});
                        }

                    } else if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) {

                        if (ContextCompat.checkSelfPermission(
                                MainActivity.this,
                                android.Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {

                            request.grant(new String[]{resource});
                        }
                    }
                }
            }
        });

        webView.addJavascriptInterface(
                new AndroidBridge(),
                "AndroidBridge"
        );
    }

    private void initializeFCM() {

        FirebaseMessaging.getInstance()
                .getToken()
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
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );
    }

    public class AndroidBridge {

        @android.webkit.JavascriptInterface
        public String getFCMToken() {
            return null;
        }

        @android.webkit.JavascriptInterface
        public void requestFCMToken() {

            FirebaseMessaging.getInstance()
                    .getToken()
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

package com.example.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.InputStream;

public class MainActivity extends Activity {

    private WebView mWebView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.activity_main_webview);
        WebSettings webSettings = mWebView.getSettings();

        // 1. ENABLE FEATURES
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        // 2. COOKIES
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);

        // 3. THE CLIENT
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                
                // A. REDIRECT: Fixes the white screen issue
                if (url.contains("/dashboard") && !url.contains("calendar")) {
                    Toast.makeText(MainActivity.this, "Aligning Satellite...", Toast.LENGTH_SHORT).show();
                    view.loadUrl("https://app.tokportal.com/account-manager/calendar");
                    return;
                }

                // B. INJECT: Load the Full Dashboard from the file
                if (url.contains("/calendar")) {
                    injectDashboardScript();
                }
            }
        });

        // 4. LAUNCH
        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    private void injectDashboardScript() {
        try {
            // Try to load the big external file
            InputStream inputStream = getAssets().open("dashboard-injector.js");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = new String(buffer);
            mWebView.evaluateJavascript(encoded, null);
            
        } catch (Exception e) {
            // FALLBACK: If the file is missing, load the "Lite" version so the app doesn't break
            Toast.makeText(this, "Full UI File Missing! Loading Backup...", Toast.LENGTH_LONG).show();
            injectLiteBackup();
        }
    }

    // This is the "Lite" code you currently have, kept as a backup
    private void injectLiteBackup() {
        String js = "javascript:(function() { " +
                "if(document.getElementById('cyber-root')) return;" +
                "var style = document.createElement('style');" +
                "style.innerHTML = `body > *:not(#cyber-root) { display: none !important; } #cyber-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:white; z-index:99999; display:flex; align-items:center; justify-content:center; }`;" +
                "document.head.appendChild(style);" +
                "var root = document.createElement('div'); root.id = 'cyber-root'; root.innerHTML = '<h1>BACKUP SYSTEM ONLINE</h1>'; document.body.appendChild(root);" +
                "})()";
        mWebView.loadUrl(js);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }
    
    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) mWebView.goBack();
        else super.onBackPressed();
    }
}

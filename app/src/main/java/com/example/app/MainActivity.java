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

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                
                // 1. AUTO-PILOT: If we land on 'Dashboard', redirect to 'Calendar'
                // This fixes the issue of landing on a page that our script can't read.
                if (url.contains("/dashboard")) {
                    Toast.makeText(MainActivity.this, "Redirecting to Mission Control...", Toast.LENGTH_SHORT).show();
                    view.loadUrl("https://app.tokportal.com/account-manager/calendar");
                    return; // Stop here, wait for calendar to load
                }

                // 2. THE INJECTION: Only run when we finally hit the Calendar
                if (url.contains("/calendar")) {
                    injectDashboardScript();
                }
            }
        });

        // Start by trying to go to calendar (Login might override this, but our Auto-Pilot will catch it)
        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    private void injectDashboardScript() {
        try {
            InputStream inputStream = getAssets().open("dashboard-injector.js");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = new String(buffer);
            mWebView.evaluateJavascript(encoded, null);
            
            // Confirm it worked
            Toast.makeText(this, "Cyberpunk UI Online", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            // If this pops up, the file is definitely missing/misnamed
            Toast.makeText(this, "Error: dashboard-injector.js missing!", Toast.LENGTH_LONG).show();
        }
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

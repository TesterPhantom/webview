package com.example.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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

        // 3. THE "AUTO-PILOT" CLIENT
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                
                // A. REDIRECT: If on Dashboard, go to Calendar
                if (url.contains("/dashboard")) {
                    Toast.makeText(MainActivity.this, "Navigating to Mission Control...", Toast.LENGTH_SHORT).show();
                    view.loadUrl("https://app.tokportal.com/account-manager/calendar");
                    return;
                }

                // B. INJECT: If on Calendar, RUN THE CODE
                if (url.contains("/calendar")) {
                    injectCyberpunkUI(view);
                }
            }
        });

        // 4. START ENGINE
        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    // --- THE EMBEDDED CYBERPUNK CODE ---
    // No external files needed. This string IS the interface.
    private void injectCyberpunkUI(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        
        // 1. CHECK: Don't inject twice
        js.append("  if(document.getElementById('cyber-root')) return;");

        // 2. DATA SCRAPING (Simplified for reliability)
        js.append("  var count = 0;");
        js.append("  var rows = document.querySelectorAll('.grid.grid-cols-8.border-b');");
        js.append("  if(rows.length > 0) {");
        js.append("    var day = new Date().getDay(); var col = (day===0)?7:day;");
        js.append("    rows.forEach(function(row) {");
        js.append("      var txt = row.children[col] ? row.children[col].innerText.toLowerCase() : '';");
        js.append("      if(txt.includes('scheduled')) count++;");
        js.append("    });");
        js.append("  }");

        // 3. CSS STYLES (Dark Mode)
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    body > *:not(#cyber-root) { display: none !important; }"); // HIDE OLD SITE
        js.append("    #cyber-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:white; z-index:99999; font-family:sans-serif; overflow-y:auto; }");
        js.append("    .cy-header { padding: 20px; display:flex; justify-content:space-between; align-items:center; }");
        js.append("    .cy-title { font-size: 24px; font-weight:bold; letter-spacing:1px; }");
        js.append("    .cy-card { background: #13131f; margin: 20px; padding: 20px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.1); }");
        js.append("    .cy-stat { font-size: 36px; font-weight:bold; color: #00f3ff; }");
        js.append("    .cy-label { color: #8b9bb4; font-size: 14px; }");
        js.append("    .cy-btn { width:100%; padding:15px; background:transparent; border:1px solid #00f3ff; color:#00f3ff; font-weight:bold; margin-top:10px; border-radius:8px; }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // 4. HTML STRUCTURE
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cyber-root';");
        js.append("  root.innerHTML = `");
        js.append("    <div class='cy-header'>");
        js.append("      <div class='cy-title'>COMMAND<br>CENTER</div>");
        js.append("      <div style='color:#00ff9d; font-size:12px;'>ONLINE</div>");
        js.append("    </div>");
        
        js.append("    <div class='cy-card'>");
        js.append("      <div class='cy-label'>ACTIVE MISSIONS</div>");
        js.append("      <div class='cy-stat'>` + count + `</div>"); // INSERT COUNT
        js.append("    </div>");

        js.append("    <div class='cy-card' style='border-left: 4px solid #bd00ff;'>");
        js.append("      <div class='cy-label'>TOTAL XP</div>");
        js.append("      <div class='cy-stat' style='color:#bd00ff'>1,250</div>");
        js.append("    </div>");
        
        js.append("    <div style='padding:20px;'>");
        js.append("      <button class='cy-btn' onclick='location.reload()'>REFRESH SYSTEMS</button>");
        js.append("    </div>");
        js.append("  `;");
        
        js.append("  document.body.appendChild(root);");
        js.append("})()");

        // 5. INJECT
        view.loadUrl(js.toString());
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

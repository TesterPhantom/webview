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

        // 3. THE NAVIGATOR
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                
                // REDIRECT: If we land on Dashboard, go to Calendar
                if (url.contains("/dashboard") && !url.contains("calendar")) {
                    Toast.makeText(MainActivity.this, "Rerouting to Command Deck...", Toast.LENGTH_SHORT).show();
                    view.loadUrl("https://app.tokportal.com/account-manager/calendar");
                    return;
                }

                // INJECT: If we are on Calendar, load the FULL UI
                if (url.contains("/calendar")) {
                    injectFullCyberpunkUI(view);
                }
            }
        });

        // 4. LAUNCH
        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    // --- THE FULL DASHBOARD EMBEDDED ---
    // This looks messy here, but it guarantees the UI loads perfectly on your phone.
    private void injectFullCyberpunkUI(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        
        // Safety: Don't run twice
        js.append("  if(document.getElementById('cyber-root')) return;");

        // 1. DATA SCRAPING (Get real mission count)
        js.append("  var count = 0;");
        js.append("  var rows = document.querySelectorAll('.grid.grid-cols-8.border-b');");
        js.append("  if(rows.length > 0) {");
        js.append("    var day = new Date().getDay(); var col = (day===0)?7:day;");
        js.append("    rows.forEach(function(row) {");
        js.append("      var txt = row.children[col] ? row.children[col].innerText.toLowerCase() : '';");
        js.append("      if(txt.includes('scheduled')) count++;");
        js.append("    });");
        js.append("  }");

        // 2. DEFINE CSS (The Full Cyberpunk Style)
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Inter:wght@300;400;600&display=swap');");
        js.append("    body > *:not(#cyber-root) { display: none !important; }"); // HIDE OLD SITE
        js.append("    #cyber-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:white; z-index:99999; font-family:'Inter',sans-serif; overflow-y:auto; display:flex; flex-direction:column; }");
        
        // Sidebar & Nav Styles
        js.append("    .top-bar { padding: 20px; display:flex; justify-content:space-between; align-items:center; background:rgba(19,19,31,0.9); border-bottom:1px solid rgba(255,255,255,0.1); }");
        js.append("    .logo { font-family:'Orbitron'; font-size:20px; color:#00f3ff; font-weight:bold; letter-spacing:1px; }");
        js.append("    .status { font-size:10px; color:#00ff9d; border:1px solid #00ff9d; padding:2px 6px; border-radius:4px; }");
        
        // Content Styles
        js.append("    .content-area { padding:20px; flex:1; }");
        js.append("    .section-title { font-family:'Orbitron'; font-size:18px; margin-bottom:15px; color:white; }");
        
        // Cards
        js.append("    .card { background:#13131f; border:1px solid rgba(255,255,255,0.1); border-radius:12px; padding:20px; margin-bottom:15px; position:relative; }");
        js.append("    .card-label { color:#8b9bb4; font-size:12px; margin-bottom:5px; }");
        js.append("    .card-val { font-family:'Orbitron'; font-size:32px; color:white; }");
        js.append("    .card.highlight { border-color:#00f3ff; box-shadow:0 0 15px rgba(0,243,255,0.1); }");
        
        // Mission List
        js.append("    .mission { display:flex; align-items:center; justify-content:space-between; background:#1a1a2e; padding:15px; border-radius:8px; margin-bottom:10px; border-left:3px solid #00f3ff; }");
        js.append("    .btn { background:transparent; border:1px solid #00f3ff; color:#00f3ff; padding:8px 16px; border-radius:4px; font-weight:bold; }");
        
        // Bottom Tabs
        js.append("    .tabs { display:flex; justify-content:space-around; background:#0f0f1a; padding:15px; border-top:1px solid rgba(255,255,255,0.1); }");
        js.append("    .tab-icon { color:#666; font-size:20px; }");
        js.append("    .tab-icon.active { color:#00f3ff; text-shadow:0 0 10px #00f3ff; }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // 3. BUILD HTML STRUCTURE
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cyber-root';");
        js.append("  root.innerHTML = `");
        
        // Header
        js.append("    <div class='top-bar'>");
        js.append("      <div class='logo'>TOKPORTAL</div>");
        js.append("      <div class='status'>SYSTEM ONLINE</div>");
        js.append("    </div>");
        
        // Scrollable Content
        js.append("    <div class='content-area'>");
        
        // Stat Cards
        js.append("      <div class='card highlight'>");
        js.append("        <div class='card-label'>PENDING MISSIONS</div>");
        js.append("        <div class='card-val'>` + count + `</div>");
        js.append("      </div>");
        
        js.append("      <div class='card' style='border-left:4px solid #bd00ff'>");
        js.append("        <div class='card-label'>TOTAL XP</div>");
        js.append("        <div class='card-val' style='color:#bd00ff'>1,250</div>");
        js.append("      </div>");
        
        // Mission List
        js.append("      <div class='section-title'>ACTIVE CONTRACTS</div>");
        js.append("      <div id='mission-container'>");
        js.append("        <div class='mission'>");
        js.append("          <div><div style='font-weight:bold; color:white;'>Daily Engagement</div><div style='font-size:12px; color:#888;'>Reply to 5 comments</div></div>");
        js.append("          <button class='btn'>START</button>");
        js.append("        </div>");
        js.append("      </div>");
        
        js.append("    </div>"); // End Content Area
        
        // Bottom Navigation (Tabs)
        js.append("    <div class='tabs'>");
        js.append("      <i class='fa-solid fa-chart-line tab-icon'></i>");
        js.append("      <i class='fa-solid fa-crosshairs tab-icon active'></i>");
        js.append("      <i class='fa-solid fa-calendar tab-icon'></i>");
        js.append("      <i class='fa-solid fa-gear tab-icon'></i>");
        js.append("    </div>");
        
        js.append("  `;");
        js.append("  document.body.appendChild(root);");
        js.append("})()");

        // 4. INJECT
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

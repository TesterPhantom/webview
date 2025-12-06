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
                    Toast.makeText(MainActivity.this, "Initializing System...", Toast.LENGTH_SHORT).show();
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

    // --- THE INTERACTIVE DASHBOARD ---
    private void injectFullCyberpunkUI(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        
        // Safety: Don't run twice
        js.append("  if(document.getElementById('cyber-root')) return;");

        // 1. DATA SCRAPING
        js.append("  var count = 0;");
        js.append("  var rows = document.querySelectorAll('.grid.grid-cols-8.border-b');");
        js.append("  if(rows.length > 0) {");
        js.append("    var day = new Date().getDay(); var col = (day===0)?7:day;");
        js.append("    rows.forEach(function(row) {");
        js.append("      var txt = row.children[col] ? row.children[col].innerText.toLowerCase() : '';");
        js.append("      if(txt.includes('scheduled')) count++;");
        js.append("    });");
        js.append("  }");

        // 2. CSS STYLES
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Inter:wght@300;400;600&display=swap');");
        js.append("    body > *:not(#cyber-root) { display: none !important; }"); 
        js.append("    #cyber-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:white; z-index:99999; font-family:'Inter',sans-serif; display:flex; flex-direction:column; }");
        
        // Layout
        js.append("    .top-bar { padding:20px; display:flex; justify-content:space-between; align-items:center; background:rgba(19,19,31,0.9); border-bottom:1px solid rgba(255,255,255,0.1); }");
        js.append("    .logo { font-family:'Orbitron'; font-size:20px; color:#00f3ff; font-weight:bold; letter-spacing:1px; }");
        js.append("    .content-area { flex:1; overflow-y:auto; padding:20px; position:relative; }");
        
        // Views (Tabs)
        js.append("    .view-section { display:none; animation: fadeIn 0.3s; }");
        js.append("    .view-section.active { display:block; }");
        js.append("    @keyframes fadeIn { from { opacity:0; transform:translateY(10px); } to { opacity:1; transform:translateY(0); } }");

        // Components
        js.append("    .card { background:#13131f; border:1px solid rgba(255,255,255,0.1); border-radius:12px; padding:20px; margin-bottom:15px; }");
        js.append("    .card-val { font-family:'Orbitron'; font-size:32px; color:white; }");
        js.append("    .btn { background:transparent; border:1px solid #00f3ff; color:#00f3ff; padding:8px 16px; border-radius:4px; font-weight:bold; width:100%; margin-top:10px; }");
        js.append("    .btn:active { background: #00f3ff; color: black; }");

        // Tabs
        js.append("    .tabs { display:flex; justify-content:space-around; background:#0f0f1a; padding:15px; border-top:1px solid rgba(255,255,255,0.1); }");
        js.append("    .tab-icon { color:#666; font-size:20px; padding:10px; transition:0.2s; }");
        js.append("    .tab-icon.active { color:#00f3ff; text-shadow:0 0 10px #00f3ff; transform:scale(1.2); }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // 3. HTML STRUCTURE
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cyber-root';");
        js.append("  root.innerHTML = `");
        
        // Header
        js.append("    <div class='top-bar'><div class='logo'>TOKPORTAL</div><div style='color:#00ff9d; font-size:10px; border:1px solid #00ff9d; padding:2px 5px; border-radius:4px;'>ONLINE</div></div>");
        
        // --- CONTENT AREA ---
        js.append("    <div class='content-area'>");
        
        // VIEW 1: OVERVIEW (Default)
        js.append("      <div id='view-1' class='view-section active'>");
        js.append("        <div class='card' style='border-color:#00f3ff; box-shadow:0 0 15px rgba(0,243,255,0.1);'>");
        js.append("          <div style='color:#8b9bb4; font-size:12px;'>PENDING UPLOADS</div>");
        js.append("          <div class='card-val'>` + count + `</div>");
        js.append("        </div>");
        js.append("        <div class='card' style='border-left:4px solid #bd00ff'>");
        js.append("          <div style='color:#8b9bb4; font-size:12px;'>TOTAL XP</div>");
        js.append("          <div class='card-val' style='color:#bd00ff'>1,250</div>");
        js.append("        </div>");
        js.append("        <h3 style='font-family:Orbitron; margin-top:20px;'>ACTIVE CONTRACTS</h3>");
        js.append("        <div class='card' style='border-left:3px solid #00f3ff;'>");
        js.append("          <div style='font-weight:bold;'>Daily Engagement</div>");
        js.append("          <div style='font-size:12px; color:#888; margin-bottom:10px;'>Reply to 5 comments</div>");
        js.append("          <button class='btn' onclick='alert(\"Mission Started!\")'>START</button>");
        js.append("        </div>");
        js.append("      </div>");

        // VIEW 2: MISSIONS (Placeholder)
        js.append("      <div id='view-2' class='view-section'>");
        js.append("        <h2 style='font-family:Orbitron'>Mission Log</h2>");
        js.append("        <p style='color:#888'>No other active missions.</p>");
        js.append("      </div>");

        // VIEW 3: CALENDAR (Placeholder)
        js.append("      <div id='view-3' class='view-section'>");
        js.append("        <h2 style='font-family:Orbitron'>Calendar</h2>");
        js.append("        <p style='color:#888'>Syncing with TokPortal...</p>");
        js.append("      </div>");

        js.append("    </div>"); // End Content
        
        // --- BOTTOM TABS ---
        js.append("    <div class='tabs'>");
        js.append("      <i class='fa-solid fa-chart-line tab-icon active' onclick='switchTab(1, this)'></i>");
        js.append("      <i class='fa-solid fa-crosshairs tab-icon' onclick='switchTab(2, this)'></i>");
        js.append("      <i class='fa-solid fa-calendar tab-icon' onclick='switchTab(3, this)'></i>");
        js.append("      <i class='fa-solid fa-gear tab-icon' onclick='location.reload()'></i>"); // Gear = Refresh for now
        js.append("    </div>");
        
        js.append("  `;");
        js.append("  document.body.appendChild(root);");
        
        // 4. JAVASCRIPT LOGIC (The wiring)
        js.append("  window.switchTab = function(id, el) {");
        js.append("    // Hide all views");
        js.append("    document.querySelectorAll('.view-section').forEach(v => v.classList.remove('active'));");
        js.append("    // Show selected view");
        js.append("    document.getElementById('view-'+id).classList.add('active');");
        js.append("    // Update icons");
        js.append("    document.querySelectorAll('.tab-icon').forEach(i => i.classList.remove('active'));");
        js.append("    el.classList.add('active');");
        js.append("  };");
        
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

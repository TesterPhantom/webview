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

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                
                // 1. DASHBOARD MODE (Calendar)
                if (url.contains("/calendar")) {
                    injectDashboardUI(view);
                } 
                // 2. COCKPIT MODE (Order Details / Upload Page)
                // Note: I am guessing the URL contains "order" or "upload". 
                // We might need to adjust this keyword based on the real URL!
                else if (url.contains("order") || url.contains("upload")) {
                    injectMissionCockpit(view);
                }
            }
        });

        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    // --- MODE 1: THE DASHBOARD (What you already have) ---
    private void injectDashboardUI(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  if(document.getElementById('cyber-root')) return;");
        // ... (I have condensed the dashboard code for brevity, assumes you keep the styling) ...
        // IMPORTANT CHANGE: The button now navigates to the link!
        js.append("  window.scanData = function() {");
        js.append("    var rows = document.querySelectorAll('.grid.grid-cols-8.border-b');");
        js.append("    var html = ''; var count=0;");
        js.append("    if(rows.length > 0) {");
        js.append("      var day = new Date().getDay(); var col = (day===0)?7:day;");
        js.append("      for(var i=0; i<rows.length; i++) {");
        js.append("        var row = rows[i];");
        js.append("        var txt = row.children[col] ? row.children[col].innerText.toLowerCase() : '';");
        js.append("        if(txt.indexOf('scheduled') !== -1) {");
        js.append("          count++;");
        js.append("          var userEl = row.querySelector('.text-gray-900.truncate');");
        js.append("          var linkEl = row.querySelector('a');"); // Find the link
        js.append("          var url = linkEl ? linkEl.href : '#';");
        js.append("          var username = userEl ? userEl.innerText : 'Unknown';");
        js.append("          html += \"<div class='card' style='border-left:3px solid #00f3ff; margin-bottom:10px;'>\";");
        js.append("          html += \"<div style='font-weight:bold; color:white;'>\" + username + \"</div>\";");
        // BUTTON NAVIGATES TO COCKPIT
        js.append("          html += \"<button class='btn' onclick='location.href=\\\"\" + url + \"\\\"'>OPEN COCKPIT</button>\";");
        js.append("          html += \"</div>\";");
        js.append("        }");
        js.append("      }");
        js.append("    }");
        js.append("    if(document.getElementById('calendar-feed')) document.getElementById('calendar-feed').innerHTML = html;");
        js.append("  };");
        // (Insert the rest of your dashboard styling here or load from file)
        js.append("  setTimeout(function() { window.scanData(); }, 2000);");
        js.append("})()");
        view.loadUrl(js.toString());
    }

    // --- MODE 2: THE MISSION COCKPIT (New!) ---
    private void injectMissionCockpit(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  if(document.getElementById('cockpit-root')) return;");

        // 1. CSS (The High-Tech Look)
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Share+Tech+Mono&display=swap');");
        js.append("    body > *:not(#cockpit-root) { display: none !important; }");
        js.append("    #cockpit-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:#00f3ff; z-index:99999; font-family:'Share Tech Mono', monospace; display:grid; grid-template-columns: 250px 1fr 300px; grid-template-rows: 60px 1fr; gap:10px; padding:10px; }");
        
        js.append("    .cp-header { grid-column: 1 / -1; display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #333; }");
        js.append("    .cp-title { font-family:'Orbitron'; font-size:24px; letter-spacing:2px; color:white; }");
        
        js.append("    .cp-panel { border:1px solid rgba(0,243,255,0.2); background:rgba(19,19,31,0.5); padding:15px; display:flex; flex-direction:column; }");
        js.append("    .cp-panel-title { color:#666; font-size:12px; margin-bottom:15px; border-bottom:1px solid #333; padding-bottom:5px; text-transform:uppercase; letter-spacing:1px; }");
        
        js.append("    .data-row { display:flex; justify-content:space-between; margin-bottom:10px; align-items:center; }");
        js.append("    .data-label { color:#888; font-size:12px; }");
        js.append("    .data-val { color:white; background:#111; padding:4px 8px; border:1px solid #333; width:120px; text-align:right; }");
        js.append("    .btn-copy { background:#00f3ff; color:black; border:none; padding:2px 6px; font-weight:bold; cursor:pointer; font-size:10px; }");
        
        js.append("    .timeline-item { border-left:2px solid #333; padding-left:15px; margin-bottom:20px; position:relative; }");
        js.append("    .timeline-item.active { border-left-color:#00f3ff; }");
        js.append("    .timeline-dot { position:absolute; left:-6px; top:0; width:10px; height:10px; background:#050507; border:2px solid #333; border-radius:50%; }");
        js.append("    .timeline-item.active .timeline-dot { border-color:#00f3ff; background:#00f3ff; box-shadow:0 0 10px #00f3ff; }");
        
        js.append("    .action-btn { background:transparent; border:1px solid #00f3ff; color:#00f3ff; padding:15px; margin-bottom:10px; text-align:center; cursor:pointer; transition:0.2s; text-transform:uppercase; letter-spacing:1px; }");
        js.append("    .action-btn:hover { background:rgba(0,243,255,0.1); box-shadow:0 0 15px rgba(0,243,255,0.2); }");
        js.append("    .meta-box { background:#111; border:1px solid #333; color:#aaa; padding:10px; width:100%; font-family:monospace; margin-bottom:10px; min-height:60px; }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // 2. HTML (The 3-Column Layout)
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cockpit-root';");
        js.append("  root.innerHTML = `");
        
        js.append("    <div class='cp-header'>");
        js.append("      <div class='cp-title'>MISSION COCKPIT</div>");
        js.append("      <div style='color:#00ff9d'>SYSTEM ONLINE</div>");
        js.append("    </div>");

        // COLUMN 1: CREDENTIALS
        js.append("    <div class='cp-panel'>");
        js.append("      <div class='cp-panel-title'>/// CREDENTIALS</div>");
        js.append("      <div class='data-row'><span class='data-label'>USER</span><div class='data-val'>glippy.trimrx</div><button class='btn-copy'>COPY</button></div>");
        js.append("      <div class='data-row'><span class='data-label'>PASS</span><div class='data-val'>********</div><button class='btn-copy'>COPY</button></div>");
        js.append("      <div style='margin-top:auto; border-top:1px solid #333; padding-top:10px;'>");
        js.append("         <div class='cp-panel-title'>/// NICHE STRATEGY</div>");
        js.append("         <div style='color:#e0e0e0; font-size:12px;'>Health & Fitness</div>");
        js.append("         <div style='color:#666; font-size:10px; margin-top:5px;'>Focus on workout humor. Do not engage with politics.</div>");
        js.append("      </div>");
        js.append("    </div>");

        // COLUMN 2: MISSION LOG (Timeline)
        js.append("    <div class='cp-panel'>");
        js.append("      <div class='cp-panel-title'>/// MISSION LOG</div>");
        js.append("      <div class='timeline-item'>");
        js.append("        <div class='timeline-dot'></div>");
        js.append("        <div style='color:#666;'>Video 1: Intro</div>");
        js.append("        <div style='font-size:10px; color:#00ff9d;'>POSTED 14:30</div>");
        js.append("      </div>");
        js.append("      <div class='timeline-item active'>");
        js.append("        <div class='timeline-dot'></div>");
        js.append("        <div style='color:white; font-weight:bold;'>Video 2: Motivation</div>");
        js.append("        <div style='font-size:10px; color:#00f3ff;'>ACTION REQUIRED</div>");
        js.append("      </div>");
        js.append("      <div class='timeline-item'>");
        js.append("        <div class='timeline-dot'></div>");
        js.append("        <div style='color:#666;'>Video 3: Tips</div>");
        js.append("        <div style='font-size:10px; color:#666;'>SCHEDULED</div>");
        js.append("      </div>");
        js.append("    </div>");

        // COLUMN 3: ACTIVE PROTOCOL (Actions)
        js.append("    <div class='cp-panel'>");
        js.append("      <div class='cp-panel-title'>/// ACTIVE PROTOCOL</div>");
        js.append("      <div style='font-size:24px; color:white; font-family:Orbitron; margin-bottom:20px;'>16:00 EST</div>");
        
        js.append("      <div class='cp-panel-title'>1. ASSET RETRIEVAL</div>");
        js.append("      <div class='action-btn'><i class='fa fa-download'></i> DOWNLOAD VIDEO</div>");
        js.append("      <div class='action-btn' style='font-size:12px; border-style:dashed;'><i class='fa fa-music'></i> SOUND LINK</div>");
        
        js.append("      <div class='cp-panel-title' style='margin-top:20px;'>2. METADATA</div>");
        js.append("      <textarea class='meta-box'>Get fit with Glippy! 💪 #fitness</textarea>");
        
        js.append("      <div class='cp-panel-title' style='margin-top:20px;'>3. DEPLOYMENT</div>");
        js.append("      <input type='text' class='meta-box' style='min-height:30px;' placeholder='Paste Video URL...'>");
        js.append("      <div class='action-btn' style='background:#00f3ff; color:black; font-weight:bold;'>CONFIRM UPLOAD</div>");
        js.append("    </div>");

        js.append("  `;");
        js.append("  document.body.appendChild(root);");
        js.append("})()");
        view.loadUrl(js.toString());
    }
}

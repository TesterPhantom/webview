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

        // 2. LONG TERM MEMORY
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);

        // 3. THE NAVIGATOR
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                
                // PRIORITY 1: MISSION COCKPIT
                // If we are on an order details page, load the Cockpit UI
                if (url.contains("order") || url.contains("details") || url.contains("job")) {
                    injectMissionCockpit(view);
                    return;
                }

                // PRIORITY 2: DASHBOARD
                // If we are on the calendar, load the Command Deck UI
                if (url.contains("/calendar")) {
                    injectDashboardUI(view);
                    return;
                }

                // PRIORITY 3: AUTO-PILOT REDIRECT
                // If we are logged in (Account Manager) but on the wrong page (like the yellow warning page),
                // redirect to the Calendar immediately.
                if (url.contains("account-manager") && !url.contains("login")) {
                    Toast.makeText(MainActivity.this, "Aligning Satellite...", Toast.LENGTH_SHORT).show();
                    view.loadUrl("https://app.tokportal.com/account-manager/calendar");
                }
            }
        });

        // 4. LAUNCH
        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    // ==========================================
    // MODULE 1: THE COMMAND DECK (Calendar View)
    // ==========================================
    private void injectDashboardUI(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  if(document.getElementById('cyber-root')) return;");
        
        // CSS
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Inter:wght@300;400;600&display=swap');");
        js.append("    body > *:not(#cyber-root) { display: none !important; }"); 
        js.append("    #cyber-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:white; z-index:99999; font-family:'Inter',sans-serif; display:flex; flex-direction:column; }");
        js.append("    .top-bar { padding:15px; background:rgba(19,19,31,0.95); border-bottom:1px solid #333; display:flex; justify-content:space-between; align-items:center; }");
        js.append("    .content-area { flex:1; overflow-y:auto; padding:15px; }");
        
        // UI Components
        js.append("    .card { background:#13131f; border:1px solid rgba(255,255,255,0.1); border-radius:8px; padding:12px; margin-bottom:8px; }");
        js.append("    .btn { background:transparent; border:1px solid #00f3ff; color:#00f3ff; padding:8px; border-radius:4px; font-weight:bold; width:100%; margin-top:5px; }");
        js.append("    .category-header { padding:12px; background:#1a1a2e; margin-top:10px; border-radius:6px; font-family:'Orbitron'; font-size:14px; color:#00f3ff; border:1px solid rgba(255,255,255,0.1); }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // HTML
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cyber-root';");
        js.append("  root.innerHTML = `");
        js.append("    <div class='top-bar'><span style='font-family:Orbitron; color:#00f3ff;'>COMMAND DECK</span><span style='color:#00ff9d; font-size:10px; border:1px solid #00ff9d; padding:2px;'>ONLINE</span></div>");
        js.append("    <div class='content-area' id='calendar-feed'><p style='color:#666; text-align:center; margin-top:20px;'>Scanning Network...</p></div>");
        js.append("  `;");
        js.append("  document.body.appendChild(root);");

        // LOGIC: Scrape the Calendar and build the UI
        js.append("  window.scanData = function() {");
        js.append("    var rows = document.querySelectorAll('.grid.grid-cols-8.border-b');");
        js.append("    var html = ''; var count = 0;");
        js.append("    if(rows.length > 0) {");
        js.append("      var day = new Date().getDay(); var col = (day===0)?7:day;");
        js.append("      for(var i=0; i<rows.length; i++) {");
        js.append("        var row = rows[i];");
        js.append("        var txt = row.children[col] ? row.children[col].innerText.toLowerCase() : '';");
        
        // FIND SCHEDULED ITEMS
        js.append("        if(txt.indexOf('scheduled') !== -1) {");
        js.append("          count++;");
        js.append("          var userEl = row.querySelector('.text-gray-900.truncate');");
        js.append("          var linkEl = row.querySelector('a');"); 
        js.append("          var url = linkEl ? linkEl.href : '#';");
        js.append("          var username = userEl ? userEl.innerText : 'Unknown';");
        
        js.append("          html += \"<div class='card' style='border-left:3px solid #00f3ff;'>\";");
        js.append("          html += \"<div style='font-weight:bold;'>\" + username + \"</div>\";");
        js.append("          html += \"<div style='font-size:12px; color:#888;'>STATUS: READY</div>\";");
        // BUTTON: Navigates to the Order Page
        js.append("          html += \"<button class='btn' onclick='location.href=\\\"\" + url + \"\\\"'>OPEN COCKPIT</button>\";");
        js.append("          html += \"</div>\";");
        js.append("        }");
        js.append("      }");
        js.append("    }");
        
        // Header with count
        js.append("    var header = \"<div class='category-header'>⚡ ACTION REQUIRED (\" + count + \")</div>\";");
        js.append("    if(html !== '') document.getElementById('calendar-feed').innerHTML = header + html;");
        js.append("    else document.getElementById('calendar-feed').innerHTML = \"<p style='color:#666; text-align:center;'>No missions detected.</p>\";");
        js.append("  };");
        
        js.append("  setTimeout(function() { window.scanData(); }, 1500);");
        js.append("})()");
        view.loadUrl(js.toString());
    }

    // ==========================================
    // MODULE 2: THE MISSION COCKPIT (Order View)
    // ==========================================
    private void injectMissionCockpit(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  if(document.getElementById('cockpit-root')) return;");

        // CSS
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Share+Tech+Mono&display=swap');");
        js.append("    body > *:not(#cockpit-root) { display: none !important; }");
        js.append("    #cockpit-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:#00f3ff; z-index:99999; font-family:'Share Tech Mono', monospace; display:flex; flex-direction:column; padding:10px; overflow-y:auto; }");
        js.append("    .cp-header { display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #333; padding-bottom:10px; margin-bottom:15px; }");
        js.append("    .cp-title { font-family:'Orbitron'; font-size:20px; letter-spacing:2px; color:white; }");
        js.append("    .cp-panel { border:1px solid rgba(0,243,255,0.2); background:rgba(19,19,31,0.5); padding:15px; margin-bottom:15px; border-radius:4px; }");
        js.append("    .cp-panel-title { color:#666; font-size:12px; margin-bottom:10px; border-bottom:1px solid #333; text-transform:uppercase; letter-spacing:1px; }");
        js.append("    .data-row { display:flex; justify-content:space-between; margin-bottom:8px; align-items:center; }");
        js.append("    .data-val { color:white; background:#111; padding:6px; border:1px solid #333; font-size:14px; }");
        js.append("    .action-btn { background:rgba(0,243,255,0.1); border:1px solid #00f3ff; color:#00f3ff; padding:15px; margin-bottom:10px; text-align:center; cursor:pointer; font-weight:bold; width:100%; display:block; }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // HTML
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cockpit-root';");
        js.append("  root.innerHTML = `");
        js.append("    <div class='cp-header'><div class='cp-title'>MISSION COCKPIT</div><div style='color:#00ff9d'>SECURE</div></div>");
        
        js.append("    <div class='cp-panel'>");
        js.append("      <div class='cp-panel-title'>/// TARGET CREDENTIALS</div>");
        js.append("      <div class='data-row'><span>USER</span><span class='data-val' id='cp-user'>Detecting...</span></div>");
        js.append("      <div class='data-row'><span>PASS</span><span class='data-val'>********</span></div>");
        js.append("    </div>");

        js.append("    <div class='cp-panel'>");
        js.append("      <div class='cp-panel-title'>/// ACTIONS</div>");
        js.append("      <button class='action-btn'>1. DOWNLOAD VIDEO</button>");
        js.append("      <button class='action-btn' style='background:#00f3ff; color:black;'>2. CONFIRM UPLOAD</button>");
        js.append("    </div>");
        
        js.append("    <button class='action-btn' style='border-color:#ff0050; color:#ff0050; margin-top:auto;' onclick='history.back()'>ABORT MISSION</button>");
        js.append("  `;");
        js.append("  document.body.appendChild(root);");

        // AUTO-FILL DATA
        js.append("  setTimeout(function() {");
        js.append("    var userEl = document.querySelector('h1') || document.querySelector('.text-xl');");
        js.append("    if(userEl) document.getElementById('cp-user').innerText = userEl.innerText;");
        js.append("  }, 1000);");

        js.append("})()");
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

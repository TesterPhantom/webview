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
                
                // DEBUG: THIS WILL TELL US THE EXACT URL IF IT STICKS
                Toast.makeText(MainActivity.this, "Loc: " + url, Toast.LENGTH_LONG).show();

                // 1. COCKPIT MODE (Priority 1)
                // We check this first so we don't redirect away from an active mission
                if (url.contains("order") || url.contains("details") || url.contains("job")) {
                    Toast.makeText(MainActivity.this, "Mission Cockpit Engaged", Toast.LENGTH_SHORT).show();
                    injectMissionCockpit(view);
                    return;
                }

                // 2. DASHBOARD MODE (Priority 2)
                if (url.contains("/calendar")) {
                    injectDashboardUI(view);
                    return;
                }

                // 3. CATCH-ALL REDIRECT (Priority 3)
                // If we are in the Account Manager but NOT on the calendar or an order...
                // ...REDIRECT TO CALENDAR.
                if (url.contains("account-manager") && !url.contains("login")) {
                    Toast.makeText(MainActivity.this, "Aligning Satellite...", Toast.LENGTH_SHORT).show();
                    view.loadUrl("https://app.tokportal.com/account-manager/calendar");
                }
            }
        });

        // 4. LAUNCH
        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    // --- MODE 1: DASHBOARD (Calendar) ---
    private void injectDashboardUI(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  if(document.getElementById('cyber-root')) return;");
        
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Inter:wght@300;400;600&display=swap');");
        js.append("    body > *:not(#cyber-root) { display: none !important; }"); 
        js.append("    #cyber-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:white; z-index:99999; font-family:'Inter',sans-serif; display:flex; flex-direction:column; }");
        js.append("    .top-bar { padding:15px; background:rgba(19,19,31,0.95); border-bottom:1px solid #333; display:flex; justify-content:space-between; align-items:center; }");
        js.append("    .card { background:#13131f; border:1px solid rgba(255,255,255,0.1); border-radius:8px; padding:12px; margin-bottom:8px; }");
        js.append("    .btn { background:transparent; border:1px solid #00f3ff; color:#00f3ff; padding:8px; border-radius:4px; font-weight:bold; width:100%; margin-top:5px; }");
        js.append("    .content-area { flex:1; overflow-y:auto; padding:15px; }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cyber-root';");
        js.append("  root.innerHTML = `");
        js.append("    <div class='top-bar'><span style='font-family:Orbitron; color:#00f3ff;'>COMMAND DECK</span><span style='color:#00ff9d; font-size:10px; border:1px solid #00ff9d; padding:2px;'>ONLINE</span></div>");
        js.append("    <div class='content-area' id='calendar-feed'><p style='color:#666; text-align:center; margin-top:20px;'>Scanning Network...</p></div>");
        js.append("  `;");
        js.append("  document.body.appendChild(root);");

        js.append("  window.scanData = function() {");
        js.append("    var rows = document.querySelectorAll('.grid.grid-cols-8.border-b');");
        js.append("    var html = '';");
        js.append("    if(rows.length > 0) {");
        js.append("      var day = new Date().getDay(); var col = (day===0)?7:day;");
        js.append("      for(var i=0; i<rows.length; i++) {");
        js.append("        var row = rows[i];");
        js.append("        var txt = row.children[col] ? row.children[col].innerText.toLowerCase() : '';");
        js.append("        if(txt.indexOf('scheduled') !== -1) {");
        js.append("          var userEl = row.querySelector('.text-gray-900.truncate');");
        // GRAB LINK
        js.append("          var linkEl = row.querySelector('a');"); 
        js.append("          var url = linkEl ? linkEl.href : '#';");
        js.append("          var username = userEl ? userEl.innerText : 'Unknown';");
        js.append("          html += \"<div class='card' style='border-left:3px solid #00f3ff;'>\";");
        js.append("          html += \"<div style='font-weight:bold;'>\" + username + \"</div>\";");
        js.append("          html += \"<div style='font-size:12px; color:#888;'>STATUS: READY</div>\";");
        // CLICKING BUTTON NAVIGATES TO URL
        js.append("          html += \"<button class='btn' onclick='location.href=\\\"\" + url + \"\\\"'>OPEN COCKPIT</button>\";");
        js.append("          html += \"</div>\";");
        js.append("        }");
        js.append("      }");
        js.append("    }");
        js.append("    if(html !== '') document.getElementById('calendar-feed').innerHTML = html;");
        js.append("  };");
        js.append("  setTimeout(function() { window.scanData(); }, 2000);");
        js.append("})()");
        view.loadUrl(js.toString());
    }

    // --- MODE 2: MISSION COCKPIT ---
    private void injectMissionCockpit(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  if(document.getElementById('cockpit-root')) return;");

        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Share+Tech+Mono&display=swap');");
        js.append("    body > *:not(#cockpit-root) { display: none !important; }");
        js.append("    #cockpit-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:#00f3ff; z-index:99999; font-family:'Share Tech Mono', monospace; display:flex; flex-direction:column; padding:10px; overflow-y:auto; }");
        js.append("    .cp-header { display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #333; padding-bottom:10px; margin-bottom:15px; }");
        js.append("    .cp-title { font-family:'Orbitron'; font-size:20px; letter-spacing:2px; color:white; }");
        js.append("    .cp-panel { border:1px solid rgba(0,243,255,0.2); background:rgba(19,19,31,0.5); padding:15px; margin-bottom:15px; border-radius:4px; }");
        js.append("    .data-row { display:flex; justify-content:space-between; margin-bottom:8px; align-items:center; }");
        js.append("    .data-val { color:white; background:#111; padding:6px; border:1px solid #333; font-size:14px; }");
        js.append("    .action-btn { background:rgba(0,243,255,0.1); border:1px solid #00f3ff; color:#00f3ff; padding:15px; margin-bottom:10px; text-align:center; cursor:pointer; font-weight:bold; width:100%; display:block; }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cockpit-root';");
        js.append("  root.innerHTML = `");
        js.append("    <div class='cp-header'><div class='cp-title'>MISSION COCKPIT</div><div style='color:#00ff9d'>SECURE</div></div>");
        js.append("    <div class='cp-panel'><div class='data-row'><span>USER</span><span class='data-val' id='cp-user'>Detecting...</span></div></div>");
        js.append("    <div class='cp-panel'><button class='action-btn' id='dl-btn'>1. DOWNLOAD VIDEO</button><button class='action-btn' style='background:#00f3ff; color:black;'>2. CONFIRM UPLOAD</button></div>");
        js.append("    <button class='action-btn' style='border-color:#ff0050; color:#ff0050; margin-top:auto;' onclick='history.back()'>ABORT MISSION</button>");
        js.append("  `;");
        js.append("  document.body.appendChild(root);");

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

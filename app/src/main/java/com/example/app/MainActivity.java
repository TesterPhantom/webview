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
                
                // REDIRECT: Dashboard -> Calendar
                if (url.contains("/dashboard") && !url.contains("calendar")) {
                    view.loadUrl("https://app.tokportal.com/account-manager/calendar");
                    return;
                }

                // INJECT: Calendar -> Full UI
                if (url.contains("/calendar")) {
                    injectFullCyberpunkUI(view);
                }
            }
        });

        // 4. LAUNCH
        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    private void injectFullCyberpunkUI(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        
        js.append("  if(document.getElementById('cyber-root')) return;");

        // 1. CSS STYLES (Compact Mode)
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Inter:wght@300;400;600&display=swap');");
        js.append("    body > *:not(#cyber-root) { display: none !important; }"); 
        js.append("    #cyber-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:white; z-index:99999; font-family:'Inter',sans-serif; display:flex; flex-direction:column; }");
        
        js.append("    .top-bar { padding:15px; display:flex; justify-content:space-between; align-items:center; background:rgba(19,19,31,0.95); border-bottom:1px solid rgba(255,255,255,0.1); }");
        js.append("    .logo { font-family:'Orbitron'; font-size:18px; color:#00f3ff; font-weight:bold; letter-spacing:1px; }");
        js.append("    .content-area { flex:1; overflow-y:auto; padding:15px; position:relative; }");
        
        js.append("    .view-section { display:none; }");
        js.append("    .view-section.active { display:block; animation: fadeIn 0.3s; }");
        js.append("    @keyframes fadeIn { from { opacity:0; transform:translateY(10px); } to { opacity:1; transform:translateY(0); } }");
        
        // COMPACT CARDS
        js.append("    .card { background:#13131f; border:1px solid rgba(255,255,255,0.1); border-radius:8px; padding:12px; margin-bottom:8px; display:flex; flex-direction:column; }");
        js.append("    .card-row { display:flex; justify-content:space-between; align-items:center; }");
        
        // COMPACT BUTTONS
        js.append("    .btn { background:transparent; border:1px solid #00f3ff; color:#00f3ff; padding:6px 12px; border-radius:4px; font-weight:bold; font-size:12px; margin-top:8px; width:100%; }");
        js.append("    .btn:active { background: #00f3ff; color: black; }");

        // ACCORDION HEADERS
        js.append("    .category-header { padding:12px; background:#1a1a2e; margin-top:10px; border-radius:6px; font-family:'Orbitron'; font-size:14px; display:flex; justify-content:space-between; align-items:center; cursor:pointer; border:1px solid rgba(255,255,255,0.1); }");
        js.append("    .cat-blue { border-left:3px solid #00f3ff; }");
        js.append("    .cat-orange { border-left:3px solid #ff9900; }");
        js.append("    .cat-grey { border-left:3px solid #666; }");
        js.append("    .cat-content { display:none; margin-top:5px; }"); // Hidden by default
        js.append("    .cat-content.open { display:block; animation: slideDown 0.2s; }");
        js.append("    @keyframes slideDown { from { opacity:0; transform:translateY(-5px); } to { opacity:1; transform:translateY(0); } }");
        
        // TABS
        js.append("    .tabs { display:flex; justify-content:space-around; background:#0f0f1a; padding:10px; border-top:1px solid rgba(255,255,255,0.1); }");
        js.append("    .tab-icon { color:#666; font-size:18px; padding:10px; transition:0.2s; }");
        js.append("    .tab-icon.active { color:#00f3ff; text-shadow:0 0 10px #00f3ff; transform:scale(1.2); }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // 2. HTML STRUCTURE
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cyber-root';");
        js.append("  root.innerHTML = `");
        
        js.append("    <div class='top-bar'><div class='logo'>TOKPORTAL</div><div style='color:#00ff9d; font-size:9px; border:1px solid #00ff9d; padding:2px 4px; border-radius:3px;'>ONLINE</div></div>");
        
        js.append("    <div class='content-area'>");
        
        // VIEW 1: OVERVIEW
        js.append("      <div id='view-1' class='view-section active'>");
        // Big stats can stay somewhat big, but slightly reduced
        js.append("        <div class='card' style='border-color:#00f3ff; box-shadow:0 0 10px rgba(0,243,255,0.1);'>");
        js.append("          <div style='color:#8b9bb4; font-size:11px;'>PENDING UPLOADS</div>");
        js.append("          <div style='font-family:Orbitron; font-size:28px; color:white;' id='dash-count'>--</div>");
        js.append("        </div>");
        js.append("      </div>");

        // VIEW 2: MISSIONS
        js.append("      <div id='view-2' class='view-section'>");
        js.append("        <h2 style='font-family:Orbitron'>Mission Log</h2><p style='color:#888'>No active missions.</p>");
        js.append("      </div>");

        // VIEW 3: CALENDAR (Categorized & Compact)
        js.append("      <div id='view-3' class='view-section'>");
        js.append("        <h2 style='font-family:Orbitron; margin-bottom:10px; font-size:18px;'>Command Deck</h2>");
        js.append("        <div id='calendar-feed'><p style='color:#888'>Scanning network...</p></div>");
        js.append("        <button class='btn' onclick='scanData()' style='margin-top:20px; border-style:dashed;'>FORCE RE-SCAN</button>");
        js.append("      </div>");

        js.append("    </div>"); 
        
        // TABS
        js.append("    <div class='tabs'>");
        js.append("      <i class='fa-solid fa-chart-line tab-icon active' onclick='switchTab(1, this)'></i>");
        js.append("      <i class='fa-solid fa-crosshairs tab-icon' onclick='switchTab(2, this)'></i>");
        js.append("      <i class='fa-solid fa-calendar tab-icon' onclick='switchTab(3, this)'></i>");
        js.append("    </div>");
        
        js.append("  `;");
        js.append("  document.body.appendChild(root);");
        
        // 3. LOGIC FUNCTIONS
        js.append("  window.switchTab = function(id, el) {");
        js.append("    var views = document.querySelectorAll('.view-section');");
        js.append("    for(var i=0; i<views.length; i++) { views[i].classList.remove('active'); }");
        js.append("    document.getElementById('view-'+id).classList.add('active');");
        js.append("    var icons = document.querySelectorAll('.tab-icon');");
        js.append("    for(var j=0; j<icons.length; j++) { icons[j].classList.remove('active'); }");
        js.append("    el.classList.add('active');");
        js.append("  };");

        // TOGGLE FUNCTION for Collapsing Sections
        js.append("  window.toggleSection = function(id, header) {");
        js.append("    var content = document.getElementById(id);");
        js.append("    if(content.classList.contains('open')) {");
        js.append("      content.classList.remove('open');");
        js.append("      header.querySelector('i').className = 'fa-solid fa-chevron-down';"); // Arrow Down
        js.append("    } else {");
        js.append("      content.classList.add('open');");
        js.append("      header.querySelector('i').className = 'fa-solid fa-chevron-up';"); // Arrow Up
        js.append("    }");
        js.append("  };");

        // SMART SCANNER
        js.append("  window.scanData = function() {");
        js.append("    var count = 0;");
        js.append("    var actionHTML = ''; var warmHTML = ''; var doneHTML = '';"); 
        
        js.append("    var rows = document.querySelectorAll('.grid.grid-cols-8.border-b');");
        js.append("    if(rows.length > 0) {");
        js.append("      var day = new Date().getDay(); var col = (day===0)?7:day;");
        js.append("      for(var i=0; i<rows.length; i++) {");
        js.append("        var row = rows[i];");
        js.append("        var txt = row.children[col] ? row.children[col].innerText.toLowerCase() : '';");
        js.append("        var fullRow = row.innerText.toLowerCase();");
        js.append("        var userEl = row.querySelector('.text-gray-900.truncate');");
        js.append("        var username = userEl ? userEl.innerText : 'Unknown';");
        
        // LOGIC 1: ACTION REQUIRED
        js.append("        if(txt.indexOf('scheduled') !== -1) {");
        js.append("          count++;");
        js.append("          actionHTML += \"<div class='card' style='border-left:3px solid #00f3ff;'>\";");
        js.append("          actionHTML += \"<div class='card-row'>\";");
        js.append("          actionHTML += \"<div style='font-weight:bold; color:white; font-size:14px;'>\" + username + \"</div>\";");
        js.append("          actionHTML += \"<div style='font-size:10px; color:#00f3ff;'>READY</div>\";");
        js.append("          actionHTML += \"</div>\";");
        js.append("          actionHTML += \"<button class='btn' onclick='alert(\\\"Uploading...\\\")'>UPLOAD</button>\";");
        js.append("          actionHTML += \"</div>\";");
        
        // LOGIC 2: WARMING
        js.append("        } else if(fullRow.indexOf('warming') !== -1 || fullRow.indexOf('niche') !== -1) {");
        js.append("          warmHTML += \"<div class='card' style='border-left:3px solid #ff9900; opacity:0.9;'>\";");
        js.append("          warmHTML += \"<div class='card-row'>\";");
        js.append("          warmHTML += \"<div style='font-weight:bold; color:#ccc; font-size:14px;'>\" + username + \"</div>\";");
        js.append("          warmHTML += \"<div style='font-size:10px; color:#ff9900;'>WARMING</div>\";");
        js.append("          warmHTML += \"</div>\";");
        js.append("          warmHTML += \"</div>\";");
        
        // LOGIC 3: STANDBY
        js.append("        } else {");
        js.append("          doneHTML += \"<div class='card' style='border-left:3px solid #444; opacity:0.5;'>\";");
        js.append("          doneHTML += \"<div class='card-row'>\";");
        js.append("          doneHTML += \"<div style='font-weight:bold; color:#888; font-size:14px;'>\" + username + \"</div>\";");
        js.append("          doneHTML += \"<div style='font-size:10px; color:#666;'>STANDBY</div>\";");
        js.append("          doneHTML += \"</div>\";");
        js.append("          doneHTML += \"</div>\";");
        js.append("        }");
        js.append("      }");
        js.append("    }");

        js.append("    var finalHTML = '';");
        
        // 1. ACTION (Default OPEN)
        js.append("    if(actionHTML !== '') {");
        js.append("       finalHTML += \"<div class='category-header cat-blue' onclick='toggleSection(\\\"sec-action\\\", this)'><span>⚡ ACTION (\" + count + \")</span><i class='fa-solid fa-chevron-up'></i></div>\";");
        js.append("       finalHTML += \"<div id='sec-action' class='cat-content open'>\" + actionHTML + \"</div>\";");
        js.append("    }");
        
        // 2. WARMING (Default CLOSED)
        js.append("    if(warmHTML !== '') {");
        js.append("       finalHTML += \"<div class='category-header cat-orange' onclick='toggleSection(\\\"sec-warm\\\", this)'><span>🔥 WARMING</span><i class='fa-solid fa-chevron-down'></i></div>\";");
        js.append("       finalHTML += \"<div id='sec-warm' class='cat-content'>\" + warmHTML + \"</div>\";");
        js.append("    }");
        
        // 3. STANDBY (Default CLOSED)
        js.append("    if(doneHTML !== '') {");
        js.append("       finalHTML += \"<div class='category-header cat-grey' onclick='toggleSection(\\\"sec-done\\\", this)'><span>💤 STANDBY</span><i class='fa-solid fa-chevron-down'></i></div>\";");
        js.append("       finalHTML += \"<div id='sec-done' class='cat-content'>\" + doneHTML + \"</div>\";");
        js.append("    }");
        
        js.append("    if(finalHTML === '') finalHTML = '<p style=\"color:#666\">System Idle. No data found.</p>';");
        
        js.append("    document.getElementById('dash-count').innerText = count;");
        js.append("    document.getElementById('calendar-feed').innerHTML = finalHTML;");
        js.append("  };");
        
        js.append("  setTimeout(function() { window.scanData(); }, 3000);");
        
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

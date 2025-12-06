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
                
                // A. MISSION COCKPIT (Priority 1)
                // If we land on an order details page, load the Terminal
                if (url.contains("order") || url.contains("details") || url.contains("job")) {
                    injectMissionCockpit(view);
                    return;
                }

                // B. DASHBOARD / CALENDAR (Priority 2)
                if (url.contains("/calendar")) {
                    injectDashboardUI(view);
                    return;
                }

                // C. AUTO-PILOT (Redirect from Home to Calendar)
                if (url.contains("account-manager") && !url.contains("login")) {
                    view.loadUrl("https://app.tokportal.com/account-manager/calendar");
                }
            }
        });

        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    // =========================================================
    // MODULE 1: THE COMMAND DECK (Restored the "Good" Scraper)
    // =========================================================
    private void injectDashboardUI(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  if(document.getElementById('cyber-root')) return;");
        
        // CSS STYLES (The Clean, Compact Look)
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Inter:wght@300;400;600&display=swap');");
        js.append("    body > *:not(#cyber-root) { display: none !important; }"); 
        js.append("    #cyber-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:white; z-index:99999; font-family:'Inter',sans-serif; display:flex; flex-direction:column; }");
        
        js.append("    .top-bar { padding:15px; background:rgba(19,19,31,0.95); border-bottom:1px solid #333; display:flex; justify-content:space-between; align-items:center; }");
        js.append("    .content-area { flex:1; overflow-y:auto; padding:15px; }");
        
        // COMPACT CARDS
        js.append("    .card { background:#13131f; border:1px solid rgba(255,255,255,0.1); border-radius:8px; padding:12px; margin-bottom:8px; display:flex; flex-direction:column; }");
        js.append("    .card-row { display:flex; justify-content:space-between; align-items:center; }");
        js.append("    .btn { background:transparent; border:1px solid #00f3ff; color:#00f3ff; padding:6px 12px; border-radius:4px; font-weight:bold; font-size:12px; margin-top:8px; width:100%; }");
        js.append("    .btn:active { background: #00f3ff; color: black; }");

        // ACCORDION HEADERS
        js.append("    .category-header { padding:12px; background:#1a1a2e; margin-top:10px; border-radius:6px; font-family:'Orbitron'; font-size:14px; display:flex; justify-content:space-between; align-items:center; cursor:pointer; border:1px solid rgba(255,255,255,0.1); }");
        js.append("    .cat-blue { border-left:3px solid #00f3ff; }");
        js.append("    .cat-orange { border-left:3px solid #ff9900; }");
        js.append("    .cat-grey { border-left:3px solid #666; }");
        js.append("    .cat-content { display:none; margin-top:5px; }"); 
        js.append("    .cat-content.open { display:block; animation: slideDown 0.2s; }");
        js.append("    @keyframes slideDown { from { opacity:0; transform:translateY(-5px); } to { opacity:1; transform:translateY(0); } }");
        
        // TABS
        js.append("    .tabs { display:flex; justify-content:space-around; background:#0f0f1a; padding:10px; border-top:1px solid rgba(255,255,255,0.1); }");
        js.append("    .tab-icon { color:#666; font-size:18px; padding:10px; transition:0.2s; }");
        js.append("    .tab-icon.active { color:#00f3ff; text-shadow:0 0 10px #00f3ff; transform:scale(1.2); }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // HTML
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cyber-root';");
        js.append("  root.innerHTML = `");
        
        js.append("    <div class='top-bar'><span style='font-family:Orbitron; color:#00f3ff;'>COMMAND DECK</span><span style='color:#00ff9d; font-size:10px; border:1px solid #00ff9d; padding:2px;'>ONLINE</span></div>");
        
        js.append("    <div class='content-area'>");
        
        // OVERVIEW
        js.append("      <div id='view-1' class='view-section active'>");
        js.append("        <div class='card' style='border-color:#00f3ff; box-shadow:0 0 10px rgba(0,243,255,0.1);'>");
        js.append("          <div style='color:#8b9bb4; font-size:11px;'>PENDING UPLOADS</div>");
        js.append("          <div style='font-family:Orbitron; font-size:28px; color:white;' id='dash-count'>--</div>");
        js.append("        </div>");
        js.append("      </div>");

        // MISSIONS
        js.append("      <div id='view-2' class='view-section'>");
        js.append("        <h2 style='font-family:Orbitron'>Mission Log</h2><p style='color:#888'>No active missions.</p>");
        js.append("      </div>");

        // CALENDAR (With the Feeds)
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
        
        // --- LOGIC FUNCTIONS ---
        js.append("  window.switchTab = function(id, el) {");
        js.append("    var views = document.querySelectorAll('.view-section');");
        js.append("    for(var i=0; i<views.length; i++) { views[i].classList.remove('active'); }");
        js.append("    document.getElementById('view-'+id).classList.add('active');");
        js.append("    var icons = document.querySelectorAll('.tab-icon');");
        js.append("    for(var j=0; j<icons.length; j++) { icons[j].classList.remove('active'); }");
        js.append("    el.classList.add('active');");
        js.append("  };");

        js.append("  window.toggleSection = function(id, header) {");
        js.append("    var content = document.getElementById(id);");
        js.append("    if(content.classList.contains('open')) {");
        js.append("      content.classList.remove('open');");
        js.append("      header.querySelector('i').className = 'fa-solid fa-chevron-down';");
        js.append("    } else {");
        js.append("      content.classList.add('open');");
        js.append("      header.querySelector('i').className = 'fa-solid fa-chevron-up';");
        js.append("    }");
        js.append("  };");

        // --- THE GOOD SCRAPER (With Link Logic Added) ---
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
        
        // FIND LINK (This is the new part!)
        js.append("        var linkEl = row.querySelector('a');");
        js.append("        var href = linkEl ? linkEl.href : '';");

        // LOGIC 1: ACTION REQUIRED
        js.append("        if(txt.indexOf('scheduled') !== -1) {");
        js.append("          count++;");
        js.append("          actionHTML += \"<div class='card' style='border-left:3px solid #00f3ff;'>\";");
        js.append("          actionHTML += \"<div class='card-row'>\";");
        js.append("          actionHTML += \"<div style='font-weight:bold; color:white; font-size:14px;'>\" + username + \"</div>\";");
        js.append("          actionHTML += \"<div style='font-size:10px; color:#00f3ff;'>READY</div>\";");
        js.append("          actionHTML += \"</div>\";");
        // BUTTON WITH NAVIGATION
        js.append("          if(href) {");
        js.append("             actionHTML += \"<button class='btn' onclick='location.href=\\\"\" + href + \"\\\"'>OPEN COCKPIT</button>\";");
        js.append("          } else {");
        js.append("             actionHTML += \"<button class='btn' style='border-color:#666; color:#666;'>LINK NOT FOUND</button>\";");
        js.append("          }");
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
        // ACTION (Open)
        js.append("    if(actionHTML !== '') {");
        js.append("       finalHTML += \"<div class='category-header cat-blue' onclick='toggleSection(\\\"sec-action\\\", this)'><span>⚡ ACTION (\" + count + \")</span><i class='fa-solid fa-chevron-up'></i></div>\";");
        js.append("       finalHTML += \"<div id='sec-action' class='cat-content open'>\" + actionHTML + \"</div>\";");
        js.append("    }");
        // WARMING (Closed)
        js.append("    if(warmHTML !== '') {");
        js.append("       finalHTML += \"<div class='category-header cat-orange' onclick='toggleSection(\\\"sec-warm\\\", this)'><span>🔥 WARMING</span><i class='fa-solid fa-chevron-down'></i></div>\";");
        js.append("       finalHTML += \"<div id='sec-warm' class='cat-content'>\" + warmHTML + \"</div>\";");
        js.append("    }");
        // STANDBY (Closed)
        js.append("    if(doneHTML !== '') {");
        js.append("       finalHTML += \"<div class='category-header cat-grey' onclick='toggleSection(\\\"sec-done\\\", this)'><span>💤 STANDBY</span><i class='fa-solid fa-chevron-down'></i></div>\";");
        js.append("       finalHTML += \"<div id='sec-done' class='cat-content'>\" + doneHTML + \"</div>\";");
        js.append("    }");
        
        js.append("    if(finalHTML === '') finalHTML = '<p style=\"color:#666\">System Idle. No data found.</p>';");
        
        js.append("    document.getElementById('dash-count').innerText = count;");
        js.append("    document.getElementById('calendar-feed').innerHTML = finalHTML;");
        js.append("  };");
        
        js.append("  setTimeout(function() { window.scanData(); }, 2000);");
        
        js.append("})()");
        view.loadUrl(js.toString());
    }

    // ==========================================
    // MODULE 2: MISSION COCKPIT
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

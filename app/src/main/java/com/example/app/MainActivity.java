package com.example.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
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

        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                
                // 1. MISSION COCKPIT (Priority)
                if (url.contains("order") || url.contains("details") || url.contains("job")) {
                    injectMissionCockpit(view);
                    return;
                }

                // 2. COMMAND DECK
                if (url.contains("/calendar")) {
                    injectDashboardUI(view);
                    return;
                }

                // 3. AUTO-PILOT
                if (url.contains("account-manager") && !url.contains("login")) {
                    view.loadUrl("https://app.tokportal.com/account-manager/calendar");
                }
            }
        });

        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) { mContext = c; }
        @JavascriptInterface
        public void copyToClipboard(String text) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(mContext, "Copied", Toast.LENGTH_SHORT).show();
        }
    }

    // --- MODULE 1: COMMAND DECK ---
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
        js.append("    .content-area { flex:1; overflow-y:auto; padding:15px; }");
        js.append("    .card { background:#13131f; border:1px solid rgba(255,255,255,0.1); border-radius:8px; padding:12px; margin-bottom:8px; display:flex; flex-direction:column; }");
        js.append("    .card-row { display:flex; justify-content:space-between; align-items:center; }");
        js.append("    .btn { background:transparent; border:1px solid #00f3ff; color:#00f3ff; padding:6px 12px; border-radius:4px; font-weight:bold; font-size:12px; margin-top:8px; width:100%; }");
        js.append("    .category-header { padding:12px; background:#1a1a2e; margin-top:10px; border-radius:6px; font-family:'Orbitron'; font-size:14px; display:flex; justify-content:space-between; align-items:center; cursor:pointer; border:1px solid rgba(255,255,255,0.1); }");
        js.append("    .cat-blue { border-left:3px solid #00f3ff; }");
        js.append("    .cat-orange { border-left:3px solid #ff9900; }");
        js.append("    .cat-grey { border-left:3px solid #666; }");
        js.append("    .cat-content { display:none; margin-top:5px; }"); 
        js.append("    .cat-content.open { display:block; animation: slideDown 0.2s; }");
        js.append("    @keyframes slideDown { from { opacity:0; transform:translateY(-5px); } to { opacity:1; transform:translateY(0); } }");
        js.append("    .tabs { display:flex; justify-content:space-around; background:#0f0f1a; padding:10px; border-top:1px solid rgba(255,255,255,0.1); }");
        js.append("    .tab-icon { color:#666; font-size:18px; padding:10px; transition:0.2s; }");
        js.append("    .tab-icon.active { color:#00f3ff; text-shadow:0 0 10px #00f3ff; transform:scale(1.2); }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cyber-root';");
        js.append("  root.innerHTML = `");
        js.append("    <div class='top-bar'><span style='font-family:Orbitron; color:#00f3ff;'>COMMAND DECK</span><span style='color:#00ff9d; font-size:10px; border:1px solid #00ff9d; padding:2px;'>ONLINE</span></div>");
        js.append("    <div class='content-area'>");
        js.append("      <div id='view-1' class='view-section active'><div class='card' style='border-color:#00f3ff; box-shadow:0 0 10px rgba(0,243,255,0.1);'><div style='color:#8b9bb4; font-size:11px;'>PENDING UPLOADS</div><div style='font-family:Orbitron; font-size:28px; color:white;' id='dash-count'>--</div></div></div>");
        js.append("      <div id='view-2' class='view-section'><h2 style='font-family:Orbitron'>Mission Log</h2><p style='color:#888'>No active missions.</p></div>");
        js.append("      <div id='view-3' class='view-section'><h2 style='font-family:Orbitron; margin-bottom:10px; font-size:18px;'>Command Deck</h2><div id='calendar-feed'><p style='color:#888'>Scanning network...</p></div><button class='btn' onclick='scanData()' style='margin-top:20px; border-style:dashed;'>FORCE RE-SCAN</button></div>");
        js.append("    </div>"); 
        js.append("    <div class='tabs'><i class='fa-solid fa-chart-line tab-icon active' onclick='switchTab(1, this)'></i><i class='fa-solid fa-crosshairs tab-icon' onclick='switchTab(2, this)'></i><i class='fa-solid fa-calendar tab-icon' onclick='switchTab(3, this)'></i></div>");
        js.append("  `;");
        js.append("  document.body.appendChild(root);");

        js.append("  window.switchTab = function(id, el) { var views = document.querySelectorAll('.view-section'); for(var i=0; i<views.length; i++) { views[i].classList.remove('active'); } document.getElementById('view-'+id).classList.add('active'); var icons = document.querySelectorAll('.tab-icon'); for(var j=0; j<icons.length; j++) { icons[j].classList.remove('active'); } el.classList.add('active'); };");
        js.append("  window.toggleSection = function(id, header) { var content = document.getElementById(id); if(content.classList.contains('open')) { content.classList.remove('open'); header.querySelector('i').className = 'fa-solid fa-chevron-down'; } else { content.classList.add('open'); header.querySelector('i').className = 'fa-solid fa-chevron-up'; } };");

        js.append("  window.scanData = function() {");
        js.append("    var count = 0; var actionHTML = ''; var warmHTML = ''; var doneHTML = '';"); 
        js.append("    var rows = document.querySelectorAll('.grid.grid-cols-8.border-b');");
        js.append("    if(rows.length > 0) {");
        js.append("      var day = new Date().getDay(); var col = (day===0)?7:day;");
        js.append("      for(var i=0; i<rows.length; i++) {");
        js.append("        var row = rows[i];");
        js.append("        var txt = row.children[col] ? row.children[col].innerText.toLowerCase() : '';");
        js.append("        var fullRow = row.innerText.toLowerCase();");
        js.append("        var userEl = row.querySelector('.text-gray-900.truncate');");
        js.append("        var username = userEl ? userEl.innerText : 'Unknown';");
        js.append("        var linkEl = row.querySelector('a'); var href = linkEl ? linkEl.href : '';");
        
        js.append("        if(txt.indexOf('scheduled') !== -1) {");
        js.append("          count++; actionHTML += \"<div class='card' style='border-left:3px solid #00f3ff;'>\";");
        js.append("          actionHTML += \"<div class='card-row'><div style='font-weight:bold; color:white; font-size:14px;'>\" + username + \"</div><div style='font-size:10px; color:#00f3ff;'>READY</div></div>\";");
        js.append("          if(href) { actionHTML += \"<button class='btn' onclick='location.href=\\\"\" + href + \"\\\"'>OPEN COCKPIT</button>\"; }");
        js.append("          else { actionHTML += \"<button class='btn' style='border-color:#666; color:#666;'>LINK NOT FOUND</button>\"; }");
        js.append("          actionHTML += \"</div>\";");
        js.append("        } else if(fullRow.indexOf('warming') !== -1 || fullRow.indexOf('niche') !== -1) {");
        js.append("          warmHTML += \"<div class='card' style='border-left:3px solid #ff9900; opacity:0.9;'><div class='card-row'><div style='font-weight:bold; color:#ccc; font-size:14px;'>\" + username + \"</div><div style='font-size:10px; color:#ff9900;'>WARMING</div></div></div>\";");
        js.append("        } else {");
        js.append("          doneHTML += \"<div class='card' style='border-left:3px solid #444; opacity:0.5;'><div class='card-row'><div style='font-weight:bold; color:#888; font-size:14px;'>\" + username + \"</div><div style='font-size:10px; color:#666;'>STANDBY</div></div></div>\";");
        js.append("        }");
        js.append("      }");
        js.append("    }");
        js.append("    var finalHTML = '';");
        js.append("    if(actionHTML !== '') { finalHTML += \"<div class='category-header cat-blue' onclick='toggleSection(\\\"sec-action\\\", this)'><span>⚡ ACTION (\" + count + \")</span><i class='fa-solid fa-chevron-up'></i></div><div id='sec-action' class='cat-content open'>\" + actionHTML + \"</div>\"; }");
        js.append("    if(warmHTML !== '') { finalHTML += \"<div class='category-header cat-orange' onclick='toggleSection(\\\"sec-warm\\\", this)'><span>🔥 WARMING</span><i class='fa-solid fa-chevron-down'></i></div><div id='sec-warm' class='cat-content'>\" + warmHTML + \"</div>\"; }");
        js.append("    if(doneHTML !== '') { finalHTML += \"<div class='category-header cat-grey' onclick='toggleSection(\\\"sec-done\\\", this)'><span>💤 STANDBY</span><i class='fa-solid fa-chevron-down'></i></div><div id='sec-done' class='cat-content'>\" + doneHTML + \"</div>\"; }");
        js.append("    if(finalHTML === '') finalHTML = '<p style=\"color:#666\">System Idle. No data found.</p>';");
        js.append("    document.getElementById('dash-count').innerText = count; document.getElementById('calendar-feed').innerHTML = finalHTML;");
        js.append("  };");
        js.append("  setTimeout(function() { window.scanData(); }, 1500);");
        js.append("})()");
        view.loadUrl(js.toString());
    }

    // =========================================================
    // MODULE 2: MISSION COCKPIT (The "Smart Filter" Update)
    // =========================================================
    private void injectMissionCockpit(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  if(document.getElementById('cockpit-root')) return;");

        // CSS
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Share+Tech+Mono&display=swap');");
        js.append("    body > *:not(#cockpit-root):not([class*='modal']):not([role='dialog']) { display: none !important; }");
        js.append("    #cockpit-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:#00f3ff; z-index:99999; font-family:'Share Tech Mono', monospace; display:flex; flex-direction:column; padding:10px; overflow-y:auto; }");
        js.append("    .cp-header { display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #333; padding-bottom:10px; margin-bottom:15px; }");
        js.append("    .cp-title { font-family:'Orbitron'; font-size:20px; letter-spacing:2px; color:white; }");
        js.append("    .cp-panel { border:1px solid rgba(0,243,255,0.2); background:rgba(19,19,31,0.5); padding:15px; margin-bottom:15px; border-radius:4px; }");
        js.append("    .cp-panel-title { color:#666; font-size:12px; margin-bottom:10px; border-bottom:1px solid #333; text-transform:uppercase; letter-spacing:1px; }");
        js.append("    .data-row { display:flex; justify-content:space-between; margin-bottom:8px; align-items:center; }");
        js.append("    .data-val { color:white; background:#111; padding:6px; border:1px solid #333; font-size:14px; flex:1; margin:0 10px; overflow:hidden; text-overflow:ellipsis; }");
        js.append("    .btn-copy { background:#00f3ff; color:black; border:none; padding:4px 8px; font-weight:bold; cursor:pointer; font-size:10px; border-radius:2px; }");
        js.append("    .action-btn { background:rgba(0,243,255,0.1); border:1px solid #00f3ff; color:#00f3ff; padding:15px; margin-bottom:10px; text-align:center; cursor:pointer; font-weight:bold; width:100%; display:block; }");
        js.append("    div[role='dialog'], .modal, .popup { background-color: #13131f !important; color: white !important; border: 1px solid #00f3ff !important; }");
        js.append("    input, textarea, select { background: #050507 !important; color: white !important; border: 1px solid #333 !important; }");
        js.append("    #return-btn { position:fixed; bottom:20px; left:20px; z-index:999999; background:rgba(0,0,0,0.8); border:1px solid #00f3ff; color:#00f3ff; padding:10px; display:none; }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // HTML
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cockpit-root';");
        js.append("  root.innerHTML = `");
        js.append("    <div class='cp-header'><div class='cp-title'>MISSION COCKPIT</div><div style='color:#00ff9d'>SECURE</div></div>");
        js.append("    <div class='cp-panel'><div class='cp-panel-title'>/// CREDENTIALS</div><div class='data-row'><span>USER</span><span class='data-val' id='cp-user'>Scanning...</span><button class='btn-copy' onclick='copyText(\"cp-user\")'>COPY</button></div><div class='data-row'><span>PASS</span><span class='data-val' id='cp-pass'>See Account Page</span></div></div>");
        js.append("    <div class='cp-panel'><div class='cp-panel-title'>/// ACTIONS</div><button class='action-btn' id='dl-btn' onclick='triggerRealUpload()'>1. INITIALIZE UPLOAD</button><div class='cp-panel-title' style='margin-top:10px;'>METADATA</div><div class='data-row'><span class='data-val' id='cp-caption' style='height:40px;'>Scanning...</span><button class='btn-copy' onclick='copyText(\"cp-caption\")'>COPY</button></div></div>");
        js.append("    <button class='action-btn' style='border-color:#ff0050; color:#ff0050; margin-top:auto;' onclick='history.back()'>ABORT MISSION</button>");
        js.append("  `;");
        js.append("  document.body.appendChild(root);");
        
        js.append("  var returnBtn = document.createElement('button'); returnBtn.id='return-btn'; returnBtn.innerText='RETURN TO COCKPIT'; returnBtn.onclick=function(){document.getElementById('cockpit-root').style.display='flex'; this.style.display='none';}; document.body.appendChild(returnBtn);");

        js.append("  window.copyText = function(id) { Android.copyToClipboard(document.getElementById(id).innerText); };");
        
        js.append("  window.triggerRealUpload = function() {");
        js.append("    var buttons = document.getElementsByTagName('button'); var found = false;");
        js.append("    for(var i=0; i<buttons.length; i++) { if(buttons[i].innerText.toLowerCase().includes('upload this video')) { buttons[i].click(); found = true; break; } }");
        js.append("    if(!found) { var links = document.getElementsByTagName('a'); for(var j=0; j<links.length; j++) { if(links[j].innerText.toLowerCase().includes('upload')) { links[j].click(); found=true; break; } } }");
        js.append("    if(found) { document.getElementById('cockpit-root').style.display = 'none'; document.getElementById('return-btn').style.display = 'block'; }");
        js.append("    else { alert('Target Not Found. Please scroll page.'); }");
        js.append("  };");

        // --- NEW: THE SMART FILTER SCRAPER ---
        js.append("  var attempts = 0;");
        js.append("  var scraper = setInterval(function() {");
        js.append("    attempts++;");
        
        // 1. FIND USER: Look for bold text, filtering out generic system words
        js.append("    var candidates = document.querySelectorAll('h1, h2, .text-xl, .font-bold, strong');");
        js.append("    for(var i=0; i<candidates.length; i++) {");
        js.append("       var txt = candidates[i].innerText.trim();");
        js.append("       if(txt.length > 3 && !txt.includes('Account Manager') && !txt.includes('TokPortal') && !txt.includes('Upload') && !txt.includes('Status')) {");
        js.append("          document.getElementById('cp-user').innerText = txt; break;");
        js.append("       }");
        js.append("    }");

        // 2. FIND CAPTION: Look for 'Description to use'
        js.append("    var allDivs = document.getElementsByTagName('div');");
        js.append("    for(var j=0; j<allDivs.length; j++) {");
        js.append("       if(allDivs[j].innerText.includes('Description to use:')) {");
        // Look for the text box nearby
        js.append("          var sib = allDivs[j].nextElementSibling || allDivs[j].querySelector('p, div, span');");
        js.append("          if(sib && sib.innerText.length > 2) { document.getElementById('cp-caption').innerText = sib.innerText; break; }");
        js.append("       }");
        js.append("    }");
        
        js.append("    if(attempts > 5) clearInterval(scraper);");
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

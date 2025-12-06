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
                handleUrl(view, url);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                handleUrl(view, url);
            }
        });

        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    // --- CENTRAL ROUTER LOGIC ---
    private void handleUrl(WebView view, String url) {
        if (url.contains("#video=")) {
            injectMissionCockpit(view);
            return;
        }
        
        if (url.contains("order") || url.contains("details") || url.contains("job")) {
            injectVideoSelector(view);
            return;
        }

        if (url.contains("/calendar")) {
            injectDashboardUI(view);
            return;
        }

        if (url.contains("account-manager") && !url.contains("login")) {
            view.loadUrl("https://app.tokportal.com/account-manager/calendar");
        }
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

    // --- MODULE 1: COMMAND DECK (Working Scraper) ---
    private void injectDashboardUI(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  var oldCp = document.getElementById('cockpit-root'); if(oldCp) oldCp.remove();");
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
        
        js.append("        var cleanName = encodeURIComponent(username);");
        
        js.append("        if(txt.indexOf('scheduled') !== -1) {");
        js.append("          count++; actionHTML += \"<div class='card' style='border-left:3px solid #00f3ff;'>\";");
        js.append("          actionHTML += \"<div class='card-row'><div style='font-weight:bold; color:white; font-size:14px;'>\" + username + \"</div><div style='font-size:10px; color:#00f3ff;'>READY</div></div>\";");
        
        js.append("          if(href) { actionHTML += \"<button class='btn' onclick='location.href=\\\"\" + href + \"#user=\" + cleanName + \"\\\"'>OPEN COCKPIT</button>\"; }");
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
    // MODULE 2: VIDEO SELECTOR SCREEN (Adaptive Poller)
    // =========================================================
    private void injectVideoSelector(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  if(document.getElementById('selector-root')) return;");
        
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Inter:wght@300;400;600&display=swap');");
        js.append("    body > *:not(#selector-root) { visibility: hidden !important; }"); 
        js.append("    #selector-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:white; z-index:99999; font-family:'Inter',sans-serif; padding:15px; overflow-y:auto; }");
        js.append("    .sel-btn { background:#13131f; border:1px solid #00f3ff; color:white; padding:15px; margin-bottom:10px; text-align:left; font-weight:bold; width:100%; display:block; }");
        js.append("    .sel-status { float:right; color:#00f3ff; font-weight:normal; }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'selector-root';");
        js.append("  root.innerHTML = `");
        js.append("    <h2 style='font-family:Orbitron; color:white; margin-bottom:20px;'>TARGET ACQUISITION</h2>");
        js.append("    <p style='color:#ccc; margin-bottom:10px;'>Select the video mission to initiate cockpit view:</p>");
        js.append("    <div id='video-list'>Scanning videos...</div>");
        js.append("    <button class='sel-btn' style='margin-top:20px; border-color:#666; color:#666;' onclick='history.back()'>BACK TO CALENDAR</button>");
        js.append("  `;");
        js.append("  document.body.appendChild(root);");

        // --- ADAPTIVE POLLER LOGIC ---
        js.append("  var listContainer = document.getElementById('video-list');");
        js.append("  var currentUrl = window.location.href.split('#')[0];");
        js.append("  var userName = new URLSearchParams(window.location.hash.slice(1)).get('user');");
        js.append("  var attempts = 0;");
        js.append("  var maxAttempts = 10;"); 
        js.append("  var poller = setInterval(function() {");
        js.append("    attempts++;");
        
        // **GOLD STANDARD SELECTOR:** Finds the card container based on confirmed class.
        js.append("    var videoCards = document.querySelectorAll('div[class*=\"rounded-lg shadow-sm border-gray-200\"]:not([class*=\"border-green\"])');"); 
        
        js.append("    if (videoCards.length > 0) {");
        js.append("      clearInterval(poller);");
        
        js.append("      var html = '';");
        js.append("      var videoCount = 0;");
        
        js.append("      videoCards.forEach(function(card, index) {");
        // 1. Find Title (h3 tag)
        js.append("        var titleEl = card.querySelector('h3');");
        
        // 2. Determine Status (based on text content of the card)
        js.append("        var status = card.innerText.includes('Accepted') ? 'READY' : 'SCHEDULED';");
        
        // 3. Filter for TO UPLOAD videos (must contain the Upload button)
        js.append("        var uploadBtn = card.querySelector('button.btn-primary');");

        js.append("        if (titleEl && titleEl.innerText.length > 5 && uploadBtn && uploadBtn.innerText.includes('Upload')) {"); 
        js.append("          videoCount++;");
        js.append("          var cleanTitle = titleEl.innerText.trim();");
        js.append("          var buttonHref = currentUrl + '#video=' + index + '&user=' + userName;");
        js.append("          html += '<button class=\"sel-btn\" onclick=\"location.href=\\'' + buttonHref + '\\'\">' + cleanTitle + '<span class=\"sel-status\">' + status + '</span></button>';");
        js.append("        }");
        js.append("      });");

        js.append("      if(videoCount > 0) { listContainer.innerHTML = html; }");
        js.append("      else { listContainer.innerHTML = '<p style=\"color:#f00;\">No videos available to upload. Check "Videos In Review" section.</p>'; }");

        js.append("    } else if (attempts >= maxAttempts) {");
        js.append("      clearInterval(poller);");
        js.append("      listContainer.innerHTML = '<p style=\"color:#f00;\">Timeout: Videos failed to load dynamically after 10s.</p>';");
        js.append("    }");
        js.append("  }, 1000);"); 

        js.append("})()");
        view.loadUrl(js.toString());
    }

    // =========================================================
    // MODULE 3: MISSION COCKPIT (Final Aggregation Screen)
    // =========================================================
    private void injectMissionCockpit(WebView view) {
        StringBuilder js = new StringBuilder();
        js.append("javascript:(function() {");
        js.append("  if(document.getElementById('cockpit-root')) return;");

        // CSS
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Share+Tech+Mono&display=swap');");
        js.append("    body > *:not(#cockpit-root) { display: none !important; }"); 
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
        js.append("    h1, h2, h3, h4, strong { color: #00f3ff !important; }");
        js.append("    input, textarea, select { background: #050507 !important; color: white !important; border: 1px solid #333 !important; }");
        js.append("    #return-btn { position:fixed; bottom:20px; left:20px; z-index:999999; background:rgba(0,0,0,0.8); border:1px solid #00f3ff; color:#00f3ff; padding:10px; display:none; }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // HTML
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cockpit-root';");
        js.append("  root.innerHTML = `");
        js.append("    <div class='cp-header'><div class='cp-title'>MISSION COCKPIT</div><div style='color:#00ff9d'>SECURE</div></div>");
        js.append("    <div class='cp-panel'><div class='cp-panel-title'>/// CREDENTIALS</div><div class='data-row'><span>USER</span><span class='data-val' id='cp-user'>Detecting...</span><button class='btn-copy' onclick='copyText(\"cp-user\")'>COPY</button></div><div class='data-row'><span>PASS</span><span class='data-val' id='cp-pass'>********</span><button class='btn-copy' onclick='copyText(\"cp-pass\")'>COPY</button></div></div>");
        js.append("    <div class='cp-panel'><div class='cp-panel-title'>/// ACTIONS</div><button class='action-btn' id='dl-btn' onclick='triggerRealUpload()'>1. INITIALIZE UPLOAD</button><div class='cp-panel-title' style='margin-top:10px;'>METADATA</div><div class='data-row'><span class='data-val' id='cp-caption' style='height:40px;'>Scanning...</span><button class='btn-copy' onclick='copyText(\"cp-caption\")'>COPY</button></div></div>");
        js.append("    <div class='cp-panel'><div class='cp-panel-title'>/// DEPLOYMENT</div><input type='text' id='tiktok-link' placeholder='Paste TikTok URL here...' style='width:100%; background:#050507; border:1px solid #333; color:white; padding:10px; margin-bottom:10px; box-sizing:border-box;'><button class='action-btn' style='background:#00f3ff; color:black;' onclick='submitLink()'>2. CONFIRM UPLOAD</button></div>");
        js.append("    <button class='action-btn' style='border-color:#ff0050; color:#ff0050; margin-top:20px;' onclick='history.back()'>ABORT MISSION</button>");
        js.append("  `;");
        js.append("  document.body.appendChild(root);");
        
        js.append("  var returnBtn = document.createElement('button'); returnBtn.id='return-btn'; returnBtn.innerText='RETURN TO COCKPIT'; returnBtn.onclick=function(){document.getElementById('cockpit-root').style.display='flex'; this.style.display='none';}; document.body.appendChild(returnBtn);");

        js.append("  window.copyText = function(id) { Android.copyToClipboard(document.getElementById(id).innerText); };");
        
        // --- DEPLOYMENT LOGIC (SUBMIT LINK) ---
        js.append("  window.submitLink = function() {");
        js.append("    var myLink = document.getElementById('tiktok-link').value;");
        js.append("    if(myLink.length < 5) { alert('Please paste a URL first.'); return; }");
        js.append("    var realInput = document.querySelector('input[name=\\'video_link\\']') || document.querySelector('input[placeholder*=\\'TikTok\\']');");
        js.append("    if(realInput) {");
        js.append("       realInput.value = myLink;");
        js.append("       realInput.dispatchEvent(new Event('input', { bubbles: true }));");
        js.append("       setTimeout(function() {");
        js.append("         var submitBtns = document.querySelectorAll('button');");
        js.append("         for(var k=0; k<submitBtns.length; k++) { if(submitBtns[k].innerText.includes('Submit')) { submitBtns[k].click(); break; } }");
        js.append("         alert('Submission Sent!'); history.back();");
        js.append("       }, 500);");
        js.append("    } else { alert('Could not find submission form. Is the upload modal open?'); }");
        js.append("  };");
        
        // --- AUTO-DRILL LOGIC (INITIALIZE UPLOAD) ---
        js.append("  window.triggerRealUpload = function() {");
        js.append("    var buttons = document.getElementsByTagName('button'); var found = false;");
        
        js.append("    var videoIndex = parseInt(new URLSearchParams(window.location.hash.slice(1)).get('video')) || 0;");
        js.append("    var uploadBtns = document.querySelectorAll('button.btn-primary');"); // Use confirmed class

        js.append("    if(uploadBtns.length > videoIndex) {");
        js.append("       uploadBtns[videoIndex].click();");
        js.append("       found = true;");
        js.append("    }");

        js.append("    if(found) {");
        js.append("       document.getElementById('cockpit-root').style.display = 'none';");
        js.append("       document.getElementById('return-btn').style.display = 'block';");
        js.append("       setInterval(function() {"); // Bunker Buster: Auto-Click "I Understand"
        js.append("          var btns = document.getElementsByTagName('button');");
        js.append("          for(var k=0; k<btns.length; k++) { if(btns[k].innerText.includes('Understand')) { btns[k].click(); } }");
        js.append("       }, 500);");
        js.append("    } else { alert('Upload Button Not Found. Check selector screen.'); }");
        js.append("  };");

        // --- DATA HARVEST (Baton Pass + Targeted Caption Scrape) ---
        js.append("  setTimeout(function() {");
        
        // 1. GET USER FROM URL HASH
        js.append("    if(window.location.hash.includes('user=')) {");
        js.append("       var user = decodeURIComponent(window.location.hash.split('user=')[1].split('&')[0]);");
        js.append("       document.getElementById('cp-user').innerText = user;");
        js.append("    }");
        
        // 2. SCRAPE CAPTION (Based on text content and length)
        js.append("    var captionFound = false;");
        js.append("    var possibleCaptions = document.querySelectorAll('p, div, span, strong');"); 
        js.append("    for(var i=0; i<possibleCaptions.length; i++) {");
        js.append("        var txt = possibleCaptions[i].innerText.trim();");
        js.append("        if(txt.length > 50 && txt.includes('#') && !txt.includes('Target period')) {");
        js.append("           document.getElementById('cp-caption').innerText = txt; captionFound = true; break;");
        js.append("        }");
        js.append("    }");
        js.append("    if(!captionFound) { document.getElementById('cp-caption').innerText = 'Caption scrape failed. Manual copy needed.'; }");
        
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

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
                
                // 1. MISSION COCKPIT (Order Details)
                if (url.contains("order") || url.contains("details") || url.contains("job")) {
                    injectMissionCockpit(view);
                    return;
                }

                // 2. COMMAND DECK (Calendar)
                if (url.contains("/calendar")) {
                    injectDashboardUI(view);
                    return;
                }

                // 3. AUTO-PILOT (Redirect to Calendar)
                if (url.contains("account-manager") && !url.contains("login")) {
                    view.loadUrl("https://app.tokportal.com/account-manager/calendar");
                }
            }
        });

        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    // ==========================================
    // MODULE 1: THE COMMAND DECK (With Tabs!)
    // ==========================================
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
        
        // TABS CSS
        js.append("    .tabs { display:flex; justify-content:space-around; background:#0f0f1a; padding:10px; border-top:1px solid rgba(255,255,255,0.1); }");
        js.append("    .tab-icon { color:#666; font-size:20px; padding:10px; transition:0.2s; }");
        js.append("    .tab-icon.active { color:#00f3ff; text-shadow:0 0 10px #00f3ff; transform:scale(1.2); }");
        js.append("    .view-section { display:none; }");
        js.append("    .view-section.active { display:block; animation: fadeIn 0.3s; }");
        js.append("    @keyframes fadeIn { from { opacity:0; transform:translateY(10px); } to { opacity:1; transform:translateY(0); } }");

        // UI Components
        js.append("    .card { background:#13131f; border:1px solid rgba(255,255,255,0.1); border-radius:8px; padding:12px; margin-bottom:8px; }");
        js.append("    .card-val { font-family:'Orbitron'; font-size:32px; color:white; }");
        js.append("    .btn { background:transparent; border:1px solid #00f3ff; color:#00f3ff; padding:8px; border-radius:4px; font-weight:bold; width:100%; margin-top:5px; }");
        js.append("    .category-header { padding:12px; background:#1a1a2e; margin-top:10px; border-radius:6px; font-family:'Orbitron'; font-size:14px; color:#00f3ff; border:1px solid rgba(255,255,255,0.1); }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // HTML Structure
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cyber-root';");
        js.append("  root.innerHTML = `");
        js.append("    <div class='top-bar'><span style='font-family:Orbitron; color:#00f3ff;'>COMMAND DECK</span><span style='color:#00ff9d; font-size:10px; border:1px solid #00ff9d; padding:2px;'>ONLINE</span></div>");
        
        js.append("    <div class='content-area'>");
        
        // VIEW 1: OVERVIEW (Stats)
        js.append("      <div id='view-1' class='view-section active'>");
        js.append("        <div class='card' style='border-color:#00f3ff; box-shadow:0 0 10px rgba(0,243,255,0.1);'>");
        js.append("          <div style='color:#8b9bb4; font-size:11px;'>PENDING UPLOADS</div>");
        js.append("          <div class='card-val' id='dash-count'>--</div>");
        js.append("        </div>");
        js.append("        <div class='card' style='border-left:4px solid #bd00ff'>");
        js.append("          <div style='color:#8b9bb4; font-size:11px;'>TOTAL XP</div>");
        js.append("          <div class='card-val' style='color:#bd00ff'>1,250</div>");
        js.append("        </div>");
        js.append("      </div>");

        // VIEW 2: MISSIONS (Placeholder)
        js.append("      <div id='view-2' class='view-section'>");
        js.append("        <h2 style='font-family:Orbitron'>Mission Log</h2><p style='color:#888'>No active side missions.</p>");
        js.append("      </div>");

        // VIEW 3: CALENDAR (The Scraper List)
        js.append("      <div id='view-3' class='view-section'>");
        js.append("        <h2 style='font-family:Orbitron; margin-bottom:10px; font-size:18px;'>Active Tasks</h2>");
        js.append("        <div id='calendar-feed'><p style='color:#666; text-align:center;'>Scanning...</p></div>");
        js.append("        <button class='btn' onclick='scanData()' style='margin-top:20px; border-style:dashed;'>FORCE RE-SCAN</button>");
        js.append("      </div>");

        js.append("    </div>"); 
        
        // TABS AT BOTTOM
        js.append("    <div class='tabs'>");
        js.append("      <i class='fa-solid fa-chart-line tab-icon active' onclick='switchTab(1, this)'></i>");
        js.append("      <i class='fa-solid fa-crosshairs tab-icon' onclick='switchTab(2, this)'></i>");
        js.append("      <i class='fa-solid fa-calendar tab-icon' onclick='switchTab(3, this)'></i>");
        js.append("    </div>");
        
        js.append("  `;");
        js.append("  document.body.appendChild(root);");

        // TAB LOGIC
        js.append("  window.switchTab = function(id, el) {");
        js.append("    var views = document.querySelectorAll('.view-section');");
        js.append("    for(var i=0; i<views.length; i++) { views[i].classList.remove('active'); }");
        js.append("    document.getElementById('view-'+id).classList.add('active');");
        js.append("    var icons = document.querySelectorAll('.tab-icon');");
        js.append("    for(var j=0; j<icons.length; j++) { icons[j].classList.remove('active'); }");
        js.append("    el.classList.add('active');");
        js.append("  };");

        // SCRAPER LOGIC
        js.append("  window.scanData = function() {");
        js.append("    var html = ''; var count = 0;");
        js.append("    var allElements = document.body.getElementsByTagName('*');");
        
        js.append("    for(var i=0; i<allElements.length; i++) {");
        js.append("      var el = allElements[i];");
        js.append("      if(el.innerText && el.innerText.toLowerCase() === 'scheduled' && el.tagName === 'SPAN') {");
        js.append("         count++;");
        js.append("         var card = el.parentElement;");
        js.append("         while(card && (!card.className || !card.className.includes('border'))) { card = card.parentElement; if(card === document.body) break; }");
        js.append("         if(!card) continue;");

        js.append("         var username = 'Unknown Agent';");
        js.append("         var boldEl = card.querySelector('strong') || card.querySelector('.font-bold') || card.querySelector('b');");
        js.append("         if(boldEl) username = boldEl.innerText;");
        
        js.append("         var linkEl = card.querySelector('a');");
        js.append("         var url = linkEl ? linkEl.href : '#';");

        js.append("         html += \"<div class='card' style='border-left:3px solid #00f3ff;'>\";");
        js.append("         html += \"<div style='font-weight:bold; color:white;'>\" + username + \"</div>\";");
        js.append("         html += \"<div style='font-size:12px; color:#888;'>STATUS: READY</div>\";");
        js.append("         html += \"<button class='btn' onclick='location.href=\\\"\" + url + \"\\\"'>OPEN COCKPIT</button>\";");
        js.append("         html += \"</div>\";");
        js.append("      }");
        js.append("    }");
        
        js.append("    document.getElementById('dash-count').innerText = count;");
        js.append("    if(count > 0) {");
        js.append("       var header = \"<div class='category-header'>⚡ ACTION REQUIRED (\" + count + \")</div>\";");
        js.append("       document.getElementById('calendar-feed').innerHTML = header + html;");
        js.append("    } else {");
        js.append("       document.getElementById('calendar-feed').innerHTML = \"<p style='color:#666; text-align:center;'>No 'Scheduled' items found.<br>Scan Complete.</p>\";");
        js.append("    }");
        js.append("  };");
        
        js.append("  setTimeout(function() { window.scanData(); }, 1500);");
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

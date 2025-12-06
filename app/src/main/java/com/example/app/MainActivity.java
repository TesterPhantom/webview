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

        // 1. DATA SCRAPING (Now captures names!)
        js.append("  var count = 0;");
        js.append("  var calendarHTML = '';"); // Variable to hold the list of cards
        
        js.append("  var rows = document.querySelectorAll('.grid.grid-cols-8.border-b');");
        js.append("  if(rows.length > 0) {");
        js.append("    var day = new Date().getDay(); var col = (day===0)?7:day;");
        js.append("    for(var i=0; i<rows.length; i++) {");
        js.append("      var row = rows[i];");
        js.append("      var txt = row.children[col] ? row.children[col].innerText.toLowerCase() : '';");
        
        // If we find a scheduled post:
        js.append("      if(txt.indexOf('scheduled') !== -1) {");
        js.append("        count++;");
        // Grab the username from the row
        js.append("        var userEl = row.querySelector('.text-gray-900.truncate');");
        js.append("        var username = userEl ? userEl.innerText : 'Unknown Account';");
        // Add a card to our HTML list
        js.append("        calendarHTML += `<div class='card' style='border-left:3px solid #00f3ff; margin-bottom:10px;'>");
        js.append("             <div style='font-weight:bold; color:white; font-size:16px;'>` + username + `</div>");
        js.append("             <div style='font-size:12px; color:#888; margin-top:4px;'>STATUS: READY FOR UPLOAD</div>");
        js.append("             <button class='btn' onclick='alert(\"Uploading to \" + \"" + username + "\")'>UPLOAD NOW</button>");
        js.append("        </div>`;");
        js.append("      }");
        
        js.append("    }");
        js.append("  }");

        // If no missions found, show a message
        js.append("  if(calendarHTML === '') calendarHTML = '<p style=\"color:#666\">No scheduled uploads for today.</p>';");

        // 2. CSS STYLES
        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = `");
        js.append("    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');");
        js.append("    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Inter:wght@300;400;600&display=swap');");
        js.append("    body > *:not(#cyber-root) { display: none !important; }"); 
        js.append("    #cyber-root { position:fixed; top:0; left:0; width:100%; height:100%; background:#050507; color:white; z-index:99999; font-family:'Inter',sans-serif; display:flex; flex-direction:column; }");
        
        js.append("    .top-bar { padding:20px; display:flex; justify-content:space-between; align-items:center; background:rgba(19,19,31,0.9); border-bottom:1px solid rgba(255,255,255,0.1); }");
        js.append("    .logo { font-family:'Orbitron'; font-size:20px; color:#00f3ff; font-weight:bold; letter-spacing:1px; }");
        js.append("    .content-area { flex:1; overflow-y:auto; padding:20px; position:relative; }");
        js.append("    .view-section { display:none; }");
        js.append("    .view-section.active { display:block; animation: fadeIn 0.3s; }");
        js.append("    @keyframes fadeIn { from { opacity:0; transform:translateY(10px); } to { opacity:1; transform:translateY(0); } }");
        js.append("    .card { background:#13131f; border:1px solid rgba(255,255,255,0.1); border-radius:12px; padding:20px; margin-bottom:15px; }");
        js.append("    .card-val { font-family:'Orbitron'; font-size:32px; color:white; }");
        js.append("    .btn { background:transparent; border:1px solid #00f3ff; color:#00f3ff; padding:10px; border-radius:4px; font-weight:bold; width:100%; margin-top:10px; }");
        js.append("    .tabs { display:flex; justify-content:space-around; background:#0f0f1a; padding:15px; border-top:1px solid rgba(255,255,255,0.1); }");
        js.append("    .tab-icon { color:#666; font-size:20px; padding:10px; transition:0.2s; }");
        js.append("    .tab-icon.active { color:#00f3ff; text-shadow:0 0 10px #00f3ff; transform:scale(1.2); }");
        js.append("  `;");
        js.append("  document.head.appendChild(style);");

        // 3. HTML STRUCTURE
        js.append("  var root = document.createElement('div');");
        js.append("  root.id = 'cyber-root';");
        js.append("  root.innerHTML = `");
        
        js.append("    <div class='top-bar'><div class='logo'>TOKPORTAL</div><div style='color:#00ff9d; font-size:10px; border:1px solid #00ff9d; padding:2px 5px; border-radius:4px;'>ONLINE</div></div>");
        
        js.append("    <div class='content-area'>");
        
        // VIEW 1: OVERVIEW
        js.append("      <div id='view-1' class='view-section active'>");
        js.append("        <div class='card' style='border-color:#00f3ff; box-shadow:0 0 15px rgba(0,243,255,0.1);'>");
        js.append("          <div style='color:#8b9bb4; font-size:12px;'>PENDING UPLOADS</div>");
        js.append("          <div class='card-val'>` + count + `</div>");
        js.append("        </div>");
        js.append("        <div class='card' style='border-left:4px solid #bd00ff'>");
        js.append("          <div style='color:#8b9bb4; font-size:12px;'>TOTAL XP</div>");
        js.append("          <div class='card-val' style='color:#bd00ff'>1,250</div>");
        js.append("        </div>");
        js.append("      </div>");

        // VIEW 2: MISSIONS
        js.append("      <div id='view-2' class='view-section'>");
        js.append("        <h2 style='font-family:Orbitron'>Mission Log</h2><p style='color:#888'>No active missions.</p>");
        js.append("      </div>");

        // VIEW 3: CALENDAR (Now Populated!)
        js.append("      <div id='view-3' class='view-section'>");
        js.append("        <h2 style='font-family:Orbitron; margin-bottom:15px;'>Scheduled Uploads</h2>");
        // Inject the scraping results here:
        js.append("        <div id='calendar-feed'>` + calendarHTML + `</div>");
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
        
        // 4. LOGIC
        js.append("  window.switchTab = function(id, el) {");
        js.append("    var views = document.querySelectorAll('.view-section');");
        js.append("    for(var i=0; i<views.length; i++) { views[i].classList.remove('active'); }");
        js.append("    document.getElementById('view-'+id).classList.add('active');");
        js.append("    var icons = document.querySelectorAll('.tab-icon');");
        js.append("    for(var j=0; j<icons.length; j++) { icons[j].classList.remove('active'); }");
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

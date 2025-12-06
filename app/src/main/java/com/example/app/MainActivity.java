package com.example.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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

        // 2. LONG TERM MEMORY (Keeps you logged in)
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);

        // 3. THE TRANSFORMER CLIENT
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Only run the transformer on the Calendar page
                if (url.contains("/calendar")) {
                    injectCockpitUI();
                }
            }
        });

        // 4. LOAD THE LIVE SITE
        mWebView.loadUrl("https://app.tokportal.com/account-manager/calendar");
    }

    // --- THE "SORTING HAT" LOGIC ---
    private void injectCockpitUI() {
        String js = "javascript:(function() { " +
                // A. STYLES (Cyberpunk Dark Mode)
                "var css = `<style>" +
                "  body { background: #050505; color: #e0e0e0; font-family: sans-serif; padding: 15px; padding-bottom: 50px; }" +
                "  .header { border-bottom: 1px solid #333; padding-bottom: 15px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }" +
                "  .h-title { font-size: 20px; font-weight: bold; letter-spacing: 2px; color: white; }" +
                "  .date-badge { background: #1a1a1a; padding: 5px 10px; border-radius: 4px; color: #00f3ff; font-family: monospace; font-size: 12px; }" +
                
                // Accordion Buttons
                "  .section-btn { background: #111; border: 1px solid #333; width: 100%; padding: 15px; text-align: left; font-size: 16px; font-weight: bold; color: #888; margin-top: 12px; cursor: pointer; display: flex; justify-content: space-between; transition: 0.2s; border-radius: 4px; }" +
                "  .section-btn.active { color: white; border-color: #555; background: #1a1a1a; }" +
                "  .count-badge { background: #333; color: white; padding: 2px 8px; border-radius: 10px; font-size: 12px; }" +
                
                // Content Areas
                "  .section-content { display: none; padding: 10px 0; animation: fadeIn 0.3s; }" +
                "  .section-content.show { display: block; }" +
                "  @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }" +
                
                // Colors for Sections
                "  .btn-action { border-left: 4px solid #00f3ff; }" + // Neon Blue
                "  .btn-warming { border-left: 4px solid #ff9900; }" + // Neon Orange
                "  .btn-done { border-left: 4px solid #444; }" +      // Grey
                
                // Card Design
                "  .card { background: #0c0c0c; border: 1px solid #222; margin-bottom: 8px; padding: 15px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; }" +
                "  .card-left { display: flex; flex-direction: column; gap: 4px; }" +
                "  .card-user { font-weight: bold; color: #fff; font-size: 15px; }" +
                "  .card-meta { color: #555; font-size: 11px; }" +
                "  .tag { font-size: 10px; padding: 3px 6px; border-radius: 3px; text-transform: uppercase; font-weight: bold; }" +
                "  .tag-sched { background: rgba(0, 243, 255, 0.15); color: #00f3ff; }" +
                "  .tag-warm { background: rgba(255, 153, 0, 0.15); color: #ff9900; }" +
                "  .tag-review { border: 1px solid #333; color: #666; }" +
                
                // Refresh Button
                "  .refresh-btn { position: fixed; bottom: 20px; right: 20px; width: 50px; height: 50px; background: #00f3ff; border-radius: 50%; border: none; box-shadow: 0 0 15px rgba(0,243,255,0.4); font-size: 24px; color: #000; cursor: pointer; z-index: 100; }" +
                "</style>`;" +

                // B. THE ENGINE
                "var today = new Date();" +
                "var dayIndex = today.getDay();" + // 0=Sun, 1=Mon...
                // Map JS Day (0-6) to Website Grid Column (1-7). Note: Website Mon=Col 1, Sun=Col 7.
                "var gridCol = (dayIndex === 0) ? 7 : dayIndex;" + 
                
                "var actionHTML = ''; var actionCount = 0;" +
                "var warmHTML = ''; var warmCount = 0;" +
                "var doneHTML = ''; var doneCount = 0;" +

                // Loop through every row on the website
                "var rows = document.querySelectorAll('.grid.grid-cols-8.border-b');" +
                "rows.forEach(function(row) {" +
                "   var userEl = row.querySelector('.text-gray-900.truncate');" +
                "   if(userEl) {" +
                "       var username = userEl.innerText;" +
                "       var fullText = row.innerText.toLowerCase();" +
                
                "       // Get the specific cell for TODAY" +
                "       var todayCell = row.children[gridCol];" +
                "       var todayText = todayCell ? todayCell.innerText.toLowerCase() : '';" +

                "       // --- PRIORITY 1: ACTION REQUIRED --- " +
                "       // Only if there is a 'scheduled' video for TODAY" +
                "       if (todayText.includes('scheduled')) {" +
                "           actionCount++;" +
                "           actionHTML += `<div class='card'><div class='card-left'><span class='card-user'>`+username+`</span><span class='card-meta'>READY FOR UPLOAD</span></div><span class='tag tag-sched'>SCHEDULED</span></div>`;" +
                "       }" +
                
                "       // --- PRIORITY 2: WARMING --- " +
                "       // If not scheduled, but account is Warming or Niche" +
                "       else if (fullText.includes('warming') || fullText.includes('niche')) {" +
                "           warmCount++;" +
                "           var type = fullText.includes('niche') ? 'NICHE' : 'WARMING';" +
                "           warmHTML += `<div class='card'><div class='card-left'><span class='card-user'>`+username+`</span><span class='card-meta'>AUTOMATED PROCESS</span></div><span class='tag tag-warm'>`+type+`</span></div>`;" +
                "       }" +
                
                "       // --- PRIORITY 3: NO ACTION --- " +
                "       // In Review, Completed, or Empty" +
                "       else {" +
                "           doneCount++;" +
                "           var status = todayText.includes('review') ? 'IN REVIEW' : 'NO TASK';" +
                "           doneHTML += `<div class='card'><div class='card-left'><span class='card-user' style='color:#666'>`+username+`</span></div><span class='tag tag-review'>`+status+`</span></div>`;" +
                "       }" +
                "   }" +
                "});" +

                // C. RENDER (Build the Screen)
                "document.body.innerHTML = css + " +
                "`<div class='header'>" +
                "   <span class='h-title'>TODAY'S OPS</span>" +
                "   <span class='date-badge'>` + today.toLocaleDateString() + `</span>" +
                " </div>` + " + // Fixed Missing Quote Here!

                // Section 1: ACTION (Starts Open)
                "`<div onclick='toggle(this)' class='section-btn btn-action active'>` + " +
                "   `<span>⚡ ACTION REQUIRED</span> <span class='count-badge'>` + actionCount + `</span></div>` + " +
                "`<div class='section-content show'>` + actionHTML + `</div>` + " +

                // Section 2: WARMING (Starts Closed)
                "`<div onclick='toggle(this)' class='section-btn btn-warming'>` + " +
                "   `<span>🔥 WARMING / NICHE</span> <span class='count-badge'>` + warmCount + `</span></div>` + " +
                "`<div class='section-content'>` + warmHTML + `</div>` + " +

                // Section 3: NO ACTION (Starts Closed)
                "`<div onclick='toggle(this)' class='section-btn btn-done'>` + " +
                "   `<span>💤 NO ACTION</span> <span class='count-badge'>` + doneCount + `</span></div>` + " +
                "`<div class='section-content'>` + doneHTML + `</div>` + " +
                
                // Refresh Button
                "`<button class='refresh-btn' onclick='location.reload()'>↻</button>` + " +
                
                // Toggle Logic Script
                "`<script>function toggle(btn) { " +
                "   btn.classList.toggle('active');" +
                "   var content = btn.nextElementSibling;" +
                "   if (content.style.display === 'block' || content.className.includes('show')) {" +
                "       content.style.display = 'none'; content.classList.remove('show');" +
                "   } else {" +
                "       content.style.display = 'block';" +
                "   }" +
                "}</script>`;" +
                
                "document.body.style.backgroundColor = '#050505';" +
                "})()";

        mWebView.loadUrl(js);
    }

    // 5. SAVE LOGIN ON EXIT
    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }

    // 6. HANDLE BACK BUTTON
    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

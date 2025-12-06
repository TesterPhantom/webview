// --- DASHBOARD INJECTOR v1.0 ---

(function() {
    console.log("Cyberpunk Injection Protocol Initiated...");

    // 1. DATA SCRAPING (The "Brain")
    // We scrape the real data BEFORE we wipe the screen
    let missionCount = 0;
    
    // Scrape Calendar Data (If we are on the calendar page)
    let actionHTML = ""; 
    let actionCount = 0;
    
    // Try to find the calendar grid
    const rows = document.querySelectorAll('.grid.grid-cols-8.border-b');
    if (rows.length > 0) {
        console.log("Calendar Data Detected.");
        
        // Calculate Today's Column
        const today = new Date();
        const dayIndex = today.getDay(); // 0=Sun, 1=Mon...
        // TokPortal Grid: Mon(1) -> Sun(7). 
        const gridCol = (dayIndex === 0) ? 7 : dayIndex;

        rows.forEach(row => {
            const userEl = row.querySelector('.text-gray-900.truncate');
            if(userEl) {
                const username = userEl.innerText;
                const cellText = row.children[gridCol] ? row.children[gridCol].innerText.toLowerCase() : "";
                
                // Logic: Check for Scheduled posts
                if (cellText.includes('scheduled')) {
                    actionCount++;
                    actionHTML += `
                        <div class='mission-card priority-norm'>
                            <div class='mission-header'>
                                <span style='font-size:0.8rem; color:#00f3ff;'><i class='fa-solid fa-clock'></i> SCHEDULED</span>
                            </div>
                            <div class='mission-title'>${username}</div>
                            <p class='mission-desc'>Ready for upload today.</p>
                            <button class='btn-action'>UPLOAD NOW</button>
                        </div>`;
                }
            }
        });
    }

    // 2. THE UI SHELL (The "Body")
    // This is the compressed version of the HTML/CSS we designed
    const css = `
        <style>
            @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');
            @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600&family=Orbitron:wght@500;700&display=swap');
            
            :root { --bg-deep: #050507; --bg-panel: #13131f; --primary: #00f3ff; --secondary: #bd00ff; --text-main: #ffffff; --text-muted: #8b9bb4; }
            
            /* Hide the original website */
            body > *:not(.cyberpunk-overlay) { display: none !important; }
            
            /* The Overlay Container */
            .cyberpunk-overlay {
                position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
                background-color: var(--bg-deep); color: var(--text-main);
                font-family: 'Inter', sans-serif; overflow-y: auto; z-index: 9999;
                background-image: radial-gradient(circle at 90% 20%, rgba(0, 243, 255, 0.05) 0%, transparent 40%);
            }

            .container { padding: 20px; max-width: 600px; margin: 0 auto; }
            
            .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
            .title { font-family: 'Orbitron'; font-size: 1.8rem; letter-spacing: 1px; }
            
            /* Cards */
            .stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 30px; }
            .stat-card { background: var(--bg-panel); border: 1px solid rgba(255,255,255,0.05); border-radius: 12px; padding: 15px; }
            .stat-val { font-family: 'Orbitron'; font-size: 1.5rem; color: white; }
            .stat-lbl { color: var(--text-muted); font-size: 0.8rem; }
            
            /* Missions/List */
            .section-title { font-family: 'Orbitron'; margin-bottom: 15px; color: white; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 10px; }
            .mission-card { background: var(--bg-panel); border: 1px solid rgba(255,255,255,0.05); border-radius: 12px; padding: 20px; margin-bottom: 15px; border-left: 4px solid var(--primary); }
            .mission-title { font-weight: bold; font-size: 1.1rem; margin: 5px 0; }
            .mission-desc { color: var(--text-muted); font-size: 0.9rem; margin-bottom: 15px; }
            .btn-action { width: 100%; padding: 10px; background: transparent; border: 1px solid var(--primary); color: var(--primary); font-family: 'Orbitron'; border-radius: 4px; }
            
            /* Bottom Nav */
            .nav-bar { position: fixed; bottom: 0; left: 0; width: 100%; background: rgba(19,19,31,0.95); padding: 15px; display: flex; justify-content: space-around; border-top: 1px solid rgba(255,255,255,0.05); }
            .nav-btn { color: var(--text-muted); background: none; border: none; font-size: 1.2rem; }
            .nav-btn.active { color: var(--primary); text-shadow: 0 0 10px rgba(0,243,255,0.5); }
        </style>
    `;

    // 3. BUILD THE HTML
    const html = `
        <div class="cyberpunk-overlay">
            ${css}
            <div class="container">
                <div class="header">
                    <div class="title">Command<br>Center</div>
                    <div style="text-align:right">
                        <div style="font-size:0.7rem; color:#888;">STATUS</div>
                        <div style="color:#00ff9d;">ONLINE</div>
                    </div>
                </div>

                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-lbl">Active Missions</div>
                        <div class="stat-val">${actionCount}</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-lbl">XP Earned</div>
                        <div class="stat-val" style="color:var(--secondary)">1,250</div>
                    </div>
                </div>

                <div class="section-title">PRIORITY TASKS</div>
                <div id="mission-list">
                    ${actionCount > 0 ? actionHTML : "<div style='color:#666; text-align:center; padding:20px;'>No scheduled uploads for today.</div>"}
                </div>

                <div style="height: 80px;"></div>
            </div>

            <div class="nav-bar">
                <button class="nav-btn active"><i class="fa-solid fa-chart-line"></i></button>
                <button class="nav-btn"><i class="fa-solid fa-crosshairs"></i></button>
                <button class="nav-btn"><i class="fa-solid fa-calendar"></i></button>
                <button class="nav-btn"><i class="fa-solid fa-gear"></i></button>
            </div>
        </div>
    `;

    // 4. INJECT INTO PAGE
    const div = document.createElement('div');
    div.innerHTML = html;
    document.body.appendChild(div);

})();

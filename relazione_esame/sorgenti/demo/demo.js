const puppeteer = require('puppeteer-core');
const OUT = __dirname;
const BASE = 'http://localhost:4200';
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

(async () => {
  
  const browser = await puppeteer.launch({ executablePath: process.env.CHROME_PATH || '/usr/local/bin/google-chrome', headless: 'new',
    args: ['--no-sandbox', '--window-size=1366,768'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1366, height: 768, deviceScaleFactor: 1 });
  const rec = await page.screencast({ path: require('path').join(__dirname, 'demo.webm') });
  const errors = [];
  page.on('pageerror', (e) => errors.push(e.message));
  page.on('console', (m) => { if (m.type() === 'error') errors.push(m.text()); });

  const CAP = require('./didascalie.json');
  const shot = async (name) => {
    const t = CAP[name];
    if (t) await page.evaluate((t) => {
      let d = document.getElementById('__cap');
      if (!d) { d = document.createElement('div'); d.id = '__cap'; document.body.appendChild(d);
        d.style.cssText = 'position:fixed;left:50%;bottom:22px;transform:translateX(-50%);z-index:99999;background:rgba(18,28,70,.92);color:#fff;font:600 17px Roboto,Arial;padding:10px 20px;border-radius:10px;border-left:5px solid #ff9d2e;max-width:80%;text-align:center;pointer-events:none'; }
      d.textContent = t;
    }, t);
    await sleep(t ? 3600 : 1500); console.log('step', name);
  };
  const go = async (path) => { await page.goto(BASE + path, { waitUntil: 'networkidle0' }); await sleep(900); };
  const clickText = async (sel, text) => {
    const ok = await page.evaluate((sel, text) => {
      const el = [...document.querySelectorAll(sel)].find((e) => e.textContent.includes(text) && !e.disabled);
      if (el) { el.click(); return true; } return false;
    }, sel, text);
    if (!ok) throw new Error(`Elemento non trovato: ${sel} "${text}"`);
    await sleep(900);
  };
  const login = async (ruolo) => {
    await page.evaluate(() => sessionStorage.clear());
    await go('/login');
    await clickText('button', ruolo);
    await clickText('button[type=submit]', 'Entra');
    await page.waitForNetworkIdle();
    await sleep(600);
  };

  await go('/login');
  await shot('01_login');

  // ---------- Giudice sportivo ----------
  await login('Giudice sportivo');
  await shot('02_admin_campionati');
  await clickText('button', 'Genera calendario');
  await page.waitForSelector('mat-dialog-container');
  await shot('02b_genera');
  await clickText('mat-dialog-container button', 'Genera');
  await page.waitForNetworkIdle();
  await shot('02c_generato');
  await go('/classifica?campionato=1');
  await shot('03_classifica');
  await go('/calendario');
  await shot('04_calendario');
  await go('/admin/designazioni');
  await shot('05_designazioni');
  await clickText('button', 'Designa');
  await page.waitForSelector('mat-radio-button');
  await shot('06_designa_dialog');
  await page.keyboard.press('Escape');
  await go('/admin/omologazioni');
  await shot('07_omologazioni', true);

  // ---------- Dirigente ----------
  await login('Dirigente');
  await shot('08_dirigente_squadre');
  await go('/dirigente/rosa/1');
  await shot('09_rosa');
  await go('/dirigente/partite/1');
  await shot('10_partite_squadra');
  await clickText('a', 'Lista gara');
  await page.waitForSelector('mat-checkbox');
  // seleziona le prime 13 atlete + tutti i liberi -> deve comparire l'avviso sui liberi
  await page.evaluate(() => [...document.querySelectorAll('mat-checkbox input')].forEach((i) => i.click()));
  await sleep(400);
  await shot('11_lista_gara_errori');
  // deseleziona un libero e un'atleta in eccesso -> 14 con 2 liberi
  await page.evaluate(() => {
    const liberi = [...document.querySelectorAll('fieldset')].find((f) => f.querySelector('legend').textContent.includes('LIBERO'));
    liberi.querySelectorAll('mat-checkbox input')[2].click();
    const sch = [...document.querySelectorAll('fieldset')].find((f) => f.querySelector('legend').textContent.includes('SCHIACCIATORE'));
    sch.querySelectorAll('mat-checkbox input')[0].click();
  });
  await page.type('input[required][maxlength="120"]', 'Mario Venturi');
  await sleep(400);
  await shot('12_lista_gara_ok');
  await clickText('button', 'Invia lista gara');
  await page.waitForNetworkIdle();
  await shot('13_lista_inviata');

  // ---------- Arbitro ----------
  await login('Arbitro');
  await shot('14_arbitro_designazioni');
  await clickText('button', 'Avvia gara');
  await page.waitForNetworkIdle();
  await clickText('a', 'Compila referto');
  await page.waitForSelector('input[formcontrolname=puntiCasa]');
  const sets = [[25, 21], [23, 25], [25, 24]];
  const pc = await page.$$('input[formcontrolname=puntiCasa]');
  const po = await page.$$('input[formcontrolname=puntiOspite]');
  for (let i = 0; i < 3; i++) { await pc[i].type(String(sets[i][0]), { delay: 60 }); await po[i].type(String(sets[i][1]), { delay: 60 }); }
  await sleep(400);
  await shot('15_referto_errore_set');
  // corregge il terzo set e aggiunge il quarto
  await pc[2].click({ clickCount: 3 }); await pc[2].type('27');
  await po[2].click({ clickCount: 3 }); await po[2].type('25');
  await clickText('button', 'Aggiungi set');
  const pc2 = await page.$$('input[formcontrolname=puntiCasa]');
  const po2 = await page.$$('input[formcontrolname=puntiOspite]');
  await pc2[3].type('25'); await po2[3].type('19');
  const punti = await page.$$('input[formcontrolname=puntiFatti]');
  const valori = [16, 12, 9, 7, 4, 3, 2, 0, 0, 0, 0, 0, 0, 0, 14, 11, 8, 6, 5, 2];
  for (let i = 0; i < Math.min(punti.length, valori.length); i++) {
    if (valori[i] > 0) { await punti[i].click({ clickCount: 3 }); await punti[i].type(String(valori[i])); }
  }
  const ace = await page.$$('input[formcontrolname=ace]');
  await ace[0].click({ clickCount: 3 }); await ace[0].type('3');
  await page.select('select[formcontrolname=cartellino]', 'GIALLO');
  await sleep(300);
  await shot('16_referto_compilato', true);
  await page.evaluate(() => [...document.querySelectorAll('mat-checkbox')].find((c) => c.textContent.includes('Dichiaro')).querySelector('input').click());
  await page.type('input[formcontrolname=passwordFirma]', 'password');
  await sleep(300);
  await shot('17_referto_firma');
  await clickText('button', 'Firma e invia');
  await page.waitForNetworkIdle();
  await sleep(800);
  await shot('18_referto_inviato');

  // ---------- Giudice: omologa il referto appena inviato ----------
  await login('Giudice sportivo');
  await go('/admin/omologazioni');
  await shot('19_omologa_dettaglio', true);
  await clickText('button', 'Omologa risultato');
  await page.waitForNetworkIdle();
  await shot('20_dopo_omologa');

  // ---------- Giocatrice ----------
  await login('Giocatore');
  await shot('21_area_giocatore', true);

  await shot('99_fine');
  await rec.stop();
  console.log('ERRORI CONSOLE:', JSON.stringify(errors));
  await browser.close();
})().catch((e) => { console.error(e); process.exit(1); });

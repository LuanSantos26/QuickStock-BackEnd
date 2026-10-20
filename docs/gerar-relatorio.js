const fs = require('fs');
const path = require('path');
const puppeteer = require('puppeteer-core');
const { PDFDocument, StandardFonts, rgb } = require('pdf-lib');
const pdfjsLib = require('pdfjs-dist/legacy/build/pdf.js');

const root = __dirname;
const templatePath = path.join(root, 'relatorio-integracao.html');
const outPath = path.join(root, 'Relatorio-Integracao-QuickStock-ABNT.pdf');
const workPath = path.join(root, '.relatorio-work.pdf');

const SLOTS = [
  'resumo', 'listailust', 'listatab', 'siglas',
  'sec1', 'sec11', 'sec12', 'sec2', 'sec21', 'sec22', 'sec23',
  'sec3', 'sec31', 'sec32', 'sec33', 'sec34', 'sec35',
  'sec4', 'sec41', 'sec42', 'sec43', 'sec44', 'sec45', 'sec46',
  'sec5', 'sec6', 'ref',
  'fig1', 'fig2', 'fig3', 'fig4', 'fig5',
  'tab1', 'tab2', 'tab3', 'tab4',
];

function browserPath() {
  const candidates = [
    'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe',
    'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    'C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe',
  ];
  const found = candidates.find((item) => fs.existsSync(item));
  if (!found) {
    throw new Error('Chrome ou Edge não encontrado para gerar o PDF.');
  }
  return found;
}

function fill(template, pages) {
  let html = template;
  for (const slot of SLOTS) {
    const value = pages[slot] ? String(pages[slot]) : '—';
    html = html.split(`__P_${slot}__`).join(value);
  }
  return html;
}

async function printPdf(html, destination) {
  const browser = await puppeteer.launch({
    executablePath: browserPath(),
    headless: true,
    args: ['--disable-gpu', '--font-render-hinting=none'],
  });
  try {
    const page = await browser.newPage();
    await page.setContent(html, { waitUntil: 'networkidle0' });
    await page.emulateMediaType('print');
    await page.pdf({
      path: destination,
      format: 'A4',
      printBackground: true,
      preferCSSPageSize: true,
    });
  } finally {
    await browser.close();
  }
}

async function extractPages(pdfPath) {
  const data = new Uint8Array(fs.readFileSync(pdfPath));
  const doc = await pdfjsLib.getDocument({ data, disableWorker: true }).promise;
  const found = {};
  for (let pageNumber = 1; pageNumber <= doc.numPages; pageNumber++) {
    const page = await doc.getPage(pageNumber);
    const content = await page.getTextContent();
    const text = content.items.map((item) => item.str).join('');
    for (const slot of SLOTS) {
      if (text.toLowerCase().includes(`#${slot}#`)) {
        found[slot] = pageNumber - 1;
      }
    }
  }
  return { found, numPages: doc.numPages };
}

function samePages(a, b) {
  return SLOTS.every((slot) => a[slot] === b[slot]);
}

async function stampNumbers(pdfPath, introPdfPage) {
  const bytes = fs.readFileSync(pdfPath);
  const pdf = await PDFDocument.load(bytes);
  const font = await pdf.embedFont(StandardFonts.TimesRoman);
  const pages = pdf.getPages();
  pages.forEach((page, index) => {
    const pdfPage = index + 1;
    if (pdfPage < introPdfPage) return;
    const label = String(pdfPage - 1);
    const size = 10;
    const width = font.widthOfTextAtSize(label, size);
    const { width: pageWidth, height: pageHeight } = page.getSize();
    const x = pageWidth - 56.7 - width;
    const y = pageHeight - 56.7;
    page.drawText(label, { x, y, size, font, color: rgb(0, 0, 0) });
  });
  fs.writeFileSync(outPath, await pdf.save());
}

async function main() {
  const template = fs.readFileSync(templatePath, 'utf8');
  let pages = {};
  let last = {};
  for (let attempt = 0; attempt < 4; attempt++) {
    await printPdf(fill(template, pages), workPath);
    const extracted = await extractPages(workPath);
    last = extracted.found;
    if (samePages(pages, last) && last.sec1) break;
    pages = last;
  }
  if (!last.sec1) {
    throw new Error('Não foi possível localizar a introdução no PDF gerado.');
  }
  const missing = SLOTS.filter((slot) => !last[slot]);
  if (missing.length) {
    throw new Error(`Páginas não localizadas: ${missing.join(', ')}`);
  }
  await stampNumbers(workPath, last.sec1 + 1);
  fs.unlinkSync(workPath);
  console.log(JSON.stringify({ arquivo: outPath, paginas: last }, null, 2));
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});

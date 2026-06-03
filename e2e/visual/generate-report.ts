import * as fs from 'fs';
import * as path from 'path';
import { generateReport, RegressionResult } from './utils';

const RESULTS_DIR = path.join(__dirname, 'results');
const reportPath = path.join(RESULTS_DIR, 'report.html');

function loadAllResults(): RegressionResult[] {
  const files = fs.readdirSync(RESULTS_DIR).filter((f) => f.endsWith('.result.json'));
  return files.map((f) => JSON.parse(fs.readFileSync(path.join(RESULTS_DIR, f), 'utf-8')));
}

function clearAllResults(): void {
  const files = fs.readdirSync(RESULTS_DIR).filter((f) => f.endsWith('.result.json'));
  for (const f of files) {
    fs.unlinkSync(path.join(RESULTS_DIR, f));
  }
}

const results = loadAllResults();
if (results.length === 0) {
  console.log('No visual regression results found. Run `npm run test:visual` first.');
  process.exit(1);
}

generateReport(results, reportPath);
console.log(`\n📊 Visual regression report: ${reportPath}`);
console.log(
  `   总计 ${results.length} 页 · 通过 ${results.filter((r) => r.diffRatio < 0.01).length} · 失败 ${results.filter((r) => r.diffRatio >= 0.01).length}`
);

// 可选：生成后清理临时 JSON，保留 PNG 供人工复查
// 若需要保留原始数据用于后续分析，注释掉下面两行即可
clearAllResults();
console.log('   临时 .result.json 已清理（PNG 截图保留）');

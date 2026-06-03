import * as fs from 'fs';
import * as path from 'path';
import { PNG } from 'pngjs';
import pixelmatch from 'pixelmatch';

export interface CompareResult {
  match: boolean;
  diffPixels: number;
  diffRatio: number;
  totalPixels: number;
  width: number;
  height: number;
  dimensionMismatch: boolean;
}

/**
 * 逐像素比对两张 PNG 截图。
 * 若尺寸不一致，以较大画布为基准，缺失区域以洋红 (magenta) 填充后比对，
 * 确保尺寸差异在 diff 图中清晰可见。
 */
export function compareScreenshots(
  baselinePath: string,
  actualPath: string,
  diffPath: string,
  threshold = 0.1
): CompareResult {
  const baselineBuf = fs.readFileSync(baselinePath);
  const actualBuf = fs.readFileSync(actualPath);
  const baseline = PNG.sync.read(baselineBuf);
  const actual = PNG.sync.read(actualBuf);

  const maxWidth = Math.max(baseline.width, actual.width);
  const maxHeight = Math.max(baseline.height, actual.height);
  const dimensionMismatch = baseline.width !== actual.width || baseline.height !== actual.height;

  const baseCanvas = padToSize(baseline, maxWidth, maxHeight);
  const actualCanvas = padToSize(actual, maxWidth, maxHeight);

  const diff = new PNG({ width: maxWidth, height: maxHeight });
  const diffPixels = pixelmatch(
    baseCanvas.data,
    actualCanvas.data,
    diff.data,
    maxWidth,
    maxHeight,
    { threshold, includeAA: false }
  );

  fs.writeFileSync(diffPath, PNG.sync.write(diff));

  const totalPixels = maxWidth * maxHeight;
  return {
    match: diffPixels === 0 && !dimensionMismatch,
    diffPixels,
    diffRatio: diffPixels / totalPixels,
    totalPixels,
    width: maxWidth,
    height: maxHeight,
    dimensionMismatch,
  };
}

function padToSize(src: PNG, width: number, height: number): PNG {
  if (src.width === width && src.height === height) return src;
  const dst = new PNG({ width, height });
  // Fill with magenta to highlight missing areas
  for (let i = 0; i < dst.data.length; i += 4) {
    dst.data[i] = 255;
    dst.data[i + 1] = 0;
    dst.data[i + 2] = 255;
    dst.data[i + 3] = 255;
  }
  // Copy source into top-left
  for (let y = 0; y < src.height; y++) {
    for (let x = 0; x < src.width; x++) {
      const srcIdx = (y * src.width + x) * 4;
      const dstIdx = (y * width + x) * 4;
      dst.data[dstIdx] = src.data[srcIdx];
      dst.data[dstIdx + 1] = src.data[srcIdx + 1];
      dst.data[dstIdx + 2] = src.data[srcIdx + 2];
      dst.data[dstIdx + 3] = src.data[srcIdx + 3];
    }
  }
  return dst;
}

export interface RegressionResult extends CompareResult {
  name: string;
  baselinePath: string;
  actualPath: string;
  diffPath: string;
}

export function generateReport(results: RegressionResult[], outputPath: string): void {
  const passed = results.filter((r) => r.diffRatio < 0.01 && !r.dimensionMismatch).length;
  const failed = results.length - passed;
  const outDir = path.dirname(outputPath);

  const rows = results
    .map((r) => {
      const statusClass = r.diffRatio < 0.01 && !r.dimensionMismatch ? 'pass' : 'fail';
      const baselineRel = path.relative(outDir, r.baselinePath);
      const actualRel = path.relative(outDir, r.actualPath);
      const diffRel = path.relative(outDir, r.diffPath);
      return `
    <tr class="${statusClass}">
      <td><strong>${r.name}</strong>${r.dimensionMismatch ? ' <span class="dim">(尺寸不一致)</span>' : ''}</td>
      <td>${(r.diffRatio * 100).toFixed(3)}%</td>
      <td>${r.diffPixels.toLocaleString()}</td>
      <td>${r.totalPixels.toLocaleString()}</td>
      <td>
        <div class="imgs">
          <div><label>原型 baseline</label><img src="${baselineRel}" loading="lazy" /></div>
          <div><label>实现 actual</label><img src="${actualRel}" loading="lazy" /></div>
          <div><label>差异 diff</label><img src="${diffRel}" loading="lazy" /></div>
        </div>
      </td>
    </tr>`;
    })
    .join('');

  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Visual Regression Report</title>
<style>
  *{box-sizing:border-box}
  body{font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif;margin:24px;background:#f7f8fa;color:#1f2328;line-height:1.5}
  h1{font-size:24px;font-weight:800;margin-bottom:4px}
  .sub{color:#656d76;font-size:13px;margin-bottom:18px}
  .summary{display:flex;gap:12px;margin-bottom:20px;flex-wrap:wrap}
  .badge{padding:8px 16px;border-radius:10px;font-weight:700;font-size:13px;background:#fff;border:1px solid #d1d9e0}
  .badge.pass{background:#dafbe1;border-color:#aceebb;color:#1a7f37}
  .badge.fail{background:#ffebe9;border-color:#ffcecb;color:#cf222e}
  table{width:100%;border-collapse:separate;border-spacing:0;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 1px 2px rgba(31,35,40,.04);font-size:13px}
  th,td{padding:12px 16px;text-align:left;border-bottom:1px solid #ebeff3}
  th{background:#f6f8fa;font-size:11px;font-weight:700;text-transform:uppercase;color:#656d76;letter-spacing:.4px;position:sticky;top:0;z-index:1}
  tr:last-child td{border-bottom:none}
  tr.pass td{background:rgba(218,251,225,.25)}
  tr.fail td{background:rgba(255,235,233,.25)}
  .imgs{display:flex;gap:10px;flex-wrap:wrap}
  .imgs > div{flex:0 0 auto}
  .imgs img{width:220px;height:auto;border:1px solid #d1d9e0;border-radius:6px;display:block}
  .imgs label{display:block;font-size:10px;font-weight:700;color:#656d76;margin-bottom:4px;text-transform:uppercase;letter-spacing:.3px}
  .dim{color:#656d76;font-weight:400}
  @media (max-width:900px){.imgs img{width:140px}}
</style>
</head>
<body>
<h1>Visual Regression Report</h1>
<div class="sub">生成时间：${new Date().toLocaleString('zh-CN')} · 阈值 0.1 · 容差 1%</div>
<div class="summary">
  <span class="badge pass">通过 ${passed}</span>
  <span class="badge fail">失败 ${failed}</span>
  <span class="badge">总计 ${results.length}</span>
</div>
<table>
<thead>
<tr><th>页面</th><th>差异占比</th><th>差异像素</th><th>总像素</th><th>对比图（原型 / 实现 / Diff）</th></tr>
</thead>
<tbody>${rows}</tbody>
</table>
</body>
</html>`;

  fs.mkdirSync(outDir, { recursive: true });
  fs.writeFileSync(outputPath, html);
}

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 源目录（Vue 打包后的目录）
const sourceDir = path.join(__dirname, '../dist');
// 目标目录（Spring Boot 的 static 目录）
const targetDir = path.join(__dirname, '../../pacakge/src/main/resources/static');

/**
 * 递归复制目录
 */
function copyDir(src, dest) {
  // 创建目标目录
  if (!fs.existsSync(dest)) {
    fs.mkdirSync(dest, { recursive: true });
  }

  // 读取源目录
  const entries = fs.readdirSync(src, { withFileTypes: true });

  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);

    if (entry.isDirectory()) {
      // 递归复制子目录
      copyDir(srcPath, destPath);
    } else {
      // 复制文件
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

/**
 * 清空目标目录
 */
function cleanDir(dir) {
  if (fs.existsSync(dir)) {
    const entries = fs.readdirSync(dir, { withFileTypes: true });

    for (const entry of entries) {
      const filePath = path.join(dir, entry.name);

      if (entry.isDirectory()) {
        // 递归删除子目录
        fs.rmSync(filePath, { recursive: true, force: true });
      } else {
        // 删除文件（保留目录结构中的某些系统文件）
        fs.unlinkSync(filePath);
      }
    }
  }
}

console.log('🚀 开始复制 Vue 打包文件到 Spring Boot...');
console.log(`源目录: ${sourceDir}`);
console.log(`目标目录: ${targetDir}`);

try {
  // 检查源目录是否存在
  if (!fs.existsSync(sourceDir)) {
    console.error('❌ 错误: 请先运行 npm run build 打包项目');
    process.exit(1);
  }

  // 清空目标目录
  console.log('📁 清空目标目录...');
  cleanDir(targetDir);

  // 复制文件
  console.log('📋 复制文件...');
  copyDir(sourceDir, targetDir);

  console.log('✅ 复制完成!');
  console.log(`\n📌 启动 Spring Boot 后访问: http://localhost:8080/api/`);
} catch (error) {
  console.error('❌ 复制失败:', error.message);
  process.exit(1);
}

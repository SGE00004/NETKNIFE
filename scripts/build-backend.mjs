// Compila el reactor Maven completo (todos los modulos backend) con el Maven
// Wrapper y copia el jar ejecutable resultante (producido por platform/app) a
// src-tauri/resources, desde donde Tauri lo empaqueta como recurso de la app.
import { spawnSync } from 'node:child_process';
import { copyFileSync, existsSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const repoRoot = join(here, '..');
const jarSource = join(repoRoot, 'platform', 'app', 'target', 'netknife-backend.jar');
const resourcesDir = join(repoRoot, 'src-tauri', 'resources');
const jarDest = join(resourcesDir, 'netknife-backend.jar');

const isWindows = process.platform === 'win32';
const mvnw = join(repoRoot, isWindows ? 'mvnw.cmd' : 'mvnw');

console.log('Compilando el reactor Maven (mvn clean package)...');
const result = spawnSync(mvnw, ['-q', 'clean', 'package'], {
  cwd: repoRoot,
  stdio: 'inherit',
  shell: isWindows,
});

if (result.status !== 0) {
  console.error('Fallo al compilar el backend.');
  process.exit(result.status ?? 1);
}

if (!existsSync(jarSource)) {
  console.error(`No se encontro el jar generado en ${jarSource}`);
  process.exit(1);
}

mkdirSync(resourcesDir, { recursive: true });
copyFileSync(jarSource, jarDest);
console.log(`Jar copiado a ${jarDest}`);

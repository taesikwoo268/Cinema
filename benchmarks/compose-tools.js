import { spawnSync } from 'node:child_process';

export function compose(args, input) {
  // Fixed isolated project. Never infer a production database from environment URLs.
  const result = spawnSync('docker', ['compose', '--env-file', '.env.m12', '-p', 'cinema-bench', ...args], {
    encoding: 'utf8', input, maxBuffer: 8 * 1024 * 1024,
  });
  if (result.error || result.status !== 0) throw new Error(`Compose command failed: ${args[0]}. Inspect Docker locally.`);
  return result.stdout.trim();
}

export function sql(statement) {
  return compose(['exec', '-T', 'db', 'sh', '-c',
    'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql --batch --skip-column-names -u "$MYSQL_USER" "$MYSQL_DATABASE"'], statement);
}

export function guard() {
  if (process.env.CONFIRM_BENCHMARK !== 'cinema-bench') throw new Error('Set CONFIRM_BENCHMARK=cinema-bench');
  const url = new URL(process.env.BASE_URL ?? 'http://127.0.0.1:8082');
  if (url.origin !== 'http://127.0.0.1:8082' || url.pathname !== '/' || url.search || url.username || url.password) {
    throw new Error('This runner is restricted to the isolated stack at http://127.0.0.1:8082');
  }
  const binding = compose(['port', 'app', '8080']);
  if (binding !== '127.0.0.1:8082') throw new Error('Unexpected app port; refusing benchmark');
  return url.origin;
}

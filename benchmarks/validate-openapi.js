import SwaggerParser from '@apidevtools/swagger-parser';
import { readFileSync, readdirSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = fileURLToPath(new URL('../', import.meta.url));
const spec = await SwaggerParser.validate(path.join(root, 'docs/openapi/cinema-v1.yaml'));
const expected = new Set();
for (const file of readdirSync(path.join(root, 'src/main/java/cinema/web/controllers'))) {
  if (!file.endsWith('Controller.java')) continue;
  const code = readFileSync(path.join(root, 'src/main/java/cinema/web/controllers', file), 'utf8');
  const base = code.match(/@RequestMapping\("([^"]+)"\)/)?.[1];
  for (const match of code.matchAll(/@(Get|Post|Delete)Mapping(?:\("([^"]*)"\))?/g)) {
    expected.add(`${match[1].toLowerCase()} ${base}${match[2] ?? ''}`);
  }
}
const actual = new Set(Object.entries(spec.paths).flatMap(([p, operations]) =>
  Object.keys(operations).filter(m => ['get', 'post', 'delete'].includes(m)).map(m => `${m} ${p}`)));
if (expected.size !== actual.size || [...expected].some(key => !actual.has(key))) {
  throw new Error(`Endpoint drift: missing=${[...expected].filter(x => !actual.has(x))}; extra=${[...actual].filter(x => !expected.has(x))}`);
}
for (const folder of ['web/requests', 'web/responses', 'application/common/models', 'application/venues/models']) {
  const dir = path.join(root, 'src/main/java/cinema', folder);
  for (const file of readdirSync(dir)) {
    const code = readFileSync(path.join(dir, file), 'utf8');
    const record = code.match(/public record (\w+)\(([^)]*)\)/s);
    if (!record || !spec.components.schemas[record[1]]) continue;
    const fields = record[2].split(',').map(s => s.trim().split(/\s+/).at(-1)).sort();
    const documented = Object.keys(spec.components.schemas[record[1]].properties).sort();
    if (fields.join() !== documented.join()) throw new Error(`DTO field drift: ${record[1]}`);
  }
}
console.log(`OpenAPI valid; ${actual.size} operations match controllers; documented DTO fields match Java records.`);

import { randomUUID } from 'node:crypto';
import { mkdirSync, writeFileSync, existsSync } from 'node:fs';
import { guard, sql } from './compose-tools.js';

const base = guard();
const runId = randomUUID();
const count = Number(process.env.BOOKING_COUNT ?? 30);
if (!Number.isInteger(count) || count < 1 || count > 200) throw new Error('BOOKING_COUNT must be 1..200');
if (existsSync('benchmarks/fixtures/current.json')) throw new Error('Archive/remove the old fixture first; each run needs fresh seats.');
async function api(method, path, body, token, expected = 200) {
  const response = await fetch(base + path, {method, headers: {
    'Content-Type': 'application/json', ...(token ? {Authorization: `Bearer ${token}`} : {}),
  }, body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(15000)});
  if (response.status !== expected) throw new Error(`${method} ${path}: HTTP ${response.status}`);
  return expected === 204 ? undefined : response.json();
}
async function account() {
  const email = `bench-${randomUUID()}@example.invalid`, password = randomUUID();
  const customer = await api('POST', '/identity/register', {email, password, displayName: 'Benchmark'}, undefined, 201);
  const auth = await api('POST', '/identity/login', {email, password});
  return {customerId: customer.id, email, token: auth.accessToken, expiresAt: auth.expiresAt};
}
const admin = await account();
// Email is generated above, not supplied externally. Only the fixed benchmark DB is accessed.
sql(`UPDATE user_records SET role='ADMIN' WHERE email='${admin.email}';`);
const create = async (p, data) => (await api('POST', p, data, admin.token, 201)).id;
const cinemaId = await create('/cinemas', {name: `Benchmark ${runId}`, address: 'Isolated test stack'});
const hallId = await create('/halls', {cinemaId, name: 'Benchmark hall'});
const movieId = await create('/movies', {title: `Benchmark ${runId}`, description: 'M12', durationMinutes: 120, ageRating: 'PG'});
const start = new Date(Date.now() + 7 * 86400000);
const screening = async offset => create('/screenings', {movieId, hallId,
  startTime: new Date(+start + offset).toISOString(), endTime: new Date(+start + offset + 7200000).toISOString(), basePrice: 12, currency: 'USD'});
const screeningId = await screening(0), contentionScreeningId = await screening(10800000);
const bookings = [];
for (let i = 0; i < count; i++) {
  const seatId = await create('/seats', {hallId, row: 'A', number: i + 1, type: 'NORMAL'});
  bookings.push({...await account(), screeningId, seatId});
}
const contention = [];
for (let i = 0; i < 10; i++) contention.push({...await account(), screeningId: contentionScreeningId, seatId: bookings[0].seatId});
mkdirSync('benchmarks/fixtures', {recursive: true});
writeFileSync('benchmarks/fixtures/current.json', JSON.stringify({runId, baseUrl: base, movieId, screeningId, contentionScreeningId, bookings, contention}, null, 2), {mode: 0o600});
console.log(`Seeded ${count} unique booking fixtures and 10 contention accounts. Run ID: ${runId}. Tokens remain in ignored fixture only.`);

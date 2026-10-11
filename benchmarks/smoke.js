import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import assert from 'node:assert/strict';
import { guard, compose, sql } from './compose-tools.js';

const base = guard();
const data = JSON.parse(readFileSync('benchmarks/fixtures/current.json', 'utf8'));
const customer = data.bookings[0];
async function call(path, method = 'GET', token, body) {
  return fetch(base + path, {
    method, headers: { 'Content-Type': 'application/json', 'Connection': 'close', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(15000)
  });
}
assert.equal((await call('/identity/me')).status, 403);
assert.equal((await call('/identity/me', 'GET', 'invalid')).status, 403);
assert.equal((await call('/identity/me', 'GET', customer.token)).status, 200);
assert.equal((await call('/cinemas', 'POST', customer.token, { name: 'Denied', address: 'Denied' })).status, 403);
const ticketsBefore = await (await call('/tickets/me', 'GET', customer.token)).json();
const bookingsBefore = await (await call('/bookings/me', 'GET', customer.token)).json();
assert.equal(ticketsBefore.length, 1);
assert.equal(bookingsBefore.length, 1);
assert.equal(bookingsBefore[0].status, 'CONFIRMED');
const foreign = data.contention[0];
assert.equal((await call(`/tickets/${ticketsBefore[0].id}`, 'GET', foreign.token)).status, 403);
assert.equal((await call(`/payments/${bookingsBefore[0].id}`, 'POST', foreign.token)).status, 403);
const before = sql('SELECT (SELECT COUNT(*) FROM user_records), (SELECT COUNT(*) FROM customer_records), (SELECT COUNT(*) FROM ticket_records), (SELECT COUNT(*) FROM booking_records), (SELECT COUNT(*) FROM access_token_records);');
const dbContainerBefore = compose(['ps', '-q', 'db']);
compose(['down']); // No -v: retain this project's named volume.
compose(['up', '-d', '--wait']);
const after = sql('SELECT (SELECT COUNT(*) FROM user_records), (SELECT COUNT(*) FROM customer_records), (SELECT COUNT(*) FROM ticket_records), (SELECT COUNT(*) FROM booking_records), (SELECT COUNT(*) FROM access_token_records);');
assert.equal(before, after, 'Persistent counts changed across container recreation');
assert.notEqual(dbContainerBefore, compose(['ps', '-q', 'db']), 'Expected recreated database container');
assert.equal((await call('/identity/me', 'GET', customer.token)).status, 200, 'Token must survive DB/app restart');
assert.deepEqual(await (await call('/tickets/me', 'GET', customer.token)).json(), ticketsBefore);
assert.deepEqual(await (await call('/bookings/me', 'GET', customer.token)).json(), bookingsBefore);
const report = { runId: data.runId, status: 'passed', countsBefore: before.split('\t').map(Number), countsAfter: after.split('\t').map(Number), checks: ['anonymous rejected', 'invalid token rejected', 'real token accepted', 'customer denied admin creation', 'foreign ticket/payment denied', 'container recreation preserves users/customers/tickets/bookings/tokens', 'token valid after restart', 'same ticket and confirmed booking payloads after restart'] };
mkdirSync('benchmarks/results', { recursive: true });
writeFileSync('benchmarks/results/smoke.json', JSON.stringify(report, null, 2));
console.log('HTTP security and container restart persistence smoke checks passed.');

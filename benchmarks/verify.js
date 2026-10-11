import { readFileSync, mkdirSync, writeFileSync } from 'node:fs';
import { guard, sql } from './compose-tools.js';
guard();
const data = JSON.parse(readFileSync('benchmarks/fixtures/current.json', 'utf8'));
for (const id of [data.screeningId, data.contentionScreeningId]) if (!/^[a-f0-9-]{36}$/.test(id)) throw new Error('Invalid fixture ID');
const results = {};
for (const [key, screening, expected] of [['booking', data.screeningId, data.bookings.length], ['contention', data.contentionScreeningId, 1]]) {
  const values = sql(`SELECT
    (SELECT COUNT(*) FROM booking_records WHERE screening_id='${screening}' AND status='CONFIRMED'),
    (SELECT COUNT(*) FROM seat_reservation_records WHERE screening_id='${screening}'),
    (SELECT COUNT(*) FROM ticket_records WHERE screening_id='${screening}'),
    (SELECT COUNT(*) FROM payment_records p JOIN booking_records b ON p.booking_id=b.id WHERE b.screening_id='${screening}' AND p.status='SUCCESS');`).split('\t').map(Number);
  if (values.length !== 4 || values.some(n => n !== expected)) throw new Error(`${key} consistency mismatch: ${values}`);
  results[key] = {confirmed: values[0], reservations: values[1], tickets: values[2], successfulPayments: values[3]};
}
mkdirSync('benchmarks/results', {recursive: true});
writeFileSync('benchmarks/results/consistency.json', JSON.stringify({runId: data.runId, ...results}, null, 2));
console.log(JSON.stringify(results));

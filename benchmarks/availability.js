import { check, fail } from 'k6';
import { fixture, request, trendStats } from './common.js';
export const options = {vus: Number(__ENV.VUS || 1), duration: __ENV.DURATION || '60s', thresholds: {checks: ['rate==1'], http_req_failed: ['rate==0']}, summaryTrendStats: trendStats};
export default function () {
  const seats = request('GET', `/screenings/${fixture.screeningId}/available-seats`, undefined, undefined, 200, 'available-seats').json();
  if (!check(seats, {'valid seat map': xs => Array.isArray(xs) && xs.length === fixture.bookings.length && xs.every(s => typeof s.available === 'boolean' && s.id && s.row && Number.isInteger(s.number))})) fail('Seat map mismatch');
}

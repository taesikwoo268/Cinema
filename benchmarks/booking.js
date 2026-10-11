import exec from 'k6/execution';
import { Counter } from 'k6/metrics';
import { fixture, request, book, trendStats } from './common.js';
const completed = new Counter('completed_bookings');
export const options = {
  scenarios: {booking: {executor: 'shared-iterations', vus: Math.min(5, fixture.bookings.length), iterations: fixture.bookings.length, maxDuration: '3m'}},
  thresholds: {checks: ['rate==1'], http_req_failed: ['rate==0'], dropped_iterations: ['count==0'], completed_bookings: [`count==${fixture.bookings.length}`]},
  summaryTrendStats: trendStats,
};
export default function () {
  const row = fixture.bookings[exec.scenario.iterationInTest];
  request('POST', '/carts/seats', row.token, {screeningId: row.screeningId, seatId: row.seatId}, 204, 'add-seat');
  book(row); completed.add(1);
}

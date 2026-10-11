import exec from 'k6/execution';
import { Counter } from 'k6/metrics';
import { fixture, request, book, trendStats } from './common.js';
const wins = new Counter('seat_winners'), conflicts = new Counter('seat_conflicts');
export const options = {
  scenarios: {contention: {executor: 'per-vu-iterations', vus: 10, iterations: 1, maxDuration: '1m'}},
  thresholds: {checks: ['rate==1'], http_req_failed: ['rate==0'], dropped_iterations: ['count==0'], seat_winners: ['count==1'], seat_conflicts: ['count==9']},
  summaryTrendStats: trendStats,
};
export default function () {
  const row = fixture.contention[exec.vu.idInTest - 1];
  const response = request('POST', '/carts/seats', row.token, {screeningId: row.screeningId, seatId: row.seatId}, 204, 'contended-add', [204, 409]);
  if (response.status === 409) { conflicts.add(1); return; }
  book(row); wins.add(1);
}

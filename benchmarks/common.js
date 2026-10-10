import http from 'k6/http';
import { check, fail } from 'k6';

export const base = __ENV.BASE_URL;
if (!base) throw new Error('BASE_URL is required');
export const fixture = JSON.parse(open(__ENV.FIXTURE_FILE || './fixtures/current.json'));
export const trendStats = ['avg', 'min', 'med', 'p(95)', 'p(99)', 'max'];
export function request(method, path, token, body, status, name, allowed = [status]) {
  const response = http.request(method, base + path, body === undefined ? null : JSON.stringify(body), {
    headers: {'Content-Type': 'application/json', ...(token ? {Authorization: `Bearer ${token}`} : {})},
    tags: {name}, timeout: '15s', responseCallback: http.expectedStatuses(...allowed),
  });
  if (!check(response, {[`${name}: expected status`]: r => allowed.includes(r.status)})) fail(`${name}: HTTP ${response.status}`);
  return response;
}
export function book(row) {
  const checkout = request('POST', '/bookings/checkout', row.token, undefined, 200, 'checkout').json();
  if (!check(checkout, {'one booking': r => Array.isArray(r.bookingIds) && r.bookingIds.length === 1})) fail('Checkout mismatch');
  const id = checkout.bookingIds[0];
  const paid = request('POST', `/payments/${id}`, row.token, undefined, 200, 'pay').json();
  if (!check(paid, {'payment success': p => p.successful === true})) fail('Payment declined');
  const booking = request('GET', `/bookings/${id}`, row.token, undefined, 200, 'booking').json();
  if (!check(booking, {'confirmed': b => b.status === 'CONFIRMED'})) fail('Not confirmed');
  const tickets = request('GET', '/tickets/me', row.token, undefined, 200, 'tickets').json();
  if (!check(tickets, {'one matching ticket': ts => Array.isArray(ts) && ts.filter(t => t.bookingId === id && t.screeningId === row.screeningId && t.seatId === row.seatId && t.status === 'VALID').length === 1})) fail('Ticket mismatch');
}

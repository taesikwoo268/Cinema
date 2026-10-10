import { check, fail } from 'k6';
import { fixture, request, trendStats } from './common.js';
export const options = {vus: Number(__ENV.VUS || 1), duration: __ENV.DURATION || '60s', thresholds: {checks: ['rate==1'], http_req_failed: ['rate==0']}, summaryTrendStats: trendStats};
export default function () {
  const movies = request('GET', '/movies', undefined, undefined, 200, 'movies').json();
  const screenings = request('GET', `/screenings?movieId=${fixture.movieId}`, undefined, undefined, 200, 'screenings').json();
  if (!check(movies, {'fixture movie present': xs => Array.isArray(xs) && xs.some(x => x.id === fixture.movieId)}) ||
      !check(screenings, {'fixture screening present': xs => Array.isArray(xs) && xs.some(x => x.id === fixture.screeningId)})) fail('Catalog mismatch');
}

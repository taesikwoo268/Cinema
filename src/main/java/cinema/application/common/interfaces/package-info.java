/**
 * Application boundaries independent of delivery and infrastructure frameworks.
 * {@link cinema.application.common.interfaces.UseCase} is the input boundary;
 * repository and service ports are implemented by outer-layer adapters.
 *
 * <p>Repository lookups use Optional for a missing single result and lists for
 * multiple results. Domain objects cross these boundaries, never JPA entities.
 * CurrentUser exposes an authentication user ID; CustomerRepository resolves it
 * to the business customer ID used by booking and seat-hold operations.
 * IdentityService returns an authentication user ID on registration and an
 * AuthenticationResult on login. PaymentGateway reports provider outcomes through
 * PaymentResult. SeatHoldService identifies a seat by screening ID and seat ID,
 * and identifies its holder by customer ID.
 *
 * <p>These contracts do not require JWT, a real payment provider, or Redis.
 */
package cinema.application.common.interfaces;

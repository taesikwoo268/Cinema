# Backend Package 1: M8, M9, M10

> Historical handover for the original Package 1 checkpoint. Its in-memory/no-JPA/no-Bearer limitations describe that delivery, not the current MySQL profile. Subsequent fixes `f79445b` and `9a6be03` serialize seat holds/reservations and cart creation/mutations/checkout. Package 2 adds durable persistence; Package 3 adds Bearer/roles. See [current progress](cinema-v1-progress.md) and [MySQL guide](mysql.md).

Implemented on latest main (2403699) using the existing Clean Architecture and venue-dev in-memory adapters.

## Behavior and contracts

- Ticket validates every identifier, ticket code and status. Only VALID -> USED and VALID -> CANCELLED are allowed; repeated terminal transitions fail.
- Successful payment is recorded, booking is confirmed, every screening seat is permanently marked booked, and one VALID ticket is issued per booking item. Failed payments leave the booking pending and create no tickets or reservations.
- Payment retries find the saved successful payment before calling the gateway. They complete missing reservations/tickets and return the original result. Stable ticket IDs derive from the booking/seat pair. Unique ID, code and booking-seat checks protect storage; existing ticket status is never reset by retry.
- Booking cancellation and payment synchronize on the shared BookingRepository instance in V1. This serializes concurrent payment attempts and cancellation, including attempts from separate handler instances. A different booking for an already paid screening seat is rejected before charging.
- Paid bookings cannot be cancelled through the pending-booking cancellation use case. Permanent reservations are never released. Even releasing the temporary hold cannot make a paid seat available.
- GET /tickets/me returns only the authenticated customer's tickets; GET /tickets/{id} resolves ownership through the ticket's booking. Application checks CurrentUser, CustomerRepository and BookingRepository, including a defensive ownership check on list results.
- GET /screenings/{id}/available-seats returns the hall's ordered seat map using the existing SeatView contract, including available=false for held/booked seats and cancelled screenings. It is a snapshot, not an acquisition guarantee.
- Development security explicitly permits these routes to reach application authentication and ownership checks. Anonymous and foreign-customer protected operations return 403; missing resources return 404; validation returns 400. The default security profile remains deny-all.
- GlobalExceptionHandler remains the shared HTTP error mapper.

## Necessary contract adjustments

- Retrying an already paid booking now returns its successful PaymentResult (200) instead of rejecting the confirmed booking (409). A confirmed booking without a recorded successful payment remains a conflict.
- PayBookingCommandHandler now requires TicketRepository and SeatReservationRepository; no constructor can silently skip required finalization.
- SeatHoldService exposes isHeld(screeningId, seatId) so availability can account for other customers' holds. The unused database adapter scaffold is abstract and remains unregistered; no JPA behavior is implemented.
- Cancellation translates the domain's invalid transition to application ConflictException so the existing global mapper returns 409 instead of an unhandled server error. Its tests now assert that application boundary while domain transition tests remain unchanged.

## Verification

New domain tests cover validation and transitions. Application tests cover success, failure, retries, partial ticket-write recovery, concurrent duplicate requests, paid-seat conflicts and ownership. Repository tests cover uniqueness and preserving terminal ticket state. Full Spring context MockMvc tests exercise cart -> checkout -> payment -> confirmed booking -> tickets with real in-memory adapters and Spring Security test principals, including anonymous/cross-customer rejection and paid-seat availability. Existing disabled acceptance scaffolds remain disabled.

## V1 limitations

All data is process-local and lost on restart. There is no durable transaction or rollback across payment, booking, reservations and tickets. The shared repository monitor provides process-local serialization only, not multi-instance coordination; gateway calls hold this monitor. A stored successful payment permits retry recovery, but a failure between gateway success and saving that payment cannot be resolved durably. Reads may observe intermediate finalization state. The real configured adapters do not perform external I/O; fault recovery tests cover a failed ticket write, not arbitrary infrastructure failure.

Seat holds have no expiration and are retained after payment; booked-seat state is authoritative. Authentication uses the existing CurrentUser integration: the HTTP integration tests supply Spring Security principals, not a production login/token flow. No JWT, Redis, RabbitMQ, real payment provider, or JPA persistence is added. Ticket codes are identifiers, not signed admission credentials; no ticket admission/cancellation HTTP endpoints are introduced.

## Final validation

`mvn clean test`: BUILD SUCCESS; 708 tests, 0 failures, 0 errors, 8 skipped. The 8 pre-existing disabled acceptance scaffolds are preserved. `git diff --check` passed.

## Files added

- `docs/backend-package-1.md`
- `src/main/java/cinema/application/tickets/queries/TicketAccess.java`
- `src/main/java/cinema/infrastructure/config/TicketConfiguration.java`
- `src/main/java/cinema/infrastructure/persistence/inmemory/InMemoryTicketRepository.java`
- `src/test/java/cinema/application/TicketWorkflowTest.java`
- `src/test/java/cinema/domain/ticket/TicketTest.java`
- `src/test/java/cinema/infrastructure/persistence/inmemory/InMemoryTicketRepositoryTest.java`
- `src/test/java/cinema/web/TicketHttpIntegrationTest.java`

## Files modified

- `src/main/java/cinema/application/bookings/commands/CancelBookingCommandHandler.java`
- `src/main/java/cinema/application/common/interfaces/SeatHoldService.java`
- `src/main/java/cinema/application/payments/commands/PayBookingCommandHandler.java`
- `src/main/java/cinema/application/screenings/queries/GetAvailableSeatsQueryHandler.java`
- `src/main/java/cinema/application/tickets/queries/GetMyTicketsQueryHandler.java`
- `src/main/java/cinema/application/tickets/queries/GetTicketQueryHandler.java`
- `src/main/java/cinema/domain/ticket/Ticket.java`
- `src/main/java/cinema/infrastructure/config/UseCaseConfiguration.java`
- `src/main/java/cinema/infrastructure/persistence/inmemory/SimpleSeatHoldService.java`
- `src/main/java/cinema/infrastructure/seat_hold/DatabaseSeatHoldService.java`
- `src/main/java/cinema/web/controllers/ScreeningController.java`
- `src/main/java/cinema/web/controllers/TicketController.java`
- `src/main/java/cinema/web/security/VenueDevSecurityConfig.java`
- `src/test/java/cinema/application/bookings/commands/CancelBookingCommandHandlerTest.java`
- `src/test/java/cinema/application/payments/commands/PayBookingCommandHandlerTest.java`
- `src/test/java/cinema/web/DefaultVenueSecurityTest.java`

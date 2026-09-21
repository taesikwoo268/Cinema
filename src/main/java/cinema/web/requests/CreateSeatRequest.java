package cinema.web.requests;

/** Nullable number preserves the distinction between missing input and zero. */
public record CreateSeatRequest(String hallId, String row, Integer number, String type) { }


package cinema.domain.cinema;

import java.util.Locale;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;

public final class Seat extends BaseEntity {
    private final String hallId;
    private final String row;
    private final int number;
    private final SeatType type;
    public Seat(String id, String hallId, String row, int number, SeatType type) {
        super(id);
        if (hallId == null || hallId.isBlank()) {
            throw new DomainException("Seat hallId must not be blank");
        }
        if (row == null || row.isBlank()) {
            throw new DomainException("Seat row must not be blank");
        }
        if (number <= 0) {
            throw new DomainException("Seat number must be positive");
        }
        if (type == null) {
            throw new DomainException("Seat type must not be null");
        }
        this.hallId = hallId;
        this.row = row.trim().toUpperCase(Locale.ROOT);
        this.number = number;
        this.type = type;
    }
    public String getHallId() { return hallId; }
    public String getRow() { return row; }
    public int getNumber() { return number; }
    public SeatType getType() { return type; }
}
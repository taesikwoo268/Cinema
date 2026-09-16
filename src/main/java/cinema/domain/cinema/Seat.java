package cinema.domain.cinema;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.List;

/** Domain state skeleton; invariants and transitions are intentionally pending. */
public final class Seat extends BaseEntity {
    private final String hallId;
    private final String row;
    private final int number;
    private final SeatType type;
    public Seat(String id, String hallId, String row, int number, SeatType type) {
        super(id);
        this.hallId = hallId;
        this.row = row;
        this.number = number;
        this.type = type;
    }
    public String getHallId() { return hallId; }
    public String getRow() { return row; }
    public int getNumber() { return number; }
    public SeatType getType() { return type; }
}

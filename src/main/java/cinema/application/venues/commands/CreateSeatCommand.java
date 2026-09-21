package cinema.application.venues.commands;

public record CreateSeatCommand(String hallId, String row, Integer number, String type) {

}
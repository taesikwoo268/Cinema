package cinema.web.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import cinema.application.tickets.queries.GetMyTicketsQueryHandler;
import cinema.application.tickets.queries.GetTicketQueryHandler;
/** HTTP methods, request mapping and response mapping are pending implementation. */
@RestController
@RequestMapping("/tickets")
public final class TicketController {
    private final GetMyTicketsQueryHandler getMyTicketsQuery;
    private final GetTicketQueryHandler getTicketQuery;
    public TicketController(GetMyTicketsQueryHandler getMyTicketsQuery, GetTicketQueryHandler getTicketQuery) {
        this.getMyTicketsQuery = getMyTicketsQuery;
        this.getTicketQuery = getTicketQuery;
    }
}

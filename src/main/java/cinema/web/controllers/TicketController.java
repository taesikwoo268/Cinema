package cinema.web.controllers;

import cinema.application.tickets.queries.*;
import cinema.application.common.models.TicketView;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Profile({"venue-dev", "mysql"})
@RequestMapping("/tickets")
public final class TicketController {
    private final GetMyTicketsQueryHandler getMyTicketsQuery;
    private final GetTicketQueryHandler getTicketQuery;
    public TicketController(GetMyTicketsQueryHandler getMyTicketsQuery, GetTicketQueryHandler getTicketQuery) {
        this.getMyTicketsQuery = getMyTicketsQuery;
        this.getTicketQuery = getTicketQuery;
    }
    @GetMapping("/me") public List<TicketView> mine() { return getMyTicketsQuery.handle(new GetMyTicketsQuery()); }
    @GetMapping("/{id}") public TicketView get(@PathVariable String id) { return getTicketQuery.handle(new GetTicketQuery(id)); }
}

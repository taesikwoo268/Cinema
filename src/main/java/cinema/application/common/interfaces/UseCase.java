package cinema.application.common.interfaces;

public interface UseCase<I, O> { O handle(I input); }

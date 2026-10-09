package cinema.infrastructure.persistence;

class JpaPersistenceTest extends PersistentWorkflowContract {
    @Override String databaseUrl() { return "jdbc:h2:mem:cinema-persistence;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000"; }
}

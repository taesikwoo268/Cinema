package cinema.infrastructure.persistence;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "CINEMA_TEST_MYSQL_URL", matches = ".+")
class MysqlPersistenceTest extends PersistentWorkflowContract {
    @Override String databaseUrl() { return System.getenv("CINEMA_TEST_MYSQL_URL"); }
}

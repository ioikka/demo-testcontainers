package io.ikka.demo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class CassandraIntegrationTest {
    private static final int PORT = 9042;

    private static final HostConfig HOST_CONFIG = new HostConfig()
            .withPortBindings(new PortBinding(
                    Ports.Binding.bindPort(PORT),   // host port
                    new ExposedPort(PORT)           // container port
            ));

    @SuppressWarnings("resource")
    @Container
    static CassandraContainer<?> cassandra = new CassandraContainer<>("cassandra:4.1")
            .withExposedPorts(PORT)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(HOST_CONFIG));

    @Test
    void testWithCassandra() {
        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(cassandra.getHost(), cassandra.getFirstMappedPort()))
                .withLocalDatacenter("datacenter1")
                .build()) {

            session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace " +
                    "WITH replication = {'class':'SimpleStrategy','replication_factor':'1'}");
            session.execute("USE test_keyspace");

            session.execute("CREATE TABLE IF NOT EXISTS users (id UUID PRIMARY KEY, name text)");
            session.execute("INSERT INTO users (id, name) VALUES (uuid(), 'Alice')");

            var rows = session.execute("SELECT name FROM users").all();
            assertThat(rows).hasSize(1);
            assertThat(rows.getFirst().getString("name")).isEqualTo("Alice");
        }
    }
}

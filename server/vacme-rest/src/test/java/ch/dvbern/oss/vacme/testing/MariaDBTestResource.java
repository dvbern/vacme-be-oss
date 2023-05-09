package ch.dvbern.oss.vacme.testing;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class MariaDBTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

	private Optional<String> containerNetworkId;
	private JdbcDatabaseContainer container;

	@Override
	public void setIntegrationTestContext(DevServicesContext context) {
		containerNetworkId = context.containerNetworkId();
	}

	@Override
	public Map<String, String> start() {
		// start a container making sure to call withNetworkMode() with the value of containerNetworkId if present
		container = new MariaDBContainer<>("mariadb:10.3").withLogConsumer(outputFrame -> {});

		// apply the network to the container
		containerNetworkId.ifPresent(container::withNetworkMode);

		// start container before retrieving its URL or other properties
		container.start();

		String jdbcUrl = container.getJdbcUrl();
		if (containerNetworkId.isPresent()) {
			// Replace hostname + port in the provided JDBC URL with the hostname of the Docker container
			// running Mariadb and the listening port.
			jdbcUrl = fixJdbcUrl(jdbcUrl);
		}


		// return a map containing the configuration the application needs to use the service
		return ImmutableMap.of(
			"quarkus.datasource.username", container.getUsername(),
			"quarkus.datasource.password", container.getPassword(),
			"quarkus.datasource.jdbc.url", jdbcUrl,
			"quarkus.datasource.db-kind", "mariadb");
	}

	private String fixJdbcUrl(String jdbcUrl) {
		// Part of the JDBC URL to replace
		String hostPort = container.getHost() + ':' + container.getMappedPort(3306);

		// Host/IP on the container network plus the unmapped port
		String networkHostPort =
			container.getCurrentContainerInfo().getConfig().getHostName()
				+ ':'
				+ 3306;

		String replace = jdbcUrl.replace(hostPort, networkHostPort);
		return replace;
	}

	@Override
	public void stop() {
		// close container
		container.stop();
	}
}

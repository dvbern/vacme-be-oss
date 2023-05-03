/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.rest.health;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;

import com.google.common.base.Suppliers;
import io.smallrye.health.api.Wellness;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Dieser Healthcheck gibt an welche collation verwendet wird von der hier erstellten jdbc connection
 */
@ApplicationScoped
@Wellness
@Slf4j
public class JDBCCollationCheck implements HealthCheck {
	public static final int CACHE_DURATION = 3600;

	private final Supplier<HealthCheckResponseBuilder> memoizedSupplier;

	@ConfigProperty(name = "quarkus.datasource.jdbc.url")
	String jdbcUrl;
	@ConfigProperty(name = "quarkus.datasource.username")
	String user;
	@ConfigProperty(name = "quarkus.datasource.password")
	 String password;

	public JDBCCollationCheck() {
		memoizedSupplier = Suppliers.memoizeWithExpiration(
			this::perfromHealthQuery, CACHE_DURATION, TimeUnit.SECONDS);
	}

	@Override
	public HealthCheckResponse call() {
		// health check is cached for a short time
		HealthCheckResponseBuilder healthCheckResponseBuilder = memoizedSupplier.get();

		return healthCheckResponseBuilder.build();
	}

	private HealthCheckResponseBuilder perfromHealthQuery() {
		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Database JDBC collation health check");
		String sql = "SELECT @@collation_connection;";
		try (Connection con = DriverManager.getConnection(jdbcUrl, user, password);
			PreparedStatement ps = con.prepareStatement(sql)) {

			try (ResultSet rs = ps.executeQuery()) {
				boolean hasARow = rs.next();
				if (!hasARow || rs.getMetaData().getColumnCount() != 1) {
					responseBuilder.down().withData("default", "Resultset not as expected");
				}
				 else{
					responseBuilder.up().withData("collation_connection", rs.getString(1));
				}

			}

		} catch (SQLException ex) {
			// handle any errors
			LOG.error("SQLException: " + ex.getMessage());
			LOG.error("SQLState: " + ex.getSQLState());
			LOG.error("VendorError: " + ex.getErrorCode());
			responseBuilder.down()
				.withData("SQLException", ex.getMessage())
				.withData("SQLState", ex.getSQLState())
				.withData("VendorError", ex.getErrorCode());
		} catch (Exception e) {
			responseBuilder.down().withData("default", e.getMessage());
		}
		return responseBuilder;
	}
}

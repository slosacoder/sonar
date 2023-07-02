/*
 * Copyright (C) 2023 jones
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package jones.sonar.api.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jones.sonar.api.Sonar;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;

public final class MySQLDataStorage implements Database {
  public static final Database INSTANCE = new MySQLDataStorage();
  private @Nullable DataSource dataSource;
  @Getter
  private @Nullable Connection connection;
  public static final String VERIFIED_IPS_TABLE_NAME = "verified_ips";
  public static final String BLACKLISTED_IPS_TABLE_NAME = "blacklisted_ips";

  @Override
  public void initialize(final @NotNull Sonar sonar) {
    if (!sonar.getConfig().DATABASE_ENABLED) return;

    service.execute(() -> {
      final String formattedURL = "jdbc:mysql://"
        + sonar.getConfig().DATABASE_URL
        + ":" + sonar.getConfig().DATABASE_PORT
        + "/" + sonar.getConfig().DATABASE_NAME;

      try {
        // Register MySQL driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Load Hikari
        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl(formattedURL);
        config.setUsername(sonar.getConfig().DATABASE_USERNAME);
        config.setPassword(sonar.getConfig().DATABASE_PASSWORD);

        dataSource = new HikariDataSource(config);
        connection = dataSource.getConnection();

        createTable(VERIFIED_IPS_TABLE_NAME);
        createTable(BLACKLISTED_IPS_TABLE_NAME);
      } catch (Throwable throwable) {
        sonar.getLogger().error("Failed to connect to database: {}", throwable);
      }
    });
  }

  @Override
  public Collection<String> getListFromTable(final String table) {
    if (connection == null) return Collections.emptyList();

    // TODO: add this
    throw new IllegalStateException("not implemented yet");
  }

  @Override
  public void addListToTable(final String table, final Collection<String> collection) {
    if (connection == null) return;

    // TODO: add this
  }
}

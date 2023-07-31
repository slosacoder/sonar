/*
 * Copyright (C) 2023 Sonar Contributors
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

package xyz.jonesdev.sonar.api.controller;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.jonesdev.sonar.api.Sonar;
import xyz.jonesdev.sonar.api.config.SonarConfiguration;
import xyz.jonesdev.sonar.api.model.VerifiedPlayer;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class VerifiedPlayerController {
  private static final Map<String, Collection<UUID>> MAP = new ConcurrentHashMap<>();
  private static final ExecutorService DB_UPDATE_SERVICE = Executors.newSingleThreadExecutor();

  private @Nullable ConnectionSource connectionSource;
  private Dao<VerifiedPlayer, Integer> dao;
  private QueryBuilder<VerifiedPlayer, Integer> queryBuilder;
  @Getter
  private final @NotNull SonarConfiguration.DatabaseType cachedDatabaseType;

  public VerifiedPlayerController() {
    final SonarConfiguration config = Sonar.get().getConfig();

    // Cache selected database type, so we don't need to call Sonar.get() every time
    cachedDatabaseType = config.DATABASE_TYPE;

    // Don't establish a database connection if the type is NONE
    if (cachedDatabaseType == SonarConfiguration.DatabaseType.NONE) {
      Sonar.get().getLogger().warn("Make sure to select a database type to store all verified players.");
      return;
    }

    final String databaseURL = "jdbc:mysql://" + config.MYSQL_URL + ":" + config.MYSQL_PORT + "/" + config.MYSQL_DATABASE;

    try (final ConnectionSource connectionSource = new JdbcConnectionSource(
      databaseURL, config.MYSQL_USER, config.MYSQL_PASSWORD)
    ) {
      this.connectionSource = connectionSource;

      // Create table
      try {
        TableUtils.createTableIfNotExists(connectionSource, VerifiedPlayer.class);
      } catch (SQLException exception) {
        // Duplicate index
      }

      dao = DaoManager.createDao(connectionSource, VerifiedPlayer.class);
      queryBuilder = dao.queryBuilder();

      dao.queryForAll().forEach(this::_add);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }

  public void remove(final @NotNull String inetAddress) {
    _remove(inetAddress);

    // Don't try to update the column if the database type is NONE
    if (cachedDatabaseType == SonarConfiguration.DatabaseType.NONE) {
      return;
    }

    try {
      Objects.requireNonNull(connectionSource);

      final List<VerifiedPlayer> verifiedPlayer = queryBuilder.where()
        .eq("ip_address", inetAddress)
        .query();

      if (verifiedPlayer != null) {
        for (final VerifiedPlayer player : verifiedPlayer) {
          dao.delete(player);
        }
      }
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  private void _remove(final @NotNull String inetAddress) {
    MAP.remove(inetAddress);
  }

  public void add(final @NotNull VerifiedPlayer player) {
    _add(player);

    // Don't try to update the column if the database type is NONE
    if (cachedDatabaseType == SonarConfiguration.DatabaseType.NONE) {
      return;
    }

    DB_UPDATE_SERVICE.execute(() -> {
      Objects.requireNonNull(connectionSource);

      try {
        dao.create(player);
      } catch (SQLException exception) {
        exception.printStackTrace();
      }
    });
  }

  private void _add(final @NotNull VerifiedPlayer player) {
    MAP.computeIfAbsent(player.getInetAddress(), v -> new Vector<>())
      .add(player.getPlayerUUID());
  }

  public synchronized int estimatedSize() {
    return MAP.values().stream()
      .mapToInt(Collection::size)
      .sum();
  }

  public void clearAll() {
    try {
      MAP.clear();

      // Only update the column if the database type is not NONE
      if (cachedDatabaseType != SonarConfiguration.DatabaseType.NONE) {
        dao.deleteBuilder().delete();
      }
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  public boolean has(final @NotNull String inetAddress, final @NotNull UUID uuid) {
    final Collection<UUID> got = MAP.get(inetAddress);
    if (got != null) {
      return got.contains(uuid);
    }
    return false;
  }

  public boolean has(final @NotNull String inetAddress) {
    return MAP.containsKey(inetAddress);
  }
}

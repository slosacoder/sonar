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

package xyz.jonesdev.sonar.api.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.dependencies.Dependency;

import java.net.InetAddress;
import java.util.function.Function;
import java.util.regex.Pattern;

@Getter
@ToString
public final class GeneralConfiguration extends SonarConfiguration {
  private Database database;
  private Lockdown lockdown;
  private Queue queue;
  private Verification verification;

  public GeneralConfiguration(final @NotNull TomlConfiguration tomlConfiguration) {
    super(tomlConfiguration.getConfig());
  }

  @Override
  public void load() {
    config.load();
    needsSave = false;

    // Load general configuration values
    language = getOrElse("language", "en");
    maxOnlinePerIP = config.getIntOrElse("max-online-per-ip", 3);
    logPlayerAddresses = getOrElse("log-player-addresses", true);

    // Load other (sub-)configuration values
    database = new Database(config.get("database"));
    lockdown = new Lockdown(config.get("lockdown"));
    queue = new Queue(config.get("queue"));
    verification = new Verification(config.get("verification"));

    if (needsSave) {
      config.save();
    }
  }

  /* ======================================================== */
  private String language;
  private boolean logPlayerAddresses;
  private int maxOnlinePerIP;
  /* ======================================================== */

  @Getter
  @ToString
  public static final class Database {
    private final Type type;
    private final String url;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
      MYSQL(new Dependency[]{Dependency.MYSQL}, "com.mysql.cj.jdbc.NonRegisteringDriver"),
      MARIADB(new Dependency[]{Dependency.MYSQL, Dependency.MARIADB}, "org.mariadb.jdbc.Driver"),
      NONE(null, null);
      private final Dependency[] dependencies;
      private final String driverClassName;
    }

    public Database(final @NotNull CommentedConfig config) {
      type = config.getEnumOrElse("type", Type.NONE);
      url = config.getOrElse("url", "localhost");
      port = config.getIntOrElse("port", 3306) & 0xffff;
      database = config.getOrElse("database", "sonar");
      username = config.getOrElse("username", "");
      password = config.getOrElse("password", "");
    }
  }

  @Getter
  @ToString
  public static final class Lockdown {
    private final boolean enabled;
    private final boolean logAttempts;
    private final boolean notifyAdmins;

    public Lockdown(final @NotNull CommentedConfig config) {
      enabled = config.getOrElse("enabled", false);
      logAttempts = config.getOrElse("log-attempts", true);
      notifyAdmins = config.getOrElse("notify-admins", true);
    }
  }

  @Getter
  @ToString
  public static final class Queue {
    private final int maxQueuePolls;

    public Queue(final @NotNull CommentedConfig config) {
      maxQueuePolls = config.getIntOrElse("max-polls", 35);
    }
  }

  @Getter
  @ToString
  public static final class Verification {
    private final boolean enabled;
    private final boolean checkClientGravity;
    private final boolean checkClientCollisions;
    private final short gameModeId;
    private final boolean logConnections;
    private final boolean logConnectionsDuringAttack;
    private final Pattern validNameRegex;
    private final Pattern validBrandRegex;
    private final Pattern validLocaleRegex;
    private final int maxBrandLength;
    private final int maxMovementTick;
    private final int minPlayersForAttack;
    private final int maxVerifyingPlayers;
    private final int maxLoginPackets;
    private final int timeout;
    private final int readTimeout;
    private final int connectDelay;

    public Verification(final @NotNull CommentedConfig config) {
      enabled = config.getOrElse("enabled", true);
      checkClientGravity = config.getOrElse("check-gravity", true);
      checkClientCollisions = config.getOrElse("check-collisions", true);
      gameModeId = (short) config.getIntOrElse("gamemode", 3);
      logConnections = config.getOrElse("log-connections", true);
      logConnectionsDuringAttack = config.getOrElse("log-during-attack", false);
      minPlayersForAttack = config.getIntOrElse("min-players-for-attack", 6);
      validNameRegex = Pattern.compile(config.getOrElse("valid-name-regex", "^[a-zA-Z0-9_.*!]+$"));
      validBrandRegex = Pattern.compile(config.getOrElse("valid-brand-regex", "^[!-~ ]+$"));
      validLocaleRegex = Pattern.compile(config.getOrElse("valid-locale-regex", "^[a-zA-Z_]+$"));
      maxBrandLength = config.getIntOrElse("max-brand-length", 86);
      timeout = config.getIntOrElse("timeout", 10000);
      readTimeout = config.getIntOrElse("read-timeout", 5000);
      maxLoginPackets = config.getIntOrElse("max-login-packets", 200);
      maxMovementTick = config.getIntOrElse("max-movement-ticks", 8);
      maxVerifyingPlayers = config.getIntOrElse("max-verifying-players", 1024);
      connectDelay = config.getIntOrElse("rejoin-delay", 8000);
    }
  }

  private final Function<InetAddress, String> addressParser = inetAddress -> logPlayerAddresses ?
    inetAddress.toString() : "<ip address withheld>";
}

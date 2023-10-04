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
import lombok.ToString;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.Sonar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@ToString
public final class MessagesConfiguration extends SonarConfiguration {
  private final Lockdown lockdown = new Lockdown();
  private final Commands commands = new Commands();
  private final Verification verification = new Verification();
  private final ActionBar actionBar = new ActionBar();

  public MessagesConfiguration(final @NotNull TomlConfiguration tomlConfiguration) {
    super(tomlConfiguration.getConfig());
  }

  public void load() {
    config.load();
    needsSave = false;

    // Load general configuration values
    prefix = getOrElse("prefix", "<rainbow>Sonar</rainbow> <gray>» <reset>");
    supportUrl = getOrElse("support-url", "https://jonesdev.xyz/discord/");
    header = parseFromList(getOrElse("header", Arrays.asList("<yellow><bold>Sonar", "<reset>")))
      .replace("%prefix%", prefix);
    footer = parseFromList(getOrElse("footer", Arrays.asList("<gray>If you believe that this is an error, contact an administrator.")))
      .replace("%prefix%", prefix);
    tooManyOnlinePerIP = toComponent(format(parseFromList(getOrElse("too-many-online-per-ip", new ArrayList<>()))));

    // Load other (sub-)configuration values
    lockdown.load(config.get("lockdown"));
    commands.load(config.get("commands"));
    verification.load(config.get("verification"));
    actionBar.load(config.get("action-bar"));

    if (needsSave) {
      config.save();
    }
  }

  private static @NotNull String format(final @NotNull String string) {
    return string
      .replace("%header%", Sonar.get().getTranslations().getHeader())
      .replace("%footer%", Sonar.get().getTranslations().getFooter())
      .replace("%prefix%", Sonar.get().getTranslations().getPrefix());
  }

  /* ======================================================== */
  private String prefix;
  private String supportUrl;
  private String header;
  private String footer;
  private Component tooManyOnlinePerIP;
  /* ======================================================== */

  @Getter
  @ToString
  public static final class Lockdown {
    private String enabled;
    private String disabled;
    private String notification;
    private String consoleLog;
    private Component disconnect;

    public void load(final @NotNull CommentedConfig config) {
      enabled = format(config.get("enabled"));
      disabled = format(config.get("disabled"));
      notification = format(config.get("notification"));
      consoleLog = format(config.get("console-log"));
      disconnect = toComponent(format(parseFromList(config.get("disconnect-message"))));
    }
  }

  @Getter
  @ToString
  public static final class Commands {
    private String incorrectUsage;
    private String invalidIPAddress;
    private String illegalIPAddress;
    private String playerOnlyError;
    private String consoleOnlyError;
    private String cooldown;
    private String cooldownLeft;
    private String subcommandNoPerm;

    public void load(final @NotNull CommentedConfig config) {
      incorrectUsage = format(config.get("incorrect-command-usage"));
      invalidIPAddress = format(config.get("invalid-ip-address"));
      illegalIPAddress = format(config.get("illegal-ip-address"));
      playerOnlyError = format(config.get("player-only"));
      consoleOnlyError = format(config.get("console-only"));
      cooldown = format(config.get("cool-down"));
      cooldownLeft = format(config.get("cool-down-left"));
      subcommandNoPerm = format(config.get("sub-command-no-permission"));
    }

    @Getter
    @ToString
    public static final class Reload {
      private String start;
      private String finish;

      public void load(final @NotNull CommentedConfig config) {
        start = format(config.get("start"));
        finish = format(config.get("finish"));
      }
    }

    @Getter
    @ToString
    public static final class Verbose {
      private String subscribed;
      private String unsubscribed;

      public void load(final @NotNull CommentedConfig config) {
        subscribed = format(config.get("subscribed"));
        unsubscribed = format(config.get("unsubscribed"));
      }
    }
  }

  @Getter
  @ToString
  public static final class Verification {
    private String connectionLogMessage;
    private String failedLogMessage;
    private String successLogMessage;
    private String blacklistLogMessage;
    private Component tooManyPlayers;
    private Component tooFastReconnect;
    private Component alreadyVerifying;
    private Component alreadyQueued;
    private Component blacklisted;
    private Component invalidUsername;
    private Component success;
    private Component failed;

    public void load(final @NotNull CommentedConfig config) {
      connectionLogMessage = config.get("logs.connection");
      failedLogMessage = config.get("logs.failed");
      successLogMessage = config.get("logs.successful");
      blacklistLogMessage = config.get("logs.blacklisted");
      tooManyPlayers = toComponent(format(parseFromList(config.get("too-many-players"))));
      tooFastReconnect = toComponent(format(parseFromList(config.get("too-fast-reconnect"))));
      alreadyVerifying = toComponent(format(parseFromList(config.get("already-verifying"))));
      alreadyQueued = toComponent(format(parseFromList(config.get("already-queued"))));
      blacklisted = toComponent(format(parseFromList(config.get("blacklisted"))));
      invalidUsername = toComponent(format(parseFromList(config.get("invalid-username"))));
      success = toComponent(format(parseFromList(config.get("success"))));
      failed = toComponent(format(parseFromList(config.get("failed"))));
    }
  }

  @Getter
  @ToString
  public static final class ActionBar {
    private String layout;
    private List<String> animation;

    public void load(final @NotNull CommentedConfig config) {
      layout = config.get("layout");
      animation = config.get("animation");
    }
  }

  public String BLACKLIST_EMPTY;
  public String BLACKLIST_ADD;
  public String BLACKLIST_ADD_WARNING;
  public String BLACKLIST_DUPLICATE;
  public String BLACKLIST_NOT_FOUND;
  public String BLACKLIST_REMOVE;
  public String BLACKLIST_CLEARED;
  public String BLACKLIST_SIZE;

  public String VERIFIED_REMOVE;
  public String VERIFIED_NOT_FOUND;
  public String VERIFIED_CLEARED;
  public String VERIFIED_SIZE;
  public String VERIFIED_EMPTY;
  public String VERIFIED_BLOCKED;

  public String VERBOSE_SUBSCRIBED;
  public String VERBOSE_UNSUBSCRIBED;
  public String VERBOSE_SUBSCRIBED_OTHER;
  public String VERBOSE_UNSUBSCRIBED_OTHER;
  public String RELOADING = "Reloading";
  public String RELOADED = "<green>Successfully reloaded <gray>(%taken%ms)";

  public String DATABASE_PURGE_DISALLOWED;
  public String DATABASE_PURGE_CONFIRM;
  public String DATABASE_PURGE;
  public String DATABASE_PURGE_ALREADY;
  public String DATABASE_NOT_SELECTED;
  public String DATABASE_RELOADING;
  public String DATABASE_RELOADED;
}

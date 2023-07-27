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

package xyz.jonesdev.sonar.velocity;

import com.velocitypowered.proxy.util.ratelimit.Ratelimiters;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import xyz.jonesdev.sonar.api.Sonar;
import xyz.jonesdev.sonar.api.SonarPlatform;
import xyz.jonesdev.sonar.api.SonarSupplier;
import xyz.jonesdev.sonar.api.command.InvocationSender;
import xyz.jonesdev.sonar.api.config.SonarConfiguration;
import xyz.jonesdev.sonar.api.logger.Logger;
import xyz.jonesdev.sonar.api.server.ServerWrapper;
import xyz.jonesdev.sonar.common.SonarBootstrap;
import xyz.jonesdev.sonar.common.fallback.protocol.FallbackPreparer;
import xyz.jonesdev.sonar.common.timer.DelayTimer;
import xyz.jonesdev.sonar.velocity.command.SonarCommand;
import xyz.jonesdev.sonar.velocity.fallback.FallbackListener;
import xyz.jonesdev.sonar.velocity.verbose.ActionBarVerbose;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public enum SonarVelocity implements Sonar, SonarBootstrap<SonarVelocityPlugin> {

  INSTANCE;

  @Getter
  private SonarVelocityPlugin plugin;

  @Getter
  private ActionBarVerbose actionBarVerbose;

  @Getter
  private SonarConfiguration config;

  @Getter
  private File pluginDataFolder;

  @Getter
  private final Logger logger = new Logger() {

    @Override
    public void info(final String message, final Object... args) {
      plugin.getLogger().info(message, args);
    }

    @Override
    public void warn(final String message, final Object... args) {
      plugin.getLogger().warn(message, args);
    }

    @Override
    public void error(final String message, final Object... args) {
      plugin.getLogger().error(message, args);
    }
  };

  /**
   * Create a wrapper object for our server, so we can use it outside
   * the velocity module.
   * We have to do this, so we can access all necessary API functions.
   *
   * @since 2.0.0 (7faa4b6)
   */
  @Getter
  public final ServerWrapper server = new ServerWrapper() {

    @Override
    public SonarPlatform getPlatform() {
      return SonarPlatform.VELOCITY;
    }

    @Override
    public Optional<InvocationSender> getOnlinePlayer(final String username) {
      return getPlugin().getServer().getAllPlayers().stream()
        .filter(player -> player.getUsername().equalsIgnoreCase(username))
        .findFirst()
        .map(player -> new InvocationSender() {

          @Override
          public String getName() {
            return player.getUsername();
          }

          @Override
          public void sendMessage(final String message) {
            player.sendMessage(Component.text(message));
          }
        });
    }
  };

  @Override
  public void enable(final SonarVelocityPlugin plugin) {
    this.plugin = plugin;

    final DelayTimer timer = new DelayTimer();

    // Set the API to this class
    SonarSupplier.set(this);

    logger.info("Initializing Sonar...");

    pluginDataFolder = plugin.getDataDirectory().toFile();

    // Initialize configuration
    config = new SonarConfiguration(plugin.getDataDirectory().toFile());
    reload();

    // Initialize bStats.org metrics
    plugin.getMetricsFactory().make(plugin, getServiceId());

    // Register Sonar command
    plugin.getServer().getCommandManager().register("sonar", new SonarCommand());

    // Register Fallback listener
    plugin.getServer().getEventManager().register(plugin, new FallbackListener(getFallback()));

    // Register Fallback queue task
    plugin.getServer().getScheduler().buildTask(plugin, getFallback().getQueue()::poll)
      .repeat(500L, TimeUnit.MILLISECONDS)
      .schedule();

    // Initialize action bar verbose
    actionBarVerbose = new ActionBarVerbose(plugin.getServer());

    // Register action bar verbose task
    plugin.getServer().getScheduler().buildTask(plugin, actionBarVerbose::update)
      .repeat(100L, TimeUnit.MILLISECONDS)
      .schedule();

    // Done
    logger.info("Done ({}s)!", timer.formattedDelay());
  }

  @Override
  public void reload() {
    getConfig().load();
    FallbackListener.CachedMessages.update();
    FallbackPreparer.prepare();

    // Apply filter (connection limiter) to Fallback
    getFallback().setAttemptLimiter(Ratelimiters.createWithMilliseconds(config.VERIFICATION_DELAY)::attempt);

    // Run the shared reload process
    SonarBootstrap.super.reload();
  }
}

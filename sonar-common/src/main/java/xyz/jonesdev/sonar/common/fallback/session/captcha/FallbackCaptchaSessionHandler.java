/*
 * Copyright (C) 2023-2024 Sonar Contributors
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

package xyz.jonesdev.sonar.common.fallback.session.captcha;

import io.netty.handler.codec.CorruptedFrameException;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.Sonar;
import xyz.jonesdev.sonar.api.config.SonarConfiguration;
import xyz.jonesdev.sonar.api.fallback.FallbackUser;
import xyz.jonesdev.sonar.common.fallback.protocol.FallbackCaptchaPreparer;
import xyz.jonesdev.sonar.common.fallback.protocol.FallbackPacket;
import xyz.jonesdev.sonar.common.fallback.protocol.packets.play.SetPlayerPositionPacket;
import xyz.jonesdev.sonar.common.fallback.protocol.packets.play.SetPlayerPositionRotation;
import xyz.jonesdev.sonar.common.fallback.session.FallbackSessionHandler;

import java.util.UUID;

import static xyz.jonesdev.sonar.common.fallback.protocol.FallbackPreparer.*;

public abstract class FallbackCaptchaSessionHandler extends FallbackSessionHandler {

  public FallbackCaptchaSessionHandler(final @NotNull FallbackUser user,
                                       final @NotNull String username,
                                       final @NotNull UUID uuid) {
    super(user, username, uuid);

    // Kick the player if we don't have any data loaded yet
    if (!FallbackCaptchaPreparer.isCaptchaAvailable()) {
      user.disconnect(Sonar.get().getConfig().getVerification().getCurrentlyPreparing());
      throw new CorruptedFrameException("Captcha is not available at this time");
    }

    this.triesLeft = Sonar.get().getConfig().getVerification().getMapCaptcha().getMaxTries();
    this.captchaType = Sonar.get().getConfig().getVerification().getMapCaptcha().getType();
  }

  protected final void ready() {
    // Make sure the player cannot move
    user.delayedWrite(user.isGeyser() ? CAPTCHA_ABILITIES_BEDROCK : CAPTCHA_ABILITIES);
    // Make sure the player knows what to do
    user.delayedWrite(enterCodeMessage);
    // Send all packets in one flush
    user.getChannel().flush();
  }

  protected int triesLeft;
  protected final SonarConfiguration.Verification.MapCaptcha.CaptchaType captchaType;
  private int lastCountdownIndex, keepAliveStreak;

  @Override
  public void handle(final @NotNull FallbackPacket packet) {
    // Check if the player took too long to enter the captcha
    final int maxDuration = Sonar.get().getConfig().getVerification().getMapCaptcha().getMaxDuration();
    checkState(!user.getLoginTimer().elapsed(maxDuration), "took too long to enter captcha");

    if (packet instanceof SetPlayerPositionPacket
      || packet instanceof SetPlayerPositionRotation) {
      // A position packet is sent approximately every second
      final long difference = maxDuration - user.getLoginTimer().delay();
      final int index = (int) (difference / 1000D);
      // Make sure we can actually safely get and send the packet
      if (lastCountdownIndex != index && index >= 0 && xpCountdown.length > index) {
        // Send the countdown using the experience bar
        user.write(xpCountdown[index]);
      }
      lastCountdownIndex = index;
      // Send a KeepAlive packet every few seconds
      if (keepAliveStreak++ > 20) {
        keepAliveStreak = 0;
        // Send a KeepAlive packet to prevent timeout
        user.write(CAPTCHA_KEEP_ALIVE);
      }
    }
  }

  public static @NotNull FallbackCaptchaSessionHandler getPreferred(final @NotNull FallbackUser user,
                                                                    final @NotNull String username,
                                                                    final @NotNull UUID uuid) {
    switch (Sonar.get().getConfig().getVerification().getMapCaptcha().getType()) {
      default:
      case HAND: {
        return new FallbackHandCaptchaSessionHandler(user, username, uuid);
      }
      case PUZZLE: {
        return new FallbackPuzzleCaptchaSessionHandler(user, username, uuid);
      }
    }
  }
}

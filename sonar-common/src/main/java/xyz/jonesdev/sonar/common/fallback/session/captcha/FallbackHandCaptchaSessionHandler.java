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

import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.fallback.FallbackUser;
import xyz.jonesdev.sonar.common.fallback.protocol.FallbackCaptchaPreparer;
import xyz.jonesdev.sonar.common.fallback.protocol.FallbackPacket;
import xyz.jonesdev.sonar.common.fallback.protocol.map.MapCaptchaInfo;
import xyz.jonesdev.sonar.common.fallback.protocol.packets.play.MapDataPacket;
import xyz.jonesdev.sonar.common.fallback.protocol.packets.play.SetContainerSlotPacket;
import xyz.jonesdev.sonar.common.fallback.protocol.packets.play.SystemChatPacket;

import java.util.UUID;

import static xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion.MINECRAFT_1_8;
import static xyz.jonesdev.sonar.common.fallback.protocol.FallbackPreparer.*;

/**
 * Flow for this session handler
 *
 * <li>
 *   {@link SetContainerSlotPacket} and {@link MapDataPacket} packets are sent to the client,
 *   therefore, setting the player's item to a map with a code on it (CAPTCHA).
 *   <br>
 *   See more: {@link FallbackHandCaptchaSessionHandler}, {@link MapCaptchaInfo}
 * </li>
 * <li>
 *   Then, we wait for the player to enter the {@link FallbackHandCaptchaSessionHandler#answer} in chat.
 * </li>
 */
public final class FallbackHandCaptchaSessionHandler extends FallbackCaptchaSessionHandler {

  public FallbackHandCaptchaSessionHandler(final FallbackUser user, final String username, final UUID uuid) {
    super(user, username, uuid);

    // Teleport the player to the position above the platform
    user.delayedWrite(CAPTCHA_POSITION);
    // If the player is on Java, set the 5th slot (ID 4) in the player's hotbar to the map.
    // If the player is on Bedrock, set the 1st slot (ID 0) in the player's hotbar to the map.
    user.delayedWrite(user.isGeyser() ? CAPTCHA_SET_SLOT_BEDROCK : CAPTCHA_SET_SLOT);
    // Send random captcha to the player
    final MapCaptchaInfo captcha = FallbackCaptchaPreparer.getRandomCaptcha();
    if (user.getProtocolVersion().compareTo(MINECRAFT_1_8) < 0) {
      // 1.7.2-1.7.10 needs separate packets for each axis
      for (final FallbackPacket legacyPacket : captcha.getLegacyPackets()) {
        user.delayedWrite(legacyPacket);
      }
    } else {
      // Send modern packet for 1.8+ clients
      user.delayedWrite(captcha.getModernPacket());
    }
    this.answer = captcha.getAnswer();
    // Send position and flush all packets
    ready();
  }

  private final String answer;

  @Override
  public void handle(final @NotNull FallbackPacket packet) {
    // Handle incoming chat messages
    if (packet instanceof SystemChatPacket) {
      final SystemChatPacket chat = (SystemChatPacket) packet;

      // Captcha is correct, finish verification
      if (chat.getMessage().equals(answer)) {
        finishVerification();
        return;
      }

      // Captcha is incorrect, remove one try
      checkState(triesLeft-- > 0, "failed captcha too often");
      user.write(incorrectCaptcha);
      return;
    }

    // Make sure the actual session handler receives the packet
    super.handle(packet);
  }
}

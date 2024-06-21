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
import xyz.jonesdev.sonar.common.fallback.protocol.FallbackPacket;
import xyz.jonesdev.sonar.common.fallback.protocol.packets.play.SetEntityMetadataPacket;

import java.util.UUID;

import static xyz.jonesdev.sonar.common.fallback.protocol.FallbackPreparer.*;

public final class FallbackPuzzleCaptchaSessionHandler extends FallbackCaptchaSessionHandler {

  public FallbackPuzzleCaptchaSessionHandler(final FallbackUser user, final String username, final UUID uuid) {
    super(user, username, uuid);

    // Teleport the player to the position above the platform
    user.delayedWrite(FRAMED_CAPTCHA_POSITION);

    // TODO: finish this
    for (int i = 0; i < CAPTCHA_ITEM_FRAMES.length; i++) {
      user.delayedWrite(CAPTCHA_ITEM_FRAMES[i]);
      user.delayedWrite(new SetEntityMetadataPacket(VEHICLE_ENTITY_ID + i + 1,
        SetEntityMetadataPacket.createMetadata(user.getProtocolVersion(), i)));
    }
    // Send position and flush all packets
    ready();
  }

  @Override
  public void handle(final @NotNull FallbackPacket packet) {
    // Make sure the actual session handler receives the packet
    super.handle(packet);
  }
}

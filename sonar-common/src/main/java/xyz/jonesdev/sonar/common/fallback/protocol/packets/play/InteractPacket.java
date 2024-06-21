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

package xyz.jonesdev.sonar.common.fallback.protocol.packets.play;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion;
import xyz.jonesdev.sonar.common.fallback.protocol.FallbackPacket;

import static xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion.*;
import static xyz.jonesdev.sonar.common.fallback.protocol.packets.play.InteractPacket.InteractionType.INTERACT;
import static xyz.jonesdev.sonar.common.fallback.protocol.packets.play.InteractPacket.InteractionType.INTERACT_AT;
import static xyz.jonesdev.sonar.common.util.ProtocolUtil.readVarInt;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public final class InteractPacket implements FallbackPacket {
  private int entityId;
  private InteractionType type;
  private float targetX, targetY, targetZ;
  private int hand;
  private boolean sneaking;

  public enum InteractionType {
    INTERACT,
    ATTACK,
    INTERACT_AT
  }

  @Override
  public void encode(final ByteBuf byteBuf, final @NotNull ProtocolVersion protocolVersion) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void decode(final ByteBuf byteBuf, final ProtocolVersion protocolVersion) throws Exception {
    if (protocolVersion.compareTo(MINECRAFT_1_7_6) <= 0) {
      entityId = byteBuf.readInt();
      type = InteractionType.values()[byteBuf.readByte()];
      return;
    }

    entityId = readVarInt(byteBuf);
    type = InteractionType.values()[readVarInt(byteBuf)];

    if (type == INTERACT_AT) {
      targetX = byteBuf.readFloat();
      targetY = byteBuf.readFloat();
      targetZ = byteBuf.readFloat();
    }

    if (protocolVersion.compareTo(MINECRAFT_1_8) > 0) {
      if (type == INTERACT || type == INTERACT_AT) {
        hand = readVarInt(byteBuf);
      }

      if (protocolVersion.compareTo(MINECRAFT_1_15_2) > 0) {
        sneaking = byteBuf.readBoolean();
      }
    }
  }
}

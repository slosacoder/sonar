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
import xyz.jonesdev.sonar.common.fallback.protocol.entity.EntityType;

import java.util.UUID;

import static xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion.*;
import static xyz.jonesdev.sonar.common.util.ProtocolUtil.writeUUID;
import static xyz.jonesdev.sonar.common.util.ProtocolUtil.writeVarInt;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public final class SpawnEntityPacket implements FallbackPacket {
  private int entityId, data;
  private EntityType entityType;
  private double x, y, z;
  private float yaw, headYaw, pitch;
  private float velocityX, velocityY, velocityZ;

  @Override
  public void encode(final ByteBuf byteBuf, final @NotNull ProtocolVersion protocolVersion) throws Exception {
    writeVarInt(byteBuf, entityId);

    final boolean v1_9orHigher = protocolVersion.compareTo(MINECRAFT_1_8) > 0;

    if (protocolVersion.compareTo(MINECRAFT_1_8) > 0) {
      writeUUID(byteBuf, UUID.randomUUID());

      if (protocolVersion.compareTo(MINECRAFT_1_14) >= 0) {
        writeVarInt(byteBuf, entityType.getId(protocolVersion));
      } else {
        byteBuf.writeByte(entityType.getId(protocolVersion));
      }

      byteBuf.writeDouble(x);
      byteBuf.writeDouble(y);
      byteBuf.writeDouble(z);
    } else {
      byteBuf.writeByte(entityType.getId(protocolVersion));
      byteBuf.writeInt(floor(x * 32D));
      byteBuf.writeInt(floor(y * 32D));
      byteBuf.writeInt(floor(z * 32D));
    }

    byteBuf.writeByte((int) (pitch * (256f / 360f)));
    byteBuf.writeByte((int) (yaw * (256f / 360f)));

    if (protocolVersion.compareTo(MINECRAFT_1_18_2) > 0) {
      byteBuf.writeByte((int) (headYaw * (256f / 360f)));
      writeVarInt(byteBuf, data);
    } else {
      byteBuf.writeInt(data);
    }

    byteBuf.writeShort((int) (velocityX * 8000f));
    byteBuf.writeShort((int) (velocityY * 8000f));
    byteBuf.writeShort((int) (velocityZ * 8000f));
  }

  private static int floor(final double value) {
    final int __value = (int) value;
    return value < (double) __value ? __value - 1 : __value;
  }

  @Override
  public void decode(final ByteBuf byteBuf, final @NotNull ProtocolVersion protocolVersion) {
    throw new UnsupportedOperationException();
  }
}

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
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion;
import xyz.jonesdev.sonar.common.fallback.protocol.FallbackPacket;
import xyz.jonesdev.sonar.common.fallback.protocol.captcha.ItemType;
import xyz.jonesdev.sonar.common.fallback.protocol.metadata.MetadataByteEntry;
import xyz.jonesdev.sonar.common.fallback.protocol.metadata.MetadataEntry;
import xyz.jonesdev.sonar.common.fallback.protocol.metadata.MetadataSlotEntry;
import xyz.jonesdev.sonar.common.fallback.protocol.metadata.MetadataVarIntEntry;

import java.util.Map;

import static xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion.*;
import static xyz.jonesdev.sonar.common.util.ProtocolUtil.writeVarInt;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public final class SetEntityMetadataPacket implements FallbackPacket {
  private int entityId;
  private Map<Byte, MetadataEntry> metadata;

  @Override
  public void encode(final ByteBuf byteBuf, final @NotNull ProtocolVersion protocolVersion) throws Exception {
    if (protocolVersion.compareTo(MINECRAFT_1_7_6) <= 0) {
      byteBuf.writeInt(entityId);
    } else {
      writeVarInt(byteBuf, entityId);
    }

    // https://wiki.vg/Entity_metadata#Entity_Metadata_Format
    // https://github.com/Elytrium/LimboFilter/blob/master/src/main/java/net/elytrium/limbofilter/protocol/data/EntityMetadata.java
    if (protocolVersion.compareTo(MINECRAFT_1_8) <= 0) {
      metadata.forEach((index, value) -> {
        byteBuf.writeByte((index & 0x1F) | (value.getTypeId(protocolVersion) << 5));
        value.encode(byteBuf, protocolVersion);
      });
      byteBuf.writeByte(0x7F);
    } else {
      metadata.forEach((index, value) -> {
        byteBuf.writeByte(index);
        writeVarInt(byteBuf, value.getTypeId(protocolVersion));
        value.encode(byteBuf, protocolVersion);
      });
      byteBuf.writeByte(0xFF);
    }
  }

  public static byte getMetadataIndex(@NotNull ProtocolVersion protocolVersion) {
    if (protocolVersion.compareTo(MINECRAFT_1_7_6) <= 0) {
      return 2;
    } else if (protocolVersion.compareTo(MINECRAFT_1_8) <= 0) {
      return 8;
    } else if (protocolVersion.compareTo(MINECRAFT_1_9_4) <= 0) {
      return 5;
    } else if (protocolVersion.compareTo(MINECRAFT_1_13_2) <= 0) {
      return 6;
    } else if (protocolVersion.compareTo(MINECRAFT_1_16_4) <= 0) {
      return 7;
    } else {
      return 8;
    }
  }

  public static @NotNull @Unmodifiable Map<Byte, MetadataEntry> createMetadata(final @NotNull ProtocolVersion protocolVersion, final int mapId) {
    if (protocolVersion.compareTo(MINECRAFT_1_12_2) <= 0) {
      return Map.of(getMetadataIndex(protocolVersion),
        new MetadataSlotEntry(1, mapId, ItemType.FILLED_MAP, null));
    }
    return Map.of(getMetadataIndex(protocolVersion),
      new MetadataSlotEntry(1, 0, ItemType.FILLED_MAP,
        CompoundBinaryTag.builder().put("map", IntBinaryTag.intBinaryTag(mapId)).build()));
  }

  public static @NotNull @Unmodifiable Map<Byte, MetadataEntry> createRotationMetadata(final @NotNull ProtocolVersion protocolVersion, final int rotation) {
    if (protocolVersion.compareTo(MINECRAFT_1_8) <= 0) {
      return Map.of((byte) (getMetadataIndex(protocolVersion) + 1), new MetadataByteEntry(rotation % 4));
    }
    return Map.of((byte) (getMetadataIndex(protocolVersion) + 1), new MetadataVarIntEntry(rotation));
  }

  @Override
  public void decode(final ByteBuf byteBuf, final ProtocolVersion protocolVersion) throws Exception {
    throw new UnsupportedOperationException();
  }
}

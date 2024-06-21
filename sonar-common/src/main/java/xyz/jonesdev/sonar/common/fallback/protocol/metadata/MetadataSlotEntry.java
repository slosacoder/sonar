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

package xyz.jonesdev.sonar.common.fallback.protocol.metadata;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion;
import xyz.jonesdev.sonar.common.fallback.protocol.item.ItemType;

import static xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion.*;
import static xyz.jonesdev.sonar.common.util.ProtocolUtil.*;

@Getter
@RequiredArgsConstructor
public final class MetadataSlotEntry implements MetadataEntry {
  private final int count, data;
  private final ItemType itemType;
  private final CompoundBinaryTag compoundBinaryTag;

  @Override
  public void encode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion protocolVersion) {
    if (protocolVersion.compareTo(MINECRAFT_1_20_5) >= 0) {
      writeVarInt(byteBuf, count);
      writeVarInt(byteBuf, itemType.getId(protocolVersion));
      // TODO: improve this
      writeVarInt(byteBuf, 1); // add
      writeVarInt(byteBuf, 0); // remove
      writeVarInt(byteBuf, 26); // map component
      writeVarInt(byteBuf, 0); // map id
    } else {
      if (protocolVersion.compareTo(MINECRAFT_1_13_2) >= 0) {
        byteBuf.writeBoolean(true); // present
      }

      if (protocolVersion.compareTo(MINECRAFT_1_13_2) < 0) {
        byteBuf.writeShort(itemType.getId(protocolVersion));
      } else {
        writeVarInt(byteBuf, itemType.getId(protocolVersion));
      }

      byteBuf.writeByte(count);

      if (protocolVersion.compareTo(MINECRAFT_1_13) < 0) {
        byteBuf.writeShort(data);
      }

      if (compoundBinaryTag == null) {
        if (protocolVersion.compareTo(MINECRAFT_1_8) < 0) {
          byteBuf.writeShort(-1);
        } else {
          byteBuf.writeByte(0);
        }
      } else if (protocolVersion.compareTo(MINECRAFT_1_20_2) < 0) {
        writeCompoundTag(byteBuf, compoundBinaryTag);
      } else {
        writeNamelessCompoundTag(byteBuf, compoundBinaryTag);
      }
    }
  }

  @Override
  public int getTypeId(final @NotNull ProtocolVersion protocolVersion) {
    if (protocolVersion.compareTo(MINECRAFT_1_19_1) > 0) {
      return 7;
    }
    if (protocolVersion.compareTo(MINECRAFT_1_12_2) > 0) {
      return 6;
    }
    return 5;
  }
}

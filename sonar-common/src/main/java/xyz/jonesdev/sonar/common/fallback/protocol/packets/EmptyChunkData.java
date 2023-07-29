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

package xyz.jonesdev.sonar.common.fallback.protocol.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.jonesdev.sonar.api.fallback.protocol.FallbackPacket;
import xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion;

import java.io.IOException;
import java.util.BitSet;

import static xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion.*;
import static xyz.jonesdev.sonar.common.protocol.ProtocolUtil.writeArray;
import static xyz.jonesdev.sonar.common.protocol.VarIntUtil.writeVarInt;

// Taken from
// https://github.com/Leymooo/BungeeCord/blob/master/protocol/src/main/java/ru/leymooo/botfilter/packets/EmptyChunkPacket.java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class EmptyChunkData implements FallbackPacket {
  private int x, z;
  private static final byte[] SECTION_BYTES = new byte[]{0, 0, 0, 0, 0, 0, 1, 0};
  private static final byte[] LIGHT_BYTES = new byte[]{1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0};

  @Override
  public void encode(final ByteBuf byteBuf, final ProtocolVersion protocolVersion) {
    byteBuf.writeInt(x);
    byteBuf.writeInt(z);

    if (protocolVersion.compareTo(MINECRAFT_1_17) < 0) {
      byteBuf.writeBoolean(true);
    }

    if (protocolVersion.compareTo(MINECRAFT_1_16) >= 0 && protocolVersion.compareTo(MINECRAFT_1_16_2) < 0) {
      byteBuf.writeBoolean(true);
    }

    if (protocolVersion.compareTo(MINECRAFT_1_17) < 0) {
      if (protocolVersion.compareTo(MINECRAFT_1_8) == 0) {
        byteBuf.writeShort(1);
      } else {
        writeVarInt(byteBuf, 0);
      }
    } else if (protocolVersion.compareTo(MINECRAFT_1_18) < 0) {
      final BitSet bitSet = new BitSet();

      for (int i = 0; i < 16; i++) {
        bitSet.set(i, false);
      }

      long[] mask = bitSet.toLongArray();
      writeVarInt(byteBuf, mask.length);

      for (long l : mask) {
        byteBuf.writeLong(l);
      }
    }

    if (protocolVersion.compareTo(MINECRAFT_1_14) >= 0) {
      try (final ByteBufOutputStream output = new ByteBufOutputStream(byteBuf)) {
        output.writeByte(10); // CompoundTag
        output.writeUTF(""); // CompoundName
        output.writeByte(10); // CompoundTag
        output.writeUTF("root"); // root compound
        output.writeByte(12); // long array
        output.writeUTF("MOTION_BLOCKING");
        final long[] tag = new long[protocolVersion.compareTo(MINECRAFT_1_18) < 0 ? 36 : 37];
        output.writeInt(tag.length);
        for (long l : tag) {
          output.writeLong(l);
        }
        byteBuf.writeByte(0); // end
        byteBuf.writeByte(0); // end
      } catch (IOException exception) {
        throw new RuntimeException(exception);
      }

      if (protocolVersion.compareTo(MINECRAFT_1_15) >= 0 && protocolVersion.compareTo(MINECRAFT_1_18) < 0) {
        if (protocolVersion.compareTo(MINECRAFT_1_16_2) >= 0) {
          writeVarInt(byteBuf, 1024);

          for (int i = 0; i < 1024; i++) {
            writeVarInt(byteBuf, 1);
          }
        } else {
          for (int i = 0; i < 1024; i++) {
            byteBuf.writeInt(0);
          }
        }
      }
    }

    if (protocolVersion.compareTo(MINECRAFT_1_13) < 0) {
      writeArray(byteBuf, new byte[256]); // 1.8 - 1.12.2
    } else if (protocolVersion.compareTo(MINECRAFT_1_15) < 0) {
      writeArray(byteBuf, new byte[1024]); // 1.13 - 1.14.4
    } else if (protocolVersion.compareTo(MINECRAFT_1_18) < 0) {
      writeVarInt(byteBuf, 0); // 1.15 - 1.17.1
    } else {
      writeVarInt(byteBuf, SECTION_BYTES.length * 16);

      for (int i = 0; i < 16; i++) {
        byteBuf.writeBytes(SECTION_BYTES);
      }
    }

    if (protocolVersion.compareTo(MINECRAFT_1_9_4) >= 0) {
      writeVarInt(byteBuf, 0);
    }

    if (protocolVersion.compareTo(MINECRAFT_1_18) >= 0) {
      byteBuf.ensureWritable(LIGHT_BYTES.length);

      if (protocolVersion.compareTo(MINECRAFT_1_20) >= 0) {
        byteBuf.writeBytes(LIGHT_BYTES, 1, LIGHT_BYTES.length - 1);
      } else {
        byteBuf.writeBytes(LIGHT_BYTES);
      }
    }
  }

  @Override
  public void decode(final ByteBuf byteBuf, final ProtocolVersion protocolVersion) {
    throw new UnsupportedOperationException();
  }
}

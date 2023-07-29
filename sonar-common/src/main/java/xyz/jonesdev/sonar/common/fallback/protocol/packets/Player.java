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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import xyz.jonesdev.sonar.api.fallback.protocol.FallbackPacket;
import xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public final class Player implements FallbackPacket {
  private boolean onGround;

  @Override
  public void decode(final ByteBuf byteBuf, final ProtocolVersion protocolVersion) {
    onGround = byteBuf.readBoolean();
  }

  @Override
  public void encode(final ByteBuf byteBuf, final ProtocolVersion protocolVersion) {
    throw new UnsupportedOperationException();
  }
}

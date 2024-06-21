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

package xyz.jonesdev.sonar.common.fallback.protocol.map;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.common.fallback.protocol.FallbackPacket;
import xyz.jonesdev.sonar.common.fallback.protocol.packets.play.MapDataPacket;

@Getter
public final class MapCaptchaInfo {
  private final String answer;

  private final FallbackPacket[] legacyPackets;
  private final FallbackPacket modernPacket;

  public MapCaptchaInfo(final int rows,
                        final int columns,
                        final @NotNull String answer,
                        final int @NotNull [] buffer) {
    this.answer = answer;

    // Prepare 1.7 map data using a grid
    final int[][] grid = new int[128][128];
    for (int i = 0; i < buffer.length; i++) {
      final int buf = buffer[i];
      grid[i & Byte.MAX_VALUE][i >> 7] = buf;
    }
    this.legacyPackets = new FallbackPacket[grid.length];
    for (int i = 0; i < grid.length; i++) {
      this.legacyPackets[i] = new MapDataPacket(0, i, 0, 0, rows, columns, grid[i]);
    }

    // Prepare 1.8+ map data
    this.modernPacket = new MapDataPacket(0, 0, 0, 0, rows, columns, buffer);
  }
}

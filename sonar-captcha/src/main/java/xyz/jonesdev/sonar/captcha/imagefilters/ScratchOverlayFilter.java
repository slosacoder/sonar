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

package xyz.jonesdev.sonar.captcha.imagefilters;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

@Getter
@RequiredArgsConstructor
public final class ScratchOverlayFilter extends ImageFilter {
  private final int amount;
  private final float lineWidth;
  private final Paint paint;

  @Override
  public void transform(final @NotNull BufferedImage bufferedImage) {
    final Graphics2D graphics = bufferedImage.createGraphics();
    final float halfWidth = bufferedImage.getWidth() * 0.5f;

    graphics.setStroke(new BasicStroke(lineWidth));
    graphics.setPaint(paint);

    for (int i = 0; i < amount; ++i) {
      final float randomX = bufferedImage.getWidth() * RANDOM.nextFloat();
      final float randomY = bufferedImage.getHeight() * RANDOM.nextFloat();
      final float amplitude = 6.2831855f * (RANDOM.nextFloat() - 0.5f);
      final float sin = (float) Math.sin(amplitude) * halfWidth;
      final float cos = (float) Math.cos(amplitude) * halfWidth;
      final float x1 = randomX - cos;
      final float y1 = randomY - sin;
      final float x2 = randomX + cos;
      final float y2 = randomY + sin;
      graphics.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    graphics.dispose();
  }
}

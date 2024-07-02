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
public final class RippleFilter extends ImageFilter {
  private final float xAmplitude, yAmplitude;

  @Override
  public void transform(final @NotNull BufferedImage bufferedImage) {
    final int width = bufferedImage.getWidth();
    final int height = bufferedImage.getHeight();

    final BufferedImage originalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    originalImage.getGraphics().drawImage(bufferedImage, 0, 0, null);

    final Graphics2D graphics = bufferedImage.createGraphics();

    // Iterate through each pixel of the image
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        // Calculate displacement based on sine wave
        final float nx = x / 3f;
        final float ny = y / 3f;
        final float xAmplitude = this.xAmplitude + RANDOM.nextFloat() * 0.2f;
        final float yAmplitude = this.yAmplitude + RANDOM.nextFloat() * 0.2f;
        final float dx = (float) Math.sin(ny / yAmplitude) * xAmplitude;
        final float dy = (float) Math.sin(nx / xAmplitude) * yAmplitude;

        // Apply displacement to coordinates
        final int newX = x + (int) dx;
        final int newY = y + (int) dy;

        if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
          // Get the pixel color from the original image
          final int pixel = originalImage.getRGB(newX, newY);
          // Set the pixel color in the filtered image
          bufferedImage.setRGB(x, y, pixel);
        }
      }
    }

    graphics.dispose();
  }
}

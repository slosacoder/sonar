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
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public final class CircleInverseFilter extends ImageFilter {
  private final int amount, minRadius, maxRadiusExpansion;

  private static final Color MAX_COLOR = new Color(90, 90, 90);

  @Override
  public void transform(final @NotNull BufferedImage bufferedImage) {
    final int minX = bufferedImage.getWidth() / 15;
    final int minY = bufferedImage.getHeight() / 15;

    final List<Circle> circles = new ArrayList<>();

    for (int i = 0; i < amount; i++) {
      float centerX, centerY;
      int radius;

      do {
        // Generate random position and radius for the circle
        centerX = minX + RANDOM.nextInt(bufferedImage.getWidth() - minX * 2);
        centerY = minY + RANDOM.nextInt(bufferedImage.getHeight() - minY * 2);
        radius = minRadius + RANDOM.nextInt(maxRadiusExpansion);
      } while (isOverlapping(circles, centerX, centerY, radius));

      final Circle circle = new Circle(centerX, centerY, radius);
      // Draw inverted circle
      _addInvertedCircle(bufferedImage, circle);
      // Add the circle to the list
      circles.add(circle);
    }
  }

  private static void _addInvertedCircle(final @NotNull BufferedImage bufferedImage,
                                         final @NotNull Circle circle) {
    for (int x = 0; x < bufferedImage.getWidth(); x++) {
      for (int y = 0; y < bufferedImage.getHeight(); y++) {
        if (isWithinCircle(x, y, circle, circle.radius)) {
          // Invert colors within the circle
          final int rgb = bufferedImage.getRGB(x, y);
          final int randomDivisor = 1 + RANDOM.nextInt(3);
          final int invertedRGB = invertColorAndFilter(rgb, randomDivisor);
          bufferedImage.setRGB(x, y, invertedRGB);
        }
      }
    }
  }

  private static boolean isOverlapping(final @NotNull List<Circle> circles,
                                       final float centerX, final float centerY,
                                       final int radius) {
    for (final Circle circle : circles) {
      if (isWithinCircle(centerX, centerY, circle, circle.radius + radius)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isWithinCircle(final float x, final float y,
                                        final @NotNull Circle circle,
                                        final int maxRadius) {
    final float distanceX = x - circle.centerX;
    final float distanceY = y - circle.centerY;
    return distanceX * distanceX + distanceY * distanceY <= maxRadius * maxRadius;
  }

  @Getter
  @RequiredArgsConstructor
  static class Circle {
    private final float centerX;
    private final float centerY;
    private final int radius;
  }

  private static int invertColorAndFilter(final int rgb, final int divisor) {
    final Color color = new Color(rgb);
    final int red = Math.max(MAX_COLOR.getRed() - color.getRed() / divisor, 0);
    final int green = Math.max(MAX_COLOR.getGreen() - color.getGreen() / divisor, 0);
    final int blue = Math.max(MAX_COLOR.getBlue() - color.getBlue() / divisor, 0);
    return new Color(red, green, blue).getRGB();
  }
}

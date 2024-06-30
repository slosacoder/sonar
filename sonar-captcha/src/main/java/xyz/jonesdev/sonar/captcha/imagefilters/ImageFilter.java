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
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Getter
public abstract class ImageFilter {
  protected static final Random RANDOM = new Random();

  public abstract void transform(final @NotNull BufferedImage bufferedImage);

  protected static int invertColor(final int rgb) {
    final Color color = new Color(rgb);
    final int red = 255 - color.getRed();
    final int green = 255 - color.getGreen();
    final int blue = 255 - color.getBlue();
    return new Color(red, green, blue).getRGB();
  }
}

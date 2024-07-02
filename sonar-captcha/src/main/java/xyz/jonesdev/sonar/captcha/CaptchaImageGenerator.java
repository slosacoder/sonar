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

package xyz.jonesdev.sonar.captcha;

import com.jhlabs.image.FBMFilter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Objects;
import java.util.Random;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

@Getter
@RequiredArgsConstructor
public abstract class CaptchaImageGenerator {
  protected final int width, height;
  private final @Nullable File backgroundImage;
  protected BufferedImage background;

  protected static final Random RANDOM = new Random();

  protected final void createBackgroundImage() {
    if (background == null) {
      // Try loading the background image from the given file
      try {
        background = ImageIO.read(Objects.requireNonNull(backgroundImage));
        // Clip the image if the dimensions mismatch
        if (background.getWidth() > width || background.getHeight() > height) {
          background = background.getSubimage(0, 0, width, height);
        }
      } catch (Exception exception) {
        background = new BufferedImage(width, height, TYPE_INT_RGB);
        // Add random background noise to the image
        final FBMFilter fbmFilter = new FBMFilter();
        fbmFilter.setAmount(0.7f);
        fbmFilter.setH(1.15f);
        background = fbmFilter.filter(background, null);
      }
    }
  }

  protected final Font loadFontFromFile(final @NotNull String path) {
    try (final InputStream inputStream = getClass().getResourceAsStream(path)) {
      if (inputStream == null) {
        throw new IllegalArgumentException("Could not find font file: " + path);
      }
      final Font customFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
      return customFont.deriveFont(Font.PLAIN, width / 2.5f);
    } catch (Exception exception) {
      throw new IllegalStateException(exception);
    }
  }

  protected final @NotNull BufferedImage mergeImages(final @NotNull BufferedImage background,
                                                     final @NotNull BufferedImage foreground) {
    // Get the background image and create a new foreground image
    final BufferedImage finalImage = new BufferedImage(width, height, TYPE_INT_RGB);
    // Create a new image with transparency for the merged result
    final Graphics2D graphics = finalImage.createGraphics();
    // Draw the foreground image on top of the background at specified coordinates
    graphics.drawImage(background, 0, 0, null);
    // Set AlphaComposite to handle transparency for the foreground image
    graphics.setComposite(AlphaComposite.SrcOver);
    // Draw the foreground image on top of the background at specified coordinates
    graphics.drawImage(foreground, 0, 0, null);
    graphics.dispose();
    return finalImage;
  }
}

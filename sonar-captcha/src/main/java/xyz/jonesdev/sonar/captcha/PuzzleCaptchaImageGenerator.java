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

import com.jhlabs.image.RippleFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;

public final class PuzzleCaptchaImageGenerator extends CaptchaImageGenerator {

  public PuzzleCaptchaImageGenerator(final int width, final int height, final @Nullable File backgroundImage) {
    super(width, height, backgroundImage);
  }

  public @NotNull PuzzleCaptchaImageHolder createImage() {
    // Make sure we have a background image cached/generated
    createBackgroundImage();
    // Create a new foreground image for the puzzle piece
    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = bufferedImage.createGraphics();
    graphics.setColor(Color.BLUE); // TODO
    final int puzzleDiameter = 35 + RANDOM.nextInt(15);
    final int puzzleX = puzzleDiameter + RANDOM.nextInt(width - puzzleDiameter * 3);
    final int puzzleY = puzzleDiameter + RANDOM.nextInt(height - puzzleDiameter * 3);
    drawPuzzlePiece(graphics, puzzleX, puzzleY, puzzleDiameter);
    graphics.dispose();
    // Apply ripple filter on foreground image
    bufferedImage = new RippleFilter().filter(bufferedImage, null);
    // Merge the background and foreground image into one
    bufferedImage = mergeImages(background, bufferedImage);
    return new PuzzleCaptchaImageHolder(bufferedImage, puzzleX, puzzleY, puzzleDiameter);
  }

  private void drawPuzzlePiece(final @NotNull Graphics2D graphics,
                               final int puzzleX,
                               final int puzzleY,
                               final int puzzleDiameter) {
    graphics.fill(new Ellipse2D.Double(puzzleX, puzzleY, puzzleDiameter, puzzleDiameter));
  }
}

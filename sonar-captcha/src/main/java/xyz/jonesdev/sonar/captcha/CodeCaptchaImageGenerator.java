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

import com.jhlabs.image.AbstractBufferedImageOp;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

@Getter
@Setter
public final class CodeCaptchaImageGenerator extends CaptchaImageGenerator {
  private GradientPaint gradient;

  public CodeCaptchaImageGenerator(final int width, final int height, final @Nullable File backgroundImage) {
    super(width, height, backgroundImage);
  }

  public @NotNull BufferedImage createImage(final char[] answer,
                                            final @NotNull List<AbstractBufferedImageOp> filters) {
    // Make sure we have a background image cached/generated
    createBackgroundImage();
    // Create a new foreground image for the text
    BufferedImage foreground = new BufferedImage(width, height, TYPE_INT_ARGB);
    final Graphics2D graphics = foreground.createGraphics();

    final FontRenderContext ctx = graphics.getFontRenderContext();
    // Change some rendering hints for anti aliasing
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Draw characters
    drawCharacters(graphics, ctx, answer);
    // Make sure to dispose the graphics after using it
    graphics.dispose();

    // Apply any given filter to the foreground
    for (final AbstractBufferedImageOp bufferedImageOp : filters) {
      foreground = bufferedImageOp.filter(foreground, null);
    }

    return mergeImages(background, foreground);
  }

  private void drawCharacters(final @NotNull Graphics2D graphics,
                              final @NotNull FontRenderContext ctx,
                              final char[] answer) {
    // Create font render context
    final int fontSize = 58 + RANDOM.nextInt(5); // 58 to 62
    final Font font = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);

    // Calculate string width
    final double stringWidth = font.getStringBounds(answer, 0, answer.length, ctx).getWidth();
    final double averageCharacterWidth = stringWidth / answer.length / 1.5;
    // Calculate character positions
    int beginX = (int) ((width - stringWidth) / 2 + averageCharacterWidth);
    final int beginY = (height + (fontSize / 2)) / 2;
    double rotation = 0;

    if (gradient != null) {
      graphics.setPaint(gradient);
    }

    // Draw each character one by one
    for (final char character : answer) {
      if (gradient == null) {
        // Use the inverse color by checking the background image
        // Then, create a gradient for the text by using the colors
        final int x0 = Math.min(Math.max(beginX + 5 /* small threshold */, 0), width);
        final int x1 = Math.min(Math.max(beginX + (int) averageCharacterWidth, 0), width);
        final int y = beginY + fontSize / 2;
        final Color color0 = new Color(~background.getRGB(x0, y));
        final Color color1 = new Color(~background.getRGB(x1, y));
        final GradientPaint gradient = new GradientPaint(0, 0, color0, width, height, color1);
        graphics.setPaint(gradient);
      }

      // Create a glyph vector for the character
      final GlyphVector glyphVector = font.createGlyphVector(ctx, String.valueOf(character));
      // Apply a transformation to the glyph vector using AffineTransform
      final AffineTransform transformation = AffineTransform.getTranslateInstance(beginX, beginY);

      // Add a bit of randomization to the rotation
      rotation += Math.toRadians(6 - RANDOM.nextInt(12));
      transformation.rotate(rotation);

      // Draw the glyph to the buffered image
      final Shape transformedShape = transformation.createTransformedShape(glyphVector.getOutline());
      graphics.fill(transformedShape);
      // Add text outline/shadow to confuse an AI's edge detection
      addTextOutline(graphics, transformedShape);
      // Update next X position
      beginX += (int) averageCharacterWidth;
    }
  }

  private static void addTextOutline(final @NotNull Graphics2D graphics,
                                     final @NotNull Shape transformedShape) {
    // Create a stroked copy of the text and slightly offset/distort it
    final Shape strokedShape = new BasicStroke().createStrokedShape(transformedShape);

    final double tx = 0.5 + RANDOM.nextDouble();
    final double ty = 0.5 + RANDOM.nextDouble();

    // Draw the stroked shape
    final AffineTransform transform = AffineTransform.getTranslateInstance(tx, ty);
    graphics.fill(transform.createTransformedShape(strokedShape));
  }
}

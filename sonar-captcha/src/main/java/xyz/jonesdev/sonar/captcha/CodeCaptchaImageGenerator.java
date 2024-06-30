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

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.jonesdev.sonar.captcha.imagefilters.CircleInverseFilter;
import xyz.jonesdev.sonar.captcha.imagefilters.RippleFilter;
import xyz.jonesdev.sonar.captcha.imagefilters.ScratchOverlayFilter;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

@Getter
@Setter
public final class CodeCaptchaImageGenerator extends CaptchaImageGenerator {
  private static final Random RANDOM = new Random();

  public CodeCaptchaImageGenerator(final int width, final int height, final @Nullable File backgroundImage) {
    super(width, height, backgroundImage);

    this.font = loadFontFromFile("/assets/fonts/Delius-Regular.ttf");
    final Color[] colors = new Color[] {Color.RED, Color.BLUE, Color.CYAN, Color.ORANGE, Color.GREEN, Color.MAGENTA};
    this.possibleGradients = new GradientPaint[colors.length * colors.length];
    for (int i = 0; i < possibleGradients.length; i++) {
      final Color color0 = colors[i % colors.length];
      final Color color1 = colors[(i + 1) % colors.length];
      possibleGradients[i] = new GradientPaint(0, 0, color0, width, height, color1);
    }
  }

  private final GradientPaint[] possibleGradients;

  private final float scaleModifier = height / (128f / 2.5f);
  private final Font font;

  public @NotNull BufferedImage createImage(final char[] answer) {
    createBackgroundImage();
    // Create a new foreground image for the text
    BufferedImage foregroundImage = new BufferedImage(width, height, TYPE_INT_ARGB);
    final Graphics2D graphics = foregroundImage.createGraphics();
    // Change some rendering hits to optimize the image generation
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Draw the text characters onto the foreground image
    drawCharacters(graphics, answer);

    // Make sure to dispose the graphics after using it
    graphics.dispose();

    // Apply a scratch filter for adding random lines on the image
    new ScratchOverlayFilter(4, 1).transform(foregroundImage);
    new RippleFilter(2, 2).transform(foregroundImage);

    // Create a noisy background image
    BufferedImage mergedImage = mergeImages(background, foregroundImage);

    // Draw random inverse-color circles on the merged image
    final int circleAmount = 2 + RANDOM.nextInt(2);
    final int minCircleRadius = (int) Math.floor(10 * scaleModifier);
    new CircleInverseFilter(circleAmount, minCircleRadius, minCircleRadius).transform(mergedImage);

    return mergedImage;
  }

  private void drawCharacters(final @NotNull Graphics2D graphics, final char[] answer) {
    final FontRenderContext ctx = graphics.getFontRenderContext();

    // Get the start X and Y positions
    final GlyphVector textGlyphVector = font.createGlyphVector(ctx, answer);
    final Rectangle textGlyphBounds = textGlyphVector.getOutline().getBounds();
    int glyphX = (width - textGlyphBounds.width) / 2 + textGlyphBounds.width / answer.length / 3;
    int glyphY = (height - textGlyphBounds.height) / 2 + textGlyphBounds.height;

    // Apply a gradient effect on all characters
    graphics.setPaint(possibleGradients[RANDOM.nextInt(possibleGradients.length)]);

    for (final char character : answer) {
      // Create a glyph vector for the character
      final GlyphVector glyphVector = font.createGlyphVector(ctx, new char[] {character});
      final Shape glyphShape = glyphVector.getOutline();
      final Rectangle glyphBounds = glyphShape.getBounds();

      // Apply a transformation to the glyph vector using AffineTransform
      final AffineTransform transformation = AffineTransform.getTranslateInstance(glyphX, glyphY);
      // Randomize character scale
      final double scaling = 1 + 0.2 * RANDOM.nextDouble();
      transformation.scale(scaling, scaling);

      // Draw the transformed character glyph shape
      final Shape transformedShape = transformation.createTransformedShape(glyphShape);
      // 40% chance that the text will be stroked
      if (RANDOM.nextInt(100) <= 40) {
        final float strokeWidth = 2 + scaleModifier * RANDOM.nextFloat();
        final Stroke stroke = new BasicStroke(strokeWidth);
        final Shape strokedTransformedShape = stroke.createStrokedShape(transformedShape);
        graphics.fill(strokedTransformedShape);
      } else {
        graphics.fill(transformedShape);
        // 90% chance that the text will have an outline
        if (RANDOM.nextInt(100) <= 90) {
          // Add an outline to the transformed character
          addCharacterOutline(graphics, transformedShape);
        }
      }

      // Increment the X coordinate for the next character by the bounds
      glyphX += glyphBounds.width;
      glyphY += (int) Math.floor(Math.sin(glyphX * 180) * 5);
    }
  }

  private void addCharacterOutline(final @NotNull Graphics2D graphics,
                                   final @NotNull Shape transformedShape) {
    final double minT = scaleModifier / 7.5D;
    final double tx = minT + scaleModifier * RANDOM.nextDouble();
    final double ty = minT + scaleModifier * RANDOM.nextDouble();
    final float strokeWidth = scaleModifier * RANDOM.nextFloat();

    // Draw the stroked shape
    final AffineTransform transform = AffineTransform.getTranslateInstance(tx, ty);
    // Create a stroked copy of the text and slightly offset/distort it
    final Stroke stroke = new BasicStroke(strokeWidth);
    final Shape strokedShape = stroke.createStrokedShape(transformedShape);
    // Draw the character outline
    graphics.fill(transform.createTransformedShape(strokedShape));
  }
}

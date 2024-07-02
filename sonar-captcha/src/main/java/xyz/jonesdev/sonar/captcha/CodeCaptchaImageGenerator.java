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

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

@Getter
@Setter
public final class CodeCaptchaImageGenerator extends CaptchaImageGenerator {
  public CodeCaptchaImageGenerator(final int width, final int height, final @Nullable File backgroundImage) {
    super(width, height, backgroundImage);

    this.scale = height / (128 / 2.5f);
    this.font = new Font(Font.MONOSPACED, Font.PLAIN, (int) (width / 2.5f));

    final Color[] colors = new Color[] {Color.RED, Color.BLUE, Color.CYAN, Color.ORANGE, Color.GREEN, Color.MAGENTA};
    this.possibleGradients = new GradientPaint[colors.length * colors.length];

    // Generate all possible gradient combinations
    for (int i = 0; i < possibleGradients.length; i++) {
      final Color color0 = colors[i % colors.length];
      final Color color1 = colors[(i + 1) % colors.length];
      possibleGradients[i] = new GradientPaint(0, 0, color0,
        width / 3f, height / 3f, color1, true);
    }
  }

  private final GradientPaint[] possibleGradients;
  private final float scale;
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

    // Add random displaced lines to the text
    /*final CrystallizeFilter crystallizeFilter = new CrystallizeFilter();
    crystallizeFilter.setTurbulence(0.1f + 0.25f * RANDOM.nextFloat());
    crystallizeFilter.setScale(1);
    foregroundImage = crystallizeFilter.filter(foregroundImage, null);*/

    // Add a bit of distortion to the text
    /*final MarbleFilter marbleFilter = new MarbleFilter();
    marbleFilter.setTurbulence(0.1f + 0.25f * RANDOM.nextFloat());
    foregroundImage = marbleFilter.filter(foregroundImage, null);*/

    // Apply a scratch filter for adding random lines on the image
    //new ScratchOverlayFilter(4, scale / 3f, graphics.getPaint()).transform(foregroundImage);

    // Apply a ripple filter for distorting the text on the image
    new RippleFilter(2, 2).transform(foregroundImage);

    // Create a noisy background image
    BufferedImage mergedImage = mergeImages(background, foregroundImage);

    // Draw random inverse-color circles on the merged image
    final int circleAmount = 2 + RANDOM.nextInt(2);
    final int minCircleRadius = (int) (Math.floor(20 * scale) / circleAmount);
    new CircleInverseFilter(circleAmount, minCircleRadius, minCircleRadius).transform(mergedImage);

    //mergedImage = new EmbossFilter().filter(mergedImage, null);

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
      final double scalingX = 0.9 + 0.25 * RANDOM.nextDouble();
      final double scalingY = 1.5 + 0.25 * RANDOM.nextDouble();
      transformation.scale(scalingX, scalingY);
      transformation.translate(scalingX, scalingY + scale * 4);

      // Draw the transformed character glyph shape
      final Shape transformedShape = transformation.createTransformedShape(glyphShape);
      // 35% chance that the text will be stroked
      if (RANDOM.nextInt(100) <= 35) {
        final float strokeWidth = 2.5f + scale * RANDOM.nextFloat();
        final Stroke stroke = new BasicStroke(strokeWidth);
        final Shape strokedTransformedShape = stroke.createStrokedShape(transformedShape);
        graphics.fill(strokedTransformedShape);
      } else {
        graphics.fill(transformedShape);
        // 85% chance that the text will have an outline
        if (RANDOM.nextInt(100) <= 85) {
          // Add an outline to the transformed character
          addCharacterOutline(graphics, transformedShape);
        }
      }

      // Increment the X coordinate for the next character by the bounds
      glyphX += glyphBounds.width;
      glyphY += (int) Math.floor(Math.sin(glyphX * 60) * 10);
    }
  }

  private void addCharacterOutline(final @NotNull Graphics2D graphics,
                                   final @NotNull Shape transformedShape) {
    final double minT = scale / 7.5D;
    final double tx = minT + scale * RANDOM.nextDouble();
    final double ty = minT + scale * RANDOM.nextDouble();
    final float strokeWidth = scale * RANDOM.nextFloat();

    // Draw the stroked shape
    final AffineTransform transform = AffineTransform.getTranslateInstance(tx, ty);
    // Create a stroked copy of the text and slightly offset/distort it
    final Stroke stroke = new BasicStroke(strokeWidth);
    final Shape strokedShape = stroke.createStrokedShape(transformedShape);
    // Draw the character outline
    graphics.fill(transform.createTransformedShape(strokedShape));
  }
}

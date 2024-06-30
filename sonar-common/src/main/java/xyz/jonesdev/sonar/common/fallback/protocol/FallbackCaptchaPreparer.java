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

package xyz.jonesdev.sonar.common.fallback.protocol;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.jonesdev.sonar.api.Sonar;
import xyz.jonesdev.sonar.api.config.SonarConfiguration;
import xyz.jonesdev.sonar.api.timer.SystemTimer;
import xyz.jonesdev.sonar.captcha.CodeCaptchaImageGenerator;
import xyz.jonesdev.sonar.common.fallback.protocol.map.MapCaptchaInfo;
import xyz.jonesdev.sonar.common.fallback.protocol.map.MapColorPalette;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class FallbackCaptchaPreparer {
  private final ExecutorService PREPARATION_SERVICE = Executors.newSingleThreadExecutor();
  private final Random RANDOM = new Random();

  private MapCaptchaInfo[] cached;
  private int preparedAmount;

  public void prepare() {
    // Make sure we're not preparing when Sonar is already preparing answers
    if (cached != null && preparedAmount != cached.length) return;
    preparedAmount = 0;

    final SystemTimer timer = new SystemTimer();
    Sonar.get().getLogger().info("Asynchronously preparing CAPTCHA answers...");
    Sonar.get().getLogger().info("Players will be able to join even if the preparation isn't finished");

    // Prepare cache
    final SonarConfiguration.Verification.MapCaptcha config = Sonar.get().getConfig().getVerification().getMapCaptcha();
    cached = new MapCaptchaInfo[config.getPrecomputeAmount()];

    // Prepare everything asynchronously
    PREPARATION_SERVICE.execute(() -> {
      // Create the images using capja
      final @Nullable File backgroundImage = Sonar.get().getConfig().getVerification().getMapCaptcha().getBackgroundImage();
      final CodeCaptchaImageGenerator generator = new CodeCaptchaImageGenerator(128, 128, backgroundImage);
      final char[] dictionary = config.getDictionary().toCharArray();

      for (preparedAmount = 0; preparedAmount < config.getPrecomputeAmount(); preparedAmount++) {
        final char[] answer = getRandomAnswer(5, dictionary);
        final BufferedImage image = generator.createImage(answer);
        // Convert and cache converted Minecraft map bytes
        final int[] buffer = MapColorPalette.getBufferFromImage(image);
        cached[preparedAmount] = new MapCaptchaInfo(image.getWidth(), image.getHeight(), new String(answer), buffer);
      }

      Sonar.get().getLogger().info("Finished preparing {} CAPTCHA answers ({}s)!", preparedAmount, timer);
    });
  }

  @SuppressWarnings("all")
  private static char @NotNull [] getRandomAnswer(final int length, final char[] dictionary) {
    final char[] answer = new char[length];
    for (int i = 0; i < length; i++) {
      answer[i] = dictionary[ThreadLocalRandom.current().nextInt(dictionary.length)];
    }
    return answer;
  }

  public boolean isCaptchaAvailable() {
    return cached != null && cached[0] != null;
  }

  public MapCaptchaInfo getRandomCaptcha() {
    // Give the player a random CAPTCHA out of the ones that we've already prepared
    return cached[RANDOM.nextInt(preparedAmount)];
  }
}

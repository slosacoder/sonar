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

package xyz.jonesdev.sonar.captcha.test;

import lombok.experimental.UtilityClass;
import xyz.jonesdev.sonar.captcha.CodeCaptchaImageGenerator;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class DevCaptchaImageGeneratorTest {
  public void main(final String... args) throws IOException {
    ImageIO.write(new CodeCaptchaImageGenerator(128 * 3, 128 * 2, null)
        .createImage(getRandomString(5)), "png", new File("output.png"));
  }

  private static final char[] DICTIONARY = "0123456789".toCharArray();

  private static char[] getRandomString(final int length) {
    final char[] answer = new char[length];
    for (int j = 0; j < length; j++) {
      answer[j] = DICTIONARY[ThreadLocalRandom.current().nextInt(DICTIONARY.length)];
    }
    return answer;
  }
}

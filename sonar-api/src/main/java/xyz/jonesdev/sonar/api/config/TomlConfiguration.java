package xyz.jonesdev.sonar.api.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.Sonar;

import java.io.File;
import java.net.URL;

@Getter
public final class TomlConfiguration {
  private final File configFile;
  private final @NotNull CommentedFileConfig config;

  public TomlConfiguration(final File configFile, final String defaultFallbackResource) {
    this.configFile = configFile;

    final URL defaultConfigLocation = Sonar.class.getClassLoader().getResource(defaultFallbackResource);
    if (defaultConfigLocation == null) {
      throw new RuntimeException("Default configuration file " + defaultFallbackResource + " does not exist");
    }

    this.config = CommentedFileConfig.builder(configFile)
      .defaultData(defaultConfigLocation)
      .preserveInsertionOrder()
      .autosave()
      .sync()
      .build();
  }

  public static @NotNull TomlConfiguration create(final @NotNull File dataDirectory, final String fileName) {
    return create(dataDirectory, fileName, fileName);
  }

  public static @NotNull TomlConfiguration create(final @NotNull File dataDirectory, final String fileName,
                                                  final @NotNull String defaultFallbackResource) {
    if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
      throw new RuntimeException("Could not create data directory!");
    }
    final File file = new File(dataDirectory, fileName);
    return new TomlConfiguration(file, defaultFallbackResource);
  }
}

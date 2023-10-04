package xyz.jonesdev.sonar.api.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.Sonar;

import java.util.List;

@RequiredArgsConstructor
public abstract class SonarConfiguration {
  protected final @NotNull CommentedFileConfig config;

  protected boolean needsSave;

  public abstract void load();

  @Contract("_ -> new")
  protected static @NotNull String parseFromList(final @NotNull List<String> l) {
    return String.join(Sonar.LINE_SEPARATOR, l);
  }

  protected static @NotNull Component toComponent(final @NotNull String string) {
    return MiniMessage.miniMessage().deserialize(string);
  }

  protected <T> T getOrElse(final String path, final T def) {
    if (!config.contains(path)) {
      config.set(path, def);
      needsSave = true;
      return def;
    }
    return config.getOrElse(path, def);
  }

  public final void setSaveAndLoad(final String path, final Object value) {
    config.set(path, value);
    config.save();
    load();
  }
}

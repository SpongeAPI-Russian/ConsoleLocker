package dk.xakeps.consolelock;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class Config {
    private final List<String> allowedCommands;
    private final Locale locale;
    private byte[] password;
    private ResourceBundle resourceBundle;

    public Config(Path pluginDir, Locale locale, List<String> allowedCommands, byte[] password) {
        this.allowedCommands = allowedCommands;
        this.locale = locale;
        this.password = Arrays.copyOf(password, password.length);
        this.resourceBundle = ResourceBundle.getBundle("lang", locale, new SpongeControl(pluginDir));
    }

    public List<String> getAllowedCommands() {
        return allowedCommands;
    }

    public Locale getLocale() {
        return locale;
    }

    public byte[] getPassword() {
        return Arrays.copyOf(password, password.length);
    }

    public void setPassword(byte[] password) {
        this.password = Arrays.copyOf(password, password.length);
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Config config = (Config) o;

        if (!allowedCommands.equals(config.allowedCommands)) return false;
        if (!locale.equals(config.locale)) return false;
        if (!Arrays.equals(password, config.password)) return false;
        return resourceBundle.equals(config.resourceBundle);
    }

    @Override
    public int hashCode() {
        int result = allowedCommands.hashCode();
        result = 31 * result + locale.hashCode();
        result = 31 * result + Arrays.hashCode(password);
        result = 31 * result + resourceBundle.hashCode();
        return result;
    }
}

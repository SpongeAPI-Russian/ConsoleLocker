package dk.xakeps.consolelock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class SpongeControl extends ResourceBundle.Control {

    private final Path pluginDir;

    public SpongeControl(Path pluginDir) {
        this.pluginDir = pluginDir;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");
        Path resource = pluginDir.resolve(resourceName);
        try (InputStream stream = Files.newInputStream(resource)) {
            return new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }
}

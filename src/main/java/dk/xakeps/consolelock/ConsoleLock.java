package dk.xakeps.consolelock;

import com.google.common.reflect.TypeToken;
import dk.xakeps.consolelock.command.ConsoleLockExecutor;
import dk.xakeps.consolelock.command.SetPasswordExecutor;
import dk.xakeps.consolelock.serializers.ConfigSerializer;
import dk.xakeps.consolelock.serializers.LocaleSerializer;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Plugin(id = "console-lock",
        name = "Console locker",
        version = "0.1-SNAPSHOT",
        description = "Locks console with specific password",
        url = "https://spongeapi.com",
        authors = "Xakep_SDK")
public class ConsoleLock {

    private final MessageDigest messageDigest;
    private final ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private final Config config;

    private boolean locked;

    @Inject
    public ConsoleLock(Logger logger,
                       @ConfigDir(sharedRoot = false) Path pluginDir,
                       @DefaultConfig(sharedRoot = false) Path configPath,
                       PluginContainer container)
            throws NoSuchAlgorithmException {
        saveFile(container, "lang.properties", pluginDir);
        saveFile(container, "lang_ru.properties", pluginDir);
        saveFile(container, "console-lock.conf", pluginDir);

        this.messageDigest = MessageDigest.getInstance("SHA-256");
        this.configLoader = HoconConfigurationLoader.builder().setPath(configPath).build();
        Config config = new Config(pluginDir, Locale.ENGLISH, new ArrayList<>(Arrays.asList(
                "console-lock:console-lock", "console-lock:clock", "console-lock:cl",
                "console-lock", "clock", "cl",
                "minecraft:stop", "stop")), new byte[0]);
        try {
            ConfigurationOptions defaults = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
            TypeSerializerCollection serializers = defaults.getSerializers().newChild();
            serializers.registerType(TypeToken.of(Config.class), new ConfigSerializer(logger, pluginDir));
            serializers.registerType(TypeToken.of(Locale.class), new LocaleSerializer());
            CommentedConfigurationNode rootNode = configLoader.load(defaults.setSerializers(serializers));
            Config tmp = rootNode.getValue(TypeToken.of(Config.class), config);
            configLoader.save(rootNode);
            if(tmp == config) {
                logger.warn(config.getResourceBundle().getString("password.notFound"));
            } else {
                config = tmp;
            }
        } catch (IOException | ObjectMappingException e) {
            logger.warn(config.getResourceBundle().getString("config.error"), e);
        }
        this.config = config;
    }

    @Listener
    public void onGamePreInit(GamePreInitializationEvent event) {
        CommandSpec setPassword = CommandSpec.builder()
                .arguments(GenericArguments.remainingRawJoinedStrings(Text.of("password")))
                .executor(new SetPasswordExecutor(this, messageDigest, config))
                .build();

        CommandSpec consoleLock = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(Text.of("password"))))
                .child(setPassword, "setpw", "pw", "set")
                .executor(new ConsoleLockExecutor(this, messageDigest, config))
                .build();
        Sponge.getCommandManager().register(this, consoleLock, "console-lock", "clock", "cl");
    }

    @Listener
    public void onCommand(SendCommandEvent event, @Root ConsoleSource sender) {
        String command = event.getCommand();
        if(locked && !config.getAllowedCommands().contains(command)) {
            sender.sendMessage(Text.of(config.getResourceBundle().getString("console.locked")));
            event.setCancelled(true);
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    private static void saveFile(PluginContainer container, String fileName, Path directory) {
        container.getAsset(fileName).ifPresent(asset -> {
            try {
                asset.copyToDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

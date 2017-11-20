package dk.xakeps.consolelock.command;

import dk.xakeps.consolelock.Config;
import dk.xakeps.consolelock.ConsoleLock;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.security.MessageDigest;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConsoleLockExecutor implements CommandExecutor {
    private final ConsoleLock consoleLock;
    private final MessageDigest messageDigest;
    private final Config config;

    public ConsoleLockExecutor(ConsoleLock consoleLock, MessageDigest messageDigest, Config config) {
        this.consoleLock = consoleLock;
        this.messageDigest = messageDigest;
        this.config = config;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        ResourceBundle resourceBundle = config.getResourceBundle();
        if(src instanceof ConsoleSource) {
            Optional<String> password = args.getOne("password");
            if (config.getPassword().length == 32) {
                if (consoleLock.isLocked()) {
                    if (password.isPresent()) {
                        String pwStr = password.get();
                        byte[] digest = messageDigest.digest(pwStr.getBytes());
                        if (MessageDigest.isEqual(digest, config.getPassword())) {
                            consoleLock.setLocked(false);
                            src.sendMessage(Text.of(resourceBundle.getString("console.unlocked")));
                        } else {
                            src.sendMessage(Text.of(resourceBundle.getString("password.wrong")));
                        }
                    } else {
                        src.sendMessage(Text.of(resourceBundle.getString("password.required")));
                    }
                } else {
                    consoleLock.setLocked(true);
                    src.sendMessage(Text.of(resourceBundle.getString("console.locked")));
                }
            } else {
                src.sendMessage(Text.of(resourceBundle.getString("password.empty")));
            }
        } else {
            src.sendMessage(Text.of(resourceBundle.getString("sender.wrong")));
        }
        return CommandResult.success();
    }
}

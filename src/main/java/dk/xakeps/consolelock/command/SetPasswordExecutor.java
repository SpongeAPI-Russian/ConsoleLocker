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
import java.util.ResourceBundle;

public class SetPasswordExecutor implements CommandExecutor {
    private final ConsoleLock consoleLock;
    private final MessageDigest messageDigest;
    private final Config config;

    public SetPasswordExecutor(ConsoleLock consoleLock, MessageDigest messageDigest, Config config) {
        this.consoleLock = consoleLock;
        this.messageDigest = messageDigest;
        this.config = config;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        ResourceBundle resourceBundle = config.getResourceBundle();
        if(src instanceof ConsoleSource) {
            if (!consoleLock.isLocked()) {
                String password = args.<String>getOne("password").get();
                if (!password.isEmpty()) {
                    if (!password.equalsIgnoreCase("setpw")
                            && !password.equalsIgnoreCase("pw")
                            && !password.equalsIgnoreCase("set")) {
                        config.setPassword(messageDigest.digest(password.getBytes()));
                        consoleLock.setLocked(true);
                        src.sendMessage(Text.of(resourceBundle.getString("password.set.success")));
                    } else {
                        src.sendMessage(Text.of("password.set.wrongPassword"));
                    }
                } else {
                    src.sendMessage(Text.of(resourceBundle.getString("password.set.wrongPassword")));
                }
            } else {
                src.sendMessage(Text.of(resourceBundle.getString("console.locked")));
            }
        } else {
            src.sendMessage(Text.of(resourceBundle.getString("sender.wrong")));
        }
        return CommandResult.success();
    }
}

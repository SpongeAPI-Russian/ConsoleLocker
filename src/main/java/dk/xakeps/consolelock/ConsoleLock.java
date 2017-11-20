package dk.xakeps.consolelock;

import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Plugin(id = "console-lock",
        name = "Console locker",
        version = "0.1-SNAPSHOT",
        description = "Locks console with specific password",
        url = "https://spongeapi.com",
        authors = "Xakep_SDK")
public class ConsoleLock {

    private final MessageDigest messageDigest;

    private byte[] lockPasswordHash;
    private boolean locked;

    @Inject
    public ConsoleLock(Logger logger) throws NoSuchAlgorithmException {
        try {
            this.lockPasswordHash = BaseEncoding.base16().decode(System.getProperty("consoleLockPassword"));
            logger.info("Password found! Console was locked!");
        } catch (IllegalArgumentException | NullPointerException e) {
            this.lockPasswordHash = new byte[0];
            logger.warn("Password not found! Set password SHA-256 representation using command line argument(-DconsoleLockPassword=<hash>) or use /cl set <password>");
        }
        this.locked = lockPasswordHash.length == 32;
        this.messageDigest = MessageDigest.getInstance("SHA-256");
    }

    @Listener
    public void onGamePreInit(GamePreInitializationEvent event) {
        CommandSpec setPassword = CommandSpec.builder()
                .arguments(GenericArguments.remainingRawJoinedStrings(Text.of("password")))
                .executor((src, args) -> {
                    if(src instanceof ConsoleSource) {
                        if (!locked) {
                            String password = args.<String>getOne("password").get();
                            if (!password.isEmpty()) {
                                if (!password.equalsIgnoreCase("setpw")
                                        && !password.equalsIgnoreCase("pw")
                                        && !password.equalsIgnoreCase("set")) {
                                    this.lockPasswordHash = messageDigest.digest(password.getBytes());
                                    this.locked = true;
                                    src.sendMessage(Text.of("Password was set!"));
                                } else {
                                    src.sendMessage(Text.of("You can't set setpw/pw/set or empty string as password!"));
                                }
                            } else {
                                src.sendMessage(Text.of("Can't set empty string as password!"));
                            }
                        } else {
                            src.sendMessage(Text.of("Unlock console first!"));
                        }
                    } else {
                        src.sendMessage(Text.of("This command can be used only from console!"));
                    }
                    return CommandResult.success();
                })
                .build();

        CommandSpec consoleLock = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(Text.of("password"))))
                .child(setPassword, "setpw", "pw", "set")
                .executor((src, args) -> {
                    if(src instanceof ConsoleSource) {
                        Optional<String> password = args.getOne("password");
                        if (lockPasswordHash.length == 32) {
                            if (locked) {
                                if (password.isPresent()) {
                                    String pwStr = password.get();
                                    byte[] digest = messageDigest.digest(pwStr.getBytes());
                                    if (MessageDigest.isEqual(digest, lockPasswordHash)) {
                                        this.locked = false;
                                        src.sendMessage(Text.of("Console unlocked!"));
                                    } else {
                                        src.sendMessage(Text.of("Wrong password!"));
                                    }
                                } else {
                                    src.sendMessage(Text.of("Password required!"));
                                }
                            } else {
                                this.locked = true;
                                src.sendMessage(Text.of("Console locked!"));
                            }
                        } else {
                            src.sendMessage(Text.of("No password set! Ignoring command."));
                        }
                    } else {
                        src.sendMessage(Text.of("This command can be used only from console!"));
                    }
                    return CommandResult.success();
                })
                .build();
        Sponge.getCommandManager().register(this, consoleLock, "console-lock", "clock", "cl");
    }

    @Listener
    public void onCommand(SendCommandEvent event, @Root ConsoleSource sender) {
        String command = event.getCommand();
        boolean allowedCmd = command.equalsIgnoreCase("console-lock:console-lock")
                || command.equalsIgnoreCase("console-lock:clock")
                || command.equalsIgnoreCase("console-lock:cl")
                || command.equalsIgnoreCase("console-lock")
                || command.equalsIgnoreCase("clock")
                || command.equalsIgnoreCase("cl")
                || command.equalsIgnoreCase("minecraft:stop")
                || command.equalsIgnoreCase("stop");
        if(locked && !allowedCmd) {
            sender.sendMessage(Text.of("Console locked! Unlock console using /cl <password>"));
            event.setCancelled(true);
        }
    }
}

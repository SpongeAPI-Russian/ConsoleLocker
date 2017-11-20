package dk.xakeps.consolelock.serializers;

import com.google.common.reflect.TypeToken;
import dk.xakeps.consolelock.Config;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;

public class ConfigSerializer implements TypeSerializer<Config> {
    private final Logger logger;
    private final Path pluginDir;

    public ConfigSerializer(Logger logger, Path pluginDir) {
        this.logger = logger;
        this.pluginDir = pluginDir;
    }

    @Override
    public Config deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        List<String> defaults = Arrays.asList(
                "console-lock:console-lock", "console-lock:clock", "console-lock:cl",
                "console-lock", "clock", "cl",
                "minecraft:stop", "stop");

        List<String> allowedCommands = value.getNode("allowedCommands").getList(TypeToken.of(String.class), new ArrayList<>(defaults));
        String password = System.getProperty("consoleLockPassword");
        if(StringUtils.isEmpty(password)) {
            password = value.getNode("password").getString();
        }
        Locale locale = value.getNode("locale").getValue(TypeToken.of(Locale.class), Locale.ENGLISH);
        Config config = new Config(pluginDir, locale, allowedCommands, StringUtils.isEmpty(password) ? new byte[0] : hexStringToByteArray(password));
        ResourceBundle resourceBundle = config.getResourceBundle();
        if(StringUtils.isEmpty(password)) {
            logger.warn(resourceBundle.getString("password.notFound"));
        } else if(password.equalsIgnoreCase("773d971b5d2e1e21c524e74de11f399554db2795f97d1dd032b45b41ec179c7d") //setpw hash
                || password.equalsIgnoreCase("6ee0eb490ff832101cf82a3d387c35f29e4230be786978f7acf9e811febf6723") //set hash
                || password.equalsIgnoreCase("30c952fab122c3f9759f02a6d95c3758b246b4fee239957b2d4fee46e26170c4")) { //pw hash
            logger.warn(resourceBundle.getString("password.set.wrongPassword"));
            config.setPassword(new byte[0]);
        } else {
            logger.info(resourceBundle.getString("password.foundAndLocked"));
        }
        return config;
    }

    @Override
    public void serialize(TypeToken<?> type, Config obj, ConfigurationNode value) throws ObjectMappingException {
        value.getNode("allowedCommands").setValue(obj.getAllowedCommands());
        if(obj.getPassword().length != 32) {
            value.getNode("password").setValue("");
        } else {
            value.getNode("password").setValue(String.format("%064x", new BigInteger(1, obj.getPassword())));
        }
        value.getNode("locale").setValue(TypeToken.of(Locale.class), obj.getLocale());
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}

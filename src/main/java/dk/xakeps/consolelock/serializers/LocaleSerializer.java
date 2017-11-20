package dk.xakeps.consolelock.serializers;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.Locale;

public class LocaleSerializer implements TypeSerializer<Locale> {
    @Override
    public Locale deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        return Locale.forLanguageTag(value.getString());
    }

    @Override
    public void serialize(TypeToken<?> type, Locale obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(obj.toLanguageTag());
    }
}

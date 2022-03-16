package fun.milkyway.milkypixelart.managers;

import fun.milkyway.milkypixelart.MilkyPixelart;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class LangManager {
    private static LangManager instance;
    private final MiniMessage miniMessage;

    private LangManager() {
        miniMessage = MiniMessage.builder().build();
    }

    public synchronized static LangManager getInstance() {
        if (instance == null) {
            instance = new LangManager();
        }
        return instance;
    }

    public synchronized static void reload() {
        instance = new LangManager();
    }

    public @NotNull Component getLang(@NotNull String key, @NotNull String... args) {
        String format = MilkyPixelart.getInstance().getConfiguration().getString(key);

        if (format == null) {
            return Component.empty();
        }

        if (args.length > 0) {
            TagResolver[] placeholders = new TagResolver[args.length];

            for (int i = 0; i < args.length; i++) {
                placeholders[i] = TagResolver.resolver(Placeholder.parsed("arg"+(i+1), args[i]));
            }

            return miniMessage.deserialize(format, placeholders);
        }
        else {
            return miniMessage.deserialize(format);
        }
    }

    public @NotNull String getLangPlain(@NotNull String key, @NotNull String... args) {
        return PlainTextComponentSerializer.plainText().serialize(getLang(key, args));
    }
}

package uk.tldcode.extraterrestrialprobemachine.plugins;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.pircbotx.hooks.events.MessageEvent;
import uk.tldcode.extraterrestrialprobemachine.api.*;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class JavaTestPlugin extends Plugin {
    public JavaTestPlugin(@NotNull String name) {
        super(name);
    }

    public void Init() {
        getCommands().put("!JAVA", new JavaCommand() {

            public void invoke(@NotNull MessageEvent event, @NotNull Consumer<String> respond) {
                respond.accept("JAVA!");
            }

            @NotNull
            public UserLevel UserLevel(@NotNull MessageEvent event) {
                return UserLevel.Caster;
            }


            public boolean match(@NotNull String message) {
                return message.startsWith("!JAVA");
            }
        });
    }

    @Override
    public void PostInit() {

    }
}

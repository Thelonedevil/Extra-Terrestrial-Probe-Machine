package uk.tldcode.extraterrestrialprobemachine.plugins;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.pircbotx.hooks.events.MessageEvent;
import uk.tldcode.extraterrestrialprobemachine.api.Command;
import uk.tldcode.extraterrestrialprobemachine.api.Plugin;
import uk.tldcode.extraterrestrialprobemachine.api.UserLevel;

public class JavaTestPlugin extends Plugin {
    public JavaTestPlugin(@NotNull String name) {
        super(name);
    }

    public void Init() {
        getCommands().put("!JAVA", new Command() {

            public void invoke(@NotNull MessageEvent event, @NotNull Function1<? super String, Unit> respond) {
                respond.invoke("JAVA!");
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

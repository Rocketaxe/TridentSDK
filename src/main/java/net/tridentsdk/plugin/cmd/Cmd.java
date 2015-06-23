package net.tridentsdk.plugin.cmd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method which will be called when a command is sent to the server
 *
 * @author The TridentSDK Team
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cmd {
    /**
     * @return command aliases
     */
    String[] aliases();

    /**
     * @return the permission for the command
     */
    String perm();

    /**
     * @return the issuers, as specified in {@link CmdRegistrar#forOnly(int)}
     */
    int[] issuers();
}

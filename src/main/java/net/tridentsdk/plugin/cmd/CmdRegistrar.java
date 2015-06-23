package net.tridentsdk.plugin.cmd;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import net.tridentsdk.entity.living.Player;
import net.tridentsdk.factory.Factories;
import net.tridentsdk.plugin.Plugin;
import net.tridentsdk.util.TridentLogger;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Use this class to bind commands to their executors or handlers
 * <p>
 * <p>Example:
 * <pre><code>
 *     import static net.tridentsdk.plugin.cmd.CommandRegistrar.*;
 * <p>
 *     bindAll("eat", "reload").to(CmdExecutor.class)
 *         .withAliases("consume", "munch").permit("perm.eat").forOnly(CONSOLE, BLOCKS).and()
 *         .withAliases("reloadAll").permit("perm.reload").forOnly(CONSOLE)
 *         .complete();
 * </code></pre></p>
 * <p>
 * <p>Conflicting methods will change the latest to {plugin_name}${command}</p>
 * <p>
 * <p>Commands are edited by order bound. As seen in the example, and() indicates the next command to edit.</p>
 * <p>
 * <p>In order for this to take effect, you must call {@link #complete()}. Failing to do so will result in
 * the operation passed as NOP. Editing the commands after callig complete will result in undefined behavior.</p>
 * <p>
 * <p>If Java keywords are used as commands, the method should contain $ to remove the error. You may choose
 * where to place it, but it is recommened to place it at the end of the method name.</p>
 *
 * @author The TridentSDK Team
 */
@NotThreadSafe
public class CmdRegistrar {
    public static final int CONSOLE = 0;
    public static final int PLAYERS = 1;
    public static final int BLOCKS = 2;
    public static final int ALL = 3;

    private static final Map<String, CmdWrapper> COMMAND_MAP = Factories.collect().createMap();
    private static final String SPLIT = "$";

    private final CmdWrapper NULL_DATA = new CmdWrapper();
    private final Plugin plugin;
    private final List<CmdWrapper> wrappers = Lists.newArrayList();
    private final List<String> toRegister = Lists.newArrayList();
    // I should have used a Set, but I want index based searching!!!!
    int index = 0;

    private CmdRegistrar(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Obtains the command registrar that will register commands to a particular plugin
     *
     * @param plugin the plugin registering the commands
     * @return the command registrar for the plugin provided
     */
    public static CmdRegistrar in(Plugin plugin) {
        return new CmdRegistrar(plugin);
    }

    /**
     * Runs the method that handles the command
     *
     * @param command the command to be rum
     * @param issuer  the issuer of the command
     */
    public static void handle(String command, CommandIssuer issuer) {
        String[] strings = command.split(" ");
        CmdWrapper wrapper = COMMAND_MAP.get(strings[0]);

        if (wrapper == null) {
            issuer.sendRaw("Command \"" + strings[0] + "\" doesn't exist");
            return;
        }

        String[] args = new String[strings.length - 1];
        System.arraycopy(strings, 1, args, 0, strings.length - 1);
        wrapper.invoke(wrapper.command, issuer, args, strings[0]);
    }

    /**
     * Binds the command
     * <p>
     * <p>Commands that intersect with others will be named: "{plugin_name}${command}"</p>
     *
     * @param string the command
     * @return the current instance
     */
    public CmdRegistrar bind(String string) {
        string = split(string.replaceAll("\\$", ""));

        // Ensure the command isn't registering again in the same plugin
        if (!string.contains(SPLIT) && toRegister.contains(string)) {
            string = split(string);
        }

        toRegister.add(string);
        wrappers.add(NULL_DATA); // Size the list correctly

        return this;
    }

    /**
     * Bind all the commands specified
     *
     * @param command the command to bind
     * @return the current instance
     */
    public CmdRegistrar bindAll(String... command) {
        for (String s : command) {
            bind(s);
        }

        return this;
    }

    /**
     * Will bind every method to a new command which may be invoked using the annotation specified alias or
     * the command name stripped of {@code $} if it is a keyword.
     *
     * @param cls       the class to register all methods as commands
     * @param annotated {@code true} to only register annotated command methods
     * @return the current instance
     */
    public CmdRegistrar searchAndBind(Class<?> cls, boolean annotated) {
        for (Method method : cls.getDeclaredMethods()) {
            if (annotated) {
                if (method.getAnnotation(Cmd.class) != null) {
                    bind(method.getName()).to(method);
                }
            } else {
                bind(method.getName()).to(method);
            }
        }
        return this;
    }

    /**
     * Finds the command matching the name of the method, and binds the command to it
     * <p>
     * <p>The method must be: {@code {command}(String cmd, CommandIssuer issuer, String[] args, String label)}</p>
     * <p>
     * <p>Label names are not accepted. The name of the method must be the one passed into the bind method.</p>
     *
     * @param method the method to bind
     * @return the current instance
     */
    public CmdRegistrar to(Method method) {
        Class<?> cls = method.getDeclaringClass();

        Cmd cmd;
        String string = method.getName();
        String split = split(string.replaceAll("\\$", ""));
        int idx = toRegister.indexOf(split);
        if ((cmd = method.getAnnotation(Cmd.class)) != null) {
            wrappers.set(idx, new CmdWrapper(split, cls, OpFactory.annotation(cmd, method)));
        } else {
            if (idx == -1) return this;

            wrappers.set(idx, new CmdWrapper(split, cls, OpFactory.methodName(method)));
        }

        return this;
    }

    /**
     * Binds the command to the given methods in the class
     * <p>
     * <p>This method finds the methods in the class which match the command name.</p>
     *
     * @param cls the class to search
     * @return the current instance
     */
    public CmdRegistrar to(Class<?> cls) {
        for (Method method : cls.getDeclaredMethods()) {
            to(method);
        }
        return this;
    }

    /**
     * Searches the group of classes for the method to bind each method to
     *
     * @param cls the classes to search
     * @return the current instance
     */
    public CmdRegistrar to(Class... cls) {
        for (Class<?> c : cls) {
            to(c);
        }

        return this;
    }

    /**
     * Searches all the classes in the given package for the method to bind to
     *
     * @param pack the package to search
     * @return the current instance
     */
    public CmdRegistrar toAllIn(Package pack) {
        try {
            ClassPath path = ClassPath.from(plugin.classLoader);
            for (ClassPath.ClassInfo info : path.getAllClasses()) {
                to(info.load());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Sets the aliases for the current editing command
     *
     * @param aliases the aliases to set
     * @return the current instance
     */
    public CmdRegistrar withAliases(String... aliases) {
        CmdWrapper currentWrapper = wrappers.get(index);
        if (currentWrapper.aliases != null) {
            TridentLogger.warn("You have already edited the alias for this command, try and()");
        }

        currentWrapper.aliases = aliases;
        return this;
    }

    /**
     * Permits only the players with the permission to execute
     *
     * @param permission the permission
     * @return the current instance
     */
    public CmdRegistrar permit(String permission) {
        CmdWrapper currentWrapper = wrappers.get(index);
        if (currentWrapper.perm != null) {
            TridentLogger.warn("You have already edited the permission for this command, try and()");
        }

        currentWrapper.perm = permission;
        return this;
    }

    /**
     * Allows only the issuers (given by the fields in this class, ie {@link #PLAYERS} for only players)
     *
     * @param issuers the issuers
     * @return the current instance
     */
    public CmdRegistrar forOnly(int... issuers) {
        CmdWrapper currentWrapper = wrappers.get(index);
        if (currentWrapper.issuers != null) {
            TridentLogger.warn("You have already edited the issuers for this command, try and()");
        }

        currentWrapper.issuers = issuers;
        return this;
    }

    /**
     * Moves on to editing the properties of the next command
     * <p>
     * <p>If called 3 times for 2 binding commands, an error is thrown</p>
     *
     * @return the current instance
     */
    public CmdRegistrar and() {
        index++;

        if (index >= toRegister.size()) {
            TridentLogger.error("You have exceeded the editing of commands provided");
        }

        return this;
    }

    /**
     * Pushes all the changes to the command map. Required operation.
     */
    public void complete() {
        for (int i = 0; i < toRegister.size(); i++) {
            String cmd = toRegister.get(i);
            CmdWrapper wrapper = wrappers.get(i);

            if (wrapper == null || wrapper == NULL_DATA) {
                TridentLogger.error("Couldn't find the executor for " + cmd);
                return;
            }

            wrapper.publish();
            COMMAND_MAP.put(cmd, wrapper);

            if (wrapper.aliases != null) {
                for (String a : wrapper.aliases) {
                    if (COMMAND_MAP.containsKey(a)) {
                        a = split(a);
                    }

                    COMMAND_MAP.put(a, wrapper);
                }
            }
        }
    }

    private String split(String string) {
        if (COMMAND_MAP.containsKey(string)) {
            String cmd = plugin.description().name() + SPLIT + string;
            TridentLogger.warn(string + " has already be registered... Renaming to " + cmd);
            return cmd;
        } else {
            return string;
        }
    }

    private interface ExecOp {
        void handle(String command, CommandIssuer issuer, String[] args, String label);

        void setup(CmdWrapper wrapper);
    }

    private static class OpFactory {
        public static ExecOp annotation(Cmd cmd, Method method) {
            return new ExecOp() {
                ExecOp invoke = OpFactory.methodName(method);

                @Override
                public void handle(String command, CommandIssuer issuer, String[] args, String label) {
                    invoke.handle(command, issuer, args, label);
                }

                @Override
                public void setup(CmdWrapper wrapper) {
                    invoke.setup(wrapper);
                    wrapper.aliases = cmd.aliases();
                    wrapper.perm = cmd.perm();
                    wrapper.issuers = cmd.issuers();
                }
            };
        }

        public static ExecOp methodName(Method cmd) {
            return new ExecOp() {
                private volatile CmdWrapper wrapper;

                @Override
                public void handle(String command, CommandIssuer issuer, String[] args, String label) {
                    if (checkIssuer(issuer, wrapper.issuers, wrapper.perm)) {
                        try {
                            cmd.invoke(wrapper.instance, command, issuer, args, label);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void setup(CmdWrapper wrapper) {
                    cmd.setAccessible(true);
                    this.wrapper = wrapper;
                }
            };
        }

        private static boolean checkIssuer(CommandIssuer issuer, int[] issuers, String permission) {
            if (issuers[0] == ALL && issuer instanceof Player) {
                issuer.sendRaw("You cannot execute this command");
                return ((Player) issuer).holdsPermission(permission);
            }

            if (issuer instanceof ServerConsole) {
                if (!contains(issuers, CONSOLE)) {
                    issuer.sendRaw("You cannot execute this command");
                    return false;
                }
            } else if (issuer instanceof Player) {
                if (!contains(issuers, PLAYERS) || (!((Player) issuer).holdsPermission(permission) && permission.trim().length() != 0)) {
                    issuer.sendRaw("You cannot execute this command");
                    return false;
                }
            } // TODO command blocks

            return true;
        }

        private static boolean contains(int[] arr, int type) {
            for (int i = 0; i < arr.length; i++) {
                if (i == type) return true;
            }

            return false;
        }
    }

    private class CmdWrapper {
        private final ExecOp op;
        private final String command;
        private Object instance;
        private String[] aliases;
        private String perm;
        private int[] issuers;

        public CmdWrapper() {
            op = null;
            command = null;
        }

        public CmdWrapper(String cmd, Class<?> cls, ExecOp op) {
            this.op = op;
            this.command = cmd;
            try {
                this.instance = cls.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("You command can't be instantiated", e);
            }
            op.setup(this);
        }

        public void invoke(String command, CommandIssuer issuer, String[] args, String label) {
            op.handle(command, issuer, args, label);
        }

        public void publish() {
            if (aliases == null) aliases = new String[0];
            if (perm == null) perm = "";
            if (issuers == null) issuers = new int[]{ALL };
        }
    }
}

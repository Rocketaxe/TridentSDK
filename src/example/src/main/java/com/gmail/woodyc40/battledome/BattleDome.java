package com.gmail.woodyc40.battledome;

import net.tridentsdk.plugin.Plugin;
import net.tridentsdk.plugin.annotation.PluginDescription;

/**
 * Battledome main class
 *
 * @author Pierre C
 */
@PluginDescription(name = "BattleDome", author = "Pierre C")
public class BattleDome extends Plugin {
    @Override
    public void onEnable() {
        GameManager manager = GameManager.newHandler();
        manager.loadGames();
    }

    @Override
    public void onDisable() {
        GameManager manager = GameManager.newHandler();
        manager.save();
    }
}

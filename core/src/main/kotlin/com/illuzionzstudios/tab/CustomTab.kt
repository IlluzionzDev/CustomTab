package com.illuzionzstudios.tab

import com.illuzionzstudios.mist.Logger
import com.illuzionzstudios.mist.config.PluginSettings
import com.illuzionzstudios.mist.config.locale.PluginLocale
import com.illuzionzstudios.mist.plugin.SpigotPlugin
import com.illuzionzstudios.tab.api.CustomTabAPI
import com.illuzionzstudios.tab.command.TabCommand
import com.illuzionzstudios.tab.group.GroupController
import com.illuzionzstudios.tab.settings.Locale
import com.illuzionzstudios.tab.settings.Settings
import com.illuzionzstudios.tab.skin.SkinController
import com.illuzionzstudios.tab.tab.TabController
import com.illuzionzstudios.tab.tab.components.Tab
import com.illuzionzstudios.tab.tab.components.column.TabColumn
import com.illuzionzstudios.tab.tab.components.item.TabItem
import com.illuzionzstudios.tab.tab.components.item.TextTabItem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

class CustomTab: SpigotPlugin(7282) {

    override val pluginColor: ChatColor = ChatColor.AQUA
    override val pluginId: Int = 78200
    override val pluginLocale: PluginLocale
        get() = Locale(this)
    override val pluginSettings: PluginSettings
        get() = Settings(this)

    /**
     * If placeholder API is enabled
     */
    var papiEnabled: Boolean = false

    override fun onPluginDisable() {

    }

    override fun onPluginEnable() {
        // Load plugin hooks
        papiEnabled = Bukkit.getServer().pluginManager.getPlugin("PlaceholderAPI") != null

        if (papiEnabled)
            Logger.info("Hooked into PlaceholderAPI")

        // Set API
        api = CustomTabImpl()

//        val tab = Tab("test")
//
//        tab.setColumn(1, object : TabColumn("one") {
//            override fun render(slot: Int, player: Player?, displayTitles: Boolean): MutableList<TabItem> {
//                val toReturn: MutableList<TabItem> = ArrayList()
//                val string = "Test Location: " + player?.location?.blockX
//                toReturn.add(TextTabItem(20, "&c$string", "&d$string"))
//                return toReturn
//            }
//        })
//
//        (api as CustomTabImpl).registerTab(tab)
    }

    override fun onPluginLoad() {

    }

    override fun onPluginPreEnable() {

    }

    override fun onPluginPreReload() {

    }

    override fun onPluginReload() {

    }

    override fun onRegisterReloadables() {
        registerMainCommand(TabCommand(), "tab", "customtab")
        reloadables.registerController(GroupController)
        reloadables.registerController(SkinController)
        reloadables.registerController(TabController)
    }

    companion object {
        /**
         * Return our instance of the [SpigotPlugin]
         *
         * Should be overridden in your own [SpigotPlugin] class
         * as a way to implement your own methods per plugin
         *
         * @return This instance of the plugin
         */
        var instance: CustomTab? = null
            get() {
                // Assign if null
                if (field == null) {
                    field = getPlugin(CustomTab::class.java)
                    Objects.requireNonNull(field, "Cannot create instance of plugin. Did you reload?")
                }
                return field
            }

        @JvmStatic
        var api: CustomTabAPI? = null
    }
}
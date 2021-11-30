package com.illuzionzstudios.tab.tab.instance

import com.comphenix.protocol.wrappers.*
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import com.illuzionzstudios.mist.scheduler.timer.Cooldown
import com.illuzionzstudios.tab.CustomTab
import com.illuzionzstudios.tab.model.DynamicText
import com.illuzionzstudios.tab.skin.CachedSkin
import com.illuzionzstudios.tab.skin.SkinController
import com.illuzionzstudios.tab.tab.Ping
import com.illuzionzstudios.tab.tab.TabController
import com.illuzionzstudios.tab.tab.components.Tab
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * An instance of a tab rendering for a player
 */
class TabInstance(
    val player: Player,
    val tab: Tab
) {

    /**
     * Initial list of player slots. Dummy slots before data is filled in
     */
    val initialList: MutableList<PlayerInfoData> = ArrayList()

    /**
     * Column instances
     */
    val columns: MutableMap<Int, TabColumnInstance> = HashMap()

    /**
     * Cached icon skins
     */
    var avatarCache: Table<Int, Int, CachedSkin> = HashBasedTable.create()

    /**
     * A cooldown to refresh the tab after a bit
     */
    private var refresh: Cooldown? = null

    init {
        // Add default player slots
        for (x in 1..tab.columns.size) {
            for (y in 1..tab.columns[1]?.pageElements!!) {
                val profile: WrappedGameProfile = TabController.getDisplayProfile(x, y)

                // Set default skin for inital list
                profile.properties.removeAll("textures")
                profile.properties.put(
                    "textures",
                    WrappedSignedProperty(
                        "textures",
                        SkinController.UNKNOWN_SKIN.value,
                        SkinController.UNKNOWN_SKIN.signature
                    )
                )

                initialList.add(
                    PlayerInfoData(
                        profile,
                        Ping.FIVE.ping,
                        EnumWrappers.NativeGameMode.SURVIVAL,
                        WrappedChatComponent.fromText("")
                    )
                )
            }
        }

        for (column in tab.columns) {
            this.columns[column.key] = TabColumnInstance(player, this, column.value)
        }

        refresh = Cooldown(20)
    }

    /**
     * Render this tab for a player
     */
    fun render() {
        // Build the header/footer text
        val headerText = StringBuilder()
        val footerText = StringBuilder()

        // Update text
        tab.header.forEach { head: DynamicText ->
            // PAPI
            if (CustomTab.instance!!.papiEnabled) headerText.append(
                PlaceholderAPI.setPlaceholders(
                    player,
                    head.getVisibleText()
                )
            ) else headerText.append(head.getVisibleText())

            // Last element check
            if (tab.header[tab.header.size - 1] != head) {
                headerText.append("\n")
            }

            // Change for next render
            head.changeText()
        }

        tab.footer.forEach { foot: DynamicText ->
            // PAPI
            if (CustomTab.instance!!.papiEnabled) footerText.append(
                PlaceholderAPI.setPlaceholders(
                    player,
                    foot.getVisibleText()
                )
            ) else footerText.append(foot.getVisibleText())

            // Last element check
            if (tab.footer[tab.footer.size - 1] != foot) {
                footerText.append("\n")
            }

            // Change for next render
            foot.changeText()
        }

        // Remove actual player on tab after delay so skins don't bug out :(
        // This is because usually we should have 4 columns with 20 elements, if
        // we want less we have to cheat
        val pageElements: Int = tab.columns[1]?.pageElements ?: 0
        val tryAddSkins: Boolean = tab.columns.size == 4 && pageElements >= 20
        if (this.refresh != null && this.refresh!!.isReady && !tryAddSkins) {
            // Add skins for players
            for (player in Bukkit.getOnlinePlayers()) {
                // Make sure player exists
                if (player == null) continue
                SkinController.addSkin(player, this.player)
                if (this.player != player) {
                    SkinController.addSkin(this.player, player)
                }
            }

            this.refresh = null
        }

        // Render columns
        columns.forEach { (slot, column) ->
            column.render(slot, tab.displayTitles, tab.elementWidth)
        }

        // Set the header and footer
        TabController.setHeaderFooter(headerText.toString(), footerText.toString(), player)
    }

}
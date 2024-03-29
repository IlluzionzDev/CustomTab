package com.illuzionzstudios.tab.tab.components.loader

import com.illuzionzstudios.mist.Logger
import com.illuzionzstudios.mist.config.YamlConfig
import com.illuzionzstudios.mist.config.serialization.loader.YamlFileLoader
import com.illuzionzstudios.mist.scheduler.timer.PresetCooldown
import com.illuzionzstudios.tab.tab.components.column.SimpleColumn
import com.illuzionzstudios.tab.tab.components.column.TabColumn
import com.illuzionzstudios.tab.tab.components.item.BlankTabItem
import com.illuzionzstudios.tab.tab.components.item.TabItem

/**
 * Load a tab column
 */
class TabColumnLoader(directory: String, fileName: String) : YamlFileLoader<TabColumn>(directory, fileName) {

    override fun loadYamlObject(file: YamlConfig?): TabColumn {
        val column = SimpleColumn(file?.getString("name") ?: "default")

        column.pageEnabled = file?.getBoolean("page.enabled") ?: true
        column.maxPages = file?.getInt("page.max", 5) ?: 5
        column.pageItem = TabItemLoader(file?.getConfigurationSection("page.text")).`object` ?: BlankTabItem()

        column.pageElements = file?.getInt("page.elements") ?: 20
        column.pageInterval = file?.getInt("page.interval") ?: 100

        column.title = TabItemLoader(file?.getConfigurationSection("title")!!).`object`

        val elements: MutableList<TabItem> = ArrayList()
        file.getSections("text").forEach {
            elements.add(TabItemLoader(it!!).`object`)
        }
        column.elementstoRender = elements

        return column
    }

    override fun saveYaml() {
        TODO("Not yet implemented")
    }

}
package com.illuzionzstudios.tab.components.column;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.illuzionzstudios.core.locale.player.Message;
import com.illuzionzstudios.core.util.Logger;
import com.illuzionzstudios.scheduler.MinecraftScheduler;
import com.illuzionzstudios.scheduler.sync.Async;
import com.illuzionzstudios.scheduler.sync.Rate;
import com.illuzionzstudios.scheduler.util.PresetCooldown;
import com.illuzionzstudios.tab.CustomTab;
import com.illuzionzstudios.tab.components.column.list.type.OnlineList;
import com.illuzionzstudios.tab.controller.TabController;
import com.illuzionzstudios.tab.settings.Settings;
import com.illuzionzstudios.tab.components.text.DynamicText;
import com.illuzionzstudios.tab.components.text.FrameText;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

/**
 * Represents a column of the tab
 */
public abstract class TabColumn implements Listener {

    /**
     * List of registered tabs. Sorted by tab column name
     */
    public static Map<String, Class<? extends TabColumn>> registered = new HashMap<>();

    /**
     * The number of the tab display column
     */
    @Getter
    protected final int columnNumber;

    /**
     * Delay between updating tab elements
     */
    private final PresetCooldown elementCooldown;

    /**
     * Delay between scrolling sub pages
     */
    private final PresetCooldown pageScrollCooldown;

    /**
     * The player the tab is shown to
     */
    protected Player player;

    /**
     * Cached icon skins
     */
    protected Table<Integer, Integer, UUID> avatarCache = HashBasedTable.create();

    /**
     * Cursor between pages
     */
    private int cursor = 0;

    /**
     * Text elements on the column
     */
    private List<DynamicText> elements;

    public TabColumn(Player player, int columnNumber) {
        this.player = player;
        this.columnNumber = columnNumber;

        // Set refresh cooldowns
        elementCooldown = new PresetCooldown(Settings.TAB_REFRESH.getInt());
        pageScrollCooldown = new PresetCooldown(Settings.PAGE_SCROLL_COOLDOWN.getInt());

        Bukkit.getServer().getPluginManager().registerEvents(this, CustomTab.getInstance());
        MinecraftScheduler.get().registerSynchronizationService(this);

        // Start timers
        elementCooldown.go();
        pageScrollCooldown.go();
    }

    /**
     * @param column Register a tab column
     */
    public static void register(String name, Class<? extends TabColumn> column) {
        registered.put(name, column);
    }

    /**
     * Called when column is destroyed or no longer being displayed
     */
    public void disable() {
        HandlerList.unregisterAll(this);
        MinecraftScheduler.get().dismissSynchronizationService(this);
    }

    /**
     * Refreshes the column of social
     */
    protected abstract void render(List<DynamicText> elements);

    /**
     * @return Returns the title of column
     */
    public abstract DynamicText getTitle();

    /*
     * Render all text elements in tab
     */
    @Async(rate = Rate.TICK)
    public void render() {
        // Don't render if timer not ready
        if (player == null || !elementCooldown.isReady()) {
            return;
        }
        elementCooldown.reset();
        elementCooldown.go();

        TabController API = TabController.INSTANCE;

        // Check if to refresh
        List<DynamicText> check = new ArrayList<>();
        render(check);

        // If not the same, re render
        if (elements == null || elements.isEmpty()) {
            elements = check;
        } else {
            // Refresh
            if (check.size() != elements.size()) elements = check;
        }

        // Our sub array, or our page
        List<DynamicText> sub = new ArrayList<>
                (elements.subList(Math.max(0, Math.min(cursor, elements.size())),
                        Math.min(elements.size(), cursor + (Settings.PAGE_ELEMENTS.getInt() - 3))));

        // If titles enabled
        if (Settings.TAB_TITLES.getBoolean()) {
            sub.add(0, getTitle());

            // Set our minimum tab length
            StringBuilder width = new StringBuilder();
            for (int i = 0; i < Settings.TAB_WIDTH.getInt(); i++) {
                width.append(" ");
            }
            sub.add(1, new FrameText(-1, width.toString()));
        }

        double size = (elements.size() + 2 + Math.floor((elements.size() / Settings.PAGE_ELEMENTS.getInt())));

        // If to show pagination info
        boolean pageInfo = false;

        if (size >= Settings.PAGE_ELEMENTS.getInt() - 1) {
            // Calculate page length //
            double pageDelta = ((double) (cursor + (Settings.TAB_TITLES.getBoolean() ? 3 : 1)) / Settings.PAGE_ELEMENTS.getInt()) + 1;
            int page = (int) (pageDelta < 2 ? Math.floor(pageDelta) : Math.ceil(pageDelta));
            int max = (int) Math.ceil((size + (2 * elements.size() / Settings.PAGE_ELEMENTS.getInt())) / Settings.PAGE_ELEMENTS.getInt());

            // If we can go to next page
            if (pageScrollCooldown.isReady()) {
                // Don't update if on a null page
                if (page > max) {
                    // Reset to page 1
                    this.elements = null;
                    cursor = 0;
                    return;
                }

                // Go to next page
                this.elements = check;
                pageInfo = true;

                pageScrollCooldown.reset();
                pageScrollCooldown.go();
            }

            // Pagination text
            String pagesText = new Message(Settings.TAB_PAGE_TEXT.getString())
                    .processPlaceholder("current_page", Math.max(1, page))
                    .processPlaceholder("max_page", Math.max(1, max)).getMessage();
            sub.add(new FrameText(-1, pagesText));
        }

        // For elements in the sub tab
        for (int i = 1; i <= Settings.PAGE_ELEMENTS.getInt(); i++) {
            boolean blank = (i - 1) >= sub.size();

            // Send update packet //
            String text = ChatColor.translateAlternateColorCodes('&', blank ? "" : sub.get(i - 1).getVisibleText());

            // Set placeholders
            if (CustomTab.isPapiEnabled()) {
                // Check here for players because literally gay
                if (this instanceof OnlineList) {
                    // If in range
                    if (i >= ((OnlineList) this).getPlayers().size() && !blank && i > (Settings.TAB_TITLES.getBoolean() ? 2 : 0)) {
                        OnlineList.TabPlayer tabPlayer = ((OnlineList) this).getPlayers().get(i - (Settings.TAB_TITLES.getBoolean() ? 3 : 1));

                        if (tabPlayer.getGroup() != null) {
                            // Group formatting
                            text = new Message(text).processPlaceholder("group_format",
                                    tabPlayer.getGroup().getElementText().getVisibleText()).getMessage();
                        }

                        text = PlaceholderAPI.setPlaceholders(tabPlayer.getTabPlayer(), text);
                    }
                }

                // Try render anything else.
                // By this point it will be formatted by player
                text = PlaceholderAPI.setPlaceholders(player, text);
            }

            // Trim text
            if (text.length() > Settings.TAB_WIDTH.getInt()) {
                // Check for colour code
                boolean previousCode = false;
                // If the text is currently bold
                boolean isBold = false;
                // Total number of bold characters
                int boldChars = 0;

                char[] chars = text.toCharArray();
                for (int j = 0; j < Settings.TAB_WIDTH.getInt(); j++) {
                    char c = chars[j];

                    if (c == '§') {
                        previousCode = true;
                    } else if (previousCode) {
                        previousCode = false;
                        isBold = c == 'l' || c == 'L';
                    } else {
                        boldChars += isBold ? 1 : 0;
                    }
                }

                text = text.substring(0, (Settings.TAB_WIDTH.getInt() - (boldChars / 8)) - 4);
            }

            // Get player to see if to display player skin icon
            Player tabPlayer = null;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                // Simply check for name
                if (text.contains(onlinePlayer.getName())) tabPlayer = onlinePlayer;
            }

            // Check all elements with text
            if ((i - 1) < sub.size()) {
                if (tabPlayer != null && !tabPlayer.getName().trim().equalsIgnoreCase("")) {
                    // Set the avatar for that slot
                    if ((!avatarCache.contains(columnNumber, i) || !avatarCache.get(columnNumber, i).equals(tabPlayer.getUniqueId())) && !blank) {
                        API.setAvatar(columnNumber, i, tabPlayer, this.player);
                        avatarCache.put(columnNumber, i, tabPlayer.getUniqueId());
                    }
                }

                // Set text in that slot as our final text
                API.setText(columnNumber, i, text, this.player);
            } else {
                // Otherwise text not defined so set blank
                API.setText(columnNumber, i, "", this.player);

                // Make sure avatar is blank
                if (avatarCache.contains(columnNumber, i)) {
                    avatarCache.remove(columnNumber, i);
                    API.hideAvatar(columnNumber, i, this.player);
                }
            }

            // Go to next page if applicable
            if (pageInfo)
                cursor++;
        }

        // Update text and title
        getTitle().changeText();
        elements.forEach(DynamicText::changeText);

        // If page display at bottom
        if (pageInfo) {
            cursor -= (Settings.TAB_TITLES.getBoolean() ? 3 : 1);
        }

        // Check if cursor is greater than applicable
        // number of pages
        if (cursor >= (size - ((Settings.TAB_TITLES.getBoolean() ? 3 : 1) * elements.size() / (Settings.PAGE_ELEMENTS.getInt())))) {
            // Reset to page 1
            this.elements = null;
            cursor = 0;
        }

    }


}
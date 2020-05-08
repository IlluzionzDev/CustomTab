/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.tab.listener;

import com.illuzionzstudios.tab.components.Tab;
import com.illuzionzstudios.tab.components.column.*;
import com.illuzionzstudios.tab.components.column.list.type.OnlineList;
import com.illuzionzstudios.tab.components.loader.ListLoader;
import com.illuzionzstudios.tab.controller.TabController;
import com.illuzionzstudios.tab.components.loader.ColumnLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;

/**
 * Simply used to register tabs on join
 */
public class TabRegisterListener implements Listener {

    public TabRegisterListener(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        TabController.INSTANCE.showTab(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        // Handle unregistering tabs
        TabController.INSTANCE.clearTabs(event.getPlayer());
    }

}
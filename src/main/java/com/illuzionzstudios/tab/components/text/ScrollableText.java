/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.tab.components.text;

import com.illuzionzstudios.scheduler.util.PresetCooldown;
import com.illuzionzstudios.tab.settings.Settings;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Text that scrolls along
 */
public class ScrollableText implements DynamicText {

    private String fullText;
    private int scrollPos;
    private int scrollSize;
    private int scrollPad;

    private Character format;
    private Character color;

    @Getter
    private PresetCooldown interval;

    public ScrollableText(int interval, String fullText) {
        this(interval, fullText, Settings.TAB_WIDTH.getInt() / 3);
    }

    public ScrollableText(int interval, String fullText, int scrollSize) {
        // we must use the alternate codes
        this.fullText = ChatColor.translateAlternateColorCodes('&', fullText);
        this.scrollSize = scrollSize;
        scrollPos = 0;
        scrollPad = 0;
        this.interval = new PresetCooldown(interval);
    }

    public int getScrollPos() {
        return scrollPos;
    }

    public String getVisibleText() {
        StringBuilder builder = new StringBuilder();
        if (scrollPos < 0) {
            for (int x = scrollPos; x < 0; x++) {
                builder.append(" ");
            }
            builder.append(fullText, 0, scrollSize - builder.length());
        } else {
            builder.append(getFormatChars());
            builder.append(fullText.substring(scrollPos));
            if (builder.length() > scrollSize) {
                builder.delete(scrollSize, builder.length());
            } else {
                for (int x = builder.length(); x < scrollSize; x++) {
                    builder.append(" ");
                }
            }
        }
        // remove chat color char if alone
        if (builder.length() > 0 && ChatColor.COLOR_CHAR == builder.charAt(builder.length() - 1)) {
            builder.replace(builder.length() - 1, builder.length(), " ");
        }

        return builder.toString();
    }

    public void scrollText() {
        // don't scroll small text
        if (fullText.length() <= scrollSize) {
            return;
        }
        // color/format at the end of the string only makes
        // sense before the last three characters
        // (two format chars + one formatted char)
        if (scrollPos >= 0 && scrollPos + 2 < fullText.length()) {
            if (ChatColor.COLOR_CHAR == fullText.charAt(scrollPos)) {
                scrollChatColor();
            } else {
                if (scrollPad > 0) {
                    scrollPad--;
                } else {
                    scrollPos++;
                }
            }
        } else if (scrollPos < fullText.length()) {
            if (scrollPos >= 0 && ChatColor.COLOR_CHAR == fullText.charAt(scrollPos)) {
                scrollPos++;
                scrollText();
            } else {
                if (scrollPad > 0) {
                    scrollPad--;
                } else {
                    scrollPos++;
                }
            }
        } else {
            scrollPos = 1 - scrollSize;
            color = null;
            format = null;
        }
    }

    private void scrollChatColor() {
        scrollPos++;
        Character code = fullText.charAt(scrollPos);
        if (ChatColor.RESET.getChar() == code.charValue()) {
            scrollPad = 0;
            if (null != color) {
                scrollPad += 2;
            }
            color = null;
            if (null != format) {
                scrollPad += 2;
            }
            format = null;
            scrollPos++;
        } else if (ChatColor.getByChar(code).isFormat()) {
            format = code;
            scrollPos++;
        } else if (ChatColor.getByChar(code).isColor()) {
            color = code;
            scrollPos++;
        } else {
            Bukkit.getLogger().warning(String.format("Invalid ChatColor code on string: [%s]", code));
        }

        if (ChatColor.COLOR_CHAR == fullText.charAt(scrollPos)) {
            scrollChatColor();
        } else {
            if (scrollPad > 0) {
                scrollPad--;
            } else {
                scrollPos++;
            }
        }
    }

    private String getFormatChars() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < scrollPad; i++) {
            builder.append(" ");
        }
        // Bukkit.getLogger().warning(String.format("Span chars: [%s]",
        // builder.toString()));
        if (null != color) {
            builder.append(ChatColor.COLOR_CHAR).append(color);
        }
        if (null != format) {
            builder.append(ChatColor.COLOR_CHAR).append(format);
        }
        return builder.toString();
    }

    @Override
    public String changeText() {
        scrollText();
        return getVisibleText();
    }

    @Override
    public String getOriginalText() {
        return fullText;
    }

}

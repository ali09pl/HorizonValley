package com.horizonvalley.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.horizonvalley.Config;
import com.horizonvalley.entity.Player;

public class HUD {
    public void render(Canvas c, Player player, Paint p, int screenW) {
        // Top bar
        p.setColor(0xDD14141E);
        c.drawRect(0, 0, screenW, 52, p);

        // Health
        p.setColor(0xFF333333);
        c.drawRect(12, 10, 132, 24, p);
        float hw = 116f * player.health / player.maxHealth;
        p.setColor(Config.COLOR_HEALTH);
        c.drawRect(14, 12, 14 + hw, 22, p);
        p.setColor(Config.COLOR_WHITE);
        p.setTextSize(16);
        p.setTextAlign(Paint.Align.LEFT);
        c.drawText("HP " + player.health + "/" + player.maxHealth, 140, 22, p);

        // Energy
        p.setColor(0xFF333333);
        c.drawRect(12, 28, 132, 42, p);
        float ew = 116f * player.energy / player.maxEnergy;
        p.setColor(Config.COLOR_ENERGY);
        c.drawRect(14, 30, 14 + ew, 40, p);
        c.drawText("EN " + player.energy + "/" + player.maxEnergy, 140, 40, p);

        // Money
        p.setColor(Config.COLOR_GOLD);
        p.setTextSize(22);
        c.drawText("$ " + player.money, 280, 32, p);

        // Time
        p.setColor(Config.COLOR_WHITE);
        p.setTextAlign(Paint.Align.RIGHT);
        c.drawText("Year 1  Spring 1  6:00 AM", screenW - 16, 32, p);

        // Hotbar
        float slot = 44;
        float startX = (screenW - Config.HOTBAR_SLOTS * slot) / 2f;
        float y = c.getHeight() - 56;
        for (int i = 0; i < Config.HOTBAR_SLOTS; i++) {
            float x = startX + i * slot;
            boolean sel = i == player.selectedHotbar;
            p.setColor(sel ? 0xFF4060A0 : 0xFF1E1E28);
            c.drawRect(x, y, x + slot - 4, y + slot - 4, p);
            p.setColor(sel ? 0xFF80A0FF : Config.COLOR_UI_BORDER);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(sel ? 3 : 2);
            c.drawRect(x, y, x + slot - 4, y + slot - 4, p);
            p.setStyle(Paint.Style.FILL);

            String item = player.inventory.getHotbarItem(i);
            if (item != null) {
                p.setColor(0xFF7090D0);
                c.drawRect(x + 6, y + 6, x + slot - 10, y + slot - 10, p);
                p.setColor(Config.COLOR_WHITE);
                p.setTextSize(12);
                p.setTextAlign(Paint.Align.CENTER);
                String shortName = item.length() > 5 ? item.substring(0, 5) : item;
                c.drawText(shortName, x + slot / 2 - 2, y + slot / 2 + 4, p);
            }
            p.setColor(0xFF888888);
            p.setTextSize(12);
            p.setTextAlign(Paint.Align.LEFT);
            c.drawText(String.valueOf((i + 1) % 10), x + 3, y + 14, p);
        }
    }
}

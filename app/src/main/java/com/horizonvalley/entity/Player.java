package com.horizonvalley.entity;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.horizonvalley.Config;
import com.horizonvalley.data.Inventory;
import com.horizonvalley.world.World;

public class Player {
    public float x, y;
    public int health = Config.PLAYER_MAX_HEALTH;
    public int maxHealth = Config.PLAYER_MAX_HEALTH;
    public int energy = Config.PLAYER_MAX_ENERGY;
    public int maxEnergy = Config.PLAYER_MAX_ENERGY;
    public int money = Config.PLAYER_START_MONEY;
    public int selectedHotbar = 0;
    public Inventory inventory = new Inventory();
    public int direction = 0; // 0 down, 1 left, 2 right, 3 up
    private float animTimer = 0;
    private int animFrame = 0;
    private boolean moving = false;

    public Player() {
        inventory.addItem("hoe", 1);
        inventory.addItem("watering_can", 1);
        inventory.addItem("axe", 1);
        inventory.addItem("pickaxe", 1);
        inventory.addItem("scythe", 1);
        inventory.addItem("seed_parsnip", 20);
    }

    public void move(float dx, float dy, float dt, boolean running, World world) {
        if (dx == 0 && dy == 0) {
            moving = false;
            return;
        }
        moving = true;
        if (Math.abs(dx) > Math.abs(dy)) {
            direction = dx > 0 ? 2 : 1;
        } else {
            direction = dy > 0 ? 0 : 3;
        }

        float speed = Config.PLAYER_SPEED * (running ? Config.PLAYER_RUN_MULT : 1f);
        float newX = x + dx * speed * dt;
        float newY = y + dy * speed * dt;

        // Simple collision
        if (!world.collides(newX + 6, y + 8, 20, 22)) x = newX;
        if (!world.collides(x + 6, newY + 8, 20, 22)) y = newY;

        animTimer += dt;
        if (animTimer > 0.15f) {
            animTimer = 0;
            animFrame = (animFrame + 1) % 4;
        }
        if (running) energy = Math.max(0, energy - (int)(dt * 3));
    }

    public void useTool(float worldX, float worldY, World world) {
        int tx = (int)(worldX / Config.TILE_SIZE);
        int ty = (int)(worldY / Config.TILE_SIZE);
        int ptx = (int)(x / Config.TILE_SIZE);
        int pty = (int)(y / Config.TILE_SIZE);
        if (Math.abs(tx - ptx) > 2 || Math.abs(ty - pty) > 2) return;

        String tool = inventory.getHotbarItem(selectedHotbar);
        if (tool == null) return;

        if (tool.equals("hoe") && energy >= 2) {
            if (world.hoe(tx, ty)) energy -= 2;
        } else if (tool.equals("watering_can") && energy >= 2) {
            if (world.water(tx, ty)) energy -= 2;
        } else if (tool.startsWith("seed_") && energy >= 1) {
            String crop = tool.substring(5);
            if (world.plant(tx, ty, crop)) {
                energy -= 1;
                inventory.removeHotbar(selectedHotbar, 1);
            }
        } else if (tool.equals("axe") && energy >= 3) {
            if (world.chop(tx, ty)) {
                energy -= 3;
                inventory.addItem("wood", 3);
            }
        } else if (tool.equals("pickaxe") && energy >= 3) {
            String ore = world.mine(tx, ty);
            if (ore != null) {
                energy -= 3;
                inventory.addItem(ore, 1);
            }
        } else if (tool.equals("scythe")) {
            String crop = world.harvest(tx, ty);
            if (crop != null) {
                inventory.addItem(crop, 1);
                energy = Math.max(0, energy - 1);
            }
        }
    }

    public void render(Canvas c, float camX, float camY, Paint p) {
        float sx = x - camX;
        float sy = y - camY;
        // Body
        p.setColor(Config.COLOR_PLAYER);
        c.drawRect(sx + 8, sy + 12, sx + 24, sy + 28, p);
        // Head
        p.setColor(0xFFF0C8A0);
        c.drawRect(sx + 10, sy + 4, sx + 22, sy + 14, p);
        // Hair
        p.setColor(0xFF3C2814);
        c.drawRect(sx + 10, sy + 2, sx + 22, sy + 6, p);
        // Legs
        p.setColor(0xFF282850);
        int off = moving ? ((animFrame % 2) * 2 - 1) : 0;
        c.drawRect(sx + 10 + off, sy + 26, sx + 15 + off, sy + 32, p);
        c.drawRect(sx + 17 - off, sy + 26, sx + 22 - off, sy + 32, p);
    }
}

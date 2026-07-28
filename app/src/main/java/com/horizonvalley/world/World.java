package com.horizonvalley.world;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.horizonvalley.Config;
import com.horizonvalley.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {
    public static final int GRASS = 0, DIRT = 1, TILLED = 2, WATERED = 3, WATER = 4;
    public static final int PATH = 5, WOOD = 6, WALL = 7;

    private final int w = Config.WORLD_WIDTH;
    private final int h = Config.WORLD_HEIGHT;
    private final int[][] tiles = new int[h][w];
    private final boolean[][] trees = new boolean[h][w];
    private final boolean[][] rocks = new boolean[h][w];
    private final String[][] crops = new String[h][w];
    private final int[][] cropStage = new int[h][w];
    private final boolean[][] cropWatered = new boolean[h][w];
    private final List<NPC> npcs = new ArrayList<>();
    private Player player;
    private final Random rng = new Random(42);

    public World() {
        generate();
        spawnNpcs();
    }

    public void setPlayer(Player p) { this.player = p; }

    private void generate() {
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                tiles[y][x] = GRASS;

        // Farm area
        for (int y = 10; y < 25; y++)
            for (int x = 10; x < 30; x++)
                if (rng.nextFloat() < 0.08f) tiles[y][x] = DIRT;

        // Pond
        for (int y = 30; y < 40; y++)
            for (int x = 40; x < 55; x++) {
                float dist = (float) Math.hypot(x - 47, y - 35);
                if (dist < 6) tiles[y][x] = WATER;
            }

        // Forest
        for (int y = 5; y < 55; y++)
            for (int x = 60; x < 78; x++)
                if (rng.nextFloat() < 0.14f) trees[y][x] = true;

        // Rocks
        for (int y = 45; y < 58; y++)
            for (int x = 5; x < 25; x++)
                if (rng.nextFloat() < 0.18f) rocks[y][x] = true;

        // Paths
        for (int x = 5; x < 75; x++) tiles[28][x] = PATH;
        for (int y = 5; y < 55; y++) tiles[y][35] = PATH;

        // House
        for (int y = 8; y < 14; y++)
            for (int x = 32; x < 40; x++)
                tiles[y][x] = WOOD;
        for (int x = 31; x < 41; x++) {
            tiles[7][x] = WALL;
            tiles[14][x] = WALL;
        }
        for (int y = 7; y < 15; y++) {
            tiles[y][31] = WALL;
            tiles[y][40] = WALL;
        }
        tiles[14][35] = WOOD;
        tiles[14][36] = WOOD;
    }

    private void spawnNpcs() {
        String[] names = {"Harold","Mira","Garrick","Tom","Elena","Willow","Sage","Reed",
                "Luna","Marco","Brynn","Cedric","Daisy","Finn","Greta","Hector"};
        int[][] homes = {{20,20},{50,20},{55,45},{47,38},{8,15},{25,8},{22,25},{60,30},
                {18,40},{42,22},{10,30},{15,50},{25,45},{30,10},{40,50},{45,15}};
        for (int i = 0; i < names.length; i++) {
            NPC n = new NPC(names[i], homes[i][0] * Config.TILE_SIZE, homes[i][1] * Config.TILE_SIZE);
            n.color = 0xFF000000 | (0x50 + i * 12) << 16 | (0x80 + (i % 5) * 20) << 8 | (0x60 + i * 8);
            npcs.add(n);
        }
    }

    public int getPixelWidth() { return w * Config.TILE_SIZE; }
    public int getPixelHeight() { return h * Config.TILE_SIZE; }

    public boolean collides(float x, float y, float ww, float hh) {
        int x1 = Math.max(0, (int)(x / Config.TILE_SIZE));
        int y1 = Math.max(0, (int)(y / Config.TILE_SIZE));
        int x2 = Math.min(w - 1, (int)((x + ww) / Config.TILE_SIZE));
        int y2 = Math.min(h - 1, (int)((y + hh) / Config.TILE_SIZE));
        for (int ty = y1; ty <= y2; ty++)
            for (int tx = x1; tx <= x2; tx++) {
                int t = tiles[ty][tx];
                if (t == WATER || t == WALL) return true;
                if (trees[ty][tx] || rocks[ty][tx]) return true;
            }
        return false;
    }

    public boolean hoe(int tx, int ty) {
        if (!inBounds(tx, ty)) return false;
        if ((tiles[ty][tx] == GRASS || tiles[ty][tx] == DIRT) && !trees[ty][tx] && !rocks[ty][tx]) {
            tiles[ty][tx] = TILLED;
            return true;
        }
        return false;
    }

    public boolean water(int tx, int ty) {
        if (!inBounds(tx, ty)) return false;
        if (tiles[ty][tx] == TILLED || tiles[ty][tx] == WATERED) {
            tiles[ty][tx] = WATERED;
            if (crops[ty][tx] != null) cropWatered[ty][tx] = true;
            return true;
        }
        return false;
    }

    public boolean plant(int tx, int ty, String crop) {
        if (!inBounds(tx, ty)) return false;
        if ((tiles[ty][tx] == TILLED || tiles[ty][tx] == WATERED) && crops[ty][tx] == null) {
            crops[ty][tx] = crop;
            cropStage[ty][tx] = 0;
            cropWatered[ty][tx] = tiles[ty][tx] == WATERED;
            return true;
        }
        return false;
    }

    public String harvest(int tx, int ty) {
        if (!inBounds(tx, ty) || crops[ty][tx] == null) return null;
        if (cropStage[ty][tx] >= 4) {
            String c = crops[ty][tx];
            crops[ty][tx] = null;
            cropStage[ty][tx] = 0;
            tiles[ty][tx] = TILLED;
            return c;
        }
        return null;
    }

    public boolean chop(int tx, int ty) {
        if (!inBounds(tx, ty) || !trees[ty][tx]) return false;
        trees[ty][tx] = false;
        return true;
    }

    public String mine(int tx, int ty) {
        if (!inBounds(tx, ty) || !rocks[ty][tx]) return null;
        rocks[ty][tx] = false;
        return rng.nextFloat() < 0.3f ? "copper_ore" : "stone";
    }

    public void tryInteract(Player p) {
        for (NPC n : npcs) {
            float dist = (float) Math.hypot(n.x - p.x, n.y - p.y);
            if (dist < 48) {
                // Simple friendship bump
                n.friendship = Math.min(1000, n.friendship + 10);
                break;
            }
        }
    }

    public void update(float dt) {
        for (NPC n : npcs) n.update(dt);
        // Simple crop growth over time
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (crops[y][x] != null && cropWatered[y][x] && cropStage[y][x] < 4) {
                    // Grow slowly
                    if (rng.nextFloat() < 0.002f) cropStage[y][x]++;
                }
    }

    public void render(Canvas c, float camX, float camY, int viewW, int viewH, Paint p) {
        int startTX = Math.max(0, (int)(camX / Config.TILE_SIZE) - 1);
        int startTY = Math.max(0, (int)(camY / Config.TILE_SIZE) - 1);
        int endTX = Math.min(w, (int)((camX + viewW) / Config.TILE_SIZE) + 2);
        int endTY = Math.min(h, (int)((camY + viewH) / Config.TILE_SIZE) + 2);

        for (int ty = startTY; ty < endTY; ty++) {
            for (int tx = startTX; tx < endTX; tx++) {
                float sx = tx * Config.TILE_SIZE - camX;
                float sy = ty * Config.TILE_SIZE - camY;
                int color;
                switch (tiles[ty][tx]) {
                    case DIRT: color = Config.COLOR_DIRT; break;
                    case TILLED: color = Config.COLOR_TILLED; break;
                    case WATERED: color = Config.COLOR_WATERED; break;
                    case WATER: color = Config.COLOR_WATER; break;
                    case PATH: color = Config.COLOR_PATH; break;
                    case WOOD: color = Config.COLOR_WOOD; break;
                    case WALL: color = Config.COLOR_WALL; break;
                    default: color = Config.COLOR_GRASS;
                }
                p.setColor(color);
                c.drawRect(sx, sy, sx + Config.TILE_SIZE, sy + Config.TILE_SIZE, p);

                if (trees[ty][tx]) {
                    p.setColor(0xFF644628);
                    c.drawRect(sx + 12, sy + 16, sx + 20, sy + 32, p);
                    p.setColor(Config.COLOR_TREE);
                    c.drawRect(sx + 4, sy + 2, sx + 28, sy + 20, p);
                }
                if (rocks[ty][tx]) {
                    p.setColor(Config.COLOR_ROCK);
                    c.drawRect(sx + 6, sy + 10, sx + 26, sy + 28, p);
                }
                if (crops[ty][tx] != null) {
                    int stage = cropStage[ty][tx];
                    int ch = 6 + stage * 5;
                    p.setColor(stage >= 4 ? 0xFFF0C832 : 0xFF3CA028);
                    c.drawRect(sx + 12, sy + Config.TILE_SIZE - ch - 4, sx + 20, sy + Config.TILE_SIZE - 4, p);
                }
            }
        }

        for (NPC n : npcs) n.render(c, camX, camY, p);
    }

    private boolean inBounds(int tx, int ty) {
        return tx >= 0 && ty >= 0 && tx < w && ty < h;
    }

    // Simple NPC inner class
    public static class NPC {
        public String name;
        public float x, y;
        public int friendship;
        public int color = Config.COLOR_NPC;
        private float tx, ty;
        private float timer;

        public NPC(String name, float x, float y) {
            this.name = name;
            this.x = x; this.y = y;
            this.tx = x; this.ty = y;
        }

        public void update(float dt) {
            timer += dt;
            if (timer > 3f) {
                timer = 0;
                tx = x + (float)((Math.random() - 0.5) * 64);
                ty = y + (float)((Math.random() - 0.5) * 64);
            }
            float dx = tx - x, dy = ty - y;
            float dist = (float) Math.hypot(dx, dy);
            if (dist > 2) {
                x += dx / dist * 40 * dt;
                y += dy / dist * 40 * dt;
            }
        }

        public void render(Canvas c, float camX, float camY, Paint p) {
            float sx = x - camX, sy = y - camY;
            p.setColor(color);
            c.drawRect(sx + 8, sy + 12, sx + 24, sy + 28, p);
            p.setColor(0xFFF0C8A0);
            c.drawRect(sx + 10, sy + 4, sx + 22, sy + 14, p);
        }
    }
}

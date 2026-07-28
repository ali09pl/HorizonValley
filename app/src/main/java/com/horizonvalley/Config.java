package com.horizonvalley;

public final class Config {
    public static final int TILE_SIZE = 32;
    public static final int FPS = 60;
    public static final String TITLE = "Horizon Valley";
    public static final String VERSION = "1.0.0";

    public static final float PLAYER_SPEED = 150f;
    public static final float PLAYER_RUN_MULT = 1.6f;
    public static final int PLAYER_MAX_HEALTH = 100;
    public static final int PLAYER_MAX_ENERGY = 100;
    public static final int PLAYER_START_MONEY = 500;
    public static final int INVENTORY_SLOTS = 36;
    public static final int HOTBAR_SLOTS = 12;

    public static final int WORLD_WIDTH = 80;
    public static final int WORLD_HEIGHT = 60;

    // Colors (ARGB)
    public static final int COLOR_BLACK = 0xFF000000;
    public static final int COLOR_WHITE = 0xFFFFFFFF;
    public static final int COLOR_GRASS = 0xFF50A03C;
    public static final int COLOR_DIRT = 0xFF785028;
    public static final int COLOR_TILLED = 0xFF644623;
    public static final int COLOR_WATERED = 0xFF46321E;
    public static final int COLOR_WATER = 0xFF2864B4;
    public static final int COLOR_PATH = 0xFFA08C64;
    public static final int COLOR_WOOD = 0xFF8C643C;
    public static final int COLOR_WALL = 0xFF5A4632;
    public static final int COLOR_TREE = 0xFF287828;
    public static final int COLOR_ROCK = 0xFF64646E;
    public static final int COLOR_UI_BG = 0xE01E1E28;
    public static final int COLOR_UI_BORDER = 0xFF505064;
    public static final int COLOR_HEALTH = 0xFFC83232;
    public static final int COLOR_ENERGY = 0xFF32B450;
    public static final int COLOR_GOLD = 0xFFFFC832;
    public static final int COLOR_PLAYER = 0xFF3264B4;
    public static final int COLOR_NPC = 0xFFC85050;

    private Config() {}
}

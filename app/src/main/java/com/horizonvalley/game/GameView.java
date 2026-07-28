package com.horizonvalley.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.horizonvalley.Config;
import com.horizonvalley.entity.Player;
import com.horizonvalley.ui.TouchControls;
import com.horizonvalley.ui.HUD;
import com.horizonvalley.world.World;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Thread gameThread;
    private volatile boolean running = false;
    private final SurfaceHolder holder;
    private final Paint paint = new Paint();

    private World world;
    private Player player;
    private TouchControls touch;
    private HUD hud;
    private GameState state = GameState.MENU;

    private float cameraX, cameraY;
    private long lastTime;

    // Menu buttons
    private RectF btnNewGame, btnQuit;
    private boolean menuReady = false;

    public enum GameState { MENU, PLAYING, PAUSED, INVENTORY, DIALOGUE }

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);
        paint.setAntiAlias(true);
        touch = new TouchControls();
        hud = new HUD();
    }

    @Override
    public void surfaceCreated(SurfaceHolder h) {
        initGame();
        resume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
        touch.layout(width, height);
        btnNewGame = new RectF(width / 2f - 150, height / 2f - 40, width / 2f + 150, height / 2f + 20);
        btnQuit = new RectF(width / 2f - 150, height / 2f + 50, width / 2f + 150, height / 2f + 110);
        menuReady = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder h) {
        pause();
    }

    private void initGame() {
        world = new World();
        player = new Player();
        player.x = 15 * Config.TILE_SIZE;
        player.y = 15 * Config.TILE_SIZE;
        world.setPlayer(player);
        cameraX = player.x - getWidth() / 2f;
        cameraY = player.y - getHeight() / 2f;
    }

    public void resume() {
        if (running) return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        running = false;
        try {
            if (gameThread != null) gameThread.join(500);
        } catch (InterruptedException ignored) {}
    }

    public boolean onBackPressed() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
            return true;
        } else if (state == GameState.PAUSED || state == GameState.INVENTORY || state == GameState.DIALOGUE) {
            state = GameState.PLAYING;
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        lastTime = System.nanoTime();
        final double nsPerFrame = 1_000_000_000.0 / Config.FPS;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerFrame;
            lastTime = now;

            while (delta >= 1) {
                update(1f / Config.FPS);
                delta--;
            }
            render();
        }
    }

    private void update(float dt) {
        if (state != GameState.PLAYING) return;

        float dx = 0, dy = 0;
        if (touch.isDown(TouchControls.UP)) dy -= 1;
        if (touch.isDown(TouchControls.DOWN)) dy += 1;
        if (touch.isDown(TouchControls.LEFT)) dx -= 1;
        if (touch.isDown(TouchControls.RIGHT)) dx += 1;

        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            dx /= len;
            dy /= len;
        }
        boolean run = touch.isDown(TouchControls.RUN);
        player.move(dx, dy, dt, run, world);

        // Camera follow
        float targetX = player.x - getWidth() / 2f;
        float targetY = player.y - getHeight() / 2f;
        cameraX += (targetX - cameraX) * Math.min(1f, 8f * dt);
        cameraY += (targetY - cameraY) * Math.min(1f, 8f * dt);

        // Clamp camera
        cameraX = Math.max(0, Math.min(cameraX, world.getPixelWidth() - getWidth()));
        cameraY = Math.max(0, Math.min(cameraY, world.getPixelHeight() - getHeight()));

        world.update(dt);
    }

    private void render() {
        if (!holder.getSurface().isValid()) return;
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas == null) return;

            canvas.drawColor(Color.BLACK);

            if (state == GameState.MENU) {
                drawMenu(canvas);
            } else {
                world.render(canvas, cameraX, cameraY, getWidth(), getHeight(), paint);
                player.render(canvas, cameraX, cameraY, paint);
                hud.render(canvas, player, paint, getWidth());
                touch.render(canvas, paint);

                if (state == GameState.PAUSED) drawPauseOverlay(canvas);
                if (state == GameState.INVENTORY) drawInventory(canvas);
            }
        } finally {
            if (canvas != null) {
                try {
                    holder.unlockCanvasAndPost(canvas);
                } catch (Exception ignored) {}
            }
        }
    }

    private void drawMenu(Canvas c) {
        int w = getWidth(), h = getHeight();
        // Background
        paint.setColor(0xFF1A2030);
        c.drawRect(0, 0, w, h, paint);

        paint.setColor(Config.COLOR_GOLD);
        paint.setTextSize(64);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText("Horizon Valley", w / 2f, h * 0.3f, paint);

        paint.setTextSize(28);
        paint.setColor(Config.COLOR_WHITE);
        c.drawText("v" + Config.VERSION + "  •  Native Android", w / 2f, h * 0.38f, paint);

        if (menuReady) {
            drawButton(c, btnNewGame, "New Game");
            drawButton(c, btnQuit, "Quit");
        }
    }

    private void drawButton(Canvas c, RectF r, String text) {
        paint.setColor(0xFF2A3040);
        c.drawRoundRect(r, 16, 16, paint);
        paint.setColor(0xFF6080C0);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        c.drawRoundRect(r, 16, 16, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Config.COLOR_WHITE);
        paint.setTextSize(36);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(text, r.centerX(), r.centerY() + 12, paint);
    }

    private void drawPauseOverlay(Canvas c) {
        paint.setColor(0xAA000000);
        c.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setColor(Config.COLOR_WHITE);
        paint.setTextSize(48);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText("Paused", getWidth() / 2f, getHeight() / 2f, paint);
        paint.setTextSize(24);
        c.drawText("Press Back to resume", getWidth() / 2f, getHeight() / 2f + 50, paint);
    }

    private void drawInventory(Canvas c) {
        int w = getWidth(), h = getHeight();
        float panelW = Math.min(500, w - 40);
        float panelH = Math.min(400, h - 40);
        float px = (w - panelW) / 2;
        float py = (h - panelH) / 2;

        paint.setColor(Config.COLOR_UI_BG);
        c.drawRoundRect(px, py, px + panelW, py + panelH, 12, 12, paint);
        paint.setColor(Config.COLOR_UI_BORDER);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        c.drawRoundRect(px, py, px + panelW, py + panelH, 12, 12, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Config.COLOR_WHITE);
        paint.setTextSize(36);
        paint.setTextAlign(Paint.Align.LEFT);
        c.drawText("Inventory", px + 20, py + 40, paint);

        // Simple slot grid
        float slot = 48;
        float startX = px + 30;
        float startY = py + 70;
        for (int i = 0; i < Config.INVENTORY_SLOTS; i++) {
            int col = i % 6;
            int row = i / 6;
            float x = startX + col * (slot + 8);
            float y = startY + row * (slot + 8);
            paint.setColor(0xFF2A2A38);
            c.drawRect(x, y, x + slot, y + slot, paint);
            paint.setColor(Config.COLOR_UI_BORDER);
            paint.setStyle(Paint.Style.STROKE);
            c.drawRect(x, y, x + slot, y + slot, paint);
            paint.setStyle(Paint.Style.FILL);

            // Draw item if present
            String item = player.inventory.getSlot(i);
            if (item != null) {
                paint.setColor(0xFF80A0FF);
                c.drawRect(x + 6, y + 6, x + slot - 6, y + slot - 6, paint);
                paint.setColor(Config.COLOR_WHITE);
                paint.setTextSize(14);
                paint.setTextAlign(Paint.Align.CENTER);
                c.drawText(item.substring(0, Math.min(4, item.length())), x + slot / 2, y + slot / 2 + 5, paint);
            }
        }
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(20);
        paint.setColor(0xFFAAAAAA);
        c.drawText("Back to close", px + 20, py + panelH - 20, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        if (state == GameState.MENU && menuReady) {
            if (action == MotionEvent.ACTION_UP) {
                if (btnNewGame.contains(x, y)) {
                    state = GameState.PLAYING;
                    return true;
                }
                if (btnQuit.contains(x, y)) {
                    ((android.app.Activity) getContext()).finish();
                    return true;
                }
            }
            return true;
        }

        // Inventory toggle / close
        if (action == MotionEvent.ACTION_UP) {
            if (touch.wasInventoryPressed(x, y)) {
                if (state == GameState.INVENTORY) state = GameState.PLAYING;
                else if (state == GameState.PLAYING) state = GameState.INVENTORY;
                return true;
            }
            if (state == GameState.INVENTORY) {
                state = GameState.PLAYING;
                return true;
            }
            if (touch.wasInteractPressed(x, y) && state == GameState.PLAYING) {
                // Simple interact: talk to nearest NPC
                world.tryInteract(player);
                return true;
            }
        }

        // Pass to touch controls for movement
        if (state == GameState.PLAYING || state == GameState.INVENTORY) {
            touch.onTouch(event);
            // Tool use: tap on world (not on buttons)
            if (action == MotionEvent.ACTION_UP && state == GameState.PLAYING) {
                if (!touch.isOnAnyButton(x, y)) {
                    float worldX = x + cameraX;
                    float worldY = y + cameraY;
                    player.useTool(worldX, worldY, world);
                }
            }
        }
        return true;
    }
}

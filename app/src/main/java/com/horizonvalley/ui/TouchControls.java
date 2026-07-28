package com.horizonvalley.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import com.horizonvalley.Config;
import java.util.HashMap;
import java.util.Map;

public class TouchControls {
    public static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3, INTERACT = 4, INVENTORY = 5, RUN = 6;

    private final RectF[] buttons = new RectF[7];
    private final boolean[] pressed = new boolean[7];
    private final Map<Integer, Integer> pointerMap = new HashMap<>();
    private int screenW, screenH;

    public void layout(int w, int h) {
        screenW = w;
        screenH = h;
        float size = Math.min(80, w * 0.12f);
        float m = 24;

        // D-pad left
        float bx = m;
        float by = h - m - size * 3;
        buttons[UP] = new RectF(bx + size, by, bx + size * 2, by + size);
        buttons[LEFT] = new RectF(bx, by + size, bx + size, by + size * 2);
        buttons[DOWN] = new RectF(bx + size, by + size * 2, bx + size * 2, by + size * 3);
        buttons[RIGHT] = new RectF(bx + size * 2, by + size, bx + size * 3, by + size * 2);

        // Action buttons right
        float aw = size * 1.3f, ah = size * 0.9f;
        float rx = w - m - aw;
        float ry = h - m - ah * 3 - 30;
        buttons[INTERACT] = new RectF(rx, ry, rx + aw, ry + ah);
        buttons[INVENTORY] = new RectF(rx, ry + ah + 12, rx + aw, ry + ah * 2 + 12);
        buttons[RUN] = new RectF(rx, ry + (ah + 12) * 2, rx + aw, ry + ah * 3 + 24);
    }

    public void onTouch(MotionEvent e) {
        int action = e.getActionMasked();
        int index = e.getActionIndex();
        int id = e.getPointerId(index);
        float x = e.getX(index);
        float y = e.getY(index);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                for (int i = 0; i < buttons.length; i++) {
                    if (buttons[i] != null && buttons[i].contains(x, y)) {
                        pressed[i] = true;
                        pointerMap.put(id, i);
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                Integer btn = pointerMap.remove(id);
                if (btn != null) pressed[btn] = false;
                // Clear all if last pointer
                if (e.getPointerCount() <= 1 && action != MotionEvent.ACTION_POINTER_UP) {
                    for (int i = 0; i < pressed.length; i++) pressed[i] = false;
                    pointerMap.clear();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                for (int p = 0; p < e.getPointerCount(); p++) {
                    int pid = e.getPointerId(p);
                    float px = e.getX(p), py = e.getY(p);
                    Integer mapped = pointerMap.get(pid);
                    if (mapped != null) {
                        if (!buttons[mapped].contains(px, py)) {
                            pressed[mapped] = false;
                            pointerMap.remove(pid);
                        }
                    }
                }
                break;
        }
    }

    public boolean isDown(int action) {
        return action >= 0 && action < pressed.length && pressed[action];
    }

    public boolean isOnAnyButton(float x, float y) {
        for (RectF r : buttons)
            if (r != null && r.contains(x, y)) return true;
        return false;
    }

    public boolean wasInventoryPressed(float x, float y) {
        return buttons[INVENTORY] != null && buttons[INVENTORY].contains(x, y);
    }

    public boolean wasInteractPressed(float x, float y) {
        return buttons[INTERACT] != null && buttons[INTERACT].contains(x, y);
    }

    public void render(Canvas c, Paint p) {
        String[] labels = {"▲", "▼", "◀", "▶", "E", "I", "Run"};
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == null) continue;
            p.setColor(pressed[i] ? 0xCC508CDC : 0xAA28283C);
            c.drawRoundRect(buttons[i], 14, 14, p);
            p.setColor(0xFFB0B0C8);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2);
            c.drawRoundRect(buttons[i], 14, 14, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Config.COLOR_WHITE);
            p.setTextSize(i < 4 ? 36 : 28);
            p.setTextAlign(Paint.Align.CENTER);
            c.drawText(labels[i], buttons[i].centerX(), buttons[i].centerY() + 12, p);
        }
    }
}

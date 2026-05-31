package com.mycompany.platformer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class TouchControls extends InputAdapter {
    private static final float MIN_BUTTON_SIZE = 58f;
    private static final float MAX_BUTTON_SIZE = 84f;
    private static final float MIN_MARGIN = 16f;
    private static final float MAX_MARGIN = 36f;
    private static final float GAP_SCALE = 0.18f;
    private static final float PRESSED_OFFSET = 3f;

    private final Viewport viewport;
    private final Rectangle leftButton = new Rectangle();
    private final Rectangle rightButton = new Rectangle();
    private final Rectangle jumpButton = new Rectangle();
    private final Vector2 touchPoint = new Vector2();

    private int leftPointer = -1;
    private int rightPointer = -1;
    private int jumpPointer = -1;
    private boolean jumpQueued;

    public TouchControls(Viewport viewport) {
        this.viewport = viewport;
    }

    public void resize() {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        float shortSide = Math.min(width, height);
        float buttonSize = clamp(shortSide * 0.17f, MIN_BUTTON_SIZE, MAX_BUTTON_SIZE);
        float margin = clamp(shortSide * 0.055f, MIN_MARGIN, MAX_MARGIN);
        float gap = buttonSize * GAP_SCALE;
        float y = margin;

        leftButton.set(margin, y, buttonSize, buttonSize);
        rightButton.set(margin + buttonSize + gap, y, buttonSize, buttonSize);
        jumpButton.set(width - margin - buttonSize, y, buttonSize, buttonSize);
    }

    public float getMovement() {
        float movement = 0f;
        if (leftPointer != -1) {
            movement -= 1f;
        }
        if (rightPointer != -1) {
            movement += 1f;
        }
        return movement;
    }

    public boolean consumeJump() {
        boolean requested = jumpQueued;
        jumpQueued = false;
        return requested;
    }

    public void draw(ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        drawButton(shapes, leftButton, leftPointer != -1, Arrow.LEFT);
        drawButton(shapes, rightButton, rightPointer != -1, Arrow.RIGHT);
        drawButton(shapes, jumpButton, jumpPointer != -1, Arrow.UP);

        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        unproject(screenX, screenY);

        if (leftButton.contains(touchPoint) && leftPointer == -1) {
            leftPointer = pointer;
            return true;
        }
        if (rightButton.contains(touchPoint) && rightPointer == -1) {
            rightPointer = pointer;
            return true;
        }
        if (jumpButton.contains(touchPoint) && jumpPointer == -1) {
            jumpPointer = pointer;
            jumpQueued = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        releasePointer(pointer);
        return touchDown(screenX, screenY, pointer, 0);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return releasePointer(pointer);
    }

    private boolean releasePointer(int pointer) {
        if (pointer == leftPointer) {
            leftPointer = -1;
            return true;
        }
        if (pointer == rightPointer) {
            rightPointer = -1;
            return true;
        }
        if (pointer == jumpPointer) {
            jumpPointer = -1;
            return true;
        }
        return false;
    }

    private void unproject(int screenX, int screenY) {
        touchPoint.set(screenX, screenY);
        viewport.unproject(touchPoint);
    }

    private void drawButton(ShapeRenderer shapes, Rectangle button, boolean pressed, Arrow arrow) {
        float offset = pressed ? -PRESSED_OFFSET : 0f;
        float x = button.x;
        float y = button.y + offset;
        float w = button.width;
        float h = button.height;
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float r = w * 0.5f;

        shapes.setColor(new Color(0.01f, 0.02f, 0.03f, 0.34f));
        shapes.circle(cx, cy - h * 0.08f, r * 0.98f);

        shapes.setColor(pressed ? new Color(0.08f, 0.63f, 0.70f, 0.95f) : new Color(0.07f, 0.13f, 0.17f, 0.88f));
        shapes.circle(cx, cy, r);

        shapes.setColor(new Color(0.67f, 0.92f, 0.96f, pressed ? 0.42f : 0.24f));
        shapes.circle(cx, cy, r * 0.82f);

        shapes.setColor(pressed ? new Color(0.02f, 0.20f, 0.23f, 0.86f) : new Color(0.03f, 0.08f, 0.11f, 0.82f));
        shapes.circle(cx, cy, r * 0.70f);

        shapes.setColor(new Color(1f, 1f, 1f, pressed ? 0.22f : 0.16f));
        shapes.circle(cx - r * 0.18f, cy + r * 0.24f, r * 0.22f);

        shapes.setColor(pressed ? new Color(0.02f, 0.09f, 0.11f, 1f) : new Color(0.88f, 0.98f, 1f, 1f));
        drawArrow(shapes, cx, cy, r * 0.58f, r * 0.16f, arrow);
    }

    private void drawArrow(ShapeRenderer shapes, float cx, float cy, float size, float stroke, Arrow arrow) {
        float half = size * 0.5f;
        float wing = size * 0.82f;

        switch (arrow) {
            case LEFT:
                shapes.rectLine(cx + half, cy + wing * 0.5f, cx - half, cy, stroke);
                shapes.rectLine(cx - half, cy, cx + half, cy - wing * 0.5f, stroke);
                shapes.rectLine(cx - half * 0.2f, cy, cx + half * 0.8f, cy, stroke);
                break;
            case RIGHT:
                shapes.rectLine(cx - half, cy + wing * 0.5f, cx + half, cy, stroke);
                shapes.rectLine(cx + half, cy, cx - half, cy - wing * 0.5f, stroke);
                shapes.rectLine(cx - half * 0.8f, cy, cx + half * 0.2f, cy, stroke);
                break;
            case UP:
                shapes.rectLine(cx - wing * 0.5f, cy - half, cx, cy + half, stroke);
                shapes.rectLine(cx, cy + half, cx + wing * 0.5f, cy - half, stroke);
                shapes.rectLine(cx, cy - half * 0.85f, cx, cy + half * 0.05f, stroke);
                break;
            default:
                break;
        }
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum Arrow {
        LEFT,
        RIGHT,
        UP
    }
}

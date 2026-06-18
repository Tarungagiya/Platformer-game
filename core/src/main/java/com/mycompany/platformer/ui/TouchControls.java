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
    private static final float MIN_BUTTON_SIZE = 54f;
    private static final float MAX_BUTTON_SIZE = 78f;
    private static final float MIN_MARGIN = 14f;
    private static final float MAX_MARGIN = 32f;
    private static final float GAP_SCALE = 0.16f;
    private static final float SMALL_BUTTON_SCALE = 0.66f;
    private static final float PRESSED_OFFSET = 3f;

    private final Viewport viewport;
    private final Rectangle leftButton = new Rectangle();
    private final Rectangle rightButton = new Rectangle();
    private final Rectangle jumpButton = new Rectangle();
    private final Rectangle dashButton = new Rectangle();
    private final Rectangle poundButton = new Rectangle();
    private final Rectangle shootButton = new Rectangle();
    private final Rectangle rotateLeftButton = new Rectangle();
    private final Rectangle rotateRightButton = new Rectangle();
    private final Rectangle copyButton = new Rectangle();
    private final Rectangle resetButton = new Rectangle();
    private final Vector2 touchPoint = new Vector2();

    private int leftPointer = -1;
    private int rightPointer = -1;
    private int jumpPointer = -1;
    private int dashPointer = -1;
    private int poundPointer = -1;
    private int shootPointer = -1;
    private int rotateLeftPointer = -1;
    private int rotateRightPointer = -1;
    private int copyPointer = -1;
    private int resetPointer = -1;

    private boolean jumpQueued;
    private boolean dashQueued;
    private boolean rotateLeftQueued;
    private boolean rotateRightQueued;
    private boolean copyQueued;
    private boolean resetQueued;

    public TouchControls(Viewport viewport) {
        this.viewport = viewport;
    }

    public void resize() {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        float shortSide = Math.min(width, height);
        float buttonSize = clamp(shortSide * 0.155f, MIN_BUTTON_SIZE, MAX_BUTTON_SIZE);
        float smallButtonSize = buttonSize * SMALL_BUTTON_SCALE;
        float margin = clamp(shortSide * 0.05f, MIN_MARGIN, MAX_MARGIN);
        float gap = buttonSize * GAP_SCALE;
        float y = margin;

        leftButton.set(margin, y, buttonSize, buttonSize);
        rightButton.set(margin + buttonSize + gap, y, buttonSize, buttonSize);

        float rightX = width - margin - buttonSize;
        jumpButton.set(rightX, y, buttonSize, buttonSize);
        dashButton.set(rightX - buttonSize - gap, y, buttonSize, buttonSize);
        poundButton.set(rightX, y + buttonSize + gap, buttonSize, buttonSize);
        shootButton.set(rightX - buttonSize - gap, y + buttonSize + gap, buttonSize, buttonSize);

        float smallY = y + buttonSize * 2f + gap * 2.2f;
        rotateLeftButton.set(rightX - smallButtonSize * 3f - gap * 2f, smallY, smallButtonSize, smallButtonSize);
        rotateRightButton.set(rightX - smallButtonSize * 2f - gap, smallY, smallButtonSize, smallButtonSize);
        copyButton.set(rightX - smallButtonSize, smallY, smallButtonSize, smallButtonSize);
        resetButton.set(width - margin - smallButtonSize, height - margin - smallButtonSize, smallButtonSize, smallButtonSize);
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

    public boolean consumeDash() {
        boolean requested = dashQueued;
        dashQueued = false;
        return requested;
    }

    public boolean isGroundPoundPressed() {
        return poundPointer != -1;
    }

    public boolean isShootPressed() {
        return shootPointer != -1;
    }

    public boolean consumeRotateLeft() {
        boolean requested = rotateLeftQueued;
        rotateLeftQueued = false;
        return requested;
    }

    public boolean consumeRotateRight() {
        boolean requested = rotateRightQueued;
        rotateRightQueued = false;
        return requested;
    }

    public boolean consumeCopyGravity() {
        boolean requested = copyQueued;
        copyQueued = false;
        return requested;
    }

    public boolean consumeReset() {
        boolean requested = resetQueued;
        resetQueued = false;
        return requested;
    }

    public void draw(ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        drawButton(shapes, leftButton, leftPointer != -1, Icon.LEFT);
        drawButton(shapes, rightButton, rightPointer != -1, Icon.RIGHT);
        drawButton(shapes, jumpButton, jumpPointer != -1, Icon.UP);
        drawButton(shapes, dashButton, dashPointer != -1, Icon.DASH);
        drawButton(shapes, poundButton, poundPointer != -1, Icon.DOWN);
        drawButton(shapes, shootButton, shootPointer != -1, Icon.SHOOT);
        drawButton(shapes, rotateLeftButton, rotateLeftPointer != -1, Icon.ROTATE_LEFT);
        drawButton(shapes, rotateRightButton, rotateRightPointer != -1, Icon.ROTATE_RIGHT);
        drawButton(shapes, copyButton, copyPointer != -1, Icon.COPY);
        drawButton(shapes, resetButton, resetPointer != -1, Icon.RESET);

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
        if (dashButton.contains(touchPoint) && dashPointer == -1) {
            dashPointer = pointer;
            dashQueued = true;
            return true;
        }
        if (poundButton.contains(touchPoint) && poundPointer == -1) {
            poundPointer = pointer;
            return true;
        }
        if (shootButton.contains(touchPoint) && shootPointer == -1) {
            shootPointer = pointer;
            return true;
        }
        if (rotateLeftButton.contains(touchPoint) && rotateLeftPointer == -1) {
            rotateLeftPointer = pointer;
            rotateLeftQueued = true;
            return true;
        }
        if (rotateRightButton.contains(touchPoint) && rotateRightPointer == -1) {
            rotateRightPointer = pointer;
            rotateRightQueued = true;
            return true;
        }
        if (copyButton.contains(touchPoint) && copyPointer == -1) {
            copyPointer = pointer;
            copyQueued = true;
            return true;
        }
        if (resetButton.contains(touchPoint) && resetPointer == -1) {
            resetPointer = pointer;
            resetQueued = true;
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
        if (pointer == dashPointer) {
            dashPointer = -1;
            return true;
        }
        if (pointer == poundPointer) {
            poundPointer = -1;
            return true;
        }
        if (pointer == shootPointer) {
            shootPointer = -1;
            return true;
        }
        if (pointer == rotateLeftPointer) {
            rotateLeftPointer = -1;
            return true;
        }
        if (pointer == rotateRightPointer) {
            rotateRightPointer = -1;
            return true;
        }
        if (pointer == copyPointer) {
            copyPointer = -1;
            return true;
        }
        if (pointer == resetPointer) {
            resetPointer = -1;
            return true;
        }
        return false;
    }

    private void unproject(int screenX, int screenY) {
        touchPoint.set(screenX, screenY);
        viewport.unproject(touchPoint);
    }

    private void drawButton(ShapeRenderer shapes, Rectangle button, boolean pressed, Icon icon) {
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
        drawIcon(shapes, cx, cy, r * 1.16f, r * 0.16f, icon);
    }

    private void drawIcon(ShapeRenderer shapes, float cx, float cy, float size, float stroke, Icon icon) {
        float half = size * 0.5f;
        float wing = size * 0.82f;

        switch (icon) {
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
            case DOWN:
                shapes.rectLine(cx - wing * 0.5f, cy + half, cx, cy - half, stroke);
                shapes.rectLine(cx, cy - half, cx + wing * 0.5f, cy + half, stroke);
                shapes.rectLine(cx, cy + half * 0.85f, cx, cy - half * 0.05f, stroke);
                break;
            case DASH:
                shapes.rectLine(cx - half, cy, cx + half, cy, stroke);
                shapes.rectLine(cx + half, cy, cx + half * 0.15f, cy + wing * 0.42f, stroke);
                shapes.rectLine(cx + half, cy, cx + half * 0.15f, cy - wing * 0.42f, stroke);
                shapes.rectLine(cx - half, cy + wing * 0.36f, cx - half * 0.1f, cy + wing * 0.36f, stroke * 0.75f);
                shapes.rectLine(cx - half, cy - wing * 0.36f, cx - half * 0.1f, cy - wing * 0.36f, stroke * 0.75f);
                break;
            case SHOOT:
                shapes.circle(cx - size * 0.20f, cy, stroke * 1.25f);
                shapes.rectLine(cx - size * 0.02f, cy, cx + half, cy, stroke);
                shapes.circle(cx + half, cy, stroke * 0.9f);
                break;
            case ROTATE_LEFT:
                drawRotate(shapes, cx, cy, size, stroke, true);
                break;
            case ROTATE_RIGHT:
                drawRotate(shapes, cx, cy, size, stroke, false);
                break;
            case COPY:
                shapes.circle(cx - size * 0.16f, cy + size * 0.08f, size * 0.25f);
                shapes.circle(cx + size * 0.18f, cy - size * 0.08f, size * 0.25f);
                shapes.rectLine(cx - size * 0.03f, cy + size * 0.06f, cx + size * 0.04f, cy - size * 0.03f, stroke * 0.8f);
                break;
            case RESET:
                shapes.rectLine(cx - half * 0.62f, cy + half * 0.55f, cx + half * 0.45f, cy + half * 0.55f, stroke);
                shapes.rectLine(cx + half * 0.45f, cy + half * 0.55f, cx + half * 0.45f, cy - half * 0.55f, stroke);
                shapes.rectLine(cx + half * 0.45f, cy - half * 0.55f, cx - half * 0.45f, cy - half * 0.55f, stroke);
                shapes.rectLine(cx - half * 0.45f, cy - half * 0.55f, cx - half * 0.45f, cy, stroke);
                shapes.rectLine(cx - half * 0.45f, cy, cx - half * 0.78f, cy - half * 0.24f, stroke);
                shapes.rectLine(cx - half * 0.45f, cy, cx - half * 0.16f, cy - half * 0.25f, stroke);
                break;
            default:
                break;
        }
    }

    private void drawRotate(ShapeRenderer shapes, float cx, float cy, float size, float stroke, boolean left) {
        float radius = size * 0.34f;
        int segments = 9;
        float start = left ? 35f : 145f;
        float end = left ? 305f : -125f;
        float prevX = cx + (float) Math.cos(Math.toRadians(start)) * radius;
        float prevY = cy + (float) Math.sin(Math.toRadians(start)) * radius;
        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float angle = start + (end - start) * t;
            float x = cx + (float) Math.cos(Math.toRadians(angle)) * radius;
            float y = cy + (float) Math.sin(Math.toRadians(angle)) * radius;
            shapes.rectLine(prevX, prevY, x, y, stroke);
            prevX = x;
            prevY = y;
        }
        float tipX = cx + (float) Math.cos(Math.toRadians(end)) * radius;
        float tipY = cy + (float) Math.sin(Math.toRadians(end)) * radius;
        float wingA = end + (left ? 42f : -42f);
        float wingB = end + (left ? 96f : -96f);
        shapes.rectLine(tipX, tipY, tipX + (float) Math.cos(Math.toRadians(wingA)) * size * 0.22f,
                tipY + (float) Math.sin(Math.toRadians(wingA)) * size * 0.22f, stroke);
        shapes.rectLine(tipX, tipY, tipX + (float) Math.cos(Math.toRadians(wingB)) * size * 0.22f,
                tipY + (float) Math.sin(Math.toRadians(wingB)) * size * 0.22f, stroke);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum Icon {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        DASH,
        SHOOT,
        ROTATE_LEFT,
        ROTATE_RIGHT,
        COPY,
        RESET
    }
}

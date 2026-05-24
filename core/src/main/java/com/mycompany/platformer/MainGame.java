package com.mycompany.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class MainGame extends ApplicationAdapter {
    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 480f;
    private static final float GROUND_HEIGHT = 72f;

    private static final float PLAYER_WIDTH = 42f;
    private static final float PLAYER_HEIGHT = 64f;
    private static final float PLAYER_SPEED = 260f;
    private static final float JUMP_SPEED = 560f;
    private static final float GRAVITY = -1500f;

    private static final float JOYSTICK_BASE_RADIUS = 58f;
    private static final float JOYSTICK_KNOB_RADIUS = 24f;
    private static final float JOYSTICK_MARGIN = 76f;

    private ShapeRenderer shapes;
    private OrthographicCamera worldCamera;
    private OrthographicCamera uiCamera;
    private Viewport worldViewport;
    private Viewport uiViewport;

    private final Vector2 playerPosition = new Vector2(120f, GROUND_HEIGHT);
    private final Vector2 playerVelocity = new Vector2();
    private boolean grounded;

    private final Vector2 joystickCenter = new Vector2();
    private final Vector2 joystickKnob = new Vector2();
    private final Vector2 joystickDirection = new Vector2();
    private int joystickPointer = -1;

    @Override
    public void create() {
        shapes = new ShapeRenderer();

        worldCamera = new OrthographicCamera();
        worldViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, worldCamera);

        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector2 touch = uiViewport.unproject(new Vector2(screenX, screenY));

                if (touch.dst(joystickCenter) <= JOYSTICK_BASE_RADIUS * 1.5f && joystickPointer == -1) {
                    joystickPointer = pointer;
                    updateJoystick(touch);
                    return true;
                }

                if (grounded) {
                    jump();
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (pointer != joystickPointer) {
                    return false;
                }

                updateJoystick(uiViewport.unproject(new Vector2(screenX, screenY)));
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (pointer != joystickPointer) {
                    return false;
                }

                joystickPointer = -1;
                joystickDirection.setZero();
                joystickKnob.set(joystickCenter);
                return true;
            }
        });

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        update(delta);

        ScreenUtils.clear(0.09f, 0.12f, 0.16f, 1);
        drawWorld();
        drawJoystick();
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        uiViewport.update(width, height, true);

        joystickCenter.set(JOYSTICK_MARGIN, JOYSTICK_MARGIN);
        joystickKnob.set(joystickCenter);
    }

    private void update(float delta) {
        float movement = joystickDirection.x;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            movement -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            movement += 1f;
        }

        movement = MathUtils.clamp(movement, -1f, 1f);
        playerVelocity.x = movement * PLAYER_SPEED;

        if (grounded && (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)
            || joystickDirection.y > 0.65f)) {
            jump();
        }

        playerVelocity.y += GRAVITY * delta;
        playerPosition.mulAdd(playerVelocity, delta);

        if (playerPosition.y <= GROUND_HEIGHT) {
            playerPosition.y = GROUND_HEIGHT;
            playerVelocity.y = 0f;
            grounded = true;
        } else {
            grounded = false;
        }

        playerPosition.x = MathUtils.clamp(playerPosition.x, 0f, WORLD_WIDTH - PLAYER_WIDTH);
    }

    private void jump() {
        playerVelocity.y = JUMP_SPEED;
        grounded = false;
    }

    private void updateJoystick(Vector2 touch) {
        Vector2 offset = touch.sub(joystickCenter);

        if (offset.len() > JOYSTICK_BASE_RADIUS) {
            offset.setLength(JOYSTICK_BASE_RADIUS);
        }

        joystickKnob.set(joystickCenter).add(offset);
        joystickDirection.set(offset).scl(1f / JOYSTICK_BASE_RADIUS);
    }

    private void drawWorld() {
        shapes.setProjectionMatrix(worldViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(0.17f, 0.24f, 0.32f, 1f);
        shapes.rect(0f, 0f, WORLD_WIDTH, GROUND_HEIGHT);

        shapes.setColor(0.25f, 0.34f, 0.42f, 1f);
        shapes.rect(0f, GROUND_HEIGHT - 8f, WORLD_WIDTH, 8f);

        shapes.setColor(0.96f, 0.72f, 0.27f, 1f);
        shapes.rect(playerPosition.x, playerPosition.y, PLAYER_WIDTH, PLAYER_HEIGHT);

        shapes.setColor(0.18f, 0.10f, 0.08f, 1f);
        float eyeY = playerPosition.y + PLAYER_HEIGHT - 20f;
        shapes.circle(playerPosition.x + 28f, eyeY, 4f);

        shapes.end();
    }

    private void drawJoystick() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(uiViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(new Color(1f, 1f, 1f, 0.18f));
        shapes.circle(joystickCenter.x, joystickCenter.y, JOYSTICK_BASE_RADIUS);

        shapes.setColor(new Color(1f, 1f, 1f, 0.42f));
        shapes.circle(joystickKnob.x, joystickKnob.y, JOYSTICK_KNOB_RADIUS);

        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
}

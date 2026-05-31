package com.mycompany.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mycompany.platformer.entities.Player;
import com.mycompany.platformer.graphics.PlayerRenderer;
import com.mycompany.platformer.ui.TouchControls;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class MainGame extends ApplicationAdapter {
    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 480f;
    private static final float GROUND_HEIGHT = 72f;

    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private OrthographicCamera worldCamera;
    private OrthographicCamera uiCamera;
    private Viewport worldViewport;
    private Viewport uiViewport;
    private Player player;
    private PlayerRenderer playerRenderer;
    private TouchControls touchControls;

    @Override
    public void create() {
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();

        worldCamera = new OrthographicCamera();
        worldViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, worldCamera);

        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);

        player = new Player(120f, GROUND_HEIGHT);
        playerRenderer = new PlayerRenderer();
        touchControls = new TouchControls(uiViewport);
        Gdx.input.setInputProcessor(touchControls);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        update(delta);

        ScreenUtils.clear(0.09f, 0.12f, 0.16f, 1);
        drawWorld();
        touchControls.draw(shapes);
    }

    @Override
    public void dispose() {
        playerRenderer.dispose();
        batch.dispose();
        shapes.dispose();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        uiViewport.update(width, height, true);
        touchControls.resize();
    }

    private void update(float delta) {
        float movement = touchControls.getMovement();

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            movement -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            movement += 1f;
        }

        movement = MathUtils.clamp(movement, -1f, 1f);
        boolean jumpRequested = touchControls.consumeJump()
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP)
            || Gdx.input.isKeyJustPressed(Input.Keys.W);
        player.update(delta, movement, jumpRequested, GROUND_HEIGHT, WORLD_WIDTH);
        playerRenderer.update(delta);
    }

    private void drawWorld() {
        shapes.setProjectionMatrix(worldViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(0.17f, 0.24f, 0.32f, 1f);
        shapes.rect(0f, 0f, WORLD_WIDTH, GROUND_HEIGHT);

        shapes.setColor(0.25f, 0.34f, 0.42f, 1f);
        shapes.rect(0f, GROUND_HEIGHT - 8f, WORLD_WIDTH, 8f);

        shapes.end();

        batch.setProjectionMatrix(worldViewport.getCamera().combined);
        batch.begin();
        playerRenderer.draw(batch, player);
        batch.end();
    }
}

package com.mycompany.platformer.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mycompany.platformer.entities.Player;

public class PlayerRenderer {
    private static final String IDLE_SHEET = "Player/Idle-1.png";
    private static final int IDLE_COLUMNS = 4;
    private static final int IDLE_ROWS = 2;
    private static final float FRAME_DURATION = 0.18f;
    private static final float VISUAL_HEIGHT = 132f;

    private final Texture idleTexture;
    private final Animation<PlayerFrame> idleAnimation;
    private float stateTime;

    public PlayerRenderer() {
        idleTexture = new Texture(Gdx.files.internal(IDLE_SHEET));
        idleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        idleAnimation = new Animation<PlayerFrame>(FRAME_DURATION, loadFrames(), Animation.PlayMode.LOOP_PINGPONG);
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void draw(SpriteBatch batch, Player player) {
        Vector2 position = player.getPosition();
        PlayerFrame frame = idleAnimation.getKeyFrame(stateTime);
        float scale = VISUAL_HEIGHT / frame.sourceHeight;
        float drawWidth = frame.region.getRegionWidth() * scale;
        float drawHeight = frame.region.getRegionHeight() * scale;
        float playerCenterX = position.x + Player.WIDTH * 0.5f;
        float drawX = playerCenterX - frame.footAnchorFromRegionLeft * scale;
        float drawY = position.y - frame.footPadding * scale;

        batch.draw(frame.region, drawX, drawY, drawWidth, drawHeight);
    }

    public void dispose() {
        idleTexture.dispose();
    }

    private Array<PlayerFrame> loadFrames() {
        Pixmap pixmap = new Pixmap(Gdx.files.internal(IDLE_SHEET));
        int frameWidth = pixmap.getWidth() / IDLE_COLUMNS;
        int frameHeight = pixmap.getHeight() / IDLE_ROWS;
        Array<PlayerFrame> frames = new Array<PlayerFrame>(IDLE_COLUMNS * IDLE_ROWS);

        for (int row = 0; row < IDLE_ROWS; row++) {
            for (int column = 0; column < IDLE_COLUMNS; column++) {
                TextureRegion region = new TextureRegion(
                    idleTexture,
                    column * frameWidth,
                    row * frameHeight,
                    frameWidth,
                    frameHeight
                );
                frames.add(new PlayerFrame(
                    region,
                    frameHeight,
                    findFootAnchorX(pixmap, column * frameWidth, row * frameHeight, frameWidth, frameHeight),
                    findFootPadding(pixmap, column * frameWidth, row * frameHeight, frameWidth, frameHeight)
                ));
            }
        }

        pixmap.dispose();
        return frames;
    }

    private float findFootAnchorX(Pixmap pixmap, int startX, int startY, int width, int height) {
        int bottomY = findBottomY(pixmap, startX, startY, width, height);
        int topY = Math.max(0, bottomY - Math.max(8, (int)(height * 0.12f)));
        int footMinX = width;
        int footMaxX = -1;

        for (int y = topY; y <= bottomY; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = pixmap.getPixel(startX + x, startY + y) & 0xff;
                if (alpha > 32) {
                    footMinX = Math.min(footMinX, x);
                    footMaxX = Math.max(footMaxX, x);
                }
            }
        }

        if (footMaxX < footMinX) {
            return width * 0.5f;
        }

        return (footMinX + footMaxX) * 0.5f;
    }

    private float findFootPadding(Pixmap pixmap, int startX, int startY, int width, int height) {
        return height - findBottomY(pixmap, startX, startY, width, height) - 1f;
    }

    private int findBottomY(Pixmap pixmap, int startX, int startY, int width, int height) {
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int alpha = pixmap.getPixel(startX + x, startY + y) & 0xff;
                if (alpha > 8) {
                    return y;
                }
            }
        }

        return height - 1;
    }

    private static class PlayerFrame {
        private final TextureRegion region;
        private final float sourceHeight;
        private final float footAnchorFromRegionLeft;
        private final float footPadding;

        private PlayerFrame(TextureRegion region, float sourceHeight, float footAnchorFromRegionLeft, float footPadding) {
            this.region = region;
            this.sourceHeight = sourceHeight;
            this.footAnchorFromRegionLeft = footAnchorFromRegionLeft;
            this.footPadding = footPadding;
        }
    }
}

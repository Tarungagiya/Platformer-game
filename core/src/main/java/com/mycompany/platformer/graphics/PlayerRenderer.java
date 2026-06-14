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
    private static final String IDLE_IMAGE_PATTERN = "Player/Idle/idle%02d.png";
    private static final String RUN_IMAGE_PATTERN = "Player/Run/run%02d.png";
    private static final String[] JUMP_IMAGES = {
            "Player/Jump/jump_start01.png",
            "Player/Jump/jump_start02.png",
            "Player/Jump/jump_mid01.png",
            "Player/Jump/jump_mid02.png",
            "Player/Jump/jump_mid03.png",
            "Player/Jump/jump_mid04.png",
            "Player/Jump/jump_landing.png"
    };
    private static final int IDLE_FRAME_COUNT = 9;
    private static final int RUN_FRAME_COUNT = 8;
    private static final float IDLE_FRAME_DURATION = 0.12f;
    private static final float RUN_FRAME_DURATION = 0.11f;
    private static final float JUMP_FRAME_DURATION = 0.1f;
    private static final float RUN_SPEED_THRESHOLD = 1f;
    private static final float VISUAL_HEIGHT = Player.HEIGHT;

    private final Array<Texture> textures = new Array<Texture>(IDLE_FRAME_COUNT + RUN_FRAME_COUNT + JUMP_IMAGES.length);
    private final Animation<PlayerFrame> idleAnimation;
    private final Animation<PlayerFrame> runAnimation;
    private final Animation<PlayerFrame> jumpAnimation;
    private float stateTime;
    private PlayerState currentState = PlayerState.IDLE;
    private boolean facingLeft;

    public PlayerRenderer() {
        idleAnimation = new Animation<PlayerFrame>(
                IDLE_FRAME_DURATION,
                loadNumberedFrames(IDLE_IMAGE_PATTERN, IDLE_FRAME_COUNT),
                Animation.PlayMode.LOOP);
        runAnimation = new Animation<PlayerFrame>(
                RUN_FRAME_DURATION,
                loadNumberedFrames(RUN_IMAGE_PATTERN, RUN_FRAME_COUNT),
                Animation.PlayMode.LOOP);
        jumpAnimation = new Animation<PlayerFrame>(
                JUMP_FRAME_DURATION,
                loadNamedFrames(JUMP_IMAGES),
                Animation.PlayMode.NORMAL);
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void draw(SpriteBatch batch, Player player) {
        Vector2 position = player.getPosition();
        float horizontalVelocity = player.getHorizontalVelocity();
        boolean running = Math.abs(horizontalVelocity) > RUN_SPEED_THRESHOLD;
        boolean jumping = !player.isGrounded();
        if (horizontalVelocity < -RUN_SPEED_THRESHOLD) {
            facingLeft = true;
        } else if (horizontalVelocity > RUN_SPEED_THRESHOLD) {
            facingLeft = false;
        }

        PlayerState nextState = jumping ? PlayerState.JUMP : running ? PlayerState.RUN : PlayerState.IDLE;
        if (nextState != currentState) {
            currentState = nextState;
            stateTime = 0f;
        }

        PlayerFrame frame;
        if (currentState == PlayerState.JUMP) {
            frame = jumpAnimation.getKeyFrame(stateTime);
        } else if (currentState == PlayerState.RUN) {
            frame = runAnimation.getKeyFrame(stateTime);
        } else {
            frame = idleAnimation.getKeyFrame(stateTime);
        }
        float scale = VISUAL_HEIGHT / frame.sourceHeight;
        float drawWidth = frame.region.getRegionWidth() * scale;
        float drawHeight = frame.region.getRegionHeight() * scale;
        float playerCenterX = position.x + Player.WIDTH * 0.5f;
        float footAnchor = facingLeft
                ? frame.region.getRegionWidth() - frame.footAnchorFromRegionLeft
                : frame.footAnchorFromRegionLeft;
        float drawX = playerCenterX - footAnchor * scale;
        float drawY = position.y - frame.footPadding * scale;

        if (facingLeft) {
            batch.draw(frame.region, drawX + drawWidth, drawY, -drawWidth, drawHeight);
        } else {
            batch.draw(frame.region, drawX, drawY, drawWidth, drawHeight);
        }
    }

    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
    }

    private Array<PlayerFrame> loadNumberedFrames(String pattern, int frameCount) {
        Array<PlayerFrame> frames = new Array<PlayerFrame>(frameCount);
        for (int frameNumber = 1; frameNumber <= frameCount; frameNumber++) {
            frames.add(loadFrame(String.format(pattern, frameNumber)));
        }

        return frames;
    }

    private Array<PlayerFrame> loadNamedFrames(String[] paths) {
        Array<PlayerFrame> frames = new Array<PlayerFrame>(paths.length);
        for (String path : paths) {
            frames.add(loadFrame(path));
        }

        return frames;
    }

    private PlayerFrame loadFrame(String path) {
        Texture texture = loadTexture(path);
        textures.add(texture);
        return loadFrame(path, texture);
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path), true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        return texture;
    }

    private PlayerFrame loadFrame(String path, Texture texture) {
        Pixmap pixmap = new Pixmap(Gdx.files.internal(path));
        int[] bounds = findVisibleBounds(pixmap);
        int x = bounds[0];
        int y = bounds[1];
        int width = bounds[2];
        int height = bounds[3];
        PlayerFrame frame = new PlayerFrame(
                new TextureRegion(texture, x, y, width, height),
                height,
                findFootAnchorX(pixmap, x, y, width, height),
                findFootPadding(pixmap, x, y, width, height));
        pixmap.dispose();
        return frame;
    }

    private int[] findVisibleBounds(Pixmap pixmap) {
        int minX = pixmap.getWidth();
        int minY = pixmap.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < pixmap.getHeight(); y++) {
            for (int x = 0; x < pixmap.getWidth(); x++) {
                int alpha = pixmap.getPixel(x, y) & 0xff;
                if (alpha > 8) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return new int[] { 0, 0, pixmap.getWidth(), pixmap.getHeight() };
        }

        return new int[] { minX, minY, maxX - minX + 1, maxY - minY + 1 };
    }

    private float findFootAnchorX(Pixmap pixmap, int startX, int startY, int width, int height) {
        int bottomY = findBottomY(pixmap, startX, startY, width, height);
        int topY = Math.max(0, bottomY - Math.max(8, (int) (height * 0.12f)));
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

        private PlayerFrame(TextureRegion region, float sourceHeight, float footAnchorFromRegionLeft,
                float footPadding) {
            this.region = region;
            this.sourceHeight = sourceHeight;
            this.footAnchorFromRegionLeft = footAnchorFromRegionLeft;
            this.footPadding = footPadding;
        }
    }

    private enum PlayerState {
        IDLE,
        RUN,
        JUMP
    }
}

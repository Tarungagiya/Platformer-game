package com.mycompany.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mycompany.platformer.entities.Player;
import com.mycompany.platformer.graphics.PlayerRenderer;
import com.mycompany.platformer.ui.TouchControls;

public class MainGame extends ApplicationAdapter {
    private static final float WORLD_WIDTH = 1900f;
    private static final float WORLD_HEIGHT = 760f;
    private static final float VIEW_WIDTH = 800f;
    private static final float VIEW_HEIGHT = 480f;
    private static final float SPAWN_X = 82f;
    private static final float SPAWN_Y = 116f;

    private final Array<Rectangle> platforms = new Array<Rectangle>();
    private final Array<GravityZone> zones = new Array<GravityZone>();
    private final Array<GravityBody> enemies = new Array<GravityBody>();
    private final Array<GravityBody> bullets = new Array<GravityBody>();
    private final Array<GravityBody> debris = new Array<GravityBody>();
    private final Array<Rectangle> hazards = new Array<Rectangle>();
    private final Rectangle goal = new Rectangle(1784f, 556f, 82f, 120f);
    private final Color tmpColor = new Color();
    private final Vector2 tmp = new Vector2();

    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont hudFont;
    private OrthographicCamera worldCamera;
    private OrthographicCamera uiCamera;
    private Viewport worldViewport;
    private Viewport uiViewport;
    private TouchControls touchControls;
    private Player player;
    private PlayerRenderer playerRenderer;

    private float worldTime;
    private float shootTimer;
    private float cameraShake;
    private float cameraTilt;
    private float goalPulse;
    private String currentTip = "follow the blue gravity arrows";
    private boolean finished;

    @Override
    public void create() {
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        hudFont = new BitmapFont();
        hudFont.getData().setScale(1.04f);
        hudFont.setUseIntegerPositions(false);

        worldCamera = new OrthographicCamera();
        worldViewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, worldCamera);
        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);
        touchControls = new TouchControls(uiViewport);
        Gdx.input.setInputProcessor(touchControls);

        player = new Player(SPAWN_X, SPAWN_Y);
        playerRenderer = new PlayerRenderer();
        buildLevel();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        update(delta);

        ScreenUtils.clear(0.012f, 0.014f, 0.026f, 1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawWorld();
        drawHud();
        touchControls.draw(shapes);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        uiViewport.update(width, height, true);
        touchControls.resize();
    }

    @Override
    public void dispose() {
        playerRenderer.dispose();
        hudFont.dispose();
        batch.dispose();
        shapes.dispose();
    }

    private void buildLevel() {
        platforms.clear();
        zones.clear();
        enemies.clear();
        bullets.clear();
        debris.clear();
        hazards.clear();

        addPlatform(0f, 54f, 286f, 34f);
        addPlatform(330f, 54f, 210f, 34f);
        addPlatform(596f, 118f, 116f, 26f);
        addPlatform(768f, 208f, 150f, 26f);
        addPlatform(1010f, 150f, 172f, 26f);
        addPlatform(1234f, 248f, 132f, 24f);
        addPlatform(1476f, 372f, 126f, 24f);
        addPlatform(1668f, 536f, 200f, 28f);

        addPlatform(0f, 0f, WORLD_WIDTH, 24f);
        addPlatform(0f, WORLD_HEIGHT - 24f, WORLD_WIDTH, 24f);
        addPlatform(0f, 0f, 24f, WORLD_HEIGHT);
        addPlatform(WORLD_WIDTH - 24f, 0f, 24f, WORLD_HEIGHT);

        addPlatform(420f, 88f, 26f, 286f);
        addPlatform(566f, 288f, 26f, 236f);
        addPlatform(742f, 314f, 34f, 260f);
        addPlatform(940f, 86f, 28f, 250f);
        addPlatform(1138f, 176f, 28f, 290f);
        addPlatform(1388f, 250f, 28f, 292f);
        addPlatform(1620f, 432f, 28f, 210f);

        hazards.add(new Rectangle(296f, 32f, 70f, 22f));
        hazards.add(new Rectangle(930f, 32f, 90f, 22f));
        hazards.add(new Rectangle(1516f, 344f, 70f, 22f));

        zones.add(new GravityZone("INVERT", 310f, 78f, 176f, 218f, new Vector2(0f, 1f), 690f, ZoneMode.OVERRIDE, colorForGravity(1)));
        zones.add(new GravityZone("LEFT", 540f, 80f, 178f, 300f, new Vector2(-1f, 0f), 740f, ZoneMode.OVERRIDE, colorForGravity(2)));
        zones.add(new GravityZone("RIGHT", 720f, 220f, 198f, 276f, new Vector2(1f, 0f), 740f, ZoneMode.OVERRIDE, colorForGravity(3)));
        zones.add(new GravityZone("NULL", 970f, 104f, 210f, 224f, new Vector2(0f, 0f), 0f, ZoneMode.NULLIFY, colorForGravity(5)));
        zones.add(new GravityZone("DIAGONAL", 1204f, 174f, 214f, 292f, new Vector2(1f, -1f), 820f, ZoneMode.OVERRIDE, colorForGravity(4)));
        zones.add(new GravityZone("ROTATE", 1460f, 292f, 236f, 292f, new Vector2(0f, -1f), 850f, ZoneMode.ROTATE, new Color(1f, 0.24f, 0.78f, 1f)));
        zones.add(new GravityZone("CORE", 1692f, 474f, 180f, 184f, new Vector2(0f, -1f), 790f, ZoneMode.ROTATE, new Color(0.95f, 0.18f, 0.92f, 1f)));

        enemies.add(GravityBody.enemy(EnemyType.WALL_CRAWLER, 452f, 126f, 32f, 28f, new Vector2(1f, 0f), 540f, 118f, 214f));
        enemies.add(GravityBody.enemy(EnemyType.CEILING_SNIPER, 658f, 698f, 44f, 24f, new Vector2(0f, 1f), 720f, 0f, 0f));
        enemies.add(GravityBody.enemy(EnemyType.ORBIT_DRONE, 1080f, 238f, 30f, 30f, new Vector2(0f, -1f), 0f, 62f, 38f));
        enemies.add(GravityBody.enemy(EnemyType.GRAVITY_LEECH, 1308f, 302f, 34f, 34f, new Vector2(1f, -1f), 530f, 70f, 0f));
        enemies.add(GravityBody.enemy(EnemyType.SPIKE_ROLLER, 1538f, 396f, 42f, 42f, new Vector2(0f, -1f), 980f, 104f, 0f));
        enemies.add(GravityBody.enemy(EnemyType.ORBIT_DRONE, 1740f, 610f, 34f, 34f, new Vector2(0f, -1f), 0f, 58f, 58f));

        for (int i = 0; i < 110; i++) {
            float x = 70f + (i * 53f) % (WORLD_WIDTH - 140f);
            float y = 90f + (i * 97f) % (WORLD_HEIGHT - 170f);
            GravityBody flake = GravityBody.body(EnemyType.DEBRIS, x, y, MathUtils.random(2.5f, 7f), MathUtils.random(2.5f, 7f), randomGravity(), MathUtils.random(16f, 72f));
            flake.velocity.set(MathUtils.sin(i * 1.7f) * 16f, MathUtils.cos(i * 2.1f) * 16f);
            debris.add(flake);
        }
    }

    private void update(float delta) {
        worldTime += delta;
        goalPulse += delta;
        shootTimer = Math.max(0f, shootTimer - delta);
        cameraShake = Math.max(0f, cameraShake - delta * 2.6f);

        Player.Controls controls = readControls();
        if (touchControls.consumeReset() || Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            respawnPlayer();
        }
        applyZones(player);
        player.update(delta, controls, platforms, WORLD_WIDTH, WORLD_HEIGHT);
        playerRenderer.update(delta);
        currentTip = tipForPosition(player.getPosition().x);

        if (player.didGravityShift()) {
            cameraShake = 0.12f;
            cameraTilt += player.getGravityVector().x * 7f;
        }
        if (controls.shoot && shootTimer <= 0f) {
            spawnPlayerBullet();
        }
        if (controls.copyGravity) {
            copyNearestGravity();
        }

        updateEnemies(delta);
        updateBodies(bullets, delta, true);
        updateBodies(debris, delta, false);
        updateHazardsAndGoal();
        updateCamera(delta);
    }

    private Player.Controls readControls() {
        Player.Controls controls = new Player.Controls();
        controls.movement = touchControls.getMovement();
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            controls.movement -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            controls.movement += 1f;
        }
        controls.movement = MathUtils.clamp(controls.movement, -1f, 1f);
        controls.jump = touchControls.consumeJump() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W);
        controls.dash = touchControls.consumeDash() || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT);
        controls.groundPound = touchControls.isGroundPoundPressed() || Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        controls.rotateLeft = touchControls.consumeRotateLeft() || Gdx.input.isKeyJustPressed(Input.Keys.Q);
        controls.rotateRight = touchControls.consumeRotateRight() || Gdx.input.isKeyJustPressed(Input.Keys.E);
        controls.copyGravity = touchControls.consumeCopyGravity() || Gdx.input.isKeyJustPressed(Input.Keys.C);
        controls.shoot = touchControls.isShootPressed() || Gdx.input.isKeyPressed(Input.Keys.J) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        return controls;
    }

    private void applyZones(Player target) {
        Rectangle playerBounds = target.getBounds();
        GravityZone best = null;
        for (GravityZone zone : zones) {
            if (zone.area.overlaps(playerBounds)) {
                best = zone;
            }
        }
        if (best == null) {
            return;
        }
        if (best.mode == ZoneMode.ROTATE) {
            float angle = worldTime * (best.name.equals("CORE") ? 92f : 64f);
            target.setGravity(new Vector2(MathUtils.cosDeg(angle), MathUtils.sinDeg(angle)), best.strength);
        } else {
            target.setGravity(best.direction, best.strength);
        }
    }

    private void updateEnemies(float delta) {
        for (GravityBody enemy : enemies) {
            enemy.age += delta;
            if (enemy.dead) {
                continue;
            }

            if (enemy.type == EnemyType.WALL_CRAWLER) {
                enemy.position.y = enemy.homeY + MathUtils.sin(enemy.age * 1.8f) * enemy.rangeY;
                enemy.position.x = enemy.homeX;
                enemy.velocity.set(0f, MathUtils.cos(enemy.age * 1.8f) * enemy.rangeY * 1.8f);
            } else if (enemy.type == EnemyType.CEILING_SNIPER) {
                enemy.shootTimer -= delta;
                enemy.position.x = enemy.homeX + MathUtils.sin(enemy.age * 0.7f) * 18f;
                if (enemy.shootTimer <= 0f && enemy.position.dst(player.getPosition()) < 620f) {
                    spawnEnemyBullet(enemy);
                    enemy.shootTimer = 1.45f;
                }
            } else if (enemy.type == EnemyType.ORBIT_DRONE) {
                float angle = enemy.age * 96f + enemy.homeX * 0.13f;
                enemy.position.set(enemy.homeX + MathUtils.cosDeg(angle) * enemy.rangeX, enemy.homeY + MathUtils.sinDeg(angle) * enemy.rangeY);
                enemy.gravityVector.set(MathUtils.cosDeg(angle + 90f), MathUtils.sinDeg(angle + 90f)).nor();
            } else if (enemy.type == EnemyType.GRAVITY_LEECH) {
                Vector2 toPlayer = tmp.set(player.getPosition()).sub(enemy.position);
                if (toPlayer.len2() < 230f * 230f) {
                    enemy.velocity.mulAdd(toPlayer.nor(), 155f * delta);
                } else {
                    enemy.velocity.x += MathUtils.sin(enemy.age * 1.2f) * 30f * delta;
                    enemy.velocity.y += MathUtils.cos(enemy.age * 0.9f) * 25f * delta;
                }
                enemy.velocity.limit(105f);
                enemy.update(delta);
            } else if (enemy.type == EnemyType.SPIKE_ROLLER) {
                enemy.position.x = enemy.homeX + MathUtils.sin(enemy.age * 1.65f) * enemy.rangeX;
                enemy.position.y = enemy.homeY;
                enemy.velocity.x = MathUtils.cos(enemy.age * 1.65f) * enemy.rangeX * 1.65f;
            }

            if (enemy.type != EnemyType.WALL_CRAWLER && enemy.type != EnemyType.CEILING_SNIPER && enemy.type != EnemyType.ORBIT_DRONE && enemy.type != EnemyType.SPIKE_ROLLER) {
                clampBody(enemy);
            }

            if (enemy.bounds().overlaps(player.getBounds())) {
                handleEnemyContact(enemy);
            }
        }
    }

    private void updateBodies(Array<GravityBody> bodies, float delta, boolean removeOutside) {
        for (int i = bodies.size - 1; i >= 0; i--) {
            GravityBody body = bodies.get(i);
            applyZones(body);
            body.update(delta);
            if (removeOutside && isOutsideWorld(body)) {
                bodies.removeIndex(i);
                continue;
            }
            if (body.type == EnemyType.PLAYER_BULLET) {
                for (GravityBody enemy : enemies) {
                    if (!enemy.dead && enemy.type != EnemyType.SPIKE_ROLLER && enemy.bounds().overlaps(body.bounds())) {
                        body.dead = true;
                        enemy.dead = true;
                        burstDebris(enemy.position.x, enemy.position.y, enemy.gravityVector);
                        break;
                    }
                }
            } else if (body.type == EnemyType.ENEMY_BULLET && body.bounds().overlaps(player.getBounds())) {
                if (player.damage(8)) {
                    cameraShake = 0.12f;
                }
                body.dead = true;
            }
            if (body.dead) {
                bodies.removeIndex(i);
            }
        }
    }

    private void applyZones(GravityBody body) {
        for (GravityZone zone : zones) {
            if (!zone.area.contains(body.position.x + body.width * 0.5f, body.position.y + body.height * 0.5f)) {
                continue;
            }
            if (zone.mode == ZoneMode.ROTATE) {
                float angle = worldTime * 88f + body.position.x * 0.16f;
                body.gravityVector.set(MathUtils.cosDeg(angle), MathUtils.sinDeg(angle));
                body.gravityStrength = zone.strength;
            } else {
                body.gravityVector.set(zone.direction);
                body.gravityStrength = zone.strength;
            }
        }
    }

    private void updateHazardsAndGoal() {
        for (Rectangle hazard : hazards) {
            if (hazard.overlaps(player.getBounds()) && player.damage(18)) {
                cameraShake = 0.22f;
            }
        }
        if (player.getHealth() <= 0) {
            respawnPlayer();
        }
        if (goal.overlaps(player.getBounds())) {
            finished = true;
            player.heal(1);
            currentTip = "core stabilized - you mastered personal gravity";
        }
    }

    private void handleEnemyContact(GravityBody enemy) {
        if (enemy.type == EnemyType.GRAVITY_LEECH) {
            player.copyGravity(enemy.gravityVector, enemy.gravityStrength);
            if (player.damage(6)) {
                cameraShake = 0.18f;
            }
            return;
        }
        if (enemy.type == EnemyType.ORBIT_DRONE) {
            player.copyGravity(enemy.gravityVector, 820f);
            if (player.damage(7)) {
                cameraShake = 0.13f;
            }
            return;
        }
        if (player.damage(enemy.type == EnemyType.SPIKE_ROLLER ? 24 : 10)) {
            cameraShake = enemy.type == EnemyType.SPIKE_ROLLER ? 0.28f : 0.16f;
        }
    }

    private void spawnPlayerBullet() {
        Vector2 gravity = player.getGravityVector();
        Vector2 tangent = gravity.isZero(0.001f) ? new Vector2(1f, 0f) : new Vector2(-gravity.y, gravity.x).nor();
        float dir = player.getVelocity().dot(tangent) < -8f ? -1f : 1f;
        GravityBody bullet = GravityBody.body(EnemyType.PLAYER_BULLET, player.getPosition().x + Player.WIDTH * 0.5f,
                player.getPosition().y + Player.HEIGHT * 0.5f, 10f, 10f, gravity, player.getGravityStrength() * 0.55f);
        bullet.velocity.set(tangent.scl(470f * dir)).mulAdd(gravity, -60f);
        bullets.add(bullet);
        shootTimer = 0.2f;
        player.markShoot();
    }

    private void spawnEnemyBullet(GravityBody shooter) {
        Vector2 aim = tmp.set(player.getPosition()).sub(shooter.position);
        if (aim.isZero(0.001f)) {
            aim.set(-1f, 0f);
        }
        aim.nor();
        GravityBody bullet = GravityBody.body(EnemyType.ENEMY_BULLET, shooter.position.x + shooter.width * 0.5f,
                shooter.position.y - 4f, 9f, 9f, shooter.gravityVector, shooter.gravityStrength * 0.86f);
        bullet.velocity.set(aim.scl(145f)).mulAdd(shooter.gravityVector, -120f);
        bullets.add(bullet);
    }

    private void copyNearestGravity() {
        GravityBody nearest = null;
        float nearestDistance = 150f * 150f;
        for (GravityBody enemy : enemies) {
            if (enemy.dead) {
                continue;
            }
            float distance = enemy.position.dst2(player.getPosition());
            if (distance < nearestDistance) {
                nearest = enemy;
                nearestDistance = distance;
            }
        }
        if (nearest != null) {
            player.copyGravity(nearest.gravityVector, nearest.gravityStrength);
            cameraShake = 0.10f;
        }
    }

    private void burstDebris(float x, float y, Vector2 gravity) {
        for (int i = 0; i < 10; i++) {
            GravityBody spark = GravityBody.body(EnemyType.DEBRIS, x, y, 4f, 4f, gravity, 60f);
            spark.velocity.set(MathUtils.cosDeg(i * 36f) * 110f, MathUtils.sinDeg(i * 36f) * 110f);
            debris.add(spark);
        }
    }

    private void respawnPlayer() {
        player.respawn(SPAWN_X, SPAWN_Y);
        buildLevel();
        finished = false;
        cameraShake = 0.30f;
        currentTip = "reset - read the arrows before moving";
    }

    private void updateCamera(float delta) {
        Vector2 target = player.getPosition();
        worldCamera.position.x = MathUtils.lerp(worldCamera.position.x, MathUtils.clamp(target.x, VIEW_WIDTH * 0.5f, WORLD_WIDTH - VIEW_WIDTH * 0.5f), delta * 4.2f);
        worldCamera.position.y = MathUtils.lerp(worldCamera.position.y, MathUtils.clamp(target.y + 28f, VIEW_HEIGHT * 0.5f, WORLD_HEIGHT - VIEW_HEIGHT * 0.5f), delta * 4.2f);
        cameraTilt = MathUtils.lerp(cameraTilt, player.getGravityVector().x * -4.5f, delta * 3f);
        worldCamera.up.set(MathUtils.sinDeg(cameraTilt), MathUtils.cosDeg(cameraTilt), 0f);
        worldCamera.direction.set(0f, 0f, -1f);
        if (cameraShake > 0f) {
            worldCamera.position.x += MathUtils.random(-cameraShake, cameraShake) * 16f;
            worldCamera.position.y += MathUtils.random(-cameraShake, cameraShake) * 16f;
        }
        worldCamera.update();
    }

    private void drawWorld() {
        shapes.setProjectionMatrix(worldViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawParallax();
        drawZones();
        drawPlatforms();
        drawHazards();
        drawDebris();
        drawGoal();
        drawEnemiesAndBullets();
        if (!player.isDamageFlashing()) {
            playerRenderer.draw(shapes, player, gravityColor(player.getGravityVector()));
        }
        shapes.end();
    }

    private void drawParallax() {
        for (int layer = 0; layer < 4; layer++) {
            float offset = worldCamera.position.x * (0.025f + layer * 0.015f);
            shapes.setColor(0.024f + layer * 0.008f, 0.032f + layer * 0.009f, 0.065f + layer * 0.018f, 1f);
            for (float x = -220f; x < WORLD_WIDTH + 300f; x += 190f) {
                float drawX = x - offset % 190f;
                shapes.rect(drawX, 110f + layer * 74f, 92f, 3f + layer);
                shapes.rect(drawX + 56f, 470f + layer * 36f, 124f, 2f + layer);
            }
        }
    }

    private void drawZones() {
        for (GravityZone zone : zones) {
            shapes.setColor(zone.color.r, zone.color.g, zone.color.b, 0.075f);
            shapes.rect(zone.area.x, zone.area.y, zone.area.width, zone.area.height);
            shapes.setColor(zone.color.r, zone.color.g, zone.color.b, 0.20f);
            shapes.rect(zone.area.x, zone.area.y, zone.area.width, 3f);
            shapes.rect(zone.area.x, zone.area.y + zone.area.height - 3f, zone.area.width, 3f);
            shapes.rect(zone.area.x, zone.area.y, 3f, zone.area.height);
            shapes.rect(zone.area.x + zone.area.width - 3f, zone.area.y, 3f, zone.area.height);

            for (int i = 0; i < 18; i++) {
                float px = zone.area.x + (i * 41f + worldTime * 26f) % zone.area.width;
                float py = zone.area.y + (i * 29f + worldTime * 17f) % zone.area.height;
                Vector2 dir = zone.mode == ZoneMode.ROTATE ? tmp.set(MathUtils.cos(worldTime * 1.5f + i), MathUtils.sin(worldTime * 1.5f + i)) : zone.direction;
                shapes.setColor(zone.color.r, zone.color.g, zone.color.b, 0.38f);
                if (dir.isZero(0.001f)) {
                    shapes.circle(px, py, 3f + MathUtils.sin(worldTime * 3f + i) * 1.5f);
                } else {
                    shapes.rectLine(px, py, px + dir.x * 18f, py + dir.y * 18f, 2f);
                    shapes.circle(px + dir.x * 18f, py + dir.y * 18f, 3f);
                }
            }
        }
    }

    private void drawPlatforms() {
        for (Rectangle platform : platforms) {
            shapes.setColor(0.048f, 0.058f, 0.082f, 1f);
            shapes.rect(platform.x, platform.y, platform.width, platform.height);
            shapes.setColor(0.15f, 0.20f, 0.26f, 1f);
            shapes.rect(platform.x, platform.y + platform.height - 5f, platform.width, 5f);
            shapes.setColor(0.08f, 0.62f, 0.72f, 0.28f);
            shapes.rect(platform.x + 8f, platform.y + platform.height - 2f, Math.max(0f, platform.width - 16f), 2f);
        }
    }

    private void drawHazards() {
        for (Rectangle hazard : hazards) {
            shapes.setColor(0.22f, 0.03f, 0.08f, 1f);
            shapes.rect(hazard.x, hazard.y, hazard.width, hazard.height);
            shapes.setColor(1f, 0.18f, 0.35f, 1f);
            for (float x = hazard.x; x < hazard.x + hazard.width; x += 16f) {
                shapes.triangle(x, hazard.y + hazard.height, x + 8f, hazard.y + hazard.height + 18f, x + 16f, hazard.y + hazard.height);
            }
        }
    }

    private void drawDebris() {
        for (GravityBody flake : debris) {
            if (isOutsideCamera(flake.position.x, flake.position.y, 80f)) {
                continue;
            }
            Color color = gravityColor(flake.gravityVector);
            shapes.setColor(color.r, color.g, color.b, 0.20f);
            shapes.circle(flake.position.x, flake.position.y, flake.width);
        }
    }

    private void drawGoal() {
        float pulse = 0.55f + MathUtils.sin(goalPulse * 3.2f) * 0.45f;
        shapes.setColor(0.95f, 0.16f, 0.86f, 0.12f + pulse * 0.08f);
        shapes.circle(goal.x + goal.width * 0.5f, goal.y + goal.height * 0.5f, 72f + pulse * 18f);
        shapes.setColor(0.95f, 0.16f, 0.86f, 0.85f);
        shapes.rectLine(goal.x + 18f, goal.y + goal.height * 0.5f, goal.x + goal.width - 18f, goal.y + goal.height * 0.5f, 5f);
        shapes.rectLine(goal.x + goal.width * 0.5f, goal.y + 18f, goal.x + goal.width * 0.5f, goal.y + goal.height - 18f, 5f);
        shapes.setColor(0.74f, 0.9f, 1f, 0.85f);
        shapes.circle(goal.x + goal.width * 0.5f, goal.y + goal.height * 0.5f, finished ? 20f : 12f);
    }

    private void drawEnemiesAndBullets() {
        for (GravityBody enemy : enemies) {
            if (!enemy.dead) {
                drawBody(enemy);
            }
        }
        for (GravityBody bullet : bullets) {
            Color color = gravityColor(bullet.gravityVector);
            shapes.setColor(color);
            shapes.circle(bullet.position.x, bullet.position.y, bullet.width * 0.5f);
            shapes.setColor(color.r, color.g, color.b, 0.18f);
            shapes.circle(bullet.position.x - bullet.gravityVector.x * 18f, bullet.position.y - bullet.gravityVector.y * 18f, bullet.width);
        }
    }

    private void drawBody(GravityBody body) {
        Color color = gravityColor(body.gravityVector);
        float cx = body.position.x + body.width * 0.5f;
        float cy = body.position.y + body.height * 0.5f;
        shapes.setColor(color.r, color.g, color.b, 0.18f);
        shapes.circle(cx - body.gravityVector.x * 18f, cy - body.gravityVector.y * 18f, Math.max(body.width, body.height));
        shapes.setColor(0.07f, 0.078f, 0.105f, 1f);
        shapes.rect(body.position.x, body.position.y, body.width, body.height);
        shapes.setColor(color);

        if (body.type == EnemyType.WALL_CRAWLER) {
            shapes.rectLine(cx - 18f, cy - 14f, cx + 18f, cy + 14f, 4f);
            shapes.rectLine(cx - 18f, cy + 14f, cx + 18f, cy - 14f, 4f);
            shapes.rectLine(cx - 22f, cy, cx - 36f, cy, 3f);
        } else if (body.type == EnemyType.CEILING_SNIPER) {
            shapes.triangle(cx - 24f, cy + 12f, cx + 24f, cy + 12f, cx, cy - 20f);
            shapes.rectLine(cx, cy - 6f, cx - 38f, cy - 18f, 4f);
        } else if (body.type == EnemyType.ORBIT_DRONE) {
            shapes.circle(cx, cy, body.width * 0.55f);
            shapes.setColor(0.015f, 0.018f, 0.03f, 1f);
            shapes.circle(cx, cy, body.width * 0.28f);
        } else if (body.type == EnemyType.GRAVITY_LEECH) {
            shapes.circle(cx, cy, body.width * 0.50f);
            shapes.setColor(0.015f, 0.018f, 0.03f, 1f);
            shapes.circle(cx, cy, body.width * 0.23f);
            shapes.setColor(color.r, color.g, color.b, 0.42f);
            shapes.circle(cx, cy, 72f + MathUtils.sin(body.age * 4f) * 7f);
        } else if (body.type == EnemyType.SPIKE_ROLLER) {
            shapes.circle(cx, cy, body.width * 0.5f);
            for (int i = 0; i < 8; i++) {
                float angle = i * 45f + worldTime * 210f;
                shapes.triangle(cx + MathUtils.cosDeg(angle) * 18f, cy + MathUtils.sinDeg(angle) * 18f,
                        cx + MathUtils.cosDeg(angle + 16f) * 30f, cy + MathUtils.sinDeg(angle + 16f) * 30f,
                        cx + MathUtils.cosDeg(angle - 16f) * 30f, cy + MathUtils.sinDeg(angle - 16f) * 30f);
            }
        }
    }

    private void drawHud() {
        shapes.setProjectionMatrix(uiViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float h = uiViewport.getWorldHeight();
        shapes.setColor(0.012f, 0.014f, 0.026f, 0.72f);
        shapes.rect(18f, h - 96f, 430f, 78f);
        shapes.setColor(0.09f, 0.13f, 0.17f, 1f);
        shapes.rect(32f, h - 50f, 190f, 12f);
        tmpColor.set(gravityColor(player.getGravityVector()));
        shapes.setColor(tmpColor);
        shapes.rect(32f, h - 50f, 190f * player.getHealthRatio(), 12f);
        shapes.circle(414f, h - 56f, 18f);
        Vector2 gravity = player.getGravityVector();
        shapes.setColor(0.012f, 0.014f, 0.026f, 1f);
        shapes.rectLine(414f, h - 56f, 414f + gravity.x * 18f, h - 56f + gravity.y * 18f, 4f);
        shapes.end();

        batch.setProjectionMatrix(uiViewport.getCamera().combined);
        batch.begin();
        hudFont.setColor(0.84f, 0.93f, 1f, 1f);
        hudFont.draw(batch, "GRAVITY IS PERSONAL", 32f, h - 24f);
        hudFont.setColor(tmpColor);
        hudFont.draw(batch, stateLabel(), 32f, h - 64f);
        hudFont.setColor(0.66f, 0.72f, 0.8f, 1f);
        hudFont.draw(batch, currentTip, 32f, h - 82f);
        hudFont.draw(batch, "A/D move | Space jump | Shift dash | Q/E rotate | C copy | J shoot | R reset", 32f, 26f);
        batch.end();

    }

    private String stateLabel() {
        if (finished) {
            return "CORE STABLE";
        }
        Vector2 gravity = player.getGravityVector();
        String degrees = gravity.isZero(0.001f) ? "zero" : Math.round(gravity.angleDeg()) + " deg";
        return player.getState().name().replace('_', ' ') + "  personal gravity " + degrees;
    }

    private String tipForPosition(float x) {
        if (x < 290f) {
            return "lesson 1: your jump goes against your own gravity";
        }
        if (x < 530f) {
            return "green zone: falling upward is still falling";
        }
        if (x < 735f) {
            return "purple zone: walk the wall, jump sideways";
        }
        if (x < 970f) {
            return "yellow zone: rightward gravity makes the shaft a floor";
        }
        if (x < 1200f) {
            return "zero gravity: dash and bullets keep their drift";
        }
        if (x < 1450f) {
            return "diagonal gravity: platforms become sloped decisions";
        }
        if (x < 1700f) {
            return "rotating gravity: wait, then commit";
        }
        return "stabilize the living gravity core";
    }

    private void addPlatform(float x, float y, float width, float height) {
        platforms.add(new Rectangle(x, y, width, height));
    }

    private void clampBody(GravityBody body) {
        body.position.x = MathUtils.clamp(body.position.x, 30f, WORLD_WIDTH - body.width - 30f);
        body.position.y = MathUtils.clamp(body.position.y, 32f, WORLD_HEIGHT - body.height - 32f);
    }

    private boolean isOutsideWorld(GravityBody body) {
        return body.position.x < -100f || body.position.x > WORLD_WIDTH + 100f || body.position.y < -100f || body.position.y > WORLD_HEIGHT + 100f;
    }

    private boolean isOutsideCamera(float x, float y, float margin) {
        return x < worldCamera.position.x - VIEW_WIDTH * 0.5f - margin
                || x > worldCamera.position.x + VIEW_WIDTH * 0.5f + margin
                || y < worldCamera.position.y - VIEW_HEIGHT * 0.5f - margin
                || y > worldCamera.position.y + VIEW_HEIGHT * 0.5f + margin;
    }

    private Color gravityColor(Vector2 gravity) {
        if (gravity.isZero(0.001f)) {
            return colorForGravity(5);
        }
        float angle = gravity.angleDeg();
        if (angle < 0f) {
            angle += 360f;
        }
        if (angle > 225f && angle < 315f) {
            return colorForGravity(0);
        }
        if (angle > 45f && angle < 135f) {
            return colorForGravity(1);
        }
        if (angle >= 135f && angle <= 225f) {
            return colorForGravity(2);
        }
        if (angle <= 45f || angle >= 315f) {
            return colorForGravity(3);
        }
        return colorForGravity(4);
    }

    private Color colorForGravity(int index) {
        switch (index) {
            case 0:
                return new Color(0.16f, 0.55f, 1f, 1f);
            case 1:
                return new Color(0.18f, 0.95f, 0.47f, 1f);
            case 2:
                return new Color(0.62f, 0.23f, 1f, 1f);
            case 3:
                return new Color(1f, 0.9f, 0.18f, 1f);
            case 4:
                return new Color(1f, 0.42f, 0.22f, 1f);
            default:
                return new Color(0.74f, 0.9f, 1f, 1f);
        }
    }

    private Vector2 randomGravity() {
        int option = MathUtils.random(5);
        if (option == 0) {
            return new Vector2(0f, -1f);
        }
        if (option == 1) {
            return new Vector2(0f, 1f);
        }
        if (option == 2) {
            return new Vector2(-1f, 0f);
        }
        if (option == 3) {
            return new Vector2(1f, 0f);
        }
        if (option == 4) {
            return new Vector2(1f, -1f).nor();
        }
        return new Vector2();
    }

    private static class GravityZone {
        private final String name;
        private final Rectangle area;
        private final Vector2 direction;
        private final float strength;
        private final ZoneMode mode;
        private final Color color;

        private GravityZone(String name, float x, float y, float width, float height, Vector2 direction, float strength, ZoneMode mode, Color color) {
            this.name = name;
            this.area = new Rectangle(x, y, width, height);
            this.direction = direction.isZero(0.001f) ? new Vector2() : new Vector2(direction).nor();
            this.strength = strength;
            this.mode = mode;
            this.color = color;
        }
    }

    private static class GravityBody {
        private EnemyType type;
        private final Vector2 position = new Vector2();
        private final Vector2 velocity = new Vector2();
        private final Vector2 gravityVector = new Vector2();
        private final Rectangle rectangle = new Rectangle();
        private float gravityStrength;
        private float width;
        private float height;
        private float homeX;
        private float homeY;
        private float rangeX;
        private float rangeY;
        private float shootTimer = 0.8f;
        private float age;
        private boolean dead;

        private static GravityBody enemy(EnemyType type, float x, float y, float width, float height, Vector2 gravityVector, float gravityStrength, float rangeX, float rangeY) {
            GravityBody body = body(type, x, y, width, height, gravityVector, gravityStrength);
            body.homeX = x;
            body.homeY = y;
            body.rangeX = rangeX;
            body.rangeY = rangeY;
            return body;
        }

        private static GravityBody body(EnemyType type, float x, float y, float width, float height, Vector2 gravityVector, float gravityStrength) {
            GravityBody body = new GravityBody();
            body.type = type;
            body.position.set(x, y);
            body.width = width;
            body.height = height;
            body.gravityVector.set(gravityVector);
            if (!body.gravityVector.isZero(0.001f)) {
                body.gravityVector.nor();
            }
            body.gravityStrength = gravityStrength;
            body.homeX = x;
            body.homeY = y;
            return body;
        }

        private GravityBody() {
            type = EnemyType.DEBRIS;
        }

        private void update(float delta) {
            velocity.mulAdd(gravityVector, gravityStrength * delta);
            velocity.scl(1f - Math.min(0.075f, delta * 0.65f));
            position.mulAdd(velocity, delta);
        }

        private Rectangle bounds() {
            return rectangle.set(position.x, position.y, width, height);
        }
    }

    private enum ZoneMode {
        OVERRIDE,
        ROTATE,
        NULLIFY
    }

    private enum EnemyType {
        WALL_CRAWLER,
        CEILING_SNIPER,
        ORBIT_DRONE,
        GRAVITY_LEECH,
        SPIKE_ROLLER,
        PLAYER_BULLET,
        ENEMY_BULLET,
        DEBRIS
    }
}

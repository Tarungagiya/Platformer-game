package com.mycompany.platformer.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Player {
    public static final float WIDTH = 34f;
    public static final float HEIGHT = 58f;
    public static final int MAX_HEALTH = 100;

    private static final float MOVE_SPEED = 220f;
    private static final float AIR_CONTROL = 560f;
    private static final float JUMP_SPEED = 360f;
    private static final float DASH_SPEED = 500f;
    private static final float DEFAULT_GRAVITY = 760f;
    private static final float GROUND_POUND_SPEED = 660f;
    private static final float WALL_CLING_MAX_FALL = 70f;
    private static final float ROTATION_STEP = 45f;
    private static final float DASH_COOLDOWN = 0.35f;
    private static final float ROTATE_COOLDOWN = 0.18f;
    private static final float DAMAGE_COOLDOWN = 0.85f;

    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 gravityVector = new Vector2(0f, -1f);
    private final Vector2 desiredGravity = new Vector2(0f, -1f);
    private final Rectangle bounds = new Rectangle();

    private float gravityStrength = DEFAULT_GRAVITY;
    private float desiredGravityStrength = DEFAULT_GRAVITY;
    private int health = MAX_HEALTH;
    private boolean grounded;
    private boolean wallClinging;
    private boolean gravityShifted;
    private float dashTimer;
    private float rotateTimer;
    private float copyTimer;
    private float shootPoseTimer;
    private float damageTimer;
    private float animationTimer;
    private State state = State.IDLE;

    public Player(float x, float y) {
        position.set(x, y);
        updateBounds();
    }

    public void update(float delta, Controls controls, Array<Rectangle> platforms, float worldWidth, float worldHeight) {
        animationTimer += delta;
        dashTimer = Math.max(0f, dashTimer - delta);
        rotateTimer = Math.max(0f, rotateTimer - delta);
        copyTimer = Math.max(0f, copyTimer - delta);
        shootPoseTimer = Math.max(0f, shootPoseTimer - delta);
        damageTimer = Math.max(0f, damageTimer - delta);
        gravityShifted = false;

        if (controls.rotateLeft && rotateTimer <= 0f) {
            rotateGravity(ROTATION_STEP);
        } else if (controls.rotateRight && rotateTimer <= 0f) {
            rotateGravity(-ROTATION_STEP);
        }

        if (desiredGravity.isZero(0.001f)) {
            gravityVector.scl(Math.max(0f, 1f - delta * 8f));
            if (gravityVector.len2() < 0.0001f) {
                gravityVector.setZero();
            }
        } else {
            gravityVector.lerp(desiredGravity, Math.min(1f, delta * 12f)).nor();
        }
        gravityStrength = MathUtils.lerp(gravityStrength, desiredGravityStrength, Math.min(1f, delta * 10f));

        Vector2 tangent = tangent();
        float targetTangentSpeed = controls.movement * MOVE_SPEED;
        float currentTangentSpeed = velocity.dot(tangent);
        float tangentDelta = MathUtils.clamp(targetTangentSpeed - currentTangentSpeed, -AIR_CONTROL * delta, AIR_CONTROL * delta);
        velocity.mulAdd(tangent, tangentDelta);

        if (grounded && controls.jump) {
            velocity.mulAdd(gravityVector, -JUMP_SPEED);
            grounded = false;
            wallClinging = false;
            state = State.JUMP;
        }

        if (controls.dash && dashTimer <= 0f) {
            float direction = controls.movement == 0f ? 1f : Math.signum(controls.movement);
            velocity.mulAdd(tangent, direction * DASH_SPEED);
            dashTimer = DASH_COOLDOWN;
            state = State.DASH;
        }

        if (controls.groundPound && !grounded) {
            velocity.mulAdd(gravityVector, GROUND_POUND_SPEED - velocity.dot(gravityVector));
            state = State.GROUND_POUND;
        }

        velocity.mulAdd(gravityVector, getGravityStrength() * delta);
        position.mulAdd(velocity, delta);

        resolveWorldBounds(worldWidth, worldHeight);
        resolvePlatforms(platforms);

        float alongGravitySpeed = velocity.dot(gravityVector);
        wallClinging = !grounded && Math.abs(controls.movement) > 0.01f && touchesSide(worldWidth, worldHeight);
        if (wallClinging && alongGravitySpeed > WALL_CLING_MAX_FALL) {
            velocity.mulAdd(gravityVector, WALL_CLING_MAX_FALL - alongGravitySpeed);
        }

        if (dashTimer > DASH_COOLDOWN - 0.14f) {
            state = State.DASH;
        } else if (shootPoseTimer > 0f) {
            state = State.SHOOT;
        } else if (controls.groundPound && !grounded) {
            state = State.GROUND_POUND;
        } else if (wallClinging) {
            state = State.WALL_RUN;
        } else if (!grounded && velocity.dot(gravityVector) > 20f) {
            state = State.FALL;
        } else if (!grounded) {
            state = State.JUMP;
        } else if (Math.abs(velocity.dot(tangent)) > 25f) {
            state = State.RUN;
        } else {
            state = State.IDLE;
        }

        updateBounds();
    }

    public void setGravity(Vector2 direction, float strength) {
        if (direction.isZero(0.001f)) {
            desiredGravity.setZero();
        } else {
            desiredGravity.set(direction).nor();
        }
        desiredGravityStrength = Math.max(0f, strength);
        gravityShifted = !gravityVector.epsilonEquals(desiredGravity, 0.02f);
    }

    public void copyGravity(Vector2 direction, float strength) {
        setGravity(direction, strength);
        copyTimer = 1.2f;
    }

    public void markShoot() {
        shootPoseTimer = 0.12f;
    }

    public boolean damage(int amount) {
        if (damageTimer > 0f) {
            return false;
        }
        health = Math.max(0, health - Math.max(0, amount));
        damageTimer = DAMAGE_COOLDOWN;
        return true;
    }

    public void heal(int amount) {
        health = Math.min(MAX_HEALTH, health + Math.max(0, amount));
    }

    public Rectangle getBounds() {
        updateBounds();
        return bounds;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Vector2 getGravityVector() {
        return gravityVector;
    }

    public float getGravityStrength() {
        return copyTimer > 0f ? gravityStrength * 1.12f : gravityStrength;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isWallClinging() {
        return wallClinging;
    }

    public boolean didGravityShift() {
        return gravityShifted;
    }

    public float getHealthRatio() {
        return health / (float) MAX_HEALTH;
    }

    public int getHealth() {
        return health;
    }

    public boolean isDamageFlashing() {
        return damageTimer > 0f && ((int) (damageTimer * 18f)) % 2 == 0;
    }

    public void respawn(float x, float y) {
        position.set(x, y);
        velocity.setZero();
        gravityVector.set(0f, -1f);
        desiredGravity.set(0f, -1f);
        gravityStrength = DEFAULT_GRAVITY;
        desiredGravityStrength = DEFAULT_GRAVITY;
        health = MAX_HEALTH;
        grounded = false;
        wallClinging = false;
        dashTimer = 0f;
        rotateTimer = 0f;
        copyTimer = 0f;
        shootPoseTimer = 0f;
        damageTimer = 0f;
        state = State.IDLE;
        updateBounds();
    }

    public State getState() {
        return state;
    }

    public float getAnimationTimer() {
        return animationTimer;
    }

    public int getGravityColorIndex() {
        if (gravityVector.isZero(0.001f)) {
            return 5;
        }
        float angle = gravityVector.angleDeg();
        if (angle < 0f) {
            angle += 360f;
        }
        if (angle > 225f && angle < 315f) {
            return 0;
        }
        if (angle > 45f && angle < 135f) {
            return 1;
        }
        if (angle >= 135f && angle <= 225f) {
            return 2;
        }
        if (angle <= 45f || angle >= 315f) {
            return 3;
        }
        return 4;
    }

    private void rotateGravity(float degrees) {
        desiredGravity.rotateDeg(degrees).nor();
        rotateTimer = ROTATE_COOLDOWN;
        gravityShifted = true;
        state = State.GRAVITY_SHIFT;
    }

    private Vector2 tangent() {
        if (gravityVector.isZero(0.001f)) {
            return new Vector2(1f, 0f);
        }
        return new Vector2(-gravityVector.y, gravityVector.x).nor();
    }

    private void resolveWorldBounds(float worldWidth, float worldHeight) {
        updateBounds();
        if (bounds.x < 0f) {
            position.x -= bounds.x;
            stopAgainst(new Vector2(1f, 0f));
        }
        if (bounds.x + bounds.width > worldWidth) {
            position.x -= bounds.x + bounds.width - worldWidth;
            stopAgainst(new Vector2(-1f, 0f));
        }
        if (bounds.y < 0f) {
            position.y -= bounds.y;
            stopAgainst(new Vector2(0f, 1f));
        }
        if (bounds.y + bounds.height > worldHeight) {
            position.y -= bounds.y + bounds.height - worldHeight;
            stopAgainst(new Vector2(0f, -1f));
        }
    }

    private void resolvePlatforms(Array<Rectangle> platforms) {
        grounded = false;
        updateBounds();
        for (Rectangle platform : platforms) {
            if (!bounds.overlaps(platform)) {
                continue;
            }

            float overlapLeft = bounds.x + bounds.width - platform.x;
            float overlapRight = platform.x + platform.width - bounds.x;
            float overlapDown = bounds.y + bounds.height - platform.y;
            float overlapUp = platform.y + platform.height - bounds.y;
            float minX = Math.min(overlapLeft, overlapRight);
            float minY = Math.min(overlapDown, overlapUp);

            Vector2 normal;
            float push;
            if (minX < minY) {
                normal = overlapLeft < overlapRight ? new Vector2(-1f, 0f) : new Vector2(1f, 0f);
                push = minX;
            } else {
                normal = overlapDown < overlapUp ? new Vector2(0f, -1f) : new Vector2(0f, 1f);
                push = minY;
            }

            position.mulAdd(normal, push);
            stopAgainst(normal);
            if (normal.dot(gravityVector) < -0.65f) {
                grounded = true;
            }
            updateBounds();
        }
    }

    private void stopAgainst(Vector2 normal) {
        float intoSurface = velocity.dot(normal);
        if (intoSurface < 0f) {
            velocity.mulAdd(normal, -intoSurface);
        }
    }

    private boolean touchesSide(float worldWidth, float worldHeight) {
        return position.x <= 1f || position.x + WIDTH >= worldWidth - 1f || position.y <= 1f || position.y + HEIGHT >= worldHeight - 1f;
    }

    private void updateBounds() {
        bounds.set(position.x, position.y, WIDTH, HEIGHT);
    }

    public static class Controls {
        public float movement;
        public boolean jump;
        public boolean dash;
        public boolean groundPound;
        public boolean rotateLeft;
        public boolean rotateRight;
        public boolean copyGravity;
        public boolean shoot;
    }

    public enum State {
        IDLE,
        RUN,
        JUMP,
        FALL,
        WALL_RUN,
        GRAVITY_SHIFT,
        SHOOT,
        GROUND_POUND,
        DASH
    }
}

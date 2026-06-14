package com.mycompany.platformer.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Player {
    public static final float HEIGHT = 66f;
    public static final float WIDTH = HEIGHT * 0.62f;

    private static final float SPEED = HEIGHT * 2.75f;
    private static final float JUMP_SPEED = HEIGHT * 5.85f;
    private static final float GRAVITY = -HEIGHT * 15.6f;

    private final Vector2 position;
    private final Vector2 velocity = new Vector2();
    private boolean grounded;

    public Player(float x, float y) {
        position = new Vector2(x, y);
        grounded = true;
    }

    public void update(float delta, float movement, boolean jumpRequested, float groundHeight, float worldWidth) {
        movement = MathUtils.clamp(movement, -1f, 1f);
        velocity.x = movement * SPEED;

        if (grounded && jumpRequested) {
            jump();
        }

        velocity.y += GRAVITY * delta;
        position.mulAdd(velocity, delta);

        if (position.y <= groundHeight) {
            position.y = groundHeight;
            velocity.y = 0f;
            grounded = true;
        } else {
            grounded = false;
        }

        position.x = MathUtils.clamp(position.x, 0f, worldWidth - WIDTH);
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getHorizontalVelocity() {
        return velocity.x;
    }

    public boolean isGrounded() {
        return grounded;
    }

    private void jump() {
        velocity.y = JUMP_SPEED;
        grounded = false;
    }
}

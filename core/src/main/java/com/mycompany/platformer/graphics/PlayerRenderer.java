package com.mycompany.platformer.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mycompany.platformer.entities.Player;

public class PlayerRenderer {
    private static final Color BODY = new Color(0.77f, 0.86f, 0.92f, 1f);
    private static final Color SHADOW = new Color(0.02f, 0.03f, 0.05f, 1f);
    private static final Color VISOR = new Color(0.21f, 0.95f, 1f, 1f);

    public void update(float delta) {
    }

    public void draw(ShapeRenderer shapes, Player player, Color gravityColor) {
        Vector2 position = player.getPosition();
        Vector2 gravity = player.getGravityVector();
        Vector2 up = gravity.isZero(0.001f) ? new Vector2(0f, 1f) : new Vector2(gravity).scl(-1f).nor();
        Vector2 right = new Vector2(up.y, -up.x).nor();
        float cx = position.x + Player.WIDTH * 0.5f;
        float cy = position.y + Player.HEIGHT * 0.5f;
        float bob = MathUtils.sin(player.getAnimationTimer() * 7f) * (player.isGrounded() ? 1.5f : 0.4f);
        Vector2 center = new Vector2(cx, cy).mulAdd(up, bob);

        drawGlow(shapes, center, gravity, gravityColor);
        drawLimbs(shapes, center, right, up, player, gravityColor);
        drawBody(shapes, center, right, up, gravityColor);
        drawGravityArrow(shapes, center, gravity, gravityColor);
    }

    public void dispose() {
    }

    private void drawGlow(ShapeRenderer shapes, Vector2 center, Vector2 gravity, Color gravityColor) {
        shapes.setColor(gravityColor.r, gravityColor.g, gravityColor.b, 0.18f);
        shapes.circle(center.x - gravity.x * 22f, center.y - gravity.y * 22f, 24f);
        shapes.setColor(gravityColor.r, gravityColor.g, gravityColor.b, 0.10f);
        shapes.circle(center.x - gravity.x * 38f, center.y - gravity.y * 38f, 14f);
    }

    private void drawBody(ShapeRenderer shapes, Vector2 center, Vector2 right, Vector2 up, Color gravityColor) {
        Vector2 hip = local(center, right, up, 0f, -11f);
        Vector2 chest = local(center, right, up, 0f, 9f);
        Vector2 head = local(center, right, up, 0f, 23f);

        shapes.setColor(SHADOW);
        shapes.rectLine(hip.x + 2f, hip.y - 2f, chest.x + 2f, chest.y - 2f, 16f);
        shapes.setColor(BODY);
        shapes.rectLine(hip.x, hip.y, chest.x, chest.y, 14f);

        shapes.setColor(gravityColor);
        shapes.rectLine(local(center, right, up, -7f, 2f), local(center, right, up, 7f, 2f), 4f);
        shapes.rectLine(local(center, right, up, -5f, -8f), local(center, right, up, 5f, -8f), 3f);

        shapes.setColor(SHADOW);
        shapes.circle(head.x + 2f, head.y - 2f, 13f);
        shapes.setColor(BODY);
        shapes.circle(head.x, head.y, 12f);
        Vector2 visorA = local(center, right, up, -8f, 23f);
        Vector2 visorB = local(center, right, up, 8f, 23f);
        shapes.setColor(VISOR);
        shapes.rectLine(visorA.x, visorA.y, visorB.x, visorB.y, 5f);
    }

    private void drawLimbs(ShapeRenderer shapes, Vector2 center, Vector2 right, Vector2 up, Player player, Color gravityColor) {
        float stride = MathUtils.sin(player.getAnimationTimer() * 12f) * (player.getState() == Player.State.RUN ? 7f : 2f);
        if (!player.isGrounded()) {
            stride = player.getState() == Player.State.GROUND_POUND ? -9f : 5f;
        }
        Vector2 leftHand = local(center, right, up, -16f, 0f + stride);
        Vector2 rightHand = local(center, right, up, 16f, 0f - stride);
        Vector2 leftFoot = local(center, right, up, -10f, -28f - stride);
        Vector2 rightFoot = local(center, right, up, 10f, -28f + stride);
        Vector2 leftShoulder = local(center, right, up, -8f, 7f);
        Vector2 rightShoulder = local(center, right, up, 8f, 7f);
        Vector2 leftHip = local(center, right, up, -6f, -12f);
        Vector2 rightHip = local(center, right, up, 6f, -12f);

        shapes.setColor(0.48f, 0.58f, 0.66f, 1f);
        shapes.rectLine(leftShoulder.x, leftShoulder.y, leftHand.x, leftHand.y, 5f);
        shapes.rectLine(rightShoulder.x, rightShoulder.y, rightHand.x, rightHand.y, 5f);
        shapes.rectLine(leftHip.x, leftHip.y, leftFoot.x, leftFoot.y, 6f);
        shapes.rectLine(rightHip.x, rightHip.y, rightFoot.x, rightFoot.y, 6f);
        shapes.setColor(gravityColor);
        shapes.circle(leftFoot.x, leftFoot.y, 3f);
        shapes.circle(rightFoot.x, rightFoot.y, 3f);
    }

    private void drawGravityArrow(ShapeRenderer shapes, Vector2 center, Vector2 gravity, Color color) {
        if (gravity.isZero(0.001f)) {
            shapes.setColor(color);
            shapes.circle(center.x, center.y - 38f, 5f);
            return;
        }
        Vector2 start = new Vector2(center).mulAdd(gravity, -36f);
        Vector2 end = new Vector2(center).mulAdd(gravity, -68f);
        Vector2 side = new Vector2(-gravity.y, gravity.x).scl(7f);
        shapes.setColor(color);
        shapes.rectLine(start.x, start.y, end.x, end.y, 4f);
        shapes.triangle(end.x, end.y, end.x - gravity.x * 13f + side.x, end.y - gravity.y * 13f + side.y,
                end.x - gravity.x * 13f - side.x, end.y - gravity.y * 13f - side.y);
    }

    private Vector2 local(Vector2 center, Vector2 right, Vector2 up, float x, float y) {
        return new Vector2(center).mulAdd(right, x).mulAdd(up, y);
    }

}

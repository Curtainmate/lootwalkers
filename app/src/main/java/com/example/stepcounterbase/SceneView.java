package com.example.stepcounterbase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;

final class SceneView extends View {
    interface Model {
        boolean sceneHeroWalking();

        int scenePlayerHp();

        int scenePlayerMaxHp();

        boolean sceneShouldShowEnemy();

        boolean sceneIsBossFight();

        int sceneEnemyHp();

        int sceneEnemyMaxHp();

        String sceneEnemyName();

        boolean sceneChestReady();

        long sceneChestOpenedAt();
    }

    private static final int FRAME_COUNT = 4;
    private static final int HERO_BASELINE_OFFSET_DP = -8;
    private static final int GOBLIN_BASELINE_OFFSET_DP = 0;
    private static final int BOSS_BASELINE_OFFSET_DP = 0;

    private final Model model;
    private final Bitmap background;
    private final Bitmap heroIdle;
    private final Bitmap heroWalk;
    private final Bitmap goblin;
    private final Bitmap goblinBoss;
    private final Bitmap chestOpening;
    private final Paint paint = new Paint();
    private final Paint shadePaint = new Paint();
    private final Rect src = new Rect();
    private final Rect dst = new Rect();

    SceneView(Context context, Model model) {
        super(context);
        this.model = model;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.goblin_dungeon);
        heroIdle = BitmapFactory.decodeResource(getResources(), R.drawable.hero_idle);
        heroWalk = BitmapFactory.decodeResource(getResources(), R.drawable.hero_walk);
        goblin = BitmapFactory.decodeResource(getResources(), R.drawable.goblin);
        goblinBoss = BitmapFactory.decodeResource(getResources(), R.drawable.goblin_boss);
        chestOpening = BitmapFactory.decodeResource(getResources(), R.drawable.chest_opening);
        shadePaint.setColor(Color.argb(70, 0, 0, 0));
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setDither(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);

        int frame = (int) ((System.currentTimeMillis() / 220) % FRAME_COUNT);
        Bitmap heroSheet = model.sceneHeroWalking() ? heroWalk : heroIdle;
        int ground = (int) (getHeight() * 0.88f);
        int heroSize = Math.min(dp(112), (int) (getHeight() * 0.78f));
        int heroX = dp(24);
        int heroY = ground - heroSize + dp(HERO_BASELINE_OFFSET_DP);
        drawFrame(canvas, heroSheet, frame, heroX, heroY, heroSize, heroSize);
        drawHpBar(canvas, heroX, heroY - dp(18), heroSize, model.scenePlayerHp(), model.scenePlayerMaxHp(), "Arin");

        if (model.sceneShouldShowEnemy()) {
            boolean boss = model.sceneIsBossFight();
            Bitmap enemySheet = boss ? goblinBoss : goblin;
            int enemySize = boss
                    ? Math.min(dp(145), (int) (getHeight() * 0.88f))
                    : Math.min(dp(112), (int) (getHeight() * 0.75f));
            int enemyX = getWidth() - enemySize - dp(22);
            int enemyY = ground - enemySize + dp(boss ? BOSS_BASELINE_OFFSET_DP : GOBLIN_BASELINE_OFFSET_DP);
            drawFrame(canvas, enemySheet, frame, enemyX, enemyY, enemySize, enemySize);
            drawHpBar(canvas, enemyX, enemyY - dp(18), enemySize, Math.max(0, model.sceneEnemyHp()),
                    model.sceneEnemyMaxHp(), model.sceneEnemyName());
        }

        if (model.sceneChestReady() || recentlyOpenedChest()) {
            int chestSize = Math.min(dp(92), (int) (getHeight() * 0.52f));
            int chestX = getWidth() - chestSize - dp(42);
            int chestFrame = model.sceneChestReady()
                    ? 0
                    : Math.min(3, (int) ((System.currentTimeMillis() - model.sceneChestOpenedAt()) / 180));
            drawFrame(canvas, chestOpening, chestFrame, chestX, ground - chestSize - dp(12), chestSize, chestSize);
        }

        canvas.drawRect(0, 0, getWidth(), getHeight(), shadePaint);
        postInvalidateDelayed(180);
    }

    private void drawBackground(Canvas canvas) {
        if (background == null) {
            canvas.drawColor(Color.rgb(35, 45, 39));
            return;
        }

        float viewRatio = getWidth() / (float) getHeight();
        float bitmapRatio = background.getWidth() / (float) background.getHeight();
        if (bitmapRatio > viewRatio) {
            int cropWidth = Math.round(background.getHeight() * viewRatio);
            int left = (background.getWidth() - cropWidth) / 2;
            src.set(left, 0, left + cropWidth, background.getHeight());
        } else {
            int cropHeight = Math.round(background.getWidth() / viewRatio);
            int top = (background.getHeight() - cropHeight) / 2;
            src.set(0, top, background.getWidth(), top + cropHeight);
        }
        dst.set(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(background, src, dst, paint);
    }

    private void drawFrame(Canvas canvas, Bitmap sheet, int frame, int x, int y, int width, int height) {
        if (sheet == null) {
            return;
        }

        int frameWidth = sheet.getWidth() / FRAME_COUNT;
        src.set(frame * frameWidth, 0, (frame + 1) * frameWidth, sheet.getHeight());
        dst.set(x, y, x + width, y + height);
        canvas.drawBitmap(sheet, src, dst, paint);
    }

    private void drawHpBar(Canvas canvas, int x, int y, int width, int current, int max, String label) {
        if (max <= 0) {
            return;
        }

        int barWidth = Math.max(dp(72), Math.min(width, dp(126)));
        int barHeight = dp(9);
        int barX = x + (width - barWidth) / 2;
        int barY = Math.max(dp(8), y);
        int fillWidth = Math.max(0, Math.min(barWidth, current * barWidth / max));

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(210, 20, 12, 10));
        canvas.drawRect(barX - dp(2), barY - dp(2), barX + barWidth + dp(2), barY + barHeight + dp(2), paint);
        paint.setColor(Color.rgb(85, 17, 20));
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);
        paint.setColor(Color.rgb(214, 54, 48));
        canvas.drawRect(barX, barY, barX + fillWidth, barY + barHeight, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.rgb(245, 224, 177));
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(10));
        paint.setColor(Color.rgb(245, 224, 177));
        canvas.drawText(label + " " + current + "/" + max, barX + barWidth / 2f, barY - dp(4), paint);
    }

    private boolean recentlyOpenedChest() {
        return model.sceneChestOpenedAt() > 0L && System.currentTimeMillis() - model.sceneChestOpenedAt() < 1400L;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

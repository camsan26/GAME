package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    // Sound dropSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite;
    Vector2 touchPos;
    Array<Sprite> dropSprites;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;
    float dropTimer;
    float worldWidth;
    float worldHeight;
    int score = 0;
    BitmapFont font;
    boolean gameOver = false;
    boolean gameWon = false;
    

    @Override
    public void create() {
        // Prepare your application here.
        backgroundTexture = new Texture("bg.jpg");
        bucketTexture = new Texture("axolotl.gif");
        dropTexture = new Texture("fish.jpg");
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(16, 9);
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(3f, 3f);
        touchPos = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
        font = new BitmapFont();
        font.getData().setScale(0.05f);
        
    }

    private void resetGame() {
        score = 0;
        dropSprites.clear();
        dropTimer = 0f;
        gameOver = false;
        gameWon = false;
        bucketSprite.setPosition(
            viewport.getWorldWidth() / 2f - bucketSprite.getWidth() / 2f,
            0
        );
    }   

    @Override
    public void resize(int width, int height) {
        // Resize your application here. The parameters represent the new window size.
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        // Draw your application here.
        if (gameOver || gameWon) {
            drawEndScreen();
        } else{
            input();
            logic();
            draw();
        }
    }

    private void createDroplet() {
        float dropWidth = 1f;
        float dropHeight = 1f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
        }
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenter(touchPos.x, touchPos.y);
            bucketSprite.setX(MathUtils.clamp(
            bucketSprite.getX(),
            0,
            viewport.getWorldWidth() - bucketSprite.getWidth()
            ));

            bucketSprite.setY(MathUtils.clamp(
            bucketSprite.getY(),
            0,
            viewport.getWorldHeight() - bucketSprite.getHeight()
            ));
        }
    }

    private void logic() {
        worldWidth = viewport.getWorldWidth();
        worldHeight = viewport.getWorldHeight();
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketSprite.getWidth()));
        float delta = Gdx.graphics.getDeltaTime();
        for (Sprite dropSprite : dropSprites) {
            dropSprite.translateY(-2f * delta);
        }
    }
    
    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        bucketSprite.draw(spriteBatch);
        float delta = Gdx.graphics.getDeltaTime();
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(),bucketWidth, bucketHeight);
        for (int i = dropSprites.size -1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();
            dropSprite.draw(spriteBatch);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);
            if (dropSprite.getY() < -dropHeight) {
                dropSprites.removeIndex(i);
                gameOver = true;
            }
            else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                score++;
                if (score >= 10) {
                    gameWon = true;
                }
                // dropSound.play();
            }
        }
        String text = "Score: " + this.score;
        font.draw(spriteBatch, text, 0.3f, viewport.getWorldHeight() - 0.2f);
        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
        }
        spriteBatch.end();
    }

    private void drawEndScreen() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        // Still draw the frozen game in the background
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        bucketSprite.draw(spriteBatch);
        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }
        // Draw the end message in the center
        String message = gameWon ? "You Win!" : "You Lose!";
        font.draw(spriteBatch, message, 
            viewport.getWorldWidth() / 2f - 1f, 
            viewport.getWorldHeight() / 2f + 0.3f);
        font.draw(spriteBatch, "Final Score: " + this.score,
            viewport.getWorldWidth() / 2f - 1.5f,
            viewport.getWorldHeight() / 2f - 0.2f);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        // Destroy application's resources here.

    }
}
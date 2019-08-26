package com.mygdx.game;
/*
*This Class displays the screen using a ScreenAdapter
 */
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.awt.Color;

public class GameScreen extends ScreenAdapter {

    //variables for game state
    private enum STATE {PLAYING, GAME_OVER};
    private STATE state = STATE.PLAYING;
    private BitmapFont bitmapFont;
    private GlyphLayout layout = new GlyphLayout();
    private static final String GAME_OVER_TEXT = "Game Over!...Press SPACE to Play Again!";
    public Viewport viewport;

    //variables for score
    private int score = 0;
    private static final int POINTS_PER_APPLE = 20;
    private GlyphLayout scoreLayout = new GlyphLayout();
    private BitmapFont scoreBitmap;



    //SpriteBatch is an implentation of Batch which is responsible of 2D images
    SpriteBatch batch;

    //Maps bitmap images to screen using OpenGl
    private Texture snakeHead;
    private Texture snakeBody;
    private Texture apple;
    private ShapeRenderer shaperenderer;

    //Variables for timer
    private static final float MOVE_TIME = 0.1F;
    private float timer = MOVE_TIME;

    //Variables for snake movement
    private static final int SNAKE_MOVEMENT = 32;
    public static int snakeX = 0, snakeY = 0;
    private static final int RIGHT = 0;
    private static final int LEFT = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;
    private int snakeDirection  = RIGHT;
    private Array<BodyPart> bodyParts = new Array<BodyPart>();
    private int snakeXBeforeUpdate=0, snakeYBeforeUpdate=0;
    private boolean directionSet = false;
    private boolean hasHit = false;


    //Variables for apple
    private int appleX = 96, appleY = 96;
    private boolean appleAvailable = false;

    //variables for the grid
    private static final int GRID_CELL = 32;

    /*
    *Creates and loads Sprites and Textures into memory
     */

    @Override
    public void show () {
        shaperenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        snakeHead = new Texture(Gdx.files.internal("snakehead.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
        snakeBody = new Texture(Gdx.files.internal("snakebody.png"));
        bitmapFont = new BitmapFont();
        scoreBitmap = new BitmapFont();


    }

    /*
    *Renders the sprites to the screen
     */
    @Override
    public void render (float delta) {

        switch(state) {
            case PLAYING:
                //
                queryInput();
                //Moves the snake
                updateSnake(delta);
                //checks apple collision
                checkAppleCollision();
                //places apple
                checkAndPlaceApple();
                break;
            case GAME_OVER:
                checkForRestart();
                break;
        }


        //Clears screen and Sets the background color to black
        clear();

        //draws the grid
        //drawGrid();

        //Draws sprites
        draw();


    }


    /*
    *Disposes of Sprites and Textures in order to free up memory
     */
    @Override
    public void dispose () {
        batch.dispose();
        snakeHead.dispose();
    }

    /*
    *draws sprites on screen
     */
    private void draw(){
        batch.begin();
        batch.draw(snakeHead, snakeX, snakeY);

        for(BodyPart bodyPart: bodyParts) {
            bodyPart.draw(batch);
        }



        if(appleAvailable){
            batch.draw(apple,appleX,appleY);
        }

        if(state == STATE.GAME_OVER){

            layout.setText( bitmapFont, GAME_OVER_TEXT);
            bitmapFont.draw(batch,GAME_OVER_TEXT,(Gdx.graphics.getWidth() - layout.width)/2,(Gdx.graphics.getHeight() -layout.height)/2);

        }

        drawScore();
        batch.end();
    }

    /*
    *clears the screen and sets background
     */
    private void clear(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /*
    *This method generates the apples position
    *
     */

    private void checkAndPlaceApple(){
        if(!appleAvailable){
            do{
                appleX = MathUtils.random((Gdx.graphics.getWidth()/SNAKE_MOVEMENT -1)*SNAKE_MOVEMENT);
                appleY = MathUtils.random((Gdx.graphics.getHeight()/SNAKE_MOVEMENT -1)*SNAKE_MOVEMENT);

//                double randomDoubleX = Math.random() * (Gdx.graphics.getWidth()/SNAKE_MOVEMENT);
//                int randomIntX = (int)randomDoubleX  * SNAKE_MOVEMENT;
//                appleX = randomIntX;
//                double randomDoubleY = Math.random() * (Gdx.graphics.getWidth()/SNAKE_MOVEMENT);
//                int randomIntY = (int)randomDoubleY * SNAKE_MOVEMENT;
//                appleY = randomIntY;
                appleAvailable = true;

            } while(appleX==snakeX&&appleY==snakeY);
        }


    }

    private void checkAppleCollision(){
        //if(appleAvailable && appleX == snakeX && appleY == snakeY){

        double distance = getDistance();
        if(distance<=(double)32){
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPosition(snakeXBeforeUpdate,snakeYBeforeUpdate);
            bodyParts.insert(0,bodyPart);
            addToScore();
            appleAvailable = false;
        }

        //}
    }

    /*calculates the distance between the snake and the apple
    *using the pythagorean thereom
    * @return the distance as a double
     */
    private double getDistance(){
        double x = appleX >= snakeX ?appleX-snakeX:snakeX-appleX;
        double x2 = Math.pow(x,2);
        double y = appleY >= snakeY ?appleY-snakeY:snakeY-appleY;
        double y2 = Math.pow(y,2);
        double distance2 = x2 + y2;
        double distance = Math.sqrt(distance2);

        return distance;
    }

    /*
    *This method uses shapeRenderer to draw squares on the screen
     */
    private void drawGrid(){
        shaperenderer.begin(ShapeRenderer.ShapeType.Line);
        for(int x = 0; x < Gdx.graphics.getWidth();x += GRID_CELL){
            for(int y = 0; y < Gdx.graphics.getHeight();y += GRID_CELL){
                shaperenderer.rect(x,y,GRID_CELL,GRID_CELL);
            }
        }

        shaperenderer.end();
    }



    /*
    *This method checks for the position of the snake and
    * if it is out of bounds it resets its position
     */
    private void checkForOutOfBounds(){
        if(snakeX>=Gdx.graphics.getWidth()){
            snakeX = 0;
        }
        if(snakeX<0){
            snakeX = Gdx.graphics.getWidth() - SNAKE_MOVEMENT;
        }
        if(snakeY>=Gdx.graphics.getHeight()){
            snakeY = 0;
        }
        if(snakeY<0){
            snakeY = Gdx.graphics.getHeight() - SNAKE_MOVEMENT;
        }

    }

    /*
    *Moves the snake by setting changing its x an y co-ordinates
     */
    private void moveSnake(){

        snakeXBeforeUpdate = snakeX;
        snakeYBeforeUpdate = snakeY;
        switch(snakeDirection){
            case RIGHT:
                snakeX+=SNAKE_MOVEMENT;
                return;
            case LEFT:
                snakeX-=SNAKE_MOVEMENT;
                return;
            case UP:
                snakeY+=SNAKE_MOVEMENT;
                return;
            case DOWN:
                snakeY-=SNAKE_MOVEMENT;
                return;
        }

    }

    private void updateBodyPartsPosition(){
        if(bodyParts.size>0){
            BodyPart bodyPart = bodyParts.removeIndex(0);
            bodyPart.updateBodyPosition(snakeXBeforeUpdate,snakeYBeforeUpdate);
            bodyParts.add(bodyPart);
        }
    }


    /*
    *This function allows the user to set the snake direction my using the directional keys
     */
    private void queryInput(){
        boolean lPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean uPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean dPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);

        if(lPressed){
            updateDirection(LEFT);
            //snakeDirection = LEFT;
        }
        if(rPressed){
            updateDirection(RIGHT);
            //snakeDirection = RIGHT;
        }
        if(uPressed){
            updateDirection(UP);
            //snakeDirection = UP;
        }
        if(dPressed){
            updateDirection(DOWN);
            //snakeDirection = DOWN;
        }
    }

    private void updateIfNotOppositeDirection(int newSnakeDirection, int oppositeDirection){
        if(snakeDirection != oppositeDirection){
            snakeDirection = newSnakeDirection;
        }


    }

    private void updateDirection(int newSnakeDirection){
        if(!directionSet&&snakeDirection!=newSnakeDirection){
            directionSet = true;
            switch(newSnakeDirection){
                case LEFT:
                    updateIfNotOppositeDirection(newSnakeDirection, RIGHT);
                    break;
                case RIGHT:
                    updateIfNotOppositeDirection(newSnakeDirection, LEFT);
                    break;
                case UP:
                    updateIfNotOppositeDirection(newSnakeDirection, DOWN);
                    break;
                case DOWN:
                    updateIfNotOppositeDirection(newSnakeDirection, UP);
                    break;

            }
        }
    }

    private void checkSnakeBodyCollision(){
        for(BodyPart bodyPart:bodyParts){
            if(bodyPart.x == snakeX && bodyPart.y == snakeY){
                state = STATE.GAME_OVER;
            }
        }
    }

    private void updateSnake(float delta){
        if(state == STATE.PLAYING){
            //Creates a timer which moves the snake
            timer -= delta;
            if (timer <= 0) {
                timer = MOVE_TIME;
                moveSnake();
                checkForOutOfBounds();
                updateBodyPartsPosition();
                checkSnakeBodyCollision();
                directionSet = false;
            }

        }

    }

    private void checkForRestart(){
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            doRestart();
        }

    }

    private void doRestart(){
        state = STATE.PLAYING;
        bodyParts.clear();
        snakeDirection = RIGHT;
        directionSet=false;
        timer=MOVE_TIME;
        snakeX=0;
        snakeY=0;
        snakeXBeforeUpdate=0;
        snakeYBeforeUpdate=0;
        appleAvailable = false;

        score = 0;
    }

    private void addToScore(){
        score += POINTS_PER_APPLE;
    }

    private void drawScore(){

        String scoreAsString = Integer.toString(score);
        scoreLayout.setText( scoreBitmap, scoreAsString);
        scoreBitmap.draw(batch,scoreAsString,(Gdx.graphics.getWidth() - scoreLayout.width)/2,(4*Gdx.graphics.getHeight() -scoreLayout.height)/5);

    }
}

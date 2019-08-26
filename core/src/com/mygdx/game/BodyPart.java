package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class BodyPart {
    public int x, y;
    private Texture texture;

    public BodyPart(Texture texture){
        this.texture = texture;
    }

    public void updateBodyPosition(int x,int y){
        this.x=x;
        this.y=y;
    }

    public void draw(Batch batch){
        if(!(x==GameScreen.snakeX &&y==GameScreen.snakeY)){
            batch.draw(texture,x,y);
        }
    }
}

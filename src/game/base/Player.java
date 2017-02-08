package game.base;


import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

/**
 * Created by hehef on 2017/1/21.
 */
public class Player extends BaseObject {
    private Scene scene;
    private Image image;

    private long lastTime;

    private int acc=2;
    private double verticalSpeed;
    private final int MAX_SPEED=3;
    private int upSpeed=-16;

    private double gravity=9.8;


    private enum STATE{UP,CENTER,DOWN};
    private STATE currentState;






    public Player() {
    }

    public Player(int posX,int posY,int width, int height,Image image) {
        super( posX, posY,width, height);
        this.image=image;
        currentState=STATE.CENTER;

    }

    @Override
    public void draw(GraphicsContext gc) {
        switch (currentState){
            case UP:
                gc.save();
                gc.rotate(0.5);
                gc.drawImage(image, getX(), getY(), getWidth(), getHeight());


                break;
            case CENTER:

                gc.drawImage(image, getX(), getY(), getWidth(), getHeight());
                break;
            case DOWN:
                gc.save();
                gc.rotate(0.5);

                gc.drawImage(image, getX(), getY(), getWidth(), getHeight());
                break;

        }
        gc.restore();

    }

    @Override
    public void update(long currentTime) {
        /*long elapse=currentTime-lastTime;
        lastTime=currentTime;
        double deltaTimeInSec=elapse/1_000_000_000.0;// from milli to sec
        verticalSpeed+=verticalSpeed+deltaTimeInSec*gravity;
        moveY(getY()+verticalSpeed*deltaTimeInSec);*/





        //当前速度 = 初始速度 + 加速度 x dt
       // 当前位置 = 初始位置 + 速度 x dt

        //speed+=
        // height = 0.5*gravity*time*time+speed*time
        //double deltaTime=(currentTime-lastTime)/1_000_000_000.0;//for m

        //lastTime=currentTime;
        /*if(lastTime>0) deltaTime=(currentTime-lastTime)/ns;
        else deltaTime=0;*/
        verticalSpeed+=acc;
        if(verticalSpeed>MAX_SPEED){
            verticalSpeed=MAX_SPEED;
        }
        //double height=0.5*gravity*framePerSec*framePerSec+speed*framePerSec;
        //System.out.println(height);
        moveY(verticalSpeed);

        if(getY()<=0) setY(0);
        currentState=STATE.DOWN;

    }

    public void setScene(Scene scene) {
        this.scene = scene;
        scene.setOnKeyPressed(event -> onKeyPressed(event));
        scene.setOnMouseClicked(event -> onMouseClick(event));
    }
    private void onMouseClick(Event e){
        verticalSpeed=upSpeed;
        currentState=STATE.UP;


    }
    private void onKeyPressed(KeyEvent event){
        switch(event.getCode()){
            case RIGHT:
                moveX(5);
                break;
            case LEFT:
                moveX(-1);
                break;
            case UP:
                moveY(-1);
                break;
            case DOWN:
                moveY(+1);
                break;
            case SPACE:
                onMouseClick(new ActionEvent());
                break;

        }
    }

}

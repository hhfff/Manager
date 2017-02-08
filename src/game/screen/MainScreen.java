package game.screen;

import game.base.*;
import game.util.SystemConfiguration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by hehef on 2017/1/21.
 */
public class MainScreen extends GameScreen{
    private int score;
    private int groundHeight=80;
    private Player player;
    private int minY;
    private int maxY;
    private int pipeGap=150;
    private int pipeWidth=80;

    private ArrayList<BaseObject> pipeList =new ArrayList<>();
    private ArrayList<Ground> groudList=new ArrayList<>();

    private Image playerImage;
    private Image backgroundImage;
    private Image groundImage;
    private Text scoreText;
    private Image topPipeImage;
    private Image bottomPipeImage;


    public MainScreen(double width, double height) {
        super(width, height);
    }

    @Override
    public  void initiation() {


        //init ground
        int numOfGround=SystemConfiguration.getStageWidth()/50+1;
        int x=0;

        int y=SystemConfiguration.getStageHeight()-groundHeight;
        int width=50;
        for(int i=0;i<numOfGround;i++){
            Ground ground=new Ground(x,y,width,groundHeight,groundImage);
            groudList.add(ground);
            x+=width;
        }
        scoreText=new Text("0");


        minY=150;
        maxY=SystemConfiguration.getStageHeight()-250;
        initPipe();
        initPlayer();

        //init pipe
        //fisr pipe complete one round 580   secpipe 580-500-300 now at 220+30-40(width of pipe in mid


    }

    @Override
    protected void loadContent() {
        playerImage=new Image(getClass().getResource("../../image/bird2.png").toString(),true);
        backgroundImage=new Image(getClass().getResource("../../image/gamebackground.png").toString(),true);
        groundImage=new Image(getClass().getResource("../../image/stage_ground.png").toString(),true);
        topPipeImage=new Image(getClass().getResource("../../image/pipeTop.png").toString(),true);
        bottomPipeImage=new Image(getClass().getResource("../../image/pipeBottom.png").toString(),true);






    }

    @Override
    protected void update(long currentTime) {
        scoreText.setText(""+score);


        player.update(currentTime);

        for(int i=0;i<pipeList.size();i++){
            BaseObject obj=pipeList.get(i);
            obj.update(currentTime);
            if(obj.isInterset(player)){
                stop();
                Button btn =new Button();
                Image btnImage=new Image(getClass().getResourceAsStream("../../image/start.png"));
                ImageView imageView=new ImageView(btnImage);
                imageView.setFitHeight(50);
                imageView.setFitWidth(80);
                btn.setGraphic(imageView);
                btn.setStyle("-fx-background-color: transparent;");
                btn.relocate(SystemConfiguration.getStageWidth()/2-imageView.getFitWidth()/2,SystemConfiguration.getStageHeight()/2-imageView.getFitHeight()/2);

                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        initPipe();
                        initPlayer();
                        group.getChildren().remove(btn);
                        score=0;
                        start();
                        group.getChildren().remove(btn);
                    }
                });
                group.getChildren().add(btn);


            }

            //check pipe move to end change location
            if(obj instanceof TopPipe){
                TopPipe p=(TopPipe)obj;
                if(p.getX()+p.getWidth()<player.getX()) {
                    if(p.isScore()) {
                        score++;
                        p.setScore(false);

                    }
                }
                if(p.isRelocation()){
                    int height=getRandom();
                    p.setHeight(height);
                    p.setRelocation(false);
                    BaseObject objs=pipeList.get(i+1);
                    if(objs instanceof BottomPipe){
                        objs.setX(p.getX());
                        objs.setY(height+pipeGap);
                    }
                }

            }

        }



        for(Ground g:groudList){
            g.update(currentTime);
            if(g.getX()+g.getWidth()<=0){
                g.setX(SystemConfiguration.getStageWidth());
            }
            if(g.isInterset(player)){
                player.setY(SystemConfiguration.getStageHeight()-groundHeight-player.getHeight());
            }
        }


    }

    @Override
    protected void draw(GraphicsContext gc) {



        gc.drawImage(backgroundImage,0,0, SystemConfiguration.getStageWidth(),SystemConfiguration.getStageHeight()-groundHeight);


        //back object in first
        player.draw(gc);
        for(BaseObject obj: pipeList){
            obj.draw(gc);
        }
        for(Ground g:groudList) g.draw(gc);

        //set score font
        gc.setStroke(Color.WHITE);
        gc.setFont(new Font("ARIAL",30));
        gc.strokeText(""+score,SystemConfiguration.getStageWidth()/2,50);


    }
    private int getRandom(){
        return ThreadLocalRandom.current().nextInt(minY, maxY + 1);

    }

    private void MusicPlayer(){

    }
    private void initPlayer(){
        player=new Player(50,(int)(getWidth()/2),45,32,playerImage);
        player.setScene(getParent().getScene());

    }
    private void initPipe(){
        pipeList.clear();
        int pipeX=SystemConfiguration.getStageWidth()+30-40;
        for(int i=0;i<2;i++){
            int pipeHeight=getRandom();
            pipeList.add(new TopPipe(pipeX,0,pipeWidth,pipeHeight,topPipeImage));
            pipeList.add(new BottomPipe(pipeX,pipeHeight+pipeGap,pipeWidth,SystemConfiguration.getStageHeight()-groundHeight+30,bottomPipeImage));
            pipeX+=300;
        }

    }


}

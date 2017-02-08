package main.scene;

import database.GpaDA;
import database.SqlRetrieveData;
import database.TaskDA;
import database.UserDA;
import entity.Calendar;
import entity.User;
import game.screen.MainScreen;
import game.util.SystemConfiguration;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Created by hehef on 12/6/2016.
 * Updated by WK on the 15th of January 2017
 */
public class MainSceneController implements Initializable{
    // Create static FXMLLoader so that the MainSceneController does not need to keep creating new FXMLLoaders
    private static FXMLLoader fxmlLoader;

    @FXML
    private AnchorPane content;

    @FXML
    private Label taskCounter;

    @FXML
    private Label calendarCounter;

    @FXML
    private Label gpaCounter;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        // Set SceneSelector.fxml to a FXMLLoader and set a controller to the FXMLLoader
        fxmlLoader = new FXMLLoader(getClass().getResource("SceneSelector.fxml"));
        fxmlLoader.setController(this);
    }

    private Parent loadSceneFile(String fileName) {
        Parent p= null;
        try {
            p = FXMLLoader.load(getClass().getResource(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    @FXML
    void homeClick(ActionEvent event) {
        //FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SceneSelector.fxml"));
        //fxmlLoader.setController(this);
        try {
            content.getChildren().setAll((AnchorPane)fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        updaterCounters();
    }

    @FXML
    void calendarClick(ActionEvent event) throws IOException{
        content.getChildren().setAll((AnchorPane)FXMLLoader.load(getClass().getResource("Calendar.fxml")));
    }

    @FXML
    void taskListClick(ActionEvent event) throws IOException {
        content.getChildren().setAll((AnchorPane)FXMLLoader.load(getClass().getResource("Tasklist.fxml")));
    }

    @FXML
    void noteClick(ActionEvent event) throws IOException{
        new SecondaryScene("Note.fxml", "Note", false);
    }

    @FXML
    void emailClick(ActionEvent event) throws IOException {
        content.getChildren().setAll((AnchorPane)FXMLLoader.load(getClass().getResource("email.fxml")));
    }

    @FXML
    void timerClick(ActionEvent event) throws IOException{
        new SecondaryScene("PomodoroTimer.fxml", "Timer", false);
    }

    @FXML
    void profileClick(ActionEvent event) throws IOException {
        content.getChildren().setAll((AnchorPane)FXMLLoader.load(getClass().getResource("ProfilePage.fxml")));
    }

    @FXML
    void gpaClick(ActionEvent event) throws IOException {
        content.getChildren().setAll((AnchorPane)FXMLLoader.load(getClass().getResource("gpaCalculator.fxml")));

    }
    @FXML
    void gameClick(ActionEvent event) {

        Group group=new Group();

        Scene  scene=new Scene(group, SystemConfiguration.getStageWidth(),SystemConfiguration.getStageHeight());

        //this line has problem in artifact
        MainScreen ms=null;
        try {
            ms = new MainScreen(SystemConfiguration.getStageWidth(), SystemConfiguration.getStageHeight());
        }catch (Exception e){
            Alert a=new Alert(Alert.AlertType.INFORMATION);
            a.setContentText(e.getMessage());
            a.setTitle(e.getMessage());
            a.showAndWait();
        }
        //MainScreen ms=new MainScreen(500,600);

        //music
        String path=new File("src/media/Flappy Bird Theme Song.mp3").getAbsolutePath();
        group.getChildren().add(ms);
        ms.setGroup(group);
        ms.start();
        ms.initiation();

        Stage stage=new Stage();
        //scene.setFill(Color.BLACK);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setScene(scene);
        //music
        Media media=new Media(new File(path).toURI().toString());
        MediaPlayer mediaPlayer=new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        stage.showAndWait();
        mediaPlayer.stop();
    }
    /*@FXML
    void musicClick(ActionEvent event) {
        Stage stage=new Stage();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("MusicPlayer.fxml"));
            Scene scene=new Scene(root);
            stage.setScene(scene);
            stage.show();
            System.out.println("showmusic");
        }catch (IOException e){
            System.out.println(e.getMessage());

        }



    }*/

    @FXML
    void logoutClick(ActionEvent event) {
        //Wipe old user data
        new UserDA().userLogout();

        // Create a stage object and lock on the current stage
        Stage stage=(Stage)((Node)event.getTarget()).getScene().getWindow();

        // Close the stage
        stage.close();

        // Prepare stage
        Parent p = loadSceneFile("Login.fxml");
        stage.setScene(new Scene(p));

        //Show the stage
        stage.show();
    }

    public void updaterCounters() {
        Calendar cal = new Calendar();
        TaskDA taskDA = new TaskDA();
        GpaDA gpaDA = new GpaDA();
        String date = cal.arrangeDate(cal.getCurrentYear(), cal.getCurrentMonth() + 1, cal.getCurrentDay());

        taskCounter.setText(taskDA.getTotalTasksCounter() + " tasks remaining");
        calendarCounter.setText(taskDA.getTodayTasksCounter(date) + " tasks due today");
        gpaCounter.setText(String.format("%.2f", gpaDA.calculateTotalGPA()) + " Cumulative GPA");
    }

    public void setContent(AnchorPane content) {
        this.content.getChildren().setAll(content);
    }
}

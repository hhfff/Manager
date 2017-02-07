package main.scene;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import database.UserDA;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;;
import java.time.LocalTime;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import entity.Task;


public class AddTaskController implements Initializable{
    // no touch this
    private String adminNo = new UserDA().getUser().getAdminNo();

    @FXML
    private JFXComboBox<String> priority;
    @FXML
    private JFXButton save;
    @FXML
    private JFXButton cancel;


    @FXML
    private JFXTextField title;
    @FXML
    private JFXDatePicker dueDate;
    @FXML
    private JFXDatePicker dueTime;
    @FXML
    private JFXTextField location;

    private String type;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        priority.getItems().addAll("Very urgent","Urgent","Normal");
    }

    @FXML private javafx.scene.control.Button saveButton;

    @FXML
    private void closeButtonAction(ActionEvent event){
        LocalDate date = dueDate.getValue();
        Date localD = Date.valueOf(date);

        LocalTime time = dueTime.getTime();
        Time localT = Time.valueOf(time);


        //Task task = new Task(title.getText(),dueDate.get,dueTime.getTime(),location.getText(),priority.getPromptText());
        Task task = new Task(title.getText(),localD,localT,location.getText(),priority.getSelectionModel().getSelectedItem(), adminNo,type);
        task.storeData();

        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
    @FXML
    void cancelButtonAction(ActionEvent event){
       /* title.setText("");
        dueDate.setValue(null);
        dueTime.setTime(null);
        location.setText("");
        priority.getSelectionModel().clearSelection();*/
        Stage stage=(Stage)saveButton.getScene().getWindow();
        stage.close();;
    }
    public void setType(String type) {
        this.type = type;
    }

    public void setDueDate(String date) {
        dueDate.setValue(LocalDate.parse(date));
    }

}
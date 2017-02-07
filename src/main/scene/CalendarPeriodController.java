package main.scene;

import database.TaskDA;
import entity.Task;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by Liu Woon Kit on 16/12/2016.
 */
public class CalendarPeriodController extends CalendarController implements Initializable {
    private String[] monthOfYear = cal.getMonthOfYear();
    private TaskDA taskDA = new TaskDA();

    private ObservableList tasksList = FXCollections.observableArrayList();

    @FXML
    private TableView eventTable;

    @FXML
    Label dateLabel;

    @FXML
    TableColumn taskTimeColumn;

    @FXML
    TableColumn taskDescriptionColumn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dateLabel.setText(cal.getSelectedDay() + " " + monthOfYear[cal.getSelectedMonth()].toLowerCase() + " " + cal.getSelectedYear());

        getTimeSlots();

        updateScreen();
    }

    public void getTimeSlots() {
        String date = cal.arrangeDate(cal.getSelectedYear(), (cal.getSelectedMonth()+1), cal.getSelectedDay());

        System.out.println("Selected date(YYY-MM-DD): " + date);

        ArrayList<Task> taskArrayList = taskDA.getCustomTasks(date);

        tasksList.addAll(taskArrayList);
    }

    public void updateScreen() {

        taskTimeColumn.setCellValueFactory(new PropertyValueFactory("dueTime"));

        taskDescriptionColumn.setCellValueFactory(new PropertyValueFactory("title"));

        eventTable.setItems(tasksList);
    }

    public void addTask() {
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Choose type");
        alert.setHeaderText("Please choose type of task");

        ButtonType personalButton=new ButtonType("Personal");
        ButtonType schoolButton=new ButtonType("School");
        ButtonType workButton=new ButtonType("Work");

        alert.getButtonTypes().setAll(personalButton,schoolButton,workButton);
        Optional<ButtonType> result=alert.showAndWait();
        String type=null;
        if(result.get()==personalButton){
            type=personalButton.getText();

        }else if(result.get()==workButton){
            type=workButton.getText();

        }else if(result.get()==schoolButton){
            type=schoolButton.getText();
        }
        Stage stage=new Stage();
        FXMLLoader loader=new FXMLLoader(getClass().getResource("AddTask.fxml"));
        try {
            Parent root=(AnchorPane)loader.load();
            AddTaskController controller=loader.getController();

            // Send to AddTaskController
            controller.setType(type);
            controller.setDueDate(cal.arrangeDate(cal.getSelectedYear(), (cal.getSelectedMonth()+1), cal.getSelectedDay()));

            // Set scene to stage
            Scene scene=new Scene(root);
            stage.setScene(scene);

            stage.setOnHidden(WindowEvent -> {
                System.out.println("Refreshed");
                Platform.runLater(() -> {
                    eventTable.getItems().clear();
                    getTimeSlots();
                    updateScreen();
                });
            });

            // Show stage
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
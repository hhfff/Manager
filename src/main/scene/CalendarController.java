package main.scene;

import database.CalendarDA;
import database.TaskDA;
import entity.Calendar;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static database.UserDA.user;

/**
 * Created by Liu Woon Kit on 1/12/2016.
 */
public class CalendarController extends MainSceneController implements Initializable {
    // Prepare UI elements for the table
    private Label[] daysLabel = new Label[7];
    private Button[] daysButton = new Button[31];
    private Button[] monthButton = new Button[12];
    private Button[] yearButton = new Button[49];

    // Get attributes from Calendar class
    protected static Calendar cal = new Calendar();
    private String[] dayOfWeek = cal.getDayOfWeek();
    private String[] monthOfYear = cal.getMonthOfYear();

    // Set default mode to DayPicker
    private String selectedMode = "DayPicker";

    @FXML
    GridPane calGrid;

    @FXML
    Button modeDisplay;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        daySelector();
    }

    @FXML
    void setMode(ActionEvent event) {
        switch (selectedMode) {

            case "DayPicker":
                monthSelector();
                break;

            case "MonthPicker":
                yearSelector();
                break;

        }
    }

    public void periodicView() throws IOException {
        new SecondaryScene("CalendarPeriod.fxml", "null", true);
    }

    public void daySelector() {
        clearGrid();
        modeDisplay.setText(cal.getStringDate());

        //Set days labels
        for (int u = 0; u < 7; u++) {
            calGrid.add(daysLabel[u] = new Label(dayOfWeek[u]), u, 0);
        }

        for (int i = 0, x = (cal.getFirstDay() - 1), y = 1; i < cal.getLastDayOfMonth(); i++) {
            if (x > 6) {
                x = 0;
                y++;
            }
            calGrid.add(daysButton[i] = new Button(Integer.toString(i + 1)), x, y);

            int tempNum = i + 1;
            daysButton[i].setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    cal.setSelectedDay(tempNum);
                    try {
                        periodicView();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            x++;
        }

        checkDays();
    }

    public void monthSelector() {
        clearGrid();
        selectedMode = "MonthPicker";
        modeDisplay.setText(cal.getSelectedYear() + "");

        for (int i = 0, x = 0, y = 0; i <= 11; i++) {
            if (x > 6) {
                y = 1;
                x = 0;
            }

            calGrid.add(monthButton[i] = new Button(monthOfYear[i]), x, y);

            int tempNum = i;
            monthButton[i].setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectedMode = "DayPicker";
                    cal.setSelectedMonth(tempNum);
                    daySelector();
                }
            });

            x++;
        }
    }

    public void yearSelector() {
        clearGrid();
        selectedMode = "YearPicker";
        modeDisplay.setText(cal.getCurrentYear()-10 + "-" + (cal.getSelectedYear()+52) );
        for (int i = 0, x = 0, y = 0, year = cal.getCurrentYear()-10; i < 49; i++) {
            if (y > 6) {
                x++;
                y = 0;
            }
            calGrid.add(yearButton[i] = new Button(year + ""), y, x);

            int j = i;

            // Try out lambda
            int tempYear = year;
            yearButton[i].setOnAction(ActionEvent -> {
                selectedMode = "MonthPicker";
                cal.setSelectedYear(tempYear);
                monthSelector();
            });

            y++;
            year++;
        }
    }

    public void clearGrid() {
        //wipe grid of elements
        calGrid.getChildren().clear();
    }

    public void ButtonDown() {
        cal.setSelectedMonth(cal.getSelectedMonth() + 1);
        refreshDisplay();
    }

    public void ButtonUp() {
        cal.setSelectedMonth(cal.getSelectedMonth() - 1);
        refreshDisplay();
    }

    public void refreshDisplay() {
        // Refreshes the display
        switch (selectedMode) {

            case "DayPicker":
                daySelector();
                break;

            case "MonthPicker":
                monthSelector();
                break;

            case "YearPicker":
                yearSelector();
                break;

        }
    }

    private CalendarDA calendarDA = new CalendarDA();

    public void checkDays() {
        for (int i = 1, z = cal.getLastDayOfMonth() + 1; i < z; i++) {
            String date = cal.arrangeDate(cal.getSelectedYear(), cal.getSelectedMonth() + 1, i);
            if (calendarDA.checkGotTask(date) == true) {
                daysButton[i - 1].setStyle("-fx-background-color: #e91e63; -fx-text-fill: #ffffff;");
            }

            if (calendarDA.checkHoliday(date) == true) {
                daysButton[i - 1].setStyle("-fx-background-color: #009688; -fx-text-fill: #ffffff;");
            }

            if (calendarDA.checkHoliday(date) == true && calendarDA.checkGotTask(date) == true) {
                daysButton[i - 1].setStyle("-fx-background-color: #f9a825; -fx-text-fill: #ffffff;");
            }

        }
    }
}
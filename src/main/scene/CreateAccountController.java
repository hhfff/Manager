package main.scene;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import entity.User;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class CreateAccountController implements Initializable {
    private String[] monthInNumber;

    @FXML
    private ComboBox<String> month;

    @FXML
    private TextField yearField;

    @FXML
    private TextField adminNoField;

    @FXML
    private Text adminNoError;

    @FXML
    private ComboBox school;

    @FXML
    private ToggleGroup genderGroup;

    @FXML
    private TextField dayField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private Text emailError;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Text passMatchError;

    @FXML
    private Text passMissing;

    @FXML
    private RadioButton radioButMale;

    @FXML
    private RadioButton radioButOther;

    @FXML
    private RadioButton radioButFemale;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        month.getItems().addAll("January","Febuary","March","April","May","June","July","August","September","October","November","December");
        monthInNumber=new String[]{"01","02","03","04","05","06","07","08","09","10","11","12"};
        school.getItems().addAll("SBM", "SCL", "SDN", "SEG", "SHS", "SIT", "SIDM");
        radioButFemale.setUserData("Female");
        radioButMale.setUserData("Male");
        radioButOther.setUserData("Other");


    }
    @FXML
    void signUp(ActionEvent event) {
        emailError.setVisible(false);
        adminNoError.setVisible(false);
        passMatchError.setVisible(false);
        yearField.setStyle("-fx-border-color:#dbdbdb");
        dayField.setStyle("-fx-border-color:#dbdbdb");

        //convert date to string format
        String mth="01";//set default
        try {
             mth= monthInNumber[month.selectionModelProperty().get().getSelectedIndex()];
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println(e);

        }
        boolean checkPass=true;
        String year=yearField.getText();
        String day=dayField.getText();
        if(year.isEmpty()|| Pattern.matches("[a-zA-Z]+",year)||year.length()>4||year.length()<4){
            yearField.setStyle("-fx-border-color:#ff0000");
            checkPass=false;
        }
        if(day.isEmpty()|| Pattern.matches("[a-zA-Z]+",day)||day.length()>2||day.length()<2){
            dayField.setStyle("-fx-border-color:#ff0000");

            checkPass=false;
        }else {
            int i=Integer.parseInt(day);
            if(i>31||i<1){
                dayField.setStyle("-fx-border-color:#ff0000");

                checkPass=false;

            }
        }



        if(!String.valueOf(confirmPasswordField.getText()).equals(String.valueOf(passwordField.getText())) || confirmPasswordField.getText().isEmpty()){
            passMatchError.setVisible(true);
            checkPass=false;

        }
        if(isValidEmailAddress(emailField.getText()) == false){
            emailError.setVisible(true);
            checkPass=false;
        }


        if((adminNoField.getText().length() > 7)||adminNoField.getText().length()<7) {
            adminNoError.setVisible(true);
            System.out.println("length");
            checkPass=false;
        }
        else if (!adminNoField.getText().substring(0, 6).matches("[0-9]+")) {
            adminNoError.setVisible(true);
            System.out.println("number");
            checkPass=false;
        }

        else if (!(Character.isLetter(adminNoField.getText().charAt(6)))) {
            adminNoError.setVisible(true);
            System.out.println("last letter");
            checkPass=false;
        }
        if(checkPass) {
            String birthDateText=year+"-"+mth+"-"+day;

            User user = new User(
                    nameField.getText(),                                               //name
                    genderGroup.getSelectedToggle().getUserData().toString(),         //gender
                    Date.valueOf(birthDateText),                                     //date in sql.Date format
                    passwordField.getText(),                                        //pass
                    adminNoField.getText().toUpperCase(),                                        //adminNo
                    emailField.getText(),                                         //email
                    school.getValue() + "");                               //school
            user.storeData();


                Stage stage = (Stage) ((Node) event.getTarget()).getScene().getWindow();
                stage.close();
                //Parent parent = FXMLLoader.load(getClass().getResource("Login.fxml"));
                //Scene scene = new Scene(parent);
                //stage.hide();

                //stage.setScene(scene);
                //stage.sizeToScene();
                //stage.show();

        }

    }
    private boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }
    @FXML
    void cancel(ActionEvent event){
        Stage stage=(Stage)adminNoError.getScene().getWindow();
        stage.close();


    }
}
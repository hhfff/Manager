package main.scene;

import database.SqlDeleteData;
import database.SqlRetrieveData;

import database.UserDA;
import email.RetriveEmail;
import entity.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.Calendar;

/**
 * Created by hehef on 12/6/2016.
 */
public class EmailController implements Initializable{
    private User user;
    @FXML
    private ListView<String> listView;//for display in ui
    ////for email from inbox of official server,only display title
    private ObservableList<String> displayList=FXCollections.observableArrayList();//for message main body

    private ObservableList<Message> inboxMessages;
    //list for important email from own server,only for title
    private ObservableList<Email> importantMessages=FXCollections.observableArrayList();//for message main body

    // to check wether sychrom
    private boolean isSynchronize=false;

    private enum State{INBOX,IMPORTANT}
    private State currentState;

    private String[] daysName={"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};

    private final ToggleGroup toolBarGroup=new ToggleGroup();

    //today yesterday,2 more day last week(start end) older==last week enddate
    private ArrayList<Date> dateForListRetrive=new ArrayList<Date>();
    private ArrayList<Integer> dateStartIndex=new ArrayList<Integer>();
    private int currButIndex;
    private String password;
    private String adminNo;
    private boolean passSet=false;


    @FXML
    private ToolBar dateSelectorBar;

    @FXML
    private ToggleButton inboxButton;

    @FXML
    private ToggleButton  importantButton;

    @FXML
    private ToggleButton startSynchron;

    private int currentInboxButtonIndex=0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user= UserDA.user;

        listView.setOnMouseClicked(events -> setIndex());


        /*importantMessages.addListener(new ListChangeListener<Email>() {
            @Override
            public void onChanged(Change<? extends Email> c) {
                displayImportantListContent(importantMessages);
            }
        });*/
        important(new ActionEvent());
        currentState=State.IMPORTANT;

    }
    //get different index in diffent list
    private void setIndex(){
        int index=0;
        switch(currentState){
            case INBOX:
                index=currentInboxButtonIndex-listView.getSelectionModel().getSelectedIndex();
                System.out.println(currentInboxButtonIndex+" "+listView.getSelectionModel().getSelectedIndex()+" "+index);
                break;
            case IMPORTANT:
                index=importantMessages.size()-1-listView.getSelectionModel().getSelectedIndex();

                break;
        }
        openContextMenu(index);
    }
    private void openContextMenu(int index){
        ContextMenu contextMenu=new ContextMenu();
        //after show
        contextMenu.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {

            }
        });

        switch(currentState){
            case INBOX:
                MenuItem open=new MenuItem("open");
                open.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        openMail(getEmailObject(inboxMessages.get(index)));
                    }
                });
                MenuItem add=new MenuItem("add to Important");
                add.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Email email=getEmailObject(inboxMessages.get(index));
                        email.storeData(user.getAdminNo());
                        importantMessages.add(email);
                        //save to data base
                    }
                });
                contextMenu.getItems().addAll(open,add);

                break;
            case IMPORTANT:
                MenuItem open2=new MenuItem("open");
                open2.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        openMail(importantMessages.get(index));


                    }
                });
                MenuItem delete=new MenuItem("Delete");
                delete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        //delete data
                        Email email=importantMessages.get(index);
                        SqlDeleteData sql=new SqlDeleteData();
                        sql.openConnection();
                        sql.deleteTableRow(String.format("DELETE FROM email WHERE adminNo=\"%s\" AND subject=\"%s\"",user.getAdminNo(),email.getSubject()));
                        sql.closeConnection();
                        importantMessages.remove(email);
                        displayImportantListContent(importantMessages);

                    }
                });
                contextMenu.getItems().addAll(open2,delete);
                break;
        }
        listView.setContextMenu(contextMenu);
    }


    @FXML
    void inbox(ActionEvent event) {

        retriveMail();
        displayListContent(inboxMessages);
        if(!inboxButton.isSelected())
            inboxButton.setSelected(true);
        currentState=State.INBOX;
    }

    @FXML
    void important(ActionEvent event) {
        //load date from data base
        importantMessages.clear();
        SqlRetrieveData sql=new SqlRetrieveData();
        sql.openConnection();
        ResultSet rs=sql.retriveData("SELECT subject,sender,sentDate,cc,content FROM email WHERE adminNo=\""+user.getAdminNo()+"\"");
        sql.closeConnection();
        try {

            while(rs.next()){

                Email email=new Email(
                        rs.getString("subject"),
                        rs.getString("sender"),
                        rs.getTimestamp("sentDate"),
                        rs.getString("cc"),
                        rs.getString("content"));
                importantMessages.add(email);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        displayImportantListContent(importantMessages);
        importantButton.setSelected(true);
        currentState=State.IMPORTANT;
        dateSelectorBar.getItems().clear();
        //listView.getContextMenu().getItems().remove(1);
    }

    @FXML
    void startSynchron(ActionEvent event) {
        //set to fasle so it can syn chron
        isSynchronize=false;
        if(inboxMessages!=null){
            inboxMessages.clear();//clear all so no need change
        }

        retriveMail();
        inboxButton.setSelected(true);
        currentState= State.IMPORTANT;

    }


    //get all message subject from message to list and display it
    private void displayListContent(ObservableList<Message> message){
        displayList.clear();
        if(message!=null) {
            for (int i = message.size() - 1; i >= 0; i--) {
                try {
                    displayList.add(message.get(i).getSubject());
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
            listView.setItems(displayList);
        }
    }
    private void displayListContent(ObservableList<Message> message,int startIndex,int endIndex){
        displayList.clear();
        if(message!=null) {
            for (int i = startIndex; i >= endIndex; i--) {
                try {
                    displayList.add(message.get(i).getSubject());
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
            listView.setItems(displayList);
        }
    }
    private void displayImportantListContent(ObservableList<Email> emails){
        displayList.clear();
        if(emails!=null) {
            for (int i = emails.size() - 1; i >= 0; i--) {
                displayList.add(emails.get(i).getSubject());
            }
            listView.setItems(displayList);
        }
    }

    //email get and init button bar
    private void retriveMail(){
        if(isSynchronize==false) {
            if(passSet==false) {
                //to get user id and password
                TextField nameField = new TextField();
                nameField.setPromptText("admin Number");
                PasswordField passField = new PasswordField();
                passField.setPromptText("Password");
                passField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        if (event.getCode() == KeyCode.ENTER) {
                            //get current window
                            if (nameField.getText().equals("") || passField.getText().equals("")) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setContentText("empty password or username");
                                alert.show();
                            } else {
                                Stage stage = (Stage) passField.getScene().getWindow();
                                stage.close();
                            }

                        }
                    }
                });
                Button btn = new Button("ok");
                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (nameField.getText().equals("") || passField.getText().equals("")) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setContentText("empty password or username");
                            alert.show();
                        } else {
                            Stage stage = (Stage) btn.getScene().getWindow();
                            stage.close();
                        }

                    }
                });
                //tempory solution as i alway
                //get mail username and pass
                VBox vb = new VBox();
                vb.getChildren().addAll(nameField, passField, btn);
                Scene scene = new Scene(vb);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.showAndWait();
                password = passField.getText();
                adminNo = nameField.getText();

            }
            if(password.equals("")==false&&adminNo.equals("")==false) {
                passSet=true;
                RetriveEmail re = new RetriveEmail(adminNo, password);
                re.openConnection();
                try {
                    inboxMessages = FXCollections.observableArrayList(re.retriveEmail());//wrap it in observable arraylist
                    isSynchronize = true;

                } catch (NullPointerException e) {
                    passSet = false;
                    util.Util.prln(e.getMessage());

                }
            }


        }
        if(inboxMessages!=null) initDateSelectBar();


    }

    //open email object in new window
    private void openMail(Email email){
        try {
            Stage emailContentStage=new Stage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("emailContent.fxml"));
            Parent root=loader.load();//call initiable method during load
            EmailContentController ctl=loader.<EmailContentController>getController();

            ctl.setEmail(email);//pass message to another stage

            Scene scene=new Scene(root);
            emailContentStage.setScene(scene);
            emailContentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Email getEmailObject(Message message){
        Email email=new Email();


        try {
            //for header
            email.setSubject(message.getSubject().toString());
            Address[] froms=message.getFrom();
            String address=froms==null?null:((InternetAddress)froms[0]).getAddress();
            email.setSender(address);
            Address[] receipients=message.getAllRecipients();
            String receipientText="";
            for(Address add:receipients){

                receipientText+=add.toString().replace("\"","")+",";
            }
            email.setCc(receipientText);
            email.setSentDate(message.getSentDate());
            StringBuffer buffer=new StringBuffer();

            //for email body
            Object messageContent=message.getContent();
            if(messageContent instanceof String){
                buffer.append(messageContent.toString());

            }else if(messageContent instanceof Multipart) {
                Multipart multipart=(Multipart)messageContent;
                for(int i=0;i<multipart.getCount();i++) {
                    getMailContent(multipart.getBodyPart(i),buffer);

                }
                //getMailContent((Part) message.getContent());
            }
            email.setContent(buffer.toString());
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return email;
    }
    private void getMailContent(Part part,StringBuffer buffer) throws Exception{
        String contentType=part.getContentType();

        int nameIndex=contentType.indexOf("name");

        System.out.println("type content "+contentType);
        if(part.isMimeType("text/plain")){
            // System.out.println("text/plain");
            buffer.append(part.getContent().toString());

        }else if(part.isMimeType("text/html")){
            //System.out.println("text/html");
            buffer.append(part.getContent().toString());

        }else if(part.isMimeType("multipart/*")){
            // System.out.println("multipart/*");
            Multipart multipart=(Multipart)part.getContent();
            for(int i=0;i<multipart.getCount();i++){
                getMailContent(multipart.getBodyPart(i),buffer);
            }
        }else if(part.isMimeType("message/rfc822")) {
            //System.out.println("message/rfc822");
            getMailContent((Part) part.getContent(),buffer);

        }

    }

    //date button
    ////fore start index use first item ech day in list in next loop
    private void initDateSelectBar() {
        ///////
        //remove all button
        dateSelectorBar.getItems().clear();
        int startIndex=inboxMessages.size()-1;


        //get last email date in inboxlist,last item display first
        Calendar calendar = Calendar.getInstance();
        Calendar firstDateOfWeek=Calendar.getInstance();
        Calendar firstDateOfCurrentWeek=Calendar.getInstance();

        firstDateOfCurrentWeek.set(Calendar.HOUR_OF_DAY,0);
        firstDateOfCurrentWeek.set(Calendar.MINUTE, 0);
        firstDateOfCurrentWeek.set(Calendar.SECOND, 0);
        Calendar todayInCalendar=Calendar.getInstance().getInstance();
        todayInCalendar.set(Calendar.HOUR_OF_DAY,0);
        todayInCalendar.set(Calendar.MINUTE, 0);
        todayInCalendar.set(Calendar.SECOND, 0);
        Calendar yesterInCalendar=Calendar.getInstance();
        yesterInCalendar.setTime(todayInCalendar.getTime());
        yesterInCalendar.add(Calendar.DATE,-1);
        //System.out.println(todayInCalendar.getTime().toString()+" "+yesterInCalendar.getTime().toString());



        try {
            calendar.setTime(inboxMessages.get(startIndex).getSentDate());

            //System.out.println(calendar.getTime().toString());

            /*calendar.set(Calendar.HOUR, 0);

            ;*/

            //use tocalculate only this week
            //the calendar first day of week is sunday,if get this sunday must -7 to get last week sunday date and +1 for this week monday
            int dayOfweek=calendar.getFirstDayOfWeek();
            if(dayOfweek==1){
                dayOfweek+=1;
                firstDateOfWeek.add(Calendar.DATE,-7);
            }
            firstDateOfWeek.set(Calendar.DAY_OF_WEEK,dayOfweek);
            firstDateOfWeek.set(Calendar.HOUR_OF_DAY,0);
            firstDateOfWeek.set(Calendar.MINUTE, 0);
            firstDateOfWeek.set(Calendar.SECOND, 0);

            System.out.println(calendar.getTime().toString()+"     "+firstDateOfCurrentWeek.getTime().toString());

            if(calendar.getTime().after(firstDateOfWeek.getTime())) {
                calendar.set(Calendar.HOUR_OF_DAY,0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                for (int i = startIndex; i >= 0; i--) {

                    if (inboxMessages.get(i).getSentDate().before(calendar.getTime())) {
                        Message message=inboxMessages.get(i);
                        Message previousMessage=inboxMessages.get(i+1);
                        //System.out.println(i + "\t" + calendar.getTime().toString() + "\t" + inboxMessages.get(i).getSentDate().toString());
                        String dayText=daysName[calendar.get(Calendar.DAY_OF_WEEK) - 1];
                        if(previousMessage.getSentDate().after(todayInCalendar.getTime())) dayText="Today";
                        else if(previousMessage.getSentDate().before(todayInCalendar.getTime())&&previousMessage.getSentDate().after(yesterInCalendar.getTime())) {
                            dayText = "Yesterday";
                        }
                        ToggleButton button = new ToggleButton(dayText);
                        addButtonToDateSelectBar(button, startIndex, i + 1);

                        calendar.setTime(message.getSentDate());
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        startIndex = i;
                        if (message.getSentDate().before(firstDateOfCurrentWeek.getTime())) {
                            break;
                        }
                    }
                }
            }

            ToggleButton lastWeekButton=new ToggleButton("Last Week");
            firstDateOfCurrentWeek.add(Calendar.DAY_OF_WEEK,-7);

           //System.out.println("first date of last week "+firstDateOfWeek.getTime().toString());
            for(int i=startIndex;i>=0;i--){
                if(inboxMessages.get(i).getSentDate().before(firstDateOfCurrentWeek.getTime())) {
                    addButtonToDateSelectBar(lastWeekButton,startIndex,i+1);
                    startIndex=i;
                    break;
                }
            }

            ToggleButton olderButton=new ToggleButton("Older");
            addButtonToDateSelectBar(olderButton,startIndex,0);

            ToggleButton allButton=new ToggleButton("All");
            allButton.setSelected(true);
            addButtonToDateSelectBar(allButton,inboxMessages.size()-1,0);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    private void addButtonToDateSelectBar(ToggleButton btn,int startIndex,int endIndex){
        btn.setPrefWidth(90);
        btn.setToggleGroup(toolBarGroup);
        btn.setId("dateSelect");
        //btn.setStyle("-fx-border-color: white");
        btn.setStyle("-fx-text-fill:white;;");
        btn.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(1))));
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                displayListContent(inboxMessages,startIndex,endIndex);
                currentInboxButtonIndex=startIndex;
            }
        });
        dateSelectorBar.getItems().add(0,btn);
    }
    public void setUser(User user){
        this.user=user;
    }


}
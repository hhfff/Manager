package database;

import entity.User;
import javafx.scene.control.Alert;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Liu Woon Kit on 12/1/2017.
 * Updated by Hefei on ?/1/2017
 */
public class UserDA {
    private SqlRetrieveData sqlRetrieveData = new SqlRetrieveData();
    public static User user;
    private ResultSet rs;

    
    public boolean checkLogin(String adminNo, String password) {
        sqlRetrieveData.openConnection();
        adminNo=adminNo.toUpperCase();
        String sqlQuery = "SELECT school, name, email, gender FROM user WHERE adminNo='"+adminNo+"' AND password='"+password+"' ";
        rs = sqlRetrieveData.retriveData(sqlQuery);
        try {
            if(rs.next()) {
                user = new User(adminNo, rs.getString("school"), rs.getString("name"), rs.getString("email"), rs.getString("gender"));
                sqlRetrieveData.closeConnection();
                //System.out.println("Login unsuccessful");
                return true;
            }
        }catch (NullPointerException e){
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("OH,NOOOOOOOOOOOOOOOOOOOOOOO");
            alert.setContentText("Server or Internet is DEAD,PLease SUPPORT THE DEVELOPER by CALL 1383838438");
            alert.show();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        sqlRetrieveData.closeConnection();
        //System.out.println("Login successful");
        return false;
    }

    
    public User getUser() {
        return user;
    }


    
    public void userLogout() {
        user.setAdminNo(null);
        user.setSchool(null);
        user.setName(null);
        user.setEmail(null);
        user.setGender(null);
    }
}

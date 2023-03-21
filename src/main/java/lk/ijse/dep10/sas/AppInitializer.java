package lk.ijse.dep10.sas;

import javafx.application.Application;
import javafx.stage.Stage;
import lk.ijse.dep10.sas.db.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                System.out.println("Database connection is about to save");
                if(DBConnection.getInstance().getConnection() != null &&
                        !DBConnection.getInstance().getConnection().isClosed()) {
                    DBConnection.getInstance().getConnection().close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) {
        generateSchemaIfNotExist();

    }

    private void generateSchemaIfNotExist(){
        Connection connection = DBConnection.getInstance().getConnection();
        Statement stm = null;
        try {
            stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SHOW TABLES");

            HashSet<String> tableNameList = new HashSet<>();
            while ((rst.next())){
                tableNameList.add(rst.getString(1));
            }
            boolean tableExist = tableNameList.containsAll(Set.of("Attendance", "Picture","Student","user")); // of(); after java 9

            if(!tableExist){
                stm.execute(readDBScript());
            }
            System.out.println(tableExist);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
    private String readDBScript(){
        InputStream is = getClass().getResourceAsStream("/schema.sql");
        try(BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            String line;
            StringBuilder dbScriptBuilder = new StringBuilder();
            while((line = br.readLine()) != null){
                dbScriptBuilder.append(line);
            }
            return dbScriptBuilder.toString();
        }catch (IOException e){
            throw new RuntimeException();
        }
    }
}

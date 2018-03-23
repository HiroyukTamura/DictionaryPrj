import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class DbConnector {

    private static final String URL = "jdbc:mysql://localhost:3306/DictionaryPrj";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    DbConnector(){
        System.out.println(this.getClass().getSimpleName());
    }

    void connectDb(){

    }
}

import com.sun.istack.internal.NotNull;

import java.sql.*;

class CompletionOperator {

    private static final String COMPETE_TABLE_NAME = "completed";
    private static final String COLUMNT_START ="index_start";
    private static final String COLUMNT_END ="index_end";
    private static final String COLUMNT_TIME ="time";
    private static final String COLUMNT_EXTRA ="EXTRA";
    private static final String SQL = "INSERT INTO "+ COMPETE_TABLE_NAME +" VALUES (?, ?, ?, ?);";
    private PreparedStatement ps;
    private int start;
    private int end;
    private String extra;

    CompletionOperator(int start, int end, @NotNull String extra){
        this.start = start - Checker.OFFSET;
        this.end = end - Checker.OFFSET;
        this.extra = extra;
    }

    void recordCompletion(){
        try (
                Connection connection = DriverManager.getConnection(Operator.URL, Operator.USERNAME, Operator.PASSWORD)
        ) {
            ps = connection.prepareStatement(SQL);
            ps.setInt(1, start);
            ps.setInt(2, end);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(3, timestamp);
            ps.setString(4, extra);
            ps.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}

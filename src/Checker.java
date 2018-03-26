import com.sun.istack.internal.NotNull;
import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import java.io.IOException;
import java.sql.*;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class Checker {

    private int start;
    private int end;
    static final int OFFSET = 151315;

    static String TABLE_NAME_LOST = "lost_value";
    private static String COLUMN_1 = "column_1";

    private static final String sql = "SELECT name, definition FROM "+ Operator.TABLE_NAME +" WHERE indexId = ?";
    private static final String searchSql = "SELECT * FROM "+ Operator.TABLE_NAME +" WHERE definition = ?;";
    private static final String insertSql =
            "INSERT INTO "+ TABLE_NAME_LOST +
            " SELECT * FROM (SELECT ?, ?) AS TMP "+
            "WHERE NOT EXISTS (SELECT * FROM "+ TABLE_NAME_LOST +" WHERE "+ COLUMN_1 +"=?);";

    private PreparedStatement ps;
    private PreparedStatement pstmt;
    private PreparedStatement statement;
    private Exception exception;

    Checker(int start, int end){
        this.start = start + OFFSET;
        this.end = end + OFFSET;

        System.setProperty("treetagger.home", "C:\\TreeTagger");// ここに本体が必要　\treetagger\bin\tree-tagger.exe　これのことのようです。
    }

    void check(){
        try (
                Connection connection = DriverManager.getConnection(Operator.URL, Operator.USERNAME, Operator.PASSWORD)
        ) {
            ps = connection.prepareStatement(sql);
            pstmt = connection.prepareStatement(searchSql);
            statement = connection.prepareStatement(insertSql);

            for (int i = start; i <= end; i++) {
                if (i%10 == 9){
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        exception = e;
                    }
                }

                ps.setInt(1, i);
                System.out.println("mysql    > " + ps.toString());
                ResultSet result = ps.executeQuery();
                if(!result.next()) {
                    System.out.println("!存在せず!");
                    return;
                }
                String definition = result.getString(1);
                String name = result.getString(2);
                result.close();

                List<String> list = tokenize(definition);
                TreeTaggerWrapper tt = new TreeTaggerWrapper<String>();

                try {
                    tt.setModel("C:\\TreeTagger\\lib\\english-utf8.par");// ここにpar パラメータファイルなるものが必要
                    final int[] tokenCount = {0};
                    int finalI = i;
                    tt.setHandler((TokenHandler<String>) (token, pos, lemma) -> {
                        searchWord(lemma, name);
                        tokenCount[0]++;
                        if (tokenCount[0] == list.size() && finalI == end){
                            String errMsg = null;
                            if (exception != null)
                                errMsg = exception.getMessage();
                            new CompletionOperator(start, end, errMsg)
                                    .recordCompletion();
                        }
                    });
                    tt.process(list);
                } catch (IOException | TreeTaggerException e) {
                    e.printStackTrace();
                    exception = e;
                } finally {
                    tt.destroy();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            exception = e;
        }
    }

    private void searchWord(@NotNull String word, @NotNull String itemName){
        try {
            pstmt.setString(1, word);
            ResultSet resultSearch = pstmt.executeQuery();
            if(!resultSearch.isBeforeFirst()) {
                System.out.println("!!レコード存在せず!!: "+ word + " itemName: "+ itemName);
                statement.setString(1, word);
                statement.setString(2, itemName);
                statement.setString(3, word);
                statement.executeUpdate();
            }
            resultSearch.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage()+": "+ word);
            exception = e;
        }
    }

    private void treeTag(){
        TreeTaggerWrapper tt = new TreeTaggerWrapper<String>();
        String str="I've been to America. I bought leaves";//サンプル
        List<String> sampleStr  = tokenize(str);

        try {
            tt.setModel("C:\\TreeTagger\\lib\\english-utf8.par");// ここにpar パラメータファイルなるものが必要
            tt.setHandler((TokenHandler<String>) (token, pos, lemma) -> System.out.println(token + "\t" + pos + "\t" + lemma));
            //tt.process(asList(new String[] { "This", "is", "a", "test", ".", "I","bought","the","car",".","I've","bought","a","car","."}));
            tt.process(sampleStr);
        } catch (IOException | TreeTaggerException e) {
            e.printStackTrace();
        } finally {
            tt.destroy();
        }
    }

    private static List<String> tokenize(final String aString) {
        List<String> tokens = new ArrayList<>();
        BreakIterator bi = BreakIterator.getWordInstance();
        bi.setText(aString);
        int begin = bi.first();
        int end;
        for (end = bi.next(); end != BreakIterator.DONE; end = bi.next()) {
            String t = aString.substring(begin, end);
            if (t.trim().length() > 0) {
                tokens.add(aString.substring(begin, end));
            }
            begin = end;
        }
        if (end != -1)
            tokens.add(aString.substring(end));

        return tokens;
    }
}

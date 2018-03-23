import com.sun.istack.internal.NotNull;
import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import java.io.IOException;
import java.sql.*;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

class Checker {

    private int start;
    private int end;

    Checker(int start, int end){
        this.start = start;
        this.end = end;

        System.setProperty("treetagger.home", "C:\\TreeTagger");// ここに本体が必要　\treetagger\bin\tree-tagger.exe　これのことのようです。
    }

    void check(){
        try (
                Connection connection = DriverManager.getConnection(Operator.URL, Operator.USERNAME, Operator.PASSWORD);
                Statement statement = connection.createStatement()
        ) {
            for (int i = start; i <= end; i++) {
                String sql = "SELECT name, definition FROM "+ Operator.TABLE_NAME +" WHERE indexId = "+ i + ";";
                System.out.println("sql: " + sql);
                ResultSet result = statement.executeQuery(sql);
                if(!result.next()) {
                    System.out.println("!存在せず!");
                    return;
                }
                String definition = result.getString(1);
                result.close();

                List<String> list = tokenize(definition);
                TreeTaggerWrapper tt = new TreeTaggerWrapper<String>();

                try {
                    tt.setModel("C:\\TreeTagger\\lib\\english-utf8.par");// ここにpar パラメータファイルなるものが必要
                    tt.setHandler((TokenHandler<String>) (token, pos, lemma) -> {
                        searchWord(statement, lemma);
                    });
                    tt.process(list);
                } catch (IOException | TreeTaggerException e) {
                    e.printStackTrace();
                } finally {
                    tt.destroy();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchWord(@NotNull Statement statement, @NotNull String word){
        try {
            String searchSql = "SELECT * FROM "+ Operator.TABLE_NAME +" WHERE definition = '"+ word + "';";
            System.out.println("searchSql: "+ searchSql);
            ResultSet resultSearch = statement.executeQuery(searchSql);
            if(!resultSearch.isBeforeFirst())
                System.out.println("!!レコード存在せず!!: "+ word);
            resultSearch.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage()+": "+ word);
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

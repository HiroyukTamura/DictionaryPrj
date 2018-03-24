import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

class Operator {

    private static final String URL_TEMPLATE = "http://www.mso.anu.edu.au/~ralph/OPTED/v003/wb1913_X.html";
    private static String alphabets = " A · B · C · D · E · F · G · H · I · J · K · L · M · N · O · P · Q · R · S · T · U · V · W · X · Y · Z";
    private static String[] alphabetArr;
    private int count = 151315;

    static final String URL = "jdbc:mysql://localhost:3306/DictionaryPrj";
    static final String USERNAME = "root";
    static final String PASSWORD = "";
    static final String TABLE_NAME = "sampletable";

    Operator() {
    }

    void getWebPage() {
        alphabetArr = alphabets.replace(" ", "").split("·");
        for (String alphabet : alphabetArr) {
            String url = URL_TEMPLATE.replace("X", alphabet.toLowerCase());
            Document document = null;
            try {
                document = Jsoup.connect(url)
                        .timeout((int) TimeUnit.SECONDS.toMillis(100))
                        .maxBodySize(1000*1024*1024)
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (document == null)
                return;

            try (
                    Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                    Statement statement = connection.createStatement()
            ) {
//                boolean check = false;
                Elements elements = document.getElementsByTag("P");
                for (Element element : elements) {
                    String val = element.getElementsByTag("B").html();
                    element.getElementsByTag("B").remove();
                    element.getElementsByTag("I").remove();
                    String define = element.html().replace("()", "");
//                    if (check){
                        log(escapeSQL(define), escapeSQL(val), statement);
//                    } else if (val.equals("Smudge")){
//                        System.out.println("A suffocating smoke来ました");
//                        check = true;
//                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void log(@Nullable String val, @Nullable String define, @NotNull Statement statement) throws SQLException {
        System.out.println("val = [" + val + "], define = [" + define + "]");
        String sql = "INSERT INTO "+ TABLE_NAME +" VALUES ('" + val + "', '" + define + "', " + count + ");";
        int result = statement.executeUpdate(sql);
        if (result != 1)
            System.out.println("val = [" + val + "], define = [" + define + "]" + " result: " + result);
        count++;
    }

    int reformat(@NotNull String tableName) {
        int result = -1;
        try (
                Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                Statement statement = connection.createStatement()
        ) {
            String sql = "Truncate table " + tableName;
            result = statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 文字列の置換を行う
     * @param input   処理の対象の文字列
     * @param pattern 置換前の文字列
     * @return 置換処理後の文字列
     */
    private static String substitute(String input, String pattern, String replacement) {

        // 置換対象文字列が存在する場所を取得
        int index = input.indexOf(pattern);
        // 置換対象文字列が存在しなければ終了
        if (index == -1)
            return input;
        // 処理を行うための StringBuffer
        StringBuilder buffer = new StringBuilder();
        buffer.append(input.substring(0, index)).append(replacement);

        if (index + pattern.length() < input.length()) {
            // 残りの文字列を再帰的に置換
            String rest = input.substring(index + pattern.length(), input.length());
            buffer.append(substitute(rest, pattern, replacement));
        }
        return buffer.toString();
    }

    static String escapeSQL(String input) {
        input = substitute(input, "'", "''");
        input = substitute(input, "\\", "\\\\");
        return input;
    }
}

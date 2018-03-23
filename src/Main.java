import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;
import sun.reflect.generics.tree.Tree;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.setProperty("treetagger.home", "C:\\TreeTagger");// ここに本体が必要　\treetagger\bin\tree-tagger.exe　これのことのようです。
        TreeTaggerWrapper tt = new TreeTaggerWrapper<String>();

        String str="I've been to America. I bought leaves";//サンプル
        List<String> sampleStr  = tokenize(str);

        try {
            //tt.setModel("/opt/treetagger/models/english.par:iso8859-1");
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

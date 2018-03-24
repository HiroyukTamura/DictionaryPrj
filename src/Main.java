public class Main {

    private static final int RECORD_COUNT = 176050;
    private static final int START = 1;
    private static final int END = 5;

    public static void main(String[] args) {
//
//        new DbConnector().connectDb();
//        new Operator().getWebPage();
        new Checker(START, END).check();
//        new Operator().reformat(Checker.TABLE_NAME_LOST);
    }
}

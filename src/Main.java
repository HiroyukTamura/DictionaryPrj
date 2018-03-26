public class Main {

    private static final int RECORD_COUNT = 176050;
    private static final int START = 101;
    private static final int END = 200;

    public static void main(String[] args) {
//
//        new DbConnector().connectDb();
//        new Operator().getWebPage();
        new Checker(START, RECORD_COUNT).check();
//        new Operator().reformat(Checker.TABLE_NAME_LOST);
    }
}

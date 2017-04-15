package uk.co.endofhome.javoice;

public class Main {
    public static void main(String[] args) {
        if (args.length != 0) {
            if (args[0].equals("cli")) {
                while (true) {
                    new App().runCli();
                }
            }
        } else {
            new App().runGui();
        }
    }
}

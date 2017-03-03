import uk.co.endofhome.javoice.App

object Main {
    @JvmStatic fun main(args: Array<String>) {
        if (args.size != 0) {
            if (args[0] == "cli") {
                while (true) {
                    App().runCli()
                }
            }
        } else {
            App().runGui()
        }
    }
}

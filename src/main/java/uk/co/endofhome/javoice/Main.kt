package uk.co.endofhome.javoice

object Main {
    @JvmStatic fun main(args: Array<String>) {
        if (args.isNotEmpty()) {
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

package uk.co.endofhome.javoice.pdf

import uk.co.endofhome.javoice.Config
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class PdfConvertor {
    @Throws(Exception::class)
    fun convert(invoiceFilePath: Path) {
        killExistingSofficeProcess()

        val builder = ProcessBuilder(
            commands(invoiceFilePath)
        )
        builder.directory(File(Config.libreOfficePath().toString()))
        val conversion = builder.start()
        val exitCode = conversion.waitFor()

        if (exitCode != 0) {
            throw RuntimeException("There was a problem converting file: " + invoiceFilePath + "and/or saving to: " + Config.invoicePdfFileOutputPath())
        }
    }

    @Throws(InterruptedException::class)
    private fun killExistingSofficeProcess() {
        val killer = ProcessBuilder()
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            killer.command("cmd.exe", "taskkill", "/IM", "soffice.bin")
        } else {
            killer.command("pkill", "soffice")
        }
        try {
            val killed = killer.start()
            killed.waitFor(300, TimeUnit.MILLISECONDS)
        } catch (e: IOException) {
            throw RuntimeException("Could not kill existing process, not writing out PDF: " + e)
        }
    }

    private fun commands(invoiceFilePath: Path): List<String> {
        val isWindows = System.getProperty("os.name").toLowerCase().contains("windows")
        val libreOfficeProgram = if (isWindows) "soffice.exe" else "./soffice"

        val command = mutableListOf(
            libreOfficeProgram,
            "--headless",
            "--convert-to",
            "pdf",
            invoiceFilePath.toString(),
            "--outdir",
            Config.invoicePdfFileOutputPath().toString()
        )

        if (isWindows) {
            command.add(0, "cmd.exe")
            command.add(1, "/c")
            command.add(2, "start")
        }

        return command
    }
}
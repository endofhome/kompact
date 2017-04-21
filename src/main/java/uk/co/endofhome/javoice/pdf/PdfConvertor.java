package uk.co.endofhome.javoice.pdf;

import uk.co.endofhome.javoice.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.googlecode.totallylazy.Sequences.sequence;

public class PdfConvertor {

    public static void convert(Path invoiceFilePath) throws Exception {
        killExistingSofficeProcess();

        ProcessBuilder builder = new ProcessBuilder(
                commands(invoiceFilePath)
        );
        builder.directory(new File(Config.libreOfficePath().toString()));
        Process conversion = builder.start();
        int exitCode = conversion.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("There was a problem converting file: " + invoiceFilePath + "and/or saving to: " + Config.invoicePdfFileOutputPath());
        }
    }

    private static void killExistingSofficeProcess() throws InterruptedException {
        ProcessBuilder killer = new ProcessBuilder();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            killer.command("cmd.exe", "taskkill", "/IM", "soffice.bin");
        } else {
            killer.command("pkill", "soffice");
        }
        try {
            Process killed = killer.start();
            killed.waitFor(300, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            throw new RuntimeException("Could not kill existing process, not writing out PDF: " + e);
        }
    }

    private static List<String> commands(Path invoiceFilePath) {
        Boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        String libreOfficeProgram = isWindows ? "soffice.exe" : "./soffice";

        List<String> command = sequence(
            libreOfficeProgram,
            "--headless",
            "--convert-to",
            "pdf",
            invoiceFilePath.toString(),
            "--outdir",
            Config.invoicePdfFileOutputPath().toString()
        ).toList();

        if (isWindows) {
            command.add(0, "cmd.exe");
            command.add(1, "/c");
            command.add(2, "start");
        }

        return command;
    }
}
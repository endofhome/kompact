package uk.co.endofhome.javoice.pdf;

import uk.co.endofhome.javoice.Config;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.googlecode.totallylazy.Sequences.sequence;

public class PdfConvertor {

    public static void convert(Path invoiceFilePath) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(
                commands(invoiceFilePath)
        );
        builder.directory(new File(Config.libreOfficePath().toString()));
        Process conversion = builder.start();
        conversion.waitFor(500, TimeUnit.MILLISECONDS);

        if (conversion.exitValue() != 0) {
            throw new RuntimeException("There was a problem converting file: " + invoiceFilePath + "and/or saving to: " + Config.invoicePdfFileOutputPath());
        }

        conversion.destroy();
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
        }

        return command;
    }
}
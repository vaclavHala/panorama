import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ChunkBuilder {

    public static void main(String[] args) throws IOException {

        String usage = "Usage: java ChunkBuilder LAT LON; format n48, e12 etc.";

        if (args.length != 2) {
            System.out.println("Want 2 args, got "+args.length+"\n"+usage);
            System.exit(1);
        }

        String lat = args[0];
        String lon = args[1];

        if (!lat.matches("[ns]\\d+") || !lon.matches("[we]\\d+")) {
            System.out.println("Invalid format\n"+usage);
            System.exit(1);
        }

        String bilIn = "/home/hala/Documents/toy/panorama/data/1arc/" + lat + "_" + lon + "_1arc_v3.bil";
        String featuresIn = "/home/hala/Documents/toy/panorama/data/features/peak_" + lat + "_" + lon + ".csv";

        Path tempElev = Files.createTempFile("chunk_" + lat + "_" + lon, ".elev");
        File chunkZip = new File("/home/hala/Documents/toy/panorama/data/chunks/chunk_" + lat + "_" + lon);

        System.out.println("elev from " + bilIn + " via " + tempElev.toAbsolutePath().toString());
        System.out.println("features from " + featuresIn);
        System.out.println("chunk output to " + chunkZip.toPath().toAbsolutePath().toString());

        FileReverse.reverse(bilIn, tempElev.toAbsolutePath().toString());

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(chunkZip));

        // files need to be in some directory inside the ZIP, otherwise there are problems reading it back

        out.putNextEntry(new ZipEntry("chunk/elev"));
        transferContents(tempElev.toAbsolutePath().toString(), out);
        // out.closeEntry();

        out.putNextEntry(new ZipEntry("chunk/features"));
        transferContents(featuresIn, out);
        // out.closeEntry();

        out.close();

        System.out.println("all done a ok");
    }

    private static void transferContents(String fromFile, OutputStream toStream) throws IOException {
        InputStream in = new FileInputStream(fromFile);
        try {
            byte[] buffer = new byte[1 << 16];
            int len;
            while ((len = in.read(buffer)) != -1) {
                toStream.write(buffer, 0, len);
            }
        } finally {
            in.close();
        }
    }

}

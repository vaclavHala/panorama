import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ChunkBuilder {

    public static void main(String[] args) throws IOException {

        String usage = "Usage: java ChunkBuilder LON LAT; format e12, n48 etc.";

        if (args.length != 2) {
            System.out.println(usage);
            System.exit(1);
        }

        String lon = args[0];
        String lat = args[1];

        if (!lon.matches("[ns]\\d+") || !lat.matches("[we]\\d+")) {
            System.out.println(usage);
            System.exit(1);
        }

        String bilIn = "/home/hala/Documents/toy/panorama/data/1arc/" + lon + "_" + lat + "_1arc_v3.bil";
        String featuresIn = "/home/hala/Documents/toy/panorama/data/features/peak_" + lon + "_" + lat + ".csv";

        Path tempElev = Files.createTempFile("chunk_" + lon + "_" + lat, ".elev");
        File chunkZip = new File("/home/hala/Documents/toy/panorama/data/chunks/" + lon + "_" + lat + ".chunk");

        System.out.println("elev from " + bilIn + " via " + tempElev.toAbsolutePath().toString());
        System.out.println("features from " + featuresIn);
        System.out.println("chunk output to " + chunkZip.toPath().toAbsolutePath().toString());

        FileReverse.reverse(bilIn, tempElev.toAbsolutePath().toString());

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(chunkZip));

        out.putNextEntry(new ZipEntry("elev"));
        transferContents(tempElev.toAbsolutePath().toString(), out);
        out.closeEntry();

        out.putNextEntry(new ZipEntry("features"));
        transferContents(featuresIn, out);
        out.closeEntry();

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

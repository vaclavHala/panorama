import java.io.*;
import java.nio.file.Paths;

public class FileReverse {

    public static void main(String[] args) throws Exception {

        String usage = "Usage: java FileReverse LON LAT; format e12, n48 etc.";

        if (args.length != 2) {
            System.out.println(usage);
            System.exit(1);
        }

        String lon = args[0];
        String lat = args[1];

        if (!lon.matches("[we]\\d+") || !lat.matches("[ns]\\d+")) {
            System.out.println(usage);
            System.exit(1);
        }

        String in = "/home/hala/Documents/toy/panorama/data/1arc/" + lat + "_" + lon + "_1arc_v3.bil";
        String out = "/home/hala/Documents/toy/panorama/android/assets/chunks/chunk_" + lon + "_" + lat;

        reverse(in, out);

        System.out.println("chunk ready: " + out);
    }

    public static void reverse(String bilFile, String outFile) throws IOException {

        InputStream is = new BufferedInputStream(new FileInputStream(Paths.get(bilFile).toFile()));
        OutputStream os = new BufferedOutputStream(new FileOutputStream(Paths.get(outFile).toFile()));

        byte[][] buffs = new byte[3601][];
        for (int row = 0; row < buffs.length; row++) {
            buffs[row] = fillBuffer(3601 * 2, is);
        }
        is.close();
        for (int row = 3601 - 1; row >= 0; row--) {
            os.write(buffs[row]);
        }
        os.close();
    }

    private static byte[] fillBuffer(int size, InputStream is) throws IOException {
        byte[] buff = new byte[size];
        int at = 0;
        while (at < buff.length) {
            at += is.read(buff, at, buff.length - at);
        }
        return buff;
    }

}

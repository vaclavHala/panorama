import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public class BilToImage {

    public static void main(String[] args) throws Exception {

        String bilFile = "/home/hala/Documents/toy/panorama/android/assets/chunks/chunk_e14_n48";
        String pngFile = "/home/hala/Documents/toy/panorama/android/assets/chunks/chunk_e14_n48.png";

        int dim = 3601;
        int[] buff = new int[dim * dim];

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        InputStream in = Files.newInputStream(Paths.get(bilFile));
        for (int i = 0; i < buff.length; i++) {
            int lsb = in.read();
            int msb = in.read();
            if (lsb == -1 || msb == -1) {
                throw new IllegalStateException("boom at index " + i);
            }
            short elev = (short) (lsb + (msb << 8));
            buff[i] = elev;
            min = min < elev ? min : elev;
            max = max > elev ? max : elev;
        }

        BufferedImage img = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
        float delta = max - min;

        for (int y = 0; y < dim; y++) {
            if (y % 100 == 0) {
                System.out.println("Row " + y);
            }
            for (int x = 0; x < dim; x++) {
                int elev = buff[dim * y + x];
                float rel = (elev - min) / delta;
                int rgb = (int) (rel * 255);
                int col = rgb + (rgb << 8) + (rgb << 16) + (255 << 24);
                img.setRGB(x, y, col);
            }
        }

        ImageIO.write(img, "PNG", Paths.get(pngFile).toFile());

    }

}

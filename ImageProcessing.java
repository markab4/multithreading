// recolors an image (of flowers in this case) from white to purple

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessing {
    public static final String SOURCE_FILE = "./resources/many-flowers.jpg";
    public static final String DESTINATION_FILE = "./out/many-flowers.jpg";


    public static void main(String[] args) throws IOException {
        File source = new File(SOURCE_FILE);
        BufferedImage originalImage = ImageIO.read(source);
        BufferedImage resultImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);


        // Latency of each threaded process

        // single-threaded process = 1080 ms
        // multi-threaded process with 1 thread = 1157 ms
        // multi-threaded process with 2 threads = 1001 ms
        // multi-threaded process with 6 threads = 624 ms
        // multi-threaded process with 16 threads = 773 ms
        // multi-threaded process with 40 threads = 1160 ms


        long startTime = System.currentTimeMillis();
//        recolorSingleThreaded(originalImage, resultImage);

        int numberOfThreads = 1;
        recolorMultithreaded(originalImage, resultImage, numberOfThreads);

        long duration = System.currentTimeMillis() - startTime;

        File outputFile = new File(DESTINATION_FILE);
        ImageIO.write(resultImage, "jpg", outputFile);

        System.out.println(duration);
    }

    public static void recolorSingleThreaded(BufferedImage originalImage, BufferedImage resultImage){
        recolorImage(originalImage, resultImage, 0, 0, originalImage.getWidth(), originalImage.getHeight());
    }

    public static void recolorMultithreaded(BufferedImage originalImage, BufferedImage resultImage, int numberOfThreads){
        List<Thread> threads = new ArrayList<>();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight() / numberOfThreads;

        for(int i = 0; i < numberOfThreads; i++) {
            final int threadMultiplier = i;

            Thread thread = new Thread(() -> {
                int leftCorner = 0;
                int topCorner = height * threadMultiplier;

                recolorImage(originalImage, resultImage, leftCorner, topCorner, width, height);
            });

            threads.add(thread);
        }

        for(Thread thread : threads) {
            thread.start();
        }
        for(Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void recolorImage(BufferedImage originalImage, BufferedImage resultImage, int leftCorner, int topCorner,
                                    int width, int height){
        for(int x = leftCorner; x < leftCorner + width && x < originalImage.getWidth(); x++) {
            for(int y = topCorner; y < topCorner + height && y<originalImage.getHeight(); y++){
                recolorPixel(originalImage, resultImage, x, y);
            }
        }
    }

    public static void recolorPixel(BufferedImage originalImage, BufferedImage resultImage, int x, int y){

        int rgb = originalImage.getRGB(x, y),
                red = getRed(rgb),
                blue = getBlue(rgb),
                green = getGreen(rgb),
                newRed,
                newGreen,
                newBlue;

        // purple is a combination of red and blue, so we decrease amount of green
        if(isShadeOfGray(red, green, blue)) {       // our target values are shades of grey
            newRed = Math.min(255, red + 10);
            newGreen = Math.max(0, green - 80);
            newBlue = Math.max(0, blue - 20);
        } else {
            newRed = red;
            newGreen = green;
            newBlue = blue;
        }
        int newRGB = createRGBFromColors(newRed, newGreen, newBlue);
        setRGB(resultImage, x, y, newRGB);
    }

    public static void setRGB(BufferedImage image, int x, int y, int rgb) {
        image.getRaster().setDataElements(x, y, image.getColorModel().getDataElements(rgb, null));
    }


    //checking that all three components have a similar color intensity,
    // ie if no one particular component is stronger than the rest.
    public static boolean isShadeOfGray(int red, int green, int blue){
        // if the color is almost a perfect mix of green red and blue, it's a shade of gray.
        return  Math.abs(red - green) < 30 &&
                Math.abs(red - blue) < 30 &&
                Math.abs(green - blue) < 30;
    }

    public static int createRGBFromColors(int red, int green, int blue){
        int rgb = 0;
        rgb |= blue;
        rgb |= green << 8;
        rgb |= red << 16;

        rgb |= 0xFF000000;
        return rgb;
    }

    public static int getRed(int rgb) {
        return (rgb & 0x00FF0000) >> 16;
    }


    public static int getGreen(int rgb) {
        return (rgb & 0x0000FF00) >> 8;
    }

    public static int getBlue(int rgb) {
        return rgb & 0x000000FF;
    }

}

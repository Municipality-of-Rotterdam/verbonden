package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PasfotoCropUtilTest {

    @Test
    void cropReducesImageToSpecifiedArea() throws IOException {
        // Create a 100x100 red image
        BufferedImage original = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                original.setRGB(x, y, 0xFF0000);
            }
        }
        byte[] imageData = toBytes(original, "png");

        byte[] cropped = PasfotoCropUtil.crop(imageData, 10, 20, 50, 60, "image/png");

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(cropped));
        assertNotNull(result);
        assertEquals(50, result.getWidth());
        assertEquals(60, result.getHeight());
    }

    @Test
    void cropWithZeroDimensionsReturnsOriginal() throws IOException {
        BufferedImage original = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        byte[] imageData = toBytes(original, "png");

        byte[] result = PasfotoCropUtil.crop(imageData, 0, 0, 0, 0, "image/png");

        assertArrayEquals(imageData, result);
    }

    @Test
    void cropClampsToImageBounds() throws IOException {
        BufferedImage original = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        byte[] imageData = toBytes(original, "png");

        // Request crop larger than image
        byte[] cropped = PasfotoCropUtil.crop(imageData, 10, 10, 200, 200, "image/png");

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(cropped));
        assertNotNull(result);
        // Should be clamped to 40x40 (50-10=40)
        assertEquals(40, result.getWidth());
        assertEquals(40, result.getHeight());
    }

    @Test
    void cropWithInvalidDataReturnsOriginal() {
        byte[] invalidData = new byte[]{0x00, 0x01, 0x02};

        byte[] result = PasfotoCropUtil.crop(invalidData, 0, 0, 50, 50, "image/png");

        assertArrayEquals(invalidData, result);
    }

    private byte[] toBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }
}

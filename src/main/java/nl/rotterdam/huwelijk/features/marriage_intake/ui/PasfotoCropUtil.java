package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility for server-side cropping of passport photos based on client-provided coordinates.
 */
final class PasfotoCropUtil {

    private PasfotoCropUtil() {
    }

    /**
     * Crops the given image bytes to the specified rectangle.
     * Coordinates refer to the original image dimensions.
     *
     * @param imageData   the original image bytes
     * @param x           left offset in pixels
     * @param y           top offset in pixels
     * @param width       crop width in pixels
     * @param height      crop height in pixels
     * @param contentType the MIME type (used to determine output format)
     * @return the cropped image bytes, or the original bytes if cropping fails
     */
    static byte[] crop(byte[] imageData, int x, int y, int width, int height, String contentType) {
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageData));
            if (original == null) {
                return imageData;
            }

            // Clamp coordinates to image bounds
            int imgWidth = original.getWidth();
            int imgHeight = original.getHeight();
            int clampedX = Math.max(0, Math.min(x, imgWidth - 1));
            int clampedY = Math.max(0, Math.min(y, imgHeight - 1));
            int clampedWidth = Math.min(width, imgWidth - clampedX);
            int clampedHeight = Math.min(height, imgHeight - clampedY);

            if (clampedWidth <= 0 || clampedHeight <= 0) {
                return imageData;
            }

            BufferedImage cropped = original.getSubimage(clampedX, clampedY, clampedWidth, clampedHeight);

            String formatName = switch (contentType) {
                case "image/png" -> "png";
                case "image/gif" -> "gif";
                case "image/webp" -> "webp";
                default -> "jpeg";
            };

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean written = ImageIO.write(cropped, formatName, out);
            if (!written) {
                // Fallback: if the format is not supported (e.g. webp), write as png
                ImageIO.write(cropped, "png", out);
            }
            return out.toByteArray();
        } catch (IOException e) {
            // If cropping fails, return the original image
            return imageData;
        }
    }
}

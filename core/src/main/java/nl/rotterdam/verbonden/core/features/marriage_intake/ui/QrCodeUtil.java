package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class QrCodeUtil {

    private QrCodeUtil() {
    }

    /**
     * Generates a QR code for {@code content} and returns it as a
     * {@code data:image/svg+xml;base64,…} data URI.
     *
     * <p>SVG is used instead of PNG/AWT so that no Java AWT or ImageIO
     * infrastructure is needed — the encoding is pure computation and works
     * correctly in headless server environments.</p>
     *
     * @param content the text to encode (must not be blank)
     * @param width   the CSS display width in pixels (embedded in the SVG {@code width} attribute)
     * @param height  the CSS display height in pixels (embedded in the SVG {@code height} attribute)
     * @return a {@code data:image/svg+xml;base64,…} string
     */
    static String generateQrCodeDataUri(String content, int width, int height) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be null or blank");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        try {
            // Passing 0,0 causes ZXing to choose the minimum module grid for the
            // content (equivalent to Math.max(0, qrWidth) == qrWidth, scale=1).
            // This is well-defined behaviour in ZXing 3.5.x: see QRCodeWriter#renderResult.
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0);
            int size = bitMatrix.getWidth();

            StringBuilder svg = new StringBuilder();
            svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\"")
               .append(" viewBox=\"0 0 ").append(size).append(' ').append(size).append('"')
               .append(" width=\"").append(width).append('"')
               .append(" height=\"").append(height).append('"')
               .append(" shape-rendering=\"crispEdges\">");
            // White background
            svg.append("<rect width=\"").append(size)
               .append("\" height=\"").append(size).append("\" fill=\"white\"/>");
            // One <rect> per horizontal run of black modules to keep the SVG compact
            for (int y = 0; y < size; y++) {
                int x = 0;
                while (x < size) {
                    if (bitMatrix.get(x, y)) {
                        int runStart = x;
                        while (x < size && bitMatrix.get(x, y)) {
                            x++;
                        }
                        svg.append("<rect x=\"").append(runStart)
                           .append("\" y=\"").append(y)
                           .append("\" width=\"").append(x - runStart)
                           .append("\" height=\"1\"/>");
                    } else {
                        x++;
                    }
                }
            }
            svg.append("</svg>");

            String base64 = Base64.getEncoder().encodeToString(
                    svg.toString().getBytes(StandardCharsets.UTF_8));
            return "data:image/svg+xml;base64," + base64;
        } catch (WriterException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}

package org.delcom.ifs25014springbootcashflow.framework.view;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public final class ImageOptimizer {

    private ImageOptimizer() {
    }

    public static byte[] optimizeAvatar(byte[] input) {
        if (input == null || input.length == 0) {
            return input;
        }
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(input));
            if (src == null) {
                return input;
            }
            int w = src.getWidth();
            int h = src.getHeight();
            int side = Math.min(w, h);
            int x = (w - side) / 2;
            int y = (h - side) / 2;
            BufferedImage cropped = src.getSubimage(x, y, side, side);

            int targetSize = 72;
            BufferedImage dst = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = dst.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, targetSize, targetSize);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(cropped, 0, 0, targetSize, targetSize, null);
            g.dispose();

            return encodeJpeg(dst, 0.82f, input);
        } catch (Exception e) {
            return input;
        }
    }

    public static byte[] optimizeProof(byte[] input) {
        if (input == null || input.length == 0) {
            return input;
        }
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(input));
            if (src == null) {
                return input;
            }
            int w = src.getWidth();
            int h = src.getHeight();
            int maxDim = 480;
            int targetW = w;
            int targetH = h;
            if (w > maxDim || h > maxDim) {
                if (w >= h) {
                    targetW = maxDim;
                    targetH = Math.max(1, (int) Math.round((double) h * maxDim / w));
                } else {
                    targetH = maxDim;
                    targetW = Math.max(1, (int) Math.round((double) w * maxDim / h));
                }
            }
            BufferedImage dst = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = dst.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, targetW, targetH);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, targetW, targetH, null);
            g.dispose();

            return encodeJpeg(dst, 0.82f, input);
        } catch (Exception e) {
            return input;
        }
    }

    private static byte[] encodeJpeg(BufferedImage image, float quality, byte[] fallback) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (!writers.hasNext()) {
                return fallback;
            }
            ImageWriter writer = writers.next();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(quality);
                }
                writer.write(null, new IIOImage(image, null, null), param);
            } finally {
                writer.dispose();
            }
            return out.toByteArray();
        } catch (Exception e) {
            return fallback;
        }
    }
}

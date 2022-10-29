package com.chavin.util.image

import javax.imageio.ImageIO
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Transparency
import java.awt.image.BufferedImage

/**
 * CustomCompressor 基于JDK ImageIO 与 Graphics2D
 */
class CustomCompressor extends ICompressor {
    @Override
    void compress(File src, File dst, double quality) {
        if (invalidFiles(src, dst)) return
        quality = validQuality(quality)
        BufferedImage inputImage = ImageIO.read(src)
        int width = inputImage.getWidth(null)
        int height = inputImage.getHeight(null)
        String fmt = src.name.substring(src.name.lastIndexOf('.') + 1)
        int scaleW = (int) (width * quality)
        int scaleH = (int) (height * quality)
        ByteArrayOutputStream buf = new ByteArrayOutputStream()
        zoom(new FileInputStream(src), buf, fmt, scaleW, scaleH, Image.SCALE_SMOOTH)
        zoom(new ByteArrayInputStream(buf.toByteArray()), new FileOutputStream(dst),
                fmt, width, height, Image.SCALE_FAST)
    }

    @Override
    void scale(File src, File dst, double scale, double quality) {
        if (invalidFiles(src, dst)) return
        scale = validQuality(scale)
        quality = validQuality(quality)
        BufferedImage inputImage = ImageIO.read(src)
        int width = inputImage.getWidth(null)
        int height = inputImage.getHeight(null)
        String fmt = src.name.substring(src.name.lastIndexOf('.') + 1)
        int scaleW = (int) (width * scale)
        int scaleH = (int) (height * scale)
        int hint
        if (Double.compare(quality, 0.5) <= 0) {
            hint = Image.SCALE_FAST | Image.SCALE_REPLICATE
        } else {
            hint = Image.SCALE_SMOOTH | Image.SCALE_AREA_AVERAGING
        }
        zoom(new FileInputStream(src), new FileOutputStream(dst), fmt, scaleW, scaleH, hint)
    }

    private static void zoom(InputStream src, OutputStream dst, String fmt, int width, int height, int hint) {
        BufferedImage inputImage = ImageIO.read(src)
        BufferedImage buffedImage
        if (inputImage.colorModel.hasAlpha()) {
            buffedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        } else {
            buffedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        }
        Graphics2D graphics = buffedImage.createGraphics()
        if (inputImage.colorModel.hasAlpha()) { // 有透明通道
            buffedImage = graphics.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT)
            graphics.dispose()
            graphics = buffedImage.createGraphics()
        } else { // 无透明通道设置为白色背景
            graphics.setBackground(Color.WHITE)
            graphics.setColor(Color.WHITE)
            graphics.fillRect(0, 0, width, height)
        }
        graphics.drawImage(inputImage.getScaledInstance(width, height, hint), 0, 0, null)
        ImageIO.write(buffedImage, fmt, dst)
        graphics.dispose()
    }
}

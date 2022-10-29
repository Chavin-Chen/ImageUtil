package com.chavin.util.image
/**
 * 压缩机抽象
 */
abstract class ICompressor {
    public static final double DEF_SCALE = 1.0
    public static final double DEF_QUALITY = 1.0

    /**
     * 压缩，新图大小和原图大小一致
     * @param src
     * @param dst
     * @param quality
     */
    abstract void compress(File src, File dst, double quality)

    /**
     * 缩放与压缩，新图大小是原图大小的scale倍，0.0 < scale <= 1.0
     * @param src
     * @param dst
     * @param quality
     */
    abstract void scale(File src, File dst, double scale, double quality)


    static boolean invalidFiles(File src, File dst) {
        if (null == src || !src.canRead()) {
            println "source file ${src} cannot read"
            return true
        }
        if (null == dst) {
            println "dest file is null"
            return true
        }
        if (null != dst && dst.exists()) {
            println "dest file ${dst} will be over written"
        }
        return false
    }

    static double validScale(double scale) {
        if (Double.compare(scale, 0) <= 0 || Double.compare(scale, Double.MAX_VALUE) >= 0) {
            scale = DEF_SCALE
        }
        return scale
    }

    static double validQuality(double quality) {
        if (Double.compare(quality, 0) <= 0 || Double.compare(quality, 1) >= 0) {
            quality = DEF_QUALITY
        }
        return quality
    }
}

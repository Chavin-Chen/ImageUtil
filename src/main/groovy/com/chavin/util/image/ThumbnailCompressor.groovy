package com.chavin.util.image

import net.coobird.thumbnailator.Thumbnails

/**
 * 基于Google Thumbnails 库
 */
class ThumbnailCompressor extends ICompressor {

    @Override
    void compress(File src, File dst, double quality) {
        if (invalidFiles(src, dst)) {
            return
        }
        Thumbnails.of(src)
                .scale(DEF_SCALE)
                .outputQuality(validQuality(quality))
                .toFile(dst)
    }

    @Override
    void scale(File src, File dst, double scale, double quality) {
        if (invalidFiles(src, dst)) {
            return
        }
        Thumbnails.of(src)
                .scale(validScale(scale))
                .outputQuality(validQuality(quality))
                .toFile(dst)
    }
}

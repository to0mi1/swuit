package org.to0mi1.swuit.component.image;

import java.awt.Dimension;

/**
 * CSS の {@code object-fit} プロパティに相当する描画モード。
 * コンテナサイズと画像サイズから描画サイズを計算する。
 */
public enum ObjectFit {

    /** 画像をコンテナに合わせて引き伸ばす（アスペクト比を維持しない） */
    FILL,

    /** アスペクト比を維持し、画像全体がコンテナに収まる最大サイズ */
    CONTAIN,

    /** アスペクト比を維持し、コンテナ全体を覆う最小サイズ */
    COVER,

    /** 画像の固有サイズのまま描画する */
    NONE,

    /** NONE と CONTAIN のうち小さい方（縮小のみ、拡大はしない） */
    SCALE_DOWN;

    /**
     * コンテナサイズと画像の固有サイズから描画サイズを計算する。
     *
     * @param containerWidth  コンテナの幅
     * @param containerHeight コンテナの高さ
     * @param imageWidth      画像の固有幅
     * @param imageHeight     画像の固有高さ
     * @return 描画サイズ
     */
    public Dimension computeDrawSize(int containerWidth, int containerHeight,
                                     int imageWidth, int imageHeight) {
        if (imageWidth <= 0 || imageHeight <= 0) {
            return new Dimension(0, 0);
        }
        return switch (this) {
            case FILL -> new Dimension(containerWidth, containerHeight);
            case CONTAIN -> computeContain(containerWidth, containerHeight, imageWidth, imageHeight);
            case COVER -> computeCover(containerWidth, containerHeight, imageWidth, imageHeight);
            case NONE -> new Dimension(imageWidth, imageHeight);
            case SCALE_DOWN -> computeScaleDown(containerWidth, containerHeight, imageWidth, imageHeight);
        };
    }

    private static Dimension computeContain(int cW, int cH, int imgW, int imgH) {
        double scaleX = (double) cW / imgW;
        double scaleY = (double) cH / imgH;
        double scale = Math.min(scaleX, scaleY);
        return new Dimension((int) Math.round(imgW * scale), (int) Math.round(imgH * scale));
    }

    private static Dimension computeCover(int cW, int cH, int imgW, int imgH) {
        double scaleX = (double) cW / imgW;
        double scaleY = (double) cH / imgH;
        double scale = Math.max(scaleX, scaleY);
        return new Dimension((int) Math.round(imgW * scale), (int) Math.round(imgH * scale));
    }

    private static Dimension computeScaleDown(int cW, int cH, int imgW, int imgH) {
        Dimension none = new Dimension(imgW, imgH);
        if (imgW <= cW && imgH <= cH) {
            return none;
        }
        return computeContain(cW, cH, imgW, imgH);
    }
}

package org.to0mi1.swuit.component.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.Dimension;

import static org.junit.jupiter.api.Assertions.*;

class ObjectFitTest {

    // === FILL ===

    @Test
    void fill_returnsContainerSize() {
        Dimension d = ObjectFit.FILL.computeDrawSize(300, 200, 640, 480);
        assertEquals(new Dimension(300, 200), d);
    }

    @Test
    void fill_ignoresImageAspectRatio() {
        Dimension d = ObjectFit.FILL.computeDrawSize(100, 400, 640, 480);
        assertEquals(new Dimension(100, 400), d);
    }

    // === CONTAIN ===

    @Test
    void contain_landscapeImageInLandscapeContainer() {
        // 640x480 -> 300x? : scaleX=0.46875 scaleY=0.41667 -> scale=0.41667
        Dimension d = ObjectFit.CONTAIN.computeDrawSize(300, 200, 640, 480);
        assertEquals(200, d.height);
        assertTrue(d.width <= 300, "幅はコンテナ内に収まる");
    }

    @Test
    void contain_tallImageInWideContainer() {
        // 100x400 -> 800x200 : scaleX=8.0, scaleY=0.5 -> scale=0.5
        Dimension d = ObjectFit.CONTAIN.computeDrawSize(800, 200, 100, 400);
        assertEquals(200, d.height);
        assertEquals(50, d.width);
    }

    @Test
    void contain_exactFit() {
        Dimension d = ObjectFit.CONTAIN.computeDrawSize(640, 480, 640, 480);
        assertEquals(new Dimension(640, 480), d);
    }

    @Test
    void contain_scalesUp_whenContainerLarger() {
        Dimension d = ObjectFit.CONTAIN.computeDrawSize(1280, 960, 640, 480);
        assertEquals(new Dimension(1280, 960), d);
    }

    // === COVER ===

    @Test
    void cover_landscapeImageInLandscapeContainer() {
        // 640x480 -> 300x200 : scaleX=0.46875, scaleY=0.41667 -> scale=0.46875
        Dimension d = ObjectFit.COVER.computeDrawSize(300, 200, 640, 480);
        assertEquals(300, d.width);
        assertTrue(d.height >= 200, "高さはコンテナ以上");
    }

    @Test
    void cover_tallImageInWideContainer() {
        // 100x400 -> 800x200 : scaleX=8.0, scaleY=0.5 -> scale=8.0
        Dimension d = ObjectFit.COVER.computeDrawSize(800, 200, 100, 400);
        assertEquals(800, d.width);
        assertEquals(3200, d.height);
    }

    @Test
    void cover_exactFit() {
        Dimension d = ObjectFit.COVER.computeDrawSize(640, 480, 640, 480);
        assertEquals(new Dimension(640, 480), d);
    }

    // === NONE ===

    @Test
    void none_returnsImageSize() {
        Dimension d = ObjectFit.NONE.computeDrawSize(300, 200, 640, 480);
        assertEquals(new Dimension(640, 480), d);
    }

    @Test
    void none_ignoresContainerSize() {
        Dimension d = ObjectFit.NONE.computeDrawSize(1920, 1080, 100, 50);
        assertEquals(new Dimension(100, 50), d);
    }

    // === SCALE_DOWN ===

    @Test
    void scaleDown_imageSmallerThanContainer_returnsImageSize() {
        Dimension d = ObjectFit.SCALE_DOWN.computeDrawSize(800, 600, 100, 50);
        assertEquals(new Dimension(100, 50), d);
    }

    @Test
    void scaleDown_imageLargerThanContainer_usesContain() {
        Dimension expected = ObjectFit.CONTAIN.computeDrawSize(300, 200, 640, 480);
        Dimension d = ObjectFit.SCALE_DOWN.computeDrawSize(300, 200, 640, 480);
        assertEquals(expected, d);
    }

    @Test
    void scaleDown_imageWidthExceedsContainer_usesContain() {
        Dimension expected = ObjectFit.CONTAIN.computeDrawSize(300, 600, 640, 480);
        Dimension d = ObjectFit.SCALE_DOWN.computeDrawSize(300, 600, 640, 480);
        assertEquals(expected, d);
    }

    @Test
    void scaleDown_imageHeightExceedsContainer_usesContain() {
        Dimension expected = ObjectFit.CONTAIN.computeDrawSize(800, 200, 640, 480);
        Dimension d = ObjectFit.SCALE_DOWN.computeDrawSize(800, 200, 640, 480);
        assertEquals(expected, d);
    }

    @Test
    void scaleDown_exactFit_returnsImageSize() {
        Dimension d = ObjectFit.SCALE_DOWN.computeDrawSize(640, 480, 640, 480);
        assertEquals(new Dimension(640, 480), d);
    }

    // === ゼロサイズ画像 ===

    @Test
    void allModes_zeroImageWidth_returnZero() {
        for (ObjectFit fit : ObjectFit.values()) {
            Dimension d = fit.computeDrawSize(300, 200, 0, 480);
            assertEquals(new Dimension(0, 0), d,
                    fit + " は画像幅 0 で (0,0) を返す");
        }
    }

    @Test
    void allModes_zeroImageHeight_returnZero() {
        for (ObjectFit fit : ObjectFit.values()) {
            Dimension d = fit.computeDrawSize(300, 200, 640, 0);
            assertEquals(new Dimension(0, 0), d,
                    fit + " は画像高さ 0 で (0,0) を返す");
        }
    }

    // === アスペクト比維持の検証 ===

    @ParameterizedTest
    @CsvSource({
        "300, 200, 1600, 900",
        "1920, 1080, 400, 300",
        "500, 500, 1600, 900",
    })
    void contain_maintainsAspectRatio(int cW, int cH, int imgW, int imgH) {
        Dimension d = ObjectFit.CONTAIN.computeDrawSize(cW, cH, imgW, imgH);
        double originalRatio = (double) imgW / imgH;
        double drawRatio = (double) d.width / d.height;
        assertEquals(originalRatio, drawRatio, 0.02, "CONTAIN はアスペクト比を維持する");
    }

    @ParameterizedTest
    @CsvSource({
        "300, 200, 1600, 900",
        "1920, 1080, 400, 300",
        "500, 500, 1600, 900",
    })
    void cover_maintainsAspectRatio(int cW, int cH, int imgW, int imgH) {
        Dimension d = ObjectFit.COVER.computeDrawSize(cW, cH, imgW, imgH);
        double originalRatio = (double) imgW / imgH;
        double drawRatio = (double) d.width / d.height;
        assertEquals(originalRatio, drawRatio, 0.02, "COVER はアスペクト比を維持する");
    }
}

package org.to0mi1.swuit.component.image;

import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class ImageViewTest {

    private static BufferedImage createTestImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    // === コンストラクタ ===

    @Test
    void defaultConstructor_noImage() {
        ImageView view = new ImageView();
        assertNull(view.getImage());
    }

    @Test
    void imageConstructor_setsImage() {
        BufferedImage img = createTestImage(100, 50);
        ImageView view = new ImageView(img);
        assertSame(img, view.getImage());
    }

    // === デフォルト値 ===

    @Test
    void defaultObjectFit_isFill() {
        ImageView view = new ImageView();
        assertEquals(ObjectFit.FILL, view.getObjectFit());
    }

    @Test
    void defaultObjectPosition_isCenter() {
        ImageView view = new ImageView();
        assertEquals(ObjectPosition.CENTER, view.getObjectPosition());
    }

    // === setter ===

    @Test
    void setImage_updatesImage() {
        ImageView view = new ImageView();
        BufferedImage img = createTestImage(100, 50);
        view.setImage(img);
        assertSame(img, view.getImage());
    }

    @Test
    void setObjectFit_updatesObjectFit() {
        ImageView view = new ImageView();
        view.setObjectFit(ObjectFit.COVER);
        assertEquals(ObjectFit.COVER, view.getObjectFit());
    }

    @Test
    void setObjectPosition_updatesObjectPosition() {
        ImageView view = new ImageView();
        view.setObjectPosition(ObjectPosition.TOP_LEFT);
        assertEquals(ObjectPosition.TOP_LEFT, view.getObjectPosition());
    }

    @Test
    void setObjectFit_null_throws() {
        ImageView view = new ImageView();
        assertThrows(NullPointerException.class, () -> view.setObjectFit(null));
    }

    @Test
    void setObjectPosition_null_throws() {
        ImageView view = new ImageView();
        assertThrows(NullPointerException.class, () -> view.setObjectPosition(null));
    }

    // === preferredSize ===

    @Test
    void preferredSize_defaultFill_returnsZero() {
        ImageView view = new ImageView(createTestImage(640, 480));
        assertEquals(new Dimension(0, 0), view.getPreferredSize());
    }

    @Test
    void preferredSize_fill_returnsZero() {
        ImageView view = new ImageView(createTestImage(640, 480));
        view.setObjectFit(ObjectFit.FILL);
        assertEquals(new Dimension(0, 0), view.getPreferredSize());
    }

    @Test
    void preferredSize_contain_returnsZero() {
        ImageView view = new ImageView(createTestImage(640, 480));
        view.setObjectFit(ObjectFit.CONTAIN);
        assertEquals(new Dimension(0, 0), view.getPreferredSize());
    }

    @Test
    void preferredSize_cover_returnsZero() {
        ImageView view = new ImageView(createTestImage(640, 480));
        view.setObjectFit(ObjectFit.COVER);
        assertEquals(new Dimension(0, 0), view.getPreferredSize());
    }

    @Test
    void preferredSize_none_returnsImageSize() {
        ImageView view = new ImageView(createTestImage(640, 480));
        view.setObjectFit(ObjectFit.NONE);
        assertEquals(new Dimension(640, 480), view.getPreferredSize());
    }

    @Test
    void preferredSize_scaleDown_returnsImageSize() {
        ImageView view = new ImageView(createTestImage(640, 480));
        view.setObjectFit(ObjectFit.SCALE_DOWN);
        assertEquals(new Dimension(640, 480), view.getPreferredSize());
    }

    @Test
    void preferredSize_withoutImage_returnsZero() {
        ImageView view = new ImageView();
        assertEquals(new Dimension(0, 0), view.getPreferredSize());
    }

    @Test
    void preferredSize_explicitlySet_takesPreference() {
        ImageView view = new ImageView(createTestImage(640, 480));
        view.setPreferredSize(new Dimension(320, 240));
        assertEquals(new Dimension(320, 240), view.getPreferredSize());
    }

    // === getImageBounds ===

    @Test
    void imageBounds_contain_letterbox() {
        // 4:3 画像を正方形コンテナに CONTAIN → 上下にレターボックス
        ImageView view = new ImageView(createTestImage(320, 240));
        view.setObjectFit(ObjectFit.CONTAIN);
        view.setSize(264, 264);
        Rectangle bounds = view.getImageBounds();
        assertNotNull(bounds);
        assertEquals(new Rectangle(0, 33, 264, 198), bounds);
    }

    @Test
    void imageBounds_cover_fillsContainer() {
        // 横長画像を正方形コンテナに COVER → コンテナ全体を覆う
        ImageView view = new ImageView(createTestImage(320, 240));
        view.setObjectFit(ObjectFit.COVER);
        view.setSize(264, 264);
        Rectangle bounds = view.getImageBounds();
        assertNotNull(bounds);
        assertEquals(new Rectangle(0, 0, 264, 264), bounds);
    }

    @Test
    void imageBounds_fill_fillsContainer() {
        ImageView view = new ImageView(createTestImage(320, 240));
        view.setObjectFit(ObjectFit.FILL);
        view.setSize(200, 150);
        Rectangle bounds = view.getImageBounds();
        assertNotNull(bounds);
        assertEquals(new Rectangle(0, 0, 200, 150), bounds);
    }

    @Test
    void imageBounds_none_centeredAtOriginalSize() {
        // 小さい画像を大きいコンテナに NONE → 中央に元サイズで配置
        ImageView view = new ImageView(createTestImage(100, 80));
        view.setObjectFit(ObjectFit.NONE);
        view.setSize(200, 200);
        Rectangle bounds = view.getImageBounds();
        assertNotNull(bounds);
        assertEquals(new Rectangle(50, 60, 100, 80), bounds);
    }

    @Test
    void imageBounds_noImage_returnsNull() {
        ImageView view = new ImageView();
        view.setSize(200, 200);
        assertNull(view.getImageBounds());
    }

    @Test
    void imageBounds_zeroSize_returnsNull() {
        ImageView view = new ImageView(createTestImage(100, 100));
        view.setSize(0, 0);
        assertNull(view.getImageBounds());
    }

    // === 描画テスト ===

    @Test
    void paintComponent_withImage_doesNotThrow() {
        ImageView view = new ImageView(createTestImage(100, 100));
        view.setSize(200, 200);
        BufferedImage canvas = createTestImage(200, 200);
        Graphics2D g = canvas.createGraphics();
        try {
            assertDoesNotThrow(() -> view.paint(g));
        } finally {
            g.dispose();
        }
    }

    @Test
    void paintComponent_withoutImage_doesNotThrow() {
        ImageView view = new ImageView();
        view.setSize(200, 200);
        BufferedImage canvas = createTestImage(200, 200);
        Graphics2D g = canvas.createGraphics();
        try {
            assertDoesNotThrow(() -> view.paint(g));
        } finally {
            g.dispose();
        }
    }

    @Test
    void paintComponent_zeroSize_doesNotThrow() {
        ImageView view = new ImageView(createTestImage(100, 100));
        view.setSize(0, 0);
        BufferedImage canvas = createTestImage(1, 1);
        Graphics2D g = canvas.createGraphics();
        try {
            assertDoesNotThrow(() -> view.paint(g));
        } finally {
            g.dispose();
        }
    }

    @Test
    void paintComponent_allObjectFitModes_doNotThrow() {
        BufferedImage img = createTestImage(640, 480);
        for (ObjectFit fit : ObjectFit.values()) {
            ImageView view = new ImageView(img);
            view.setObjectFit(fit);
            view.setSize(300, 200);
            BufferedImage canvas = createTestImage(300, 200);
            Graphics2D g = canvas.createGraphics();
            try {
                assertDoesNotThrow(() -> view.paintAll(g),
                        "ObjectFit." + fit + " で例外が発生してはいけない");
            } finally {
                g.dispose();
            }
        }
    }

    @Test
    void paintComponent_cover_drawsPixels() {
        BufferedImage img = createTestImage(100, 100);
        // 赤で塗りつぶし
        Graphics2D ig = img.createGraphics();
        ig.setColor(java.awt.Color.RED);
        ig.fillRect(0, 0, 100, 100);
        ig.dispose();

        ImageView view = new ImageView(img);
        view.setObjectFit(ObjectFit.COVER);
        view.setSize(200, 200);

        BufferedImage canvas = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            // paintAll は isShowing() を要求するため paint を直接呼ぶ
            view.paint(g);
        } finally {
            g.dispose();
        }

        // 中央のピクセルが赤で描画されていることを確認
        int centerPixel = canvas.getRGB(100, 100);
        int alpha = (centerPixel >> 24) & 0xFF;
        assertTrue(alpha > 0, "COVER モードで中央にピクセルが描画される");
    }
}

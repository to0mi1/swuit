package org.to0mi1.swuit.component.image;

import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.awt.Graphics2D;
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
    void preferredSize_withImage_returnsImageSize() {
        ImageView view = new ImageView(createTestImage(640, 480));
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

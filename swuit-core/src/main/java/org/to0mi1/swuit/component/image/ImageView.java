package org.to0mi1.swuit.component.image;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Objects;

/**
 * CSS の {@code <img>} に相当する画像描画コンポーネント。
 *
 * <p>{@link ObjectFit} と {@link ObjectPosition} により、
 * CSS の {@code object-fit} / {@code object-position} と同等の描画制御が可能。</p>
 *
 * <pre>{@code
 * ImageView view = new ImageView(myImage);
 * view.setObjectFit(ObjectFit.COVER);
 * view.setObjectPosition(ObjectPosition.CENTER);
 * }</pre>
 */
public class ImageView extends JComponent {

    private Image image;
    private ObjectFit objectFit = ObjectFit.FILL;
    private ObjectPosition objectPosition = ObjectPosition.CENTER;

    public ImageView() {
    }

    public ImageView(Image image) {
        this.image = image;
    }

    // === プロパティ ===

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        invalidate();
        repaint();
    }

    public ObjectFit getObjectFit() {
        return objectFit;
    }

    public void setObjectFit(ObjectFit objectFit) {
        this.objectFit = Objects.requireNonNull(objectFit, "objectFit");
        invalidate();
        repaint();
    }

    public ObjectPosition getObjectPosition() {
        return objectPosition;
    }

    public void setObjectPosition(ObjectPosition objectPosition) {
        this.objectPosition = Objects.requireNonNull(objectPosition, "objectPosition");
        repaint();
    }

    // === サイズ ===

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        if (image != null && (objectFit == ObjectFit.NONE || objectFit == ObjectFit.SCALE_DOWN)) {
            int w = image.getWidth(this);
            int h = image.getHeight(this);
            if (w > 0 && h > 0) {
                return new Dimension(w, h);
            }
        }
        return new Dimension(0, 0);
    }

    // === 描画 ===

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            return;
        }
        int imgW = image.getWidth(this);
        int imgH = image.getHeight(this);
        if (imgW <= 0 || imgH <= 0) {
            return;
        }

        int cW = getWidth();
        int cH = getHeight();
        if (cW <= 0 || cH <= 0) {
            return;
        }

        Dimension drawSize = objectFit.computeDrawSize(cW, cH, imgW, imgH);
        int drawW = drawSize.width;
        int drawH = drawSize.height;

        int offsetX = objectPosition.computeOffset(cW, drawW, true);
        int offsetY = objectPosition.computeOffset(cH, drawH, false);

        // 描画矩形とコンテナの交差を計算（クリッピング）
        Rectangle drawRect = new Rectangle(offsetX, offsetY, drawW, drawH);
        Rectangle containerRect = new Rectangle(0, 0, cW, cH);
        Rectangle visible = drawRect.intersection(containerRect);

        if (visible.isEmpty()) {
            return;
        }

        // 可視領域に対応するソース矩形を計算
        double scaleX = (double) imgW / drawW;
        double scaleY = (double) imgH / drawH;

        int srcX = (int) Math.round((visible.x - offsetX) * scaleX);
        int srcY = (int) Math.round((visible.y - offsetY) * scaleY);
        int srcW = (int) Math.round(visible.width * scaleX);
        int srcH = (int) Math.round(visible.height * scaleY);

        // ソース矩形を画像範囲にクランプ
        srcX = Math.max(0, Math.min(srcX, imgW));
        srcY = Math.max(0, Math.min(srcY, imgH));
        srcW = Math.min(srcW, imgW - srcX);
        srcH = Math.min(srcH, imgH - srcY);

        if (srcW <= 0 || srcH <= 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(image,
                    visible.x, visible.y, visible.x + visible.width, visible.y + visible.height,
                    srcX, srcY, srcX + srcW, srcY + srcH,
                    this);
        } finally {
            g2.dispose();
        }
    }
}

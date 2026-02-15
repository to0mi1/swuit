package org.to0mi1.swuit.layout.aspectratio;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 * 単一子コンポーネントのアスペクト比を維持する {@link LayoutManager2}。
 *
 * <p>CSS の {@code aspect-ratio} プロパティに相当する。
 * 子の preferred width からアスペクト比で height を計算し、
 * 子はコンテナ全体に配置される（{@code width: 100%; height: 100%} 相当）。</p>
 *
 * <pre>{@code
 * JPanel container = new JPanel(new AspectRatioLayout(16.0 / 9.0));
 * container.add(new ImageView(myImage));
 * }</pre>
 */
public class AspectRatioLayout implements LayoutManager2 {

    private final double ratio;

    /**
     * @param ratio アスペクト比 (幅 / 高さ)。例: 16.0/9.0
     */
    public AspectRatioLayout(double ratio) {
        if (ratio <= 0 || !Double.isFinite(ratio)) {
            throw new IllegalArgumentException("ratio must be a positive finite number: " + ratio);
        }
        this.ratio = ratio;
    }

    public double getRatio() {
        return ratio;
    }

    // === LayoutManager2 ===

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        // constraints 不要
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // constraints 不要
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        // 状態なし
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
        // キャッシュなし
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            Component child = getFirstVisibleChild(parent);
            int width;
            if (child != null) {
                width = child.getPreferredSize().width;
            } else {
                width = 0;
            }
            int contentWidth = width + insets.left + insets.right;
            int contentHeight = (int) Math.round(width / ratio) + insets.top + insets.bottom;
            return new Dimension(contentWidth, contentHeight);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            Component child = getFirstVisibleChild(parent);
            int width;
            if (child != null) {
                width = child.getMinimumSize().width;
            } else {
                width = 0;
            }
            int contentHeight = (int) Math.round(width / ratio);
            return new Dimension(
                    width + insets.left + insets.right,
                    contentHeight + insets.top + insets.bottom);
        }
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int x = insets.left;
            int y = insets.top;
            int w = parent.getWidth() - insets.left - insets.right;
            int h = parent.getHeight() - insets.top - insets.bottom;

            Component child = getFirstVisibleChild(parent);
            if (child != null) {
                child.setBounds(x, y, Math.max(0, w), Math.max(0, h));
            }
        }
    }

    private static Component getFirstVisibleChild(Container parent) {
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (child.isVisible()) {
                return child;
            }
        }
        return null;
    }
}

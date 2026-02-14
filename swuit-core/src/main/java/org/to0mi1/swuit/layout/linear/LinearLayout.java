package org.to0mi1.swuit.layout.linear;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.LinkedHashMap;
import java.util.Map;

import org.to0mi1.swuit.layout.Gravity;
import org.to0mi1.swuit.layout.Orientation;

/**
 * Android の {@code LinearLayout} に相当するレイアウトマネージャー。
 * <p>
 * 子コンポーネントを水平または垂直に一列に並べる。
 * {@link LinearConstraints#weight} による余剰スペース分配と
 * {@link Gravity} による配置制御を提供する。
 */
public class LinearLayout implements LayoutManager2 {

    private Orientation orientation;
    private int gravity;
    private int gap;
    private float weightSum;

    private final Map<Component, LinearConstraints> constraintsMap = new LinkedHashMap<>();

    /**
     * デフォルト設定（水平、gap=0）でレイアウトを作成する。
     */
    public LinearLayout() {
        this(Orientation.HORIZONTAL);
    }

    /**
     * 指定した方向でレイアウトを作成する。
     *
     * @param orientation 主軸方向
     */
    public LinearLayout(Orientation orientation) {
        this(orientation, 0);
    }

    /**
     * 方向と gap を指定してレイアウトを作成する。
     *
     * @param orientation 主軸方向
     * @param gap         子コンポーネント間のギャップ (px)
     */
    public LinearLayout(Orientation orientation, int gap) {
        this(orientation, gap, Gravity.NONE);
    }

    /**
     * 方向、gap、gravity を指定してレイアウトを作成する。
     *
     * @param orientation 主軸方向
     * @param gap         子コンポーネント間のギャップ (px)
     * @param gravity     子コンポーネント群全体の配置
     */
    public LinearLayout(Orientation orientation, int gap, int gravity) {
        this.orientation = orientation;
        this.gap = gap;
        this.gravity = gravity;
        this.weightSum = 0;
    }

    // --- プロパティ ---

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public float getWeightSum() {
        return weightSum;
    }

    public void setWeightSum(float weightSum) {
        this.weightSum = weightSum;
    }

    // --- LayoutManager2 ---

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints == null) {
            constraintsMap.put(comp, new LinearConstraints());
        } else if (constraints instanceof LinearConstraints lc) {
            constraintsMap.put(comp, lc.clone());
        } else {
            throw new IllegalArgumentException(
                    "constraints must be a LinearConstraints instance: " + constraints.getClass().getName());
        }
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        addLayoutComponent(comp, null);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        constraintsMap.remove(comp);
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

    // --- サイズ計算 ---

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return computeSize(parent, false);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return computeSize(parent, true);
    }

    private Dimension computeSize(Container parent, boolean minimum) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int mainAxis = 0;
            int crossAxis = 0;
            int visibleCount = 0;

            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component child = parent.getComponent(i);
                if (!child.isVisible()) continue;

                LinearConstraints lc = getConstraints(child);
                Dimension d = minimum ? child.getMinimumSize() : child.getPreferredSize();
                Insets m = lc.margin;

                if (orientation == Orientation.HORIZONTAL) {
                    mainAxis += d.width + m.left + m.right;
                    crossAxis = Math.max(crossAxis, d.height + m.top + m.bottom);
                } else {
                    mainAxis += d.height + m.top + m.bottom;
                    crossAxis = Math.max(crossAxis, d.width + m.left + m.right);
                }
                visibleCount++;
            }

            if (visibleCount > 1) {
                mainAxis += gap * (visibleCount - 1);
            }

            if (orientation == Orientation.HORIZONTAL) {
                return new Dimension(
                        mainAxis + insets.left + insets.right,
                        crossAxis + insets.top + insets.bottom);
            } else {
                return new Dimension(
                        crossAxis + insets.left + insets.right,
                        mainAxis + insets.top + insets.bottom);
            }
        }
    }

    // --- レイアウト ---

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int availableWidth = parent.getWidth() - insets.left - insets.right;
            int availableHeight = parent.getHeight() - insets.top - insets.bottom;

            // 可視子コンポーネントを収集
            int visibleCount = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                if (parent.getComponent(i).isVisible()) visibleCount++;
            }
            if (visibleCount == 0) return;

            int totalGap = gap * (visibleCount - 1);

            if (orientation == Orientation.HORIZONTAL) {
                layoutHorizontal(parent, insets, availableWidth, availableHeight, totalGap);
            } else {
                layoutVertical(parent, insets, availableWidth, availableHeight, totalGap);
            }
        }
    }

    private void layoutHorizontal(Container parent, Insets insets,
                                   int availableWidth, int availableHeight, int totalGap) {
        // Phase 1: 主軸サイズ決定
        int fixedTotal = totalGap;
        float totalWeight = 0;

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            LinearConstraints lc = getConstraints(child);
            Insets m = lc.margin;

            if (lc.weight > 0) {
                totalWeight += lc.weight;
                fixedTotal += m.left + m.right;
            } else {
                fixedTotal += child.getPreferredSize().width + m.left + m.right;
            }
        }

        float effectiveWeightSum = weightSum > 0 ? weightSum : totalWeight;
        int excess = Math.max(0, availableWidth - fixedTotal);

        // 主軸サイズを配列に格納
        int[] widths = new int[parent.getComponentCount()];
        float remainingWeight = effectiveWeightSum;
        int remainingExcess = excess;

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            LinearConstraints lc = getConstraints(child);

            if (lc.weight > 0 && effectiveWeightSum > 0) {
                int share;
                if (remainingWeight == lc.weight) {
                    // 最後の weight 持ち → 残り全部
                    share = remainingExcess;
                } else {
                    share = Math.round(excess * lc.weight / effectiveWeightSum);
                }
                widths[i] = share;
                remainingExcess -= share;
                remainingWeight -= lc.weight;
            } else {
                widths[i] = child.getPreferredSize().width;
            }
        }

        // Phase 2: 主軸オフセット（gravity の水平成分）
        int usedWidth = totalGap;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            LinearConstraints lc = getConstraints(child);
            usedWidth += widths[i] + lc.margin.left + lc.margin.right;
        }
        int mainOffset = computeMainOffset(availableWidth - usedWidth, true);

        // Phase 3: 配置
        int x = insets.left + mainOffset;
        boolean first = true;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            LinearConstraints lc = getConstraints(child);
            Insets m = lc.margin;

            if (!first) x += gap;
            first = false;

            x += m.left;
            int childWidth = widths[i];

            // 副軸（垂直）
            int crossGravity = resolveCrossGravity(lc.gravity, false);
            int childHeight;
            int y;
            int crossAvailable = availableHeight - m.top - m.bottom;

            if (crossGravity == Gravity.FILL_VERTICAL) {
                childHeight = crossAvailable;
                y = insets.top + m.top;
            } else if (crossGravity == Gravity.CENTER_VERTICAL) {
                childHeight = child.getPreferredSize().height;
                y = insets.top + m.top + (crossAvailable - childHeight) / 2;
            } else if (crossGravity == Gravity.BOTTOM) {
                childHeight = child.getPreferredSize().height;
                y = insets.top + m.top + crossAvailable - childHeight;
            } else {
                // TOP or default
                childHeight = child.getPreferredSize().height;
                y = insets.top + m.top;
            }

            child.setBounds(x, y, childWidth, childHeight);
            x += childWidth + m.right;
        }
    }

    private void layoutVertical(Container parent, Insets insets,
                                 int availableWidth, int availableHeight, int totalGap) {
        // Phase 1: 主軸サイズ決定
        int fixedTotal = totalGap;
        float totalWeight = 0;

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            LinearConstraints lc = getConstraints(child);
            Insets m = lc.margin;

            if (lc.weight > 0) {
                totalWeight += lc.weight;
                fixedTotal += m.top + m.bottom;
            } else {
                fixedTotal += child.getPreferredSize().height + m.top + m.bottom;
            }
        }

        float effectiveWeightSum = weightSum > 0 ? weightSum : totalWeight;
        int excess = Math.max(0, availableHeight - fixedTotal);

        int[] heights = new int[parent.getComponentCount()];
        float remainingWeight = effectiveWeightSum;
        int remainingExcess = excess;

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            LinearConstraints lc = getConstraints(child);

            if (lc.weight > 0 && effectiveWeightSum > 0) {
                int share;
                if (remainingWeight == lc.weight) {
                    share = remainingExcess;
                } else {
                    share = Math.round(excess * lc.weight / effectiveWeightSum);
                }
                heights[i] = share;
                remainingExcess -= share;
                remainingWeight -= lc.weight;
            } else {
                heights[i] = child.getPreferredSize().height;
            }
        }

        // Phase 2: 主軸オフセット
        int usedHeight = totalGap;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            LinearConstraints lc = getConstraints(child);
            usedHeight += heights[i] + lc.margin.top + lc.margin.bottom;
        }
        int mainOffset = computeMainOffset(availableHeight - usedHeight, false);

        // Phase 3: 配置
        int y = insets.top + mainOffset;
        boolean first = true;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            LinearConstraints lc = getConstraints(child);
            Insets m = lc.margin;

            if (!first) y += gap;
            first = false;

            y += m.top;
            int childHeight = heights[i];

            // 副軸（水平）
            int crossGravity = resolveCrossGravity(lc.gravity, true);
            int childWidth;
            int x;
            int crossAvailable = availableWidth - m.left - m.right;

            if (crossGravity == Gravity.FILL_HORIZONTAL) {
                childWidth = crossAvailable;
                x = insets.left + m.left;
            } else if (crossGravity == Gravity.CENTER_HORIZONTAL) {
                childWidth = child.getPreferredSize().width;
                x = insets.left + m.left + (crossAvailable - childWidth) / 2;
            } else if (crossGravity == Gravity.RIGHT) {
                childWidth = child.getPreferredSize().width;
                x = insets.left + m.left + crossAvailable - childWidth;
            } else {
                // LEFT or default
                childWidth = child.getPreferredSize().width;
                x = insets.left + m.left;
            }

            child.setBounds(x, y, childWidth, childHeight);
            y += childHeight + m.bottom;
        }
    }

    /**
     * 主軸方向の gravity によるオフセットを計算する。
     */
    private int computeMainOffset(int remaining, boolean horizontal) {
        if (remaining <= 0) return 0;

        int mainGravity;
        if (horizontal) {
            mainGravity = Gravity.getHorizontal(gravity, Gravity.LEFT);
        } else {
            mainGravity = Gravity.getVertical(gravity, Gravity.TOP);
        }

        if (mainGravity == Gravity.CENTER_HORIZONTAL || mainGravity == Gravity.CENTER_VERTICAL) {
            return remaining / 2;
        } else if (mainGravity == Gravity.RIGHT || mainGravity == Gravity.BOTTOM) {
            return remaining;
        }
        return 0;
    }

    /**
     * 副軸方向の gravity を解決する。子の gravity → 親の gravity → FILL。
     */
    private int resolveCrossGravity(int childGravity, boolean crossIsHorizontal) {
        if (crossIsHorizontal) {
            int g = Gravity.getHorizontal(childGravity, Gravity.NONE);
            if (g == Gravity.NONE) {
                g = Gravity.getHorizontal(gravity, Gravity.FILL_HORIZONTAL);
            }
            return g;
        } else {
            int g = Gravity.getVertical(childGravity, Gravity.NONE);
            if (g == Gravity.NONE) {
                g = Gravity.getVertical(gravity, Gravity.FILL_VERTICAL);
            }
            return g;
        }
    }

    /**
     * コンポーネントの制約を取得する。未登録の場合はデフォルト制約を返す。
     */
    private LinearConstraints getConstraints(Component comp) {
        LinearConstraints lc = constraintsMap.get(comp);
        return lc != null ? lc : new LinearConstraints();
    }
}

package org.to0mi1.swuit.layout.relative;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.to0mi1.swuit.layout.Gravity;

/**
 * Android の {@code RelativeLayout} に相当するレイアウトマネージャー。
 * <p>
 * 子コンポーネントを他のコンポーネントや親コンテナとの相対的な位置関係で配置する。
 * {@link RelativeConstraints} で制約を指定する。
 *
 * <pre>{@code
 * JPanel panel = new JPanel(new RelativeLayout());
 * JLabel header = new JLabel("Header");
 * JLabel content = new JLabel("Content");
 *
 * panel.add(header, new RelativeConstraints()
 *     .alignParentTop().alignParentLeft().alignParentRight());
 * panel.add(content, new RelativeConstraints()
 *     .below(header).alignParentLeft().alignParentRight());
 * }</pre>
 */
public class RelativeLayout implements LayoutManager2 {

    private int gravity;

    private final Map<Component, RelativeConstraints> constraintsMap = new LinkedHashMap<>();

    /**
     * デフォルト設定でレイアウトを作成する。
     */
    public RelativeLayout() {
        this(Gravity.NONE);
    }

    /**
     * gravity を指定してレイアウトを作成する。
     *
     * @param gravity 将来の拡張用
     */
    public RelativeLayout(int gravity) {
        this.gravity = gravity;
    }

    // --- プロパティ ---

    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    // --- LayoutManager2 ---

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints == null) {
            constraintsMap.put(comp, new RelativeConstraints());
        } else if (constraints instanceof RelativeConstraints rc) {
            constraintsMap.put(comp, rc.clone());
        } else {
            throw new IllegalArgumentException(
                    "constraints must be a RelativeConstraints instance: " + constraints.getClass().getName());
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

            List<Component> visible = collectVisible(parent);
            if (visible.isEmpty()) {
                return new Dimension(insets.left + insets.right, insets.top + insets.bottom);
            }

            // 大きめの仮サイズでレイアウトシミュレーション
            int simWidth = 100000;
            int simHeight = 100000;

            Map<Component, ChildBounds> boundsMap = new HashMap<>();
            for (Component c : visible) {
                boundsMap.put(c, new ChildBounds());
            }

            List<Component> hOrder = topologicalSort(visible, true);
            List<Component> vOrder = topologicalSort(visible, false);

            resolveHorizontal(hOrder, boundsMap, insets.left, simWidth - insets.right, minimum, true);
            resolveVertical(vOrder, boundsMap, insets.top, simHeight - insets.bottom, minimum, true);

            int maxRight = 0;
            int maxBottom = 0;
            for (Component c : visible) {
                ChildBounds cb = boundsMap.get(c);
                RelativeConstraints rc = getConstraints(c);
                Insets m = rc.margin;
                maxRight = Math.max(maxRight, cb.right + m.right);
                maxBottom = Math.max(maxBottom, cb.bottom + m.bottom);
            }

            return new Dimension(
                    maxRight - insets.left + insets.left + insets.right,
                    maxBottom - insets.top + insets.top + insets.bottom);
        }
    }

    // --- レイアウト ---

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int parentLeft = insets.left;
            int parentTop = insets.top;
            int parentRight = parent.getWidth() - insets.right;
            int parentBottom = parent.getHeight() - insets.bottom;

            List<Component> visible = collectVisible(parent);
            if (visible.isEmpty()) return;

            Map<Component, ChildBounds> boundsMap = new HashMap<>();
            for (Component c : visible) {
                boundsMap.put(c, new ChildBounds());
            }

            List<Component> hOrder = topologicalSort(visible, true);
            List<Component> vOrder = topologicalSort(visible, false);

            resolveHorizontal(hOrder, boundsMap, parentLeft, parentRight, false, false);
            resolveVertical(vOrder, boundsMap, parentTop, parentBottom, false, false);

            for (Component c : visible) {
                ChildBounds cb = boundsMap.get(c);
                c.setBounds(cb.left, cb.top, cb.right - cb.left, cb.bottom - cb.top);
            }
        }
    }

    // --- 水平パス ---

    private void resolveHorizontal(List<Component> order, Map<Component, ChildBounds> boundsMap,
                                    int parentLeft, int parentRight, boolean minimum, boolean sizeMode) {
        for (Component child : order) {
            RelativeConstraints rc = getConstraints(child);
            ChildBounds cb = boundsMap.get(child);
            Insets m = rc.margin;

            int prefWidth = minimum ? child.getMinimumSize().width : child.getPreferredSize().width;

            int left = Integer.MIN_VALUE;
            int right = Integer.MIN_VALUE;

            // 左端の決定
            if (rc.isEnabled(RelativeConstraints.ALIGN_PARENT_LEFT) && !sizeMode) {
                left = parentLeft + m.left;
            }
            if (rc.hasAnchor(RelativeConstraints.RIGHT_OF)) {
                Component anchor = (Component) rc.getRule(RelativeConstraints.RIGHT_OF);
                ChildBounds ab = boundsMap.get(anchor);
                if (ab != null) {
                    Insets am = getConstraints(anchor).margin;
                    left = ab.right + am.right + m.left;
                }
            }
            if (rc.hasAnchor(RelativeConstraints.ALIGN_LEFT)) {
                Component anchor = (Component) rc.getRule(RelativeConstraints.ALIGN_LEFT);
                ChildBounds ab = boundsMap.get(anchor);
                if (ab != null) {
                    left = ab.left + m.left;
                }
            }

            // 右端の決定
            if (rc.isEnabled(RelativeConstraints.ALIGN_PARENT_RIGHT) && !sizeMode) {
                right = parentRight - m.right;
            }
            if (rc.hasAnchor(RelativeConstraints.LEFT_OF)) {
                Component anchor = (Component) rc.getRule(RelativeConstraints.LEFT_OF);
                ChildBounds ab = boundsMap.get(anchor);
                if (ab != null) {
                    Insets am = getConstraints(anchor).margin;
                    right = ab.left - am.left - m.right;
                }
            }
            if (rc.hasAnchor(RelativeConstraints.ALIGN_RIGHT)) {
                Component anchor = (Component) rc.getRule(RelativeConstraints.ALIGN_RIGHT);
                ChildBounds ab = boundsMap.get(anchor);
                if (ab != null) {
                    right = ab.right - m.right;
                }
            }

            // 未解決端の補完
            if (left == Integer.MIN_VALUE && right == Integer.MIN_VALUE) {
                left = parentLeft + m.left;
                right = left + prefWidth;
            } else if (left != Integer.MIN_VALUE && right == Integer.MIN_VALUE) {
                right = left + prefWidth;
            } else if (left == Integer.MIN_VALUE) {
                left = right - prefWidth;
            }
            // 両方解決済み → ストレッチ

            // CENTER_HORIZONTAL / CENTER_IN_PARENT
            if (!sizeMode && (rc.isEnabled(RelativeConstraints.CENTER_HORIZONTAL)
                    || rc.isEnabled(RelativeConstraints.CENTER_IN_PARENT))) {
                int width = right - left;
                int parentCenter = (parentLeft + parentRight) / 2;
                left = parentCenter - width / 2;
                right = left + width;
            }

            cb.left = left;
            cb.right = right;
        }
    }

    // --- 垂直パス ---

    private void resolveVertical(List<Component> order, Map<Component, ChildBounds> boundsMap,
                                  int parentTop, int parentBottom, boolean minimum, boolean sizeMode) {
        for (Component child : order) {
            RelativeConstraints rc = getConstraints(child);
            ChildBounds cb = boundsMap.get(child);
            Insets m = rc.margin;

            int prefHeight = minimum ? child.getMinimumSize().height : child.getPreferredSize().height;

            int top = Integer.MIN_VALUE;
            int bottom = Integer.MIN_VALUE;

            // 上端の決定
            if (rc.isEnabled(RelativeConstraints.ALIGN_PARENT_TOP) && !sizeMode) {
                top = parentTop + m.top;
            }
            if (rc.hasAnchor(RelativeConstraints.BELOW)) {
                Component anchor = (Component) rc.getRule(RelativeConstraints.BELOW);
                ChildBounds ab = boundsMap.get(anchor);
                if (ab != null) {
                    Insets am = getConstraints(anchor).margin;
                    top = ab.bottom + am.bottom + m.top;
                }
            }
            if (rc.hasAnchor(RelativeConstraints.ALIGN_TOP)) {
                Component anchor = (Component) rc.getRule(RelativeConstraints.ALIGN_TOP);
                ChildBounds ab = boundsMap.get(anchor);
                if (ab != null) {
                    top = ab.top + m.top;
                }
            }

            // 下端の決定
            if (rc.isEnabled(RelativeConstraints.ALIGN_PARENT_BOTTOM) && !sizeMode) {
                bottom = parentBottom - m.bottom;
            }
            if (rc.hasAnchor(RelativeConstraints.ABOVE)) {
                Component anchor = (Component) rc.getRule(RelativeConstraints.ABOVE);
                ChildBounds ab = boundsMap.get(anchor);
                if (ab != null) {
                    Insets am = getConstraints(anchor).margin;
                    bottom = ab.top - am.top - m.bottom;
                }
            }
            if (rc.hasAnchor(RelativeConstraints.ALIGN_BOTTOM)) {
                Component anchor = (Component) rc.getRule(RelativeConstraints.ALIGN_BOTTOM);
                ChildBounds ab = boundsMap.get(anchor);
                if (ab != null) {
                    bottom = ab.bottom - m.bottom;
                }
            }

            // 未解決端の補完
            if (top == Integer.MIN_VALUE && bottom == Integer.MIN_VALUE) {
                top = parentTop + m.top;
                bottom = top + prefHeight;
            } else if (top != Integer.MIN_VALUE && bottom == Integer.MIN_VALUE) {
                bottom = top + prefHeight;
            } else if (top == Integer.MIN_VALUE) {
                top = bottom - prefHeight;
            }

            // CENTER_VERTICAL / CENTER_IN_PARENT
            if (!sizeMode && (rc.isEnabled(RelativeConstraints.CENTER_VERTICAL)
                    || rc.isEnabled(RelativeConstraints.CENTER_IN_PARENT))) {
                int height = bottom - top;
                int parentCenter = (parentTop + parentBottom) / 2;
                top = parentCenter - height / 2;
                bottom = top + height;
            }

            cb.top = top;
            cb.bottom = bottom;
        }
    }

    // --- トポロジカルソート（Kahn のアルゴリズム） ---

    private List<Component> topologicalSort(List<Component> visible, boolean horizontal) {
        // 可視コンポーネントのセット
        Map<Component, Integer> inDegree = new LinkedHashMap<>();
        Map<Component, List<Component>> adjacency = new HashMap<>();

        for (Component c : visible) {
            inDegree.put(c, 0);
            adjacency.put(c, new ArrayList<>());
        }

        // 依存辺を構築
        for (Component c : visible) {
            RelativeConstraints rc = getConstraints(c);
            int[] deps = horizontal
                    ? new int[]{RelativeConstraints.LEFT_OF, RelativeConstraints.RIGHT_OF,
                                RelativeConstraints.ALIGN_LEFT, RelativeConstraints.ALIGN_RIGHT}
                    : new int[]{RelativeConstraints.ABOVE, RelativeConstraints.BELOW,
                                RelativeConstraints.ALIGN_TOP, RelativeConstraints.ALIGN_BOTTOM};

            for (int dep : deps) {
                if (rc.hasAnchor(dep)) {
                    Component anchor = (Component) rc.getRule(dep);
                    if (inDegree.containsKey(anchor)) {
                        // anchor → c (anchor が先に解決される)
                        adjacency.get(anchor).add(c);
                        inDegree.merge(c, 1, Integer::sum);
                    }
                }
            }
        }

        // Kahn のアルゴリズム
        List<Component> queue = new ArrayList<>();
        for (Map.Entry<Component, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<Component> sorted = new ArrayList<>();
        int head = 0;
        while (head < queue.size()) {
            Component c = queue.get(head++);
            sorted.add(c);
            for (Component neighbor : adjacency.get(c)) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);
                if (newDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (sorted.size() != visible.size()) {
            throw new IllegalStateException("Circular dependency detected in RelativeLayout constraints");
        }

        return sorted;
    }

    // --- ユーティリティ ---

    private List<Component> collectVisible(Container parent) {
        List<Component> visible = new ArrayList<>();
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                visible.add(c);
            }
        }
        return visible;
    }

    private RelativeConstraints getConstraints(Component comp) {
        RelativeConstraints rc = constraintsMap.get(comp);
        return rc != null ? rc : new RelativeConstraints();
    }

    // --- 内部データ構造 ---

    private static class ChildBounds {
        int left = Integer.MIN_VALUE;
        int right = Integer.MIN_VALUE;
        int top = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
    }
}

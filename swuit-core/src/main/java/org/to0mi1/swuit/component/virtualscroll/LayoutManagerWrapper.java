package org.to0mi1.swuit.component.virtualscroll;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Rectangle;

/**
 * 元の {@link LayoutManager} をラップし、レイアウト計算時に全子コンポーネントを一時的に
 * 可視状態にすることで、{@code setVisible(false)} による仮想化とレイアウト精度を両立する。
 */
class LayoutManagerWrapper implements LayoutManager2 {

    private final LayoutManager delegate;
    private final VirtualViewport viewport;
    private boolean laying;

    LayoutManagerWrapper(LayoutManager delegate, VirtualViewport viewport) {
        this.delegate = delegate;
        this.viewport = viewport;
    }

    LayoutManager getDelegate() {
        return delegate;
    }

    // === LayoutManager ===

    @Override
    public void addLayoutComponent(String name, Component comp) {
        delegate.addLayoutComponent(name, comp);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        delegate.removeLayoutComponent(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return computeDelegateSize(parent, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return computeDelegateSize(parent, false);
    }

    @Override
    public void layoutContainer(Container parent) {
        if (laying) {
            delegate.layoutContainer(parent);
            return;
        }
        laying = true;
        viewport.setSuppressUpdate(true);
        try {
            boolean[] saved = saveAndShowAll(parent);
            try {
                delegate.layoutContainer(parent);
            } finally {
                restoreVisibility(parent, saved);
            }
            applyVirtualization(parent);
        } finally {
            viewport.setSuppressUpdate(false);
            laying = false;
        }
    }

    // === LayoutManager2 ===

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (delegate instanceof LayoutManager2 lm2) {
            lm2.addLayoutComponent(comp, constraints);
        }
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        if (delegate instanceof LayoutManager2 lm2) {
            return lm2.maximumLayoutSize(target);
        }
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        if (delegate instanceof LayoutManager2 lm2) {
            return lm2.getLayoutAlignmentX(target);
        }
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        if (delegate instanceof LayoutManager2 lm2) {
            return lm2.getLayoutAlignmentY(target);
        }
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
        if (delegate instanceof LayoutManager2 lm2) {
            lm2.invalidateLayout(target);
        }
    }

    // === 内部 ===

    private Dimension computeDelegateSize(Container parent, boolean preferred) {
        if (laying) {
            return preferred
                    ? delegate.preferredLayoutSize(parent)
                    : delegate.minimumLayoutSize(parent);
        }
        laying = true;
        viewport.setSuppressUpdate(true);
        try {
            boolean[] saved = saveAndShowAll(parent);
            try {
                return preferred
                        ? delegate.preferredLayoutSize(parent)
                        : delegate.minimumLayoutSize(parent);
            } finally {
                restoreVisibility(parent, saved);
            }
        } finally {
            viewport.setSuppressUpdate(false);
            laying = false;
        }
    }

    private boolean[] saveAndShowAll(Container parent) {
        int count = parent.getComponentCount();
        boolean[] saved = new boolean[count];
        for (int i = 0; i < count; i++) {
            Component child = parent.getComponent(i);
            saved[i] = child.isVisible();
            if (!child.isVisible() && viewport.isManagedHidden(child)) {
                child.setVisible(true);
            }
        }
        return saved;
    }

    private void restoreVisibility(Container parent, boolean[] saved) {
        int count = parent.getComponentCount();
        for (int i = 0; i < count; i++) {
            parent.getComponent(i).setVisible(saved[i]);
        }
    }

    private void applyVirtualization(Container parent) {
        Rectangle expanded = viewport.getExpandedViewRect();
        if (expanded == null) {
            return;
        }
        int count = parent.getComponentCount();
        for (int i = 0; i < count; i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) {
                // ユーザーが明示的に非表示にした子 → そのまま
                continue;
            }
            if (child.getBounds().intersects(expanded)) {
                viewport.removeManagedHidden(child);
            } else {
                child.setVisible(false);
                viewport.addManagedHidden(child);
            }
        }
    }
}

package org.to0mi1.swuit.component.virtualscroll;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * {@link JViewport} を拡張し、ビューの子コンポーネントの可視性をスクロール位置に応じて切り替える。
 */
class VirtualViewport extends JViewport implements ChangeListener {

    private final Set<Component> managedHidden =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private int bufferZone = 50;
    private boolean suppressUpdate;

    VirtualViewport() {
        addChangeListener(this);
    }

    void setBufferZone(int bufferZone) {
        this.bufferZone = bufferZone;
    }

    int getBufferZone() {
        return bufferZone;
    }

    // === ChangeListener ===

    @Override
    public void stateChanged(ChangeEvent e) {
        updateChildVisibility();
    }

    // === ビューの管理 ===

    @Override
    public void setView(Component view) {
        Component oldView = getView();
        if (oldView != null) {
            unwrapLayoutManager(oldView);
            managedHidden.clear();
        }
        super.setView(view);
        if (view != null) {
            wrapLayoutManager(view);
        }
    }

    // === managedHidden 管理 ===

    boolean isManagedHidden(Component comp) {
        return managedHidden.contains(comp);
    }

    void addManagedHidden(Component comp) {
        managedHidden.add(comp);
    }

    void removeManagedHidden(Component comp) {
        managedHidden.remove(comp);
    }

    void setSuppressUpdate(boolean suppress) {
        this.suppressUpdate = suppress;
    }

    // === viewRect 拡張 ===

    Rectangle getExpandedViewRect() {
        Rectangle viewRect = getViewRect();
        if (viewRect.width == 0 && viewRect.height == 0) {
            return null;
        }
        return new Rectangle(
                viewRect.x - bufferZone,
                viewRect.y - bufferZone,
                viewRect.width + bufferZone * 2,
                viewRect.height + bufferZone * 2
        );
    }

    // === 内部 ===

    private void updateChildVisibility() {
        if (suppressUpdate) {
            return;
        }
        Component view = getView();
        if (view == null) {
            return;
        }
        Rectangle expanded = getExpandedViewRect();
        if (expanded == null) {
            return;
        }
        if (view instanceof java.awt.Container container) {
            for (int i = 0; i < container.getComponentCount(); i++) {
                Component child = container.getComponent(i);
                Rectangle bounds = child.getBounds();
                if (bounds.width == 0 && bounds.height == 0) {
                    // レイアウト未実行の子 → スキップ（wrapper の layoutContainer で処理される）
                    continue;
                }
                if (!child.isVisible() && !isManagedHidden(child)) {
                    // ユーザーが明示的に非表示にした子 → スキップ
                    continue;
                }
                if (bounds.intersects(expanded)) {
                    if (!child.isVisible()) {
                        child.setVisible(true);
                        removeManagedHidden(child);
                    }
                } else {
                    if (child.isVisible()) {
                        child.setVisible(false);
                        addManagedHidden(child);
                    }
                }
            }
        }
    }

    private void wrapLayoutManager(Component view) {
        if (view instanceof java.awt.Container container) {
            LayoutManager lm = container.getLayout();
            if (lm != null && !(lm instanceof LayoutManagerWrapper)) {
                container.setLayout(new LayoutManagerWrapper(lm, this));
            }
        }
    }

    private void unwrapLayoutManager(Component view) {
        if (view instanceof java.awt.Container container) {
            LayoutManager lm = container.getLayout();
            if (lm instanceof LayoutManagerWrapper wrapper) {
                container.setLayout(wrapper.getDelegate());
            }
        }
    }
}

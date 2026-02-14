package org.to0mi1.swuit.layout.flex;

import java.awt.Component;
import java.awt.Insets;

/**
 * レイアウト計算用の1アイテムデータ。
 */
class CssFlexItem {
    final Component component;
    final CssFlexConstraints constraints;
    final int addOrder; // 追加順（安定ソート用）

    // flex-basis（主軸方向の初期サイズ）
    int flexBasis;

    // 計算結果
    int mainSize;   // 主軸サイズ（margin 除く）
    int crossSize;  // 副軸サイズ（margin 除く）
    int mainPos;    // 主軸位置（margin 除く）
    int crossPos;   // 副軸位置（margin 除く）

    boolean frozen; // grow/shrink 計算でフリーズ済みか

    CssFlexItem(Component component, CssFlexConstraints constraints, int addOrder) {
        this.component = component;
        this.constraints = constraints;
        this.addOrder = addOrder;
    }

    Insets margin() {
        return constraints.getMargin();
    }

    /** 主軸方向のマージン合計。 */
    int mainMargin(boolean horizontal) {
        Insets m = margin();
        return horizontal ? m.left + m.right : m.top + m.bottom;
    }

    /** 副軸方向のマージン合計。 */
    int crossMargin(boolean horizontal) {
        Insets m = margin();
        return horizontal ? m.top + m.bottom : m.left + m.right;
    }

    /** 主軸方向の開始マージン。 */
    int mainMarginStart(boolean horizontal) {
        Insets m = margin();
        return horizontal ? m.left : m.top;
    }

    /** 副軸方向の開始マージン。 */
    int crossMarginStart(boolean horizontal) {
        Insets m = margin();
        return horizontal ? m.top : m.left;
    }

    /** 主軸方向の最小サイズ。 */
    int mainMinSize(boolean horizontal) {
        if (horizontal) {
            return constraints.getMinWidth() >= 0
                    ? constraints.getMinWidth()
                    : component.getMinimumSize().width;
        } else {
            return constraints.getMinHeight() >= 0
                    ? constraints.getMinHeight()
                    : component.getMinimumSize().height;
        }
    }

    /** 主軸方向の最大サイズ。 */
    int mainMaxSize(boolean horizontal) {
        if (horizontal) {
            return constraints.getMaxWidth() >= 0
                    ? constraints.getMaxWidth()
                    : Integer.MAX_VALUE;
        } else {
            return constraints.getMaxHeight() >= 0
                    ? constraints.getMaxHeight()
                    : Integer.MAX_VALUE;
        }
    }

    /** 副軸方向の最小サイズ。 */
    int crossMinSize(boolean horizontal) {
        if (horizontal) {
            return constraints.getMinHeight() >= 0
                    ? constraints.getMinHeight()
                    : component.getMinimumSize().height;
        } else {
            return constraints.getMinWidth() >= 0
                    ? constraints.getMinWidth()
                    : component.getMinimumSize().width;
        }
    }

    /** 副軸方向の最大サイズ。 */
    int crossMaxSize(boolean horizontal) {
        if (horizontal) {
            return constraints.getMaxHeight() >= 0
                    ? constraints.getMaxHeight()
                    : Integer.MAX_VALUE;
        } else {
            return constraints.getMaxWidth() >= 0
                    ? constraints.getMaxWidth()
                    : Integer.MAX_VALUE;
        }
    }
}

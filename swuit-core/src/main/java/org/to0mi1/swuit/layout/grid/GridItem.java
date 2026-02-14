package org.to0mi1.swuit.layout.grid;

import java.awt.Component;

/**
 * レイアウト計算用の1アイテムデータ。
 */
class GridItem {

    final Component component;
    final GridConstraints constraints;
    final int addOrder;

    // 解決済みグリッド位置
    int column;
    int row;
    int columnSpan;
    int rowSpan;

    GridItem(Component component, GridConstraints constraints, int addOrder) {
        this.component = component;
        this.constraints = constraints;
        this.addOrder = addOrder;
        this.column = constraints.getColumn();
        this.row = constraints.getRow();
        this.columnSpan = constraints.getColumnSpan();
        this.rowSpan = constraints.getRowSpan();
    }

    boolean isPositioned() {
        return column >= 0 && row >= 0;
    }
}

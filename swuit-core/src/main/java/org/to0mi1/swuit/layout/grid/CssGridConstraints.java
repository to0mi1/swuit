package org.to0mi1.swuit.layout.grid;

import java.awt.Insets;

/**
 * {@link CssGridLayout} の子コンポーネントに適用する制約。
 * <p>
 * Fluent API でチェーン記述が可能。
 * <p>
 * column と row は両方指定するか、両方省略（自動配置）すること。
 * 片方のみ指定した場合は自動配置として扱われる。
 *
 * <pre>{@code
 * panel.add(component, new CssGridConstraints()
 *     .column(0).row(0).columnSpan(3)
 *     .justifySelf(CssJustifySelf.CENTER));
 * }</pre>
 */
public class CssGridConstraints implements Cloneable {

    private int column = -1;
    private int row = -1;
    private int columnSpan = 1;
    private int rowSpan = 1;
    private CssJustifySelf justifySelf = CssJustifySelf.AUTO;
    private CssAlignSelf alignSelf = CssAlignSelf.AUTO;
    private Insets margin = new Insets(0, 0, 0, 0);

    // --- Fluent API ---

    /**
     * 列位置を設定する。-1 で自動配置。
     *
     * @throws IllegalArgumentException column が -1 未満の場合
     */
    public CssGridConstraints column(int column) {
        if (column < -1) throw new IllegalArgumentException("column must be >= -1: " + column);
        this.column = column;
        return this;
    }

    /**
     * 行位置を設定する。-1 で自動配置。
     *
     * @throws IllegalArgumentException row が -1 未満の場合
     */
    public CssGridConstraints row(int row) {
        if (row < -1) throw new IllegalArgumentException("row must be >= -1: " + row);
        this.row = row;
        return this;
    }

    /**
     * 列スパン（結合数）を設定する。
     *
     * @throws IllegalArgumentException columnSpan が 1 未満の場合
     */
    public CssGridConstraints columnSpan(int columnSpan) {
        if (columnSpan < 1) throw new IllegalArgumentException("columnSpan must be >= 1: " + columnSpan);
        this.columnSpan = columnSpan;
        return this;
    }

    /**
     * 行スパン（結合数）を設定する。
     *
     * @throws IllegalArgumentException rowSpan が 1 未満の場合
     */
    public CssGridConstraints rowSpan(int rowSpan) {
        if (rowSpan < 1) throw new IllegalArgumentException("rowSpan must be >= 1: " + rowSpan);
        this.rowSpan = rowSpan;
        return this;
    }

    /** 個別の水平方向配置を設定する。 */
    public CssGridConstraints justifySelf(CssJustifySelf justifySelf) {
        this.justifySelf = justifySelf;
        return this;
    }

    /** 個別の垂直方向配置を設定する。 */
    public CssGridConstraints alignSelf(CssAlignSelf alignSelf) {
        this.alignSelf = alignSelf;
        return this;
    }

    /**
     * マージンを設定する。
     *
     * @param top    上マージン
     * @param left   左マージン
     * @param bottom 下マージン
     * @param right  右マージン
     */
    public CssGridConstraints margin(int top, int left, int bottom, int right) {
        this.margin = new Insets(top, left, bottom, right);
        return this;
    }

    // --- package-private ゲッター ---

    int getColumn() {
        return column;
    }

    int getRow() {
        return row;
    }

    int getColumnSpan() {
        return columnSpan;
    }

    int getRowSpan() {
        return rowSpan;
    }

    CssJustifySelf getCssJustifySelf() {
        return justifySelf;
    }

    CssAlignSelf getCssAlignSelf() {
        return alignSelf;
    }

    Insets getMargin() {
        return margin;
    }

    // --- clone ---

    @Override
    public CssGridConstraints clone() {
        try {
            CssGridConstraints c = (CssGridConstraints) super.clone();
            c.margin = new Insets(margin.top, margin.left, margin.bottom, margin.right);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

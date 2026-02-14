package org.to0mi1.swuit.layout.flex;

import java.awt.Insets;

/**
 * {@link CssFlexLayout} の子コンポーネントに適用する制約。
 * <p>
 * Fluent API でチェーン記述が可能。
 *
 * <pre>{@code
 * panel.add(component, new CssFlexConstraints()
 *     .flexGrow(1).alignSelf(CssAlignSelf.CENTER).margin(5, 10, 5, 10));
 * }</pre>
 */
public class CssFlexConstraints implements Cloneable {

    private float flexGrow = 0;
    private float flexShrink = 1;
    private float flexBasisPercent = -1;
    private CssAlignSelf alignSelf = CssAlignSelf.AUTO;
    private int order = 0;
    private int minWidth = -1;
    private int minHeight = -1;
    private int maxWidth = -1;
    private int maxHeight = -1;
    private Insets margin = new Insets(0, 0, 0, 0);

    // --- Fluent API ---

    /** 余剰スペースの分配比率を設定する。 */
    public CssFlexConstraints flexGrow(float flexGrow) {
        this.flexGrow = flexGrow;
        return this;
    }

    /** 不足時の縮小比率を設定する。 */
    public CssFlexConstraints flexShrink(float flexShrink) {
        this.flexShrink = flexShrink;
        return this;
    }

    /** 初期サイズ (%) を設定する。-1 で preferredSize を使用。 */
    public CssFlexConstraints flexBasisPercent(float flexBasisPercent) {
        this.flexBasisPercent = flexBasisPercent;
        return this;
    }

    /** 個別の副軸配置を設定する。 */
    public CssFlexConstraints alignSelf(CssAlignSelf alignSelf) {
        this.alignSelf = alignSelf;
        return this;
    }

    /** 表示順序を設定する。 */
    public CssFlexConstraints order(int order) {
        this.order = order;
        return this;
    }

    /** 最小幅を設定する。-1 で minimumSize を使用。 */
    public CssFlexConstraints minWidth(int minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    /** 最小高さを設定する。-1 で minimumSize を使用。 */
    public CssFlexConstraints minHeight(int minHeight) {
        this.minHeight = minHeight;
        return this;
    }

    /** 最大幅を設定する。-1 で無制限。 */
    public CssFlexConstraints maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    /** 最大高さを設定する。-1 で無制限。 */
    public CssFlexConstraints maxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
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
    public CssFlexConstraints margin(int top, int left, int bottom, int right) {
        this.margin = new Insets(top, left, bottom, right);
        return this;
    }

    // --- package-private ゲッター ---

    float getFlexGrow() {
        return flexGrow;
    }

    float getFlexShrink() {
        return flexShrink;
    }

    float getFlexBasisPercent() {
        return flexBasisPercent;
    }

    CssAlignSelf getCssAlignSelf() {
        return alignSelf;
    }

    int getOrder() {
        return order;
    }

    int getMinWidth() {
        return minWidth;
    }

    int getMinHeight() {
        return minHeight;
    }

    int getMaxWidth() {
        return maxWidth;
    }

    int getMaxHeight() {
        return maxHeight;
    }

    Insets getMargin() {
        return margin;
    }

    // --- clone ---

    @Override
    public CssFlexConstraints clone() {
        try {
            CssFlexConstraints c = (CssFlexConstraints) super.clone();
            c.margin = new Insets(margin.top, margin.left, margin.bottom, margin.right);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

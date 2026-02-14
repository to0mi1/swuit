package org.to0mi1.swuit.layout.relative;

import java.awt.Component;
import java.awt.Insets;

/**
 * {@link RelativeLayout} の子コンポーネントに適用する制約。
 * <p>
 * 各ルールは兄弟コンポーネントとの相対位置や、親コンテナとのアライメントを定義する。
 * Fluent API でチェーン記述が可能。
 *
 * <pre>{@code
 * panel.add(content, new RelativeConstraints()
 *     .below(header)
 *     .alignParentLeft()
 *     .alignParentRight());
 * }</pre>
 */
public class RelativeConstraints implements Cloneable {

    // --- 兄弟相対位置 (Component 参照) ---
    /** 指定コンポーネントの左側に配置 */
    public static final int LEFT_OF = 0;
    /** 指定コンポーネントの右側に配置 */
    public static final int RIGHT_OF = 1;
    /** 指定コンポーネントの上に配置 */
    public static final int ABOVE = 2;
    /** 指定コンポーネントの下に配置 */
    public static final int BELOW = 3;

    // --- 兄弟アライメント (Component 参照) ---
    /** 指定コンポーネントの左端に揃える */
    public static final int ALIGN_LEFT = 4;
    /** 指定コンポーネントの右端に揃える */
    public static final int ALIGN_RIGHT = 5;
    /** 指定コンポーネントの上端に揃える */
    public static final int ALIGN_TOP = 6;
    /** 指定コンポーネントの下端に揃える */
    public static final int ALIGN_BOTTOM = 7;

    // --- 親アライメント (boolean) ---
    /** 親の左端に揃える */
    public static final int ALIGN_PARENT_LEFT = 8;
    /** 親の右端に揃える */
    public static final int ALIGN_PARENT_RIGHT = 9;
    /** 親の上端に揃える */
    public static final int ALIGN_PARENT_TOP = 10;
    /** 親の下端に揃える */
    public static final int ALIGN_PARENT_BOTTOM = 11;

    // --- センタリング (boolean) ---
    /** 親の中央に配置 */
    public static final int CENTER_IN_PARENT = 12;
    /** 親の水平中央に配置 */
    public static final int CENTER_HORIZONTAL = 13;
    /** 親の垂直中央に配置 */
    public static final int CENTER_VERTICAL = 14;

    static final int RULE_COUNT = 15;

    private Object[] rules = new Object[RULE_COUNT];

    /**
     * コンポーネント周囲の余白。
     */
    public Insets margin;

    /**
     * デフォルト値で制約を作成する。
     */
    public RelativeConstraints() {
        this.margin = new Insets(0, 0, 0, 0);
    }

    /**
     * マージンを指定して制約を作成する。
     *
     * @param margin コンポーネント周囲の余白
     */
    public RelativeConstraints(Insets margin) {
        this.margin = margin != null
                ? new Insets(margin.top, margin.left, margin.bottom, margin.right)
                : new Insets(0, 0, 0, 0);
    }

    // --- package-private アクセサ ---

    /**
     * 指定ルールの値を取得する。
     * 兄弟ルール (0-7) の場合は {@link Component}、親/センタリングルール (8-14) の場合は {@link Boolean#TRUE}。
     */
    Object getRule(int rule) {
        return rules[rule];
    }

    /**
     * 指定ルールに {@link Component} アンカーが設定されているか。
     */
    boolean hasAnchor(int rule) {
        return rules[rule] instanceof Component;
    }

    /**
     * 指定ルール (boolean 型) が有効か。
     */
    boolean isEnabled(int rule) {
        return Boolean.TRUE.equals(rules[rule]);
    }

    // --- Fluent API: 兄弟相対位置 ---

    /** 指定コンポーネントの左側に配置 */
    public RelativeConstraints leftOf(Component anchor) {
        rules[LEFT_OF] = anchor;
        return this;
    }

    /** 指定コンポーネントの右側に配置 */
    public RelativeConstraints rightOf(Component anchor) {
        rules[RIGHT_OF] = anchor;
        return this;
    }

    /** 指定コンポーネントの上に配置 */
    public RelativeConstraints above(Component anchor) {
        rules[ABOVE] = anchor;
        return this;
    }

    /** 指定コンポーネントの下に配置 */
    public RelativeConstraints below(Component anchor) {
        rules[BELOW] = anchor;
        return this;
    }

    // --- Fluent API: 兄弟アライメント ---

    /** 指定コンポーネントの左端に揃える */
    public RelativeConstraints alignLeft(Component anchor) {
        rules[ALIGN_LEFT] = anchor;
        return this;
    }

    /** 指定コンポーネントの右端に揃える */
    public RelativeConstraints alignRight(Component anchor) {
        rules[ALIGN_RIGHT] = anchor;
        return this;
    }

    /** 指定コンポーネントの上端に揃える */
    public RelativeConstraints alignTop(Component anchor) {
        rules[ALIGN_TOP] = anchor;
        return this;
    }

    /** 指定コンポーネントの下端に揃える */
    public RelativeConstraints alignBottom(Component anchor) {
        rules[ALIGN_BOTTOM] = anchor;
        return this;
    }

    // --- Fluent API: 親アライメント ---

    /** 親の左端に揃える */
    public RelativeConstraints alignParentLeft() {
        rules[ALIGN_PARENT_LEFT] = Boolean.TRUE;
        return this;
    }

    /** 親の右端に揃える */
    public RelativeConstraints alignParentRight() {
        rules[ALIGN_PARENT_RIGHT] = Boolean.TRUE;
        return this;
    }

    /** 親の上端に揃える */
    public RelativeConstraints alignParentTop() {
        rules[ALIGN_PARENT_TOP] = Boolean.TRUE;
        return this;
    }

    /** 親の下端に揃える */
    public RelativeConstraints alignParentBottom() {
        rules[ALIGN_PARENT_BOTTOM] = Boolean.TRUE;
        return this;
    }

    // --- Fluent API: センタリング ---

    /** 親の中央に配置 */
    public RelativeConstraints centerInParent() {
        rules[CENTER_IN_PARENT] = Boolean.TRUE;
        return this;
    }

    /** 親の水平中央に配置 */
    public RelativeConstraints centerHorizontal() {
        rules[CENTER_HORIZONTAL] = Boolean.TRUE;
        return this;
    }

    /** 親の垂直中央に配置 */
    public RelativeConstraints centerVertical() {
        rules[CENTER_VERTICAL] = Boolean.TRUE;
        return this;
    }

    // --- Fluent API: マージン ---

    /**
     * マージンを設定する。
     *
     * @param top    上マージン
     * @param left   左マージン
     * @param bottom 下マージン
     * @param right  右マージン
     * @return this
     */
    public RelativeConstraints margin(int top, int left, int bottom, int right) {
        this.margin = new Insets(top, left, bottom, right);
        return this;
    }

    // --- clone ---

    @Override
    public RelativeConstraints clone() {
        try {
            RelativeConstraints c = (RelativeConstraints) super.clone();
            c.rules = rules.clone();
            c.margin = new Insets(margin.top, margin.left, margin.bottom, margin.right);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

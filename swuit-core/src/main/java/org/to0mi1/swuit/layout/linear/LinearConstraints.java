package org.to0mi1.swuit.layout.linear;

import java.awt.Insets;

import org.to0mi1.swuit.layout.Gravity;

/**
 * {@link LinearLayout} の子コンポーネントに適用する制約。
 * <p>
 * {@link java.awt.GridBagConstraints} と同様に public フィールドを直接操作する。
 */
public class LinearConstraints implements Cloneable {

    /**
     * 余剰スペース分配比率。0 の場合は preferred サイズを使用する。
     */
    public float weight;

    /**
     * 副軸方向の配置。{@link Gravity#NONE} の場合は親の gravity に従う。
     */
    public int gravity;

    /**
     * コンポーネント周囲の余白。
     */
    public Insets margin;

    /**
     * デフォルト値で制約を作成する。
     */
    public LinearConstraints() {
        this.weight = 0.0f;
        this.gravity = Gravity.NONE;
        this.margin = new Insets(0, 0, 0, 0);
    }

    /**
     * weight を指定して制約を作成する。
     *
     * @param weight 余剰スペース分配比率
     */
    public LinearConstraints(float weight) {
        this.weight = weight;
        this.gravity = Gravity.NONE;
        this.margin = new Insets(0, 0, 0, 0);
    }

    /**
     * weight と gravity を指定して制約を作成する。
     *
     * @param weight  余剰スペース分配比率
     * @param gravity 副軸方向の配置
     */
    public LinearConstraints(float weight, int gravity) {
        this.weight = weight;
        this.gravity = gravity;
        this.margin = new Insets(0, 0, 0, 0);
    }

    /**
     * 全フィールドを指定して制約を作成する。
     *
     * @param weight  余剰スペース分配比率
     * @param gravity 副軸方向の配置
     * @param margin  コンポーネント周囲の余白
     */
    public LinearConstraints(float weight, int gravity, Insets margin) {
        this.weight = weight;
        this.gravity = gravity;
        this.margin = margin != null ? new Insets(margin.top, margin.left, margin.bottom, margin.right) : new Insets(0, 0, 0, 0);
    }

    @Override
    public LinearConstraints clone() {
        try {
            LinearConstraints c = (LinearConstraints) super.clone();
            c.margin = new Insets(margin.top, margin.left, margin.bottom, margin.right);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

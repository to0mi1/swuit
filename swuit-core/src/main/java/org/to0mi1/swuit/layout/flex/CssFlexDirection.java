package org.to0mi1.swuit.layout.flex;

/**
 * フレックスコンテナの主軸方向。
 */
public enum CssFlexDirection {
    /** 左から右（水平）。 */
    ROW,
    /** 右から左（水平逆順）。 */
    ROW_REVERSE,
    /** 上から下（垂直）。 */
    COLUMN,
    /** 下から上（垂直逆順）。 */
    COLUMN_REVERSE;

    /** 水平方向かどうかを返す。 */
    public boolean isHorizontal() {
        return this == ROW || this == ROW_REVERSE;
    }

    /** 逆順かどうかを返す。 */
    public boolean isReverse() {
        return this == ROW_REVERSE || this == COLUMN_REVERSE;
    }
}

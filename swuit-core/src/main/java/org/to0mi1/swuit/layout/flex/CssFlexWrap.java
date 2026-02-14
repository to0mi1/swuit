package org.to0mi1.swuit.layout.flex;

/**
 * フレックスコンテナの折り返しモード。
 */
public enum CssFlexWrap {
    /** 折り返しなし（1ライン）。 */
    NOWRAP,
    /** 主軸方向に溢れたら副軸正方向へ折り返す。 */
    WRAP,
    /** 主軸方向に溢れたら副軸逆方向へ折り返す。 */
    WRAP_REVERSE
}

package org.to0mi1.swuit.layout.flex;

/**
 * 個別アイテムの副軸方向の配置。{@link CssAlignItems} をアイテム単位でオーバーライドする。
 */
public enum CssAlignSelf {
    /** コンテナの {@link CssAlignItems} に従う。 */
    AUTO,
    /** 副軸の開始端に寄せる。 */
    FLEX_START,
    /** 副軸の終了端に寄せる。 */
    FLEX_END,
    /** 副軸の中央に寄せる。 */
    CENTER,
    /** 副軸方向に引き伸ばす。 */
    STRETCH
}

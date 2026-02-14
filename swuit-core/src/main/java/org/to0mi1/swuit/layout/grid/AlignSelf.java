package org.to0mi1.swuit.layout.grid;

/**
 * 個別アイテムの垂直方向の配置。{@link AlignItems} をアイテム単位でオーバーライドする。
 */
public enum AlignSelf {
    /** コンテナの {@link AlignItems} に従う。 */
    AUTO,
    /** セルの上端に寄せる。 */
    START,
    /** セルの下端に寄せる。 */
    END,
    /** セルの中央に寄せる。 */
    CENTER,
    /** セル高さに引き伸ばす。 */
    STRETCH
}

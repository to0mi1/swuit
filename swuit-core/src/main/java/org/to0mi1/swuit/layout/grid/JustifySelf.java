package org.to0mi1.swuit.layout.grid;

/**
 * 個別アイテムの水平方向の配置。{@link JustifyItems} をアイテム単位でオーバーライドする。
 */
public enum JustifySelf {
    /** コンテナの {@link JustifyItems} に従う。 */
    AUTO,
    /** セルの左端に寄せる。 */
    START,
    /** セルの右端に寄せる。 */
    END,
    /** セルの中央に寄せる。 */
    CENTER,
    /** セル幅に引き伸ばす。 */
    STRETCH
}

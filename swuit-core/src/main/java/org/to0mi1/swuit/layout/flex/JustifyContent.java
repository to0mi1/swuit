package org.to0mi1.swuit.layout.flex;

/**
 * 主軸方向のアイテム配置。
 */
public enum JustifyContent {
    /** 主軸の開始端に寄せる。 */
    FLEX_START,
    /** 主軸の終了端に寄せる。 */
    FLEX_END,
    /** 主軸の中央に寄せる。 */
    CENTER,
    /** 最初と最後のアイテムを両端に配置し、残りを均等配分する。 */
    SPACE_BETWEEN,
    /** 各アイテムの両側に均等なスペースを配置する。 */
    SPACE_AROUND,
    /** 各アイテム間および両端に均等なスペースを配置する。 */
    SPACE_EVENLY
}

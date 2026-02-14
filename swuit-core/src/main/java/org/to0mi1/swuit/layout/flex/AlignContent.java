package org.to0mi1.swuit.layout.flex;

/**
 * 複数ラインの副軸方向の配置。wrap モードでのみ効果がある。
 */
public enum AlignContent {
    /** 副軸の開始端に寄せる。 */
    FLEX_START,
    /** 副軸の終了端に寄せる。 */
    FLEX_END,
    /** 副軸の中央に寄せる。 */
    CENTER,
    /** 最初と最後のラインを両端に配置し、残りを均等配分する。 */
    SPACE_BETWEEN,
    /** 各ラインの両側に均等なスペースを配置する。 */
    SPACE_AROUND,
    /** 副軸方向に引き伸ばす。 */
    STRETCH
}

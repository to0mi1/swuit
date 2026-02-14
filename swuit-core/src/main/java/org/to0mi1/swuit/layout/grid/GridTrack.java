package org.to0mi1.swuit.layout.grid;

/**
 * トラック（列 or 行）の計算データ。
 */
class GridTrack {

    final TrackSize definition;

    int baseSize;  // 解決済みピクセルサイズ
    int offset;    // トラック開始位置

    GridTrack(TrackSize definition) {
        this.definition = definition;
    }
}

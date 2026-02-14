package org.to0mi1.swuit.layout.grid;

/**
 * トラック（列 or 行）の計算データ。
 */
class CssGridTrack {

    final CssTrackSize definition;

    int baseSize;  // 解決済みピクセルサイズ
    int offset;    // トラック開始位置

    CssGridTrack(CssTrackSize definition) {
        this.definition = definition;
    }
}

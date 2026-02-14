package org.to0mi1.swuit.layout.flex;

import java.util.ArrayList;
import java.util.List;

/**
 * 1本のフレックスラインデータ。
 */
class FlexLine {
    final List<FlexItem> items = new ArrayList<>();

    int mainSize;    // ライン全体の主軸サイズ（gap・margin 込み）
    int crossSize;   // ライン全体の副軸サイズ（アイテムの最大副軸サイズ）
    int crossOffset; // alignContent 適用後の副軸オフセット

    void add(FlexItem item) {
        items.add(item);
    }
}

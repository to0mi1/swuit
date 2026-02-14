package org.to0mi1.swuit.layout.flex;

import java.util.ArrayList;
import java.util.List;

/**
 * 1本のフレックスラインデータ。
 */
class CssFlexLine {
    final List<CssFlexItem> items = new ArrayList<>();

    int mainSize;    // ライン全体の主軸サイズ（gap・margin 込み）
    int crossSize;   // ライン全体の副軸サイズ（アイテムの最大副軸サイズ）
    int crossOffset; // alignContent 適用後の副軸オフセット

    void add(CssFlexItem item) {
        items.add(item);
    }
}

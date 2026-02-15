package org.to0mi1.swuit.component.recycler;

import java.awt.*;

/**
 * アイテムの装飾（区切り線、余白など）を定義する。
 * <p>
 * {@link RecyclerPane#addItemDecoration(ItemDecoration)} で登録する。
 */
public abstract class ItemDecoration {

    /**
     * 背景レイヤーに描画する（子コンポーネントの下）。
     *
     * @param g      グラフィクスコンテキスト
     * @param parent RecyclerPane
     */
    public void onDraw(Graphics g, RecyclerPane parent) {
    }

    /**
     * 前景レイヤーに描画する（子コンポーネントの上）。
     *
     * @param g      グラフィクスコンテキスト
     * @param parent RecyclerPane
     */
    public void onDrawOver(Graphics g, RecyclerPane parent) {
    }

    /**
     * アイテム周囲のオフセット（余白）を返す。
     *
     * @param position アイテム位置
     * @return 上下左右のオフセット
     */
    public Insets getItemOffsets(int position) {
        return new Insets(0, 0, 0, 0);
    }
}

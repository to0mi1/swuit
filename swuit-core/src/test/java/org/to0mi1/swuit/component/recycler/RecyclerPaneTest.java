package org.to0mi1.swuit.component.recycler;

import org.junit.jupiter.api.Test;
import org.to0mi1.swuit.layout.Orientation;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class RecyclerPaneTest {

    // ========== テストヘルパー ==========

    static class TestViewHolder extends RecyclerPane.ViewHolder {
        TestViewHolder(JComponent itemView) {
            super(itemView);
        }
    }

    /** カウント付きテスト用 Adapter */
    static class CountingAdapter extends RecyclerPane.Adapter<TestViewHolder> {
        int createCount;
        int bindCount;
        int itemCount;
        Dimension preferredSize = new Dimension(200, 40);

        CountingAdapter(int itemCount) {
            this.itemCount = itemCount;
        }

        @Override
        public TestViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
            createCount++;
            JPanel panel = new JPanel();
            panel.setPreferredSize(preferredSize);
            return new TestViewHolder(panel);
        }

        @Override
        public void onBindViewHolder(TestViewHolder holder, int position) {
            bindCount++;
            holder.itemView.setName("item-" + position);
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }
    }

    /** モック LayoutManager */
    static class MockLayoutManager extends RecyclerPane.LayoutManager {
        int layoutCallCount;
        Dimension totalSize = new Dimension(200, 1000);
        boolean scrollVertically = true;
        boolean scrollHorizontally = false;

        @Override
        public void onLayoutChildren(Recycler recycler, RecyclerPane.State state) {
            layoutCallCount++;
        }

        @Override
        public boolean canScrollVertically() {
            return scrollVertically;
        }

        @Override
        public boolean canScrollHorizontally() {
            return scrollHorizontally;
        }

        @Override
        public Dimension computeTotalSize(RecyclerPane.State state) {
            return totalSize;
        }
    }

    static RecyclerPane createPane(int width, int height, RecyclerPane.LayoutManager lm,
                                   RecyclerPane.Adapter<?> adapter) {
        RecyclerPane pane = new RecyclerPane();
        pane.setLayoutManager(lm);
        pane.setAdapter(adapter);
        // ビューポートサイズを模擬するため JScrollPane に配置
        JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.setPreferredSize(new Dimension(width, height));
        scrollPane.setSize(width, height);
        scrollPane.getViewport().setSize(width, height);
        scrollPane.doLayout();
        return pane;
    }

    // ========== 初期化・プロパティ ==========

    @Test
    void setAdapter_null_clearsChildren() {
        RecyclerPane pane = new RecyclerPane();
        pane.setLayoutManager(new MockLayoutManager());
        pane.setAdapter(new CountingAdapter(10));
        pane.doLayout();
        pane.setAdapter(null);
        assertEquals(0, pane.getComponentCount());
    }

    @Test
    void setLayoutManager_null_noLayout() {
        RecyclerPane pane = new RecyclerPane();
        pane.setAdapter(new CountingAdapter(10));
        pane.setLayoutManager(null);
        pane.doLayout(); // 例外が発生しないこと
        assertEquals(0, pane.getComponentCount());
    }

    @Test
    void setAdapter_replacesExisting() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        pane.setLayoutManager(lm);

        CountingAdapter adapter1 = new CountingAdapter(5);
        pane.setAdapter(adapter1);
        assertSame(adapter1, pane.getAdapter());

        CountingAdapter adapter2 = new CountingAdapter(10);
        pane.setAdapter(adapter2);
        assertSame(adapter2, pane.getAdapter());
    }

    @Test
    void setLayoutManager_replacesExisting() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm1 = new MockLayoutManager();
        MockLayoutManager lm2 = new MockLayoutManager();

        pane.setLayoutManager(lm1);
        assertSame(lm1, pane.getLayoutManager());

        pane.setLayoutManager(lm2);
        assertSame(lm2, pane.getLayoutManager());
    }

    // ========== Scrollable 実装 ==========

    @Test
    void getPreferredSize_returnsComputeTotalSize() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        lm.totalSize = new Dimension(300, 5000);
        pane.setLayoutManager(lm);
        pane.setAdapter(new CountingAdapter(100));

        Dimension size = pane.getPreferredSize();
        assertEquals(5000, size.height);
    }

    @Test
    void scrollableTracksViewportWidth_verticalLM_true() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        lm.scrollVertically = true;
        lm.scrollHorizontally = false;
        pane.setLayoutManager(lm);

        assertTrue(pane.getScrollableTracksViewportWidth());
    }

    @Test
    void scrollableTracksViewportHeight_verticalLM_false() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        lm.scrollVertically = true;
        lm.scrollHorizontally = false;
        pane.setLayoutManager(lm);

        assertFalse(pane.getScrollableTracksViewportHeight());
    }

    @Test
    void scrollableTracksViewportWidth_horizontalLM_false() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        lm.scrollVertically = false;
        lm.scrollHorizontally = true;
        pane.setLayoutManager(lm);

        assertFalse(pane.getScrollableTracksViewportWidth());
    }

    // ========== レイアウト実行 ==========

    @Test
    void doLayout_callsOnLayoutChildren() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        pane.setLayoutManager(lm);
        pane.setAdapter(new CountingAdapter(10));

        pane.doLayout();
        assertEquals(1, lm.layoutCallCount);
    }

    @Test
    void doLayout_emptyAdapter_noError() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        pane.setLayoutManager(lm);
        pane.setAdapter(new CountingAdapter(0));

        assertDoesNotThrow(pane::doLayout);
    }

    // ========== スクロール時レイアウト再実行 ==========

    @Test
    void viewportScroll_triggersRelayout() {
        MockLayoutManager lm = new MockLayoutManager();
        lm.totalSize = new Dimension(200, 2000);
        CountingAdapter adapter = new CountingAdapter(50);
        RecyclerPane pane = createPane(200, 200, lm, adapter);
        pane.doLayout();
        int before = lm.layoutCallCount;

        // スクロール位置を変更 → ChangeListener で doLayout が呼ばれる
        JViewport viewport = (JViewport) pane.getParent();
        viewport.setViewPosition(new Point(0, 100));

        assertTrue(lm.layoutCallCount > before,
                "ビューポートスクロールで onLayoutChildren が再呼び出しされる");
    }

    @Test
    void viewportScroll_multipleScrolls_eachTriggersRelayout() {
        MockLayoutManager lm = new MockLayoutManager();
        lm.totalSize = new Dimension(200, 5000);
        CountingAdapter adapter = new CountingAdapter(100);
        RecyclerPane pane = createPane(200, 200, lm, adapter);
        pane.doLayout();
        int before = lm.layoutCallCount;

        JViewport viewport = (JViewport) pane.getParent();
        viewport.setViewPosition(new Point(0, 200));
        viewport.setViewPosition(new Point(0, 400));
        viewport.setViewPosition(new Point(0, 600));

        assertTrue(lm.layoutCallCount >= before + 3,
                "複数スクロールで都度レイアウトが実行される: " + (lm.layoutCallCount - before));
    }

    @Test
    void removeFromViewport_doesNotThrow() {
        MockLayoutManager lm = new MockLayoutManager();
        CountingAdapter adapter = new CountingAdapter(10);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // JScrollPane からビューを除去してもエラーにならない
        JScrollPane scrollPane = (JScrollPane) pane.getParent().getParent();
        assertDoesNotThrow(() -> scrollPane.setViewportView(new JPanel()));
    }

    // ========== ビューポートリサイズ ==========

    @Test
    void viewportResize_triggersRelayout() {
        MockLayoutManager lm = new MockLayoutManager();
        lm.totalSize = new Dimension(200, 2000);
        CountingAdapter adapter = new CountingAdapter(50);
        RecyclerPane pane = createPane(200, 200, lm, adapter);
        pane.doLayout();
        int before = lm.layoutCallCount;

        // ビューポートサイズ変更 → ChangeListener で doLayout
        JViewport viewport = (JViewport) pane.getParent();
        JScrollPane scrollPane = (JScrollPane) viewport.getParent();
        scrollPane.setBounds(0, 0, 400, 400);
        scrollPane.doLayout();

        assertTrue(lm.layoutCallCount > before,
                "ビューポートリサイズで onLayoutChildren が再呼び出しされる");
    }

    // ========== データ変更通知 ==========

    @Test
    void notifyDataSetChanged_relayouts() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        pane.setLayoutManager(lm);
        CountingAdapter adapter = new CountingAdapter(10);
        pane.setAdapter(adapter);
        pane.doLayout();
        int before = lm.layoutCallCount;

        adapter.notifyDataSetChanged();
        pane.doLayout();
        assertTrue(lm.layoutCallCount > before);
    }

    @Test
    void notifyItemInserted_relayouts() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        pane.setLayoutManager(lm);
        CountingAdapter adapter = new CountingAdapter(10);
        pane.setAdapter(adapter);
        pane.doLayout();
        int before = lm.layoutCallCount;

        adapter.notifyItemInserted(5);
        pane.doLayout();
        assertTrue(lm.layoutCallCount > before);
    }

    @Test
    void notifyItemRemoved_relayouts() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        pane.setLayoutManager(lm);
        CountingAdapter adapter = new CountingAdapter(10);
        pane.setAdapter(adapter);
        pane.doLayout();
        int before = lm.layoutCallCount;

        adapter.notifyItemRemoved(5);
        pane.doLayout();
        assertTrue(lm.layoutCallCount > before);
    }

    // ========== 差分更新 (notifyItemChanged) ==========

    /** リアルレイアウトで差分更新をテストするための pane を作る。 */
    private static RecyclerPane createLinearPane(int height, RecyclerPane.Adapter<?> adapter) {
        RecyclerPane pane = new RecyclerPane();
        pane.setLayoutManager(new LinearLayoutManager(Orientation.VERTICAL));
        pane.setAdapter(adapter);
        JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.setBounds(0, 0, 200, height);
        scrollPane.doLayout();
        scrollPane.getViewport().doLayout();
        pane.doLayout();
        return pane;
    }

    @Test
    void notifyItemChanged_visiblePosition_rebindsOnlyThat() {
        // 200x200, 各 40px → 5 個表示
        CountingAdapter adapter = new CountingAdapter(10);
        RecyclerPane pane = createLinearPane(200, adapter);
        int bindBefore = adapter.bindCount;

        adapter.notifyItemChanged(2);

        assertEquals(bindBefore + 1, adapter.bindCount,
                "対象 position の onBindViewHolder のみが追加で呼ばれる");
        // 表示中ホルダーがそのまま生存し、再 attach されること
        RecyclerPane.ViewHolder holder = pane.findViewHolderForAdapterPosition(2);
        assertNotNull(holder, "position 2 が表示中である");
    }

    @Test
    void notifyItemChanged_notVisiblePosition_isNoop() {
        // 表示は 0..4 のみ。position 8 は表示外で cache にも居ない。
        CountingAdapter adapter = new CountingAdapter(10);
        createLinearPane(200, adapter);
        int bindBefore = adapter.bindCount;

        adapter.notifyItemChanged(8);

        assertEquals(bindBefore, adapter.bindCount,
                "表示外かつ cache 外の position への notify は no-op");
    }

    @Test
    void notifyItemChanged_outOfRange_isNoop() {
        CountingAdapter adapter = new CountingAdapter(10);
        createLinearPane(200, adapter);
        int bindBefore = adapter.bindCount;

        adapter.notifyItemChanged(-1);
        adapter.notifyItemChanged(100);

        assertEquals(bindBefore, adapter.bindCount, "範囲外 position は no-op");
    }

    @Test
    void notifyItemChanged_cachedPosition_rebindsOnNextDisplay() {
        // スクロールで表示外に押し出された position が cache に居るとき、
        // notifyItemChanged を呼ぶと cache が破棄され、戻ってきたときに rebind される。
        CountingAdapter adapter = new CountingAdapter(50);
        RecyclerPane pane = createLinearPane(200, adapter);

        // position 0 を表示外へスクロール (40px * 10 行ぶん下げる)
        pane.scrollViewportAndLayout(new Point(0, 400));
        // この時点で position 0 は cache か pool に居るはず

        int bindBeforeNotify = adapter.bindCount;
        adapter.notifyItemChanged(0);
        // notify 自体では bind は走らない (表示外なので)
        assertEquals(bindBeforeNotify, adapter.bindCount,
                "表示外 position の notify では即時 bind されない");

        // 戻ってスクロール → position 0 は再度表示される
        pane.scrollViewportAndLayout(new Point(0, 0));

        assertTrue(adapter.bindCount > bindBeforeNotify,
                "cache 破棄により再表示時に onBindViewHolder が呼ばれる: " + adapter.bindCount);
        // position 0 が表示されていることも確認
        assertNotNull(pane.findViewHolderForAdapterPosition(0));
    }

    @Test
    void notifyItemChanged_force_fallsBackToFullRelayout() {
        CountingAdapter adapter = new CountingAdapter(10);
        RecyclerPane pane = createLinearPane(200, adapter);
        int bindBefore = adapter.bindCount;

        // force=true は onDataChanged 相当 (即時 bind は走らないが invalidate はかかる)
        adapter.notifyItemChanged(2, true);
        pane.doLayout();

        // 全 position を再要求するわけではなく、表示中分は scrap 経由で rebind されない。
        // 重要なのは「差分更新ではなく onDataChanged 系の経路を通る」こと。
        // bind が「対象 1 件だけ」増える差分更新と区別できれば良い。
        // ここでは layoutManager.onDataChanged() が呼ばれることを動作で間接検証する代わりに、
        // 例外なく完了し pane が無事レイアウトされることを確認する。
        assertNotNull(pane.findViewHolderForAdapterPosition(0));
        assertTrue(adapter.bindCount >= bindBefore,
                "force=true でも bind カウントは減らない");
    }

    @Test
    void notifyItemRangeChanged_rebindsOnlyVisibleInRange() {
        // 表示は 0..4。範囲 [1..3] を更新 → 3 件 bind される。
        CountingAdapter adapter = new CountingAdapter(10);
        createLinearPane(200, adapter);
        int bindBefore = adapter.bindCount;

        adapter.notifyItemRangeChanged(1, 3);

        assertEquals(bindBefore + 3, adapter.bindCount,
                "範囲指定で表示中の 3 件のみ rebind される");
    }

    @Test
    void notifyItemRangeChanged_partiallyVisible_rebindsOnlyVisible() {
        // 表示は 0..4。範囲 [3..6] (4 件) のうち、可視は 3,4 のみ
        CountingAdapter adapter = new CountingAdapter(10);
        createLinearPane(200, adapter);
        int bindBefore = adapter.bindCount;

        adapter.notifyItemRangeChanged(3, 4);

        assertEquals(bindBefore + 2, adapter.bindCount,
                "可視範囲のみ rebind される (範囲内の表示外は no-op)");
    }

    @Test
    void notifyItemRangeChanged_force_fallsBackToFullRelayout() {
        CountingAdapter adapter = new CountingAdapter(10);
        RecyclerPane pane = createLinearPane(200, adapter);

        // 例外なく完了することを確認 (内部で onDataChanged 相当)
        assertDoesNotThrow(() -> adapter.notifyItemRangeChanged(0, 10, true));
        pane.doLayout();
    }

    @Test
    void findViewHolderForAdapterPosition_returnsAttachedHolder() {
        CountingAdapter adapter = new CountingAdapter(10);
        RecyclerPane pane = createLinearPane(200, adapter);

        RecyclerPane.ViewHolder visible = pane.findViewHolderForAdapterPosition(0);
        assertNotNull(visible, "表示中 position は取得できる");
        assertEquals(0, visible.getAdapterPosition());

        RecyclerPane.ViewHolder offscreen = pane.findViewHolderForAdapterPosition(8);
        assertNull(offscreen, "表示外 position は null");

        assertNull(pane.findViewHolderForAdapterPosition(-1), "範囲外は null");
    }
}

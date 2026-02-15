package org.to0mi1.swuit.component.recycler;

import org.junit.jupiter.api.Test;

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

    @Test
    void notifyItemChanged_relayouts() {
        RecyclerPane pane = new RecyclerPane();
        MockLayoutManager lm = new MockLayoutManager();
        pane.setLayoutManager(lm);
        CountingAdapter adapter = new CountingAdapter(10);
        pane.setAdapter(adapter);
        pane.doLayout();
        int before = lm.layoutCallCount;

        adapter.notifyItemChanged(5);
        pane.doLayout();
        assertTrue(lm.layoutCallCount > before);
    }
}

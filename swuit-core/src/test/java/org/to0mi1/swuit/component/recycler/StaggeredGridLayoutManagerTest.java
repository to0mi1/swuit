package org.to0mi1.swuit.component.recycler;

import org.junit.jupiter.api.Test;
import org.to0mi1.swuit.layout.Orientation;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.*;

class StaggeredGridLayoutManagerTest {

    // ========== テストヘルパー ==========

    static class TestViewHolder extends RecyclerPane.ViewHolder {
        TestViewHolder(JComponent itemView) {
            super(itemView);
        }
    }

    static class CountingAdapter extends RecyclerPane.Adapter<TestViewHolder> {
        int createCount;
        int bindCount;
        int itemCount;
        IntFunction<Dimension> sizeProvider;

        CountingAdapter(int itemCount) {
            this(itemCount, pos -> new Dimension(100, 40));
        }

        CountingAdapter(int itemCount, IntFunction<Dimension> sizeProvider) {
            this.itemCount = itemCount;
            this.sizeProvider = sizeProvider;
        }

        @Override
        public TestViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
            createCount++;
            JPanel panel = new JPanel();
            return new TestViewHolder(panel);
        }

        @Override
        public void onBindViewHolder(TestViewHolder holder, int position) {
            bindCount++;
            holder.itemView.setPreferredSize(sizeProvider.apply(position));
            holder.itemView.setName("item-" + position);
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }
    }

    static RecyclerPane createPane(int width, int height, RecyclerPane.LayoutManager lm,
                                   RecyclerPane.Adapter<?> adapter) {
        RecyclerPane pane = new RecyclerPane();
        pane.setLayoutManager(lm);
        pane.setAdapter(adapter);
        JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.setBounds(0, 0, width, height);
        scrollPane.doLayout();
        scrollPane.getViewport().doLayout();
        pane.doLayout();
        return pane;
    }

    static void simulateScroll(RecyclerPane pane, int x, int y) {
        pane.scrollViewportAndLayout(new Point(x, y));
    }

    static Component findItem(RecyclerPane pane, String name) {
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (name.equals(comp.getName()) && comp.isVisible()) {
                return comp;
            }
        }
        return null;
    }

    // ========== 基本配置 ==========

    @Test
    void vertical3cols_shortestColumnFirst() {
        // 高さが異なるアイテム → 最も短い列に配置
        CountingAdapter adapter = new CountingAdapter(6, pos -> {
            // item-0: 80px (列0), item-1: 40px (列1), item-2: 40px (列2)
            // item-3: 最短は列1か列2 (40px) → 列1に
            if (pos == 0) return new Dimension(100, 80);
            return new Dimension(100, 40);
        });
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        Component item3 = findItem(pane, "item-3");
        assertNotNull(item3);
        // item-3 は列1 (item-1の下、y=40) に配置される
        assertEquals(40, item3.getY(), "最短列(列1)の下に配置");
    }

    @Test
    void variableHeight_staggeredLayout() {
        // 各アイテムの高さが異なる → 互い違い配置
        CountingAdapter adapter = new CountingAdapter(6, pos -> {
            int[] heights = {80, 40, 60, 50, 30, 70};
            return new Dimension(100, heights[pos]);
        });
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        // 少なくとも全アイテムが配置されている
        for (int i = 0; i < 6; i++) {
            assertNotNull(findItem(pane, "item-" + i), "item-" + i + " が配置されている");
        }

        // 各列の高さが不均一であることを確認
        Component item0 = findItem(pane, "item-0");
        Component item1 = findItem(pane, "item-1");
        Component item2 = findItem(pane, "item-2");
        assertNotNull(item0);
        assertNotNull(item1);
        assertNotNull(item2);

        // 1行目は全て y=0
        assertEquals(0, item0.getY());
        assertEquals(0, item1.getY());
        assertEquals(0, item2.getY());
        // 高さは異なる
        assertNotEquals(item0.getHeight(), item1.getHeight());
    }

    @Test
    void columnWidth_dividedEqually() {
        CountingAdapter adapter = new CountingAdapter(6);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        int vpWidth = lm.getWidth();
        int expectedWidth = vpWidth / 3;

        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible()) {
                assertEquals(expectedWidth, comp.getWidth(),
                        "列幅が均等: " + comp.getName());
            }
        }
    }

    @Test
    void withGaps_correctSpacing() {
        CountingAdapter adapter = new CountingAdapter(6);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL)
                .setMainAxisGap(10)
                .setCrossAxisGap(5);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        Component item0 = findItem(pane, "item-0");
        Component item1 = findItem(pane, "item-1");
        assertNotNull(item0);
        assertNotNull(item1);

        // item-0 と item-1 は隣接列で gap=5 の間隔
        int vpWidth = lm.getWidth();
        int colWidth = (vpWidth - 5 * 2) / 3;
        assertEquals(0, item0.getX());
        assertEquals(colWidth + 5, item1.getX(), "crossAxisGap が反映");

        // item-3 は item-0 or item-1 の下に mainAxisGap=10 の間隔で配置
        Component item3 = findItem(pane, "item-3");
        assertNotNull(item3);
        assertTrue(item3.getY() >= 40 + 10, "mainAxisGap が反映");
    }

    // ========== スクロール ==========

    @Test
    void scrollDown_recyclesToppedItems() {
        CountingAdapter adapter = new CountingAdapter(50, pos -> new Dimension(100, 30 + (pos % 5) * 10));
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        simulateScroll(pane, 0, 300);

        assertTrue(adapter.createCount < 50,
                "全アイテム分は作成されない: " + adapter.createCount);
    }

    @Test
    void scrollDown_thenUp_reusesFromCache() {
        CountingAdapter adapter = new CountingAdapter(50, pos -> new Dimension(100, 30 + (pos % 5) * 10));
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        simulateScroll(pane, 0, 300);
        int createAfterScroll = adapter.createCount;

        simulateScroll(pane, 0, 0);
        assertTrue(adapter.createCount <= createAfterScroll + 10,
                "Cache から再利用: " + adapter.createCount);
    }

    @Test
    void onlyVisibleCreated() {
        CountingAdapter adapter = new CountingAdapter(100);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        assertTrue(adapter.createCount <= 20,
                "可視分のみ create: " + adapter.createCount);
    }

    // ========== エッジケース ==========

    @Test
    void allSameHeight_uniformGrid() {
        CountingAdapter adapter = new CountingAdapter(9);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        // 全アイテム同じ高さ → 通常グリッドと同等
        Component item0 = findItem(pane, "item-0");
        Component item3 = findItem(pane, "item-3");
        Component item6 = findItem(pane, "item-6");

        assertNotNull(item0);
        assertNotNull(item3);
        assertNotNull(item6);

        // 3列で均等配置 → item-3 は item-0 の下、item-6 は item-3 の下
        assertEquals(0, item0.getY());
        assertEquals(40, item3.getY(), "均等高さで通常グリッドと同等");
        assertEquals(80, item6.getY(), "3行目");
    }

    @Test
    void spanCount1_equivalentToLinear() {
        CountingAdapter adapter = new CountingAdapter(5);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(1, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        int vpWidth = lm.getWidth();

        // spanCount=1 → 全幅使い、一列配置
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible()) {
                assertEquals(vpWidth, comp.getWidth(),
                        "spanCount=1 で全幅使用: " + comp.getName());
            }
        }

        // 各アイテムが縦一列に並ぶ
        Component item0 = findItem(pane, "item-0");
        Component item1 = findItem(pane, "item-1");
        assertNotNull(item0);
        assertNotNull(item1);
        assertEquals(0, item0.getY());
        assertEquals(40, item1.getY(), "縦一列に配置");
    }

    // ========== Phase 1: setter の自動レイアウト更新 ==========

    @Test
    void setSpanCount_afterLayout_requestsLayout() {
        CountingAdapter adapter = new CountingAdapter(12);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        lm.setSpanCount(2);
        pane.doLayout();

        assertEquals(2, lm.getSpanCount());
    }

    @Test
    void setMainAxisGap_clearsCache() {
        CountingAdapter adapter = new CountingAdapter(6);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        lm.setMainAxisGap(20);
        pane.doLayout();

        // item-3 は item-0 の下に mainAxisGap=20 で配置
        Component item3 = findItem(pane, "item-3");
        assertNotNull(item3);
        assertTrue(item3.getY() >= 40 + 20, "mainAxisGap=20 適用後");
    }

    @Test
    void setCrossAxisGap_clearsCache() {
        CountingAdapter adapter = new CountingAdapter(6);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        lm.setCrossAxisGap(10);
        pane.doLayout();

        int vpWidth = lm.getWidth();
        int colWidth = (vpWidth - 10 * 2) / 3;

        Component item1 = findItem(pane, "item-1");
        assertNotNull(item1);
        assertEquals(colWidth + 10, item1.getX(), "crossAxisGap=10 適用後");
    }

    @Test
    void setOrientation_staggered_changesScrollDirection() {
        CountingAdapter adapter = new CountingAdapter(6);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        assertTrue(lm.canScrollVertically());
        assertFalse(lm.canScrollHorizontally());

        lm.setOrientation(Orientation.HORIZONTAL);
        pane.doLayout();

        assertFalse(lm.canScrollVertically());
        assertTrue(lm.canScrollHorizontally());
    }

    // ========== Phase 2: scrollToPosition ==========

    @Test
    void scrollToPosition_middle_correctOffset() {
        CountingAdapter adapter = new CountingAdapter(50);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        lm.scrollToPosition(20);

        assertNotNull(findItem(pane, "item-20"), "scrollToPosition(20) 後に item-20 が可視");
    }

    @Test
    void scrollToPosition_first_zero() {
        CountingAdapter adapter = new CountingAdapter(50);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        simulateScroll(pane, 0, 200);
        lm.scrollToPosition(0);

        assertNotNull(findItem(pane, "item-0"), "scrollToPosition(0) 後に item-0 が可視");
    }

    @Test
    void scrollToPositionWithOffset_appliesOffset() {
        CountingAdapter adapter = new CountingAdapter(50);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        lm.scrollToPositionWithOffset(10, 20);

        assertNotNull(findItem(pane, "item-10"), "offset 付きで item-10 が可視");
    }

    // ========== Phase 3: findFirst/LastVisibleItemPosition ==========

    @Test
    void findFirstVisibleItemPosition_initial_zero() {
        CountingAdapter adapter = new CountingAdapter(12);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        assertEquals(0, lm.findFirstVisibleItemPosition());
    }

    @Test
    void findLastVisibleItemPosition_initial_correct() {
        CountingAdapter adapter = new CountingAdapter(50);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        int last = lm.findLastVisibleItemPosition();
        assertTrue(last > 0, "最後の可視: " + last);
    }

    @Test
    void findFirstVisibleItemPosition_afterScroll_updated() {
        CountingAdapter adapter = new CountingAdapter(50);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        simulateScroll(pane, 0, 200);

        int first = lm.findFirstVisibleItemPosition();
        assertTrue(first > 0, "スクロール後の最初の可視: " + first);
    }

    @Test
    void emptyAdapter_returnsMinusOne() {
        CountingAdapter adapter = new CountingAdapter(0);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        assertEquals(-1, lm.findFirstVisibleItemPosition());
        assertEquals(-1, lm.findLastVisibleItemPosition());
    }

    // ========== Phase 5: GapStrategy ==========

    @Test
    void gapStrategy_none_accepted() {
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        lm.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        assertEquals(StaggeredGridLayoutManager.GAP_HANDLING_NONE, lm.getGapStrategy());
    }

    @Test
    void gapStrategy_moveItems_default() {
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        assertEquals(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS, lm.getGapStrategy());
    }

    @Test
    void invalidateSpanAssignments_clearsAssignments() {
        CountingAdapter adapter = new CountingAdapter(12);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        // invalidateSpanAssignments がエラーなく実行される
        assertDoesNotThrow(lm::invalidateSpanAssignments);
    }


    @Test
    void horizontal_correctLayout() {
        CountingAdapter adapter = new CountingAdapter(6, pos -> new Dimension(30 + (pos % 3) * 20, 100));
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.HORIZONTAL);
        RecyclerPane pane = createPane(400, 300, lm, adapter);

        // 全アイテムが配置されている
        for (int i = 0; i < 6; i++) {
            assertNotNull(findItem(pane, "item-" + i), "horizontal: item-" + i + " が配置されている");
        }

        // 1行目のアイテムは y=0
        Component item0 = findItem(pane, "item-0");
        assertNotNull(item0);
        assertEquals(0, item0.getY(), "horizontal: item-0 は y=0");
        assertEquals(0, item0.getX(), "horizontal: item-0 は x=0");
    }

    @Test
    void horizontal_scrollRight_recyclesLeftItems() {
        CountingAdapter adapter = new CountingAdapter(50, pos -> new Dimension(30 + (pos % 5) * 10, 100));
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.HORIZONTAL);
        RecyclerPane pane = createPane(200, 300, lm, adapter);

        simulateScroll(pane, 300, 0);

        assertTrue(adapter.createCount < 50,
                "水平スクロールで全アイテム分は作成されない: " + adapter.createCount);
    }


    @Test
    void constructor_spanCountZero_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> new StaggeredGridLayoutManager(0, Orientation.VERTICAL));
    }

    @Test
    void setSpanCount_negative_throwsIAE() {
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        assertThrows(IllegalArgumentException.class,
                () -> lm.setSpanCount(-1));
    }


    @Test
    void findFirstCompletelyVisibleItemPosition_initial() {
        CountingAdapter adapter = new CountingAdapter(12);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        int firstComplete = lm.findFirstCompletelyVisibleItemPosition();
        assertTrue(firstComplete >= 0, "初期の完全可視 first >= 0: " + firstComplete);
    }

    @Test
    void findLastCompletelyVisibleItemPosition_initial() {
        CountingAdapter adapter = new CountingAdapter(12);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        int lastComplete = lm.findLastCompletelyVisibleItemPosition();
        assertTrue(lastComplete > 0, "初期の完全可視 last > 0: " + lastComplete);
    }


    @Test
    void scrollToPosition_horizontal_correctItem() {
        CountingAdapter adapter = new CountingAdapter(50, pos -> new Dimension(30 + (pos % 5) * 10, 100));
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.HORIZONTAL);
        RecyclerPane pane = createPane(200, 300, lm, adapter);

        lm.scrollToPosition(20);

        assertNotNull(findItem(pane, "item-20"),
                "horizontal scrollToPosition(20) で item-20 が可視");
    }


    @Test
    void computeTotalSize_vertical_withGap_correct() {
        CountingAdapter adapter = new CountingAdapter(6);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(3, Orientation.VERTICAL)
                .setMainAxisGap(10);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        RecyclerPane.State state = new RecyclerPane.State(6);
        Dimension size = lm.computeTotalSize(state);

        // 3列, 6アイテム(各40px), gap=10 → 各列2アイテム → 40+10+40 = 90
        assertEquals(90, size.height, "gap=10 込みの totalHeight");
    }
}

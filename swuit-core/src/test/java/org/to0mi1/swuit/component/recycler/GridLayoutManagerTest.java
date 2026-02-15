package org.to0mi1.swuit.component.recycler;

import org.junit.jupiter.api.Test;
import org.to0mi1.swuit.layout.Orientation;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.*;

class GridLayoutManagerTest {

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
    void vertical3cols_itemBounds_correctGrid() {
        // 9アイテムを3列に配置 (3行)
        CountingAdapter adapter = new CountingAdapter(9);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        int vpWidth = lm.getWidth();
        int colWidth = vpWidth / 3;

        // item-0: (0, 0), item-1: (colWidth, 0), item-2: (2*colWidth, 0)
        Component item0 = findItem(pane, "item-0");
        Component item1 = findItem(pane, "item-1");
        Component item2 = findItem(pane, "item-2");
        Component item3 = findItem(pane, "item-3");

        assertNotNull(item0);
        assertNotNull(item1);
        assertNotNull(item2);
        assertNotNull(item3);

        assertEquals(0, item0.getX(), "item-0 は左端");
        assertEquals(colWidth, item1.getX(), "item-1 は2列目");
        assertEquals(2 * colWidth, item2.getX(), "item-2 は3列目");
        assertEquals(0, item3.getX(), "item-3 は次の行の左端");
        assertEquals(40, item3.getY(), "item-3 は2行目");
    }

    @Test
    void vertical3cols_itemWidth_dividedEqually() {
        CountingAdapter adapter = new CountingAdapter(6);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        int vpWidth = lm.getWidth();
        int expectedWidth = vpWidth / 3;

        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible()) {
                assertEquals(expectedWidth, comp.getWidth(),
                        "列幅が均等分割: " + comp.getName());
            }
        }
    }

    @Test
    void vertical3cols_rowHeight_maxInRow() {
        // 行内で高さが異なるアイテム
        CountingAdapter adapter = new CountingAdapter(6, pos -> {
            if (pos == 1) return new Dimension(100, 80); // 1行目の2番目が高い
            return new Dimension(100, 40);
        });
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        // 2行目のアイテムは y = 80 (1行目の最大高さ)
        Component item3 = findItem(pane, "item-3");
        assertNotNull(item3);
        assertEquals(80, item3.getY(), "2行目は1行目の最大高さ(80)の次から");
    }

    @Test
    void withGaps_correctSpacing() {
        CountingAdapter adapter = new CountingAdapter(9);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL)
                .setMainAxisGap(10)
                .setCrossAxisGap(5);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        int vpWidth = lm.getWidth();
        int colWidth = (vpWidth - 5 * 2) / 3; // 2つのcrossAxisGap

        Component item0 = findItem(pane, "item-0");
        Component item1 = findItem(pane, "item-1");
        Component item3 = findItem(pane, "item-3");

        assertNotNull(item0);
        assertNotNull(item1);
        assertNotNull(item3);

        assertEquals(0, item0.getX());
        assertEquals(colWidth + 5, item1.getX(), "crossAxisGap が反映");
        assertEquals(40 + 10, item3.getY(), "mainAxisGap が反映");
    }

    @Test
    void onlyVisibleRowsCreated() {
        // 100アイテム、3列 → 34行、各40px → 1360px
        // ビューポート200px → 5行分 (15アイテム) のみ作成
        CountingAdapter adapter = new CountingAdapter(100);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        assertTrue(adapter.createCount <= 20,
                "可視行のアイテムのみ create: " + adapter.createCount);
    }

    // ========== SpanSizeLookup ==========

    @Test
    void spanSize2_occupiesTwoColumns() {
        CountingAdapter adapter = new CountingAdapter(5);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? 2 : 1;
            }
        });
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        int vpWidth = lm.getWidth();
        int colWidth = vpWidth / 3;

        Component item0 = findItem(pane, "item-0");
        assertNotNull(item0);
        // spanSize=2 → 2列分の幅
        assertEquals(colWidth * 2, item0.getWidth(),
                "spanSize=2 のアイテムが2列占有");
    }

    @Test
    void spanSizeFullWidth_occupiesEntireRow() {
        CountingAdapter adapter = new CountingAdapter(4);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? 3 : 1;
            }
        });
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        int vpWidth = lm.getWidth();

        Component item0 = findItem(pane, "item-0");
        assertNotNull(item0);
        assertEquals(vpWidth, item0.getWidth(),
                "spanSize=spanCount で全幅占有");

        // item-1 は次の行
        Component item1 = findItem(pane, "item-1");
        assertNotNull(item1);
        assertEquals(40, item1.getY(), "item-1 は2行目");
    }

    @Test
    void mixedSpanSizes_correctLayout() {
        // 0: span=2, 1: span=1, 2: span=1, 3: span=1, 4: span=1
        // 行1: [0(2), 1(1)] → 3列使い切り
        // 行2: [2(1), 3(1), 4(1)] → 3列使い切り
        CountingAdapter adapter = new CountingAdapter(5);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? 2 : 1;
            }
        });
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        Component item0 = findItem(pane, "item-0");
        Component item1 = findItem(pane, "item-1");
        Component item2 = findItem(pane, "item-2");

        assertNotNull(item0);
        assertNotNull(item1);
        assertNotNull(item2);

        // item-0 と item-1 は同じ行
        assertEquals(item0.getY(), item1.getY(), "item-0 と item-1 は同一行");
        // item-2 は次の行
        assertTrue(item2.getY() > item0.getY(), "item-2 は次の行");
    }

    // ========== スクロール ==========

    @Test
    void scrollDown_recyclesToppedRow() {
        CountingAdapter adapter = new CountingAdapter(90);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        simulateScroll(pane, 0, 200);

        // create は制限されている
        assertTrue(adapter.createCount < 90,
                "全アイテム分は作成されない: " + adapter.createCount);
    }

    @Test
    void scrollDown_thenUp_reusesFromCache() {
        CountingAdapter adapter = new CountingAdapter(30);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        simulateScroll(pane, 0, 200);
        int createAfterScroll = adapter.createCount;

        simulateScroll(pane, 0, 0);
        assertTrue(adapter.createCount <= createAfterScroll + 3,
                "Cache から再利用: " + adapter.createCount);
    }

    // ========== 水平 ==========

    @Test
    void horizontal2rows_correctGrid() {
        CountingAdapter adapter = new CountingAdapter(6, pos -> new Dimension(80, 40));
        GridLayoutManager lm = new GridLayoutManager(2, Orientation.HORIZONTAL);
        RecyclerPane pane = createPane(400, 200, lm, adapter);

        int vpHeight = lm.getHeight();
        int rowHeight = vpHeight / 2;

        Component item0 = findItem(pane, "item-0");
        Component item1 = findItem(pane, "item-1");
        Component item2 = findItem(pane, "item-2");

        assertNotNull(item0);
        assertNotNull(item1);
        assertNotNull(item2);

        assertEquals(0, item0.getY(), "item-0 は1行目");
        assertEquals(rowHeight, item1.getY(), "item-1 は2行目");
        assertEquals(0, item2.getY(), "item-2 は次の列の1行目");
        assertTrue(item2.getX() > item0.getX(), "item-2 は右の列");
    }

    // ========== Phase 1: setter の自動レイアウト更新 ==========

    @Test
    void setSpanCount_afterLayout_requestsLayout() {
        CountingAdapter adapter = new CountingAdapter(12);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        // 列数を変更
        lm.setSpanCount(4);
        pane.doLayout();

        assertEquals(4, lm.getSpanCount());
    }

    @Test
    void setMainAxisGap_clearsCache() {
        CountingAdapter adapter = new CountingAdapter(9);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        // gap を変更して再レイアウト
        lm.setMainAxisGap(15);
        pane.doLayout();

        Component item3 = findItem(pane, "item-3");
        assertNotNull(item3);
        assertEquals(40 + 15, item3.getY(), "mainAxisGap=15 適用後");
    }

    @Test
    void setCrossAxisGap_clearsCache() {
        CountingAdapter adapter = new CountingAdapter(9);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
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
    void setOrientation_grid_changesScrollDirection() {
        CountingAdapter adapter = new CountingAdapter(6);
        GridLayoutManager lm = new GridLayoutManager(2, Orientation.VERTICAL);
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
        CountingAdapter adapter = new CountingAdapter(30);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        // position 15 にスクロール (行5: 5*40=200px)
        lm.scrollToPosition(15);

        assertNotNull(findItem(pane, "item-15"), "scrollToPosition(15) 後に item-15 が可視");
    }

    @Test
    void scrollToPosition_first_zero() {
        CountingAdapter adapter = new CountingAdapter(30);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        simulateScroll(pane, 0, 200);
        lm.scrollToPosition(0);

        assertNotNull(findItem(pane, "item-0"), "scrollToPosition(0) 後に item-0 が可視");
    }

    @Test
    void scrollToPositionWithOffset_appliesOffset() {
        CountingAdapter adapter = new CountingAdapter(30);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        lm.scrollToPositionWithOffset(9, 20);

        assertNotNull(findItem(pane, "item-9"), "offset 付きで item-9 が可視");
    }

    // ========== Phase 3: findFirst/LastVisibleItemPosition ==========

    @Test
    void findFirstVisibleItemPosition_initial_zero() {
        CountingAdapter adapter = new CountingAdapter(12);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        assertEquals(0, lm.findFirstVisibleItemPosition());
    }

    @Test
    void findLastVisibleItemPosition_initial_correct() {
        CountingAdapter adapter = new CountingAdapter(30);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        int last = lm.findLastVisibleItemPosition();
        // 200px / 40px = 5行, 5行 * 3列 = 15アイテム → last は14前後
        assertTrue(last >= 14 && last <= 17, "最後の可視: " + last);
    }

    @Test
    void findFirstVisibleItemPosition_afterScroll_updated() {
        CountingAdapter adapter = new CountingAdapter(30);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        simulateScroll(pane, 0, 200);

        int first = lm.findFirstVisibleItemPosition();
        // スクロール後は最初の可視位置が 0 より大きい
        assertTrue(first > 0, "スクロール後の最初の可視: " + first);
    }

    @Test
    void emptyAdapter_returnsMinusOne() {
        CountingAdapter adapter = new CountingAdapter(0);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        assertEquals(-1, lm.findFirstVisibleItemPosition());
        assertEquals(-1, lm.findLastVisibleItemPosition());
    }

    // ========== Phase 4: reverseLayout ==========

    @Test
    void reverseLayout_grid_firstRowAtBottom() {
        CountingAdapter adapter = new CountingAdapter(9);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        lm.setReverseLayout(true);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        // reverseLayout: 最初の行(item 6,7,8)が上、最後の行(item 0,1,2)が下
        Component item6 = findItem(pane, "item-6");
        Component item0 = findItem(pane, "item-0");
        assertNotNull(item6);
        assertNotNull(item0);
        assertTrue(item6.getY() < item0.getY(), "item-6 (最後の行) が item-0 (最初の行) より上");
    }

    @Test
    void reverseLayout_grid_correctBounds() {
        CountingAdapter adapter = new CountingAdapter(6);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        lm.setReverseLayout(true);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        // 6アイテム / 3列 = 2行
        // reverseLayout: 行1(items 3,4,5) が y=0、行0(items 0,1,2) が y=40
        Component item3 = findItem(pane, "item-3");
        Component item0 = findItem(pane, "item-0");
        assertNotNull(item3);
        assertNotNull(item0);
        assertEquals(0, item3.getY(), "reverse: 後の行が上");
        assertEquals(40, item0.getY(), "reverse: 前の行が下");
    }

    // ========== Phase 5: SpanSizeLookup 拡張 ==========

    @Test
    void getSpanIndex_firstRow_correct() {
        GridLayoutManager.SpanSizeLookup lookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        };

        assertEquals(0, lookup.getSpanIndex(0, 3));
        assertEquals(1, lookup.getSpanIndex(1, 3));
        assertEquals(2, lookup.getSpanIndex(2, 3));
        assertEquals(0, lookup.getSpanIndex(3, 3));
    }

    @Test
    void getSpanIndex_withSpanSize2_correct() {
        GridLayoutManager.SpanSizeLookup lookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? 2 : 1;
            }
        };

        // 行1: [0(span=2), 1(span=1)] → position 0 は index 0, position 1 は index 2
        assertEquals(0, lookup.getSpanIndex(0, 3));
        assertEquals(2, lookup.getSpanIndex(1, 3));
        // 行2: [2(span=1), 3(span=1), 4(span=1)]
        assertEquals(0, lookup.getSpanIndex(2, 3));
    }

    @Test
    void getSpanGroupIndex_correct() {
        GridLayoutManager.SpanSizeLookup lookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        };

        // 3列: pos 0,1,2 → group 0, pos 3,4,5 → group 1
        assertEquals(0, lookup.getSpanGroupIndex(0, 3));
        assertEquals(0, lookup.getSpanGroupIndex(1, 3));
        assertEquals(0, lookup.getSpanGroupIndex(2, 3));
        assertEquals(1, lookup.getSpanGroupIndex(3, 3));
        assertEquals(1, lookup.getSpanGroupIndex(4, 3));
    }

    @Test
    void invalidateSpanIndexCache_noError() {
        GridLayoutManager.SpanSizeLookup lookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        };

        assertDoesNotThrow(lookup::invalidateSpanIndexCache);
    }


    @Test
    void getSpanIndex_itemOverflowsRow_wrapsToNextRow() {
        // spanCount=4, spans=[2, 3, 1, 2]
        // row 0: [item-0(span=2)]          → spanUsed=2, item-1(span=3) は収まらない
        // row 1: [item-1(span=3), item-2(span=1)] → spanUsed=4
        // row 2: [item-3(span=2)]
        GridLayoutManager.SpanSizeLookup lookup = new GridLayoutManager.SpanSizeLookup() {
            final int[] spans = {2, 3, 1, 2};
            @Override
            public int getSpanSize(int position) {
                return spans[position];
            }
        };

        assertEquals(0, lookup.getSpanIndex(0, 4), "item-0: row 0, index 0");
        assertEquals(0, lookup.getSpanIndex(1, 4), "item-1: row 1, index 0 (span=3 は row 0 の残り 2 に収まらない)");
        assertEquals(3, lookup.getSpanIndex(2, 4), "item-2: row 1, index 3");
        assertEquals(0, lookup.getSpanIndex(3, 4), "item-3: row 2, index 0");
    }

    @Test
    void getSpanGroupIndex_itemOverflowsRow_correctGroup() {
        GridLayoutManager.SpanSizeLookup lookup = new GridLayoutManager.SpanSizeLookup() {
            final int[] spans = {2, 3, 1, 2};
            @Override
            public int getSpanSize(int position) {
                return spans[position];
            }
        };

        assertEquals(0, lookup.getSpanGroupIndex(0, 4), "item-0: group 0");
        assertEquals(1, lookup.getSpanGroupIndex(1, 4), "item-1: group 1 (overflow)");
        assertEquals(1, lookup.getSpanGroupIndex(2, 4), "item-2: group 1");
        assertEquals(2, lookup.getSpanGroupIndex(3, 4), "item-3: group 2");
    }


    @Test
    void setSpanCount_zero_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> new GridLayoutManager(0, Orientation.VERTICAL));
    }

    @Test
    void setSpanCount_negative_throwsIAE() {
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        assertThrows(IllegalArgumentException.class,
                () -> lm.setSpanCount(-1));
    }


    @Test
    void reverseLayout_scrollToPosition_correctItem() {
        CountingAdapter adapter = new CountingAdapter(30);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        lm.setReverseLayout(true);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        lm.scrollToPosition(10);

        assertNotNull(findItem(pane, "item-10"),
                "reverseLayout + scrollToPosition(10) で item-10 が可視");
    }


    @Test
    void findFirstCompletelyVisibleItemPosition_initial() {
        CountingAdapter adapter = new CountingAdapter(30);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        int firstComplete = lm.findFirstCompletelyVisibleItemPosition();
        assertTrue(firstComplete >= 0, "初期の完全可視 first >= 0: " + firstComplete);
    }

    @Test
    void findLastCompletelyVisibleItemPosition_initial() {
        CountingAdapter adapter = new CountingAdapter(30);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        int lastComplete = lm.findLastCompletelyVisibleItemPosition();
        assertTrue(lastComplete > 0, "初期の完全可視 last > 0: " + lastComplete);
    }


    @Test
    void computeTotalSize_vertical_withGap_correct() {
        CountingAdapter adapter = new CountingAdapter(9);
        GridLayoutManager lm = new GridLayoutManager(3, Orientation.VERTICAL)
                .setMainAxisGap(10);
        RecyclerPane pane = createPane(300, 400, lm, adapter);

        RecyclerPane.State state = new RecyclerPane.State(9);
        Dimension size = lm.computeTotalSize(state);

        // 3行 * 40px + 2 * 10px(gap) = 140
        assertEquals(140, size.height, "gap=10 込みの totalHeight");
    }
}

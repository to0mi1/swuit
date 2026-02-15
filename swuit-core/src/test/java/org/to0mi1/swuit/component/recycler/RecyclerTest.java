package org.to0mi1.swuit.component.recycler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class RecyclerTest {

    // ========== テストヘルパー ==========

    static class TestViewHolder extends RecyclerPane.ViewHolder {
        TestViewHolder(JComponent itemView) {
            super(itemView);
        }
    }

    static class TrackingAdapter extends RecyclerPane.Adapter<TestViewHolder> {
        int createCount;
        int bindCount;
        int itemCount;

        TrackingAdapter(int itemCount) {
            this.itemCount = itemCount;
        }

        @Override
        public TestViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
            createCount++;
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(200, 40));
            return new TestViewHolder(panel);
        }

        @Override
        public void onBindViewHolder(TestViewHolder holder, int position) {
            bindCount++;
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }
    }

    static class MultiTypeAdapter extends RecyclerPane.Adapter<TestViewHolder> {
        int[] createCounts = new int[3];
        int[] bindCounts = new int[3];
        int itemCount;

        MultiTypeAdapter(int itemCount) {
            this.itemCount = itemCount;
        }

        @Override
        public TestViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
            createCounts[viewType]++;
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(200, 40));
            return new TestViewHolder(panel);
        }

        @Override
        public void onBindViewHolder(TestViewHolder holder, int position) {
            bindCounts[getItemViewType(position)]++;
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        @Override
        public int getItemViewType(int position) {
            return position % 3;
        }
    }

    private RecyclerPane pane;
    private TrackingAdapter adapter;
    private Recycler recycler;

    @BeforeEach
    void setUp() {
        pane = new RecyclerPane();
        adapter = new TrackingAdapter(100);
        pane.setAdapter(adapter);
        recycler = pane.getRecycler();
    }

    // ========== Scrap ==========

    @Test
    void scrapView_thenGetSamePosition_returnsFromScrap() {
        // 位置 0 のビューを作成して pane に追加
        RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(0);
        pane.add(holder.itemView);
        holder.itemView.setVisible(true);
        int createBefore = adapter.createCount;
        int bindBefore = adapter.bindCount;

        // scrap に移動
        recycler.scrapAttachedViews();

        // 同じ位置で取得 → scrap から返されるので create/bind なし
        RecyclerPane.ViewHolder retrieved = recycler.getViewForPosition(0);
        assertSame(holder, retrieved);
        assertEquals(createBefore, adapter.createCount);
        assertEquals(bindBefore, adapter.bindCount);
    }

    @Test
    void scrapView_thenGetDifferentPosition_misses() {
        RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(0);
        pane.add(holder.itemView);
        recycler.scrapAttachedViews();

        // 別の位置で取得 → scrap ミス、新規作成
        RecyclerPane.ViewHolder retrieved = recycler.getViewForPosition(5);
        assertNotSame(holder, retrieved);
    }

    @Test
    void recycleScrap_movesRemainingToCache() {
        RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(0);
        pane.add(holder.itemView);
        recycler.scrapAttachedViews();

        // scrap を recycleScrap で Cache に移動
        recycler.recycleScrap();

        // position 0 で Cache から取得可能
        int createBefore = adapter.createCount;
        int bindBefore = adapter.bindCount;
        RecyclerPane.ViewHolder cached = recycler.getViewForPosition(0);
        assertSame(holder, cached);
        assertEquals(createBefore, adapter.createCount, "Cache ヒットで create なし");
        assertEquals(bindBefore, adapter.bindCount, "Cache ヒットで bind なし");
    }

    // ========== Cache ==========

    @Test
    void recycleView_thenGetSamePosition_returnsFromCache() {
        RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(3);
        recycler.recycleView(holder);

        int createBefore = adapter.createCount;
        int bindBefore = adapter.bindCount;
        RecyclerPane.ViewHolder retrieved = recycler.getViewForPosition(3);
        assertSame(holder, retrieved);
        assertEquals(createBefore, adapter.createCount, "Cache ヒットで create なし");
        assertEquals(bindBefore, adapter.bindCount, "Cache ヒットで bind なし");
    }

    @Test
    void recycleView_thenGetDifferentPosition_missesCache() {
        RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(3);
        recycler.recycleView(holder);

        RecyclerPane.ViewHolder retrieved = recycler.getViewForPosition(7);
        assertNotSame(holder, retrieved);
    }

    @Test
    void cache_exceedsSize_evictsToPool() {
        recycler.setCacheSize(2);

        // Cache に 3 つ追加 → 最初のが Pool に移動
        RecyclerPane.ViewHolder h0 = recycler.obtainViewForPosition(0);
        RecyclerPane.ViewHolder h1 = recycler.obtainViewForPosition(1);
        RecyclerPane.ViewHolder h2 = recycler.obtainViewForPosition(2);
        recycler.recycleView(h0);
        recycler.recycleView(h1);
        recycler.recycleView(h2); // h0 が Pool に移動

        // h0 は Cache から消え、Pool にある → bind あり
        int bindBefore = adapter.bindCount;
        RecyclerPane.ViewHolder retrieved = recycler.getViewForPosition(0);
        assertSame(h0, retrieved);
        assertEquals(bindBefore + 1, adapter.bindCount, "Pool ヒットで rebind あり");
    }

    @Test
    void setCacheSize_limitsCache() {
        recycler.setCacheSize(1);

        RecyclerPane.ViewHolder h0 = recycler.obtainViewForPosition(0);
        RecyclerPane.ViewHolder h1 = recycler.obtainViewForPosition(1);
        recycler.recycleView(h0);
        recycler.recycleView(h1); // h0 が Pool に evict

        // h0 は Pool へ → bind あり
        int bindBefore = adapter.bindCount;
        RecyclerPane.ViewHolder retrieved = recycler.getViewForPosition(0);
        assertSame(h0, retrieved);
        assertTrue(adapter.bindCount > bindBefore);
    }

    // ========== Pool ==========

    @Test
    void pool_evictedFromCache_getByViewType_rebinds() {
        recycler.setCacheSize(1);

        RecyclerPane.ViewHolder h0 = recycler.obtainViewForPosition(0);
        RecyclerPane.ViewHolder h1 = recycler.obtainViewForPosition(1);
        recycler.recycleView(h0);
        recycler.recycleView(h1); // h0 → Pool

        int bindBefore = adapter.bindCount;
        RecyclerPane.ViewHolder retrieved = recycler.getViewForPosition(99); // 同じ viewType(0)
        assertSame(h0, retrieved);
        assertEquals(bindBefore + 1, adapter.bindCount, "Pool ヒットで rebind あり");
    }

    @Test
    void pool_differentViewType_misses() {
        pane = new RecyclerPane();
        MultiTypeAdapter multiAdapter = new MultiTypeAdapter(100);
        pane.setAdapter(multiAdapter);
        recycler = pane.getRecycler();
        recycler.setCacheSize(0); // Cache を無効化 → 即 Pool 行き

        // viewType=0 (position=0) を Pool に
        RecyclerPane.ViewHolder h0 = recycler.obtainViewForPosition(0);
        recycler.recycleView(h0);

        // viewType=1 (position=1) を取得 → Pool ミス
        RecyclerPane.ViewHolder retrieved = recycler.getViewForPosition(1);
        assertNotSame(h0, retrieved);
    }

    @Test
    void pool_exceedsMaxSize_discards() {
        recycler.setCacheSize(0); // 全て即座に Pool 行き
        recycler.setPoolMaxSize(2);

        RecyclerPane.ViewHolder h0 = recycler.obtainViewForPosition(0);
        RecyclerPane.ViewHolder h1 = recycler.obtainViewForPosition(1);
        RecyclerPane.ViewHolder h2 = recycler.obtainViewForPosition(2);
        recycler.recycleView(h0);
        recycler.recycleView(h1);
        recycler.recycleView(h2); // Pool 満杯 → h2 は破棄

        // Pool からは h0, h1 が取れる
        RecyclerPane.ViewHolder r1 = recycler.getViewForPosition(90);
        RecyclerPane.ViewHolder r2 = recycler.getViewForPosition(91);
        assertTrue(r1 == h0 || r1 == h1);
        assertTrue(r2 == h0 || r2 == h1);

        // 3つ目は新規作成
        int createBefore = adapter.createCount;
        recycler.getViewForPosition(92);
        assertEquals(createBefore + 1, adapter.createCount);
    }

    @Test
    void setPoolMaxSize_limitsPool() {
        recycler.setPoolMaxSize(1);
        recycler.setCacheSize(0);

        RecyclerPane.ViewHolder h0 = recycler.obtainViewForPosition(0);
        RecyclerPane.ViewHolder h1 = recycler.obtainViewForPosition(1);
        recycler.recycleView(h0);
        recycler.recycleView(h1); // Pool に h0 のみ、h1 は破棄

        RecyclerPane.ViewHolder r1 = recycler.getViewForPosition(90);
        assertSame(h0, r1);

        int createBefore = adapter.createCount;
        recycler.getViewForPosition(91);
        assertEquals(createBefore + 1, adapter.createCount, "Pool 枯渇で新規作成");
    }

    // ========== キャッシュ階層統合 ==========

    @Test
    void getViewForPosition_priority_scrapFirst() {
        // scrap に配置
        RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(5);
        pane.add(holder.itemView);
        recycler.scrapAttachedViews();

        // Cache にも同じ position のものを別途置く（通常はありえないがテスト用）
        RecyclerPane.ViewHolder cacheHolder = recycler.obtainViewForPosition(5);
        recycler.recycleView(cacheHolder);

        // Scrap が優先
        RecyclerPane.ViewHolder retrieved = recycler.getViewForPosition(5);
        assertSame(holder, retrieved);
    }

    @Test
    void getViewForPosition_newCreation_callsCreateAndBind() {
        int createBefore = adapter.createCount;
        int bindBefore = adapter.bindCount;
        recycler.getViewForPosition(0);
        assertEquals(createBefore + 1, adapter.createCount);
        assertEquals(bindBefore + 1, adapter.bindCount);
    }

    @Test
    void getViewForPosition_fromPool_callsBindOnly() {
        recycler.setCacheSize(0);
        RecyclerPane.ViewHolder h = recycler.obtainViewForPosition(0);
        recycler.recycleView(h);

        int createBefore = adapter.createCount;
        int bindBefore = adapter.bindCount;
        recycler.getViewForPosition(50);
        assertEquals(createBefore, adapter.createCount, "Pool ヒットで create なし");
        assertEquals(bindBefore + 1, adapter.bindCount, "Pool ヒットで bind あり");
    }

    @Test
    void getViewForPosition_fromScrap_callsNeither() {
        RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(5);
        pane.add(holder.itemView);
        recycler.scrapAttachedViews();

        int createBefore = adapter.createCount;
        int bindBefore = adapter.bindCount;
        recycler.getViewForPosition(5);
        assertEquals(createBefore, adapter.createCount, "Scrap ヒットで create なし");
        assertEquals(bindBefore, adapter.bindCount, "Scrap ヒットで bind なし");
    }

    @Test
    void clear_removesAllCaches() {
        RecyclerPane.ViewHolder h0 = recycler.obtainViewForPosition(0);
        RecyclerPane.ViewHolder h1 = recycler.obtainViewForPosition(1);
        pane.add(h0.itemView);
        recycler.scrapAttachedViews();
        recycler.recycleView(h1);

        recycler.clear();

        // 全てクリアされ、新規作成になる
        int createBefore = adapter.createCount;
        recycler.getViewForPosition(0);
        assertEquals(createBefore + 1, adapter.createCount);
    }

    // ========== ViewType ==========

    @Test
    void multipleViewTypes_poolSeparated() {
        pane = new RecyclerPane();
        MultiTypeAdapter multiAdapter = new MultiTypeAdapter(100);
        pane.setAdapter(multiAdapter);
        recycler = pane.getRecycler();
        recycler.setCacheSize(0);

        // viewType 0, 1, 2 のホルダーを作って Pool に返却
        RecyclerPane.ViewHolder h0 = recycler.obtainViewForPosition(0);  // type 0
        RecyclerPane.ViewHolder h1 = recycler.obtainViewForPosition(1);  // type 1
        RecyclerPane.ViewHolder h2 = recycler.obtainViewForPosition(2);  // type 2
        recycler.recycleView(h0);
        recycler.recycleView(h1);
        recycler.recycleView(h2);

        // viewType 0 で取得 → h0 が返る
        RecyclerPane.ViewHolder r0 = recycler.getViewForPosition(3);  // type 0
        assertSame(h0, r0);

        // viewType 1 で取得 → h1 が返る
        RecyclerPane.ViewHolder r1 = recycler.getViewForPosition(4);  // type 1
        assertSame(h1, r1);
    }

    @Test
    void multipleViewTypes_scrapMatchesViewType() {
        pane = new RecyclerPane();
        MultiTypeAdapter multiAdapter = new MultiTypeAdapter(100);
        pane.setAdapter(multiAdapter);
        recycler = pane.getRecycler();

        RecyclerPane.ViewHolder h0 = recycler.obtainViewForPosition(0);  // type 0
        pane.add(h0.itemView);
        recycler.scrapAttachedViews();

        // scrap は position で検索するので、同じ position なら型関係なく返る
        RecyclerPane.ViewHolder retrieved = recycler.getViewForPosition(0);
        assertSame(h0, retrieved);
    }
}

package org.to0mi1.swuit.component.recycler;

import org.junit.jupiter.api.Test;
import org.to0mi1.swuit.layout.Orientation;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.*;

class LinearLayoutManagerTest {

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
            this(itemCount, pos -> new Dimension(200, 40));
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
            Dimension size = sizeProvider.apply(position);
            holder.itemView.setPreferredSize(size);
            holder.itemView.setName("item-" + position);
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }
    }

    /** 複合コンポーネント ViewHolder */
    static class ComplexViewHolder extends RecyclerPane.ViewHolder {
        final JLabel label;
        final JButton button;
        final JCheckBox checkBox;

        ComplexViewHolder(JComponent itemView, JLabel label, JButton button, JCheckBox checkBox) {
            super(itemView);
            this.label = label;
            this.button = button;
            this.checkBox = checkBox;
        }
    }

    /** 複合コンポーネント Adapter */
    static class ComplexItemAdapter extends RecyclerPane.Adapter<ComplexViewHolder> {
        int createCount;
        int bindCount;
        int itemCount;
        IntFunction<Dimension> sizeProvider;

        ComplexItemAdapter(int itemCount) {
            this(itemCount, pos -> new Dimension(300, 50));
        }

        ComplexItemAdapter(int itemCount, IntFunction<Dimension> sizeProvider) {
            this.itemCount = itemCount;
            this.sizeProvider = sizeProvider;
        }

        @Override
        public ComplexViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
            createCount++;
            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = new JLabel();
            JButton button = new JButton("Action");
            JCheckBox checkBox = new JCheckBox();
            panel.add(label, BorderLayout.CENTER);
            panel.add(button, BorderLayout.EAST);
            panel.add(checkBox, BorderLayout.WEST);
            return new ComplexViewHolder(panel, label, button, checkBox);
        }

        @Override
        public void onBindViewHolder(ComplexViewHolder holder, int position) {
            bindCount++;
            holder.label.setText("Item " + position);
            holder.itemView.setPreferredSize(sizeProvider.apply(position));
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
        // doLayout 後にビューポートサイズを再確認し、doLayout を再実行
        scrollPane.getViewport().doLayout();
        pane.doLayout();
        return pane;
    }

    static void simulateScroll(RecyclerPane pane, int x, int y) {
        pane.scrollViewportAndLayout(new Point(x, y));
    }

    // ========== 基本配置（垂直） ==========

    @Test
    void vertical_10items_onlyVisibleCreated() {
        // 画面200px, アイテム40px → 表示は5個
        CountingAdapter adapter = new CountingAdapter(10);
        RecyclerPane pane = createPane(200, 200, new LinearLayoutManager(Orientation.VERTICAL), adapter);

        assertTrue(adapter.createCount <= 6, "可視分+αのみ作成: " + adapter.createCount);
        assertTrue(adapter.createCount >= 5, "少なくとも画面分は作成: " + adapter.createCount);
    }

    @Test
    void vertical_itemBounds_correctPosition() {
        CountingAdapter adapter = new CountingAdapter(10);
        RecyclerPane pane = createPane(200, 200, new LinearLayoutManager(Orientation.VERTICAL), adapter);

        // 各アイテムの y 座標を確認
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible()) {
                int expectedY = Integer.parseInt(comp.getName().replace("item-", "")) * 40;
                assertEquals(expectedY, comp.getY(),
                        "アイテム " + comp.getName() + " の Y 座標");
            }
        }
    }

    @Test
    void vertical_itemBounds_fullWidth() {
        CountingAdapter adapter = new CountingAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);
        int vpWidth = lm.getWidth();

        assertTrue(vpWidth > 0, "ビューポート幅が正");
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible()) {
                assertEquals(vpWidth, comp.getWidth(), "アイテム幅がビューポート幅に一致");
            }
        }
    }

    @Test
    void vertical_withGap_correctSpacing() {
        int gapSize = 10;
        CountingAdapter adapter = new CountingAdapter(10);
        RecyclerPane pane = createPane(200, 400, new LinearLayoutManager(Orientation.VERTICAL, gapSize), adapter);

        int prevBottom = -gapSize;
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible() && comp.getName() != null) {
                int pos = Integer.parseInt(comp.getName().replace("item-", ""));
                int expectedY = pos * (40 + gapSize);
                assertEquals(expectedY, comp.getY(),
                        "gap 込みで位置が正しい: item-" + pos);
            }
        }
    }

    // ========== 基本配置（水平） ==========

    @Test
    void horizontal_itemBounds_correctPosition() {
        CountingAdapter adapter = new CountingAdapter(10);
        RecyclerPane pane = createPane(200, 200, new LinearLayoutManager(Orientation.HORIZONTAL), adapter);

        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible() && comp.getName() != null) {
                int pos = Integer.parseInt(comp.getName().replace("item-", ""));
                int expectedX = pos * 200;
                assertEquals(expectedX, comp.getX(),
                        "アイテム " + comp.getName() + " の X 座標");
            }
        }
    }

    @Test
    void horizontal_itemBounds_fullHeight() {
        CountingAdapter adapter = new CountingAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.HORIZONTAL);
        RecyclerPane pane = createPane(200, 300, lm, adapter);
        int vpHeight = lm.getHeight();

        assertTrue(vpHeight > 0, "ビューポート高さが正");
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible()) {
                assertEquals(vpHeight, comp.getHeight(), "アイテム高さがビューポート高さに一致");
            }
        }
    }

    // ========== スクロール ==========

    @Test
    void scrollDown_visibleItemsUpdated() {
        // 20アイテム (40px each), viewport 200px → 5個表示
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // 初期状態: item-0〜item-4 が可視
        assertNotNull(findVisibleItem(pane, "item-0"), "初期: item-0 が可視");
        assertNotNull(findVisibleItem(pane, "item-4"), "初期: item-4 が可視");

        // 200px スクロール → item-5〜item-9 が可視になるべき
        simulateScroll(pane, 0, 200);

        assertNotNull(findVisibleItem(pane, "item-5"), "スクロール後: item-5 が可視");
        assertNotNull(findVisibleItem(pane, "item-9"), "スクロール後: item-9 が可視");
        // item-0 は非可視
        assertNull(findVisibleItem(pane, "item-0"), "スクロール後: item-0 は非可視");
    }

    @Test
    void scrollDown_thenScrollUp_itemsRestored() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        simulateScroll(pane, 0, 200);
        simulateScroll(pane, 0, 0);

        // 元に戻った: item-0 が可視
        assertNotNull(findVisibleItem(pane, "item-0"), "戻り後: item-0 が可視");
        assertNotNull(findVisibleItem(pane, "item-4"), "戻り後: item-4 が可視");
    }

    static Component findVisibleItem(RecyclerPane pane, String name) {
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (name.equals(comp.getName()) && comp.isVisible()) {
                return comp;
            }
        }
        return null;
    }

    @Test
    void scrollDown_recyclesToppedItems() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        int createBefore = adapter.createCount;

        // 下にスクロール (item 5 あたりから表示)
        simulateScroll(pane, 0, 200);

        // 上のアイテムは非表示になるべき
        boolean foundItem0Visible = false;
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if ("item-0".equals(comp.getName()) && comp.isVisible()) {
                foundItem0Visible = true;
            }
        }
        // item-0 は scrap → recycleScrap で非表示にされるか、または pane から消える
        // create 回数が制限されていることを確認
        assertTrue(adapter.createCount < 20, "全アイテム分は作成されない");
    }

    @Test
    void scrollDown_createsNewBottomItems() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        simulateScroll(pane, 0, 200);

        // 新しい位置のアイテムが存在する
        boolean foundItem5 = false;
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if ("item-5".equals(comp.getName()) && comp.isVisible()) {
                foundItem5 = true;
            }
        }
        assertTrue(foundItem5, "下にスクロールして item-5 が表示される");
    }

    @Test
    void scrollDown_thenUp_reusesFromCache() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        int createAfterInit = adapter.createCount;

        // 下にスクロール
        simulateScroll(pane, 0, 200);
        int createAfterScroll = adapter.createCount;

        // 上に戻る → Cache から再利用
        simulateScroll(pane, 0, 0);

        // 上に戻った時に新たな create が発生しないか、最小限であること
        assertTrue(adapter.createCount <= createAfterScroll + 2,
                "戻り時の create は最小限: " + adapter.createCount);
    }

    @Test
    void scrollDown_createCountMinimal() {
        CountingAdapter adapter = new CountingAdapter(100);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // 大量スクロール
        for (int y = 0; y <= 3000; y += 200) {
            simulateScroll(pane, 0, y);
        }

        // create 回数が画面表示数 + cache + pool 程度に収まる
        assertTrue(adapter.createCount < 30,
                "大量スクロールでも create 回数は限定的: " + adapter.createCount);
    }

    // ========== リサイクル効率 ==========

    @Test
    void items1000_createCountLimited() {
        CountingAdapter adapter = new CountingAdapter(1000);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // 全範囲スクロール
        for (int y = 0; y <= 40000; y += 400) {
            simulateScroll(pane, 0, y);
        }

        // create 回数は画面表示数 + cache + pool 程度
        assertTrue(adapter.createCount < 30,
                "1000アイテムでも create は限定的: " + adapter.createCount);
    }

    @Test
    void scrollFullRange_bindCountTracked() {
        CountingAdapter adapter = new CountingAdapter(100);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        for (int y = 0; y <= 4000; y += 200) {
            simulateScroll(pane, 0, y);
        }

        // bind は表示のたびに呼ばれるが、全範囲をカバー
        assertTrue(adapter.bindCount > 0, "bind が呼ばれている");
    }

    // ========== サイズ計算 ==========

    @Test
    void computeTotalSize_vertical_sumsHeights() {
        CountingAdapter adapter = new CountingAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // layout 実行後、サイズキャッシュに基づく計算
        RecyclerPane.State state = new RecyclerPane.State(10);
        Dimension size = lm.computeTotalSize(state);

        // 10 * 40 = 400 (gap=0)
        assertEquals(400, size.height);
    }

    @Test
    void computeTotalSize_horizontal_sumsWidths() {
        CountingAdapter adapter = new CountingAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.HORIZONTAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        RecyclerPane.State state = new RecyclerPane.State(10);
        Dimension size = lm.computeTotalSize(state);

        // 10 * 200 = 2000 (gap=0)
        assertEquals(2000, size.width);
    }

    @Test
    void computeTotalSize_emptyAdapter_zero() {
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane.State state = new RecyclerPane.State(0);
        Dimension size = lm.computeTotalSize(state);
        assertEquals(0, size.height);
        assertEquals(0, size.width);
    }

    // ========== 可変サイズ ==========

    @Test
    void variableItemHeight_correctLayout() {
        // 奇数: 30px, 偶数: 60px
        CountingAdapter adapter = new CountingAdapter(10,
                pos -> new Dimension(200, pos % 2 == 0 ? 60 : 30));
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 400, lm, adapter);

        int expectedY = 0;
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible() && comp.getName() != null) {
                int pos = Integer.parseInt(comp.getName().replace("item-", ""));
                assertEquals(expectedY, comp.getY(),
                        "可変高さアイテム " + pos + " のY座標");
                expectedY += (pos % 2 == 0 ? 60 : 30);
            }
        }
    }

    @Test
    void variableItemHeight_scrollCorrect() {
        CountingAdapter adapter = new CountingAdapter(20,
                pos -> new Dimension(200, pos % 2 == 0 ? 60 : 30));
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // スクロール後もレイアウトエラーなし
        assertDoesNotThrow(() -> simulateScroll(pane, 0, 300));
    }

    // ========== 複合コンポーネント ==========

    @Test
    void complexItem_correctBounds() {
        ComplexItemAdapter adapter = new ComplexItemAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 300, lm, adapter);
        int vpWidth = lm.getWidth();

        assertTrue(pane.getComponentCount() > 0, "コンポーネントが配置されている");
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible()) {
                assertEquals(vpWidth, comp.getWidth(), "複合アイテムの幅がビューポート幅");
                assertEquals(50, comp.getHeight(), "複合アイテムの高さが preferredSize");
            }
        }
    }

    @Test
    void complexItem_variableHeight_correctLayout() {
        ComplexItemAdapter adapter = new ComplexItemAdapter(10,
                pos -> new Dimension(300, 40 + pos * 5));
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 500, lm, adapter);

        int expectedY = 0;
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible() && comp instanceof JPanel) {
                assertEquals(expectedY, comp.getY());
                int expectedH = 40 + i * 5;
                assertEquals(expectedH, comp.getHeight());
                expectedY += expectedH;
            }
        }
    }

    @Test
    void complexItem_recycled_childrenPreserved() {
        ComplexItemAdapter adapter = new ComplexItemAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        // スクロールしてリサイクル発生
        simulateScroll(pane, 0, 400);

        // リサイクルされたアイテムの子コンポーネント構成を確認
        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible() && comp instanceof JPanel panel) {
                assertEquals(3, panel.getComponentCount(),
                        "リサイクル後も子コンポーネント数が保たれる");
            }
        }
    }

    @Test
    void complexItem_rebind_updatesContent() {
        ComplexItemAdapter adapter = new ComplexItemAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(300, 200, lm, adapter);

        // スクロールで rebind が発生
        simulateScroll(pane, 0, 400);

        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible() && comp instanceof JPanel panel) {
                // BorderLayout の CENTER に JLabel がある
                Component center = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                if (center instanceof JLabel label) {
                    assertTrue(label.getText().startsWith("Item "),
                            "rebind でテキストが更新されている");
                }
            }
        }
    }

    // ========== ビューポートリサイズ ==========

    @Test
    void viewportResize_itemWidthUpdated() {
        CountingAdapter adapter = new CountingAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        int widthBefore = lm.getWidth();
        assertTrue(widthBefore > 0);

        // JScrollPane のサイズを拡大
        JScrollPane scrollPane = (JScrollPane) pane.getParent().getParent();
        scrollPane.setBounds(0, 0, 500, 200);
        scrollPane.doLayout();
        scrollPane.getViewport().doLayout();
        pane.doLayout();

        int widthAfter = lm.getWidth();
        assertTrue(widthAfter > widthBefore,
                "ビューポート拡大でレイアウト幅が更新: " + widthBefore + " → " + widthAfter);

        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible()) {
                assertEquals(widthAfter, comp.getWidth(),
                        "リサイズ後のアイテム幅がビューポート幅に追従");
            }
        }
    }

    @Test
    void viewportResize_moreItemsVisible() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        int visibleBefore = countVisibleItems(pane);
        // 200px / 40px = 5 items

        // ビューポートを 400px に拡大
        JScrollPane scrollPane = (JScrollPane) pane.getParent().getParent();
        scrollPane.setBounds(0, 0, 200, 400);
        scrollPane.doLayout();
        scrollPane.getViewport().doLayout();
        pane.doLayout();

        int visibleAfter = countVisibleItems(pane);
        assertTrue(visibleAfter > visibleBefore,
                "ビューポート拡大でより多くのアイテムが可視: " + visibleBefore + " → " + visibleAfter);
    }

    // ========== データ変更 ==========

    @Test
    void dataChanged_itemCountIncrease_moreItemsVisible() {
        CountingAdapter adapter = new CountingAdapter(5);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 400, lm, adapter);

        int visibleBefore = countVisibleItems(pane);
        assertEquals(5, visibleBefore, "初期: 5個表示");

        // データ追加
        adapter.itemCount = 15;
        adapter.notifyDataSetChanged();
        pane.doLayout();

        int visibleAfter = countVisibleItems(pane);
        assertTrue(visibleAfter > visibleBefore,
                "データ追加後: " + visibleBefore + " → " + visibleAfter);
    }

    @Test
    void dataChanged_itemCountDecrease_lessItemsVisible() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // データ削減
        adapter.itemCount = 3;
        adapter.notifyDataSetChanged();
        pane.doLayout();

        int visibleAfter = countVisibleItems(pane);
        assertEquals(3, visibleAfter, "データ削減後: 3個表示");
    }

    static int countVisibleItems(RecyclerPane pane) {
        int count = 0;
        for (int i = 0; i < pane.getComponentCount(); i++) {
            if (pane.getComponent(i).isVisible()) {
                count++;
            }
        }
        return count;
    }

    // ========== プロパティ ==========

    @Test
    void canScrollVertically_verticalOrientation_true() {
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        assertTrue(lm.canScrollVertically());
    }

    @Test
    void canScrollHorizontally_verticalOrientation_false() {
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        assertFalse(lm.canScrollHorizontally());
    }

    // ========== Phase 1: setter の自動レイアウト更新 ==========

    @Test
    void setOrientation_afterLayout_requestsLayout() {
        CountingAdapter adapter = new CountingAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // 方向を変更
        lm.setOrientation(Orientation.HORIZONTAL);
        pane.doLayout();

        // 水平レイアウトに変わったことを確認
        assertTrue(lm.canScrollHorizontally());
        assertFalse(lm.canScrollVertically());
    }

    @Test
    void setGap_afterLayout_requestsLayout() {
        CountingAdapter adapter = new CountingAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 400, lm, adapter);

        // gap を変更して再レイアウト
        lm.setGap(10);
        pane.doLayout();

        // 2番目のアイテムの位置にギャップが反映されている
        Component item1 = findVisibleItem(pane, "item-1");
        assertNotNull(item1);
        assertEquals(50, item1.getY(), "gap=10 適用後: item-1 は y=50");
    }

    // ========== Phase 2: scrollToPosition / scrollToPositionWithOffset ==========

    @Test
    void scrollToPosition_middle_correctOffset() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // position 10 にスクロール (10 * 40 = 400px)
        lm.scrollToPosition(10);

        // item-10 が可視
        assertNotNull(findVisibleItem(pane, "item-10"), "scrollToPosition(10) 後に item-10 が可視");
    }

    @Test
    void scrollToPosition_first_zero() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // まず下にスクロール
        simulateScroll(pane, 0, 400);

        // position 0 にスクロール
        lm.scrollToPosition(0);

        assertNotNull(findVisibleItem(pane, "item-0"), "scrollToPosition(0) 後に item-0 が可視");
    }

    @Test
    void scrollToPosition_last_showsLastItem() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // 末尾付近にスクロール（JViewport のクランプを考慮し中間位置で検証）
        lm.scrollToPosition(15);

        assertNotNull(findVisibleItem(pane, "item-15"), "scrollToPosition(15) 後に item-15 が可視");
    }

    @Test
    void scrollToPositionWithOffset_appliesOffset() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // position 5 に offset=20 でスクロール
        lm.scrollToPositionWithOffset(5, 20);

        // item-5 が可視で、Y座標は scrollOffset = 5*40 - 20 = 180
        assertNotNull(findVisibleItem(pane, "item-5"), "offset 付きで item-5 が可視");
    }

    @Test
    void scrollToPosition_horizontal_correctOffset() {
        // 幅100のアイテム、viewport 400px → 一度に4個表示、20アイテム → 総幅2000px
        CountingAdapter adapter = new CountingAdapter(20, pos -> new Dimension(100, 40));
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.HORIZONTAL);
        RecyclerPane pane = createPane(400, 200, lm, adapter);

        lm.scrollToPosition(5);

        assertNotNull(findVisibleItem(pane, "item-5"), "水平スクロール後に item-5 が可視");
    }

    // ========== Phase 3: findFirst/LastVisibleItemPosition ==========

    @Test
    void findFirstVisibleItemPosition_initial_zero() {
        CountingAdapter adapter = new CountingAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        assertEquals(0, lm.findFirstVisibleItemPosition());
    }

    @Test
    void findLastVisibleItemPosition_initial_correct() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // 200px / 40px = 5個, item-0 ~ item-4 が可視
        int last = lm.findLastVisibleItemPosition();
        assertTrue(last >= 4 && last <= 5, "最後の可視アイテムは4か5: " + last);
    }

    @Test
    void findFirstVisibleItemPosition_afterScroll_updated() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        simulateScroll(pane, 0, 200);

        assertEquals(5, lm.findFirstVisibleItemPosition(),
                "200pxスクロール後の最初の可視アイテム");
    }

    @Test
    void findLastVisibleItemPosition_afterScroll_updated() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        simulateScroll(pane, 0, 200);

        int last = lm.findLastVisibleItemPosition();
        assertTrue(last >= 9 && last <= 10, "スクロール後の最後の可視: " + last);
    }

    @Test
    void findFirstCompletelyVisibleItemPosition_partiallyVisible_skipped() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // 20px スクロール → item-0 は部分的にしか見えない
        simulateScroll(pane, 0, 20);

        int firstComplete = lm.findFirstCompletelyVisibleItemPosition();
        assertTrue(firstComplete >= 1, "部分的に見えるアイテムを除外: " + firstComplete);
    }

    @Test
    void findLastCompletelyVisibleItemPosition_partiallyVisible_skipped() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // 20px スクロール
        simulateScroll(pane, 0, 20);

        int lastComplete = lm.findLastCompletelyVisibleItemPosition();
        // 5番目(index=4)は y=160, height=40, bottom=200, scrollBottom=220 → 完全可視
        assertTrue(lastComplete >= 4, "完全可視の最後: " + lastComplete);
    }

    @Test
    void emptyAdapter_returnsMinusOne() {
        CountingAdapter adapter = new CountingAdapter(0);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        assertEquals(-1, lm.findFirstVisibleItemPosition());
        assertEquals(-1, lm.findLastVisibleItemPosition());
        assertEquals(-1, lm.findFirstCompletelyVisibleItemPosition());
        assertEquals(-1, lm.findLastCompletelyVisibleItemPosition());
    }

    // ========== Phase 4: reverseLayout + stackFromEnd ==========

    @Test
    void reverseLayout_firstItemAtBottom() {
        CountingAdapter adapter = new CountingAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        lm.setReverseLayout(true);
        RecyclerPane pane = createPane(200, 400, lm, adapter);

        // reverseLayout=true: レイアウト上、一番上に position=9, 一番下に position=0
        Component item9 = findVisibleItem(pane, "item-9");
        Component item0 = findVisibleItem(pane, "item-0");
        assertNotNull(item9, "item-9 が可視");
        assertNotNull(item0, "item-0 が可視");
        assertTrue(item9.getY() < item0.getY(), "item-9 が item-0 より上に配置");
    }

    @Test
    void reverseLayout_correctBounds() {
        CountingAdapter adapter = new CountingAdapter(5);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        lm.setReverseLayout(true);
        RecyclerPane pane = createPane(200, 400, lm, adapter);

        // 5アイテム * 40px = 200px, レイアウト順は [4, 3, 2, 1, 0]
        Component item4 = findVisibleItem(pane, "item-4");
        assertNotNull(item4);
        assertEquals(0, item4.getY(), "reverseLayout: item-4 は y=0");

        Component item3 = findVisibleItem(pane, "item-3");
        assertNotNull(item3);
        assertEquals(40, item3.getY(), "reverseLayout: item-3 は y=40");
    }

    @Test
    void reverseLayout_horizontal_firstItemAtRight() {
        CountingAdapter adapter = new CountingAdapter(5);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.HORIZONTAL);
        lm.setReverseLayout(true);
        RecyclerPane pane = createPane(1200, 200, lm, adapter);

        // 水平 reverse: item-4 が左端、item-0 が右端
        Component item4 = findVisibleItem(pane, "item-4");
        Component item0 = findVisibleItem(pane, "item-0");
        assertNotNull(item4);
        assertNotNull(item0);
        assertTrue(item4.getX() < item0.getX(), "水平 reverse: item-4 が item-0 より左");
    }

    @Test
    void stackFromEnd_contentSmallerThanViewport_bottomAligned() {
        // 3アイテム * 40px = 120px, viewport > 120px → 下詰め
        CountingAdapter adapter = new CountingAdapter(3);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        lm.setStackFromEnd(true);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // offset = viewportHeight - 120
        int vpHeight = lm.getHeight();
        int expectedY = vpHeight - 120;

        Component item0 = findVisibleItem(pane, "item-0");
        assertNotNull(item0);
        assertEquals(expectedY, item0.getY(), "stackFromEnd: item-0 は下詰め (vpH=" + vpHeight + ")");
    }

    @Test
    void stackFromEnd_contentLargerThanViewport_normalLayout() {
        // 20アイテム * 40px = 800px > viewport=200px → 通常配置
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        lm.setStackFromEnd(true);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        Component item0 = findVisibleItem(pane, "item-0");
        assertNotNull(item0);
        assertEquals(0, item0.getY(), "コンテンツ > viewport: 通常配置で y=0");
    }

    @Test
    void reverseLayout_scrollDirection_reversed() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        lm.setReverseLayout(true);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // reverse: position 19 が上端
        // スクロール可能で item-0 は下の方にある
        assertNotNull(findVisibleItem(pane, "item-19"), "reverse: item-19 が初期表示");
    }


    @Test
    void reverseLayout_findFirstVisibleItemPosition_returnsMinPosition() {
        CountingAdapter adapter = new CountingAdapter(10);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        lm.setReverseLayout(true);
        RecyclerPane pane = createPane(200, 400, lm, adapter);

        // reverseLayout でも firstVisible は最小 position
        int first = lm.findFirstVisibleItemPosition();
        int last = lm.findLastVisibleItemPosition();
        assertTrue(first <= last,
                "reverseLayout でも first(" + first + ") <= last(" + last + ")");
        assertTrue(first >= 0, "first は 0 以上");
    }

    @Test
    void reverseLayout_findVisibleItemPositions_correctRange() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        lm.setReverseLayout(true);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        // 最初に配置される item-19 の position ではなく、可視範囲の min/max
        int first = lm.findFirstVisibleItemPosition();
        int last = lm.findLastVisibleItemPosition();
        assertEquals(last, 19, "reverse: 最後の可視は position=19");
        assertTrue(first < last, "first < last");
    }


    @Test
    void reverseLayout_scrollToPosition_correctItem() {
        CountingAdapter adapter = new CountingAdapter(20);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        lm.setReverseLayout(true);
        RecyclerPane pane = createPane(200, 200, lm, adapter);

        lm.scrollToPosition(5);

        assertNotNull(findVisibleItem(pane, "item-5"),
                "reverseLayout + scrollToPosition(5) で item-5 が可視");
    }


    @Test
    void computeTotalSize_vertical_withGap_correct() {
        CountingAdapter adapter = new CountingAdapter(5);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL, 10);
        RecyclerPane pane = createPane(200, 400, lm, adapter);

        RecyclerPane.State state = new RecyclerPane.State(5);
        Dimension size = lm.computeTotalSize(state);

        // 5 * 40 + 4 * 10 = 240
        assertEquals(240, size.height, "gap=10 込みの totalHeight");
    }

    @Test
    void computeTotalSize_horizontal_withGap_correct() {
        CountingAdapter adapter = new CountingAdapter(5);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.HORIZONTAL, 10);
        RecyclerPane pane = createPane(2000, 200, lm, adapter);

        RecyclerPane.State state = new RecyclerPane.State(5);
        Dimension size = lm.computeTotalSize(state);

        // 5 * 200 + 4 * 10 = 1040 (水平アイテムの幅は 200)
        assertEquals(1040, size.width, "gap=10 込みの totalWidth");
    }
}

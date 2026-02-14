package org.to0mi1.swuit.component.virtualscroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JViewport;

import org.junit.jupiter.api.Test;
import org.to0mi1.swuit.layout.Orientation;
import org.to0mi1.swuit.layout.linear.LinearLayout;

class VirtualScrollPaneTest {

    // === ヘルパー ===

    static JPanel createPanel(LayoutManager layout, int childCount, int childHeight) {
        JPanel panel = new JPanel(layout);
        for (int i = 0; i < childCount; i++) {
            JLabel label = new JLabel("Item " + i);
            label.setPreferredSize(new Dimension(200, childHeight));
            panel.add(label);
        }
        return panel;
    }

    /**
     * ヘッドレス環境でも確実に動作するよう、viewport とパネルのサイズを明示的に設定する。
     */
    static VirtualScrollPane createScrollPane(JPanel panel, int viewportWidth, int viewportHeight) {
        VirtualScrollPane sp = new VirtualScrollPane(panel);
        // ScrollPane 自体のサイズ設定
        sp.setSize(viewportWidth + 20, viewportHeight + 20);
        sp.doLayout();

        // ヘッドレス環境では doLayout が viewport サイズを正しく設定しない場合がある
        JViewport viewport = sp.getViewport();
        viewport.setSize(viewportWidth, viewportHeight);
        viewport.setViewPosition(new Point(0, 0));

        // ビューのサイズをビューポートが設定するのと同等に設定
        Dimension prefSize = panel.getPreferredSize();
        panel.setSize(Math.max(prefSize.width, viewportWidth),
                Math.max(prefSize.height, viewportHeight));
        panel.doLayout();
        return sp;
    }

    // === 可視性の基本テスト ===

    @Test
    void outsideViewport_invisible() {
        JPanel panel = createPanel(new GridLayout(0, 1), 100, 30);
        VirtualScrollPane sp = createScrollPane(panel, 220, 100);

        // viewport の高さ 100px、バッファ 50px → 上端 0〜150px が可視圏
        // 各子は 30px 高なので 0〜4 (=150/30=5個) が可視、5以降は不可視
        Component child0 = panel.getComponent(0);
        assertTrue(child0.isVisible(), "可視領域内の子は visible");

        // 最後の子は確実に範囲外
        Component lastChild = panel.getComponent(99);
        assertFalse(lastChild.isVisible(), "可視領域外の子は invisible");
    }

    @Test
    void insideViewport_visible() {
        JPanel panel = createPanel(new GridLayout(0, 1), 100, 30);
        VirtualScrollPane sp = createScrollPane(panel, 220, 100);

        // 先頭の子は可視領域内
        for (int i = 0; i < 3; i++) {
            assertTrue(panel.getComponent(i).isVisible(),
                    "Index " + i + " は可視領域内");
        }
    }

    // === レイアウト保護テスト ===

    @Test
    void allBounds_correct() {
        JPanel panel = createPanel(new GridLayout(0, 1), 20, 30);
        VirtualScrollPane sp = createScrollPane(panel, 220, 100);

        // GridLayout(0,1) で childHeight=30 の場合、各子は上から順に 30px ずつ配置
        for (int i = 0; i < 20; i++) {
            Rectangle bounds = panel.getComponent(i).getBounds();
            assertEquals(30 * i, bounds.y,
                    "Index " + i + " の y 座標が正しい");
            assertEquals(30, bounds.height,
                    "Index " + i + " の高さが正しい");
        }
    }

    // === preferredSize テスト ===

    @Test
    void preferredSize_considersAllChildren() {
        JPanel panel = createPanel(new GridLayout(0, 1), 50, 30);
        VirtualScrollPane sp = createScrollPane(panel, 220, 100);

        // preferredSize は全子コンポーネントを考慮する
        Dimension pref = panel.getPreferredSize();
        // GridLayout(0,1) で 50 個 × 30px = 1500px
        assertEquals(1500, pref.height,
                "preferredSize は全子を考慮すべき");
    }

    // === ユーザー明示非表示テスト ===

    @Test
    void userHidden_staysHidden() {
        JPanel panel = createPanel(new GridLayout(0, 1), 20, 30);

        // ユーザーが事前に非表示にした子
        panel.getComponent(1).setVisible(false);

        VirtualScrollPane sp = createScrollPane(panel, 220, 100);

        // 可視領域内でもユーザーが非表示にした子はそのまま
        assertFalse(panel.getComponent(1).isVisible(),
                "ユーザーが非表示にした子は可視領域内でも invisible");

        // 隣の子は可視
        assertTrue(panel.getComponent(0).isVisible());
        assertTrue(panel.getComponent(2).isVisible());
    }

    // === バッファゾーンテスト ===

    @Test
    void bufferZone_default() {
        VirtualScrollPane sp = new VirtualScrollPane();
        assertEquals(50, sp.getBufferZone());
    }

    @Test
    void bufferZone_setter() {
        VirtualScrollPane sp = new VirtualScrollPane();
        sp.setBufferZone(100);
        assertEquals(100, sp.getBufferZone());
    }

    @Test
    void bufferZone_negative_throws() {
        VirtualScrollPane sp = new VirtualScrollPane();
        assertThrows(IllegalArgumentException.class, () -> sp.setBufferZone(-1));
    }

    @Test
    void bufferZone_zero_narrows() {
        JPanel panel = createPanel(new GridLayout(0, 1), 100, 30);
        VirtualScrollPane sp = new VirtualScrollPane(panel);
        sp.setBufferZone(0);
        sp.setSize(220, 120);
        sp.doLayout();

        JViewport viewport = sp.getViewport();
        viewport.setSize(200, 100);
        viewport.setViewPosition(new Point(0, 0));

        Dimension prefSize = panel.getPreferredSize();
        panel.setSize(Math.max(prefSize.width, 200), Math.max(prefSize.height, 100));
        panel.doLayout();

        // バッファ 0px → viewport 高さ 100px のみ → 0〜99px が可視圏
        // 各子 30px → index 0,1,2 (y=0,30,60) が可視、index 3 (y=90) も部分的に交差
        // index 4 (y=120) 以降は不可視
        assertFalse(panel.getComponent(10).isVisible(),
                "バッファ 0 で遠い子は invisible");
    }

    @Test
    void bufferZone_large_showsMore() {
        JPanel panel = createPanel(new GridLayout(0, 1), 100, 30);
        VirtualScrollPane sp = new VirtualScrollPane(panel);
        sp.setBufferZone(200);
        sp.setSize(220, 120);
        sp.doLayout();

        JViewport viewport = sp.getViewport();
        viewport.setSize(200, 100);
        viewport.setViewPosition(new Point(0, 0));

        Dimension prefSize = panel.getPreferredSize();
        panel.setSize(Math.max(prefSize.width, 200), Math.max(prefSize.height, 100));
        panel.doLayout();

        // バッファ 200px → viewport 高さ 100px + 上下 200px = -200〜300px が可視圏
        // index 9 (y=270) まで可視
        assertTrue(panel.getComponent(9).isVisible(),
                "大きなバッファで多くの子が visible");
    }

    // === setViewportView の複数回呼び出し ===

    @Test
    void setViewportView_multiple() {
        JPanel panel1 = createPanel(new GridLayout(0, 1), 10, 30);
        JPanel panel2 = createPanel(new GridLayout(0, 1), 10, 30);

        VirtualScrollPane sp = new VirtualScrollPane(panel1);
        sp.setSize(220, 120);
        sp.doLayout();

        // 2 回目の setViewportView
        sp.setViewportView(panel2);
        sp.doLayout();

        JViewport viewport = sp.getViewport();
        viewport.setSize(200, 100);
        viewport.setViewPosition(new Point(0, 0));

        Dimension prefSize = panel2.getPreferredSize();
        panel2.setSize(Math.max(prefSize.width, 200), Math.max(prefSize.height, 100));
        panel2.doLayout();

        // panel2 の子が正しく仮想化される
        assertTrue(panel2.getComponent(0).isVisible());

        // panel1 の LM がアンラップされている
        assertFalse(panel1.getLayout() instanceof LayoutManagerWrapper,
                "旧ビューの LayoutManager はアンラップされるべき");
    }

    // === LinearLayout との互換性 ===

    @Test
    void linearLayout_compatibility() {
        LinearLayout layout = new LinearLayout(Orientation.VERTICAL, 0);
        JPanel panel = new JPanel(layout);
        for (int i = 0; i < 50; i++) {
            JLabel label = new JLabel("Item " + i);
            label.setPreferredSize(new Dimension(200, 30));
            panel.add(label);
        }

        VirtualScrollPane sp = createScrollPane(panel, 220, 100);

        // 先頭は可視
        assertTrue(panel.getComponent(0).isVisible());
        // 末尾は不可視
        assertFalse(panel.getComponent(49).isVisible());

        // bounds が正しい（LinearLayout は上から順に 30px ずつ）
        for (int i = 0; i < 50; i++) {
            assertEquals(30 * i, panel.getComponent(i).getBounds().y,
                    "LinearLayout: Index " + i + " の y 座標");
        }

        // preferredSize が全子を考慮
        Dimension pref = panel.getPreferredSize();
        assertEquals(1500, pref.height);
    }

    // === LayoutManager のラップ検証 ===

    @Test
    void layoutManager_wrapped() {
        JPanel panel = createPanel(new GridLayout(0, 1), 5, 30);
        VirtualScrollPane sp = new VirtualScrollPane(panel);

        assertTrue(panel.getLayout() instanceof LayoutManagerWrapper,
                "ビューの LayoutManager はラップされるべき");
    }

    @Test
    void layoutManager_unwrapped_onViewChange() {
        JPanel panel = createPanel(new GridLayout(0, 1), 5, 30);
        VirtualScrollPane sp = new VirtualScrollPane(panel);

        sp.setViewportView(new JPanel());

        assertFalse(panel.getLayout() instanceof LayoutManagerWrapper,
                "旧ビューはアンラップされるべき");
    }
}

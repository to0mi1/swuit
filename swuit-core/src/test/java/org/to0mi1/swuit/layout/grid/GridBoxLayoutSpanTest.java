package org.to0mi1.swuit.layout.grid;

import java.awt.Component;
import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.to0mi1.swuit.layout.grid.GridBoxLayoutTest.*;

class GridBoxLayoutSpanTest {

    // === columnSpan ===

    @Test
    void columnSpan_basic() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100), TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fixed(50));
        javax.swing.JPanel panel = createContainer(layout, 300, 100);

        Component header = fixedSize(200, 30);
        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        panel.add(header, new GridConstraints().column(0).row(0).columnSpan(3));
        panel.add(a, new GridConstraints().column(0).row(1));
        panel.add(b, new GridConstraints().column(1).row(1));
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 300, 50), boundsOf(header));
    }

    @Test
    void columnSpan_withGap() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100), TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fr(1))
                .setColumnGap(10);
        javax.swing.JPanel panel = createContainer(layout, 320, 100);

        Component header = fixedSize(200, 30);
        panel.add(header, new GridConstraints().column(0).row(0).columnSpan(3));
        panel.doLayout();

        // 100 + 10 + 100 + 10 + 100 = 320
        assertEquals(new Rectangle(0, 0, 320, 100), boundsOf(header));
    }

    // === rowSpan ===

    @Test
    void rowSpan_basic() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fixed(50));
        javax.swing.JPanel panel = createContainer(layout, 300, 100);

        Component sidebar = fixedSize(80, 80);
        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        panel.add(sidebar, new GridConstraints().column(0).row(0).rowSpan(2));
        panel.add(a, new GridConstraints().column(1).row(0));
        panel.add(b, new GridConstraints().column(1).row(1));
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(sidebar));
    }

    @Test
    void rowSpan_withGap() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fixed(50))
                .setRowGap(10);
        javax.swing.JPanel panel = createContainer(layout, 300, 200);

        Component sidebar = fixedSize(80, 80);
        panel.add(sidebar, new GridConstraints().column(0).row(0).rowSpan(2));
        panel.doLayout();

        // 50 + 10 + 50 = 110
        assertEquals(new Rectangle(0, 0, 100, 110), boundsOf(sidebar));
    }

    // === 自動配置 ===

    @Test
    void autoPlacement_sequential() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100));
        javax.swing.JPanel panel = createContainer(layout, 200, 200);

        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        Component c = fixedSize(80, 30);
        // 位置指定なし → 自動配置
        panel.add(a);
        panel.add(b);
        panel.add(c);
        panel.doLayout();

        // a=(0,0), b=(1,0), c=(0,1)
        assertEquals(0, boundsOf(a).x);
        assertEquals(0, boundsOf(a).y);
        assertEquals(100, boundsOf(b).x);
        assertEquals(0, boundsOf(b).y);
        assertEquals(0, boundsOf(c).x);
        assertEquals(30, boundsOf(c).y); // auto 行は preferredSize 高さ (30px)
    }

    @Test
    void autoPlacement_skipsOccupied() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100), TrackSize.fixed(100));
        javax.swing.JPanel panel = createContainer(layout, 300, 200);

        Component positioned = fixedSize(80, 30);
        Component auto1 = fixedSize(80, 30);
        Component auto2 = fixedSize(80, 30);
        Component auto3 = fixedSize(80, 30);

        // (1,0) に明示的配置
        panel.add(positioned, new GridConstraints().column(1).row(0));
        panel.add(auto1);
        panel.add(auto2);
        panel.add(auto3);
        panel.doLayout();

        // positioned=(1,0), auto1=(0,0), auto2=(2,0), auto3=(0,1)
        assertEquals(100, boundsOf(positioned).x);
        assertEquals(0, boundsOf(positioned).y);
        assertEquals(0, boundsOf(auto1).x);
        assertEquals(0, boundsOf(auto1).y);
        assertEquals(200, boundsOf(auto2).x);
        assertEquals(0, boundsOf(auto2).y);
        assertEquals(0, boundsOf(auto3).x);
        assertEquals(30, boundsOf(auto3).y); // auto 行は preferredSize 高さ (30px)
    }

    // === 暗黙的行追加 ===

    @Test
    void implicitRows_addedWhenOverflow() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fixed(50));
        javax.swing.JPanel panel = createContainer(layout, 100, 200);

        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 40);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(0).row(1)); // 暗黙的行
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 100, 50), boundsOf(a));
        // 暗黙的行は auto サイズ (preferredSize)
        assertEquals(0, boundsOf(b).x);
        assertEquals(50, boundsOf(b).y);
        assertEquals(100, boundsOf(b).width);
        assertEquals(40, boundsOf(b).height);
    }

    @Test
    void autoPlacement_withSpan() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100), TrackSize.fixed(100));
        javax.swing.JPanel panel = createContainer(layout, 300, 200);

        Component wide = fixedSize(180, 30);
        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);

        // wide は columnSpan=2、位置未指定
        panel.add(wide, new GridConstraints().columnSpan(2));
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // wide=(0,0) span 2, a=(2,0), b=(0,1)
        assertEquals(0, boundsOf(wide).x);
        assertEquals(0, boundsOf(wide).y);
        assertEquals(200, boundsOf(wide).width);

        assertEquals(200, boundsOf(a).x);
        assertEquals(0, boundsOf(a).y);

        assertEquals(0, boundsOf(b).x);
        assertEquals(30, boundsOf(b).y); // auto 行は preferredSize 高さ (30px)
    }

    // === columnSpan + rowSpan 同時 ===

    @Test
    void columnSpan_and_rowSpan_combined() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100), TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fixed(50), TrackSize.fixed(50));
        javax.swing.JPanel panel = createContainer(layout, 300, 150);

        Component big = fixedSize(180, 80);
        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);

        panel.add(big, new GridConstraints().column(0).row(0).columnSpan(2).rowSpan(2));
        panel.add(a, new GridConstraints().column(2).row(0));
        panel.add(b, new GridConstraints().column(2).row(1));
        panel.doLayout();

        // big: x=0, y=0, w=200 (100+100), h=100 (50+50)
        assertEquals(new Rectangle(0, 0, 200, 100), boundsOf(big));
        assertEquals(new Rectangle(200, 0, 100, 50), boundsOf(a));
        assertEquals(new Rectangle(200, 50, 100, 50), boundsOf(b));
    }

    // === 自動配置 + rowSpan ===

    @Test
    void autoPlacement_withRowSpan() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fixed(50));
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component tall = fixedSize(80, 80);
        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);

        // tall は rowSpan=2, 位置未指定
        panel.add(tall, new GridConstraints().rowSpan(2));
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // tall=(0,0) rowSpan 2, a=(1,0), b=(1,1)
        assertEquals(0, boundsOf(tall).x);
        assertEquals(0, boundsOf(tall).y);
        assertEquals(100, boundsOf(tall).height); // 50 + 50

        assertEquals(100, boundsOf(a).x);
        assertEquals(0, boundsOf(a).y);

        assertEquals(100, boundsOf(b).x);
        assertEquals(50, boundsOf(b).y);
    }

    // === スパンが auto トラックサイズに影響 ===

    @Test
    void span_expandsAutoTrack() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.auto(), TrackSize.auto());
        javax.swing.JPanel panel = createContainer(layout, 400, 100);

        // 単体アイテム: 各列 50px
        Component a = fixedSize(50, 30);
        Component b = fixedSize(50, 30);
        // スパンアイテム: 2列で 200px 必要
        Component wide = fixedSize(200, 30);

        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.add(wide, new GridConstraints().column(0).row(1).columnSpan(2));
        panel.doLayout();

        // auto トラックの合計が 200px 以上になるはず
        int totalWidth = boundsOf(a).width + boundsOf(b).width;
        assertTrue(totalWidth >= 200, "span item should expand auto tracks: " + totalWidth);
    }

    // === auto-placement: columnSpan > numCols (Bug 1-1 回帰テスト) ===

    @Test
    void autoPlacement_columnSpanExceedsColumnCount() {
        // 2列テンプレートだが columnSpan=3 のアイテムがある
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100));
        javax.swing.JPanel panel = createContainer(layout, 400, 100);

        Component wide = fixedSize(200, 30);
        // columnSpan=3 > numCols=2 → numCols が 3 に拡張されるべき
        panel.add(wide, new GridConstraints().columnSpan(3));
        panel.doLayout();

        // 無限ループにならず、正常にレイアウトされる
        assertEquals(0, boundsOf(wide).x);
        assertEquals(0, boundsOf(wide).y);
        assertTrue(boundsOf(wide).width > 0);
    }

    // === 自動配置 skipsOccupied の厳密な検証 ===

    @Test
    void autoPlacement_skipsOccupied_exactPositions() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100), TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fixed(50));
        javax.swing.JPanel panel = createContainer(layout, 300, 100);

        Component positioned = fixedSize(80, 30);
        Component auto1 = fixedSize(80, 30);
        Component auto2 = fixedSize(80, 30);
        Component auto3 = fixedSize(80, 30);

        // (1,0) に明示的配置
        panel.add(positioned, new GridConstraints().column(1).row(0));
        panel.add(auto1);
        panel.add(auto2);
        panel.add(auto3);
        panel.doLayout();

        // positioned=(1,0), auto1=(0,0), auto2=(2,0), auto3=(0,1)
        assertEquals(new Rectangle(100, 0, 100, 50), boundsOf(positioned));
        assertEquals(new Rectangle(0, 0, 100, 50), boundsOf(auto1));
        assertEquals(new Rectangle(200, 0, 100, 50), boundsOf(auto2));
        assertEquals(new Rectangle(0, 50, 100, 50), boundsOf(auto3));
    }
}

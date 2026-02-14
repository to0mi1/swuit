package org.to0mi1.swuit.layout.grid;

import java.awt.Component;
import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.to0mi1.swuit.layout.grid.GridBoxLayoutTest.*;

class GridBoxLayoutAlignTest {

    // === justifyItems ===

    @Test
    void justifyItems_start() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setJustifyItems(JustifyItems.START);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.doLayout();

        assertEquals(0, boundsOf(a).x);
        assertEquals(80, boundsOf(a).width);
    }

    @Test
    void justifyItems_end() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setJustifyItems(JustifyItems.END);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.doLayout();

        assertEquals(120, boundsOf(a).x);
        assertEquals(80, boundsOf(a).width);
    }

    @Test
    void justifyItems_center() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setJustifyItems(JustifyItems.CENTER);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.doLayout();

        assertEquals(60, boundsOf(a).x);
        assertEquals(80, boundsOf(a).width);
    }

    @Test
    void justifyItems_stretch() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setJustifyItems(JustifyItems.STRETCH);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.doLayout();

        assertEquals(0, boundsOf(a).x);
        assertEquals(200, boundsOf(a).width);
    }

    // === alignItems ===

    @Test
    void alignItems_start() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100))
                .setAlignItems(AlignItems.START);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.doLayout();

        assertEquals(0, boundsOf(a).y);
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void alignItems_end() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100))
                .setAlignItems(AlignItems.END);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.doLayout();

        assertEquals(70, boundsOf(a).y);
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void alignItems_center() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100))
                .setAlignItems(AlignItems.CENTER);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.doLayout();

        assertEquals(35, boundsOf(a).y);
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void alignItems_stretch() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100))
                .setAlignItems(AlignItems.STRETCH);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.doLayout();

        assertEquals(0, boundsOf(a).y);
        assertEquals(100, boundsOf(a).height);
    }

    // === justifySelf ===

    @Test
    void justifySelf_overridesContainer() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setJustifyItems(JustifyItems.STRETCH);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).justifySelf(JustifySelf.CENTER));
        panel.doLayout();

        assertEquals(60, boundsOf(a).x);
        assertEquals(80, boundsOf(a).width);
    }

    @Test
    void justifySelf_auto_fallsBackToContainer() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setJustifyItems(JustifyItems.END);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).justifySelf(JustifySelf.AUTO));
        panel.doLayout();

        assertEquals(120, boundsOf(a).x);
        assertEquals(80, boundsOf(a).width);
    }

    // === alignSelf ===

    @Test
    void alignSelf_overridesContainer() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100))
                .setAlignItems(AlignItems.STRETCH);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).alignSelf(AlignSelf.CENTER));
        panel.doLayout();

        assertEquals(35, boundsOf(a).y);
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void alignSelf_auto_fallsBackToContainer() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100))
                .setAlignItems(AlignItems.END);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).alignSelf(AlignSelf.AUTO));
        panel.doLayout();

        assertEquals(70, boundsOf(a).y);
        assertEquals(30, boundsOf(a).height);
    }

    // === margin 付きアライメント ===

    @Test
    void margin_withStretch() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100));
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).margin(10, 20, 10, 20));
        panel.doLayout();

        assertEquals(20, boundsOf(a).x);
        assertEquals(10, boundsOf(a).y);
        assertEquals(160, boundsOf(a).width);  // 200 - 20 - 20
        assertEquals(80, boundsOf(a).height);  // 100 - 10 - 10
    }

    @Test
    void margin_withCenter() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100))
                .setJustifyItems(JustifyItems.CENTER)
                .setAlignItems(AlignItems.CENTER);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).margin(10, 20, 10, 20));
        panel.doLayout();

        // margin エリア: x=20, w=160, y=10, h=80
        // CENTER: x = 20 + (160-80)/2 = 60, y = 10 + (80-30)/2 = 35
        assertEquals(60, boundsOf(a).x);
        assertEquals(35, boundsOf(a).y);
        assertEquals(80, boundsOf(a).width);
        assertEquals(30, boundsOf(a).height);
    }

    // === 複合テスト ===

    @Test
    void mixedAlignment_perItem() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100), TrackSize.fixed(100))
                .setJustifyItems(JustifyItems.STRETCH)
                .setAlignItems(AlignItems.STRETCH);
        javax.swing.JPanel panel = createContainer(layout, 200, 200);

        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0)
                .justifySelf(JustifySelf.START).alignSelf(AlignSelf.START));
        panel.add(b, new GridConstraints().column(0).row(1)
                .justifySelf(JustifySelf.END).alignSelf(AlignSelf.END));
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 80, 30), boundsOf(a));
        assertEquals(new Rectangle(120, 170, 80, 30), boundsOf(b));
    }

    // === margin > cell size (Bug 1-2 回帰テスト) ===

    @Test
    void margin_exceedsCellSize_clampedToZero() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fixed(50));
        javax.swing.JPanel panel = createContainer(layout, 100, 50);

        Component a = fixedSize(80, 30);
        // margin 合計がセルサイズを超える: left(60) + right(60) = 120 > 100
        panel.add(a, new GridConstraints().column(0).row(0).margin(30, 60, 30, 60));
        panel.doLayout();

        // 負サイズにならず、0 にクランプされる
        assertTrue(boundsOf(a).width >= 0);
        assertTrue(boundsOf(a).height >= 0);
    }

    // === justifySelf 全値テスト ===

    @Test
    void justifySelf_start() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200));
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).justifySelf(JustifySelf.START));
        panel.doLayout();

        assertEquals(0, boundsOf(a).x);
        assertEquals(80, boundsOf(a).width);
    }

    @Test
    void justifySelf_end() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200));
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).justifySelf(JustifySelf.END));
        panel.doLayout();

        assertEquals(120, boundsOf(a).x);
        assertEquals(80, boundsOf(a).width);
    }

    @Test
    void justifySelf_stretch() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200));
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).justifySelf(JustifySelf.STRETCH));
        panel.doLayout();

        assertEquals(0, boundsOf(a).x);
        assertEquals(200, boundsOf(a).width);
    }

    // === alignSelf 全値テスト ===

    @Test
    void alignSelf_start() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100));
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).alignSelf(AlignSelf.START));
        panel.doLayout();

        assertEquals(0, boundsOf(a).y);
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void alignSelf_end() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100));
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).alignSelf(AlignSelf.END));
        panel.doLayout();

        assertEquals(70, boundsOf(a).y);
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void alignSelf_center() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100));
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).alignSelf(AlignSelf.CENTER));
        panel.doLayout();

        assertEquals(35, boundsOf(a).y);
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void alignSelf_stretch() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100));
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).alignSelf(AlignSelf.STRETCH));
        panel.doLayout();

        assertEquals(0, boundsOf(a).y);
        assertEquals(100, boundsOf(a).height);
    }

    // === margin + START / END アライメント ===

    @Test
    void margin_withStart() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100))
                .setJustifyItems(JustifyItems.START)
                .setAlignItems(AlignItems.START);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).margin(5, 10, 5, 10));
        panel.doLayout();

        // margin エリア内の START
        assertEquals(10, boundsOf(a).x);
        assertEquals(5, boundsOf(a).y);
        assertEquals(80, boundsOf(a).width);
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void margin_withEnd() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100))
                .setJustifyItems(JustifyItems.END)
                .setAlignItems(AlignItems.END);
        javax.swing.JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).margin(5, 10, 5, 10));
        panel.doLayout();

        // margin エリア: x=10, w=180, y=5, h=90
        // END: x = 10 + (180 - 80) = 110, y = 5 + (90 - 30) = 65
        assertEquals(110, boundsOf(a).x);
        assertEquals(65, boundsOf(a).y);
        assertEquals(80, boundsOf(a).width);
        assertEquals(30, boundsOf(a).height);
    }
}

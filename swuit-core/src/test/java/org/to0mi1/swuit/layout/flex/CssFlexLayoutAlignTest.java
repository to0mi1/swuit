package org.to0mi1.swuit.layout.flex;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.to0mi1.swuit.layout.flex.CssFlexLayoutTest.*;

class CssFlexLayoutAlignTest {

    // === alignItems ===

    @Test
    void alignItems_stretch() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssAlignItems(CssAlignItems.STRETCH);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a);
        panel.doLayout();

        assertEquals(100, boundsOf(a).height); // STRETCH: コンテナ高さ全体
    }

    @Test
    void alignItems_flexStart() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssAlignItems(CssAlignItems.FLEX_START);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a);
        panel.doLayout();

        assertEquals(0, boundsOf(a).y);
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void alignItems_flexEnd() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssAlignItems(CssAlignItems.FLEX_END);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a);
        panel.doLayout();

        assertEquals(70, boundsOf(a).y); // 100-30
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void alignItems_center() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssAlignItems(CssAlignItems.CENTER);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a);
        panel.doLayout();

        assertEquals(35, boundsOf(a).y); // (100-30)/2
        assertEquals(30, boundsOf(a).height);
    }

    // === alignSelf ===

    @Test
    void alignSelf_overridesCssAlignItems() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssAlignItems(CssAlignItems.STRETCH);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(100, 30);
        panel.add(a); // STRETCH
        panel.add(b, new CssFlexConstraints().alignSelf(CssAlignSelf.CENTER));
        panel.doLayout();

        assertEquals(100, boundsOf(a).height); // STRETCH
        assertEquals(35, boundsOf(b).y);       // CENTER: (100-30)/2
        assertEquals(30, boundsOf(b).height);
    }

    @Test
    void alignSelf_auto_inheritsCssAlignItems() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssAlignItems(CssAlignItems.FLEX_END);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a, new CssFlexConstraints().alignSelf(CssAlignSelf.AUTO));
        panel.doLayout();

        // AUTO → FLEX_END
        assertEquals(70, boundsOf(a).y);
    }

    @Test
    void alignSelf_allVariants() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssAlignItems(CssAlignItems.STRETCH);
        JPanel panel = createContainer(layout, 500, 100);
        Component start = fixedSize(100, 30);
        Component end = fixedSize(100, 30);
        Component center = fixedSize(100, 30);
        Component stretch = fixedSize(100, 30);
        panel.add(start, new CssFlexConstraints().alignSelf(CssAlignSelf.FLEX_START));
        panel.add(end, new CssFlexConstraints().alignSelf(CssAlignSelf.FLEX_END));
        panel.add(center, new CssFlexConstraints().alignSelf(CssAlignSelf.CENTER));
        panel.add(stretch, new CssFlexConstraints().alignSelf(CssAlignSelf.STRETCH));
        panel.doLayout();

        assertEquals(0, boundsOf(start).y);
        assertEquals(30, boundsOf(start).height);

        assertEquals(70, boundsOf(end).y);
        assertEquals(30, boundsOf(end).height);

        assertEquals(35, boundsOf(center).y);
        assertEquals(30, boundsOf(center).height);

        assertEquals(0, boundsOf(stretch).y);
        assertEquals(100, boundsOf(stretch).height);
    }

    // === COLUMN + align ===

    @Test
    void alignItems_column_center() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.COLUMN)
                .setCssAlignItems(CssAlignItems.CENTER);
        JPanel panel = createContainer(layout, 300, 400);
        Component a = fixedSize(100, 30);
        panel.add(a);
        panel.doLayout();

        assertEquals(100, boundsOf(a).x); // (300-100)/2
        assertEquals(100, boundsOf(a).width);
    }

    // === flexShrink ===

    @Test
    void flexShrink_proportional() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 200, 100);
        // total basis = 150+150=300, overflow=100
        Component a = fixedSize(150, 30);
        Component b = fixedSize(150, 30);
        panel.add(a, new CssFlexConstraints().flexShrink(1));
        panel.add(b, new CssFlexConstraints().flexShrink(1));
        panel.doLayout();

        // 均等縮小 → 各 100
        assertEquals(100, boundsOf(a).width);
        assertEquals(100, boundsOf(b).width);
    }

    @Test
    void flexShrink_zero_preventsShrink() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 200, 100);
        Component a = fixedSize(150, 30);
        Component b = fixedSize(150, 30);
        panel.add(a, new CssFlexConstraints().flexShrink(0));
        panel.add(b, new CssFlexConstraints().flexShrink(1).minWidth(0));
        panel.doLayout();

        // a は縮まない、b が全縮小
        assertEquals(150, boundsOf(a).width);
        assertEquals(50, boundsOf(b).width);
    }

    // === flexBasisPercent ===

    @Test
    void flexBasisPercent() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(100, 30);
        panel.add(a, new CssFlexConstraints().flexBasisPercent(50));
        panel.doLayout();

        // basis = 50% of 400 = 200
        assertEquals(200, boundsOf(a).width);
    }

    // === min/max ===

    @Test
    void maxWidth_clampsGrow() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(0, 30);
        Component b = fixedSize(0, 30);
        panel.add(a, new CssFlexConstraints().flexGrow(1).maxWidth(100));
        panel.add(b, new CssFlexConstraints().flexGrow(1));
        panel.doLayout();

        // a は max 100 でクランプ、残り 300 が b へ
        assertEquals(100, boundsOf(a).width);
        assertEquals(300, boundsOf(b).width);
    }

    @Test
    void minWidth_preventsShrink() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 200, 100);
        Component a = fixedSize(150, 30);
        Component b = fixedSize(150, 30);
        panel.add(a, new CssFlexConstraints().flexShrink(1).minWidth(120));
        panel.add(b, new CssFlexConstraints().flexShrink(1));
        panel.doLayout();

        // a は min 120 でクランプ、b が残り80を受け持つ
        assertEquals(120, boundsOf(a).width);
        assertEquals(80, boundsOf(b).width);
    }
}

package org.to0mi1.swuit.layout.flex;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.to0mi1.swuit.layout.flex.CssFlexLayoutTest.*;

class CssFlexLayoutWrapTest {

    // === WRAP 基本 ===

    @Test
    void wrap_basicLineBreak() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP);
        JPanel panel = createContainer(layout, 250, 200);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(100, 30);
        Component c = fixedSize(100, 30);
        panel.add(a);
        panel.add(b);
        panel.add(c);
        panel.doLayout();

        // 250px に 100+100=200 → 1行目に a,b。c は2行目
        assertEquals(0, boundsOf(a).x);
        assertEquals(100, boundsOf(b).x);
        assertEquals(0, boundsOf(c).x);
        // 副軸位置が異なる
        assertTrue(boundsOf(c).y > boundsOf(a).y);
    }

    @Test
    void wrap_singleLine() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP);
        JPanel panel = createContainer(layout, 400, 200);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(100, 30);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // 十分なスペース → 1行
        assertEquals(0, boundsOf(a).x);
        assertEquals(100, boundsOf(b).x);
        assertEquals(boundsOf(a).y, boundsOf(b).y);
    }

    @Test
    void wrap_withGap() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP)
                .setMainAxisGap(20);
        JPanel panel = createContainer(layout, 250, 200);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(100, 30);
        Component c = fixedSize(100, 30);
        panel.add(a);
        panel.add(b);
        panel.add(c);
        panel.doLayout();

        // 100+20+100=220 ≤ 250 → 1行目に a,b。c は2行目
        assertEquals(0, boundsOf(a).x);
        assertEquals(120, boundsOf(b).x);
        assertEquals(0, boundsOf(c).x);
        assertTrue(boundsOf(c).y > boundsOf(a).y);
    }

    // === WRAP_REVERSE ===

    @Test
    void wrapReverse_linesInReverseCrossOrder() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP_REVERSE);
        JPanel panel = createContainer(layout, 150, 200);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(100, 30);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // a は1行目、b は2行目だが WRAP_REVERSE で副軸反転
        assertTrue(boundsOf(a).y > boundsOf(b).y);
    }

    // === alignContent ===

    @Test
    void alignContent_flexStart() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP)
                .setCssAlignContent(CssAlignContent.FLEX_START);
        JPanel panel = createContainer(layout, 100, 300);
        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // 2行、各30px。alignContent=FLEX_START → 上端に詰める
        assertEquals(0, boundsOf(a).y);
    }

    @Test
    void alignContent_center() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP)
                .setCssAlignContent(CssAlignContent.CENTER);
        JPanel panel = createContainer(layout, 100, 300);
        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // 2行 60px total → offset = (300-60)/2 = 120
        assertEquals(120, boundsOf(a).y);
    }

    @Test
    void alignContent_spaceBetween() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP)
                .setCssAlignContent(CssAlignContent.SPACE_BETWEEN);
        JPanel panel = createContainer(layout, 100, 300);
        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // 2行、free=240、spaceBetween → a=0, b=300-30=270
        assertEquals(0, boundsOf(a).y);
        assertEquals(270, boundsOf(b).y);
    }
}

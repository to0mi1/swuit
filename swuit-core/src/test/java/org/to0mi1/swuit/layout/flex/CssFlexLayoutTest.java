package org.to0mi1.swuit.layout.flex;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CssFlexLayoutTest {

    // === ヘルパー ===

    static JPanel createContainer(CssFlexLayout layout, int width, int height) {
        JPanel panel = new JPanel(layout);
        panel.setSize(width, height);
        return panel;
    }

    static Component fixedSize(int width, int height) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width, height));
        label.setMinimumSize(new Dimension(width / 2, height / 2));
        return label;
    }

    static Rectangle boundsOf(Component c) {
        return c.getBounds();
    }

    // === preferredLayoutSize ===

    @Test
    void preferredSize_row_basic() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 400, 200);
        panel.add(fixedSize(100, 30));
        panel.add(fixedSize(50, 40));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(150, pref.width);
        assertEquals(40, pref.height);
    }

    @Test
    void preferredSize_column_basic() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.COLUMN);
        JPanel panel = createContainer(layout, 400, 200);
        panel.add(fixedSize(100, 30));
        panel.add(fixedSize(50, 40));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(100, pref.width);
        assertEquals(70, pref.height);
    }

    @Test
    void preferredSize_withGap() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW).setMainAxisGap(10);
        JPanel panel = createContainer(layout, 400, 200);
        panel.add(fixedSize(100, 30));
        panel.add(fixedSize(50, 30));
        panel.add(fixedSize(50, 30));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(220, pref.width); // 100+50+50 + 10*2
    }

    @Test
    void preferredSize_withMargin() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 400, 200);
        Component c = fixedSize(100, 30);
        panel.add(c, new CssFlexConstraints().margin(5, 10, 5, 10));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(120, pref.width);  // 100 + 10 + 10
        assertEquals(40, pref.height);  // 30 + 5 + 5
    }

    @Test
    void preferredSize_withInsets() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = new JPanel(layout) {
            @Override
            public Insets getInsets() {
                return new Insets(10, 20, 10, 20);
            }
        };
        panel.setSize(400, 200);
        panel.add(fixedSize(100, 30));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(140, pref.width);  // 100 + 20 + 20
        assertEquals(50, pref.height);  // 30 + 10 + 10
    }

    @Test
    void preferredSize_invisibleSkipped() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW).setMainAxisGap(10);
        JPanel panel = createContainer(layout, 400, 200);
        panel.add(fixedSize(100, 30));
        Component invisible = fixedSize(50, 30);
        invisible.setVisible(false);
        panel.add(invisible);
        panel.add(fixedSize(50, 30));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(160, pref.width); // 100+50 + 10*1
    }

    @Test
    void preferredSize_emptyContainer() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 400, 200);

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(0, pref.width);
        assertEquals(0, pref.height);
    }

    // === 基本配置 (ROW) ===

    @Test
    void layout_row_basic() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(50, 40);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // ROW 基本配置、alignItems=STRETCH (デフォルト)
        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(a));
        assertEquals(new Rectangle(100, 0, 50, 100), boundsOf(b));
    }

    @Test
    void layout_row_withGap() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW).setMainAxisGap(10);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(50, 30);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(a));
        assertEquals(new Rectangle(110, 0, 50, 100), boundsOf(b));
    }

    @Test
    void layout_row_withMargin() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(100, 30);
        panel.add(a, new CssFlexConstraints().margin(5, 10, 5, 10));
        panel.doLayout();

        // margin: top=5, left=10, bottom=5, right=10
        // alignItems=STRETCH: height = 100 - 5 - 5 = 90
        assertEquals(new Rectangle(10, 5, 100, 90), boundsOf(a));
    }

    @Test
    void layout_row_withInsets() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = new JPanel(layout) {
            @Override
            public Insets getInsets() {
                return new Insets(10, 20, 10, 20);
            }
        };
        panel.setSize(400, 100);
        Component a = fixedSize(100, 30);
        panel.add(a);
        panel.doLayout();

        // insets: top=10, left=20; alignItems=STRETCH: height = 100-10-10 = 80
        assertEquals(new Rectangle(20, 10, 100, 80), boundsOf(a));
    }

    @Test
    void layout_invisibleSkipped() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(100, 30);
        Component invisible = fixedSize(50, 30);
        invisible.setVisible(false);
        Component b = fixedSize(50, 30);
        panel.add(a);
        panel.add(invisible);
        panel.add(b);
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(a));
        assertEquals(new Rectangle(100, 0, 50, 100), boundsOf(b));
    }

    // === flexGrow ===

    @Test
    void flexGrow_equalDistribution() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(10, 30);
        Component b = fixedSize(10, 30);
        Component c = fixedSize(10, 30);
        panel.add(a, new CssFlexConstraints().flexGrow(1));
        panel.add(b, new CssFlexConstraints().flexGrow(1));
        panel.add(c, new CssFlexConstraints().flexGrow(1));
        panel.doLayout();

        assertEquals(100, boundsOf(a).width);
        assertEquals(100, boundsOf(b).width);
        assertEquals(100, boundsOf(c).width);
    }

    @Test
    void flexGrow_unequalDistribution() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(0, 30);
        Component b = fixedSize(0, 30);
        panel.add(a, new CssFlexConstraints().flexGrow(1));
        panel.add(b, new CssFlexConstraints().flexGrow(3));
        panel.doLayout();

        assertEquals(100, boundsOf(a).width);
        assertEquals(300, boundsOf(b).width);
    }

    @Test
    void flexGrow_mixedWithFixed() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 300, 100);
        Component fixed = fixedSize(100, 30);
        Component weighted = fixedSize(10, 30);
        panel.add(fixed);
        panel.add(weighted, new CssFlexConstraints().flexGrow(1));
        panel.doLayout();

        assertEquals(100, boundsOf(fixed).width);
        assertEquals(200, boundsOf(weighted).width);
    }

    @Test
    void flexGrow_noExcess() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 110, 100);
        Component a = fixedSize(50, 30);
        Component b = fixedSize(50, 30);
        Component c = fixedSize(10, 30);
        panel.add(a);
        panel.add(b);
        panel.add(c, new CssFlexConstraints().flexGrow(1));
        panel.doLayout();

        // 50+50+10=110、余剰なし → grow の子は元のサイズ
        assertEquals(50, boundsOf(a).width);
        assertEquals(50, boundsOf(b).width);
        assertEquals(10, boundsOf(c).width);
    }

    // === flexDirection ===

    @Test
    void layout_column_basic() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.COLUMN);
        JPanel panel = createContainer(layout, 200, 400);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(50, 40);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // COLUMN: 垂直配置、alignItems=STRETCH → 幅 200
        assertEquals(new Rectangle(0, 0, 200, 30), boundsOf(a));
        assertEquals(new Rectangle(0, 30, 200, 40), boundsOf(b));
    }

    @Test
    void layout_rowReverse() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW_REVERSE);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(50, 30);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // ROW_REVERSE: 右端から配置
        assertEquals(new Rectangle(200, 0, 100, 100), boundsOf(a));
        assertEquals(new Rectangle(150, 0, 50, 100), boundsOf(b));
    }

    @Test
    void layout_columnReverse() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.COLUMN_REVERSE);
        JPanel panel = createContainer(layout, 200, 300);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(50, 40);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // COLUMN_REVERSE: 下端から配置
        assertEquals(new Rectangle(0, 270, 200, 30), boundsOf(a));
        assertEquals(new Rectangle(0, 230, 200, 40), boundsOf(b));
    }

    @Test
    void flexGrow_column() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.COLUMN);
        JPanel panel = createContainer(layout, 200, 300);
        Component a = fixedSize(100, 10);
        Component b = fixedSize(100, 10);
        Component c = fixedSize(100, 10);
        panel.add(a, new CssFlexConstraints().flexGrow(1));
        panel.add(b, new CssFlexConstraints().flexGrow(1));
        panel.add(c, new CssFlexConstraints().flexGrow(1));
        panel.doLayout();

        assertEquals(100, boundsOf(a).height);
        assertEquals(100, boundsOf(b).height);
        assertEquals(100, boundsOf(c).height);
    }

    // === justifyContent ===

    @Test
    void justifyContent_flexEnd() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssJustifyContent(CssJustifyContent.FLEX_END);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a);
        panel.doLayout();

        assertEquals(200, boundsOf(a).x); // 300 - 100
    }

    @Test
    void justifyContent_center() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssJustifyContent(CssJustifyContent.CENTER);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a);
        panel.doLayout();

        assertEquals(100, boundsOf(a).x); // (300-100)/2
    }

    @Test
    void justifyContent_spaceBetween() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssJustifyContent(CssJustifyContent.SPACE_BETWEEN);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(50, 30);
        Component b = fixedSize(50, 30);
        Component c = fixedSize(50, 30);
        panel.add(a);
        panel.add(b);
        panel.add(c);
        panel.doLayout();

        // free=150, between=150/2=75
        assertEquals(0, boundsOf(a).x);
        assertEquals(125, boundsOf(b).x);
        assertEquals(250, boundsOf(c).x);
    }

    @Test
    void justifyContent_spaceAround() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssJustifyContent(CssJustifyContent.SPACE_AROUND);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(60, 30);
        Component b = fixedSize(60, 30);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // free=180, around=180/2=90, offset=45, between=90
        assertEquals(45, boundsOf(a).x);
        assertEquals(195, boundsOf(b).x); // 45+60+90
    }

    @Test
    void justifyContent_spaceEvenly() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
                .setCssJustifyContent(CssJustifyContent.SPACE_EVENLY);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(60, 30);
        Component b = fixedSize(60, 30);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // free=180, evenly=180/3=60
        assertEquals(60, boundsOf(a).x);
        assertEquals(180, boundsOf(b).x); // 60+60+60
    }

    // === order ソート ===

    @Test
    void order_sort() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(100, 30);
        Component c = fixedSize(100, 30);
        panel.add(a, new CssFlexConstraints().order(2));
        panel.add(b, new CssFlexConstraints().order(0));
        panel.add(c, new CssFlexConstraints().order(1));
        panel.doLayout();

        // order: b(0), c(1), a(2)
        assertEquals(200, boundsOf(a).x);
        assertEquals(0, boundsOf(b).x);
        assertEquals(100, boundsOf(c).x);
    }

    // === 不正な制約 ===

    @Test
    void addWithInvalidConstraints_throws() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        assertThrows(IllegalArgumentException.class, () -> panel.add(a, "invalid"));
    }
}

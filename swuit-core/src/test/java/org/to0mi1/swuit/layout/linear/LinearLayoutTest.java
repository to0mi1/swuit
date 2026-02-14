package org.to0mi1.swuit.layout.linear;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;
import org.to0mi1.swuit.layout.Gravity;
import org.to0mi1.swuit.layout.Orientation;

import static org.junit.jupiter.api.Assertions.*;

class LinearLayoutTest {

    // === ヘルパー ===

    private static JPanel createContainer(LinearLayout layout, int width, int height) {
        JPanel panel = new JPanel(layout);
        panel.setSize(width, height);
        return panel;
    }

    private static Component fixedSize(int width, int height) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width, height));
        label.setMinimumSize(new Dimension(width / 2, height / 2));
        return label;
    }

    private static Rectangle boundsOf(Component c) {
        return c.getBounds();
    }

    // === preferredLayoutSize / minimumLayoutSize ===

    @Test
    void preferredSize_horizontal_basic() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 400, 200);
        panel.add(fixedSize(100, 30));
        panel.add(fixedSize(50, 40));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(150, pref.width);
        assertEquals(40, pref.height);
    }

    @Test
    void preferredSize_vertical_basic() {
        LinearLayout layout = new LinearLayout(Orientation.VERTICAL);
        JPanel panel = createContainer(layout, 400, 200);
        panel.add(fixedSize(100, 30));
        panel.add(fixedSize(50, 40));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(100, pref.width);
        assertEquals(70, pref.height);
    }

    @Test
    void preferredSize_withGap() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL, 10);
        JPanel panel = createContainer(layout, 400, 200);
        panel.add(fixedSize(100, 30));
        panel.add(fixedSize(50, 30));
        panel.add(fixedSize(50, 30));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(220, pref.width); // 100+50+50 + 10*2
    }

    @Test
    void preferredSize_withMargin() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 400, 200);
        Component c = fixedSize(100, 30);
        panel.add(c, new LinearConstraints(0, Gravity.NONE, new Insets(5, 10, 5, 10)));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(120, pref.width);  // 100 + 10 + 10
        assertEquals(40, pref.height);  // 30 + 5 + 5
    }

    @Test
    void preferredSize_withInsets() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
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
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL, 10);
        JPanel panel = createContainer(layout, 400, 200);
        panel.add(fixedSize(100, 30));
        Component invisible = fixedSize(50, 30);
        invisible.setVisible(false);
        panel.add(invisible);
        panel.add(fixedSize(50, 30));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(160, pref.width); // 100+50 + 10*1 (invisible skipped)
    }

    @Test
    void minimumSize_usesMinimumOfChildren() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 400, 200);
        panel.add(fixedSize(100, 30)); // min = 50x15

        Dimension min = layout.minimumLayoutSize(panel);
        assertEquals(50, min.width);
        assertEquals(15, min.height);
    }

    @Test
    void preferredSize_emptyContainer() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 400, 200);

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(0, pref.width);
        assertEquals(0, pref.height);
    }

    // === 基本配置 ===

    @Test
    void layout_horizontal_basic() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(50, 40);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // 水平配置、副軸は FILL（デフォルト）
        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(a));
        assertEquals(new Rectangle(100, 0, 50, 100), boundsOf(b));
    }

    @Test
    void layout_vertical_basic() {
        LinearLayout layout = new LinearLayout(Orientation.VERTICAL);
        JPanel panel = createContainer(layout, 200, 400);
        Component a = fixedSize(100, 30);
        Component b = fixedSize(50, 40);
        panel.add(a);
        panel.add(b);
        panel.doLayout();

        // 垂直配置、副軸は FILL
        assertEquals(new Rectangle(0, 0, 200, 30), boundsOf(a));
        assertEquals(new Rectangle(0, 30, 200, 40), boundsOf(b));
    }

    @Test
    void layout_horizontal_withGap() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL, 10);
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
    void layout_withMargin() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(100, 30);
        panel.add(a, new LinearConstraints(0, Gravity.NONE, new Insets(5, 10, 5, 10)));
        panel.doLayout();

        // margin: top=5, left=10, bottom=5, right=10
        // 副軸 FILL: height = 100 - 5 - 5 = 90
        assertEquals(new Rectangle(10, 5, 100, 90), boundsOf(a));
    }

    @Test
    void layout_withInsets() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
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

        // insets: top=10, left=20
        // 副軸 FILL: height = 100 - 10 - 10 = 80
        assertEquals(new Rectangle(20, 10, 100, 80), boundsOf(a));
    }

    @Test
    void layout_invisibleSkipped() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
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

    // === Weight ===

    @Test
    void weight_equalDistribution() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(10, 30);
        Component b = fixedSize(10, 30);
        Component c = fixedSize(10, 30);
        panel.add(a, new LinearConstraints(1));
        panel.add(b, new LinearConstraints(1));
        panel.add(c, new LinearConstraints(1));
        panel.doLayout();

        assertEquals(100, boundsOf(a).width);
        assertEquals(100, boundsOf(b).width);
        assertEquals(100, boundsOf(c).width);
    }

    @Test
    void weight_unequalDistribution() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(10, 30);
        Component b = fixedSize(10, 30);
        panel.add(a, new LinearConstraints(1));
        panel.add(b, new LinearConstraints(3));
        panel.doLayout();

        assertEquals(100, boundsOf(a).width);
        assertEquals(300, boundsOf(b).width);
    }

    @Test
    void weight_mixedWithFixed() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 300, 100);
        Component fixed = fixedSize(100, 30);
        Component weighted = fixedSize(10, 30);
        panel.add(fixed);
        panel.add(weighted, new LinearConstraints(1));
        panel.doLayout();

        assertEquals(100, boundsOf(fixed).width);
        assertEquals(200, boundsOf(weighted).width);
    }

    @Test
    void weight_withWeightSum() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        layout.setWeightSum(4);
        JPanel panel = createContainer(layout, 400, 100);
        Component a = fixedSize(10, 30);
        Component b = fixedSize(10, 30);
        panel.add(a, new LinearConstraints(1));
        panel.add(b, new LinearConstraints(1));
        panel.doLayout();

        // weightSum=4, 各 weight=1 → 各 100px, 残り200pxは未使用
        assertEquals(100, boundsOf(a).width);
        assertEquals(100, boundsOf(b).width);
    }

    @Test
    void weight_zeroExcess() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 100, 100);
        Component a = fixedSize(50, 30);
        Component b = fixedSize(50, 30);
        Component c = fixedSize(10, 30);
        panel.add(a);
        panel.add(b);
        panel.add(c, new LinearConstraints(1));
        panel.doLayout();

        // 固定子で100px、余剰なし → weight の子は 0
        assertEquals(50, boundsOf(a).width);
        assertEquals(50, boundsOf(b).width);
        assertEquals(0, boundsOf(c).width);
    }

    // === Gravity ===

    @Test
    void gravity_mainAxis_center() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL, 0, Gravity.CENTER_HORIZONTAL);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a);
        panel.doLayout();

        // 300 - 100 = 200 余り → offset = 100
        assertEquals(100, boundsOf(a).x);
    }

    @Test
    void gravity_mainAxis_end() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL, 0, Gravity.RIGHT);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a);
        panel.doLayout();

        // 300 - 100 = 200 → offset = 200
        assertEquals(200, boundsOf(a).x);
    }

    @Test
    void gravity_crossAxis_center() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a, new LinearConstraints(0, Gravity.CENTER_VERTICAL));
        panel.doLayout();

        assertEquals(35, boundsOf(a).y); // (100 - 30) / 2
        assertEquals(30, boundsOf(a).height);
    }

    @Test
    void gravity_crossAxis_end() {
        LinearLayout layout = new LinearLayout(Orientation.VERTICAL);
        JPanel panel = createContainer(layout, 300, 400);
        Component a = fixedSize(100, 30);
        panel.add(a, new LinearConstraints(0, Gravity.RIGHT));
        panel.doLayout();

        assertEquals(200, boundsOf(a).x); // 300 - 100
        assertEquals(100, boundsOf(a).width);
    }

    // === Vertical weight ===

    @Test
    void weight_vertical_equalDistribution() {
        LinearLayout layout = new LinearLayout(Orientation.VERTICAL);
        JPanel panel = createContainer(layout, 200, 300);
        Component a = fixedSize(100, 10);
        Component b = fixedSize(100, 10);
        Component c = fixedSize(100, 10);
        panel.add(a, new LinearConstraints(1));
        panel.add(b, new LinearConstraints(1));
        panel.add(c, new LinearConstraints(1));
        panel.doLayout();

        assertEquals(100, boundsOf(a).height);
        assertEquals(100, boundsOf(b).height);
        assertEquals(100, boundsOf(c).height);
    }

    // === Vertical gravity ===

    @Test
    void gravity_vertical_mainAxis_center() {
        LinearLayout layout = new LinearLayout(Orientation.VERTICAL, 0, Gravity.CENTER_VERTICAL);
        JPanel panel = createContainer(layout, 200, 300);
        Component a = fixedSize(100, 50);
        panel.add(a);
        panel.doLayout();

        assertEquals(125, boundsOf(a).y); // (300 - 50) / 2
    }

    @Test
    void gravity_vertical_mainAxis_end() {
        LinearLayout layout = new LinearLayout(Orientation.VERTICAL, 0, Gravity.BOTTOM);
        JPanel panel = createContainer(layout, 200, 300);
        Component a = fixedSize(100, 50);
        panel.add(a);
        panel.doLayout();

        assertEquals(250, boundsOf(a).y); // 300 - 50
    }

    // === 制約なし addLayoutComponent(String, Component) ===

    @Test
    void addWithoutConstraints_usesDefaults() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        panel.add(a); // no constraints
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(a));
    }

    // === 不正な制約 ===

    @Test
    void addWithInvalidConstraints_throws() {
        LinearLayout layout = new LinearLayout(Orientation.HORIZONTAL);
        JPanel panel = createContainer(layout, 300, 100);
        Component a = fixedSize(100, 30);
        assertThrows(IllegalArgumentException.class, () -> panel.add(a, "invalid"));
    }
}

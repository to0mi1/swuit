package org.to0mi1.swuit.layout.aspectratio;

import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import static org.junit.jupiter.api.Assertions.*;

class AspectRatioLayoutTest {

    // === コンストラクタ ===

    @Test
    void constructor_validRatio() {
        AspectRatioLayout layout = new AspectRatioLayout(16.0 / 9.0);
        assertEquals(16.0 / 9.0, layout.getRatio(), 0.0001);
    }

    @Test
    void constructor_zeroRatio_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AspectRatioLayout(0));
    }

    @Test
    void constructor_negativeRatio_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AspectRatioLayout(-1));
    }

    @Test
    void constructor_nanRatio_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AspectRatioLayout(Double.NaN));
    }

    @Test
    void constructor_infiniteRatio_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new AspectRatioLayout(Double.POSITIVE_INFINITY));
    }

    // === preferredLayoutSize ===

    @Test
    void preferredLayoutSize_16by9_calculatesHeight() {
        AspectRatioLayout layout = new AspectRatioLayout(16.0 / 9.0);
        JPanel parent = new JPanel(layout);
        JLabel child = new JLabel();
        child.setPreferredSize(new Dimension(320, 100));
        parent.add(child);

        Dimension pref = layout.preferredLayoutSize(parent);
        assertEquals(320, pref.width);
        assertEquals(180, pref.height); // 320 / (16/9) = 180
    }

    @Test
    void preferredLayoutSize_1by1_squareOutput() {
        AspectRatioLayout layout = new AspectRatioLayout(1.0);
        JPanel parent = new JPanel(layout);
        JLabel child = new JLabel();
        child.setPreferredSize(new Dimension(200, 50));
        parent.add(child);

        Dimension pref = layout.preferredLayoutSize(parent);
        assertEquals(200, pref.width);
        assertEquals(200, pref.height);
    }

    @Test
    void preferredLayoutSize_4by3() {
        AspectRatioLayout layout = new AspectRatioLayout(4.0 / 3.0);
        JPanel parent = new JPanel(layout);
        JLabel child = new JLabel();
        child.setPreferredSize(new Dimension(400, 100));
        parent.add(child);

        Dimension pref = layout.preferredLayoutSize(parent);
        assertEquals(400, pref.width);
        assertEquals(300, pref.height); // 400 / (4/3) = 300
    }

    @Test
    void preferredLayoutSize_noChildren_zeroSize() {
        AspectRatioLayout layout = new AspectRatioLayout(16.0 / 9.0);
        JPanel parent = new JPanel(layout);

        Dimension pref = layout.preferredLayoutSize(parent);
        assertEquals(0, pref.width);
        assertEquals(0, pref.height);
    }

    @Test
    void preferredLayoutSize_invisibleChild_zeroSize() {
        AspectRatioLayout layout = new AspectRatioLayout(16.0 / 9.0);
        JPanel parent = new JPanel(layout);
        JLabel child = new JLabel();
        child.setPreferredSize(new Dimension(320, 100));
        child.setVisible(false);
        parent.add(child);

        Dimension pref = layout.preferredLayoutSize(parent);
        assertEquals(0, pref.width);
        assertEquals(0, pref.height);
    }

    @Test
    void preferredLayoutSize_withInsets() {
        AspectRatioLayout layout = new AspectRatioLayout(2.0);
        JPanel parent = new JPanel(layout) {
            @Override
            public Insets getInsets() {
                return new Insets(10, 20, 10, 20);
            }
        };
        JLabel child = new JLabel();
        child.setPreferredSize(new Dimension(200, 50));
        parent.add(child);

        Dimension pref = layout.preferredLayoutSize(parent);
        // width: 200 + 20 + 20 = 240
        // height: 200/2.0 + 10 + 10 = 120
        assertEquals(240, pref.width);
        assertEquals(120, pref.height);
    }

    // === minimumLayoutSize ===

    @Test
    void minimumLayoutSize_usesMinimumSize() {
        AspectRatioLayout layout = new AspectRatioLayout(2.0);
        JPanel parent = new JPanel(layout);
        JLabel child = new JLabel();
        child.setMinimumSize(new Dimension(100, 30));
        child.setPreferredSize(new Dimension(200, 50));
        parent.add(child);

        Dimension min = layout.minimumLayoutSize(parent);
        assertEquals(100, min.width);
        assertEquals(50, min.height); // 100 / 2.0 = 50
    }

    // === layoutContainer ===

    @Test
    void layoutContainer_childFillsContainer() {
        AspectRatioLayout layout = new AspectRatioLayout(16.0 / 9.0);
        JPanel parent = new JPanel(layout);
        parent.setSize(400, 300);
        JLabel child = new JLabel();
        parent.add(child);

        layout.layoutContainer(parent);

        assertEquals(0, child.getX());
        assertEquals(0, child.getY());
        assertEquals(400, child.getWidth());
        assertEquals(300, child.getHeight());
    }

    @Test
    void layoutContainer_withInsets() {
        AspectRatioLayout layout = new AspectRatioLayout(16.0 / 9.0);
        JPanel parent = new JPanel(layout) {
            @Override
            public Insets getInsets() {
                return new Insets(5, 10, 5, 10);
            }
        };
        parent.setSize(420, 310);
        JLabel child = new JLabel();
        parent.add(child);

        layout.layoutContainer(parent);

        assertEquals(10, child.getX());
        assertEquals(5, child.getY());
        assertEquals(400, child.getWidth()); // 420 - 10 - 10
        assertEquals(300, child.getHeight()); // 310 - 5 - 5
    }

    @Test
    void layoutContainer_multipleChildren_onlyFirstVisible() {
        AspectRatioLayout layout = new AspectRatioLayout(1.0);
        JPanel parent = new JPanel(layout);
        parent.setSize(200, 200);
        JLabel first = new JLabel("first");
        JLabel second = new JLabel("second");
        parent.add(first);
        parent.add(second);

        layout.layoutContainer(parent);

        // 最初の可視子のみ配置
        assertEquals(200, first.getWidth());
        assertEquals(200, first.getHeight());
    }

    @Test
    void layoutContainer_noVisibleChildren_doesNotThrow() {
        AspectRatioLayout layout = new AspectRatioLayout(1.0);
        JPanel parent = new JPanel(layout);
        parent.setSize(200, 200);

        assertDoesNotThrow(() -> layout.layoutContainer(parent));
    }

    @Test
    void layoutContainer_zeroSize_clampedToZero() {
        AspectRatioLayout layout = new AspectRatioLayout(1.0);
        JPanel parent = new JPanel(layout);
        parent.setSize(0, 0);
        JLabel child = new JLabel();
        parent.add(child);

        layout.layoutContainer(parent);

        assertEquals(0, child.getWidth());
        assertEquals(0, child.getHeight());
    }

    // === LayoutManager2 デフォルト ===

    @Test
    void maximumLayoutSize_returnsMaxValue() {
        AspectRatioLayout layout = new AspectRatioLayout(1.0);
        JPanel parent = new JPanel(layout);
        Dimension max = layout.maximumLayoutSize(parent);
        assertEquals(Integer.MAX_VALUE, max.width);
        assertEquals(Integer.MAX_VALUE, max.height);
    }

    @Test
    void layoutAlignment_centered() {
        AspectRatioLayout layout = new AspectRatioLayout(1.0);
        JPanel parent = new JPanel(layout);
        assertEquals(0.5f, layout.getLayoutAlignmentX(parent));
        assertEquals(0.5f, layout.getLayoutAlignmentY(parent));
    }

    @Test
    void invalidateLayout_doesNotThrow() {
        AspectRatioLayout layout = new AspectRatioLayout(1.0);
        assertDoesNotThrow(() -> layout.invalidateLayout(new JPanel()));
    }

    @Test
    void addRemoveComponent_doNotThrow() {
        AspectRatioLayout layout = new AspectRatioLayout(1.0);
        Component comp = new JLabel();
        assertDoesNotThrow(() -> {
            layout.addLayoutComponent(comp, null);
            layout.addLayoutComponent("name", comp);
            layout.removeLayoutComponent(comp);
        });
    }
}

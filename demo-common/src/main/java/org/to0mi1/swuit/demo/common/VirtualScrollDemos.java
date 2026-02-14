package org.to0mi1.swuit.demo.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.to0mi1.swuit.component.virtualscroll.VirtualScrollPane;
import org.to0mi1.swuit.layout.Orientation;
import org.to0mi1.swuit.layout.flex.CssFlexLayout;
import org.to0mi1.swuit.layout.flex.CssFlexDirection;
import org.to0mi1.swuit.layout.flex.CssFlexWrap;
import org.to0mi1.swuit.layout.linear.LinearLayout;

/**
 * VirtualScrollPane デモパネル生成ユーティリティ。
 */
public final class VirtualScrollDemos {

    private static final int ITEM_COUNT = 1000;

    private static final Color[] COLORS = {
            new Color(0x4CAF50), new Color(0x2196F3), new Color(0xFF9800),
            new Color(0xE91E63), new Color(0x9C27B0), new Color(0x3F51B5),
            new Color(0x00BCD4), new Color(0xFF5722), new Color(0x607D8B),
            new Color(0xFFC107),
    };

    private VirtualScrollDemos() {
    }

    /** GridLayout で 1000 個のパネルを縦に配置し VirtualScrollPane でラップ */
    public static JComponent virtualScrollGridLayout() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 2));
        for (int i = 0; i < ITEM_COUNT; i++) {
            JPanel item = DemoPanels.colorPanel(COLORS[i % COLORS.length], "Grid Item " + i);
            item.setPreferredSize(new Dimension(300, 40));
            panel.add(item);
        }
        VirtualScrollPane sp = new VirtualScrollPane(panel);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return sp;
    }

    /** LinearLayout(VERTICAL) で 1000 個のパネルを配置し VirtualScrollPane でラップ */
    public static JComponent virtualScrollLinearLayout() {
        JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 2));
        for (int i = 0; i < ITEM_COUNT; i++) {
            JPanel item = DemoPanels.colorPanel(COLORS[i % COLORS.length], "Linear Item " + i);
            item.setPreferredSize(new Dimension(300, 40));
            panel.add(item);
        }
        VirtualScrollPane sp = new VirtualScrollPane(panel);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return sp;
    }

    /** CssFlexLayout(COLUMN, WRAP) で 1000 個のパネルを配置し VirtualScrollPane でラップ */
    public static JComponent virtualScrollFlexLayout() {
        CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.COLUMN)
                .setCssFlexWrap(CssFlexWrap.WRAP)
                .setMainAxisGap(2)
                .setCrossAxisGap(2);
        JPanel panel = new JPanel(layout);
        for (int i = 0; i < ITEM_COUNT; i++) {
            JPanel item = DemoPanels.colorPanel(COLORS[i % COLORS.length], "Flex Item " + i);
            item.setPreferredSize(new Dimension(150, 40));
            panel.add(item);
        }
        VirtualScrollPane sp = new VirtualScrollPane(panel);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return sp;
    }
}

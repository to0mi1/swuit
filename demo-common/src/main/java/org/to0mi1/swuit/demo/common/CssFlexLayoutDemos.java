package org.to0mi1.swuit.demo.common;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.to0mi1.swuit.layout.flex.CssAlignItems;
import org.to0mi1.swuit.layout.flex.CssAlignSelf;
import org.to0mi1.swuit.layout.flex.CssFlexLayout;
import org.to0mi1.swuit.layout.flex.CssFlexConstraints;
import org.to0mi1.swuit.layout.flex.CssFlexDirection;
import org.to0mi1.swuit.layout.flex.CssFlexWrap;
import org.to0mi1.swuit.layout.flex.CssJustifyContent;

/**
 * CssFlexLayout デモパネル生成ユーティリティ。
 */
public final class CssFlexLayoutDemos {

    private CssFlexLayoutDemos() {
    }

    /** FlexBox: ROW + flexGrow */
    public static JComponent flexBasic() {
        JPanel panel = new JPanel(new CssFlexLayout(CssFlexDirection.ROW).setMainAxisGap(4));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(DemoPanels.colorPanel(new Color(0x4CAF50), "固定 100px"));
        panel.getComponent(0).setPreferredSize(new Dimension(100, 40));
        panel.add(DemoPanels.colorPanel(new Color(0x2196F3), "grow=1"), new CssFlexConstraints().flexGrow(1));
        panel.add(DemoPanels.colorPanel(new Color(0xFF9800), "grow=2"), new CssFlexConstraints().flexGrow(2));
        return panel;
    }

    /** FlexBox: WRAP + CssJustifyContent */
    public static JComponent flexWrap() {
        JPanel panel = new JPanel(new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP)
                .setCssJustifyContent(CssJustifyContent.SPACE_AROUND)
                .setMainAxisGap(8)
                .setCrossAxisGap(8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Color[] colors = {
                new Color(0xE91E63), new Color(0x9C27B0), new Color(0x3F51B5),
                new Color(0x03A9F4), new Color(0x009688), new Color(0x4CAF50),
                new Color(0xFFC107), new Color(0xFF5722), new Color(0x795548)
        };
        for (int i = 0; i < colors.length; i++) {
            JPanel item = DemoPanels.colorPanel(colors[i], "Item " + (i + 1));
            item.setPreferredSize(new Dimension(120, 50));
            panel.add(item);
        }
        return panel;
    }

    /** FlexBox: alignItems + alignSelf */
    public static JComponent flexAlign() {
        JPanel panel = new JPanel(new CssFlexLayout(CssFlexDirection.ROW)
                .setCssAlignItems(CssAlignItems.CENTER)
                .setMainAxisGap(8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel a = DemoPanels.colorPanel(new Color(0x4CAF50), "STRETCH");
        a.setPreferredSize(new Dimension(100, 40));
        panel.add(a, new CssFlexConstraints().flexGrow(1)
                .alignSelf(CssAlignSelf.STRETCH));

        JPanel b = DemoPanels.colorPanel(new Color(0x2196F3), "CENTER");
        b.setPreferredSize(new Dimension(100, 60));
        panel.add(b, new CssFlexConstraints().flexGrow(1));

        JPanel c = DemoPanels.colorPanel(new Color(0xFF9800), "FLEX_END");
        c.setPreferredSize(new Dimension(100, 30));
        panel.add(c, new CssFlexConstraints().flexGrow(1)
                .alignSelf(CssAlignSelf.FLEX_END));

        JPanel d = DemoPanels.colorPanel(new Color(0xE91E63), "FLEX_START");
        d.setPreferredSize(new Dimension(100, 50));
        panel.add(d, new CssFlexConstraints().flexGrow(1)
                .alignSelf(CssAlignSelf.FLEX_START));

        return panel;
    }
}

package org.to0mi1.swuit.demo.common;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.to0mi1.swuit.layout.flex.AlignItems;
import org.to0mi1.swuit.layout.flex.AlignSelf;
import org.to0mi1.swuit.layout.flex.FlexBoxLayout;
import org.to0mi1.swuit.layout.flex.FlexConstraints;
import org.to0mi1.swuit.layout.flex.FlexDirection;
import org.to0mi1.swuit.layout.flex.FlexWrap;
import org.to0mi1.swuit.layout.flex.JustifyContent;

/**
 * FlexBoxLayout デモパネル生成ユーティリティ。
 */
public final class FlexBoxLayoutDemos {

    private FlexBoxLayoutDemos() {
    }

    /** FlexBox: ROW + flexGrow */
    public static JComponent flexBasic() {
        JPanel panel = new JPanel(new FlexBoxLayout(FlexDirection.ROW).setMainAxisGap(4));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(DemoPanels.colorPanel(new Color(0x4CAF50), "固定 100px"));
        panel.getComponent(0).setPreferredSize(new Dimension(100, 40));
        panel.add(DemoPanels.colorPanel(new Color(0x2196F3), "grow=1"), new FlexConstraints().flexGrow(1));
        panel.add(DemoPanels.colorPanel(new Color(0xFF9800), "grow=2"), new FlexConstraints().flexGrow(2));
        return panel;
    }

    /** FlexBox: WRAP + JustifyContent */
    public static JComponent flexWrap() {
        JPanel panel = new JPanel(new FlexBoxLayout(FlexDirection.ROW)
                .setFlexWrap(FlexWrap.WRAP)
                .setJustifyContent(JustifyContent.SPACE_AROUND)
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
        JPanel panel = new JPanel(new FlexBoxLayout(FlexDirection.ROW)
                .setAlignItems(AlignItems.CENTER)
                .setMainAxisGap(8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel a = DemoPanels.colorPanel(new Color(0x4CAF50), "STRETCH");
        a.setPreferredSize(new Dimension(100, 40));
        panel.add(a, new FlexConstraints().flexGrow(1)
                .alignSelf(AlignSelf.STRETCH));

        JPanel b = DemoPanels.colorPanel(new Color(0x2196F3), "CENTER");
        b.setPreferredSize(new Dimension(100, 60));
        panel.add(b, new FlexConstraints().flexGrow(1));

        JPanel c = DemoPanels.colorPanel(new Color(0xFF9800), "FLEX_END");
        c.setPreferredSize(new Dimension(100, 30));
        panel.add(c, new FlexConstraints().flexGrow(1)
                .alignSelf(AlignSelf.FLEX_END));

        JPanel d = DemoPanels.colorPanel(new Color(0xE91E63), "FLEX_START");
        d.setPreferredSize(new Dimension(100, 50));
        panel.add(d, new FlexConstraints().flexGrow(1)
                .alignSelf(AlignSelf.FLEX_START));

        return panel;
    }
}

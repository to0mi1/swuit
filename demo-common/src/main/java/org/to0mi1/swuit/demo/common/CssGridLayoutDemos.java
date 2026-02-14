package org.to0mi1.swuit.demo.common;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.to0mi1.swuit.layout.grid.CssAlignSelf;
import org.to0mi1.swuit.layout.grid.CssGridLayout;
import org.to0mi1.swuit.layout.grid.CssGridConstraints;
import org.to0mi1.swuit.layout.grid.CssJustifySelf;
import org.to0mi1.swuit.layout.grid.CssTrackSize;

/**
 * CssGridLayout デモパネル生成ユーティリティ。
 */
public final class CssGridLayoutDemos {

    private CssGridLayoutDemos() {
    }

    /** Grid: fixed + fr 混在の基本配置 */
    public static JComponent gridBasic() {
        CssGridLayout layout = new CssGridLayout()
                .setColumnTemplate(CssTrackSize.fixed(100), CssTrackSize.fr(1), CssTrackSize.fr(2))
                .setRowTemplate(CssTrackSize.fixed(50), CssTrackSize.fr(1))
                .setColumnGap(4).setRowGap(4);
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(DemoPanels.colorPanel(new Color(0x4CAF50), "固定 100px"),
                new CssGridConstraints().column(0).row(0));
        panel.add(DemoPanels.colorPanel(new Color(0x2196F3), "fr=1"),
                new CssGridConstraints().column(1).row(0));
        panel.add(DemoPanels.colorPanel(new Color(0xFF9800), "fr=2"),
                new CssGridConstraints().column(2).row(0));
        panel.add(DemoPanels.colorPanel(new Color(0x9C27B0), "(0,1)"),
                new CssGridConstraints().column(0).row(1));
        panel.add(DemoPanels.colorPanel(new Color(0xE91E63), "(1,1)"),
                new CssGridConstraints().column(1).row(1));
        panel.add(DemoPanels.colorPanel(new Color(0x607D8B), "(2,1)"),
                new CssGridConstraints().column(2).row(1));
        return panel;
    }

    /** Grid: ヘッダー/サイドバー/コンテンツ/フッターのスパンレイアウト */
    public static JComponent gridSpan() {
        CssGridLayout layout = new CssGridLayout()
                .setColumnTemplate(CssTrackSize.fixed(120), CssTrackSize.fr(1), CssTrackSize.fr(1))
                .setRowTemplate(CssTrackSize.fixed(50), CssTrackSize.fr(1), CssTrackSize.fixed(40))
                .setColumnGap(4).setRowGap(4);
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(DemoPanels.colorPanel(new Color(0x2196F3), "Header (span 3)"),
                new CssGridConstraints().column(0).row(0).columnSpan(3));
        panel.add(DemoPanels.colorPanel(new Color(0x4CAF50), "Sidebar"),
                new CssGridConstraints().column(0).row(1));
        panel.add(DemoPanels.colorPanel(new Color(0xFFC107), "Content (span 2)"),
                new CssGridConstraints().column(1).row(1).columnSpan(2));
        panel.add(DemoPanels.colorPanel(new Color(0x607D8B), "Footer (span 3)"),
                new CssGridConstraints().column(0).row(2).columnSpan(3));
        return panel;
    }

    /** Grid: アライメント比較 */
    public static JComponent gridAlign() {
        CssGridLayout layout = new CssGridLayout()
                .setColumnTemplate(CssTrackSize.fr(1), CssTrackSize.fr(1))
                .setRowTemplate(CssTrackSize.fr(1), CssTrackSize.fr(1))
                .setColumnGap(8).setRowGap(8);
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel a = DemoPanels.colorPanel(new Color(0x4CAF50), "STRETCH");
        a.setPreferredSize(new Dimension(100, 40));
        panel.add(a, new CssGridConstraints().column(0).row(0));

        JPanel b = DemoPanels.colorPanel(new Color(0x2196F3), "CENTER");
        b.setPreferredSize(new Dimension(100, 40));
        panel.add(b, new CssGridConstraints().column(1).row(0)
                .justifySelf(CssJustifySelf.CENTER)
                .alignSelf(CssAlignSelf.CENTER));

        JPanel c = DemoPanels.colorPanel(new Color(0xFF9800), "START");
        c.setPreferredSize(new Dimension(100, 40));
        panel.add(c, new CssGridConstraints().column(0).row(1)
                .justifySelf(CssJustifySelf.START)
                .alignSelf(CssAlignSelf.START));

        JPanel d = DemoPanels.colorPanel(new Color(0xE91E63), "END");
        d.setPreferredSize(new Dimension(100, 40));
        panel.add(d, new CssGridConstraints().column(1).row(1)
                .justifySelf(CssJustifySelf.END)
                .alignSelf(CssAlignSelf.END));

        return panel;
    }
}

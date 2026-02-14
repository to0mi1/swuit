package org.to0mi1.swuit.demo.common;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.to0mi1.swuit.layout.grid.AlignSelf;
import org.to0mi1.swuit.layout.grid.GridBoxLayout;
import org.to0mi1.swuit.layout.grid.GridConstraints;
import org.to0mi1.swuit.layout.grid.JustifySelf;
import org.to0mi1.swuit.layout.grid.TrackSize;

/**
 * GridBoxLayout デモパネル生成ユーティリティ。
 */
public final class GridBoxLayoutDemos {

    private GridBoxLayoutDemos() {
    }

    /** Grid: fixed + fr 混在の基本配置 */
    public static JComponent gridBasic() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fr(1), TrackSize.fr(2))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fr(1))
                .setColumnGap(4).setRowGap(4);
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(DemoPanels.colorPanel(new Color(0x4CAF50), "固定 100px"),
                new GridConstraints().column(0).row(0));
        panel.add(DemoPanels.colorPanel(new Color(0x2196F3), "fr=1"),
                new GridConstraints().column(1).row(0));
        panel.add(DemoPanels.colorPanel(new Color(0xFF9800), "fr=2"),
                new GridConstraints().column(2).row(0));
        panel.add(DemoPanels.colorPanel(new Color(0x9C27B0), "(0,1)"),
                new GridConstraints().column(0).row(1));
        panel.add(DemoPanels.colorPanel(new Color(0xE91E63), "(1,1)"),
                new GridConstraints().column(1).row(1));
        panel.add(DemoPanels.colorPanel(new Color(0x607D8B), "(2,1)"),
                new GridConstraints().column(2).row(1));
        return panel;
    }

    /** Grid: ヘッダー/サイドバー/コンテンツ/フッターのスパンレイアウト */
    public static JComponent gridSpan() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(120), TrackSize.fr(1), TrackSize.fr(1))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fr(1), TrackSize.fixed(40))
                .setColumnGap(4).setRowGap(4);
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(DemoPanels.colorPanel(new Color(0x2196F3), "Header (span 3)"),
                new GridConstraints().column(0).row(0).columnSpan(3));
        panel.add(DemoPanels.colorPanel(new Color(0x4CAF50), "Sidebar"),
                new GridConstraints().column(0).row(1));
        panel.add(DemoPanels.colorPanel(new Color(0xFFC107), "Content (span 2)"),
                new GridConstraints().column(1).row(1).columnSpan(2));
        panel.add(DemoPanels.colorPanel(new Color(0x607D8B), "Footer (span 3)"),
                new GridConstraints().column(0).row(2).columnSpan(3));
        return panel;
    }

    /** Grid: アライメント比較 */
    public static JComponent gridAlign() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fr(1), TrackSize.fr(1))
                .setRowTemplate(TrackSize.fr(1), TrackSize.fr(1))
                .setColumnGap(8).setRowGap(8);
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel a = DemoPanels.colorPanel(new Color(0x4CAF50), "STRETCH");
        a.setPreferredSize(new Dimension(100, 40));
        panel.add(a, new GridConstraints().column(0).row(0));

        JPanel b = DemoPanels.colorPanel(new Color(0x2196F3), "CENTER");
        b.setPreferredSize(new Dimension(100, 40));
        panel.add(b, new GridConstraints().column(1).row(0)
                .justifySelf(JustifySelf.CENTER)
                .alignSelf(AlignSelf.CENTER));

        JPanel c = DemoPanels.colorPanel(new Color(0xFF9800), "START");
        c.setPreferredSize(new Dimension(100, 40));
        panel.add(c, new GridConstraints().column(0).row(1)
                .justifySelf(JustifySelf.START)
                .alignSelf(AlignSelf.START));

        JPanel d = DemoPanels.colorPanel(new Color(0xE91E63), "END");
        d.setPreferredSize(new Dimension(100, 40));
        panel.add(d, new GridConstraints().column(1).row(1)
                .justifySelf(JustifySelf.END)
                .alignSelf(AlignSelf.END));

        return panel;
    }
}

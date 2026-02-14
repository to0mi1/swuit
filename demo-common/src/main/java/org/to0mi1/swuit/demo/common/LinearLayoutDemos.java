package org.to0mi1.swuit.demo.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.to0mi1.swuit.layout.Gravity;
import org.to0mi1.swuit.layout.Orientation;
import org.to0mi1.swuit.layout.linear.LinearConstraints;
import org.to0mi1.swuit.layout.linear.LinearLayout;

/**
 * LinearLayout デモパネル生成ユーティリティ。
 */
public final class LinearLayoutDemos {

    private LinearLayoutDemos() {
    }

    /** 3ボタン垂直配置 */
    public static JComponent linearVertical() {
        JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JButton("Button 1"));
        panel.add(new JButton("Button 2"));
        panel.add(new JButton("Button 3"));
        return panel;
    }

    /** 3ボタン水平配置 */
    public static JComponent linearHorizontal() {
        JPanel panel = new JPanel(new LinearLayout(Orientation.HORIZONTAL, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JButton("Button 1"));
        panel.add(new JButton("Button 2"));
        panel.add(new JButton("Button 3"));
        return panel;
    }

    /** 1:2:1 カラーパネル */
    public static JComponent linearWeight() {
        JPanel panel = new JPanel(new LinearLayout(Orientation.HORIZONTAL, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(DemoPanels.colorPanel(new Color(0x4CAF50), "1"), new LinearConstraints(1));
        panel.add(DemoPanels.colorPanel(new Color(0x2196F3), "2"), new LinearConstraints(2));
        panel.add(DemoPanels.colorPanel(new Color(0xFF9800), "1"), new LinearConstraints(1));
        return panel;
    }

    /** LEFT/CENTER/RIGHT 配置 */
    public static JComponent linearGravity() {
        JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton left = new JButton("LEFT");
        JButton center = new JButton("CENTER");
        JButton right = new JButton("RIGHT");

        panel.add(left, new LinearConstraints(0, Gravity.LEFT));
        panel.add(center, new LinearConstraints(0, Gravity.CENTER_HORIZONTAL));
        panel.add(right, new LinearConstraints(0, Gravity.RIGHT));
        return panel;
    }

    /** 異なるマージン */
    public static JComponent linearMargin() {
        JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel a = DemoPanels.colorPanel(new Color(0xE91E63), "margin: 0");
        JPanel b = DemoPanels.colorPanel(new Color(0x9C27B0), "margin: 10");
        JPanel c = DemoPanels.colorPanel(new Color(0x3F51B5), "margin: 20");

        a.setPreferredSize(new Dimension(200, 50));
        b.setPreferredSize(new Dimension(200, 50));
        c.setPreferredSize(new Dimension(200, 50));

        panel.add(a, new LinearConstraints(0, Gravity.NONE, new Insets(0, 0, 0, 0)));
        panel.add(b, new LinearConstraints(0, Gravity.NONE, new Insets(10, 10, 10, 10)));
        panel.add(c, new LinearConstraints(0, Gravity.NONE, new Insets(20, 20, 20, 20)));
        return panel;
    }

    /** ツールバー+コンテンツのネスト */
    public static JComponent linearNested() {
        JPanel outer = new JPanel(new LinearLayout(Orientation.VERTICAL, 4));
        outer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ツールバー風
        JPanel toolbar = new JPanel(new LinearLayout(Orientation.HORIZONTAL, 4));
        toolbar.add(new JButton("File"));
        toolbar.add(new JButton("Edit"));
        toolbar.add(new JButton("View"));
        outer.add(toolbar);

        // コンテンツ
        JPanel content = new JPanel(new LinearLayout(Orientation.HORIZONTAL, 4));
        content.add(DemoPanels.colorPanel(new Color(0x607D8B), "Sidebar"), new LinearConstraints(1));
        content.add(DemoPanels.colorPanel(new Color(0xFFC107), "Content"), new LinearConstraints(3));
        outer.add(content, new LinearConstraints(1));

        return outer;
    }
}

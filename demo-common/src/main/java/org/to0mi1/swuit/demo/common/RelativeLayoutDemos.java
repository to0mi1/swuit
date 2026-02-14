package org.to0mi1.swuit.demo.common;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.to0mi1.swuit.layout.relative.RelativeConstraints;
import org.to0mi1.swuit.layout.relative.RelativeLayout;

/**
 * RelativeLayout デモパネル生成ユーティリティ。
 */
public final class RelativeLayoutDemos {

    private RelativeLayoutDemos() {
    }

    /** 親アライメントの基本（4隅+中央） */
    public static JComponent relativeBasic() {
        JPanel panel = new JPanel(new RelativeLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton topLeft = new JButton("Top-Left");
        JButton topRight = new JButton("Top-Right");
        JButton bottomLeft = new JButton("Bottom-Left");
        JButton bottomRight = new JButton("Bottom-Right");
        JLabel center = new JLabel("Center");

        panel.add(topLeft, new RelativeConstraints()
                .alignParentTop().alignParentLeft());
        panel.add(topRight, new RelativeConstraints()
                .alignParentTop().alignParentRight());
        panel.add(bottomLeft, new RelativeConstraints()
                .alignParentBottom().alignParentLeft());
        panel.add(bottomRight, new RelativeConstraints()
                .alignParentBottom().alignParentRight());
        panel.add(center, new RelativeConstraints()
                .centerInParent());

        return panel;
    }

    /** フォームレイアウト */
    public static JComponent relativeForm() {
        JPanel panel = new JPanel(new RelativeLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setPreferredSize(new Dimension(80, 28));
        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(200, 28));

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setPreferredSize(new Dimension(80, 28));
        JTextField emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(200, 28));

        panel.add(nameLabel, new RelativeConstraints()
                .alignParentTop().alignParentLeft());
        panel.add(nameField, new RelativeConstraints()
                .alignParentTop().rightOf(nameLabel).alignParentRight());
        panel.add(emailLabel, new RelativeConstraints()
                .below(nameLabel).alignParentLeft()
                .margin(8, 0, 0, 0));
        panel.add(emailField, new RelativeConstraints()
                .below(nameField).rightOf(emailLabel).alignParentRight()
                .margin(8, 0, 0, 0));

        return panel;
    }

    /** ヘッダー/サイドバー/コンテンツ/フッター */
    public static JComponent relativeComplex() {
        JPanel panel = new JPanel(new RelativeLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = DemoPanels.colorPanel(new Color(0x2196F3), "Header");
        header.setPreferredSize(new Dimension(0, 50));
        JPanel footer = DemoPanels.colorPanel(new Color(0x607D8B), "Footer");
        footer.setPreferredSize(new Dimension(0, 40));
        JPanel sidebar = DemoPanels.colorPanel(new Color(0x4CAF50), "Sidebar");
        sidebar.setPreferredSize(new Dimension(120, 0));
        JPanel content = DemoPanels.colorPanel(new Color(0xFFC107), "Content");
        content.setPreferredSize(new Dimension(0, 0));

        panel.add(header, new RelativeConstraints()
                .alignParentTop().alignParentLeft().alignParentRight());
        panel.add(footer, new RelativeConstraints()
                .alignParentBottom().alignParentLeft().alignParentRight());
        panel.add(sidebar, new RelativeConstraints()
                .below(header).alignParentLeft().above(footer)
                .margin(4, 0, 4, 0));
        panel.add(content, new RelativeConstraints()
                .below(header).rightOf(sidebar).alignParentRight().above(footer)
                .margin(4, 4, 4, 0));

        return panel;
    }
}

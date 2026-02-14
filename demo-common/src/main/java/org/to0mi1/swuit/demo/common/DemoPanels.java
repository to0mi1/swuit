package org.to0mi1.swuit.demo.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.to0mi1.swuit.layout.Gravity;
import org.to0mi1.swuit.layout.Orientation;
import org.to0mi1.swuit.layout.linear.LinearConstraints;
import org.to0mi1.swuit.layout.linear.LinearLayout;
import org.to0mi1.swuit.layout.relative.RelativeConstraints;
import org.to0mi1.swuit.layout.relative.RelativeLayout;

/**
 * デモ用パネル生成ユーティリティ。
 * <p>
 * 各メソッドが1つのレイアウトコンセプトを示す自己完結型のデモパネルを生成する。
 */
public final class DemoPanels {

    private DemoPanels() {
    }

    // === LinearLayout デモ ===

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
        panel.add(colorPanel(new Color(0x4CAF50), "1"), new LinearConstraints(1));
        panel.add(colorPanel(new Color(0x2196F3), "2"), new LinearConstraints(2));
        panel.add(colorPanel(new Color(0xFF9800), "1"), new LinearConstraints(1));
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

        JPanel a = colorPanel(new Color(0xE91E63), "margin: 0");
        JPanel b = colorPanel(new Color(0x9C27B0), "margin: 10");
        JPanel c = colorPanel(new Color(0x3F51B5), "margin: 20");

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
        content.add(colorPanel(new Color(0x607D8B), "Sidebar"), new LinearConstraints(1));
        content.add(colorPanel(new Color(0xFFC107), "Content"), new LinearConstraints(3));
        outer.add(content, new LinearConstraints(1));

        return outer;
    }

    // === RelativeLayout デモ ===

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

        JPanel header = colorPanel(new Color(0x2196F3), "Header");
        header.setPreferredSize(new Dimension(0, 50));
        JPanel footer = colorPanel(new Color(0x607D8B), "Footer");
        footer.setPreferredSize(new Dimension(0, 40));
        JPanel sidebar = colorPanel(new Color(0x4CAF50), "Sidebar");
        sidebar.setPreferredSize(new Dimension(120, 0));
        JPanel content = colorPanel(new Color(0xFFC107), "Content");
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

    // === ユーティリティ ===

    static JPanel colorPanel(Color color, String text) {
        JPanel panel = new JPanel(new LinearLayout(Orientation.HORIZONTAL, 0, Gravity.CENTER));
        panel.setBackground(color);
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        panel.add(label);
        return panel;
    }

    // === タブペイン構築 ===

    /** 全デモを含むタブペインを生成する。 */
    public static JTabbedPane createTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("基本 (Vertical)", linearVertical());
        tabs.addTab("基本 (Horizontal)", linearHorizontal());
        tabs.addTab("Weight", linearWeight());
        tabs.addTab("Gravity", linearGravity());
        tabs.addTab("Margin", linearMargin());
        tabs.addTab("ネスト", linearNested());
        tabs.addTab("Relative: 基本", relativeBasic());
        tabs.addTab("Relative: フォーム", relativeForm());
        tabs.addTab("Relative: 複合", relativeComplex());
        return tabs;
    }
}

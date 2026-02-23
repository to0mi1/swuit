package org.to0mi1.swuit.layout.relative;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RelativeLayoutTest {

    // === ヘルパー ===

    private static JPanel createContainer(RelativeLayout layout, int width, int height) {
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

    // === 基本・デフォルト ===

    @Test
    void noRules_placedAtTopLeft() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a);
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 100, 50), boundsOf(a));
    }

    @Test
    void noRules_withMargin() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().margin(10, 20, 10, 20));
        panel.doLayout();

        assertEquals(new Rectangle(20, 10, 100, 50), boundsOf(a));
    }

    @Test
    void invisible_skipped() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        a.setVisible(false);
        Component b = fixedSize(80, 40);
        panel.add(a);
        panel.add(b, new RelativeConstraints().rightOf(a));
        panel.doLayout();

        // a は非表示、rightOf(a) のアンカーが無視される → b はデフォルト位置
        assertEquals(new Rectangle(0, 0, 80, 40), boundsOf(b));
    }

    // === 親アライメント ===

    @Test
    void alignParentLeft() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().alignParentLeft());
        panel.doLayout();

        assertEquals(0, boundsOf(a).x);
        assertEquals(100, boundsOf(a).width);
    }

    @Test
    void alignParentRight() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().alignParentRight());
        panel.doLayout();

        // right = 400, left = 400 - 100 = 300
        assertEquals(300, boundsOf(a).x);
        assertEquals(100, boundsOf(a).width);
    }

    @Test
    void alignParentTop() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().alignParentTop());
        panel.doLayout();

        assertEquals(0, boundsOf(a).y);
        assertEquals(50, boundsOf(a).height);
    }

    @Test
    void alignParentBottom() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().alignParentBottom());
        panel.doLayout();

        assertEquals(250, boundsOf(a).y);
        assertEquals(50, boundsOf(a).height);
    }

    @Test
    void alignParentLeftAndRight_stretches() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().alignParentLeft().alignParentRight());
        panel.doLayout();

        assertEquals(0, boundsOf(a).x);
        assertEquals(400, boundsOf(a).width);
    }

    // === 相対位置 ===

    @Test
    void rightOf() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        panel.add(a);
        panel.add(b, new RelativeConstraints().rightOf(a));
        panel.doLayout();

        assertEquals(0, boundsOf(a).x);
        // b.left = a.right(100) + a.margin.right(0) + b.margin.left(0) = 100
        assertEquals(100, boundsOf(b).x);
    }

    @Test
    void leftOf() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        panel.add(a, new RelativeConstraints().alignParentRight());
        panel.add(b, new RelativeConstraints().leftOf(a));
        panel.doLayout();

        // a: right=400, left=300
        // b.right = a.left(300) - a.margin.left(0) - b.margin.right(0) = 300
        // b.left = 300 - 80 = 220
        assertEquals(220, boundsOf(b).x);
        assertEquals(80, boundsOf(b).width);
    }

    @Test
    void below() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        panel.add(a);
        panel.add(b, new RelativeConstraints().below(a));
        panel.doLayout();

        assertEquals(0, boundsOf(a).y);
        assertEquals(50, boundsOf(b).y);
    }

    @Test
    void above() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        panel.add(a, new RelativeConstraints().alignParentBottom());
        panel.add(b, new RelativeConstraints().above(a));
        panel.doLayout();

        // a: bottom=300, top=250
        // b.bottom = a.top(250) - a.margin.top(0) - b.margin.bottom(0) = 250
        // b.top = 250 - 40 = 210
        assertEquals(210, boundsOf(b).y);
        assertEquals(40, boundsOf(b).height);
    }

    // === 兄弟アライメント ===

    @Test
    void alignLeft_withSibling() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        panel.add(a, new RelativeConstraints().margin(0, 30, 0, 0));
        panel.add(b, new RelativeConstraints().alignLeft(a).below(a));
        panel.doLayout();

        // a.left = 0 + 30 = 30
        // b.left = a.left(30) + b.margin.left(0) = 30
        assertEquals(30, boundsOf(a).x);
        assertEquals(30, boundsOf(b).x);
    }

    @Test
    void alignRight_withSibling() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        panel.add(a, new RelativeConstraints().alignParentLeft());
        panel.add(b, new RelativeConstraints().alignRight(a).below(a));
        panel.doLayout();

        // a: left=0, right=100
        // b.right = a.right(100) - b.margin.right(0) = 100
        // b.left = 100 - 80 = 20
        assertEquals(20, boundsOf(b).x);
        assertEquals(80, boundsOf(b).width);
    }

    @Test
    void alignTop_withSibling() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        panel.add(a, new RelativeConstraints().margin(20, 0, 0, 0));
        panel.add(b, new RelativeConstraints().alignTop(a).rightOf(a));
        panel.doLayout();

        // a.top = 0 + 20 = 20
        // b.top = a.top(20) + b.margin.top(0) = 20
        assertEquals(20, boundsOf(a).y);
        assertEquals(20, boundsOf(b).y);
    }

    @Test
    void alignBottom_withSibling() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        panel.add(a, new RelativeConstraints().alignParentTop());
        panel.add(b, new RelativeConstraints().alignBottom(a).rightOf(a));
        panel.doLayout();

        // a: top=0, bottom=50
        // b.bottom = a.bottom(50) - b.margin.bottom(0) = 50
        // b.top = 50 - 40 = 10
        assertEquals(10, boundsOf(b).y);
        assertEquals(40, boundsOf(b).height);
    }

    // === センタリング ===

    @Test
    void centerInParent() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().centerInParent());
        panel.doLayout();

        assertEquals(150, boundsOf(a).x);
        assertEquals(125, boundsOf(a).y);
        assertEquals(100, boundsOf(a).width);
        assertEquals(50, boundsOf(a).height);
    }

    @Test
    void centerHorizontal() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().centerHorizontal());
        panel.doLayout();

        assertEquals(150, boundsOf(a).x);
        assertEquals(0, boundsOf(a).y);
    }

    @Test
    void centerVertical() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().centerVertical());
        panel.doLayout();

        assertEquals(0, boundsOf(a).x);
        assertEquals(125, boundsOf(a).y);
    }

    // === 複合ルール ===

    @Test
    void below_alignLeft_alignRight() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component header = fixedSize(400, 50);
        Component content = fixedSize(100, 100);
        panel.add(header, new RelativeConstraints().alignParentLeft().alignParentRight().alignParentTop());
        panel.add(content, new RelativeConstraints().below(header).alignParentLeft().alignParentRight());
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 400, 50), boundsOf(header));
        assertEquals(new Rectangle(0, 50, 400, 100), boundsOf(content));
    }

    @Test
    void chain_A_B_C() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        Component c = fixedSize(60, 30);
        panel.add(a);
        panel.add(b, new RelativeConstraints().rightOf(a));
        panel.add(c, new RelativeConstraints().rightOf(b));
        panel.doLayout();

        assertEquals(0, boundsOf(a).x);
        assertEquals(100, boundsOf(b).x);
        assertEquals(180, boundsOf(c).x);
    }

    @Test
    void headerContentFooter() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component header = fixedSize(400, 50);
        Component footer = fixedSize(400, 40);
        Component content = fixedSize(100, 100);
        panel.add(header, new RelativeConstraints()
                .alignParentTop().alignParentLeft().alignParentRight());
        panel.add(footer, new RelativeConstraints()
                .alignParentBottom().alignParentLeft().alignParentRight());
        panel.add(content, new RelativeConstraints()
                .below(header).above(footer).alignParentLeft().alignParentRight());
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 400, 50), boundsOf(header));
        assertEquals(new Rectangle(0, 260, 400, 40), boundsOf(footer));
        // content: top=50, bottom=260 → height=210
        assertEquals(new Rectangle(0, 50, 400, 210), boundsOf(content));
    }

    // === エラーケース ===

    @Test
    void circularDependency_throws() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        panel.add(a, new RelativeConstraints().rightOf(b));
        panel.add(b, new RelativeConstraints().rightOf(a));

        assertThrows(IllegalStateException.class, panel::doLayout);
    }

    @Test
    void invalidConstraints_throws() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        assertThrows(IllegalArgumentException.class, () -> panel.add(a, "invalid"));
    }

    // === サイズ計算 ===

    @Test
    void preferredSize_basic() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        Component b = fixedSize(80, 40);
        panel.add(a);
        panel.add(b, new RelativeConstraints().rightOf(a));
        // a: left=0, right=100
        // b: left=100, right=180

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(180, pref.width);
        assertEquals(50, pref.height);
    }

    @Test
    void preferredSize_withMarginAndInsets() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = new JPanel(layout) {
            @Override
            public Insets getInsets() {
                return new Insets(10, 20, 10, 20);
            }
        };
        panel.setSize(400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().margin(5, 10, 5, 10));

        Dimension pref = layout.preferredLayoutSize(panel);
        // width: insets.left(20) + margin.left(10) + 100 + margin.right(10) + insets.right(20) = 160
        // height: insets.top(10) + margin.top(5) + 50 + margin.bottom(5) + insets.bottom(10) = 80
        assertEquals(160, pref.width);
        assertEquals(80, pref.height);
    }

    @Test
    void preferredSize_emptyContainer() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(0, pref.width);
        assertEquals(0, pref.height);
    }

    // === Insets ===

    @Test
    void containerInsets_actAsPadding() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = new JPanel(layout) {
            @Override
            public Insets getInsets() {
                return new Insets(10, 20, 10, 20);
            }
        };
        panel.setSize(400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().alignParentLeft().alignParentTop());
        panel.doLayout();

        assertEquals(new Rectangle(20, 10, 100, 50), boundsOf(a));
    }

    // === setConstraints / getConstraints ===

    @Test
    void setConstraints_updatesLayout() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints().alignParentLeft().alignParentTop());
        panel.doLayout();
        assertEquals(new Rectangle(0, 0, 100, 50), boundsOf(a));

        // 制約を変更して再レイアウト
        layout.setConstraints(a, new RelativeConstraints()
                .alignParentRight().alignParentBottom());
        panel.doLayout();
        assertEquals(new Rectangle(300, 250, 100, 50), boundsOf(a));
    }

    @Test
    void getConstraints_returnsClone() {
        RelativeLayout layout = new RelativeLayout();
        JPanel panel = createContainer(layout, 400, 300);
        Component a = fixedSize(100, 50);
        panel.add(a, new RelativeConstraints()
                .alignParentLeft().alignParentTop().margin(10, 20, 0, 0));
        panel.doLayout();
        assertEquals(new Rectangle(20, 10, 100, 50), boundsOf(a));

        // 取得した制約を変更しても内部状態に影響しない
        RelativeConstraints got = layout.getConstraints(a);
        assertNotNull(got);
        got.margin(99, 99, 99, 99);
        panel.doLayout();
        assertEquals(new Rectangle(20, 10, 100, 50), boundsOf(a));
    }

    @Test
    void setConstraints_unknownComponent_throws() {
        RelativeLayout layout = new RelativeLayout();
        createContainer(layout, 400, 300);
        Component unknown = fixedSize(100, 50);
        assertThrows(IllegalArgumentException.class,
                () -> layout.setConstraints(unknown, new RelativeConstraints()));
    }

    @Test
    void getConstraints_unknownComponent_returnsNull() {
        RelativeLayout layout = new RelativeLayout();
        createContainer(layout, 400, 300);
        Component unknown = fixedSize(100, 50);
        assertNull(layout.getConstraints(unknown));
    }
}

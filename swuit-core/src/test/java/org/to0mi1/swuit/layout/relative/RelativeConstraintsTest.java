package org.to0mi1.swuit.layout.relative;

import java.awt.Insets;

import javax.swing.JLabel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RelativeConstraintsTest {

    @Test
    void defaultConstructor_setsDefaults() {
        RelativeConstraints rc = new RelativeConstraints();
        assertEquals(new Insets(0, 0, 0, 0), rc.margin);
        for (int i = 0; i < RelativeConstraints.RULE_COUNT; i++) {
            assertNull(rc.getRule(i));
        }
    }

    @Test
    void constructorWithMargin_setsMargin() {
        Insets original = new Insets(5, 10, 15, 20);
        RelativeConstraints rc = new RelativeConstraints(original);
        assertEquals(new Insets(5, 10, 15, 20), rc.margin);

        // コピーされているので元を変更しても影響なし
        original.top = 99;
        assertEquals(5, rc.margin.top);
    }

    @Test
    void fluentMethods_setRules() {
        JLabel header = new JLabel("header");
        RelativeConstraints rc = new RelativeConstraints()
                .below(header)
                .alignParentLeft();

        assertTrue(rc.hasAnchor(RelativeConstraints.BELOW));
        assertSame(header, rc.getRule(RelativeConstraints.BELOW));
        assertTrue(rc.isEnabled(RelativeConstraints.ALIGN_PARENT_LEFT));
    }

    @Test
    void fluentMethods_returnSelf() {
        JLabel anchor = new JLabel();
        RelativeConstraints rc = new RelativeConstraints();

        assertSame(rc, rc.leftOf(anchor));
        assertSame(rc, rc.rightOf(anchor));
        assertSame(rc, rc.above(anchor));
        assertSame(rc, rc.below(anchor));
        assertSame(rc, rc.alignLeft(anchor));
        assertSame(rc, rc.alignRight(anchor));
        assertSame(rc, rc.alignTop(anchor));
        assertSame(rc, rc.alignBottom(anchor));
        assertSame(rc, rc.alignParentLeft());
        assertSame(rc, rc.alignParentRight());
        assertSame(rc, rc.alignParentTop());
        assertSame(rc, rc.alignParentBottom());
        assertSame(rc, rc.centerInParent());
        assertSame(rc, rc.centerHorizontal());
        assertSame(rc, rc.centerVertical());
        assertSame(rc, rc.margin(1, 2, 3, 4));
    }

    @Test
    void clone_isIndependentCopy() {
        JLabel anchor = new JLabel();
        RelativeConstraints original = new RelativeConstraints()
                .below(anchor)
                .alignParentLeft()
                .margin(10, 10, 10, 10);

        RelativeConstraints cloned = original.clone();

        // 同じ値
        assertSame(anchor, cloned.getRule(RelativeConstraints.BELOW));
        assertTrue(cloned.isEnabled(RelativeConstraints.ALIGN_PARENT_LEFT));
        assertEquals(new Insets(10, 10, 10, 10), cloned.margin);

        // rules 配列が独立
        cloned.alignParentRight();
        assertFalse(original.isEnabled(RelativeConstraints.ALIGN_PARENT_RIGHT));

        // margin が独立
        cloned.margin.top = 99;
        assertEquals(10, original.margin.top);
    }

    @Test
    void clone_sharesComponentReferences() {
        JLabel anchor = new JLabel();
        RelativeConstraints original = new RelativeConstraints().rightOf(anchor);
        RelativeConstraints cloned = original.clone();

        // Component 参照は共有
        assertSame(original.getRule(RelativeConstraints.RIGHT_OF),
                   cloned.getRule(RelativeConstraints.RIGHT_OF));
    }

    @Test
    void margin_fluentSetter() {
        RelativeConstraints rc = new RelativeConstraints().margin(5, 10, 5, 10);
        assertEquals(new Insets(5, 10, 5, 10), rc.margin);
    }
}

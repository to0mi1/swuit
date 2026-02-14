package org.to0mi1.swuit.layout.flex;

import java.awt.Insets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlexConstraintsTest {

    @Test
    void defaults() {
        FlexConstraints fc = new FlexConstraints();
        assertEquals(0, fc.getFlexGrow());
        assertEquals(1, fc.getFlexShrink());
        assertEquals(-1, fc.getFlexBasisPercent());
        assertEquals(AlignSelf.AUTO, fc.getAlignSelf());
        assertEquals(0, fc.getOrder());
        assertEquals(-1, fc.getMinWidth());
        assertEquals(-1, fc.getMinHeight());
        assertEquals(-1, fc.getMaxWidth());
        assertEquals(-1, fc.getMaxHeight());
        assertEquals(new Insets(0, 0, 0, 0), fc.getMargin());
    }

    @Test
    void fluentApi() {
        FlexConstraints fc = new FlexConstraints()
                .flexGrow(2)
                .flexShrink(0.5f)
                .flexBasisPercent(50)
                .alignSelf(AlignSelf.CENTER)
                .order(3)
                .minWidth(10)
                .minHeight(20)
                .maxWidth(300)
                .maxHeight(400)
                .margin(5, 10, 15, 20);

        assertEquals(2, fc.getFlexGrow());
        assertEquals(0.5f, fc.getFlexShrink());
        assertEquals(50, fc.getFlexBasisPercent());
        assertEquals(AlignSelf.CENTER, fc.getAlignSelf());
        assertEquals(3, fc.getOrder());
        assertEquals(10, fc.getMinWidth());
        assertEquals(20, fc.getMinHeight());
        assertEquals(300, fc.getMaxWidth());
        assertEquals(400, fc.getMaxHeight());
        assertEquals(new Insets(5, 10, 15, 20), fc.getMargin());
    }

    @Test
    void clone_isIndependentCopy() {
        FlexConstraints original = new FlexConstraints()
                .flexGrow(1)
                .margin(5, 10, 15, 20);

        FlexConstraints cloned = original.clone();

        // 値が同じ
        assertEquals(1, cloned.getFlexGrow());
        assertEquals(new Insets(5, 10, 15, 20), cloned.getMargin());

        // 変更しても元に影響しない
        cloned.flexGrow(99).margin(0, 0, 0, 0);
        assertEquals(1, original.getFlexGrow());
        assertEquals(new Insets(5, 10, 15, 20), original.getMargin());
    }
}

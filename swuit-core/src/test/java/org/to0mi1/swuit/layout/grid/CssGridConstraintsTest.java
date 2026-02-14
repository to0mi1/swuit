package org.to0mi1.swuit.layout.grid;

import java.awt.Insets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CssGridConstraintsTest {

    @Test
    void defaultValues() {
        CssGridConstraints gc = new CssGridConstraints();
        assertEquals(-1, gc.getColumn());
        assertEquals(-1, gc.getRow());
        assertEquals(1, gc.getColumnSpan());
        assertEquals(1, gc.getRowSpan());
        assertEquals(CssJustifySelf.AUTO, gc.getCssJustifySelf());
        assertEquals(CssAlignSelf.AUTO, gc.getCssAlignSelf());
        assertEquals(new Insets(0, 0, 0, 0), gc.getMargin());
    }

    @Test
    void fluentApi() {
        CssGridConstraints gc = new CssGridConstraints()
                .column(2).row(3).columnSpan(4).rowSpan(2)
                .justifySelf(CssJustifySelf.CENTER).alignSelf(CssAlignSelf.END)
                .margin(5, 10, 15, 20);

        assertEquals(2, gc.getColumn());
        assertEquals(3, gc.getRow());
        assertEquals(4, gc.getColumnSpan());
        assertEquals(2, gc.getRowSpan());
        assertEquals(CssJustifySelf.CENTER, gc.getCssJustifySelf());
        assertEquals(CssAlignSelf.END, gc.getCssAlignSelf());
        assertEquals(new Insets(5, 10, 15, 20), gc.getMargin());
    }

    @Test
    void clone_independence() {
        CssGridConstraints original = new CssGridConstraints()
                .column(1).row(2).columnSpan(3).rowSpan(4)
                .justifySelf(CssJustifySelf.START).alignSelf(CssAlignSelf.STRETCH)
                .margin(10, 20, 30, 40);

        CssGridConstraints cloned = original.clone();

        assertEquals(1, cloned.getColumn());
        assertEquals(2, cloned.getRow());
        assertEquals(3, cloned.getColumnSpan());
        assertEquals(4, cloned.getRowSpan());
        assertEquals(CssJustifySelf.START, cloned.getCssJustifySelf());
        assertEquals(CssAlignSelf.STRETCH, cloned.getCssAlignSelf());
        assertEquals(new Insets(10, 20, 30, 40), cloned.getMargin());

        // margin の独立性
        cloned.margin(99, 99, 99, 99);
        assertEquals(new Insets(10, 20, 30, 40), original.getMargin());
    }

    // === バリデーション ===

    @Test
    void column_rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new CssGridConstraints().column(-2));
    }

    @Test
    void row_rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new CssGridConstraints().row(-2));
    }

    @Test
    void columnSpan_rejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> new CssGridConstraints().columnSpan(0));
    }

    @Test
    void columnSpan_rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new CssGridConstraints().columnSpan(-1));
    }

    @Test
    void rowSpan_rejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> new CssGridConstraints().rowSpan(0));
    }

    @Test
    void rowSpan_rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new CssGridConstraints().rowSpan(-1));
    }

    @Test
    void column_acceptsMinusOne() {
        CssGridConstraints gc = new CssGridConstraints().column(-1);
        assertEquals(-1, gc.getColumn());
    }

    @Test
    void row_acceptsMinusOne() {
        CssGridConstraints gc = new CssGridConstraints().row(-1);
        assertEquals(-1, gc.getRow());
    }
}

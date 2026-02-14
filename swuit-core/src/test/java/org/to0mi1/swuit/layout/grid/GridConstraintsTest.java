package org.to0mi1.swuit.layout.grid;

import java.awt.Insets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridConstraintsTest {

    @Test
    void defaultValues() {
        GridConstraints gc = new GridConstraints();
        assertEquals(-1, gc.getColumn());
        assertEquals(-1, gc.getRow());
        assertEquals(1, gc.getColumnSpan());
        assertEquals(1, gc.getRowSpan());
        assertEquals(JustifySelf.AUTO, gc.getJustifySelf());
        assertEquals(AlignSelf.AUTO, gc.getAlignSelf());
        assertEquals(new Insets(0, 0, 0, 0), gc.getMargin());
    }

    @Test
    void fluentApi() {
        GridConstraints gc = new GridConstraints()
                .column(2).row(3).columnSpan(4).rowSpan(2)
                .justifySelf(JustifySelf.CENTER).alignSelf(AlignSelf.END)
                .margin(5, 10, 15, 20);

        assertEquals(2, gc.getColumn());
        assertEquals(3, gc.getRow());
        assertEquals(4, gc.getColumnSpan());
        assertEquals(2, gc.getRowSpan());
        assertEquals(JustifySelf.CENTER, gc.getJustifySelf());
        assertEquals(AlignSelf.END, gc.getAlignSelf());
        assertEquals(new Insets(5, 10, 15, 20), gc.getMargin());
    }

    @Test
    void clone_independence() {
        GridConstraints original = new GridConstraints()
                .column(1).row(2).columnSpan(3).rowSpan(4)
                .justifySelf(JustifySelf.START).alignSelf(AlignSelf.STRETCH)
                .margin(10, 20, 30, 40);

        GridConstraints cloned = original.clone();

        assertEquals(1, cloned.getColumn());
        assertEquals(2, cloned.getRow());
        assertEquals(3, cloned.getColumnSpan());
        assertEquals(4, cloned.getRowSpan());
        assertEquals(JustifySelf.START, cloned.getJustifySelf());
        assertEquals(AlignSelf.STRETCH, cloned.getAlignSelf());
        assertEquals(new Insets(10, 20, 30, 40), cloned.getMargin());

        // margin の独立性
        cloned.margin(99, 99, 99, 99);
        assertEquals(new Insets(10, 20, 30, 40), original.getMargin());
    }

    // === バリデーション ===

    @Test
    void column_rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new GridConstraints().column(-2));
    }

    @Test
    void row_rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new GridConstraints().row(-2));
    }

    @Test
    void columnSpan_rejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> new GridConstraints().columnSpan(0));
    }

    @Test
    void columnSpan_rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new GridConstraints().columnSpan(-1));
    }

    @Test
    void rowSpan_rejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> new GridConstraints().rowSpan(0));
    }

    @Test
    void rowSpan_rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new GridConstraints().rowSpan(-1));
    }

    @Test
    void column_acceptsMinusOne() {
        GridConstraints gc = new GridConstraints().column(-1);
        assertEquals(-1, gc.getColumn());
    }

    @Test
    void row_acceptsMinusOne() {
        GridConstraints gc = new GridConstraints().row(-1);
        assertEquals(-1, gc.getRow());
    }
}

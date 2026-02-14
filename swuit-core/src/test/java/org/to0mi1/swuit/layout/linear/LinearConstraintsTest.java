package org.to0mi1.swuit.layout.linear;

import java.awt.Insets;

import org.junit.jupiter.api.Test;
import org.to0mi1.swuit.layout.Gravity;

import static org.junit.jupiter.api.Assertions.*;

class LinearConstraintsTest {

    @Test
    void defaultConstructor_setsDefaults() {
        LinearConstraints lc = new LinearConstraints();
        assertEquals(0.0f, lc.weight);
        assertEquals(Gravity.NONE, lc.gravity);
        assertEquals(new Insets(0, 0, 0, 0), lc.margin);
    }

    @Test
    void constructorWithAllArgs_setsFields() {
        Insets margin = new Insets(1, 2, 3, 4);
        LinearConstraints lc = new LinearConstraints(2.0f, Gravity.CENTER, margin);
        assertEquals(2.0f, lc.weight);
        assertEquals(Gravity.CENTER, lc.gravity);
        assertEquals(new Insets(1, 2, 3, 4), lc.margin);
    }

    @Test
    void clone_isIndependentCopy() {
        LinearConstraints original = new LinearConstraints(1.0f, Gravity.LEFT, new Insets(10, 10, 10, 10));
        LinearConstraints cloned = original.clone();

        // 同じ値
        assertEquals(original.weight, cloned.weight);
        assertEquals(original.gravity, cloned.gravity);
        assertEquals(original.margin, cloned.margin);

        // margin の変更が影響しない
        cloned.margin.top = 99;
        assertEquals(10, original.margin.top);
    }
}

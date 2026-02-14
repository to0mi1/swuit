package org.to0mi1.swuit.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GravityTest {

    @Test
    void bitFlags_areIndependent() {
        // 各ビットフラグが独立していることを確認
        int all = Gravity.LEFT | Gravity.CENTER_HORIZONTAL | Gravity.RIGHT | Gravity.FILL_HORIZONTAL
                | Gravity.TOP | Gravity.CENTER_VERTICAL | Gravity.BOTTOM | Gravity.FILL_VERTICAL;
        // 8ビット全てが個別に設定される
        assertEquals(0xFF, all);
    }

    @Test
    void getHorizontal_extractsHorizontalComponent() {
        int gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        assertEquals(Gravity.CENTER_HORIZONTAL, Gravity.getHorizontal(gravity, Gravity.LEFT));
    }

    @Test
    void getVertical_extractsVerticalComponent() {
        int gravity = Gravity.LEFT | Gravity.BOTTOM;
        assertEquals(Gravity.BOTTOM, Gravity.getVertical(gravity, Gravity.TOP));
    }

    @Test
    void compoundConstants_areCombinations() {
        assertEquals(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, Gravity.CENTER);
        assertEquals(Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, Gravity.FILL);
    }

    @Test
    void getHorizontal_returnsDefault_whenNone() {
        assertEquals(Gravity.LEFT, Gravity.getHorizontal(Gravity.NONE, Gravity.LEFT));
        assertEquals(Gravity.FILL_HORIZONTAL, Gravity.getHorizontal(Gravity.TOP, Gravity.FILL_HORIZONTAL));
    }

    @Test
    void getVertical_returnsDefault_whenNone() {
        assertEquals(Gravity.TOP, Gravity.getVertical(Gravity.NONE, Gravity.TOP));
        assertEquals(Gravity.FILL_VERTICAL, Gravity.getVertical(Gravity.LEFT, Gravity.FILL_VERTICAL));
    }
}

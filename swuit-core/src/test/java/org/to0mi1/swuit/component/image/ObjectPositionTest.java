package org.to0mi1.swuit.component.image;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectPositionTest {

    // === CENTER ===

    @Test
    void center_horizontalOffset_centersDrawArea() {
        // container=300, draw=200 -> offset = (300-200)*0.5 = 50
        int offset = ObjectPosition.CENTER.computeOffset(300, 200, true);
        assertEquals(50, offset);
    }

    @Test
    void center_verticalOffset_centersDrawArea() {
        int offset = ObjectPosition.CENTER.computeOffset(400, 300, false);
        assertEquals(50, offset);
    }

    @Test
    void center_drawLargerThanContainer_negativeOffset() {
        // container=200, draw=400 -> offset = (200-400)*0.5 = -100
        int offset = ObjectPosition.CENTER.computeOffset(200, 400, true);
        assertEquals(-100, offset);
    }

    @Test
    void center_exactFit_zeroOffset() {
        int offset = ObjectPosition.CENTER.computeOffset(300, 300, true);
        assertEquals(0, offset);
    }

    // === TOP_LEFT ===

    @Test
    void topLeft_alwaysZeroOffset() {
        assertEquals(0, ObjectPosition.TOP_LEFT.computeOffset(300, 200, true));
        assertEquals(0, ObjectPosition.TOP_LEFT.computeOffset(300, 200, false));
    }

    @Test
    void topLeft_drawLargerThanContainer_zeroOffset() {
        assertEquals(0, ObjectPosition.TOP_LEFT.computeOffset(200, 400, true));
        assertEquals(0, ObjectPosition.TOP_LEFT.computeOffset(200, 400, false));
    }

    // === BOTTOM_RIGHT ===

    @Test
    void bottomRight_horizontalOffset() {
        // container=300, draw=200 -> offset = (300-200)*1.0 = 100
        int offset = ObjectPosition.BOTTOM_RIGHT.computeOffset(300, 200, true);
        assertEquals(100, offset);
    }

    @Test
    void bottomRight_verticalOffset() {
        int offset = ObjectPosition.BOTTOM_RIGHT.computeOffset(400, 300, false);
        assertEquals(100, offset);
    }

    @Test
    void bottomRight_drawLargerThanContainer_negativeOffset() {
        // container=200, draw=400 -> offset = (200-400)*1.0 = -200
        int offset = ObjectPosition.BOTTOM_RIGHT.computeOffset(200, 400, true);
        assertEquals(-200, offset);
    }

    // === カスタム位置 ===

    @Test
    void customPosition_25percent() {
        ObjectPosition pos = new ObjectPosition(0.25f, 0.75f);
        // horizontal: (300-200)*0.25 = 25
        assertEquals(25, pos.computeOffset(300, 200, true));
        // vertical: (300-200)*0.75 = 75
        assertEquals(75, pos.computeOffset(300, 200, false));
    }

    // === equals / hashCode ===

    @Test
    void equals_sameValues() {
        ObjectPosition a = new ObjectPosition(0.5f, 0.5f);
        assertEquals(ObjectPosition.CENTER, a);
        assertEquals(ObjectPosition.CENTER.hashCode(), a.hashCode());
    }

    @Test
    void equals_differentValues() {
        assertNotEquals(ObjectPosition.CENTER, ObjectPosition.TOP_LEFT);
    }

    @Test
    void equals_null() {
        assertNotEquals(null, ObjectPosition.CENTER);
    }

    // === getter ===

    @Test
    void getters() {
        ObjectPosition pos = new ObjectPosition(0.3f, 0.7f);
        assertEquals(0.3f, pos.getX(), 0.0001f);
        assertEquals(0.7f, pos.getY(), 0.0001f);
    }

    // === toString ===

    @Test
    void toStringContainsValues() {
        String s = new ObjectPosition(0.5f, 0.5f).toString();
        assertTrue(s.contains("0.5"));
    }
}

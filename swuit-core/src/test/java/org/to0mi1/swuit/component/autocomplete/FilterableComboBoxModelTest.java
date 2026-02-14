package org.to0mi1.swuit.component.autocomplete;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilterableComboBoxModelTest {

    // === ListDataEvent 記録用ヘルパー ===

    private static class EventRecorder implements ListDataListener {
        final List<ListDataEvent> added = new ArrayList<>();
        final List<ListDataEvent> removed = new ArrayList<>();
        final List<ListDataEvent> changed = new ArrayList<>();

        @Override
        public void intervalAdded(ListDataEvent e) {
            added.add(e);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            removed.add(e);
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            changed.add(e);
        }

        void clear() {
            added.clear();
            removed.clear();
            changed.clear();
        }
    }

    // === 初期状態 ===

    @Test
    void emptyModel() {
        var model = new FilterableComboBoxModel<String>();
        assertEquals(0, model.getSize());
        assertNull(model.getSelectedItem());
        assertNull(model.getFirstFilteredItem());
        assertFalse(model.isFiltered());
    }

    @Test
    void initialItems() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana", "Cherry"));
        assertEquals(3, model.getSize());
        assertEquals(3, model.getAllItemsCount());
        assertEquals("Apple", model.getElementAt(0));
        assertEquals("Cherry", model.getElementAt(2));
    }

    // === 要素操作 ===

    @Test
    void addElement() {
        var model = new FilterableComboBoxModel<String>();
        model.addElement("Apple");
        model.addElement("Banana");
        assertEquals(2, model.getSize());
        assertEquals(2, model.getAllItemsCount());
        assertEquals("Apple", model.getElementAt(0));
        assertEquals("Banana", model.getElementAt(1));
    }

    @Test
    void removeElement() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana", "Cherry"));
        model.removeElement("Banana");
        assertEquals(2, model.getSize());
        assertEquals(2, model.getAllItemsCount());
        assertEquals("Apple", model.getElementAt(0));
        assertEquals("Cherry", model.getElementAt(1));
    }

    @Test
    void removeElement_clearsSelection() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana"));
        model.setSelectedItem("Apple");
        model.removeElement("Apple");
        assertNull(model.getSelectedItem());
    }

    @Test
    void insertElementAt() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Cherry"));
        model.insertElementAt("Banana", 1);
        assertEquals(3, model.getSize());
        assertEquals("Banana", model.getElementAt(1));
    }

    @Test
    void removeElementAt() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana", "Cherry"));
        model.removeElementAt(1);
        assertEquals(2, model.getSize());
        assertEquals("Apple", model.getElementAt(0));
        assertEquals("Cherry", model.getElementAt(1));
    }

    // === 選択 ===

    @Test
    void selectedItem() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana"));
        assertNull(model.getSelectedItem());
        model.setSelectedItem("Apple");
        assertEquals("Apple", model.getSelectedItem());
    }

    @Test
    void setSelectedItem_sameValue_noEvent() {
        var model = new FilterableComboBoxModel<>(List.of("Apple"));
        model.setSelectedItem("Apple");
        // 同じ値を再設定しても例外が出ないことを確認
        model.setSelectedItem("Apple");
        assertEquals("Apple", model.getSelectedItem());
    }

    // === フィルタリング ===

    @Test
    void applyFilter_reducesItems() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Apricot", "Banana", "Cherry"));
        model.applyFilter(new StartsWithMatcher<>(), "Ap");
        assertEquals(2, model.getSize());
        assertEquals("Apple", model.getElementAt(0));
        assertEquals("Apricot", model.getElementAt(1));
        // allItems は変更されない
        assertEquals(4, model.getAllItemsCount());
    }

    @Test
    void applyFilter_noMatch() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana", "Cherry"));
        model.applyFilter(new StartsWithMatcher<>(), "Xyz");
        assertEquals(0, model.getSize());
        assertNull(model.getFirstFilteredItem());
    }

    @Test
    void clearFilter_restoresAllItems() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana", "Cherry"));
        model.applyFilter(new StartsWithMatcher<>(), "A");
        assertEquals(1, model.getSize());
        model.clearFilter();
        assertEquals(3, model.getSize());
        assertEquals("Apple", model.getElementAt(0));
        assertEquals("Banana", model.getElementAt(1));
        assertEquals("Cherry", model.getElementAt(2));
    }

    @Test
    void getFirstFilteredItem() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Apricot", "Banana"));
        model.applyFilter(new StartsWithMatcher<>(), "Ap");
        assertEquals("Apple", model.getFirstFilteredItem());
    }

    @Test
    void applyFilter_withContainsMatcher() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Pineapple", "Banana"));
        model.applyFilter(new ContainsMatcher<>(), "apple");
        assertEquals(1, model.getSize());
        assertEquals("Pineapple", model.getElementAt(0));
    }

    @Test
    void applyFilter_withContainsMatcher_ignoreCase() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Pineapple", "Banana"));
        model.applyFilter(new ContainsMatcher<>(true), "apple");
        assertEquals(2, model.getSize());
        assertEquals("Apple", model.getElementAt(0));
        assertEquals("Pineapple", model.getElementAt(1));
    }

    // === isFiltered 状態遷移 ===

    @Test
    void isFiltered_transitions() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana"));
        assertFalse(model.isFiltered());

        model.applyFilter(new StartsWithMatcher<>(), "A");
        assertTrue(model.isFiltered());

        model.clearFilter();
        assertFalse(model.isFiltered());

        model.applyFilter(new StartsWithMatcher<>(), "B");
        assertTrue(model.isFiltered());
    }

    // === フィルタ中の要素操作 ===

    @Test
    void addElement_duringFilter_addsToAllItemsOnly() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana"));
        model.applyFilter(new StartsWithMatcher<>(), "A");
        assertEquals(1, model.getSize()); // "Apple" のみ

        model.addElement("Avocado");
        // フィルタ中は filteredItems に追加されない
        assertEquals(1, model.getSize());
        assertEquals(3, model.getAllItemsCount());
    }

    @Test
    void addElement_duringFilter_visibleAfterClearFilter() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana"));
        model.applyFilter(new StartsWithMatcher<>(), "A");
        model.addElement("Avocado");

        model.clearFilter();
        assertEquals(3, model.getSize());
        assertEquals("Avocado", model.getElementAt(2));
    }

    @Test
    void insertElementAt_duringFilter_addsToAllItemsEnd() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana", "Cherry"));
        model.applyFilter(new StartsWithMatcher<>(), "A");
        assertEquals(1, model.getSize()); // "Apple" のみ

        model.insertElementAt("Avocado", 0);
        // フィルタ中は filteredItems に追加されない
        assertEquals(1, model.getSize());
        assertEquals(4, model.getAllItemsCount());

        // clearFilter 後に確認
        model.clearFilter();
        assertEquals(4, model.getSize());
        // フィルタ中の insertElementAt は allItems 末尾に追加
        assertEquals("Avocado", model.getElementAt(3));
    }

    // === ListDataEvent 発火検証 ===

    @Test
    void addElement_firesIntervalAdded() {
        var model = new FilterableComboBoxModel<String>();
        var recorder = new EventRecorder();
        model.addListDataListener(recorder);

        model.addElement("Apple");

        assertEquals(1, recorder.added.size());
        assertEquals(0, recorder.added.get(0).getIndex0());
        assertEquals(0, recorder.added.get(0).getIndex1());
        assertTrue(recorder.removed.isEmpty());
    }

    @Test
    void addElement_duringFilter_doesNotFireEvent() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana"));
        model.applyFilter(new StartsWithMatcher<>(), "A");
        var recorder = new EventRecorder();
        model.addListDataListener(recorder);

        model.addElement("Avocado");

        // フィルタ中は filteredItems に追加しないのでイベントなし
        assertTrue(recorder.added.isEmpty());
        assertTrue(recorder.removed.isEmpty());
    }

    @Test
    void removeElement_firesIntervalRemoved() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana"));
        var recorder = new EventRecorder();
        model.addListDataListener(recorder);

        model.removeElement("Apple");

        assertEquals(1, recorder.removed.size());
        assertEquals(0, recorder.removed.get(0).getIndex0());
        assertEquals(0, recorder.removed.get(0).getIndex1());
    }

    @Test
    void removeElement_selectedItem_firesContentsChanged() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana"));
        model.setSelectedItem("Apple");
        var recorder = new EventRecorder();
        model.addListDataListener(recorder);

        model.removeElement("Apple");

        assertEquals(1, recorder.removed.size());
        // contentsChanged: 選択解除分
        assertEquals(1, recorder.changed.size());
        assertEquals(-1, recorder.changed.get(0).getIndex0());
    }

    @Test
    void applyFilter_firesRemovedThenAdded() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Apricot", "Banana", "Cherry"));
        var recorder = new EventRecorder();
        model.addListDataListener(recorder);

        model.applyFilter(new StartsWithMatcher<>(), "Ap");

        // 4 件の全削除 → 2 件の追加
        assertEquals(1, recorder.removed.size());
        assertEquals(0, recorder.removed.get(0).getIndex0());
        assertEquals(3, recorder.removed.get(0).getIndex1());
        assertEquals(1, recorder.added.size());
        assertEquals(0, recorder.added.get(0).getIndex0());
        assertEquals(1, recorder.added.get(0).getIndex1());
    }

    @Test
    void clearFilter_firesRemovedThenAdded() {
        var model = new FilterableComboBoxModel<>(List.of("Apple", "Banana", "Cherry"));
        model.applyFilter(new StartsWithMatcher<>(), "A");
        assertEquals(1, model.getSize());

        var recorder = new EventRecorder();
        model.addListDataListener(recorder);

        model.clearFilter();

        // 1 件の削除 → 3 件の追加
        assertEquals(1, recorder.removed.size());
        assertEquals(0, recorder.removed.get(0).getIndex0());
        assertEquals(0, recorder.removed.get(0).getIndex1());
        assertEquals(1, recorder.added.size());
        assertEquals(0, recorder.added.get(0).getIndex0());
        assertEquals(2, recorder.added.get(0).getIndex1());
    }
}

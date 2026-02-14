package org.to0mi1.swuit.component.autocomplete;

import java.util.List;

import javax.swing.DefaultComboBoxModel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutoCompleteComboBoxTest {

    // === 初期状態 ===

    @Test
    void defaultState() {
        var combo = new AutoCompleteComboBox<String>();
        assertTrue(combo.isEditable());
        assertTrue(combo.isFilterEnabled());
        assertTrue(combo.isSuggestEnabled());
        assertFalse(combo.isSuggestIgnoreCase());
        assertTrue(combo.isPopupOnInput());
        assertNotNull(combo.getMatcher());
        assertInstanceOf(StartsWithMatcher.class, combo.getMatcher());
    }

    @Test
    void initialItems() {
        var combo = new AutoCompleteComboBox<>(List.of("Apple", "Banana", "Cherry"));
        assertEquals(3, combo.getModel().getSize());
        assertEquals("Apple", combo.getModel().getElementAt(0));
    }

    @Test
    void modelType() {
        var combo = new AutoCompleteComboBox<String>();
        assertInstanceOf(FilterableComboBoxModel.class, combo.getModel());
    }

    // === プロパティ設定 ===

    @Test
    void setMatcher() {
        var combo = new AutoCompleteComboBox<String>();
        var containsMatcher = new ContainsMatcher<String>();
        combo.setMatcher(containsMatcher);
        assertSame(containsMatcher, combo.getMatcher());
    }

    @Test
    void setMatcher_null_throws() {
        var combo = new AutoCompleteComboBox<String>();
        assertThrows(IllegalArgumentException.class, () -> combo.setMatcher(null));
    }

    @Test
    void setFilterEnabled() {
        var combo = new AutoCompleteComboBox<String>();
        combo.setFilterEnabled(false);
        assertFalse(combo.isFilterEnabled());
        combo.setFilterEnabled(true);
        assertTrue(combo.isFilterEnabled());
    }

    @Test
    void suggestIgnoreCase_defaultFalse() {
        var combo = new AutoCompleteComboBox<String>();
        assertFalse(combo.isSuggestIgnoreCase());
    }

    @Test
    void setSuggestIgnoreCase() {
        var combo = new AutoCompleteComboBox<String>();
        combo.setSuggestIgnoreCase(true);
        assertTrue(combo.isSuggestIgnoreCase());
    }

    @Test
    void setSuggestEnabled() {
        var combo = new AutoCompleteComboBox<String>();
        combo.setSuggestEnabled(false);
        assertFalse(combo.isSuggestEnabled());
    }

    @Test
    void setPopupOnInput() {
        var combo = new AutoCompleteComboBox<String>();
        combo.setPopupOnInput(false);
        assertFalse(combo.isPopupOnInput());
    }

    // === editable 保証 ===

    @Test
    void alwaysEditable() {
        var combo = new AutoCompleteComboBox<>(List.of("Apple", "Banana"));
        assertTrue(combo.isEditable());
    }

    // === setModel 防御 ===

    @Test
    void setModel_withDefaultComboBoxModel_throws() {
        var combo = new AutoCompleteComboBox<String>();
        assertThrows(IllegalArgumentException.class,
                () -> combo.setModel(new DefaultComboBoxModel<>()));
    }

    @Test
    void setModel_withFilterableComboBoxModel_succeeds() {
        var combo = new AutoCompleteComboBox<String>();
        var newModel = new FilterableComboBoxModel<>(List.of("X", "Y"));
        combo.setModel(newModel);
        assertSame(newModel, combo.getModel());
    }

    // === setEditable 防御 ===

    @Test
    void setEditable_false_throws() {
        var combo = new AutoCompleteComboBox<String>();
        assertThrows(UnsupportedOperationException.class,
                () -> combo.setEditable(false));
    }

    @Test
    void setEditable_true_succeeds() {
        var combo = new AutoCompleteComboBox<String>();
        // 例外なし
        combo.setEditable(true);
        assertTrue(combo.isEditable());
    }

    // === ラムダマッチャー ===

    @Test
    void customLambdaMatcher() {
        var combo = new AutoCompleteComboBox<>(List.of("Apple", "Banana", "Cherry"));
        combo.setMatcher((item, input) -> item.endsWith(input));
        assertTrue(combo.getMatcher().matches("Apple", "le"));
        assertFalse(combo.getMatcher().matches("Apple", "App"));
    }

    // === モデル操作 ===

    @Test
    void addAndRemoveItems() {
        var combo = new AutoCompleteComboBox<String>();
        combo.getModel().addElement("Apple");
        combo.getModel().addElement("Banana");
        assertEquals(2, combo.getModel().getSize());

        combo.getModel().removeElement("Apple");
        assertEquals(1, combo.getModel().getSize());
        assertEquals("Banana", combo.getModel().getElementAt(0));
    }

    // === フィルタリング（モデル経由） ===

    @Test
    void filterThroughModel() {
        var combo = new AutoCompleteComboBox<>(List.of("Apple", "Apricot", "Banana", "Cherry"));
        combo.getModel().applyFilter(combo.getMatcher(), "Ap");
        assertEquals(2, combo.getModel().getSize());
        assertEquals("Apple", combo.getModel().getElementAt(0));
        assertEquals("Apricot", combo.getModel().getElementAt(1));
    }

    @Test
    void clearFilterThroughModel() {
        var combo = new AutoCompleteComboBox<>(List.of("Apple", "Banana", "Cherry"));
        combo.getModel().applyFilter(combo.getMatcher(), "A");
        assertEquals(1, combo.getModel().getSize());
        combo.getModel().clearFilter();
        assertEquals(3, combo.getModel().getSize());
    }

    // === 非 String 型 ===

    @Test
    void nonStringType() {
        var combo = new AutoCompleteComboBox<>(List.of(100, 123, 200, 234));
        combo.getModel().applyFilter(combo.getMatcher(), "12");
        assertEquals(1, combo.getModel().getSize());
        assertEquals(123, combo.getModel().getElementAt(0));
    }

    // === stringConverter ===

    @Test
    void stringConverter_defaultIsStringValueOf() {
        var combo = new AutoCompleteComboBox<String>();
        assertNotNull(combo.getStringConverter());
        assertEquals("Apple", combo.getStringConverter().apply("Apple"));
    }

    @Test
    void setStringConverter() {
        record Fruit(String name, int calories) {}
        var combo = new AutoCompleteComboBox<>(List.of(
                new Fruit("Apple", 95),
                new Fruit("Banana", 105)));
        combo.setStringConverter(Fruit::name);
        assertEquals("Apple", combo.getStringConverter().apply(combo.getModel().getElementAt(0)));
        assertEquals("Banana", combo.getStringConverter().apply(combo.getModel().getElementAt(1)));
    }

    @Test
    void setStringConverter_null_throws() {
        var combo = new AutoCompleteComboBox<String>();
        assertThrows(IllegalArgumentException.class, () -> combo.setStringConverter(null));
    }
}

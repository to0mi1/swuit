package org.to0mi1.swuit.component.autocomplete;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutoCompleteMatcherTest {

    // === StartsWithMatcher (大文字小文字区別あり) ===

    @Test
    void startsWith_caseSensitive_matches() {
        var matcher = new StartsWithMatcher<String>();
        assertTrue(matcher.matches("Apple", "App"));
        assertTrue(matcher.matches("Apple", "Apple"));
        assertTrue(matcher.matches("Apple", ""));
    }

    @Test
    void startsWith_caseSensitive_noMatch() {
        var matcher = new StartsWithMatcher<String>();
        assertFalse(matcher.matches("Apple", "app"));
        assertFalse(matcher.matches("Apple", "Banana"));
        assertFalse(matcher.matches("Apple", "pple"));
    }

    @Test
    void startsWith_caseSensitive_nullSafe() {
        var matcher = new StartsWithMatcher<String>();
        assertFalse(matcher.matches(null, "App"));
        assertFalse(matcher.matches("Apple", null));
        assertFalse(matcher.matches(null, null));
    }

    // === StartsWithMatcher (大文字小文字無視) ===

    @Test
    void startsWith_ignoreCase_matches() {
        var matcher = new StartsWithMatcher<String>(true);
        assertTrue(matcher.matches("Apple", "app"));
        assertTrue(matcher.matches("Apple", "APP"));
        assertTrue(matcher.matches("apple", "App"));
    }

    @Test
    void startsWith_ignoreCase_noMatch() {
        var matcher = new StartsWithMatcher<String>(true);
        assertFalse(matcher.matches("Apple", "Banana"));
        assertFalse(matcher.matches("Apple", "pple"));
    }

    // === StartsWithMatcher (Function コンストラクタ) ===

    record NamedItem(String name, int value) {}

    @Test
    void startsWith_function_matches() {
        var matcher = new StartsWithMatcher<>(NamedItem::name);
        assertTrue(matcher.matches(new NamedItem("Apple", 1), "App"));
        assertTrue(matcher.matches(new NamedItem("Apple", 1), ""));
    }

    @Test
    void startsWith_function_noMatch() {
        var matcher = new StartsWithMatcher<>(NamedItem::name);
        assertFalse(matcher.matches(new NamedItem("Apple", 1), "Ban"));
    }

    @Test
    void startsWith_function_ignoreCase() {
        var matcher = new StartsWithMatcher<>(NamedItem::name, true);
        assertTrue(matcher.matches(new NamedItem("Apple", 1), "app"));
        assertTrue(matcher.matches(new NamedItem("Apple", 1), "APP"));
        assertFalse(matcher.matches(new NamedItem("Apple", 1), "ban"));
    }

    @Test
    void startsWith_function_nullReturn() {
        var matcher = new StartsWithMatcher<NamedItem>(item -> null);
        assertFalse(matcher.matches(new NamedItem("Apple", 1), "App"));
    }

    @Test
    void startsWith_function_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new StartsWithMatcher<String>(null));
    }

    // === ContainsMatcher (大文字小文字区別あり) ===

    @Test
    void contains_caseSensitive_matches() {
        var matcher = new ContainsMatcher<String>();
        assertTrue(matcher.matches("Apple", "ppl"));
        assertTrue(matcher.matches("Apple", "Apple"));
        assertTrue(matcher.matches("Apple", ""));
        assertTrue(matcher.matches("Apple", "le"));
    }

    @Test
    void contains_caseSensitive_noMatch() {
        var matcher = new ContainsMatcher<String>();
        assertFalse(matcher.matches("Apple", "PPL"));
        assertFalse(matcher.matches("Apple", "Banana"));
    }

    @Test
    void contains_caseSensitive_nullSafe() {
        var matcher = new ContainsMatcher<String>();
        assertFalse(matcher.matches(null, "ppl"));
        assertFalse(matcher.matches("Apple", null));
        assertFalse(matcher.matches(null, null));
    }

    // === ContainsMatcher (大文字小文字無視) ===

    @Test
    void contains_ignoreCase_matches() {
        var matcher = new ContainsMatcher<String>(true);
        assertTrue(matcher.matches("Apple", "PPL"));
        assertTrue(matcher.matches("Apple", "apple"));
        assertTrue(matcher.matches("APPLE", "ppl"));
    }

    @Test
    void contains_ignoreCase_noMatch() {
        var matcher = new ContainsMatcher<String>(true);
        assertFalse(matcher.matches("Apple", "Banana"));
        assertFalse(matcher.matches("Apple", "xyz"));
    }

    // === ContainsMatcher (Function コンストラクタ) ===

    @Test
    void contains_function_matches() {
        var matcher = new ContainsMatcher<>(NamedItem::name);
        assertTrue(matcher.matches(new NamedItem("Apple", 1), "ppl"));
        assertTrue(matcher.matches(new NamedItem("Apple", 1), ""));
    }

    @Test
    void contains_function_noMatch() {
        var matcher = new ContainsMatcher<>(NamedItem::name);
        assertFalse(matcher.matches(new NamedItem("Apple", 1), "xyz"));
    }

    @Test
    void contains_function_ignoreCase() {
        var matcher = new ContainsMatcher<>(NamedItem::name, true);
        assertTrue(matcher.matches(new NamedItem("Apple", 1), "PPL"));
        assertFalse(matcher.matches(new NamedItem("Apple", 1), "xyz"));
    }

    @Test
    void contains_function_nullReturn() {
        var matcher = new ContainsMatcher<NamedItem>(item -> null);
        assertFalse(matcher.matches(new NamedItem("Apple", 1), "ppl"));
    }

    @Test
    void contains_function_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new ContainsMatcher<String>(null));
    }

    // === ラムダ指定 ===

    @Test
    void lambda_matcher() {
        AutoCompleteMatcher<Integer> matcher = (item, input) -> {
            try {
                return item == Integer.parseInt(input);
            } catch (NumberFormatException e) {
                return false;
            }
        };
        assertTrue(matcher.matches(42, "42"));
        assertFalse(matcher.matches(42, "43"));
        assertFalse(matcher.matches(42, "abc"));
    }

    // === toString を使った非 String 型 ===

    @Test
    void startsWith_nonStringType() {
        var matcher = new StartsWithMatcher<Integer>();
        assertTrue(matcher.matches(123, "12"));
        assertFalse(matcher.matches(123, "23"));
    }

    @Test
    void contains_nonStringType() {
        var matcher = new ContainsMatcher<Integer>();
        assertTrue(matcher.matches(123, "23"));
        assertFalse(matcher.matches(123, "45"));
    }
}

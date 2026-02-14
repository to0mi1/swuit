package org.to0mi1.swuit.demo.common;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.to0mi1.swuit.component.autocomplete.AutoCompleteComboBox;
import org.to0mi1.swuit.component.autocomplete.ContainsMatcher;
import org.to0mi1.swuit.component.autocomplete.StartsWithMatcher;
import org.to0mi1.swuit.layout.Orientation;
import org.to0mi1.swuit.layout.linear.LinearLayout;

/**
 * AutoComplete デモパネル生成ユーティリティ。
 */
public final class AutoCompleteDemos {

    private AutoCompleteDemos() {
    }

    private static final List<String> FRUIT_ITEMS = List.of(
            "Apple", "Apricot", "Avocado",
            "Banana", "Blueberry", "Blackberry",
            "Cherry", "Coconut", "Cranberry",
            "Date", "Dragonfruit",
            "Elderberry",
            "Fig",
            "Grape", "Grapefruit", "Guava",
            "Kiwi",
            "Lemon", "Lime", "Lychee",
            "Mango", "Melon",
            "Nectarine",
            "Orange",
            "Papaya", "Peach", "Pear", "Pineapple", "Plum", "Pomegranate",
            "Raspberry",
            "Strawberry",
            "Tangerine",
            "Watermelon"
    );

    record Country(String code, String name) {
        @Override
        public String toString() {
            return "Country{code=" + code + ", name=" + name + "}";
        }
    }

    private static final List<Country> COUNTRY_ITEMS = List.of(
            new Country("JP", "Japan"),
            new Country("US", "United States"),
            new Country("GB", "United Kingdom"),
            new Country("DE", "Germany"),
            new Country("FR", "France"),
            new Country("IT", "Italy"),
            new Country("ES", "Spain"),
            new Country("CA", "Canada"),
            new Country("AU", "Australia"),
            new Country("BR", "Brazil"),
            new Country("IN", "India"),
            new Country("CN", "China"),
            new Country("KR", "South Korea"),
            new Country("SE", "Sweden"),
            new Country("SG", "Singapore")
    );

    /** AutoComplete の各バリエーションデモ */
    public static JComponent autoCompleteBasic() {
        JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. デフォルト（前方一致 + フィルタ + サジェスト）
        panel.add(labeled("前方一致（デフォルト）:",
                new AutoCompleteComboBox<>(FRUIT_ITEMS)));

        // 2. 部分一致（大文字小文字無視）
        var containsCombo = new AutoCompleteComboBox<>(FRUIT_ITEMS);
        containsCombo.setMatcher(new ContainsMatcher<>(true));
        panel.add(labeled("部分一致（大文字小文字無視）:", containsCombo));

        // 3. フィルタのみ（サジェスト OFF）
        var filterOnlyCombo = new AutoCompleteComboBox<>(FRUIT_ITEMS);
        filterOnlyCombo.setSuggestEnabled(false);
        panel.add(labeled("フィルタのみ:", filterOnlyCombo));

        // 4. サジェストのみ（フィルタ OFF、ポップアップ手動）
        var suggestOnlyCombo = new AutoCompleteComboBox<>(FRUIT_ITEMS);
        suggestOnlyCombo.setFilterEnabled(false);
        suggestOnlyCombo.setPopupOnInput(false);
        panel.add(labeled("サジェストのみ:", suggestOnlyCombo));

        // 5. カスタムマッチャー（末尾一致）
        var customCombo = new AutoCompleteComboBox<>(FRUIT_ITEMS);
        customCombo.setMatcher((item, input) ->
                item.toLowerCase().endsWith(input.toLowerCase()));
        panel.add(labeled("カスタム（末尾一致）:", customCombo));

        return panel;
    }

    /** オブジェクト型アイテムの AutoComplete デモ */
    public static JComponent autoCompleteObject() {
        JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Country record — toString() は \"Country{code=JP, name=Japan}\" 形式"));

        // 1. name で検索・補完（Function コンストラクタ使用）
        var byNameCombo = new AutoCompleteComboBox<>(COUNTRY_ITEMS);
        byNameCombo.setStringConverter(Country::name);
        byNameCombo.setMatcher(new StartsWithMatcher<>(Country::name, true));
        byNameCombo.setSuggestIgnoreCase(true);
        panel.add(labeled("国名で検索（前方一致）:", byNameCombo));

        // 2. code で検索・補完
        var byCodeCombo = new AutoCompleteComboBox<>(COUNTRY_ITEMS);
        byCodeCombo.setStringConverter(Country::code);
        byCodeCombo.setMatcher((c, input) ->
                c.code().toLowerCase().startsWith(input.toLowerCase()));
        byCodeCombo.setSuggestIgnoreCase(true);
        panel.add(labeled("国コードで検索:", byCodeCombo));

        // 3. name + code の部分一致フィルタ、name でサジェスト
        var combinedCombo = new AutoCompleteComboBox<>(COUNTRY_ITEMS);
        combinedCombo.setStringConverter(Country::name);
        combinedCombo.setMatcher((c, input) -> {
            String lower = input.toLowerCase();
            return c.name().toLowerCase().contains(lower)
                    || c.code().toLowerCase().contains(lower);
        });
        combinedCombo.setSuggestIgnoreCase(true);
        panel.add(labeled("名前+コードで部分一致フィルタ、名前でサジェスト:", combinedCombo));

        // 4. stringConverter 未設定（toString() がそのまま使われる）
        var noConverterCombo = new AutoCompleteComboBox<>(COUNTRY_ITEMS);
        noConverterCombo.setMatcher((c, input) ->
                c.name().toLowerCase().startsWith(input.toLowerCase()));
        panel.add(labeled("stringConverter 未設定（サジェスト不可）:", noConverterCombo));

        return panel;
    }

    private static JComponent labeled(String text, JComponent component) {
        JPanel row = new JPanel(new LinearLayout(Orientation.VERTICAL, 4));
        row.add(new JLabel(text));
        row.add(component);
        return row;
    }
}

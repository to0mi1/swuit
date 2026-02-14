package org.to0mi1.swuit.component.autocomplete;

/**
 * オートコンプリートのマッチング戦略を定義する関数型インターフェース。
 *
 * @param <E> 候補アイテムの型
 */
@FunctionalInterface
public interface AutoCompleteMatcher<E> {

    /**
     * アイテムが入力テキストにマッチするかを判定する。
     *
     * @param item      候補アイテム
     * @param inputText ユーザー入力テキスト
     * @return マッチする場合 {@code true}
     */
    boolean matches(E item, String inputText);
}

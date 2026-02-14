package org.to0mi1.swuit.component.autocomplete;

import java.util.Locale;
import java.util.function.Function;

/**
 * 前方一致マッチャー。
 * <p>
 * アイテムから取得した文字列が入力テキストで始まるかを判定する。
 * デフォルトでは {@link String#valueOf(Object)} を使用するが、
 * コンストラクタで任意の変換関数を指定可能。
 *
 * @param <E> 候補アイテムの型
 */
public class StartsWithMatcher<E> implements AutoCompleteMatcher<E> {

    private final Function<E, String> toStringFunc;
    private final boolean ignoreCase;

    /** 大文字小文字を区別する前方一致マッチャーを生成する。 */
    public StartsWithMatcher() {
        this(false);
    }

    /**
     * 前方一致マッチャーを生成する。
     *
     * @param ignoreCase {@code true} の場合、大文字小文字を無視する
     */
    public StartsWithMatcher(boolean ignoreCase) {
        this(String::valueOf, ignoreCase);
    }

    /**
     * 指定の変換関数を使用する前方一致マッチャーを生成する（大文字小文字区別あり）。
     *
     * @param toStringFunc アイテムから文字列への変換関数
     */
    public StartsWithMatcher(Function<E, String> toStringFunc) {
        this(toStringFunc, false);
    }

    /**
     * 指定の変換関数を使用する前方一致マッチャーを生成する。
     *
     * @param toStringFunc アイテムから文字列への変換関数
     * @param ignoreCase   {@code true} の場合、大文字小文字を無視する
     */
    public StartsWithMatcher(Function<E, String> toStringFunc, boolean ignoreCase) {
        if (toStringFunc == null) {
            throw new IllegalArgumentException("toStringFunc must not be null");
        }
        this.toStringFunc = toStringFunc;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean matches(E item, String inputText) {
        if (item == null || inputText == null) {
            return false;
        }
        String itemStr = toStringFunc.apply(item);
        if (itemStr == null) {
            return false;
        }
        if (ignoreCase) {
            return itemStr.toLowerCase(Locale.ROOT).startsWith(inputText.toLowerCase(Locale.ROOT));
        }
        return itemStr.startsWith(inputText);
    }
}

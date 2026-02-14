package org.to0mi1.swuit.component.autocomplete;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.function.Function;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * オートコンプリート機能付き {@link JComboBox}。
 * <p>
 * フィルタ型（ドロップダウン候補を絞り込み）とサジェスト型（インライン補完）の両方を提供し、
 * それぞれを個別に ON/OFF 可能。常に editable として動作する。
 *
 * <p><b>使用例:</b></p>
 * <pre>{@code
 * var combo = new AutoCompleteComboBox<>(List.of("Apple", "Banana", "Cherry"));
 *
 * // 部分一致・大文字小文字無視に変更
 * combo.setMatcher(new ContainsMatcher<>(true));
 *
 * // フィルタのみ（インライン補完なし）
 * combo.setSuggestEnabled(false);
 * }</pre>
 *
 * <p><b>イベント制御の設計:</b></p>
 * <p>
 * JComboBox はモデル変更時に内部で {@code configureEditor} を呼び出してエディタテキストを
 * 上書きし、それが DocumentListener を発火させてイベントが連鎖する。この問題に対し、
 * 以下の 3 つの仕組みで制御する:
 * <ol>
 *   <li><b>DocumentListener の着脱</b> — プログラムからテキストやモデルを変更する際は
 *       リスナーを物理的に取り外し、完了後に再登録する。boolean フラグと異なり確実。</li>
 *   <li><b>世代番号 ({@code textChangeGeneration})</b> — {@code invokeLater} で
 *       遅延実行される処理が、より新しい操作によって無効化されたことを検出する。</li>
 *   <li><b>{@code navigating} フラグ</b> — ポップアップ内の矢印キーナビゲーション中に
 *       {@code configureEditor} と {@code onDocumentChange} をスキップし、
 *       ユーザーの入力テキストを保持する。</li>
 * </ol>
 *
 * @param <E> 候補アイテムの型
 */
public class AutoCompleteComboBox<E> extends JComboBox<E> {

    // === プロパティ ===

    private AutoCompleteMatcher<E> matcher = new StartsWithMatcher<>();
    private Function<E, String> stringConverter = String::valueOf;
    private boolean filterEnabled = true;
    private boolean suggestEnabled = true;
    private boolean suggestIgnoreCase;
    private boolean popupOnInput = true;

    // === 内部状態 ===

    /** DocumentListener が一時的に取り外されているか */
    private boolean listenerSuspended;
    /** ポップアップ内を矢印キーでナビゲート中か */
    private boolean navigating;
    /** Backspace/Delete 後のインライン補完抑制 */
    private boolean suppressInlineCompletion;
    /** invokeLater で遅延実行されるテキスト変更処理の世代番号 */
    private int textChangeGeneration;

    // === リスナー ===

    private transient DocumentListener documentListener;
    private transient KeyListener keyListener;
    private transient FocusListener autoCompleteFocusListener;

    // === コンストラクタ ===

    /** 空のオートコンプリート ComboBox を生成する。 */
    public AutoCompleteComboBox() {
        super(new FilterableComboBoxModel<>());
        init();
    }

    /**
     * 初期アイテムを指定してオートコンプリート ComboBox を生成する。
     *
     * @param items 初期候補アイテム
     */
    public AutoCompleteComboBox(List<E> items) {
        super(new FilterableComboBoxModel<>(items));
        init();
    }

    private void init() {
        setEditable(true);
        installRenderer();
        installAutoComplete();
    }

    // === プロパティアクセサ ===

    /** マッチング戦略を返す。 */
    public AutoCompleteMatcher<E> getMatcher() {
        return matcher;
    }

    /**
     * マッチング戦略を設定する。
     *
     * @param matcher マッチング戦略（{@code null} 不可）
     */
    public void setMatcher(AutoCompleteMatcher<E> matcher) {
        if (matcher == null) {
            throw new IllegalArgumentException("matcher must not be null");
        }
        this.matcher = matcher;
    }

    /** アイテムから表示文字列への変換関数を返す。 */
    public Function<E, String> getStringConverter() {
        return stringConverter;
    }

    /**
     * アイテムから表示文字列への変換関数を設定する。
     * <p>
     * サジェスト（インライン補完）の前方一致判定とエディタへの補完テキスト生成に使用される。
     * デフォルトは {@link String#valueOf(Object)}。
     *
     * @param stringConverter 変換関数（{@code null} 不可）
     */
    public void setStringConverter(Function<E, String> stringConverter) {
        if (stringConverter == null) {
            throw new IllegalArgumentException("stringConverter must not be null");
        }
        this.stringConverter = stringConverter;
    }

    /** フィルタ機能が有効かを返す。 */
    public boolean isFilterEnabled() {
        return filterEnabled;
    }

    /** フィルタ機能の ON/OFF を設定する。 */
    public void setFilterEnabled(boolean filterEnabled) {
        this.filterEnabled = filterEnabled;
    }

    /** インライン補完（サジェスト）が有効かを返す。 */
    public boolean isSuggestEnabled() {
        return suggestEnabled;
    }

    /** インライン補完（サジェスト）の ON/OFF を設定する。 */
    public void setSuggestEnabled(boolean suggestEnabled) {
        this.suggestEnabled = suggestEnabled;
    }

    /** サジェストの前方一致で大文字小文字を無視するかを返す。 */
    public boolean isSuggestIgnoreCase() {
        return suggestIgnoreCase;
    }

    /**
     * サジェストの前方一致で大文字小文字を無視するかを設定する。
     * <p>
     * フィルタ用マッチャーの大文字小文字設定とは独立して動作する。
     *
     * @param suggestIgnoreCase {@code true} の場合、大文字小文字を無視して前方一致判定する
     */
    public void setSuggestIgnoreCase(boolean suggestIgnoreCase) {
        this.suggestIgnoreCase = suggestIgnoreCase;
    }

    /** 入力時のポップアップ自動表示が有効かを返す。 */
    public boolean isPopupOnInput() {
        return popupOnInput;
    }

    /** 入力時のポップアップ自動表示の ON/OFF を設定する。 */
    public void setPopupOnInput(boolean popupOnInput) {
        this.popupOnInput = popupOnInput;
    }

    // === モデルアクセス ===

    @Override
    public void setModel(ComboBoxModel<E> aModel) {
        if (!(aModel instanceof FilterableComboBoxModel)) {
            throw new IllegalArgumentException(
                    "AutoCompleteComboBox requires FilterableComboBoxModel");
        }
        super.setModel(aModel);
    }

    @Override
    public void setEditable(boolean aFlag) {
        if (!aFlag) {
            throw new UnsupportedOperationException(
                    "AutoCompleteComboBox must remain editable");
        }
        super.setEditable(aFlag);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FilterableComboBoxModel<E> getModel() {
        return (FilterableComboBoxModel<E>) super.getModel();
    }

    // === DocumentListener 制御 ===

    /**
     * DocumentListener を一時的に取り外す。
     * プログラムからテキストやモデルを変更する前に呼び出し、
     * イベントの連鎖を確実に防止する。
     */
    private void suspendDocumentListener() {
        if (!listenerSuspended && documentListener != null) {
            JTextComponent editor = getEditorTextComponent();
            if (editor != null) {
                editor.getDocument().removeDocumentListener(documentListener);
            }
            listenerSuspended = true;
        }
    }

    /**
     * 取り外した DocumentListener を再登録する。
     */
    private void resumeDocumentListener() {
        if (listenerSuspended && documentListener != null) {
            JTextComponent editor = getEditorTextComponent();
            if (editor != null) {
                editor.getDocument().addDocumentListener(documentListener);
            }
            listenerSuspended = false;
        }
    }

    /**
     * {@code invokeLater} で待機中の {@code onTextChanged} を無効化する。
     */
    private void cancelPendingTextChange() {
        textChangeGeneration++;
    }

    // === JComboBox オーバーライド ===

    /**
     * ポップアップを閉じる。
     * <p>
     * 外部トリガー（マウスクリック選択など）の場合はフィルタクリアと
     * エディタテキストの確定を行う。内部操作中（リスナー停止中）の場合は
     * クリーンアップをスキップする。
     */
    @Override
    public void hidePopup() {
        super.hidePopup();
        if (!listenerSuspended) {
            // 外部呼び出し: マウスクリック選択やフォーカス喪失など
            cancelPendingTextChange();
            Object selected = navigating ? getModel().getSelectedItem() : null;
            navigating = false;
            suspendDocumentListener();
            try {
                getModel().clearFilter();
                if (selected != null) {
                    JTextComponent editor = getEditorTextComponent();
                    if (editor != null) {
                        editor.setText(convertToString(selected));
                        editor.setCaretPosition(editor.getText().length());
                    }
                }
            } finally {
                resumeDocumentListener();
            }
        }
    }

    /**
     * エディタを設定する。
     * <p>
     * JComboBox はモデル変更時やポップアップ操作時にこのメソッドを内部的に呼び出す。
     * リスナー停止中（プログラムからの操作中）またはポップアップナビゲート中は
     * エディタテキストの更新をスキップし、ユーザーの入力テキストを保持する。
     */
    @Override
    public void configureEditor(ComboBoxEditor anEditor, Object anItem) {
        if (listenerSuspended || navigating) {
            return;
        }
        anEditor.setItem(convertToString(anItem));
    }

    @Override
    public void updateUI() {
        uninstallAutoComplete();
        super.updateUI();
        installRenderer();
        if (isEditable()) {
            installAutoComplete();
        }
    }

    // === 表示変換 ===

    @SuppressWarnings("unchecked")
    private String convertToString(Object item) {
        if (item == null) {
            return "";
        }
        try {
            return stringConverter.apply((E) item);
        } catch (ClassCastException e) {
            return item.toString();
        }
    }

    private void installRenderer() {
        @SuppressWarnings("serial")
        var renderer = new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(
                        list, convertToString(value), index,
                        isSelected, cellHasFocus);
            }
        };
        setRenderer(renderer);
    }

    // === リスナー管理 ===

    private void installAutoComplete() {
        JTextComponent editor = getEditorTextComponent();
        if (editor == null) {
            return;
        }

        documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onDocumentChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onDocumentChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // プレーンテキストでは発生しない
            }
        };
        editor.getDocument().addDocumentListener(documentListener);

        keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                onKeyPressed(e);
            }
        };
        editor.addKeyListener(keyListener);

        autoCompleteFocusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                editor.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!e.isTemporary()) {
                    confirmInput();
                }
            }
        };
        editor.addFocusListener(autoCompleteFocusListener);
    }

    private void uninstallAutoComplete() {
        JTextComponent editor = getEditorTextComponent();
        if (editor == null) {
            return;
        }
        if (documentListener != null) {
            if (!listenerSuspended) {
                editor.getDocument().removeDocumentListener(documentListener);
            }
            documentListener = null;
            listenerSuspended = false;
        }
        if (keyListener != null) {
            editor.removeKeyListener(keyListener);
            keyListener = null;
        }
        if (autoCompleteFocusListener != null) {
            editor.removeFocusListener(autoCompleteFocusListener);
            autoCompleteFocusListener = null;
        }
    }

    private JTextComponent getEditorTextComponent() {
        if (getEditor() == null) {
            return null;
        }
        java.awt.Component editorComponent = getEditor().getEditorComponent();
        if (editorComponent instanceof JTextComponent tc) {
            return tc;
        }
        return null;
    }

    // === イベントハンドリング ===

    /**
     * DocumentListener からの通知。
     * <p>
     * リスナーが登録されている状態でのみ発火するため、プログラムからの
     * テキスト変更（リスナー停止中）では呼ばれない。
     * ナビゲート中はスキップする。
     * <p>
     * DocumentListener のコールバック中はドキュメントを変更できないため、
     * {@code invokeLater} で遅延実行する。世代番号により、より新しい操作で
     * 無効化された場合は実行をスキップする。
     */
    private void onDocumentChange() {
        if (navigating) {
            return;
        }
        int gen = ++textChangeGeneration;
        SwingUtilities.invokeLater(() -> {
            if (gen == textChangeGeneration) {
                onTextChanged();
            }
        });
    }

    /**
     * テキスト変更後のメイン処理。フィルタリング、インライン補完、ポップアップ制御を行う。
     */
    private void onTextChanged() {
        JTextComponent editor = getEditorTextComponent();
        if (editor == null) {
            return;
        }

        suspendDocumentListener();
        try {
            String userInput = getUserInput(editor);

            if (filterEnabled) {
                getModel().applyFilter(matcher, userInput);
                // applyFilter のモデル変更で JComboBox が configureEditor を呼ぶが、
                // listenerSuspended == true なのでスキップされる
                editor.setText(userInput);
                editor.setCaretPosition(userInput.length());
            }

            if (suggestEnabled && !suppressInlineCompletion && !userInput.isEmpty()) {
                String completion = findFirstPrefixMatch(userInput);
                if (completion != null) {
                    String completedText = userInput
                            + completion.substring(userInput.length());
                    editor.setText(completedText);
                    editor.setCaretPosition(completedText.length());
                    editor.moveCaretPosition(userInput.length());
                }
            }

            if (popupOnInput) {
                if (getModel().getSize() > 0 && !userInput.isEmpty()) {
                    // 候補数の変更に対応するため、ポップアップを常に再表示する
                    if (isPopupVisible()) {
                        super.hidePopup();
                    }
                    showPopup();
                } else {
                    if (isPopupVisible()) {
                        super.hidePopup();
                    }
                }
            }
        } finally {
            suppressInlineCompletion = false;
            resumeDocumentListener();
        }
    }

    /**
     * フィルタ済み候補の中からユーザー入力に前方一致する最初のアイテムを探す。
     * インライン補完はマッチャーの種類に関わらず前方一致でのみ成立するため、
     * フィルタ結果をさらに前方一致で絞り込む。
     */
    private String findFirstPrefixMatch(String userInput) {
        FilterableComboBoxModel<E> model = getModel();
        int len = userInput.length();
        for (int i = 0; i < model.getSize(); i++) {
            E item = model.getElementAt(i);
            if (item != null) {
                String text = stringConverter.apply(item);
                if (text != null && text.length() > len
                        && text.regionMatches(suggestIgnoreCase, 0, userInput, 0, len)) {
                    return text;
                }
            }
        }
        return null;
    }

    /**
     * エディタテキストからユーザーが実際に入力した部分のみを取得する。
     * インライン補完で選択状態になっている部分は除外する。
     */
    private String getUserInput(JTextComponent editor) {
        String text = editor.getText();
        if (text == null) {
            return "";
        }
        int selStart = editor.getSelectionStart();
        if (selStart < text.length() && editor.getSelectionEnd() == text.length()) {
            return text.substring(0, selStart);
        }
        return text;
    }

    // === キー入力ハンドリング ===

    private void onKeyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER -> handleEnter(e);
            case KeyEvent.VK_ESCAPE -> handleEscape(e);
            case KeyEvent.VK_DOWN, KeyEvent.VK_UP -> handleNavigation();
            case KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE -> {
                navigating = false;
                suppressInlineCompletion = true;
            }
            default -> navigating = false;
        }
    }

    /**
     * Enter キー: ポップアップ表示中はハイライト中のアイテムを確定、
     * 非表示時は現在のテキストを確定する。
     */
    private void handleEnter(KeyEvent e) {
        if (isPopupVisible()) {
            Object selected = getModel().getSelectedItem();
            cancelPendingTextChange();
            navigating = false;
            suspendDocumentListener();
            try {
                super.hidePopup();
                getModel().clearFilter();
                JTextComponent editor = getEditorTextComponent();
                if (editor != null) {
                    editor.setText(convertToString(selected));
                    editor.setCaretPosition(editor.getText().length());
                }
            } finally {
                resumeDocumentListener();
            }
            e.consume();
        } else {
            confirmInput();
        }
    }

    /**
     * Escape キー: テキストクリア + フィルタクリア。
     * ポップアップ表示中は閉じて JComboBox の標準ハンドラを抑制する。
     */
    private void handleEscape(KeyEvent e) {
        cancelPendingTextChange();
        navigating = false;
        suspendDocumentListener();
        try {
            getModel().clearFilter();
            JTextComponent editor = getEditorTextComponent();
            if (editor != null) {
                editor.setText("");
            }
            setSelectedItem(null);
        } finally {
            resumeDocumentListener();
        }
        if (isPopupVisible()) {
            super.hidePopup();
            e.consume();
        }
    }

    /**
     * 上下矢印キー: ポップアップが閉じている場合は開く。
     * ポップアップ表示中はナビゲーションモードに入る。
     */
    private void handleNavigation() {
        if (!isPopupVisible()) {
            if (!filterEnabled) {
                suspendDocumentListener();
                try {
                    getModel().clearFilter();
                } finally {
                    resumeDocumentListener();
                }
            }
            showPopup();
        }
        navigating = isPopupVisible();
    }

    /**
     * 現在のエディタテキストを確定し、フィルタをクリアする。
     */
    private void confirmInput() {
        JTextComponent editor = getEditorTextComponent();
        if (editor == null) {
            return;
        }
        String text = editor.getText();
        if (text != null && !text.isEmpty()) {
            // 選択テキストがある場合は全文を確定
            editor.setCaretPosition(text.length());
        }
        cancelPendingTextChange();
        suspendDocumentListener();
        try {
            getModel().clearFilter();
        } finally {
            resumeDocumentListener();
        }
    }
}

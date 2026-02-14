package org.to0mi1.swuit.component.autocomplete;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

/**
 * フィルタリング可能な {@link MutableComboBoxModel} 実装。
 * <p>
 * 全アイテム ({@code allItems}) とフィルタ済みアイテム ({@code filteredItems}) を分離管理し、
 * {@link #applyFilter(AutoCompleteMatcher, String)} でドロップダウン候補を絞り込む。
 *
 * @param <E> 候補アイテムの型
 */
public class FilterableComboBoxModel<E> extends AbstractListModel<E>
        implements MutableComboBoxModel<E> {

    private final List<E> allItems = new ArrayList<>();
    private final List<E> filteredItems = new ArrayList<>();
    private Object selectedItem;
    private boolean filtered;

    /** 空のモデルを生成する。 */
    public FilterableComboBoxModel() {
    }

    /**
     * 初期アイテムを指定してモデルを生成する。
     *
     * @param items 初期アイテム
     */
    public FilterableComboBoxModel(List<E> items) {
        allItems.addAll(items);
        filteredItems.addAll(items);
    }

    // === ComboBoxModel ===

    @Override
    public void setSelectedItem(Object anItem) {
        if ((selectedItem != null && !selectedItem.equals(anItem))
                || (selectedItem == null && anItem != null)) {
            selectedItem = anItem;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    // === ListModel ===

    @Override
    public int getSize() {
        return filteredItems.size();
    }

    @Override
    public E getElementAt(int index) {
        return filteredItems.get(index);
    }

    // === MutableComboBoxModel ===

    @Override
    public void addElement(E item) {
        allItems.add(item);
        if (!filtered) {
            filteredItems.add(item);
            fireIntervalAdded(this, filteredItems.size() - 1, filteredItems.size() - 1);
        }
    }

    @Override
    public void removeElement(Object obj) {
        int filteredIndex = filteredItems.indexOf(obj);
        allItems.remove(obj);
        if (filteredIndex >= 0) {
            filteredItems.remove(filteredIndex);
            fireIntervalRemoved(this, filteredIndex, filteredIndex);
        }
        if (obj != null && obj.equals(selectedItem)) {
            selectedItem = null;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public void insertElementAt(E item, int index) {
        if (filtered) {
            allItems.add(item);
        } else {
            allItems.add(index, item);
            filteredItems.add(index, item);
            fireIntervalAdded(this, index, index);
        }
    }

    @Override
    public void removeElementAt(int index) {
        E item = filteredItems.get(index);
        // 参照一致を優先して allItems から削除（重複要素への対応）
        int allIndex = indexOfByIdentity(allItems, item);
        if (allIndex >= 0) {
            allItems.remove(allIndex);
        } else {
            allItems.remove(item);
        }
        filteredItems.remove(index);
        fireIntervalRemoved(this, index, index);
        if (item != null && item.equals(selectedItem)) {
            selectedItem = null;
            fireContentsChanged(this, -1, -1);
        }
    }

    private static <T> int indexOfByIdentity(List<T> list, T target) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    // === フィルタリング ===

    /**
     * フィルタが適用されているかを返す。
     *
     * @return フィルタ適用中なら {@code true}
     */
    public boolean isFiltered() {
        return filtered;
    }

    /**
     * 指定のマッチャーと入力テキストでフィルタを適用する。
     *
     * @param matcher   マッチング戦略
     * @param inputText ユーザー入力テキスト
     */
    public void applyFilter(AutoCompleteMatcher<E> matcher, String inputText) {
        int oldSize = filteredItems.size();
        filteredItems.clear();
        if (oldSize > 0) {
            fireIntervalRemoved(this, 0, oldSize - 1);
        }

        for (E item : allItems) {
            if (matcher.matches(item, inputText)) {
                filteredItems.add(item);
            }
        }

        if (!filteredItems.isEmpty()) {
            fireIntervalAdded(this, 0, filteredItems.size() - 1);
        }
        filtered = true;
    }

    /** フィルタを解除し、全アイテムを表示する。 */
    public void clearFilter() {
        int oldSize = filteredItems.size();
        filteredItems.clear();
        if (oldSize > 0) {
            fireIntervalRemoved(this, 0, oldSize - 1);
        }
        filteredItems.addAll(allItems);
        if (!filteredItems.isEmpty()) {
            fireIntervalAdded(this, 0, filteredItems.size() - 1);
        }
        filtered = false;
    }

    /**
     * フィルタ済みリストの最初のアイテムを返す。
     * インライン補完用。
     *
     * @return 最初のフィルタ済みアイテム、または候補がない場合は {@code null}
     */
    public E getFirstFilteredItem() {
        return filteredItems.isEmpty() ? null : filteredItems.get(0);
    }

    /**
     * 全アイテム数を返す（フィルタ前）。
     *
     * @return 全アイテム数
     */
    public int getAllItemsCount() {
        return allItems.size();
    }
}

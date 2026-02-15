package org.to0mi1.swuit.component.recycler;

import org.to0mi1.swuit.layout.Orientation;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 一列配置のレイアウトマネージャー。
 * <p>
 * 垂直方向または水平方向にアイテムを一列に配置する。
 * 可視範囲のアイテムのみ ViewHolder を生成し、効率的にレイアウトする。
 */
public class LinearLayoutManager extends RecyclerPane.LayoutManager {

    private Orientation orientation;
    private int gap;
    private boolean reverseLayout;
    private boolean stackFromEnd;

    /** position → 計測済みサイズ（主軸方向）のキャッシュ */
    private final Map<Integer, Integer> sizeCache = new HashMap<>();

    /** 推定アイテムサイズ（キャッシュがない場合に使う） */
    private int estimatedItemSize = -1;

    /** 垂直方向、gap=0 で生成する。 */
    public LinearLayoutManager() {
        this(Orientation.VERTICAL, 0);
    }

    /** 指定方向、gap=0 で生成する。 */
    public LinearLayoutManager(Orientation orientation) {
        this(orientation, 0);
    }

    /** 指定方向、指定 gap で生成する。 */
    public LinearLayoutManager(Orientation orientation, int gap) {
        this.orientation = orientation;
        this.gap = gap;
    }

    /** 方向を設定する。 */
    public LinearLayoutManager setOrientation(Orientation orientation) {
        this.orientation = orientation;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** 方向を返す。 */
    public Orientation getOrientation() {
        return orientation;
    }

    /** アイテム間の隙間を設定する。 */
    public LinearLayoutManager setGap(int gap) {
        this.gap = gap;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** アイテム間の隙間を返す。 */
    public int getGap() {
        return gap;
    }

    /** 逆順配置を設定する。true の場合 position 0 を末尾に配置する。 */
    public LinearLayoutManager setReverseLayout(boolean reverseLayout) {
        this.reverseLayout = reverseLayout;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** 逆順配置かどうかを返す。 */
    public boolean getReverseLayout() {
        return reverseLayout;
    }

    /** 末尾揃えを設定する。コンテンツがビューポートより小さい場合に末尾に揃える。 */
    public LinearLayoutManager setStackFromEnd(boolean stackFromEnd) {
        this.stackFromEnd = stackFromEnd;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** 末尾揃えかどうかを返す。 */
    public boolean getStackFromEnd() {
        return stackFromEnd;
    }

    /** レイアウトインデックスからアダプタポジションへ変換する。 */
    private int toPosition(int layoutIndex, int itemCount) {
        return reverseLayout ? (itemCount - 1 - layoutIndex) : layoutIndex;
    }

    @Override
    public void onLayoutChildren(Recycler recycler, RecyclerPane.State state) {
        int itemCount = state.getItemCount();
        if (itemCount == 0) {
            resetVisiblePositions();
            recycler.scrapAttachedViews();
            recycler.recycleScrap();
            return;
        }

        recycler.scrapAttachedViews();

        if (orientation == Orientation.VERTICAL) {
            layoutVertical(recycler, state);
        } else {
            layoutHorizontal(recycler, state);
        }

        recycler.recycleScrap();
    }

    private void layoutVertical(Recycler recycler, RecyclerPane.State state) {
        int viewportWidth = getWidth();
        int viewportHeight = getHeight();
        int scrollOffset = getScrollOffsetY();
        int itemCount = state.getItemCount();

        // 各アイテムの開始Y座標を計算（キャッシュ済みサイズまたは推定値を使用）
        int y = 0;
        int firstVisible = -1;
        int firstVisibleY = 0;

        for (int li = 0; li < itemCount; li++) {
            int pos = toPosition(li, itemCount);
            Insets offsets = getItemDecorationsOffsets(pos);
            int itemHeight = getCachedOrEstimatedSize(pos) + offsets.top + offsets.bottom;
            if (firstVisible < 0 && y + itemHeight > scrollOffset) {
                firstVisible = li;
                firstVisibleY = y;
            }
            if (firstVisible >= 0 && y > scrollOffset + viewportHeight) {
                break; // 可視範囲の終端を過ぎたら打ち切り
            }
            y += itemHeight;
            if (li < itemCount - 1) {
                y += gap;
            }
        }

        if (firstVisible < 0) {
            resetVisiblePositions();
            return;
        }

        // 可視アイテムを配置し、可視位置を追跡
        resetVisiblePositions();
        y = firstVisibleY;
        for (int li = firstVisible; li < itemCount; li++) {
            if (y > scrollOffset + viewportHeight) {
                break;
            }
            int pos = toPosition(li, itemCount);
            Insets offsets = getItemDecorationsOffsets(pos);
            RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(pos);
            int itemHeight = holder.itemView.getPreferredSize().height;
            // 計測サイズをキャッシュ
            sizeCache.put(pos, itemHeight);
            updateEstimatedSize(itemHeight);

            int itemTop = y + offsets.top;
            int itemBottom = itemTop + itemHeight;

            layoutChild(holder,
                    offsets.left,
                    itemTop,
                    viewportWidth - offsets.left - offsets.right,
                    itemHeight);

            // 可視位置を更新
            updateVisiblePositions(pos, itemTop, itemBottom, scrollOffset, scrollOffset + viewportHeight);

            y += itemHeight + offsets.top + offsets.bottom;
            if (li < itemCount - 1) {
                y += gap;
            }
        }

        // stackFromEnd: 計測済みサイズでコンテンツ < ビューポートなら下詰めにシフト
        if (stackFromEnd && scrollOffset == 0) {
            int totalHeight = computeTotalMainAxisSize(itemCount);
            if (totalHeight < viewportHeight) {
                int shift = viewportHeight - totalHeight;
                shiftVisibleChildren(0, shift);
            }
        }
    }

    private void layoutHorizontal(Recycler recycler, RecyclerPane.State state) {
        int viewportWidth = getWidth();
        int viewportHeight = getHeight();
        int scrollOffset = getScrollOffsetX();
        int itemCount = state.getItemCount();

        int x = 0;
        int firstVisible = -1;
        int firstVisibleX = 0;

        for (int li = 0; li < itemCount; li++) {
            int pos = toPosition(li, itemCount);
            Insets offsets = getItemDecorationsOffsets(pos);
            int itemWidth = getCachedOrEstimatedSize(pos) + offsets.left + offsets.right;
            if (firstVisible < 0 && x + itemWidth > scrollOffset) {
                firstVisible = li;
                firstVisibleX = x;
            }
            if (firstVisible >= 0 && x > scrollOffset + viewportWidth) {
                break;
            }
            x += itemWidth;
            if (li < itemCount - 1) {
                x += gap;
            }
        }

        if (firstVisible < 0) {
            resetVisiblePositions();
            return;
        }

        resetVisiblePositions();
        x = firstVisibleX;
        for (int li = firstVisible; li < itemCount; li++) {
            if (x > scrollOffset + viewportWidth) {
                break;
            }
            int pos = toPosition(li, itemCount);
            Insets offsets = getItemDecorationsOffsets(pos);
            RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(pos);
            int itemWidth = holder.itemView.getPreferredSize().width;
            sizeCache.put(pos, itemWidth);
            updateEstimatedSize(itemWidth);

            int itemLeft = x + offsets.left;
            int itemRight = itemLeft + itemWidth;

            layoutChild(holder,
                    itemLeft,
                    offsets.top,
                    itemWidth,
                    viewportHeight - offsets.top - offsets.bottom);

            updateVisiblePositions(pos, itemLeft, itemRight, scrollOffset, scrollOffset + viewportWidth);

            x += itemWidth + offsets.left + offsets.right;
            if (li < itemCount - 1) {
                x += gap;
            }
        }

        // stackFromEnd: 計測済みサイズでコンテンツ < ビューポートなら右詰めにシフト
        if (stackFromEnd && scrollOffset == 0) {
            int totalWidth = computeTotalMainAxisSize(itemCount);
            if (totalWidth < viewportWidth) {
                int shift = viewportWidth - totalWidth;
                shiftVisibleChildren(shift, 0);
            }
        }
    }

    /** 可視位置情報を更新する。 */
    private void updateVisiblePositions(int position, int itemStart, int itemEnd,
                                        int viewStart, int viewEnd) {
        // 部分でも見えていれば visible（min/max で追跡し reverseLayout にも対応）
        if (firstVisibleItemPosition == -1 || position < firstVisibleItemPosition) {
            firstVisibleItemPosition = position;
        }
        if (lastVisibleItemPosition == -1 || position > lastVisibleItemPosition) {
            lastVisibleItemPosition = position;
        }

        // 完全に見えていれば completely visible
        if (itemStart >= viewStart && itemEnd <= viewEnd) {
            if (firstCompletelyVisibleItemPosition == -1 || position < firstCompletelyVisibleItemPosition) {
                firstCompletelyVisibleItemPosition = position;
            }
            if (lastCompletelyVisibleItemPosition == -1 || position > lastCompletelyVisibleItemPosition) {
                lastCompletelyVisibleItemPosition = position;
            }
        }
    }

    /** 可視アイテムを指定量シフトする。 */
    private void shiftVisibleChildren(int dx, int dy) {
        for (int i = 0; i < recyclerPane.getComponentCount(); i++) {
            Component comp = recyclerPane.getComponent(i);
            if (comp.isVisible()) {
                comp.setLocation(comp.getX() + dx, comp.getY() + dy);
            }
        }
    }

    /** 主軸方向の合計サイズを計算する（stackFromEnd 用）。 */
    private int computeTotalMainAxisSize(int itemCount) {
        int total = 0;
        for (int li = 0; li < itemCount; li++) {
            int pos = toPosition(li, itemCount);
            Insets offsets = getItemDecorationsOffsets(pos);
            if (orientation == Orientation.VERTICAL) {
                total += getCachedOrEstimatedSize(pos) + offsets.top + offsets.bottom;
            } else {
                total += getCachedOrEstimatedSize(pos) + offsets.left + offsets.right;
            }
            if (li < itemCount - 1) {
                total += gap;
            }
        }
        return total;
    }

    @Override
    public void scrollToPosition(int position) {
        scrollToPositionWithOffset(position, 0);
    }

    @Override
    public void scrollToPositionWithOffset(int position, int offset) {
        if (recyclerPane == null) return;
        if (!(recyclerPane.getParent() instanceof JViewport)) return;

        int itemCount = recyclerPane.getAdapter() != null ? recyclerPane.getAdapter().getItemCount() : 0;
        if (position < 0 || position >= itemCount) return;

        // position のレイアウトインデックスを計算
        int targetLayoutIndex = reverseLayout ? (itemCount - 1 - position) : position;

        // targetLayoutIndex までの累積サイズを計算
        int accumulated = 0;
        if (stackFromEnd) {
            int viewportSize = orientation == Orientation.VERTICAL ? getHeight() : getWidth();
            int totalSize = computeTotalMainAxisSize(itemCount);
            if (totalSize < viewportSize) {
                accumulated = viewportSize - totalSize;
            }
        }

        for (int li = 0; li < targetLayoutIndex; li++) {
            int pos = toPosition(li, itemCount);
            Insets offsets = getItemDecorationsOffsets(pos);
            if (orientation == Orientation.VERTICAL) {
                accumulated += getCachedOrEstimatedSize(pos) + offsets.top + offsets.bottom;
            } else {
                accumulated += getCachedOrEstimatedSize(pos) + offsets.left + offsets.right;
            }
            accumulated += gap;
        }

        int scrollPos = Math.max(0, accumulated - offset);

        if (orientation == Orientation.VERTICAL) {
            recyclerPane.scrollViewportAndLayout(new Point(0, scrollPos));
        } else {
            recyclerPane.scrollViewportAndLayout(new Point(scrollPos, 0));
        }
    }

    /** キャッシュ済みサイズまたは推定値を返す。 */
    private int getCachedOrEstimatedSize(int position) {
        Integer cached = sizeCache.get(position);
        if (cached != null) {
            return cached;
        }
        return estimatedItemSize > 0 ? estimatedItemSize : 30; // デフォルト推定値
    }

    /** 推定アイテムサイズを更新する（計測値の移動平均）。 */
    private void updateEstimatedSize(int measuredSize) {
        if (estimatedItemSize < 0) {
            estimatedItemSize = measuredSize;
        } else {
            estimatedItemSize = (estimatedItemSize + measuredSize) / 2;
        }
    }

    @Override
    public boolean canScrollVertically() {
        return orientation == Orientation.VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return orientation == Orientation.HORIZONTAL;
    }

    @Override
    public Dimension computeTotalSize(RecyclerPane.State state) {
        int itemCount = state.getItemCount();
        if (itemCount == 0) {
            return new Dimension(0, 0);
        }

        if (orientation == Orientation.VERTICAL) {
            int totalHeight = 0;
            for (int i = 0; i < itemCount; i++) {
                Insets offsets = getItemDecorationsOffsets(i);
                totalHeight += getCachedOrEstimatedSize(i) + offsets.top + offsets.bottom;
                if (i < itemCount - 1) {
                    totalHeight += gap;
                }
            }
            return new Dimension(getWidth(), totalHeight);
        } else {
            int totalWidth = 0;
            for (int i = 0; i < itemCount; i++) {
                Insets offsets = getItemDecorationsOffsets(i);
                totalWidth += getCachedOrEstimatedSize(i) + offsets.left + offsets.right;
                if (i < itemCount - 1) {
                    totalWidth += gap;
                }
            }
            return new Dimension(totalWidth, getHeight());
        }
    }

    @Override
    protected void onDataChanged() {
        clearSizeCache();
    }

    /** サイズキャッシュをクリアする。 */
    private void clearSizeCache() {
        sizeCache.clear();
        estimatedItemSize = -1;
    }
}

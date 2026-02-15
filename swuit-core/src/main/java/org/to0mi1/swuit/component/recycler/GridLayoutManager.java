package org.to0mi1.swuit.component.recycler;

import org.to0mi1.swuit.layout.Orientation;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * グリッド配置のレイアウトマネージャー。
 * <p>
 * アイテムを指定列数（垂直時）または行数（水平時）のグリッドに配置する。
 * {@link SpanSizeLookup} で各アイテムの占有列数を変更できる。
 */
public class GridLayoutManager extends RecyclerPane.LayoutManager {

    private int spanCount;
    private Orientation orientation;
    private int mainAxisGap;
    private int crossAxisGap;
    private SpanSizeLookup spanSizeLookup;
    private boolean reverseLayout;

    /** position → 計測済みサイズ（主軸方向）のキャッシュ */
    private final Map<Integer, Integer> sizeCache = new HashMap<>();
    private int estimatedItemSize = -1;

    /** 垂直方向、指定列数で生成する。 */
    public GridLayoutManager(int spanCount) {
        this(spanCount, Orientation.VERTICAL);
    }

    /** 指定列数、指定方向で生成する。 */
    public GridLayoutManager(int spanCount, Orientation orientation) {
        if (spanCount < 1) {
            throw new IllegalArgumentException("spanCount must be >= 1, was " + spanCount);
        }
        this.spanCount = spanCount;
        this.orientation = orientation;
    }

    /** 列数を設定する。 */
    public GridLayoutManager setSpanCount(int spanCount) {
        if (spanCount < 1) {
            throw new IllegalArgumentException("spanCount must be >= 1, was " + spanCount);
        }
        this.spanCount = spanCount;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** 列数を返す。 */
    public int getSpanCount() {
        return spanCount;
    }

    /** 方向を設定する。 */
    public GridLayoutManager setOrientation(Orientation orientation) {
        this.orientation = orientation;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** 方向を返す。 */
    public Orientation getOrientation() {
        return orientation;
    }

    /** 主軸方向のギャップを設定する。 */
    public GridLayoutManager setMainAxisGap(int gap) {
        this.mainAxisGap = gap;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** 主軸方向のギャップを返す。 */
    public int getMainAxisGap() {
        return mainAxisGap;
    }

    /** 交差軸方向のギャップを設定する。 */
    public GridLayoutManager setCrossAxisGap(int gap) {
        this.crossAxisGap = gap;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** 交差軸方向のギャップを返す。 */
    public int getCrossAxisGap() {
        return crossAxisGap;
    }

    /** SpanSizeLookup を設定する。 */
    public GridLayoutManager setSpanSizeLookup(SpanSizeLookup lookup) {
        this.spanSizeLookup = lookup;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** SpanSizeLookup を返す。 */
    public SpanSizeLookup getSpanSizeLookup() {
        return spanSizeLookup;
    }

    /** 逆順配置を設定する。true の場合、行の並び順を反転する。 */
    public GridLayoutManager setReverseLayout(boolean reverseLayout) {
        this.reverseLayout = reverseLayout;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** 逆順配置かどうかを返す。 */
    public boolean getReverseLayout() {
        return reverseLayout;
    }

    /** 指定 position のスパンサイズを返す。 */
    private int getSpanSize(int position) {
        if (spanSizeLookup != null) {
            return Math.min(spanSizeLookup.getSpanSize(position), spanCount);
        }
        return 1;
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

        if (viewportWidth <= 0) return;

        // 列幅の計算
        int totalCrossGap = crossAxisGap * (spanCount - 1);
        int columnWidth = (viewportWidth - totalCrossGap) / spanCount;

        // まず全行のアイテム範囲を計算
        int[][] rows = computeRows(itemCount);
        int rowCount = rows.length;

        if (rowCount == 0) {
            resetVisiblePositions();
            return;
        }

        // reverseLayout: 行の配置順を反転
        resetVisiblePositions();

        int y = 0;
        boolean passedVisible = false;

        for (int ri = 0; ri < rowCount; ri++) {
            int rowIndex = reverseLayout ? (rowCount - 1 - ri) : ri;
            int rowStart = rows[rowIndex][0];
            int rowEnd = rows[rowIndex][1];

            int rowHeight;
            boolean isVisible = (y + getCachedOrEstimatedRowHeight(rowStart, rowEnd) > scrollOffset)
                    && (y < scrollOffset + viewportHeight);

            if (isVisible) {
                passedVisible = true;
                rowHeight = 0;
                int spanOffset = 0;
                for (int i = rowStart; i < rowEnd; i++) {
                    int span = getSpanSize(i);
                    Insets offsets = getItemDecorationsOffsets(i);
                    RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(i);
                    int itemWidth = columnWidth * span + crossAxisGap * (span - 1);
                    int itemHeight = holder.itemView.getPreferredSize().height;
                    sizeCache.put(i, itemHeight);
                    updateEstimatedSize(itemHeight);
                    rowHeight = Math.max(rowHeight, itemHeight + offsets.top + offsets.bottom);

                    int itemX = spanOffset * (columnWidth + crossAxisGap) + offsets.left;
                    int itemTop = y + offsets.top;

                    layoutChild(holder,
                            itemX,
                            itemTop,
                            itemWidth - offsets.left - offsets.right,
                            itemHeight);

                    updateVisiblePositions(i, itemTop, itemTop + itemHeight, scrollOffset, scrollOffset + viewportHeight);
                    spanOffset += span;
                }
            } else {
                rowHeight = getCachedOrEstimatedRowHeight(rowStart, rowEnd);
            }

            y += rowHeight;
            if (ri < rowCount - 1) {
                y += mainAxisGap;
            }

            if (passedVisible && y > scrollOffset + viewportHeight) {
                break;
            }
        }
    }

    private void layoutHorizontal(Recycler recycler, RecyclerPane.State state) {
        int viewportWidth = getWidth();
        int viewportHeight = getHeight();
        int scrollOffset = getScrollOffsetX();
        int itemCount = state.getItemCount();

        if (viewportHeight <= 0) return;

        int totalCrossGap = crossAxisGap * (spanCount - 1);
        int rowHeight = (viewportHeight - totalCrossGap) / spanCount;

        int[][] cols = computeRows(itemCount);
        int colCount = cols.length;

        if (colCount == 0) {
            resetVisiblePositions();
            return;
        }

        resetVisiblePositions();

        int x = 0;
        boolean passedVisible = false;

        for (int ci = 0; ci < colCount; ci++) {
            int colIndex = reverseLayout ? (colCount - 1 - ci) : ci;
            int colStart = cols[colIndex][0];
            int colEnd = cols[colIndex][1];

            int colWidth;
            boolean isVisible = (x + getCachedOrEstimatedColWidth(colStart, colEnd) > scrollOffset)
                    && (x < scrollOffset + viewportWidth);

            if (isVisible) {
                passedVisible = true;
                colWidth = 0;
                int spanOffset = 0;
                for (int i = colStart; i < colEnd; i++) {
                    int span = getSpanSize(i);
                    Insets offsets = getItemDecorationsOffsets(i);
                    RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(i);
                    int itemHeight = rowHeight * span + crossAxisGap * (span - 1);
                    int itemWidth = holder.itemView.getPreferredSize().width;
                    sizeCache.put(i, itemWidth);
                    updateEstimatedSize(itemWidth);
                    colWidth = Math.max(colWidth, itemWidth + offsets.left + offsets.right);

                    int itemY = spanOffset * (rowHeight + crossAxisGap) + offsets.top;
                    int itemLeft = x + offsets.left;

                    layoutChild(holder,
                            itemLeft,
                            itemY,
                            itemWidth,
                            itemHeight - offsets.top - offsets.bottom);

                    updateVisiblePositions(i, itemLeft, itemLeft + itemWidth, scrollOffset, scrollOffset + viewportWidth);
                    spanOffset += span;
                }
            } else {
                colWidth = getCachedOrEstimatedColWidth(colStart, colEnd);
            }

            x += colWidth;
            if (ci < colCount - 1) {
                x += mainAxisGap;
            }

            if (passedVisible && x > scrollOffset + viewportWidth) {
                break;
            }
        }
    }

    /** 行（列）ごとのアイテム範囲 [start, end) を計算する。 */
    private int[][] computeRows(int itemCount) {
        java.util.List<int[]> result = new java.util.ArrayList<>();
        int pos = 0;
        while (pos < itemCount) {
            int rowStart = pos;
            int spanUsed = 0;
            while (pos < itemCount && spanUsed + getSpanSize(pos) <= spanCount) {
                spanUsed += getSpanSize(pos);
                pos++;
            }
            if (pos == rowStart) {
                pos++;
            }
            result.add(new int[]{rowStart, pos});
        }
        return result.toArray(new int[0][]);
    }

    /** 可視位置情報を更新する。 */
    private void updateVisiblePositions(int position, int itemStart, int itemEnd,
                                        int viewStart, int viewEnd) {
        if (firstVisibleItemPosition == -1 || position < firstVisibleItemPosition) {
            firstVisibleItemPosition = position;
        }
        if (lastVisibleItemPosition == -1 || position > lastVisibleItemPosition) {
            lastVisibleItemPosition = position;
        }
        if (itemStart >= viewStart && itemEnd <= viewEnd) {
            if (firstCompletelyVisibleItemPosition == -1 || position < firstCompletelyVisibleItemPosition) {
                firstCompletelyVisibleItemPosition = position;
            }
            if (lastCompletelyVisibleItemPosition == -1 || position > lastCompletelyVisibleItemPosition) {
                lastCompletelyVisibleItemPosition = position;
            }
        }
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

        int[][] rows = computeRows(itemCount);

        // position が含まれる行を探す
        int targetRowIndex = -1;
        for (int ri = 0; ri < rows.length; ri++) {
            if (position >= rows[ri][0] && position < rows[ri][1]) {
                targetRowIndex = ri;
                break;
            }
        }
        if (targetRowIndex < 0) return;

        // reverseLayout の場合、行順が反転
        int layoutRowIndex = reverseLayout ? (rows.length - 1 - targetRowIndex) : targetRowIndex;

        // layoutRowIndex までの累積サイズを計算
        int accumulated = 0;
        for (int ri = 0; ri < layoutRowIndex; ri++) {
            int rowIndex = reverseLayout ? (rows.length - 1 - ri) : ri;
            if (orientation == Orientation.VERTICAL) {
                accumulated += getCachedOrEstimatedRowHeight(rows[rowIndex][0], rows[rowIndex][1]);
            } else {
                accumulated += getCachedOrEstimatedColWidth(rows[rowIndex][0], rows[rowIndex][1]);
            }
            accumulated += mainAxisGap;
        }

        int scrollPos = Math.max(0, accumulated - offset);

        if (orientation == Orientation.VERTICAL) {
            recyclerPane.scrollViewportAndLayout(new Point(0, scrollPos));
        } else {
            recyclerPane.scrollViewportAndLayout(new Point(scrollPos, 0));
        }
    }

    private int getCachedOrEstimatedRowHeight(int from, int to) {
        int maxHeight = 0;
        for (int i = from; i < to; i++) {
            Integer cached = sizeCache.get(i);
            int h = (cached != null) ? cached : (estimatedItemSize > 0 ? estimatedItemSize : 30);
            Insets offsets = getItemDecorationsOffsets(i);
            maxHeight = Math.max(maxHeight, h + offsets.top + offsets.bottom);
        }
        return maxHeight > 0 ? maxHeight : (estimatedItemSize > 0 ? estimatedItemSize : 30);
    }

    private int getCachedOrEstimatedColWidth(int from, int to) {
        int maxWidth = 0;
        for (int i = from; i < to; i++) {
            Integer cached = sizeCache.get(i);
            int w = (cached != null) ? cached : (estimatedItemSize > 0 ? estimatedItemSize : 30);
            Insets offsets = getItemDecorationsOffsets(i);
            maxWidth = Math.max(maxWidth, w + offsets.left + offsets.right);
        }
        return maxWidth > 0 ? maxWidth : (estimatedItemSize > 0 ? estimatedItemSize : 30);
    }

    private void updateEstimatedSize(int measuredSize) {
        if (estimatedItemSize < 0) {
            estimatedItemSize = measuredSize;
        } else {
            estimatedItemSize = (estimatedItemSize + measuredSize) / 2;
        }
    }

    @Override
    protected void onDataChanged() {
        clearSizeCache();
    }

    private void clearSizeCache() {
        sizeCache.clear();
        estimatedItemSize = -1;
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

        int[][] rows = computeRows(itemCount);

        if (orientation == Orientation.VERTICAL) {
            int totalHeight = 0;
            for (int ri = 0; ri < rows.length; ri++) {
                totalHeight += getCachedOrEstimatedRowHeight(rows[ri][0], rows[ri][1]);
                if (ri < rows.length - 1) {
                    totalHeight += mainAxisGap;
                }
            }
            return new Dimension(getWidth(), totalHeight);
        } else {
            int totalWidth = 0;
            for (int ci = 0; ci < rows.length; ci++) {
                totalWidth += getCachedOrEstimatedColWidth(rows[ci][0], rows[ci][1]);
                if (ci < rows.length - 1) {
                    totalWidth += mainAxisGap;
                }
            }
            return new Dimension(totalWidth, getHeight());
        }
    }

    /**
     * 各アイテムのスパンサイズを定義するための抽象クラス。
     */
    public static abstract class SpanSizeLookup {
        /**
         * 指定位置のアイテムが占有するスパン数を返す。
         *
         * @param position アイテム位置
         * @return スパン数（1 以上 spanCount 以下）
         */
        public abstract int getSpanSize(int position);

        /**
         * 指定位置のアイテムが開始するスパンインデックスを返す。
         * <p>
         * あるアイテムの span が現在の行の残りに収まらない場合、次の行に送る。
         *
         * @param position  アイテム位置
         * @param spanCount 総スパン数
         * @return スパンインデックス（0 以上 spanCount 未満）
         */
        public int getSpanIndex(int position, int spanCount) {
            int spanUsed = 0;
            for (int pos = 0; pos < position; pos++) {
                int span = getSpanSize(pos);
                if (spanUsed + span > spanCount) {
                    // 現在の行に収まらない → 次の行へ
                    spanUsed = 0;
                }
                spanUsed += span;
                if (spanUsed >= spanCount) {
                    spanUsed = 0;
                }
            }
            // 対象アイテム自体が現在行に収まるかチェック
            if (spanUsed + getSpanSize(position) > spanCount) {
                return 0;
            }
            return spanUsed;
        }

        /**
         * 指定位置のアイテムが属する行（グループ）番号を返す。
         * <p>
         * あるアイテムの span が現在の行の残りに収まらない場合、次の行に送る。
         *
         * @param position  アイテム位置
         * @param spanCount 総スパン数
         * @return 行番号（0 始まり）
         */
        public int getSpanGroupIndex(int position, int spanCount) {
            int group = 0;
            int spanUsed = 0;
            for (int pos = 0; pos < position; pos++) {
                int span = getSpanSize(pos);
                if (spanUsed + span > spanCount) {
                    group++;
                    spanUsed = 0;
                }
                spanUsed += span;
                if (spanUsed == spanCount) {
                    group++;
                    spanUsed = 0;
                }
            }
            // 対象アイテム自体が現在行に収まるかチェック
            if (spanUsed + getSpanSize(position) > spanCount) {
                group++;
            }
            return group;
        }

        /** スパンインデックスキャッシュをクリアする。 */
        public void invalidateSpanIndexCache() {
            // 将来のキャッシュ実装に備えたフック
        }
    }
}

package org.to0mi1.swuit.component.recycler;

import org.to0mi1.swuit.layout.Orientation;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 不規則グリッド配置のレイアウトマネージャー。
 * <p>
 * 最も短い列にアイテムを追加する方式で、Pinterest 風の互い違いレイアウトを実現する。
 */
public class StaggeredGridLayoutManager extends RecyclerPane.LayoutManager {

    /** 隙間をそのまま残す。 */
    public static final int GAP_HANDLING_NONE = 0;
    /** 隙間を埋めるためにアイテムを再配置する。 */
    public static final int GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS = 2;

    private int spanCount;
    private Orientation orientation;
    private int mainAxisGap;
    private int crossAxisGap;
    private int gapStrategy = GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS;

    /** position → 計測済みサイズ（主軸方向）のキャッシュ */
    private final Map<Integer, Integer> sizeCache = new HashMap<>();
    private int estimatedItemSize = -1;

    /** position → 配置された列インデックス */
    private final Map<Integer, Integer> columnAssignment = new HashMap<>();

    /** onLayoutChildren で確定した合計サイズのキャッシュ（フィードバックループ防止） */
    private Dimension cachedTotalSize;

    /** 指定列数、指定方向で生成する。 */
    public StaggeredGridLayoutManager(int spanCount, Orientation orientation) {
        if (spanCount < 1) {
            throw new IllegalArgumentException("spanCount must be >= 1, was " + spanCount);
        }
        this.spanCount = spanCount;
        this.orientation = orientation;
    }

    /** 列数を設定する。 */
    public StaggeredGridLayoutManager setSpanCount(int spanCount) {
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
    public StaggeredGridLayoutManager setOrientation(Orientation orientation) {
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
    public StaggeredGridLayoutManager setMainAxisGap(int gap) {
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
    public StaggeredGridLayoutManager setCrossAxisGap(int gap) {
        this.crossAxisGap = gap;
        clearSizeCache();
        requestLayout();
        return this;
    }

    /** 交差軸方向のギャップを返す。 */
    public int getCrossAxisGap() {
        return crossAxisGap;
    }

    /** ギャップ処理戦略を設定する。 */
    public StaggeredGridLayoutManager setGapStrategy(int gapStrategy) {
        this.gapStrategy = gapStrategy;
        requestLayout();
        return this;
    }

    /** ギャップ処理戦略を返す。 */
    public int getGapStrategy() {
        return gapStrategy;
    }

    /** スパン割り当てをクリアして再配置を要求する。 */
    public void invalidateSpanAssignments() {
        columnAssignment.clear();
        clearSizeCache();
        requestLayout();
    }

    @Override
    public void onLayoutChildren(Recycler recycler, RecyclerPane.State state) {
        int itemCount = state.getItemCount();
        if (itemCount == 0) {
            cachedTotalSize = new Dimension(0, 0);
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

        int totalCrossGap = crossAxisGap * (spanCount - 1);
        int columnWidth = (viewportWidth - totalCrossGap) / spanCount;

        // 各列の現在の高さ
        int[] columnTops = new int[spanCount];

        resetVisiblePositions();

        for (int pos = 0; pos < itemCount; pos++) {
            // 最も短い列を選択
            int col = findShortestColumn(columnTops);
            columnAssignment.put(pos, col);

            int y = columnTops[col];
            int x = col * (columnWidth + crossAxisGap);

            Insets offsets = getItemDecorationsOffsets(pos);
            int itemSize = getCachedOrEstimatedSize(pos);

            boolean isVisible = (y + itemSize + offsets.top + offsets.bottom > scrollOffset)
                    && (y < scrollOffset + viewportHeight);

            if (isVisible) {
                RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(pos);
                int itemHeight = holder.itemView.getPreferredSize().height;
                sizeCache.put(pos, itemHeight);
                updateEstimatedSize(itemHeight);

                int itemTop = y + offsets.top;
                layoutChild(holder,
                        x + offsets.left,
                        itemTop,
                        columnWidth - offsets.left - offsets.right,
                        itemHeight);

                updateVisiblePositions(pos, itemTop, itemTop + itemHeight, scrollOffset, scrollOffset + viewportHeight);

                columnTops[col] = y + itemHeight + offsets.top + offsets.bottom + mainAxisGap;
            } else {
                columnTops[col] = y + itemSize + offsets.top + offsets.bottom + mainAxisGap;
            }

            // 全列がビューポートの下を超えたら終了
            if (allColumnsExceed(columnTops, scrollOffset + viewportHeight)) {
                break;
            }
        }

        // 初回のみ確定サイズを計算してキャッシュ（フィードバックループ防止）
        if (cachedTotalSize == null) {
            cachedTotalSize = computeTotalSizeFromFullScan(itemCount, viewportWidth, 0);
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

        int[] rowLefts = new int[spanCount];

        resetVisiblePositions();

        for (int pos = 0; pos < itemCount; pos++) {
            int row = findShortestColumn(rowLefts);
            columnAssignment.put(pos, row);

            int x = rowLefts[row];
            int y = row * (rowHeight + crossAxisGap);

            Insets offsets = getItemDecorationsOffsets(pos);
            int itemSize = getCachedOrEstimatedSize(pos);

            boolean isVisible = (x + itemSize + offsets.left + offsets.right > scrollOffset)
                    && (x < scrollOffset + viewportWidth);

            if (isVisible) {
                RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(pos);
                int itemWidth = holder.itemView.getPreferredSize().width;
                sizeCache.put(pos, itemWidth);
                updateEstimatedSize(itemWidth);

                int itemLeft = x + offsets.left;
                layoutChild(holder,
                        itemLeft,
                        y + offsets.top,
                        itemWidth,
                        rowHeight - offsets.top - offsets.bottom);

                updateVisiblePositions(pos, itemLeft, itemLeft + itemWidth, scrollOffset, scrollOffset + viewportWidth);

                rowLefts[row] = x + itemWidth + offsets.left + offsets.right + mainAxisGap;
            } else {
                rowLefts[row] = x + itemSize + offsets.left + offsets.right + mainAxisGap;
            }

            if (allColumnsExceed(rowLefts, scrollOffset + viewportWidth)) {
                break;
            }
        }

        // 初回のみ確定サイズを計算してキャッシュ（フィードバックループ防止）
        if (cachedTotalSize == null) {
            cachedTotalSize = computeTotalSizeFromFullScan(itemCount, 0, viewportHeight);
        }
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

        // 配置シミュレーションで position のアイテムの座標を計算
        int viewportWidth = getWidth();
        int viewportHeight = getHeight();

        if (orientation == Orientation.VERTICAL) {
            if (viewportWidth <= 0) return;

            int[] columnTops = new int[spanCount];
            int targetY = 0;

            for (int pos = 0; pos <= position && pos < itemCount; pos++) {
                int col = findShortestColumn(columnTops);
                int y = columnTops[col];

                if (pos == position) {
                    targetY = y;
                    break;
                }

                Insets offsets = getItemDecorationsOffsets(pos);
                int itemSize = getCachedOrEstimatedSize(pos) + offsets.top + offsets.bottom;
                columnTops[col] = y + itemSize + mainAxisGap;
            }

            recyclerPane.scrollViewportAndLayout(new Point(0, Math.max(0, targetY - offset)));
        } else {
            if (viewportHeight <= 0) return;

            int[] rowLefts = new int[spanCount];
            int targetX = 0;

            for (int pos = 0; pos <= position && pos < itemCount; pos++) {
                int row = findShortestColumn(rowLefts);
                int x = rowLefts[row];

                if (pos == position) {
                    targetX = x;
                    break;
                }

                Insets offsets = getItemDecorationsOffsets(pos);
                int itemSize = getCachedOrEstimatedSize(pos) + offsets.left + offsets.right;
                rowLefts[row] = x + itemSize + mainAxisGap;
            }

            recyclerPane.scrollViewportAndLayout(new Point(Math.max(0, targetX - offset), 0));
        }
    }

    private int findShortestColumn(int[] heights) {
        int minIdx = 0;
        for (int i = 1; i < heights.length; i++) {
            if (heights[i] < heights[minIdx]) {
                minIdx = i;
            }
        }
        return minIdx;
    }

    private boolean allColumnsExceed(int[] positions, int threshold) {
        for (int pos : positions) {
            if (pos <= threshold) {
                return false;
            }
        }
        return true;
    }

    private int getCachedOrEstimatedSize(int position) {
        Integer cached = sizeCache.get(position);
        if (cached != null) {
            return cached;
        }
        return estimatedItemSize > 0 ? estimatedItemSize : 30;
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
        columnAssignment.clear();
        cachedTotalSize = null;
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
        if (cachedTotalSize != null) {
            return cachedTotalSize;
        }
        int itemCount = state.getItemCount();
        if (itemCount == 0) {
            return new Dimension(0, 0);
        }
        return computeTotalSizeFromFullScan(itemCount, getWidth(), getHeight());
    }

    /**
     * 全アイテムを走査して合計サイズを計算する。
     * onLayoutChildren の末尾から呼ばれ、結果は cachedTotalSize に保存される。
     */
    private Dimension computeTotalSizeFromFullScan(int itemCount, int viewportWidth, int viewportHeight) {
        if (orientation == Orientation.VERTICAL) {
            int[] columnHeights = new int[spanCount];
            for (int pos = 0; pos < itemCount; pos++) {
                int col = findShortestColumn(columnHeights);
                Insets offsets = getItemDecorationsOffsets(pos);
                int itemHeight = getCachedOrEstimatedSize(pos) + offsets.top + offsets.bottom;
                columnHeights[col] += itemHeight + mainAxisGap;
            }

            int maxHeight = 0;
            for (int h : columnHeights) {
                maxHeight = Math.max(maxHeight, h - mainAxisGap);
            }
            return new Dimension(viewportWidth, Math.max(0, maxHeight));
        } else {
            int[] rowWidths = new int[spanCount];
            for (int pos = 0; pos < itemCount; pos++) {
                int row = findShortestColumn(rowWidths);
                Insets offsets = getItemDecorationsOffsets(pos);
                int itemWidth = getCachedOrEstimatedSize(pos) + offsets.left + offsets.right;
                rowWidths[row] += itemWidth + mainAxisGap;
            }

            int maxWidth = 0;
            for (int w : rowWidths) {
                maxWidth = Math.max(maxWidth, w - mainAxisGap);
            }
            return new Dimension(Math.max(0, maxWidth), viewportHeight);
        }
    }
}

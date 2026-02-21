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
 *
 * <h2>サイズ推定とキャッシュの仕組み</h2>
 * <p>
 * RecyclerPane は可視領域のアイテムしか実体化しないため、非可視アイテムの主軸サイズは不明である。
 * しかしスクロールバーの長さを決めるには全アイテムの合計サイズが必要になる。
 * この矛盾を解決するため、本クラスは以下の 2 段階の仕組みを持つ。
 *
 * <ol>
 *   <li><b>アイテム単位のサイズ推定</b> —
 *       一度でも可視領域に入って実体化されたアイテムのサイズを {@link #sizeCache} に記録する。
 *       未計測アイテムには、計測済みアイテムの算術平均 ({@link #totalMeasuredSize} / 計測個数) を
 *       推定値として使う。</li>
 *   <li><b>合計サイズの段階的改善</b> —
 *       スクロールで新たなアイテムが計測されるたびに {@link #cachedTotalSize} を再計算する。
 *       再計算のトリガーには {@link #lastCacheSizeOnCompute} を使い、
 *       {@code sizeCache} が前回計算時より成長した場合にのみ再計算することで
 *       不要な計算とフィードバックループを防ぐ。</li>
 * </ol>
 *
 * <h2>データ変更時の挙動</h2>
 * <p>
 * {@code Adapter.notifyItemChanged()} 等のデータ変更通知はすべて {@link #onDataChanged()} →
 * {@link #clearSizeCache()} を通じてキャッシュを全クリアする。
 * StaggeredGrid は「最短列に順番に配置」する方式のため、途中のアイテムのサイズ変更が
 * 後続全アイテムの列割り当てに影響し得る。このため部分無効化ではなく全クリアが正しい。
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

    /**
     * position → 計測済みの主軸サイズ (VERTICAL なら高さ、HORIZONTAL なら幅) のキャッシュ。
     * アイテムが可視領域に入り実体化されたときに記録される。
     */
    private final Map<Integer, Integer> sizeCache = new HashMap<>();

    /**
     * {@link #sizeCache} に記録された全サイズの合計。
     * 未計測アイテムの推定値は {@code totalMeasuredSize / sizeCache.size()} (算術平均) で求める。
     * <p>
     * 算術平均を使う理由: アイテムのサイズ分布に偏りがあっても、
     * 計測数が増えるほど真の平均に収束し、合計サイズの推定精度が安定するため。
     */
    private long totalMeasuredSize;

    /** position → 配置された列 (HORIZONTAL の場合は行) インデックス。 */
    private final Map<Integer, Integer> columnAssignment = new HashMap<>();

    /**
     * {@link #onLayoutChildren} で計算された合計サイズのキャッシュ。
     * <p>
     * {@code computeTotalSize()} が呼ばれるたびに再計算すると、推定値の変化 → 合計サイズ変化 →
     * JScrollPane がビューポートを再レイアウト → {@code onLayoutChildren} が再呼出し…
     * という無限ループ (フィードバックループ) に陥る。これを防ぐためにキャッシュする。
     * <p>
     * ただしスクロールで新たなアイテムが計測されたら精度向上のため再計算が必要。
     * その判定には {@link #lastCacheSizeOnCompute} を使う。
     */
    private Dimension cachedTotalSize;

    /**
     * {@link #cachedTotalSize} を最後に計算した時点の {@code sizeCache.size()}。
     * スクロールで新たなアイテムが計測されると {@code sizeCache.size()} がこの値を超えるので、
     * それを再計算のトリガーとして使う。
     */
    private int lastCacheSizeOnCompute;

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
                // 可視アイテムは実体化して実際のサイズを計測する
                RecyclerPane.ViewHolder holder = recycler.obtainViewForPosition(pos);
                int itemHeight = holder.itemView.getPreferredSize().height;

                // 初回計測のみ totalMeasuredSize に加算する。
                // スクロールで再表示されたアイテムを二重加算すると
                // 算術平均 (totalMeasuredSize / sizeCache.size()) が不正になるため。
                if (!sizeCache.containsKey(pos)) {
                    updateEstimatedSize(itemHeight);
                }
                sizeCache.put(pos, itemHeight);

                int itemTop = y + offsets.top;
                layoutChild(holder,
                        x + offsets.left,
                        itemTop,
                        columnWidth - offsets.left - offsets.right,
                        itemHeight);

                updateVisiblePositions(pos, itemTop, itemTop + itemHeight, scrollOffset, scrollOffset + viewportHeight);

                // 実測値で列の高さを進める
                columnTops[col] = y + itemHeight + offsets.top + offsets.bottom + mainAxisGap;
            } else {
                // 非可視アイテムはキャッシュ済みサイズまたは推定値で列の高さを進める
                columnTops[col] = y + itemSize + offsets.top + offsets.bottom + mainAxisGap;
            }

            // 全列がビューポートの下を超えたら、これ以降のアイテムは確実に不可視なので終了
            if (allColumnsExceed(columnTops, scrollOffset + viewportHeight)) {
                break;
            }
        }

        // sizeCache が前回の計算時より成長していれば cachedTotalSize を再計算する。
        // スクロールで新たなアイテムが計測されると推定精度が上がるため、
        // その改善を合計サイズに反映してスクロールバーの精度を向上させる。
        if (cachedTotalSize == null || sizeCache.size() > lastCacheSizeOnCompute) {
            cachedTotalSize = computeTotalSizeFromFullScan(itemCount, viewportWidth, 0);
            lastCacheSizeOnCompute = sizeCache.size();
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

                // layoutVertical と同じく、初回計測のみ totalMeasuredSize に加算
                if (!sizeCache.containsKey(pos)) {
                    updateEstimatedSize(itemWidth);
                }
                sizeCache.put(pos, itemWidth);

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

        // layoutVertical と同じく、sizeCache 成長時に合計サイズを再計算
        if (cachedTotalSize == null || sizeCache.size() > lastCacheSizeOnCompute) {
            cachedTotalSize = computeTotalSizeFromFullScan(itemCount, 0, viewportHeight);
            lastCacheSizeOnCompute = sizeCache.size();
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

    /**
     * 指定 position のアイテムの主軸サイズを返す。
     * 計測済みならキャッシュ値、未計測なら計測済みアイテムの算術平均を推定値として返す。
     * まだ何も計測されていない初期状態ではフォールバック値 30 を返す。
     */
    private int getCachedOrEstimatedSize(int position) {
        Integer cached = sizeCache.get(position);
        if (cached != null) {
            return cached;
        }
        int cacheCount = sizeCache.size();
        return cacheCount > 0 ? (int) (totalMeasuredSize / cacheCount) : 30;
    }

    /**
     * 新たに計測されたサイズを {@link #totalMeasuredSize} に加算する。
     * 呼び出し元で {@code sizeCache.containsKey(pos)} を確認済みであること。
     */
    private void updateEstimatedSize(int measuredSize) {
        totalMeasuredSize += measuredSize;
    }

    /**
     * データ変更時に呼ばれる。{@code Adapter.notifyDataSetChanged()},
     * {@code notifyItemChanged()}, {@code notifyItemInserted()},
     * {@code notifyItemRemoved()} のいずれも本メソッドを経由する。
     * <p>
     * StaggeredGrid は「最短列に順に配置」する方式のため、途中のアイテムのサイズ変更が
     * 後続全アイテムの列割り当てに波及し得る。
     * このため部分無効化ではなく全キャッシュのクリアが必要。
     */
    @Override
    protected void onDataChanged() {
        clearSizeCache();
    }

    /** サイズキャッシュ・推定値・列割り当て・合計サイズキャッシュを全てリセットする。 */
    private void clearSizeCache() {
        sizeCache.clear();
        totalMeasuredSize = 0;
        columnAssignment.clear();
        cachedTotalSize = null;
        lastCacheSizeOnCompute = 0;
    }

    @Override
    public boolean canScrollVertically() {
        return orientation == Orientation.VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return orientation == Orientation.HORIZONTAL;
    }

    /**
     * JScrollPane がスクロール範囲を決定するために呼ぶメソッド。
     * {@link #cachedTotalSize} がある場合はそれを返し、無ければフルスキャンで計算する。
     */
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
     * 各アイテムのサイズは {@link #getCachedOrEstimatedSize} で取得するため、
     * 計測済みアイテムは実測値、未計測アイテムは算術平均の推定値が使われる。
     * <p>
     * {@link #layoutVertical} / {@link #layoutHorizontal} の末尾から呼ばれ、
     * 結果は {@link #cachedTotalSize} に保存される。
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

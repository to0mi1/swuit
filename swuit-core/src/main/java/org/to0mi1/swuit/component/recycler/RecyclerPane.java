package org.to0mi1.swuit.component.recycler;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Android の RecyclerView に相当するコンポーネント。
 * <p>
 * {@link Adapter} でデータとビューの変換を行い、
 * {@link LayoutManager} で配置戦略を定義する。
 * ビューは {@link Recycler} により3段階キャッシュ (Scrap/Cache/Pool) でリサイクルされる。
 * <p>
 * {@link Scrollable} を実装しており、{@link JScrollPane} のビューとして使用する。
 * JViewport に配置されると、スクロール位置の変化を自動的に検知して可視アイテムを再配置する。
 */
public class RecyclerPane extends JComponent implements Scrollable {

    private Adapter<?> adapter;
    private LayoutManager layoutManager;
    private final Recycler recycler = new Recycler(this);
    private final List<ItemDecoration> itemDecorations = new ArrayList<>();

    /** JViewport のスクロール位置変化を検知するリスナー。 */
    private final ChangeListener viewportChangeListener = this::onViewportChanged;

    /** 現在リスナーを登録している JViewport。 */
    private JViewport registeredViewport;

    /** レイアウト再帰・並行呼び出し防止フラグ。 */
    private final AtomicBoolean inLayout = new AtomicBoolean(false);

    /** アダプタを設定する。null を指定すると子コンポーネントがクリアされる。 */
    public void setAdapter(Adapter<?> adapter) {
        if (this.adapter != null) {
            this.adapter.recyclerPane = null;
        }
        this.adapter = adapter;
        if (adapter != null) {
            adapter.recyclerPane = this;
        }
        recycler.clear();
        removeAll();
        invalidate();
        repaint();
    }

    /** 現在のアダプタを返す。 */
    public Adapter<?> getAdapter() {
        return adapter;
    }

    /** レイアウトマネージャーを設定する。 */
    public void setLayoutManager(LayoutManager layoutManager) {
        if (this.layoutManager != null) {
            this.layoutManager.recyclerPane = null;
        }
        this.layoutManager = layoutManager;
        if (layoutManager != null) {
            layoutManager.recyclerPane = this;
        }
        recycler.clear();
        removeAll();
        invalidate();
        repaint();
    }

    /** 現在のレイアウトマネージャーを返す。 */
    public LayoutManager getLayoutManager() {
        return layoutManager;
    }

    /** リサイクラーを返す。 */
    public Recycler getRecycler() {
        return recycler;
    }

    /** アイテム装飾を追加する。 */
    public void addItemDecoration(ItemDecoration decoration) {
        itemDecorations.add(decoration);
        invalidate();
        repaint();
    }

    /** アイテム装飾を除去する。 */
    public void removeItemDecoration(ItemDecoration decoration) {
        itemDecorations.remove(decoration);
        invalidate();
        repaint();
    }

    /** 登録済みのアイテム装飾リストを返す。 */
    public List<ItemDecoration> getItemDecorations() {
        return List.copyOf(itemDecorations);
    }

    /** 指定位置までスクロールする。 */
    public void scrollToPosition(int position) {
        if (layoutManager != null) {
            layoutManager.scrollToPosition(position);
        }
    }

    /** 指定位置までオフセット付きでスクロールする。 */
    public void scrollToPositionWithOffset(int position, int offset) {
        if (layoutManager != null) {
            layoutManager.scrollToPositionWithOffset(position, offset);
        }
    }

    /** データ変更通知を受けてレイアウトを再実行する。 */
    void onDataChanged() {
        if (layoutManager != null) {
            layoutManager.onDataChanged();
        }
        invalidate();
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ensureViewportListener();
    }

    @Override
    public void removeNotify() {
        unregisterViewportListener();
        super.removeNotify();
    }

    /** 親が JViewport であればリスナーを登録する。 */
    private void ensureViewportListener() {
        Container parent = getParent();
        if (parent instanceof JViewport viewport) {
            if (registeredViewport != viewport) {
                unregisterViewportListener();
                viewport.addChangeListener(viewportChangeListener);
                registeredViewport = viewport;
            }
        } else {
            unregisterViewportListener();
        }
    }

    /** 登録済みリスナーを解除する。 */
    private void unregisterViewportListener() {
        if (registeredViewport != null) {
            registeredViewport.removeChangeListener(viewportChangeListener);
            registeredViewport = null;
        }
    }

    /**
     * ビューポートのスクロール位置を変更し、レイアウトを1回だけ実行する。
     * <p>
     * {@code viewport.setViewPosition()} は ChangeListener 経由で {@link #doLayout()} をトリガーする。
     * その後に明示的な {@code doLayout()} を呼ぶと二重レイアウトとなり、
     * ConcurrentModificationException 等の問題を引き起こす。
     * このメソッドでは ChangeListener を一時抑制してから setViewPosition を呼び、
     * 最後に doLayout を1回だけ実行する。
     */
    void scrollViewportAndLayout(Point viewPosition) {
        Container parent = getParent();
        if (!(parent instanceof JViewport viewport)) return;
        inLayout.set(true);
        try {
            viewport.setViewPosition(viewPosition);
        } finally {
            inLayout.set(false);
        }
        doLayout();
    }

    /** JViewport のスクロール位置・サイズ変化時に呼ばれる。 */
    private void onViewportChanged(ChangeEvent e) {
        if (!inLayout.get()) {
            doLayout();
            repaint();
        }
    }

    @Override
    public void doLayout() {
        if (adapter == null || layoutManager == null) {
            return;
        }
        // 他のスレッドが doLayout 中の場合は完了を待ってから実行する。
        // EDT と非 EDT スレッドの並行 doLayout による
        // ConcurrentModificationException / ArrayIndexOutOfBoundsException を防止する。
        while (!inLayout.compareAndSet(false, true)) {
            Thread.yield();
        }
        // 親が JViewport ならリスナーを遅延登録
        ensureViewportListener();
        try {
            State state = new State(adapter.getItemCount());
            layoutManager.onLayoutChildren(recycler, state);
        } finally {
            inLayout.set(false);
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        // 背景レイヤー装飾
        for (ItemDecoration decoration : itemDecorations) {
            decoration.onDraw(g, this);
        }
        super.paintChildren(g);
        // 前景レイヤー装飾
        for (ItemDecoration decoration : itemDecorations) {
            decoration.onDrawOver(g, this);
        }
    }

    // --- Scrollable 実装 ---

    @Override
    public Dimension getPreferredSize() {
        if (layoutManager != null && adapter != null) {
            State state = new State(adapter.getItemCount());
            return layoutManager.computeTotalSize(state);
        }
        return new Dimension(0, 0);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        if (layoutManager != null) {
            return !layoutManager.canScrollHorizontally();
        }
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (layoutManager != null) {
            return !layoutManager.canScrollVertically();
        }
        return true;
    }

    // ========== ネストクラス ==========

    /**
     * データとビューの変換を行うアダプタ。
     *
     * @param <VH> ViewHolder の型
     */
    public static abstract class Adapter<VH extends ViewHolder> {

        RecyclerPane recyclerPane;

        /** 新しい ViewHolder を生成する。 */
        public abstract VH onCreateViewHolder(RecyclerPane parent, int viewType);

        /** ViewHolder にデータをバインドする。 */
        public abstract void onBindViewHolder(VH holder, int position);

        /** アイテム数を返す。 */
        public abstract int getItemCount();

        /** 指定位置のビュータイプを返す。デフォルトは 0。 */
        public int getItemViewType(int position) {
            return 0;
        }

        /** 全データ変更を通知する。 */
        public void notifyDataSetChanged() {
            if (recyclerPane != null) {
                recyclerPane.onDataChanged();
            }
        }

        /** アイテム挿入を通知する。 */
        public void notifyItemInserted(int position) {
            if (recyclerPane != null) {
                recyclerPane.onDataChanged();
            }
        }

        /** アイテム削除を通知する。 */
        public void notifyItemRemoved(int position) {
            if (recyclerPane != null) {
                recyclerPane.onDataChanged();
            }
        }

        /** アイテム変更を通知する。 */
        public void notifyItemChanged(int position) {
            if (recyclerPane != null) {
                recyclerPane.onDataChanged();
            }
        }
    }

    /**
     * ビューとメタデータを保持する ViewHolder。
     */
    public static class ViewHolder {

        /** このホルダーのルートコンポーネント。 */
        public final JComponent itemView;

        int adapterPosition = -1;
        int itemViewType = 0;

        public ViewHolder(JComponent itemView) {
            this.itemView = Objects.requireNonNull(itemView, "itemView must not be null");
        }

        /** アダプタ上の位置を返す。 */
        public int getAdapterPosition() {
            return adapterPosition;
        }

        /** ビュータイプを返す。 */
        public int getItemViewType() {
            return itemViewType;
        }
    }

    /**
     * 配置戦略を定義する抽象レイアウトマネージャー。
     */
    public static abstract class LayoutManager {

        RecyclerPane recyclerPane;

        protected int firstVisibleItemPosition = -1;
        protected int lastVisibleItemPosition = -1;
        protected int firstCompletelyVisibleItemPosition = -1;
        protected int lastCompletelyVisibleItemPosition = -1;

        /** 子コンポーネントを配置する。 */
        public abstract void onLayoutChildren(Recycler recycler, State state);

        /** 垂直スクロールが可能か。 */
        public abstract boolean canScrollVertically();

        /** 水平スクロールが可能か。 */
        public abstract boolean canScrollHorizontally();

        /** 全コンテンツの推定サイズを計算する。 */
        public abstract Dimension computeTotalSize(State state);

        /** 指定位置までスクロールする。サブクラスでオーバーライド可能。 */
        public void scrollToPosition(int position) {
            // デフォルトは何もしない
        }

        /** 指定位置までオフセット付きでスクロールする。 */
        public void scrollToPositionWithOffset(int position, int offset) {
            scrollToPosition(position);
        }

        /** データ変更時に呼ばれる。サブクラスでキャッシュクリア等に使用する。 */
        protected void onDataChanged() {
            // デフォルトは何もしない
        }

        /** 最初の可視アイテムの位置を返す。 */
        public int findFirstVisibleItemPosition() { return firstVisibleItemPosition; }

        /** 最後の可視アイテムの位置を返す。 */
        public int findLastVisibleItemPosition() { return lastVisibleItemPosition; }

        /** 最初の完全可視アイテムの位置を返す。 */
        public int findFirstCompletelyVisibleItemPosition() { return firstCompletelyVisibleItemPosition; }

        /** 最後の完全可視アイテムの位置を返す。 */
        public int findLastCompletelyVisibleItemPosition() { return lastCompletelyVisibleItemPosition; }

        /** 可視アイテム位置をリセットする。 */
        protected void resetVisiblePositions() {
            firstVisibleItemPosition = -1;
            lastVisibleItemPosition = -1;
            firstCompletelyVisibleItemPosition = -1;
            lastCompletelyVisibleItemPosition = -1;
        }

        /**
         * setter 等でレイアウトの再実行を要求する。
         * <p>
         * Android の {@code requestLayout()} に相当する。
         * Swing の {@code revalidate()} は EDT にレイアウトをスケジュールするが、
         * 非 EDT スレッドから明示的に {@code doLayout()} を呼ぶと並行実行となるため、
         * ここでは即時 {@code doLayout()} を呼んで確定的にレイアウトする。
         */
        protected void requestLayout() {
            if (recyclerPane != null) {
                recyclerPane.doLayout();
                recyclerPane.repaint();
            }
        }

        /** RecyclerPane のビューポート幅を返す。 */
        public int getWidth() {
            if (recyclerPane == null) return 0;
            Container parent = recyclerPane.getParent();
            if (parent instanceof JViewport viewport) {
                return viewport.getWidth();
            }
            return recyclerPane.getWidth();
        }

        /** RecyclerPane のビューポート高さを返す。 */
        public int getHeight() {
            if (recyclerPane == null) return 0;
            Container parent = recyclerPane.getParent();
            if (parent instanceof JViewport viewport) {
                return viewport.getHeight();
            }
            return recyclerPane.getHeight();
        }

        /** 現在のスクロールオフセット（Y 方向）を返す。 */
        protected int getScrollOffsetY() {
            if (recyclerPane == null) return 0;
            Container parent = recyclerPane.getParent();
            if (parent instanceof JViewport viewport) {
                return viewport.getViewPosition().y;
            }
            return 0;
        }

        /** 現在のスクロールオフセット（X 方向）を返す。 */
        protected int getScrollOffsetX() {
            if (recyclerPane == null) return 0;
            Container parent = recyclerPane.getParent();
            if (parent instanceof JViewport viewport) {
                return viewport.getViewPosition().x;
            }
            return 0;
        }

        /**
         * ViewHolder のビューを RecyclerPane に配置する。
         *
         * @param holder 配置する ViewHolder
         * @param x      X 座標
         * @param y      Y 座標
         * @param width  幅
         * @param height 高さ
         */
        protected void layoutChild(ViewHolder holder, int x, int y, int width, int height) {
            JComponent view = holder.itemView;
            view.setBounds(x, y, width, height);
            if (view.getParent() != recyclerPane) {
                recyclerPane.add(view);
            }
            view.setVisible(true);
        }

        /**
         * アイテム装飾のオフセットを合算して返す。
         */
        protected Insets getItemDecorationsOffsets(int position) {
            if (recyclerPane == null) return new Insets(0, 0, 0, 0);
            int top = 0, left = 0, bottom = 0, right = 0;
            for (ItemDecoration decoration : recyclerPane.itemDecorations) {
                Insets offsets = decoration.getItemOffsets(position);
                top += offsets.top;
                left += offsets.left;
                bottom += offsets.bottom;
                right += offsets.right;
            }
            return new Insets(top, left, bottom, right);
        }
    }

    /**
     * レイアウト時の状態を保持する。
     */
    public static class State {

        private final int itemCount;

        public State(int itemCount) {
            this.itemCount = itemCount;
        }

        /** アイテム総数を返す。 */
        public int getItemCount() {
            return itemCount;
        }
    }
}

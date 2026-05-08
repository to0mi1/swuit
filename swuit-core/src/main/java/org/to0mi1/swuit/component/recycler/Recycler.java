package org.to0mi1.swuit.component.recycler;

import java.util.*;

/**
 * 3段階キャッシュ (Scrap / Cache / Pool) によるビューリサイクル機構。
 * <p>
 * <ol>
 *   <li><b>Scrap</b> — レイアウト中に一時退避されたビュー。同一 position かつ同一 viewType で取得すれば rebind 不要。</li>
 *   <li><b>Cache</b> — リサイクルされたビュー（position 付き）。position 一致なら rebind 不要。</li>
 *   <li><b>Pool</b> — viewType 別のビュープール。取得時に rebind が必要。</li>
 * </ol>
 */
public class Recycler {

    private final RecyclerPane recyclerPane;

    /** Scrap: レイアウト中に一時退避。position → ViewHolder */
    private final Map<Integer, RecyclerPane.ViewHolder> scrap = new LinkedHashMap<>();

    /** Cache: position 付きキャッシュ (FIFO)。 */
    private final LinkedList<RecyclerPane.ViewHolder> cache = new LinkedList<>();
    private int cacheSize = 5;

    /** Pool: viewType 別のビュープール。 */
    private final Map<Integer, LinkedList<RecyclerPane.ViewHolder>> pool = new HashMap<>();
    private int poolMaxSize = 5;

    /** Component → ViewHolder の逆引きマップ。O(1) でルックアップ。 */
    private final Map<java.awt.Component, RecyclerPane.ViewHolder> viewHolderMap = new IdentityHashMap<>();

    Recycler(RecyclerPane recyclerPane) {
        this.recyclerPane = recyclerPane;
    }

    /**
     * 現在 attach されているビューを scrap に移動する。
     * レイアウト開始時に呼び出す。
     */
    public void scrapAttachedViews() {
        scrap.clear();
        for (int i = recyclerPane.getComponentCount() - 1; i >= 0; i--) {
            java.awt.Component comp = recyclerPane.getComponent(i);
            RecyclerPane.ViewHolder holder = viewHolderMap.get(comp);
            // Cache/Pool に入った非表示ビューは除外し、表示中のビューのみスクラップ
            if (holder != null && holder.adapterPosition >= 0 && comp.isVisible()) {
                scrap.put(holder.adapterPosition, holder);
            }
        }
    }

    /**
     * 指定 position のビューを取得する。
     * Scrap → Cache → Pool → 新規作成 の順で検索する。
     * <p>
     * LayoutManager からは {@link #obtainViewForPosition(int)} を使うこと。
     */
    @SuppressWarnings("unchecked")
    RecyclerPane.ViewHolder getViewForPosition(int position) {
        RecyclerPane.Adapter<RecyclerPane.ViewHolder> adapter =
                (RecyclerPane.Adapter<RecyclerPane.ViewHolder>) recyclerPane.getAdapter();
        int viewType = adapter.getItemViewType(position);

        // 1. Scrap から検索 (position + viewType 一致)
        RecyclerPane.ViewHolder holder = scrap.remove(position);
        if (holder != null) {
            if (holder.itemViewType == viewType) {
                holder.adapterPosition = position;
                return holder;
            }
            // viewType 不一致 → Pool へ返却
            addToPool(holder);
        }

        // 2. Cache から検索 (position + viewType 一致)
        Iterator<RecyclerPane.ViewHolder> cacheIt = cache.iterator();
        while (cacheIt.hasNext()) {
            RecyclerPane.ViewHolder cached = cacheIt.next();
            if (cached.adapterPosition == position && cached.itemViewType == viewType) {
                cacheIt.remove();
                return cached;
            }
        }

        // 3. Pool から検索 (viewType 一致)
        LinkedList<RecyclerPane.ViewHolder> typePool = pool.get(viewType);
        if (typePool != null && !typePool.isEmpty()) {
            holder = typePool.removeFirst();
            holder.adapterPosition = position;
            adapter.onBindViewHolder(holder, position);
            return holder;
        }

        // 4. 新規作成
        holder = adapter.onCreateViewHolder(recyclerPane, viewType);
        holder.itemViewType = viewType;
        holder.adapterPosition = position;
        adapter.onBindViewHolder(holder, position);
        return holder;
    }

    /**
     * 残った scrap を Cache/Pool にリサイクルする。
     * レイアウト完了後に呼び出す。
     * <p>
     * setVisible(false) が Swing イベントを発火し scrap が変更される可能性があるため、
     * 先にコピーしてからイテレーションする。
     */
    public void recycleScrap() {
        var remaining = new java.util.ArrayList<>(scrap.values());
        scrap.clear();
        for (RecyclerPane.ViewHolder holder : remaining) {
            holder.itemView.setVisible(false);
            recycleView(holder);
        }
    }

    /**
     * ViewHolder を Cache/Pool に返却する。
     */
    public void recycleView(RecyclerPane.ViewHolder holder) {
        // 二重登録防止
        if (cache.contains(holder)) {
            return;
        }
        if (cacheSize <= 0) {
            addToPool(holder);
            return;
        }
        if (cache.size() >= cacheSize) {
            RecyclerPane.ViewHolder evicted = cache.removeFirst();
            addToPool(evicted);
        }
        cache.addLast(holder);
    }

    private void addToPool(RecyclerPane.ViewHolder holder) {
        int viewType = holder.itemViewType;
        LinkedList<RecyclerPane.ViewHolder> typePool = pool.computeIfAbsent(viewType, k -> new LinkedList<>());
        if (typePool.size() < poolMaxSize) {
            holder.adapterPosition = -1;
            typePool.addLast(holder);
        } else {
            // Pool 満杯 → コンポーネントツリーからも除去して完全廃棄
            discardHolder(holder);
        }
    }

    /** ViewHolder を完全廃棄する。コンポーネントツリーと逆引きマップからも除去。 */
    private void discardHolder(RecyclerPane.ViewHolder holder) {
        if (holder.itemView.getParent() == recyclerPane) {
            recyclerPane.remove(holder.itemView);
        }
        viewHolderMap.remove(holder.itemView);
    }

    /** Cache サイズを設定する。 */
    public void setCacheSize(int size) {
        this.cacheSize = size;
        while (cache.size() > cacheSize) {
            RecyclerPane.ViewHolder evicted = cache.removeFirst();
            addToPool(evicted);
        }
    }

    /** Pool の viewType 当たり最大サイズを設定する。 */
    public void setPoolMaxSize(int size) {
        this.poolMaxSize = size;
    }

    /** 全キャッシュをクリアする。 */
    public void clear() {
        scrap.clear();
        cache.clear();
        pool.clear();
        viewHolderMap.clear();
    }

    /** Cache サイズを返す。 */
    public int getCacheSize() {
        return cacheSize;
    }

    /** Pool の最大サイズを返す。 */
    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    /**
     * ViewHolder を取得し、逆引きマップに登録する。
     * LayoutManager から呼び出す。
     */
    public RecyclerPane.ViewHolder obtainViewForPosition(int position) {
        RecyclerPane.ViewHolder holder = getViewForPosition(position);
        viewHolderMap.put(holder.itemView, holder);
        return holder;
    }

    /**
     * 指定 adapter position に紐づく、現在表示中の ViewHolder を返す。
     * Cache に居る (setVisible(false) 済み) ものや、Pool に戻っている (adapterPosition = -1) ものは除外する。
     *
     * @return 表示中であれば該当 ViewHolder、なければ {@code null}
     */
    RecyclerPane.ViewHolder findAttachedHolder(int position) {
        if (position < 0) return null;
        for (RecyclerPane.ViewHolder holder : viewHolderMap.values()) {
            if (holder.adapterPosition == position && holder.itemView.isVisible()) {
                return holder;
            }
        }
        return null;
    }

    /**
     * 指定 adapter position に紐づく Cache エントリを破棄する。
     * Cache から取り出して Pool に戻し、{@code adapterPosition} を {@code -1} にリセットする。
     * これにより、次に同じ position が要求されたときに必ず {@code onBindViewHolder} が呼ばれる。
     * <p>
     * 表示中 (visible) の ViewHolder には影響しない。
     */
    void invalidatePosition(int position) {
        if (position < 0) return;
        Iterator<RecyclerPane.ViewHolder> it = cache.iterator();
        while (it.hasNext()) {
            RecyclerPane.ViewHolder cached = it.next();
            if (cached.adapterPosition == position) {
                it.remove();
                addToPool(cached);
            }
        }
    }
}

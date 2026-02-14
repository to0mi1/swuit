package org.to0mi1.swuit.layout.grid;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CSS Grid に相当するレイアウトマネージャー。
 * <p>
 * 可変サイズのトラック (fixed/fr/auto)、スパン (columnSpan/rowSpan)、
 * セル内アライメント (justifyItems/alignItems) をサポートする。
 *
 * <pre>{@code
 * CssGridLayout layout = new CssGridLayout()
 *     .setColumnTemplate(CssTrackSize.fixed(100), CssTrackSize.fr(1), CssTrackSize.fr(2))
 *     .setRowTemplate(CssTrackSize.fixed(50), CssTrackSize.fr(1))
 *     .setColumnGap(8).setRowGap(8);
 *
 * JPanel panel = new JPanel(layout);
 * panel.add(header, new CssGridConstraints().column(0).row(0).columnSpan(3));
 * panel.add(sidebar, new CssGridConstraints().column(0).row(1));
 * panel.add(content, new CssGridConstraints().column(1).row(1).columnSpan(2));
 * }</pre>
 */
public class CssGridLayout implements LayoutManager2 {

    private CssTrackSize[] columnTemplate = {};
    private CssTrackSize[] rowTemplate = {};
    private int columnGap = 0;
    private int rowGap = 0;
    private CssJustifyItems justifyItems = CssJustifyItems.STRETCH;
    private CssAlignItems alignItems = CssAlignItems.STRETCH;

    private final Map<Component, CssGridConstraints> constraintsMap = new LinkedHashMap<>();

    // --- プロパティ (fluent setters) ---

    public CssTrackSize[] getColumnTemplate() {
        return columnTemplate.clone();
    }

    public CssGridLayout setColumnTemplate(CssTrackSize... columnTemplate) {
        this.columnTemplate = columnTemplate.clone();
        return this;
    }

    public CssTrackSize[] getRowTemplate() {
        return rowTemplate.clone();
    }

    public CssGridLayout setRowTemplate(CssTrackSize... rowTemplate) {
        this.rowTemplate = rowTemplate.clone();
        return this;
    }

    public int getColumnGap() {
        return columnGap;
    }

    public CssGridLayout setColumnGap(int columnGap) {
        this.columnGap = columnGap;
        return this;
    }

    public int getRowGap() {
        return rowGap;
    }

    public CssGridLayout setRowGap(int rowGap) {
        this.rowGap = rowGap;
        return this;
    }

    public CssJustifyItems getCssJustifyItems() {
        return justifyItems;
    }

    public CssGridLayout setCssJustifyItems(CssJustifyItems justifyItems) {
        this.justifyItems = justifyItems;
        return this;
    }

    public CssAlignItems getCssAlignItems() {
        return alignItems;
    }

    public CssGridLayout setCssAlignItems(CssAlignItems alignItems) {
        this.alignItems = alignItems;
        return this;
    }

    // --- LayoutManager2 ---

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints == null) {
            constraintsMap.put(comp, new CssGridConstraints());
        } else if (constraints instanceof CssGridConstraints gc) {
            constraintsMap.put(comp, gc.clone());
        } else {
            throw new IllegalArgumentException(
                    "constraints must be a CssGridConstraints instance: " + constraints.getClass().getName());
        }
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        addLayoutComponent(comp, null);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        constraintsMap.remove(comp);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
        // キャッシュなし
    }

    // --- サイズ計算 ---

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            List<CssGridItem> items = collectItems(parent);
            if (items.isEmpty()) {
                return new Dimension(insets.left + insets.right, insets.top + insets.bottom);
            }

            autoPlace(items);

            int numCols = resolveColumnCount(items);
            int numRows = resolveRowCount(items);

            List<CssGridTrack> colTracks = buildTracks(columnTemplate, numCols);
            List<CssGridTrack> rowTracks = buildTracks(rowTemplate, numRows);

            // fr トラックはコンテンツサイズにフォールバック
            resolvePreferredCssTrackSizes(colTracks, items, true);
            resolvePreferredCssTrackSizes(rowTracks, items, false);
            resolveSpanItems(colTracks, items, true);
            resolveSpanItems(rowTracks, items, false);

            int width = sumCssTrackSizes(colTracks) + columnGap * Math.max(0, numCols - 1);
            int height = sumCssTrackSizes(rowTracks) + rowGap * Math.max(0, numRows - 1);

            return new Dimension(
                    width + insets.left + insets.right,
                    height + insets.top + insets.bottom);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    // --- レイアウト ---

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int availableWidth = parent.getWidth() - insets.left - insets.right;
            int availableHeight = parent.getHeight() - insets.top - insets.bottom;

            // Phase 1: アイテム収集
            List<CssGridItem> items = collectItems(parent);
            if (items.isEmpty()) return;

            // Phase 2: 自動配置
            autoPlace(items);

            int numCols = resolveColumnCount(items);
            int numRows = resolveRowCount(items);

            // Phase 3: トラックサイズ解決
            List<CssGridTrack> colTracks = buildTracks(columnTemplate, numCols);
            List<CssGridTrack> rowTracks = buildTracks(rowTemplate, numRows);

            resolveCssTrackSizes(colTracks, items, true, availableWidth, numCols);
            resolveCssTrackSizes(rowTracks, items, false, availableHeight, numRows);

            // トラックオフセット計算
            computeOffsets(colTracks, columnGap);
            computeOffsets(rowTracks, rowGap);

            // Phase 4-5: アライメント適用 + 座標確定
            for (CssGridItem item : items) {
                int cellX = colTracks.get(item.column).offset;
                int cellY = rowTracks.get(item.row).offset;
                int cellW = spanSize(colTracks, item.column, item.columnSpan, columnGap);
                int cellH = spanSize(rowTracks, item.row, item.rowSpan, rowGap);

                Insets m = item.constraints.getMargin();
                int marginX = cellX + m.left;
                int marginY = cellY + m.top;
                int marginW = Math.max(0, cellW - m.left - m.right);
                int marginH = Math.max(0, cellH - m.top - m.bottom);

                Dimension pref = item.component.getPreferredSize();

                // 水平方向アライメント
                CssJustifyItems hAlign = resolveJustify(item.constraints.getCssJustifySelf());
                int x, w;
                switch (hAlign) {
                    case START:
                        x = marginX;
                        w = Math.min(pref.width, marginW);
                        break;
                    case END:
                        w = Math.min(pref.width, marginW);
                        x = marginX + marginW - w;
                        break;
                    case CENTER:
                        w = Math.min(pref.width, marginW);
                        x = marginX + (marginW - w) / 2;
                        break;
                    default: // STRETCH
                        x = marginX;
                        w = marginW;
                        break;
                }

                // 垂直方向アライメント
                CssAlignItems vAlign = resolveAlign(item.constraints.getCssAlignSelf());
                int y, h;
                switch (vAlign) {
                    case START:
                        y = marginY;
                        h = Math.min(pref.height, marginH);
                        break;
                    case END:
                        h = Math.min(pref.height, marginH);
                        y = marginY + marginH - h;
                        break;
                    case CENTER:
                        h = Math.min(pref.height, marginH);
                        y = marginY + (marginH - h) / 2;
                        break;
                    default: // STRETCH
                        y = marginY;
                        h = marginH;
                        break;
                }

                item.component.setBounds(insets.left + x, insets.top + y, w, h);
            }
        }
    }

    // --- Phase 1: アイテム収集 ---

    private List<CssGridItem> collectItems(Container parent) {
        List<CssGridItem> items = new ArrayList<>();
        int addOrder = 0;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            CssGridConstraints gc = getConstraints(child);
            items.add(new CssGridItem(child, gc, addOrder++));
        }
        return items;
    }

    // --- Phase 2: 自動配置 ---

    private void autoPlace(List<CssGridItem> items) {
        int numCols = columnTemplate.length > 0 ? columnTemplate.length : 1;

        // まず列数を確定（明示的列指定があれば反映）
        for (CssGridItem item : items) {
            if (item.column >= 0) {
                numCols = Math.max(numCols, item.column + item.columnSpan);
            }
        }

        // columnSpan が列数を超えるアイテムがあれば列数を拡張
        for (CssGridItem item : items) {
            if (!item.isPositioned() && item.columnSpan > numCols) {
                numCols = item.columnSpan;
            }
        }

        // 占有マップ (行は動的に追加)
        List<boolean[]> occupied = new ArrayList<>();

        // 明示的位置のアイテムを先に配置
        for (CssGridItem item : items) {
            if (item.isPositioned()) {
                ensureRows(occupied, item.row + item.rowSpan, numCols);
                markOccupied(occupied, item.row, item.column, item.rowSpan, item.columnSpan);
            }
        }

        // 未配置アイテムを行優先で空きセルに配置
        int cursorRow = 0;
        int cursorCol = 0;
        for (CssGridItem item : items) {
            if (item.isPositioned()) continue;

            while (true) {
                ensureRows(occupied, cursorRow + item.rowSpan, numCols);

                if (cursorCol + item.columnSpan > numCols) {
                    cursorCol = 0;
                    cursorRow++;
                    continue;
                }

                if (canPlace(occupied, cursorRow, cursorCol, item.rowSpan, item.columnSpan)) {
                    item.row = cursorRow;
                    item.column = cursorCol;
                    markOccupied(occupied, cursorRow, cursorCol, item.rowSpan, item.columnSpan);
                    cursorCol += item.columnSpan;
                    break;
                }

                cursorCol++;
            }
        }
    }

    private void ensureRows(List<boolean[]> occupied, int numRows, int numCols) {
        while (occupied.size() < numRows) {
            occupied.add(new boolean[numCols]);
        }
    }

    private boolean canPlace(List<boolean[]> occupied, int row, int col, int rowSpan, int colSpan) {
        for (int r = row; r < row + rowSpan; r++) {
            if (r >= occupied.size()) continue;
            boolean[] rowData = occupied.get(r);
            for (int c = col; c < col + colSpan; c++) {
                if (c >= rowData.length) continue;
                if (rowData[c]) return false;
            }
        }
        return true;
    }

    private void markOccupied(List<boolean[]> occupied, int row, int col, int rowSpan, int colSpan) {
        for (int r = row; r < row + rowSpan; r++) {
            if (r >= occupied.size()) continue;
            boolean[] rowData = occupied.get(r);
            for (int c = col; c < col + colSpan; c++) {
                if (c < rowData.length) {
                    rowData[c] = true;
                }
            }
        }
    }

    // --- グリッドサイズ解決 ---

    private int resolveColumnCount(List<CssGridItem> items) {
        int count = columnTemplate.length;
        for (CssGridItem item : items) {
            count = Math.max(count, item.column + item.columnSpan);
        }
        return Math.max(count, 1);
    }

    private int resolveRowCount(List<CssGridItem> items) {
        int count = rowTemplate.length;
        for (CssGridItem item : items) {
            count = Math.max(count, item.row + item.rowSpan);
        }
        return Math.max(count, 1);
    }

    // --- トラック構築 ---

    private List<CssGridTrack> buildTracks(CssTrackSize[] template, int count) {
        List<CssGridTrack> tracks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            CssTrackSize def = i < template.length ? template[i] : CssTrackSize.auto();
            tracks.add(new CssGridTrack(def));
        }
        return tracks;
    }

    // --- Phase 3: トラックサイズ解決 ---

    private void resolveCssTrackSizes(List<CssGridTrack> tracks, List<CssGridItem> items,
                                   boolean isColumn, int available, int trackCount) {
        // Step 1: Fixed + Auto トラックのベースサイズ
        resolveAutoAndFixedTracks(tracks, items, isColumn);

        // Step 2: スパンアイテムによる auto トラックの拡大
        resolveSpanItems(tracks, items, isColumn);

        // Step 3: Fr トラック → 残余スペースを分配
        int usedSpace = 0;
        float totalFr = 0;
        for (CssGridTrack track : tracks) {
            if (track.definition.getType() == CssTrackSizeType.FR) {
                totalFr += track.definition.getValue();
            } else {
                usedSpace += track.baseSize;
            }
        }
        usedSpace += (trackCount > 1) ? (trackCount - 1) * (isColumn ? columnGap : rowGap) : 0;

        int freeSpace = Math.max(0, available - usedSpace);
        if (totalFr > 0) {
            float remainingFr = totalFr;
            int remainingSpace = freeSpace;
            for (CssGridTrack track : tracks) {
                if (track.definition.getType() == CssTrackSizeType.FR) {
                    if (remainingFr == track.definition.getValue()) {
                        track.baseSize = remainingSpace;
                    } else {
                        track.baseSize = Math.round(freeSpace * track.definition.getValue() / totalFr);
                    }
                    remainingSpace -= track.baseSize;
                    remainingFr -= track.definition.getValue();
                }
            }
        }
    }

    private void resolveAutoAndFixedTracks(List<CssGridTrack> tracks, List<CssGridItem> items,
                                           boolean isColumn) {
        for (int i = 0; i < tracks.size(); i++) {
            CssGridTrack track = tracks.get(i);
            switch (track.definition.getType()) {
                case FIXED:
                    track.baseSize = (int) track.definition.getValue();
                    break;
                case AUTO:
                    track.baseSize = maxPreferredSizeForTrack(items, i, isColumn);
                    break;
                case FR:
                    track.baseSize = 0;
                    break;
            }
        }
    }

    /**
     * preferredLayoutSize 用: fr トラックもコンテンツサイズでフォールバックする。
     */
    private void resolvePreferredCssTrackSizes(List<CssGridTrack> tracks, List<CssGridItem> items,
                                            boolean isColumn) {
        for (int i = 0; i < tracks.size(); i++) {
            CssGridTrack track = tracks.get(i);
            switch (track.definition.getType()) {
                case FIXED:
                    track.baseSize = (int) track.definition.getValue();
                    break;
                case AUTO:
                case FR:
                    track.baseSize = maxPreferredSizeForTrack(items, i, isColumn);
                    break;
            }
        }
    }

    private int maxPreferredSizeForTrack(List<CssGridItem> items, int trackIndex, boolean isColumn) {
        int max = 0;
        for (CssGridItem item : items) {
            int span = isColumn ? item.columnSpan : item.rowSpan;
            int pos = isColumn ? item.column : item.row;
            if (span == 1 && pos == trackIndex) {
                Dimension pref = item.component.getPreferredSize();
                Insets m = item.constraints.getMargin();
                int size = isColumn
                        ? pref.width + m.left + m.right
                        : pref.height + m.top + m.bottom;
                max = Math.max(max, size);
            }
        }
        return max;
    }

    private void resolveSpanItems(List<CssGridTrack> tracks, List<CssGridItem> items, boolean isColumn) {
        for (CssGridItem item : items) {
            int span = isColumn ? item.columnSpan : item.rowSpan;
            int pos = isColumn ? item.column : item.row;
            if (span <= 1) continue;

            Dimension pref = item.component.getPreferredSize();
            Insets m = item.constraints.getMargin();
            int needed = isColumn
                    ? pref.width + m.left + m.right
                    : pref.height + m.top + m.bottom;

            int gap = isColumn ? columnGap : rowGap;
            int current = spanBaseSize(tracks, pos, span) + gap * (span - 1);

            if (needed > current) {
                int deficit = needed - current;
                // auto / fr トラックに均等分配
                List<Integer> growableIndices = new ArrayList<>();
                for (int t = pos; t < pos + span && t < tracks.size(); t++) {
                    CssTrackSizeType type = tracks.get(t).definition.getType();
                    if (type == CssTrackSizeType.AUTO || type == CssTrackSizeType.FR) {
                        growableIndices.add(t);
                    }
                }
                if (!growableIndices.isEmpty()) {
                    int perTrack = deficit / growableIndices.size();
                    int remainder = deficit % growableIndices.size();
                    for (int idx = 0; idx < growableIndices.size(); idx++) {
                        tracks.get(growableIndices.get(idx)).baseSize += perTrack + (idx < remainder ? 1 : 0);
                    }
                }
            }
        }
    }

    private int spanBaseSize(List<CssGridTrack> tracks, int start, int span) {
        int size = 0;
        for (int i = start; i < start + span && i < tracks.size(); i++) {
            size += tracks.get(i).baseSize;
        }
        return size;
    }

    // --- オフセット計算 ---

    private void computeOffsets(List<CssGridTrack> tracks, int gap) {
        int offset = 0;
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).offset = offset;
            offset += tracks.get(i).baseSize;
            if (i < tracks.size() - 1) {
                offset += gap;
            }
        }
    }

    private int spanSize(List<CssGridTrack> tracks, int start, int span, int gap) {
        int size = 0;
        for (int i = start; i < start + span && i < tracks.size(); i++) {
            size += tracks.get(i).baseSize;
            if (i > start) {
                size += gap;
            }
        }
        return size;
    }

    private int sumCssTrackSizes(List<CssGridTrack> tracks) {
        int sum = 0;
        for (CssGridTrack track : tracks) {
            sum += track.baseSize;
        }
        return sum;
    }

    // --- アライメント解決 ---

    private CssJustifyItems resolveJustify(CssJustifySelf self) {
        if (self == CssJustifySelf.AUTO) return justifyItems;
        return switch (self) {
            case START -> CssJustifyItems.START;
            case END -> CssJustifyItems.END;
            case CENTER -> CssJustifyItems.CENTER;
            case STRETCH -> CssJustifyItems.STRETCH;
            default -> justifyItems;
        };
    }

    private CssAlignItems resolveAlign(CssAlignSelf self) {
        if (self == CssAlignSelf.AUTO) return alignItems;
        return switch (self) {
            case START -> CssAlignItems.START;
            case END -> CssAlignItems.END;
            case CENTER -> CssAlignItems.CENTER;
            case STRETCH -> CssAlignItems.STRETCH;
            default -> alignItems;
        };
    }

    // --- ユーティリティ ---

    private CssGridConstraints getConstraints(Component comp) {
        CssGridConstraints gc = constraintsMap.get(comp);
        return gc != null ? gc : new CssGridConstraints();
    }
}

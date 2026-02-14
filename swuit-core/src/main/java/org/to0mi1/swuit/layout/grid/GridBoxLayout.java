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
 * GridBoxLayout layout = new GridBoxLayout()
 *     .setColumnTemplate(TrackSize.fixed(100), TrackSize.fr(1), TrackSize.fr(2))
 *     .setRowTemplate(TrackSize.fixed(50), TrackSize.fr(1))
 *     .setColumnGap(8).setRowGap(8);
 *
 * JPanel panel = new JPanel(layout);
 * panel.add(header, new GridConstraints().column(0).row(0).columnSpan(3));
 * panel.add(sidebar, new GridConstraints().column(0).row(1));
 * panel.add(content, new GridConstraints().column(1).row(1).columnSpan(2));
 * }</pre>
 */
public class GridBoxLayout implements LayoutManager2 {

    private TrackSize[] columnTemplate = {};
    private TrackSize[] rowTemplate = {};
    private int columnGap = 0;
    private int rowGap = 0;
    private JustifyItems justifyItems = JustifyItems.STRETCH;
    private AlignItems alignItems = AlignItems.STRETCH;

    private final Map<Component, GridConstraints> constraintsMap = new LinkedHashMap<>();

    // --- プロパティ (fluent setters) ---

    public TrackSize[] getColumnTemplate() {
        return columnTemplate.clone();
    }

    public GridBoxLayout setColumnTemplate(TrackSize... columnTemplate) {
        this.columnTemplate = columnTemplate.clone();
        return this;
    }

    public TrackSize[] getRowTemplate() {
        return rowTemplate.clone();
    }

    public GridBoxLayout setRowTemplate(TrackSize... rowTemplate) {
        this.rowTemplate = rowTemplate.clone();
        return this;
    }

    public int getColumnGap() {
        return columnGap;
    }

    public GridBoxLayout setColumnGap(int columnGap) {
        this.columnGap = columnGap;
        return this;
    }

    public int getRowGap() {
        return rowGap;
    }

    public GridBoxLayout setRowGap(int rowGap) {
        this.rowGap = rowGap;
        return this;
    }

    public JustifyItems getJustifyItems() {
        return justifyItems;
    }

    public GridBoxLayout setJustifyItems(JustifyItems justifyItems) {
        this.justifyItems = justifyItems;
        return this;
    }

    public AlignItems getAlignItems() {
        return alignItems;
    }

    public GridBoxLayout setAlignItems(AlignItems alignItems) {
        this.alignItems = alignItems;
        return this;
    }

    // --- LayoutManager2 ---

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints == null) {
            constraintsMap.put(comp, new GridConstraints());
        } else if (constraints instanceof GridConstraints gc) {
            constraintsMap.put(comp, gc.clone());
        } else {
            throw new IllegalArgumentException(
                    "constraints must be a GridConstraints instance: " + constraints.getClass().getName());
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
            List<GridItem> items = collectItems(parent);
            if (items.isEmpty()) {
                return new Dimension(insets.left + insets.right, insets.top + insets.bottom);
            }

            autoPlace(items);

            int numCols = resolveColumnCount(items);
            int numRows = resolveRowCount(items);

            List<GridTrack> colTracks = buildTracks(columnTemplate, numCols);
            List<GridTrack> rowTracks = buildTracks(rowTemplate, numRows);

            // fr トラックはコンテンツサイズにフォールバック
            resolvePreferredTrackSizes(colTracks, items, true);
            resolvePreferredTrackSizes(rowTracks, items, false);
            resolveSpanItems(colTracks, items, true);
            resolveSpanItems(rowTracks, items, false);

            int width = sumTrackSizes(colTracks) + columnGap * Math.max(0, numCols - 1);
            int height = sumTrackSizes(rowTracks) + rowGap * Math.max(0, numRows - 1);

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
            List<GridItem> items = collectItems(parent);
            if (items.isEmpty()) return;

            // Phase 2: 自動配置
            autoPlace(items);

            int numCols = resolveColumnCount(items);
            int numRows = resolveRowCount(items);

            // Phase 3: トラックサイズ解決
            List<GridTrack> colTracks = buildTracks(columnTemplate, numCols);
            List<GridTrack> rowTracks = buildTracks(rowTemplate, numRows);

            resolveTrackSizes(colTracks, items, true, availableWidth, numCols);
            resolveTrackSizes(rowTracks, items, false, availableHeight, numRows);

            // トラックオフセット計算
            computeOffsets(colTracks, columnGap);
            computeOffsets(rowTracks, rowGap);

            // Phase 4-5: アライメント適用 + 座標確定
            for (GridItem item : items) {
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
                JustifyItems hAlign = resolveJustify(item.constraints.getJustifySelf());
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
                AlignItems vAlign = resolveAlign(item.constraints.getAlignSelf());
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

    private List<GridItem> collectItems(Container parent) {
        List<GridItem> items = new ArrayList<>();
        int addOrder = 0;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            GridConstraints gc = getConstraints(child);
            items.add(new GridItem(child, gc, addOrder++));
        }
        return items;
    }

    // --- Phase 2: 自動配置 ---

    private void autoPlace(List<GridItem> items) {
        int numCols = columnTemplate.length > 0 ? columnTemplate.length : 1;

        // まず列数を確定（明示的列指定があれば反映）
        for (GridItem item : items) {
            if (item.column >= 0) {
                numCols = Math.max(numCols, item.column + item.columnSpan);
            }
        }

        // columnSpan が列数を超えるアイテムがあれば列数を拡張
        for (GridItem item : items) {
            if (!item.isPositioned() && item.columnSpan > numCols) {
                numCols = item.columnSpan;
            }
        }

        // 占有マップ (行は動的に追加)
        List<boolean[]> occupied = new ArrayList<>();

        // 明示的位置のアイテムを先に配置
        for (GridItem item : items) {
            if (item.isPositioned()) {
                ensureRows(occupied, item.row + item.rowSpan, numCols);
                markOccupied(occupied, item.row, item.column, item.rowSpan, item.columnSpan);
            }
        }

        // 未配置アイテムを行優先で空きセルに配置
        int cursorRow = 0;
        int cursorCol = 0;
        for (GridItem item : items) {
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

    private int resolveColumnCount(List<GridItem> items) {
        int count = columnTemplate.length;
        for (GridItem item : items) {
            count = Math.max(count, item.column + item.columnSpan);
        }
        return Math.max(count, 1);
    }

    private int resolveRowCount(List<GridItem> items) {
        int count = rowTemplate.length;
        for (GridItem item : items) {
            count = Math.max(count, item.row + item.rowSpan);
        }
        return Math.max(count, 1);
    }

    // --- トラック構築 ---

    private List<GridTrack> buildTracks(TrackSize[] template, int count) {
        List<GridTrack> tracks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            TrackSize def = i < template.length ? template[i] : TrackSize.auto();
            tracks.add(new GridTrack(def));
        }
        return tracks;
    }

    // --- Phase 3: トラックサイズ解決 ---

    private void resolveTrackSizes(List<GridTrack> tracks, List<GridItem> items,
                                   boolean isColumn, int available, int trackCount) {
        // Step 1: Fixed + Auto トラックのベースサイズ
        resolveAutoAndFixedTracks(tracks, items, isColumn);

        // Step 2: スパンアイテムによる auto トラックの拡大
        resolveSpanItems(tracks, items, isColumn);

        // Step 3: Fr トラック → 残余スペースを分配
        int usedSpace = 0;
        float totalFr = 0;
        for (GridTrack track : tracks) {
            if (track.definition.getType() == TrackSizeType.FR) {
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
            for (GridTrack track : tracks) {
                if (track.definition.getType() == TrackSizeType.FR) {
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

    private void resolveAutoAndFixedTracks(List<GridTrack> tracks, List<GridItem> items,
                                           boolean isColumn) {
        for (int i = 0; i < tracks.size(); i++) {
            GridTrack track = tracks.get(i);
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
    private void resolvePreferredTrackSizes(List<GridTrack> tracks, List<GridItem> items,
                                            boolean isColumn) {
        for (int i = 0; i < tracks.size(); i++) {
            GridTrack track = tracks.get(i);
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

    private int maxPreferredSizeForTrack(List<GridItem> items, int trackIndex, boolean isColumn) {
        int max = 0;
        for (GridItem item : items) {
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

    private void resolveSpanItems(List<GridTrack> tracks, List<GridItem> items, boolean isColumn) {
        for (GridItem item : items) {
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
                    TrackSizeType type = tracks.get(t).definition.getType();
                    if (type == TrackSizeType.AUTO || type == TrackSizeType.FR) {
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

    private int spanBaseSize(List<GridTrack> tracks, int start, int span) {
        int size = 0;
        for (int i = start; i < start + span && i < tracks.size(); i++) {
            size += tracks.get(i).baseSize;
        }
        return size;
    }

    // --- オフセット計算 ---

    private void computeOffsets(List<GridTrack> tracks, int gap) {
        int offset = 0;
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).offset = offset;
            offset += tracks.get(i).baseSize;
            if (i < tracks.size() - 1) {
                offset += gap;
            }
        }
    }

    private int spanSize(List<GridTrack> tracks, int start, int span, int gap) {
        int size = 0;
        for (int i = start; i < start + span && i < tracks.size(); i++) {
            size += tracks.get(i).baseSize;
            if (i > start) {
                size += gap;
            }
        }
        return size;
    }

    private int sumTrackSizes(List<GridTrack> tracks) {
        int sum = 0;
        for (GridTrack track : tracks) {
            sum += track.baseSize;
        }
        return sum;
    }

    // --- アライメント解決 ---

    private JustifyItems resolveJustify(JustifySelf self) {
        if (self == JustifySelf.AUTO) return justifyItems;
        return switch (self) {
            case START -> JustifyItems.START;
            case END -> JustifyItems.END;
            case CENTER -> JustifyItems.CENTER;
            case STRETCH -> JustifyItems.STRETCH;
            default -> justifyItems;
        };
    }

    private AlignItems resolveAlign(AlignSelf self) {
        if (self == AlignSelf.AUTO) return alignItems;
        return switch (self) {
            case START -> AlignItems.START;
            case END -> AlignItems.END;
            case CENTER -> AlignItems.CENTER;
            case STRETCH -> AlignItems.STRETCH;
            default -> alignItems;
        };
    }

    // --- ユーティリティ ---

    private GridConstraints getConstraints(Component comp) {
        GridConstraints gc = constraintsMap.get(comp);
        return gc != null ? gc : new GridConstraints();
    }
}

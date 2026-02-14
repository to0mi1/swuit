package org.to0mi1.swuit.layout.flex;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CSS Flexbox に相当するレイアウトマネージャー。
 * <p>
 * 折り返し (wrap) 付きの柔軟な配置を提供する。
 * {@link CssFlexConstraints} でアイテム単位の制約を指定できる。
 *
 * <pre>{@code
 * JPanel panel = new JPanel(new CssFlexLayout(CssFlexDirection.ROW)
 *     .setCssFlexWrap(CssFlexWrap.WRAP)
 *     .setCssJustifyContent(CssJustifyContent.SPACE_BETWEEN));
 * panel.add(child, new CssFlexConstraints().flexGrow(1));
 * }</pre>
 */
public class CssFlexLayout implements LayoutManager2 {

    private CssFlexDirection flexDirection = CssFlexDirection.ROW;
    private CssFlexWrap flexWrap = CssFlexWrap.NOWRAP;
    private CssJustifyContent justifyContent = CssJustifyContent.FLEX_START;
    private CssAlignItems alignItems = CssAlignItems.STRETCH;
    private CssAlignContent alignContent = CssAlignContent.STRETCH;
    private int mainAxisGap = 0;
    private int crossAxisGap = 0;

    private final Map<Component, CssFlexConstraints> constraintsMap = new LinkedHashMap<>();

    public CssFlexLayout() {
    }

    public CssFlexLayout(CssFlexDirection flexDirection) {
        this.flexDirection = flexDirection;
    }

    // --- プロパティ (fluent setters) ---

    public CssFlexDirection getCssFlexDirection() {
        return flexDirection;
    }

    public CssFlexLayout setCssFlexDirection(CssFlexDirection flexDirection) {
        this.flexDirection = flexDirection;
        return this;
    }

    public CssFlexWrap getCssFlexWrap() {
        return flexWrap;
    }

    public CssFlexLayout setCssFlexWrap(CssFlexWrap flexWrap) {
        this.flexWrap = flexWrap;
        return this;
    }

    public CssJustifyContent getCssJustifyContent() {
        return justifyContent;
    }

    public CssFlexLayout setCssJustifyContent(CssJustifyContent justifyContent) {
        this.justifyContent = justifyContent;
        return this;
    }

    public CssAlignItems getCssAlignItems() {
        return alignItems;
    }

    public CssFlexLayout setCssAlignItems(CssAlignItems alignItems) {
        this.alignItems = alignItems;
        return this;
    }

    public CssAlignContent getCssAlignContent() {
        return alignContent;
    }

    public CssFlexLayout setCssAlignContent(CssAlignContent alignContent) {
        this.alignContent = alignContent;
        return this;
    }

    public int getMainAxisGap() {
        return mainAxisGap;
    }

    public CssFlexLayout setMainAxisGap(int mainAxisGap) {
        this.mainAxisGap = mainAxisGap;
        return this;
    }

    public int getCrossAxisGap() {
        return crossAxisGap;
    }

    public CssFlexLayout setCrossAxisGap(int crossAxisGap) {
        this.crossAxisGap = crossAxisGap;
        return this;
    }

    // --- LayoutManager2 ---

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints == null) {
            constraintsMap.put(comp, new CssFlexConstraints());
        } else if (constraints instanceof CssFlexConstraints fc) {
            constraintsMap.put(comp, fc.clone());
        } else {
            throw new IllegalArgumentException(
                    "constraints must be a CssFlexConstraints instance: " + constraints.getClass().getName());
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
        return computeSize(parent, false);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return computeSize(parent, true);
    }

    private Dimension computeSize(Container parent, boolean minimum) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            boolean horizontal = flexDirection.isHorizontal();

            int mainAxis = 0;
            int crossAxis = 0;
            int visibleCount = 0;

            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component child = parent.getComponent(i);
                if (!child.isVisible()) continue;

                CssFlexConstraints fc = getConstraints(child);
                Dimension d = minimum ? child.getMinimumSize() : child.getPreferredSize();
                Insets m = fc.getMargin();

                int itemMain, itemCross;
                if (horizontal) {
                    itemMain = d.width + m.left + m.right;
                    itemCross = d.height + m.top + m.bottom;
                } else {
                    itemMain = d.height + m.top + m.bottom;
                    itemCross = d.width + m.left + m.right;
                }

                mainAxis += itemMain;
                crossAxis = Math.max(crossAxis, itemCross);
                visibleCount++;
            }

            if (visibleCount > 1) {
                mainAxis += mainAxisGap * (visibleCount - 1);
            }

            if (horizontal) {
                return new Dimension(
                        mainAxis + insets.left + insets.right,
                        crossAxis + insets.top + insets.bottom);
            } else {
                return new Dimension(
                        crossAxis + insets.left + insets.right,
                        mainAxis + insets.top + insets.bottom);
            }
        }
    }

    // --- レイアウト ---

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int availableWidth = parent.getWidth() - insets.left - insets.right;
            int availableHeight = parent.getHeight() - insets.top - insets.bottom;
            boolean horizontal = flexDirection.isHorizontal();
            int availableMain = horizontal ? availableWidth : availableHeight;
            int availableCross = horizontal ? availableHeight : availableWidth;

            // Phase 1: アイテム収集 + order ソート
            List<CssFlexItem> items = collectItems(parent);
            if (items.isEmpty()) return;

            // Phase 2: flex-basis 計算
            computeFlexBasis(items, horizontal, availableMain);

            // Phase 3: ライン形成
            List<CssFlexLine> lines = formLines(items, horizontal, availableMain);

            // Phase 4-5: flexGrow/flexShrink + min/max クランプ
            for (CssFlexLine line : lines) {
                resolveFlexibleLengths(line, horizontal, availableMain);
            }

            // Phase 6: justifyContent 適用
            for (CssFlexLine line : lines) {
                applyCssJustifyContent(line, horizontal, availableMain);
            }

            // Phase 7: ライン副軸サイズ + alignContent 適用
            computeLineCrossSizes(lines, horizontal);
            applyCssAlignContent(lines, availableCross);

            // Phase 8: alignItems/alignSelf 適用
            for (CssFlexLine line : lines) {
                applyCssAlignItems(line, horizontal);
            }

            // Phase 9: 座標変換 + 配置
            placeBounds(lines, horizontal, insets, flexDirection.isReverse(),
                    flexWrap == CssFlexWrap.WRAP_REVERSE, availableWidth, availableHeight);
        }
    }

    // --- Phase 1: アイテム収集 ---

    private List<CssFlexItem> collectItems(Container parent) {
        List<CssFlexItem> items = new ArrayList<>();
        int addOrder = 0;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) continue;
            CssFlexConstraints fc = getConstraints(child);
            items.add(new CssFlexItem(child, fc, addOrder++));
        }
        items.sort(Comparator.comparingInt((CssFlexItem fi) -> fi.constraints.getOrder())
                .thenComparingInt(fi -> fi.addOrder));
        return items;
    }

    // --- Phase 2: flex-basis ---

    private void computeFlexBasis(List<CssFlexItem> items, boolean horizontal, int availableMain) {
        for (CssFlexItem item : items) {
            float basisPercent = item.constraints.getFlexBasisPercent();
            if (basisPercent >= 0) {
                item.flexBasis = Math.round(availableMain * basisPercent / 100f);
            } else {
                Dimension pref = item.component.getPreferredSize();
                item.flexBasis = horizontal ? pref.width : pref.height;
            }
        }
    }

    // --- Phase 3: ライン形成 ---

    private List<CssFlexLine> formLines(List<CssFlexItem> items, boolean horizontal, int availableMain) {
        List<CssFlexLine> lines = new ArrayList<>();
        CssFlexLine currentLine = new CssFlexLine();
        int lineMainUsed = 0;

        for (int i = 0; i < items.size(); i++) {
            CssFlexItem item = items.get(i);
            int itemMainWithMargin = item.flexBasis + item.mainMargin(horizontal);
            if (!currentLine.items.isEmpty()) {
                itemMainWithMargin += mainAxisGap;
            }

            if (flexWrap != CssFlexWrap.NOWRAP && !currentLine.items.isEmpty()
                    && lineMainUsed + itemMainWithMargin > availableMain) {
                currentLine.mainSize = lineMainUsed;
                lines.add(currentLine);
                currentLine = new CssFlexLine();
                lineMainUsed = 0;
                itemMainWithMargin = item.flexBasis + item.mainMargin(horizontal);
            }

            currentLine.add(item);
            lineMainUsed += itemMainWithMargin;
        }
        currentLine.mainSize = lineMainUsed;
        lines.add(currentLine);
        return lines;
    }

    // --- Phase 4-5: flexGrow/flexShrink + min/max (CSS §9.7) ---

    private void resolveFlexibleLengths(CssFlexLine line, boolean horizontal, int availableMain) {
        List<CssFlexItem> items = line.items;
        int gapTotal = mainAxisGap * (items.size() - 1);

        // 初期化: mainSize = flexBasis
        for (CssFlexItem item : items) {
            item.mainSize = item.flexBasis;
            item.frozen = false;
        }

        // 初期余剰/不足を計算
        int usedMain = gapTotal;
        for (CssFlexItem item : items) {
            usedMain += item.flexBasis + item.mainMargin(horizontal);
        }
        int initialFreeSpace = availableMain - usedMain;
        boolean growing = initialFreeSpace >= 0;

        // flex factor が 0 のアイテムをフリーズ
        for (CssFlexItem item : items) {
            float flex = growing ? item.constraints.getFlexGrow() : item.constraints.getFlexShrink();
            if (flex <= 0) {
                item.frozen = true;
            }
        }

        // フリーズ再分配ループ
        for (;;) {
            // 未フリーズのアイテムがあるか
            boolean hasUnfrozen = false;
            for (CssFlexItem item : items) {
                if (!item.frozen) { hasUnfrozen = true; break; }
            }
            if (!hasUnfrozen) break;

            // 残余スペース計算（フリーズ済みは mainSize、未フリーズは flexBasis）
            usedMain = gapTotal;
            for (CssFlexItem item : items) {
                usedMain += (item.frozen ? item.mainSize : item.flexBasis)
                        + item.mainMargin(horizontal);
            }
            int freeSpace = availableMain - usedMain;

            // 合計 flex factor
            float totalFlex = 0;
            for (CssFlexItem item : items) {
                if (!item.frozen) {
                    totalFlex += growing
                            ? item.constraints.getFlexGrow()
                            : item.constraints.getFlexShrink() * item.flexBasis;
                }
            }
            if (totalFlex <= 0) break;

            // 分配
            float remainingFlex = totalFlex;
            int remainingSpace = freeSpace;
            boolean anyViolation = false;

            for (CssFlexItem item : items) {
                if (item.frozen) continue;

                float flex = growing
                        ? item.constraints.getFlexGrow()
                        : item.constraints.getFlexShrink() * item.flexBasis;

                int share;
                if (remainingFlex == flex) {
                    share = remainingSpace;
                } else {
                    share = Math.round(freeSpace * flex / totalFlex);
                }

                int newSize = item.flexBasis + share;

                // min/max クランプ
                int minSize = item.mainMinSize(horizontal);
                int maxSize = item.mainMaxSize(horizontal);

                if (newSize < minSize) {
                    item.mainSize = minSize;
                    item.frozen = true;
                    anyViolation = true;
                } else if (newSize > maxSize) {
                    item.mainSize = maxSize;
                    item.frozen = true;
                    anyViolation = true;
                } else {
                    item.mainSize = newSize;
                }

                remainingSpace -= (item.mainSize - item.flexBasis);
                remainingFlex -= flex;
            }

            if (!anyViolation) break;
        }
    }

    // --- Phase 6: justifyContent ---

    private void applyCssJustifyContent(CssFlexLine line, boolean horizontal, int availableMain) {
        List<CssFlexItem> items = line.items;
        int count = items.size();
        int gapTotal = mainAxisGap * (count - 1);

        int usedMain = gapTotal;
        for (CssFlexItem item : items) {
            usedMain += item.mainSize + item.mainMargin(horizontal);
        }
        int freeSpace = Math.max(0, availableMain - usedMain);

        int offset = 0;
        int spaceBetween = mainAxisGap;

        switch (justifyContent) {
            case FLEX_START:
                break;
            case FLEX_END:
                offset = freeSpace;
                break;
            case CENTER:
                offset = freeSpace / 2;
                break;
            case SPACE_BETWEEN:
                if (count > 1) {
                    spaceBetween = mainAxisGap + freeSpace / (count - 1);
                }
                break;
            case SPACE_AROUND:
                if (count > 0) {
                    int around = freeSpace / count;
                    offset = around / 2;
                    spaceBetween = mainAxisGap + around;
                }
                break;
            case SPACE_EVENLY:
                if (count > 0) {
                    int evenly = freeSpace / (count + 1);
                    offset = evenly;
                    spaceBetween = mainAxisGap + evenly;
                }
                break;
        }

        // 主軸位置を設定
        int pos = offset;
        for (int i = 0; i < count; i++) {
            CssFlexItem item = items.get(i);
            item.mainPos = pos + item.mainMarginStart(horizontal);
            pos += item.mainMarginStart(horizontal) + item.mainSize
                    + (item.margin().right + item.margin().left - item.mainMarginStart(horizontal) * 2
                    + item.mainMargin(horizontal)) / 2;
            // 簡化: mainPos は margin.start を加算、mainSize 後に margin.end を加算
            pos = item.mainPos + item.mainSize
                    + (horizontal ? item.margin().right : item.margin().bottom);
            if (i < count - 1) {
                pos += spaceBetween;
            }
        }
    }

    // --- Phase 7: ライン副軸サイズ ---

    private void computeLineCrossSizes(List<CssFlexLine> lines, boolean horizontal) {
        for (CssFlexLine line : lines) {
            int maxCross = 0;
            for (CssFlexItem item : line.items) {
                Dimension pref = item.component.getPreferredSize();
                int itemCross = horizontal ? pref.height : pref.width;
                itemCross += item.crossMargin(horizontal);
                maxCross = Math.max(maxCross, itemCross);
            }
            line.crossSize = maxCross;
        }
    }

    // --- Phase 7b: alignContent ---

    private void applyCssAlignContent(List<CssFlexLine> lines, int availableCross) {
        int lineCount = lines.size();
        int totalCross = 0;
        for (CssFlexLine line : lines) {
            totalCross += line.crossSize;
        }
        if (lineCount > 1) {
            totalCross += crossAxisGap * (lineCount - 1);
        }

        int freeSpace = Math.max(0, availableCross - totalCross);

        int offset = 0;
        int gap = crossAxisGap;

        switch (alignContent) {
            case FLEX_START:
                break;
            case FLEX_END:
                offset = freeSpace;
                break;
            case CENTER:
                offset = freeSpace / 2;
                break;
            case SPACE_BETWEEN:
                if (lineCount > 1) {
                    gap = crossAxisGap + freeSpace / (lineCount - 1);
                }
                break;
            case SPACE_AROUND:
                if (lineCount > 0) {
                    int around = freeSpace / lineCount;
                    offset = around / 2;
                    gap = crossAxisGap + around;
                }
                break;
            case STRETCH:
                if (lineCount > 0) {
                    int extra = freeSpace / lineCount;
                    for (CssFlexLine line : lines) {
                        line.crossSize += extra;
                    }
                }
                break;
        }

        int pos = offset;
        for (int i = 0; i < lineCount; i++) {
            lines.get(i).crossOffset = pos;
            pos += lines.get(i).crossSize;
            if (i < lineCount - 1) {
                pos += gap;
            }
        }
    }

    // --- Phase 8: alignItems / alignSelf ---

    private void applyCssAlignItems(CssFlexLine line, boolean horizontal) {
        for (CssFlexItem item : line.items) {
            CssAlignSelf self = item.constraints.getCssAlignSelf();
            CssAlignItems effective;
            if (self == CssAlignSelf.AUTO) {
                effective = alignItems;
            } else {
                effective = CssAlignItems.valueOf(self.name());
            }

            int lineCross = line.crossSize;
            int itemCrossMargin = item.crossMargin(horizontal);
            int availCross = lineCross - itemCrossMargin;

            Dimension pref = item.component.getPreferredSize();
            int prefCross = horizontal ? pref.height : pref.width;

            switch (effective) {
                case STRETCH:
                    item.crossSize = Math.max(0, availCross);
                    item.crossPos = line.crossOffset + item.crossMarginStart(horizontal);
                    // min/max クランプ
                    item.crossSize = Math.min(item.crossSize, item.crossMaxSize(horizontal));
                    item.crossSize = Math.max(item.crossSize, item.crossMinSize(horizontal));
                    break;
                case FLEX_START:
                    item.crossSize = prefCross;
                    item.crossPos = line.crossOffset + item.crossMarginStart(horizontal);
                    break;
                case FLEX_END:
                    item.crossSize = prefCross;
                    item.crossPos = line.crossOffset + lineCross - item.crossSize
                            - (horizontal ? item.margin().bottom : item.margin().right);
                    break;
                case CENTER:
                    item.crossSize = prefCross;
                    item.crossPos = line.crossOffset + item.crossMarginStart(horizontal)
                            + (availCross - prefCross) / 2;
                    break;
            }
        }
    }

    // --- Phase 9: 座標変換 + 配置 ---

    private void placeBounds(List<CssFlexLine> lines, boolean horizontal,
                             Insets insets, boolean mainReverse, boolean crossReverse,
                             int availableWidth, int availableHeight) {
        int availableMain = horizontal ? availableWidth : availableHeight;
        int availableCross = horizontal ? availableHeight : availableWidth;

        for (CssFlexLine line : lines) {
            for (CssFlexItem item : line.items) {
                int mainP = item.mainPos;
                int crossP = item.crossPos;
                int mainS = item.mainSize;
                int crossS = item.crossSize;

                // reverse 方向の座標反転
                if (mainReverse) {
                    mainP = availableMain - mainP - mainS;
                }
                if (crossReverse) {
                    crossP = availableCross - crossP - crossS;
                }

                int x, y, w, h;
                if (horizontal) {
                    x = insets.left + mainP;
                    y = insets.top + crossP;
                    w = mainS;
                    h = crossS;
                } else {
                    x = insets.left + crossP;
                    y = insets.top + mainP;
                    w = crossS;
                    h = mainS;
                }

                item.component.setBounds(x, y, w, h);
            }
        }
    }

    // --- ユーティリティ ---

    private CssFlexConstraints getConstraints(Component comp) {
        CssFlexConstraints fc = constraintsMap.get(comp);
        return fc != null ? fc : new CssFlexConstraints();
    }
}

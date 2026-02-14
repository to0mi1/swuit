package org.to0mi1.swuit.layout.grid;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridBoxLayoutTest {

    // === ヘルパー ===

    static JPanel createContainer(GridBoxLayout layout, int width, int height) {
        JPanel panel = new JPanel(layout);
        panel.setSize(width, height);
        return panel;
    }

    static JPanel createContainerWithInsets(GridBoxLayout layout, int width, int height,
                                            int top, int left, int bottom, int right) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(top, left, bottom, right));
        panel.setSize(width, height);
        return panel;
    }

    static Component fixedSize(int width, int height) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width, height));
        label.setMinimumSize(new Dimension(width, height));
        return label;
    }

    static Rectangle boundsOf(Component c) {
        return c.getBounds();
    }

    // === 固定トラック ===

    @Test
    void fixedColumns_basic() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fr(1));
        JPanel panel = createContainer(layout, 400, 100);

        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.doLayout();

        // STRETCH (デフォルト): セル幅に引き伸ばされる
        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(a));
        assertEquals(new Rectangle(100, 0, 200, 100), boundsOf(b));
    }

    @Test
    void fixedRows_basic() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fixed(80));
        JPanel panel = createContainer(layout, 200, 200);

        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(0).row(1));
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 200, 50), boundsOf(a));
        assertEquals(new Rectangle(0, 50, 200, 80), boundsOf(b));
    }

    // === fr 分配 ===

    @Test
    void frColumns_equalDistribution() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fr(1), TrackSize.fr(1))
                .setRowTemplate(TrackSize.fr(1));
        JPanel panel = createContainer(layout, 400, 100);

        Component a = fixedSize(50, 30);
        Component b = fixedSize(50, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 200, 100), boundsOf(a));
        assertEquals(new Rectangle(200, 0, 200, 100), boundsOf(b));
    }

    @Test
    void frColumns_weightedDistribution() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fr(1), TrackSize.fr(2))
                .setRowTemplate(TrackSize.fr(1));
        JPanel panel = createContainer(layout, 300, 100);

        Component a = fixedSize(50, 30);
        Component b = fixedSize(50, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(a));
        assertEquals(new Rectangle(100, 0, 200, 100), boundsOf(b));
    }

    @Test
    void fixedAndFr_mixed() {
        // 100px 固定 + 残りを 1:2 で分配
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fr(1), TrackSize.fr(2))
                .setRowTemplate(TrackSize.fr(1));
        JPanel panel = createContainer(layout, 400, 100);

        Component a = fixedSize(50, 30);
        Component b = fixedSize(50, 30);
        Component c = fixedSize(50, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.add(c, new GridConstraints().column(2).row(0));
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(a));
        assertEquals(new Rectangle(100, 0, 100, 100), boundsOf(b)); // 300 * 1/3 = 100
        assertEquals(new Rectangle(200, 0, 200, 100), boundsOf(c)); // 300 * 2/3 = 200
    }

    // === auto サイズ ===

    @Test
    void autoColumns_fitToContent() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.auto(), TrackSize.auto());
        JPanel panel = createContainer(layout, 400, 100);

        Component a = fixedSize(80, 30);
        Component b = fixedSize(120, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.doLayout();

        assertEquals(80, boundsOf(a).width);
        assertEquals(120, boundsOf(b).width);
    }

    @Test
    void autoAndFr_mixed() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.auto(), TrackSize.fr(1));
        JPanel panel = createContainer(layout, 400, 100);

        Component a = fixedSize(100, 30);
        Component b = fixedSize(50, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.doLayout();

        assertEquals(100, boundsOf(a).width);
        assertEquals(300, boundsOf(b).width); // 400 - 100 = 300
    }

    // === gap ===

    @Test
    void columnGap() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fr(1), TrackSize.fr(1))
                .setRowTemplate(TrackSize.fr(1))
                .setColumnGap(20);
        JPanel panel = createContainer(layout, 420, 100);

        Component a = fixedSize(50, 30);
        Component b = fixedSize(50, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.doLayout();

        // (420 - 20) / 2 = 200
        assertEquals(new Rectangle(0, 0, 200, 100), boundsOf(a));
        assertEquals(new Rectangle(220, 0, 200, 100), boundsOf(b));
    }

    @Test
    void rowGap() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(40), TrackSize.fixed(40))
                .setRowGap(10);
        JPanel panel = createContainer(layout, 200, 200);

        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(0).row(1));
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 200, 40), boundsOf(a));
        assertEquals(new Rectangle(0, 50, 200, 40), boundsOf(b));
    }

    // === insets ===

    @Test
    void containerInsets() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fr(1))
                .setRowTemplate(TrackSize.fr(1));
        JPanel panel = createContainerWithInsets(layout, 400, 200, 10, 20, 10, 20);

        Component a = fixedSize(50, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.doLayout();

        // 利用可能: 400-20-20=360, 200-10-10=180
        assertEquals(new Rectangle(20, 10, 360, 180), boundsOf(a));
    }

    // === preferredLayoutSize ===

    @Test
    void preferredSize_fixedTracks() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fixed(60));
        JPanel panel = createContainer(layout, 400, 200);

        panel.add(fixedSize(80, 30), new GridConstraints().column(0).row(0));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(300, pref.width);  // 100 + 200
        assertEquals(110, pref.height); // 50 + 60
    }

    @Test
    void preferredSize_withGaps() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fixed(50))
                .setColumnGap(10).setRowGap(5);
        JPanel panel = createContainer(layout, 400, 200);

        panel.add(fixedSize(80, 30), new GridConstraints().column(0).row(0));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(210, pref.width);  // 100+100+10
        assertEquals(105, pref.height); // 50+50+5
    }

    @Test
    void preferredSize_withInsets() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fixed(50));
        JPanel panel = createContainerWithInsets(layout, 400, 200, 10, 20, 10, 20);

        panel.add(fixedSize(80, 30), new GridConstraints().column(0).row(0));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(140, pref.width);  // 100 + 20 + 20
        assertEquals(70, pref.height);  // 50 + 10 + 10
    }

    @Test
    void preferredSize_autoTracks() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.auto(), TrackSize.auto());
        JPanel panel = createContainer(layout, 400, 200);

        panel.add(fixedSize(80, 30), new GridConstraints().column(0).row(0));
        panel.add(fixedSize(120, 50), new GridConstraints().column(1).row(0));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(200, pref.width);  // 80 + 120
        assertEquals(50, pref.height);  // max(30, 50)
    }

    // === preferredLayoutSize: fr トラックはコンテンツにフォールバック ===

    @Test
    void preferredSize_frTracks_fallsBackToContent() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fr(1), TrackSize.fr(2));
        JPanel panel = createContainer(layout, 400, 200);

        panel.add(fixedSize(80, 30), new GridConstraints().column(0).row(0));
        panel.add(fixedSize(120, 50), new GridConstraints().column(1).row(0));

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(200, pref.width);  // fr → auto: 80 + 120
        assertEquals(50, pref.height);  // max(30, 50)
    }

    // === minimumLayoutSize は preferredLayoutSize に委譲 ===

    @Test
    void minimumSize_delegatesToPreferred() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(50));
        JPanel panel = createContainer(layout, 400, 200);

        panel.add(fixedSize(80, 30), new GridConstraints().column(0).row(0));

        Dimension pref = layout.preferredLayoutSize(panel);
        Dimension min = layout.minimumLayoutSize(panel);
        assertEquals(pref, min);
    }

    // === removeLayoutComponent ===

    @Test
    void removeLayoutComponent_componentNoLongerLaidOut() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fr(1));
        JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.doLayout();

        // b を削除
        panel.remove(b);
        panel.doLayout();

        // a はまだレイアウトされる
        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(a));
    }

    // === addLayoutComponent: 不正な constraints ===

    @Test
    void addLayoutComponent_rejectsInvalidConstraints() {
        GridBoxLayout layout = new GridBoxLayout();
        JPanel panel = new JPanel(layout);

        assertThrows(IllegalArgumentException.class, () ->
                panel.add(fixedSize(80, 30), "invalid"));
    }

    @Test
    void addLayoutComponent_acceptsNull() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100));
        JPanel panel = createContainer(layout, 100, 100);

        Component a = fixedSize(80, 30);
        panel.add(a); // constraints = null → デフォルト GridConstraints
        panel.doLayout();

        // エラーなくレイアウトされる
        assertTrue(boundsOf(a).width > 0);
    }

    // === invisible コンポーネントはスキップ ===

    @Test
    void invisibleComponents_skipped() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fr(1));
        JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        Component invisible = fixedSize(80, 30);
        invisible.setVisible(false);
        Component b = fixedSize(80, 30);

        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(invisible, new GridConstraints().column(1).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 100, 100), boundsOf(a));
        assertEquals(new Rectangle(100, 0, 100, 100), boundsOf(b));
        // invisible のサイズは変更されない (setBounds は呼ばれない)
    }

    // === 空コンテナ ===

    @Test
    void emptyContainer_doLayout_noError() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100));
        JPanel panel = createContainer(layout, 400, 200);

        panel.doLayout(); // 例外なし
    }

    @Test
    void emptyContainer_preferredSize_returnsInsets() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100));
        JPanel panel = createContainerWithInsets(layout, 400, 200, 10, 20, 10, 20);

        Dimension pref = layout.preferredLayoutSize(panel);
        assertEquals(40, pref.width);  // insets のみ: 20 + 20
        assertEquals(20, pref.height); // insets のみ: 10 + 10
    }

    // === columnGap + rowGap 同時適用 ===

    @Test
    void columnGap_and_rowGap_combined() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(100), TrackSize.fixed(100))
                .setRowTemplate(TrackSize.fixed(50), TrackSize.fixed(50))
                .setColumnGap(10).setRowGap(20);
        JPanel panel = createContainer(layout, 210, 120);

        Component a = fixedSize(80, 30);
        Component b = fixedSize(80, 30);
        Component c = fixedSize(80, 30);
        Component d = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(1).row(0));
        panel.add(c, new GridConstraints().column(0).row(1));
        panel.add(d, new GridConstraints().column(1).row(1));
        panel.doLayout();

        assertEquals(new Rectangle(0, 0, 100, 50), boundsOf(a));
        assertEquals(new Rectangle(110, 0, 100, 50), boundsOf(b));    // x = 100 + 10
        assertEquals(new Rectangle(0, 70, 100, 50), boundsOf(c));     // y = 50 + 20
        assertEquals(new Rectangle(110, 70, 100, 50), boundsOf(d));
    }

    // === margin + auto トラック ===

    @Test
    void margin_affectsAutoTrackSize() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.auto());
        JPanel panel = createContainer(layout, 400, 100);

        Component a = fixedSize(80, 30);
        panel.add(a, new GridConstraints().column(0).row(0).margin(0, 10, 0, 10));
        panel.doLayout();

        // auto トラックはコンテンツ + margin を考慮: 80 + 10 + 10 = 100
        assertEquals(100, boundsOf(a).x + boundsOf(a).width + 10);
        assertEquals(10, boundsOf(a).x); // margin left
        assertEquals(80, boundsOf(a).width); // STRETCH だが margin 差し引き後
    }

    // === 同一セルに複数アイテム (重複) ===

    @Test
    void sameCell_overlappingItems() {
        GridBoxLayout layout = new GridBoxLayout()
                .setColumnTemplate(TrackSize.fixed(200))
                .setRowTemplate(TrackSize.fixed(100));
        JPanel panel = createContainer(layout, 200, 100);

        Component a = fixedSize(80, 30);
        Component b = fixedSize(60, 40);
        panel.add(a, new GridConstraints().column(0).row(0));
        panel.add(b, new GridConstraints().column(0).row(0));
        panel.doLayout();

        // 両方とも同じセルにレイアウトされる (重なる)
        assertEquals(new Rectangle(0, 0, 200, 100), boundsOf(a));
        assertEquals(new Rectangle(0, 0, 200, 100), boundsOf(b));
    }

    // === TrackSize バリデーション ===

    @Test
    void trackSize_fixed_rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> TrackSize.fixed(-1));
    }

    @Test
    void trackSize_fr_rejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> TrackSize.fr(0));
    }

    @Test
    void trackSize_fr_rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> TrackSize.fr(-1));
    }

    @Test
    void trackSize_fixed_acceptsZero() {
        TrackSize ts = TrackSize.fixed(0);
        assertNotNull(ts);
    }
}

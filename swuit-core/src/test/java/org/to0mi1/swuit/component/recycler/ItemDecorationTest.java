package org.to0mi1.swuit.component.recycler;

import org.junit.jupiter.api.Test;
import org.to0mi1.swuit.layout.Orientation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemDecorationTest {

    static class TestViewHolder extends RecyclerPane.ViewHolder {
        TestViewHolder(JComponent itemView) {
            super(itemView);
        }
    }

    static class SimpleAdapter extends RecyclerPane.Adapter<TestViewHolder> {
        int itemCount;

        SimpleAdapter(int itemCount) {
            this.itemCount = itemCount;
        }

        @Override
        public TestViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(200, 40));
            return new TestViewHolder(panel);
        }

        @Override
        public void onBindViewHolder(TestViewHolder holder, int position) {
            holder.itemView.setName("item-" + position);
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }
    }

    static class OffsetDecoration extends ItemDecoration {
        final Insets offsets;

        OffsetDecoration(Insets offsets) {
            this.offsets = offsets;
        }

        @Override
        public Insets getItemOffsets(int position) {
            return offsets;
        }
    }

    static class TrackingDecoration extends ItemDecoration {
        final List<String> drawCalls = new ArrayList<>();
        final List<String> drawOverCalls = new ArrayList<>();

        @Override
        public void onDraw(Graphics g, RecyclerPane parent) {
            drawCalls.add("onDraw");
        }

        @Override
        public void onDrawOver(Graphics g, RecyclerPane parent) {
            drawOverCalls.add("onDrawOver");
        }

        @Override
        public Insets getItemOffsets(int position) {
            return new Insets(0, 0, 0, 0);
        }
    }

    static RecyclerPane createPane(int width, int height, RecyclerPane.LayoutManager lm,
                                   RecyclerPane.Adapter<?> adapter) {
        RecyclerPane pane = new RecyclerPane();
        pane.setLayoutManager(lm);
        pane.setAdapter(adapter);
        JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.setBounds(0, 0, width, height);
        scrollPane.doLayout();
        scrollPane.getViewport().doLayout();
        pane.doLayout();
        return pane;
    }

    @Test
    void getItemOffsets_appliedToLayout() {
        SimpleAdapter adapter = new SimpleAdapter(5);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = new RecyclerPane();
        pane.addItemDecoration(new OffsetDecoration(new Insets(5, 10, 5, 10)));
        pane.setLayoutManager(lm);
        pane.setAdapter(adapter);

        JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.setBounds(0, 0, 300, 400);
        scrollPane.doLayout();
        scrollPane.getViewport().doLayout();
        pane.doLayout();

        for (int i = 0; i < pane.getComponentCount(); i++) {
            Component comp = pane.getComponent(i);
            if (comp.isVisible() && comp.getName() != null) {
                // offset left=10, right=10 → 幅は viewport幅 - 20
                int vpWidth = scrollPane.getViewport().getWidth();
                assertEquals(vpWidth - 20, comp.getWidth(),
                        "オフセット適用後の幅: " + comp.getName());
                // x = offset.left = 10
                assertEquals(10, comp.getX(),
                        "オフセット適用後のX座標: " + comp.getName());
            }
        }
    }

    @Test
    void onDraw_calledDuringPaint() {
        SimpleAdapter adapter = new SimpleAdapter(3);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        TrackingDecoration decoration = new TrackingDecoration();

        RecyclerPane pane = createPane(200, 200, lm, adapter);
        pane.addItemDecoration(decoration);

        // paint を呼び出す
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(200, 200,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        pane.paint(g);
        g.dispose();

        assertFalse(decoration.drawCalls.isEmpty(), "onDraw が呼ばれる");
        assertFalse(decoration.drawOverCalls.isEmpty(), "onDrawOver が呼ばれる");
    }

    @Test
    void multipleDecorations_allApplied() {
        SimpleAdapter adapter = new SimpleAdapter(3);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);
        RecyclerPane pane = new RecyclerPane();
        pane.addItemDecoration(new OffsetDecoration(new Insets(5, 0, 5, 0)));
        pane.addItemDecoration(new OffsetDecoration(new Insets(3, 0, 3, 0)));
        pane.setLayoutManager(lm);
        pane.setAdapter(adapter);

        JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.setBounds(0, 0, 200, 500);
        scrollPane.doLayout();
        scrollPane.getViewport().doLayout();
        pane.doLayout();

        // 合計 offset: top=8, bottom=8 → アイテム間隔 = 40 + 16 = 56
        if (pane.getComponentCount() >= 2) {
            Component c0 = null, c1 = null;
            for (int i = 0; i < pane.getComponentCount(); i++) {
                Component comp = pane.getComponent(i);
                if ("item-0".equals(comp.getName())) c0 = comp;
                if ("item-1".equals(comp.getName())) c1 = comp;
            }
            if (c0 != null && c1 != null) {
                int spacing = c1.getY() - c0.getY();
                // item0: y = 8, height = 40, bottom_offset = 8 → item1 starts at 8 + 40 + 8 + 8 = 64?
                // Actually: item0.y = offsets.top(8), item1.y = 8 + 40 + 8 + 8 = 64
                assertEquals(56, spacing,
                        "複数装飾のオフセットが合算される");
            }
        }
    }

    @Test
    void removeDecoration_noLongerApplied() {
        SimpleAdapter adapter = new SimpleAdapter(3);
        LinearLayoutManager lm = new LinearLayoutManager(Orientation.VERTICAL);

        OffsetDecoration decoration = new OffsetDecoration(new Insets(10, 10, 10, 10));
        RecyclerPane pane = new RecyclerPane();
        pane.addItemDecoration(decoration);
        pane.setLayoutManager(lm);
        pane.setAdapter(adapter);

        JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.setBounds(0, 0, 200, 400);
        scrollPane.doLayout();
        scrollPane.getViewport().doLayout();
        pane.doLayout();

        // 装飾あり → X=10
        Component firstBefore = null;
        for (int i = 0; i < pane.getComponentCount(); i++) {
            if ("item-0".equals(pane.getComponent(i).getName())) {
                firstBefore = pane.getComponent(i);
                break;
            }
        }
        assertNotNull(firstBefore);
        assertEquals(10, firstBefore.getX(), "装飾あり → X=10");

        // 装飾を除去して再レイアウト
        pane.removeItemDecoration(decoration);
        pane.doLayout();

        Component firstAfter = null;
        for (int i = 0; i < pane.getComponentCount(); i++) {
            if ("item-0".equals(pane.getComponent(i).getName())) {
                firstAfter = pane.getComponent(i);
                break;
            }
        }
        assertNotNull(firstAfter);
        assertEquals(0, firstAfter.getX(), "装飾除去後 → X=0");
    }
}

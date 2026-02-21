package org.to0mi1.swuit.demo.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.to0mi1.swuit.component.recycler.GridLayoutManager;
import org.to0mi1.swuit.component.recycler.LinearLayoutManager;
import org.to0mi1.swuit.component.recycler.RecyclerPane;
import org.to0mi1.swuit.component.recycler.StaggeredGridLayoutManager;
import org.to0mi1.swuit.layout.Orientation;

/**
 * RecyclerPane デモパネル生成ユーティリティ。
 */
public final class RecyclerDemos {

    private static final Color[] COLORS = {
            new Color(0x4CAF50), new Color(0x2196F3), new Color(0xFF9800),
            new Color(0xE91E63), new Color(0x9C27B0), new Color(0x3F51B5),
            new Color(0x00BCD4), new Color(0xFF5722), new Color(0x607D8B),
            new Color(0xFFC107),
    };

    private RecyclerDemos() {
    }

    // === Adapter 定義 ===

    /** シンプルテキストリスト用 ViewHolder */
    static class TextViewHolder extends RecyclerPane.ViewHolder {
        final JLabel label;

        TextViewHolder(JLabel label) {
            super(label);
            this.label = label;
        }
    }

    /** シンプルテキストリスト用 Adapter */
    static class TextAdapter extends RecyclerPane.Adapter<TextViewHolder> {
        final int count;

        TextAdapter(int count) {
            this.count = count;
        }

        @Override
        public TextViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setFont(label.getFont().deriveFont(14f));
            label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            label.setPreferredSize(new Dimension(0, 36));
            return new TextViewHolder(label);
        }

        @Override
        public void onBindViewHolder(TextViewHolder holder, int position) {
            holder.label.setText("Item " + position);
            holder.label.setBackground(COLORS[position % COLORS.length]);
            holder.label.setForeground(Color.WHITE);
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }

    /** 複合コンポーネント用 ViewHolder */
    static class ComplexViewHolder extends RecyclerPane.ViewHolder {
        final JLabel title;
        final JLabel subtitle;
        final JProgressBar progress;
        final JButton action;

        ComplexViewHolder(JPanel panel, JLabel title, JLabel subtitle,
                          JProgressBar progress, JButton action) {
            super(panel);
            this.title = title;
            this.subtitle = subtitle;
            this.progress = progress;
            this.action = action;
        }
    }

    /** 複合コンポーネント用 Adapter */
    static class ComplexAdapter extends RecyclerPane.Adapter<ComplexViewHolder> {
        final int count;

        ComplexAdapter(int count) {
            this.count = count;
        }

        @Override
        public ComplexViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
            JPanel panel = new JPanel(new BorderLayout(8, 4));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            panel.setOpaque(true);

            JLabel title = new JLabel();
            title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

            JLabel subtitle = new JLabel();
            subtitle.setFont(subtitle.getFont().deriveFont(11f));
            subtitle.setForeground(new Color(0x757575));

            JPanel textPanel = new JPanel(new BorderLayout(0, 2));
            textPanel.setOpaque(false);
            textPanel.add(title, BorderLayout.NORTH);
            textPanel.add(subtitle, BorderLayout.SOUTH);

            JProgressBar progress = new JProgressBar(0, 100);
            progress.setPreferredSize(new Dimension(80, 16));

            JButton action = new JButton("Detail");
            action.setFocusable(false);

            JPanel rightPanel = new JPanel(new BorderLayout(4, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(progress, BorderLayout.CENTER);
            rightPanel.add(action, BorderLayout.EAST);

            panel.add(textPanel, BorderLayout.CENTER);
            panel.add(rightPanel, BorderLayout.EAST);
            panel.setPreferredSize(new Dimension(0, 56));

            return new ComplexViewHolder(panel, title, subtitle, progress, action);
        }

        @Override
        public void onBindViewHolder(ComplexViewHolder holder, int position) {
            holder.title.setText("Task #" + position);
            holder.subtitle.setText("Description of task " + position);
            holder.progress.setValue((position * 17) % 101);
            holder.itemView.setBackground(position % 2 == 0 ? Color.WHITE : new Color(0xFAFAFA));
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }

    /** グリッド用 ViewHolder */
    static class GridViewHolder extends RecyclerPane.ViewHolder {
        final JLabel label;

        GridViewHolder(JPanel panel, JLabel label) {
            super(panel);
            this.label = label;
        }
    }

    /** グリッド用 Adapter */
    static class GridAdapter extends RecyclerPane.Adapter<GridViewHolder> {
        final int count;

        GridAdapter(int count) {
            this.count = count;
        }

        @Override
        public GridViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            JLabel label = new JLabel("", SwingConstants.CENTER);
            label.setOpaque(true);
            label.setForeground(Color.WHITE);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
            panel.add(label, BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(0, 80));
            return new GridViewHolder(panel, label);
        }

        @Override
        public void onBindViewHolder(GridViewHolder holder, int position) {
            holder.label.setText(String.valueOf(position));
            holder.label.setBackground(COLORS[position % COLORS.length]);
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }

    /**
     * Staggered 用 Adapter（アスペクト比ベースの高さ）。
     * <p>
     * 横長・正方形・縦長を重み付きランダムで混在させ、
     * 画像ギャラリーに近いバリエーションを再現する。
     */
    static class StaggeredAdapter extends RecyclerPane.Adapter<GridViewHolder> {
        // 横長 (landscape) + 正方形 + 縦長 (portrait) のバリエーション
        private static final String[] ASPECT_LABELS = {
                "1:1",          // 正方形
                "4:3", "3:4",   // 標準写真 + 回転
                "3:2", "2:3",   // 一眼写真 + 回転
                "16:9", "9:16", // ワイド映像 + 回転
                "16:10", "10:16"// PC画面比 + 回転
        };
        private static final int[] W_RATIOS = {1, 4, 3, 3, 2, 16,  9, 16, 10};
        private static final int[] H_RATIOS = {1, 3, 4, 2, 3,  9, 16, 10, 16};
        // 重み: 横長多め、正方形そこそこ、縦長少なめ (合計 100)
        private static final int[] WEIGHTS  = {15, 20, 8, 15, 6, 18, 5, 10, 3};

        /** 高さ計算の基準幅 (3列・gap 4px・viewport 約 760px 想定) */
        private static final int REF_WIDTH = 250;

        final int count;
        /** position ごとの ASPECT_LABELS インデックス (重み付きランダムで事前生成) */
        final int[] aspectIndices;

        StaggeredAdapter(int count) {
            this.count = count;
            // 重みの累積和を構築
            int[] cumulative = new int[WEIGHTS.length];
            cumulative[0] = WEIGHTS[0];
            for (int i = 1; i < WEIGHTS.length; i++) {
                cumulative[i] = cumulative[i - 1] + WEIGHTS[i];
            }
            int totalWeight = cumulative[cumulative.length - 1];
            // シード固定で再現可能なランダム列を生成
            Random rng = new Random(42);
            aspectIndices = new int[count];
            for (int i = 0; i < count; i++) {
                int r = rng.nextInt(totalWeight);
                int idx = 0;
                while (idx < cumulative.length - 1 && r >= cumulative[idx]) {
                    idx++;
                }
                aspectIndices[i] = idx;
            }
        }

        @Override
        public GridViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            JLabel label = new JLabel("", SwingConstants.CENTER);
            label.setOpaque(true);
            label.setForeground(Color.WHITE);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
            panel.add(label, BorderLayout.CENTER);
            return new GridViewHolder(panel, label);
        }

        @Override
        public void onBindViewHolder(GridViewHolder holder, int position) {
            int idx = aspectIndices[position];
            holder.label.setText(position + " (" + ASPECT_LABELS[idx] + ")");
            holder.label.setBackground(COLORS[position % COLORS.length]);
            int h = REF_WIDTH * H_RATIOS[idx] / W_RATIOS[idx];
            holder.itemView.setPreferredSize(new Dimension(0, h));
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }

    // === デモパネル ===

    /** シンプルテキストリスト (LinearLayoutManager, 1000件) */
    public static JComponent recyclerLinearSimple() {
        RecyclerPane pane = new RecyclerPane();
        pane.setLayoutManager(new LinearLayoutManager(Orientation.VERTICAL, 2));
        pane.setAdapter(new TextAdapter(1000));

        JScrollPane sp = new JScrollPane(pane);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return sp;
    }

    /** 複合コンポーネントリスト (LinearLayoutManager) */
    public static JComponent recyclerLinearComplex() {
        RecyclerPane pane = new RecyclerPane();
        pane.setLayoutManager(new LinearLayoutManager(Orientation.VERTICAL));
        pane.setAdapter(new ComplexAdapter(500));

        JScrollPane sp = new JScrollPane(pane);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return sp;
    }

    /** グリッドデモ (GridLayoutManager, 3列) */
    public static JComponent recyclerGrid() {
        RecyclerPane pane = new RecyclerPane();
        pane.setLayoutManager(new GridLayoutManager(3, Orientation.VERTICAL)
                .setMainAxisGap(4)
                .setCrossAxisGap(4));
        pane.setAdapter(new GridAdapter(200));

        JScrollPane sp = new JScrollPane(pane);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return sp;
    }

    /** StaggeredGrid デモ (Pinterest 風, 3列) */
    public static JComponent recyclerStaggeredGrid() {
        RecyclerPane pane = new RecyclerPane();
        pane.setLayoutManager(new StaggeredGridLayoutManager(3, Orientation.VERTICAL)
                .setMainAxisGap(4)
                .setCrossAxisGap(4));
        pane.setAdapter(new StaggeredAdapter(1000));

        JScrollPane sp = new JScrollPane(pane);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return sp;
    }
}

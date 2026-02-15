package org.to0mi1.swuit.demo.common;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.to0mi1.swuit.layout.Gravity;
import org.to0mi1.swuit.layout.Orientation;
import org.to0mi1.swuit.layout.linear.LinearLayout;

/**
 * デモ用パネル生成ユーティリティ。
 * <p>
 * 各カテゴリのデモは専用クラスに委譲する。
 */
public final class DemoPanels {

    private DemoPanels() {
    }

    // === ユーティリティ ===

    public static JPanel colorPanel(Color color, String text) {
        JPanel panel = new JPanel(new LinearLayout(Orientation.HORIZONTAL, 0, Gravity.CENTER));
        panel.setBackground(color);
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        panel.add(label);
        return panel;
    }

    // === デモペイン構築 ===

    /** ツリーナビゲーション+コンテンツペインの分割ペインを生成する。 */
    public static JSplitPane createDemoPane() {
        // --- カード（右ペイン） ---
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        cardPanel.add(LinearLayoutDemos.linearVertical(), "linearVertical");
        cardPanel.add(LinearLayoutDemos.linearHorizontal(), "linearHorizontal");
        cardPanel.add(LinearLayoutDemos.linearWeight(), "linearWeight");
        cardPanel.add(LinearLayoutDemos.linearGravity(), "linearGravity");
        cardPanel.add(LinearLayoutDemos.linearMargin(), "linearMargin");
        cardPanel.add(LinearLayoutDemos.linearNested(), "linearNested");
        cardPanel.add(RelativeLayoutDemos.relativeBasic(), "relativeBasic");
        cardPanel.add(RelativeLayoutDemos.relativeForm(), "relativeForm");
        cardPanel.add(RelativeLayoutDemos.relativeComplex(), "relativeComplex");
        cardPanel.add(AutoCompleteDemos.autoCompleteBasic(), "autoCompleteBasic");
        cardPanel.add(AutoCompleteDemos.autoCompleteObject(), "autoCompleteObject");
        cardPanel.add(CssFlexLayoutDemos.flexBasic(), "flexBasic");
        cardPanel.add(CssFlexLayoutDemos.flexWrap(), "flexWrap");
        cardPanel.add(CssFlexLayoutDemos.flexAlign(), "flexAlign");
        cardPanel.add(CssGridLayoutDemos.gridBasic(), "gridBasic");
        cardPanel.add(CssGridLayoutDemos.gridSpan(), "gridSpan");
        cardPanel.add(CssGridLayoutDemos.gridAlign(), "gridAlign");
        cardPanel.add(VirtualScrollDemos.virtualScrollGridLayout(), "virtualScrollGrid");
        cardPanel.add(VirtualScrollDemos.virtualScrollLinearLayout(), "virtualScrollLinear");
        cardPanel.add(VirtualScrollDemos.virtualScrollFlexLayout(), "virtualScrollFlex");
        cardPanel.add(RecyclerDemos.recyclerLinearSimple(), "recyclerLinearSimple");
        cardPanel.add(RecyclerDemos.recyclerLinearComplex(), "recyclerLinearComplex");
        cardPanel.add(RecyclerDemos.recyclerGrid(), "recyclerGrid");
        cardPanel.add(RecyclerDemos.recyclerStaggeredGrid(), "recyclerStaggeredGrid");

        // --- ツリー（左ペイン） ---
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Demos");

        DefaultMutableTreeNode linear = new DefaultMutableTreeNode("LinearLayout");
        linear.add(new DefaultMutableTreeNode("linearVertical"));
        linear.add(new DefaultMutableTreeNode("linearHorizontal"));
        linear.add(new DefaultMutableTreeNode("linearWeight"));
        linear.add(new DefaultMutableTreeNode("linearGravity"));
        linear.add(new DefaultMutableTreeNode("linearMargin"));
        linear.add(new DefaultMutableTreeNode("linearNested"));
        root.add(linear);

        DefaultMutableTreeNode relative = new DefaultMutableTreeNode("RelativeLayout");
        relative.add(new DefaultMutableTreeNode("relativeBasic"));
        relative.add(new DefaultMutableTreeNode("relativeForm"));
        relative.add(new DefaultMutableTreeNode("relativeComplex"));
        root.add(relative);

        DefaultMutableTreeNode autoComplete = new DefaultMutableTreeNode("AutoComplete");
        autoComplete.add(new DefaultMutableTreeNode("autoCompleteBasic"));
        autoComplete.add(new DefaultMutableTreeNode("autoCompleteObject"));
        root.add(autoComplete);

        DefaultMutableTreeNode flex = new DefaultMutableTreeNode("CssFlexLayout");
        flex.add(new DefaultMutableTreeNode("flexBasic"));
        flex.add(new DefaultMutableTreeNode("flexWrap"));
        flex.add(new DefaultMutableTreeNode("flexAlign"));
        root.add(flex);

        DefaultMutableTreeNode grid = new DefaultMutableTreeNode("CssGridLayout");
        grid.add(new DefaultMutableTreeNode("gridBasic"));
        grid.add(new DefaultMutableTreeNode("gridSpan"));
        grid.add(new DefaultMutableTreeNode("gridAlign"));
        root.add(grid);

        DefaultMutableTreeNode virtualScroll = new DefaultMutableTreeNode("VirtualScroll");
        virtualScroll.add(new DefaultMutableTreeNode("virtualScrollGrid"));
        virtualScroll.add(new DefaultMutableTreeNode("virtualScrollLinear"));
        virtualScroll.add(new DefaultMutableTreeNode("virtualScrollFlex"));
        root.add(virtualScroll);

        DefaultMutableTreeNode recycler = new DefaultMutableTreeNode("RecyclerPane");
        recycler.add(new DefaultMutableTreeNode("recyclerLinearSimple"));
        recycler.add(new DefaultMutableTreeNode("recyclerLinearComplex"));
        recycler.add(new DefaultMutableTreeNode("recyclerGrid"));
        recycler.add(new DefaultMutableTreeNode("recyclerStaggeredGrid"));
        root.add(recycler);

        JTree tree = new JTree(root);
        tree.setRootVisible(false);
        // すべて展開
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.isLeaf()) {
                cardLayout.show(cardPanel, (String) node.getUserObject());
            }
        });

        // 初期選択
        tree.setSelectionPath(new TreePath(
                ((DefaultMutableTreeNode) linear.getChildAt(0)).getPath()));

        // --- 分割ペイン ---
        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setMinimumSize(new Dimension(150, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, cardPanel);
        splitPane.setDividerLocation(200);

        return splitPane;
    }
}

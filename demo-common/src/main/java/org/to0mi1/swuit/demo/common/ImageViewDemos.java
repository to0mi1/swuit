package org.to0mi1.swuit.demo.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.to0mi1.swuit.component.image.ImageView;
import org.to0mi1.swuit.component.image.ObjectFit;
import org.to0mi1.swuit.component.image.ObjectPosition;
import org.to0mi1.swuit.layout.aspectratio.AspectRatioLayout;
import org.to0mi1.swuit.layout.flex.CssAlignContent;
import org.to0mi1.swuit.layout.flex.CssAlignItems;
import org.to0mi1.swuit.layout.flex.CssFlexConstraints;
import org.to0mi1.swuit.layout.flex.CssFlexDirection;
import org.to0mi1.swuit.layout.flex.CssFlexLayout;
import org.to0mi1.swuit.layout.flex.CssFlexWrap;

/**
 * ImageView / AspectRatioLayout デモパネル生成ユーティリティ。
 */
public final class ImageViewDemos {

    private ImageViewDemos() {
    }

    /** ObjectFit 5モードの比較 */
    public static JComponent imageObjectFit() {
        JPanel panel = new JPanel(new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP)
                .setCssAlignItems(CssAlignItems.FLEX_START)
                .setCssAlignContent(CssAlignContent.FLEX_START)
                .setMainAxisGap(12)
                .setCrossAxisGap(12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        BufferedImage img = createSampleImage(320, 180);

        for (ObjectFit fit : ObjectFit.values()) {
            JPanel card = createCard(fit.name(), new Color(0x37474F));
            ImageView view = new ImageView(img);
            view.setObjectFit(fit);
            view.setPreferredSize(new Dimension(200, 150));
            view.setBackground(new Color(0x263238));
            view.setOpaque(true);
            card.add(view);
            panel.add(card, new CssFlexConstraints().flexBasisPercent(0.3f).flexGrow(1));
        }
        return wrapInScrollPane(panel);
    }

    /** ObjectPosition の比較 */
    public static JComponent imageObjectPosition() {
        JPanel panel = new JPanel(new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP)
                .setCssAlignItems(CssAlignItems.FLEX_START)
                .setCssAlignContent(CssAlignContent.FLEX_START)
                .setMainAxisGap(12)
                .setCrossAxisGap(12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        BufferedImage img = createSampleImage(320, 180);

        record PosEntry(String name, ObjectPosition pos) {}
        PosEntry[] entries = {
                new PosEntry("TOP_LEFT", ObjectPosition.TOP_LEFT),
                new PosEntry("CENTER", ObjectPosition.CENTER),
                new PosEntry("BOTTOM_RIGHT", ObjectPosition.BOTTOM_RIGHT),
                new PosEntry("25% 75%", new ObjectPosition(0.25f, 0.75f)),
        };

        for (PosEntry entry : entries) {
            JPanel card = createCard(entry.name(), new Color(0x1B5E20));
            ImageView view = new ImageView(img);
            view.setObjectFit(ObjectFit.COVER);
            view.setObjectPosition(entry.pos());
            view.setPreferredSize(new Dimension(160, 160));
            view.setBackground(new Color(0x263238));
            view.setOpaque(true);
            card.add(view);
            panel.add(card, new CssFlexConstraints().flexBasisPercent(0.22f).flexGrow(1));
        }
        return wrapInScrollPane(panel);
    }

    /** AspectRatioLayout で比率維持 */
    public static JComponent imageAspectRatio() {
        JPanel panel = new JPanel(new CssFlexLayout(CssFlexDirection.ROW)
                .setCssFlexWrap(CssFlexWrap.WRAP)
                .setCssAlignItems(CssAlignItems.FLEX_START)
                .setCssAlignContent(CssAlignContent.FLEX_START)
                .setMainAxisGap(12)
                .setCrossAxisGap(12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        BufferedImage img = createSampleImage(320, 180);

        record RatioEntry(String name, double ratio) {}
        RatioEntry[] entries = {
                new RatioEntry("16:9", 16.0 / 9.0),
                new RatioEntry("4:3", 4.0 / 3.0),
                new RatioEntry("1:1", 1.0),
                new RatioEntry("9:16", 9.0 / 16.0),
        };

        for (RatioEntry entry : entries) {
            JPanel card = createCard(entry.name(), new Color(0x0D47A1));

            JPanel ratioContainer = new JPanel(new AspectRatioLayout(entry.ratio()));
            ratioContainer.setOpaque(false);
            ImageView view = new ImageView(img);
            view.setObjectFit(ObjectFit.COVER);
            ratioContainer.add(view);

            card.add(ratioContainer);
            panel.add(card, new CssFlexConstraints().flexBasisPercent(0.22f).flexGrow(1));
        }
        return wrapInScrollPane(panel);
    }

    // === ヘルパー ===

    private static JPanel createCard(String title, Color borderColor) {
        JPanel card = new JPanel(new CssFlexLayout(CssFlexDirection.COLUMN).setMainAxisGap(4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        JPanel header = DemoPanels.colorPanel(borderColor, title);
        header.setPreferredSize(new Dimension(0, 28));
        card.add(header);
        return card;
    }

    private static JScrollPane wrapInScrollPane(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** デモ用のグラデーション + 格子画像を生成 */
    private static BufferedImage createSampleImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // グラデーション背景
        for (int y = 0; y < height; y++) {
            float t = (float) y / height;
            int r = (int) (30 + 200 * t);
            int gr = (int) (100 + 80 * (1 - t));
            int b = (int) (200 - 100 * t);
            g.setColor(new Color(r, gr, b));
            g.drawLine(0, y, width, y);
        }

        // 格子線（画像の伸縮がわかりやすいように）
        g.setColor(new Color(255, 255, 255, 60));
        int step = 40;
        for (int x = 0; x < width; x += step) {
            g.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += step) {
            g.drawLine(0, y, width, y);
        }

        // 中央に円（位置がわかりやすいように）
        g.setColor(new Color(255, 255, 255, 100));
        int d = Math.min(width, height) / 3;
        g.fillOval(width / 2 - d / 2, height / 2 - d / 2, d, d);

        g.dispose();
        return img;
    }
}

package org.to0mi1.swuit.component.virtualscroll;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * 大量の子コンポーネントを持つパネルの描画パフォーマンスを最適化するスクロールペイン。
 * <p>
 * 可視領域外のコンポーネントを {@code setVisible(false)} にして描画をスキップする。
 * 内部で {@link java.awt.LayoutManager} をラップし、レイアウト計算時には全コンポーネントを
 * 一時的に可視状態に戻すため、ユーザーのパネルや LayoutManager への干渉は最小限。
 * <p>
 * 使用例:
 * <pre>{@code
 * JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 4));
 * for (int i = 0; i < 1000; i++) {
 *     panel.add(new JLabel("Item " + i));
 * }
 * VirtualScrollPane scrollPane = new VirtualScrollPane(panel);
 * }</pre>
 */
public class VirtualScrollPane extends JScrollPane {

    /**
     * 空の {@code VirtualScrollPane} を作成する。
     */
    public VirtualScrollPane() {
        super();
    }

    /**
     * 指定したビューを持つ {@code VirtualScrollPane} を作成する。
     *
     * @param view ビューコンポーネント
     */
    public VirtualScrollPane(Component view) {
        super(view);
    }

    /**
     * 指定したスクロールバーポリシーで空の {@code VirtualScrollPane} を作成する。
     *
     * @param vsbPolicy 垂直スクロールバーポリシー
     * @param hsbPolicy 水平スクロールバーポリシー
     */
    public VirtualScrollPane(int vsbPolicy, int hsbPolicy) {
        super(vsbPolicy, hsbPolicy);
    }

    /**
     * 指定したビューとスクロールバーポリシーで {@code VirtualScrollPane} を作成する。
     *
     * @param view      ビューコンポーネント
     * @param vsbPolicy 垂直スクロールバーポリシー
     * @param hsbPolicy 水平スクロールバーポリシー
     */
    public VirtualScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
    }

    @Override
    protected JViewport createViewport() {
        return new VirtualViewport();
    }

    /**
     * 可視領域外のバッファゾーンサイズ（ピクセル）を取得する。
     *
     * @return バッファゾーンサイズ
     */
    public int getBufferZone() {
        return getVirtualViewport().getBufferZone();
    }

    /**
     * 可視領域外のバッファゾーンサイズ（ピクセル）を設定する。
     * <p>
     * バッファゾーンを大きくすると、高速スクロール時のちらつきが減少するが、
     * 描画されるコンポーネント数が増える。デフォルトは 50。
     *
     * @param bufferZone バッファゾーンサイズ（0 以上）
     * @throws IllegalArgumentException bufferZone が負の場合
     */
    public void setBufferZone(int bufferZone) {
        if (bufferZone < 0) {
            throw new IllegalArgumentException("bufferZone must be >= 0: " + bufferZone);
        }
        getVirtualViewport().setBufferZone(bufferZone);
    }

    private VirtualViewport getVirtualViewport() {
        return (VirtualViewport) getViewport();
    }
}

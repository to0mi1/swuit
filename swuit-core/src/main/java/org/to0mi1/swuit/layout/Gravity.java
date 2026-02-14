package org.to0mi1.swuit.layout;

/**
 * コンポーネントの配置方向を指定するビットフラグ定数。
 * Android の {@code Gravity} に相当する。
 */
public final class Gravity {

    private Gravity() {
    }

    /** 指定なし。親のデフォルトに従う。 */
    public static final int NONE = 0;

    // --- 水平方向 ---
    public static final int LEFT = 0x01;
    public static final int CENTER_HORIZONTAL = 0x02;
    public static final int RIGHT = 0x04;
    public static final int FILL_HORIZONTAL = 0x08;

    // --- 垂直方向 ---
    public static final int TOP = 0x10;
    public static final int CENTER_VERTICAL = 0x20;
    public static final int BOTTOM = 0x40;
    public static final int FILL_VERTICAL = 0x80;

    // --- 複合定数 ---
    public static final int CENTER = CENTER_HORIZONTAL | CENTER_VERTICAL;
    public static final int FILL = FILL_HORIZONTAL | FILL_VERTICAL;

    // --- マスク ---
    public static final int HORIZONTAL_MASK = 0x0F;
    public static final int VERTICAL_MASK = 0xF0;

    /**
     * gravity 値から水平成分を取得する。
     *
     * @param gravity gravity ビットフラグ
     * @param defaultGravity 水平成分が NONE の場合に返すデフォルト値
     * @return 水平成分（NONE の場合は defaultGravity）
     */
    public static int getHorizontal(int gravity, int defaultGravity) {
        int h = gravity & HORIZONTAL_MASK;
        return h != NONE ? h : defaultGravity;
    }

    /**
     * gravity 値から垂直成分を取得する。
     *
     * @param gravity gravity ビットフラグ
     * @param defaultGravity 垂直成分が NONE の場合に返すデフォルト値
     * @return 垂直成分（NONE の場合は defaultGravity）
     */
    public static int getVertical(int gravity, int defaultGravity) {
        int v = gravity & VERTICAL_MASK;
        return v != NONE ? v : defaultGravity;
    }
}

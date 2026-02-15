package org.to0mi1.swuit.component.image;

/**
 * CSS の {@code object-position} プロパティに相当する描画オフセット。
 * 描画サイズがコンテナと異なる場合、余白または切り取り位置を決定する。
 *
 * <p>{@code x}, {@code y} は 0.0〜1.0 の正規化値。
 * (0.0, 0.0) = 左上、(0.5, 0.5) = 中央、(1.0, 1.0) = 右下。</p>
 */
public final class ObjectPosition {

    /** 中央配置（デフォルト） */
    public static final ObjectPosition CENTER = new ObjectPosition(0.5f, 0.5f);

    /** 左上 */
    public static final ObjectPosition TOP_LEFT = new ObjectPosition(0.0f, 0.0f);

    /** 右下 */
    public static final ObjectPosition BOTTOM_RIGHT = new ObjectPosition(1.0f, 1.0f);

    private final float x;
    private final float y;

    /**
     * @param x 水平位置 (0.0=左, 0.5=中央, 1.0=右)
     * @param y 垂直位置 (0.0=上, 0.5=中央, 1.0=下)
     */
    public ObjectPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    /**
     * コンテナサイズと描画サイズからオフセットを計算する。
     *
     * @param containerSize コンテナの幅または高さ
     * @param drawSize      描画サイズの幅または高さ
     * @param horizontal    true の場合 x、false の場合 y を使う
     * @return オフセット（ピクセル）
     */
    public int computeOffset(int containerSize, int drawSize, boolean horizontal) {
        float pos = horizontal ? x : y;
        return Math.round((containerSize - drawSize) * pos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectPosition that)) return false;
        return Float.compare(x, that.x) == 0 && Float.compare(y, that.y) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Float.hashCode(x) + Float.hashCode(y);
    }

    @Override
    public String toString() {
        return "ObjectPosition(" + x + ", " + y + ")";
    }
}

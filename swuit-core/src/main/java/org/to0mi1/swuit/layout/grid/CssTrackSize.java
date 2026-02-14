package org.to0mi1.swuit.layout.grid;

/**
 * グリッドトラック（列/行）のサイズ定義。
 *
 * <pre>{@code
 * CssTrackSize.fixed(100)  // 固定 100px
 * CssTrackSize.fr(1)       // 余剰スペースを 1fr で分配
 * CssTrackSize.auto()      // コンテンツの preferredSize に合わせる
 * }</pre>
 */
public class CssTrackSize {

    private static final CssTrackSize AUTO = new CssTrackSize(CssTrackSizeType.AUTO, 0);

    private final CssTrackSizeType type;
    private final float value;

    private CssTrackSize(CssTrackSizeType type, float value) {
        this.type = type;
        this.value = value;
    }

    /** 固定ピクセルサイズのトラックを生成する。 */
    public static CssTrackSize fixed(int pixels) {
        if (pixels < 0) throw new IllegalArgumentException("pixels must be >= 0: " + pixels);
        return new CssTrackSize(CssTrackSizeType.FIXED, pixels);
    }

    /** 余剰スペース分配比率 (CSS の fr 単位) のトラックを生成する。 */
    public static CssTrackSize fr(float fraction) {
        if (fraction <= 0) throw new IllegalArgumentException("fraction must be > 0: " + fraction);
        return new CssTrackSize(CssTrackSizeType.FR, fraction);
    }

    /** コンテンツに合わせるトラックを生成する。 */
    public static CssTrackSize auto() {
        return AUTO;
    }

    CssTrackSizeType getType() {
        return type;
    }

    float getValue() {
        return value;
    }
}

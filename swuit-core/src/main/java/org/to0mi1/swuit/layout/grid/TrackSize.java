package org.to0mi1.swuit.layout.grid;

/**
 * グリッドトラック（列/行）のサイズ定義。
 *
 * <pre>{@code
 * TrackSize.fixed(100)  // 固定 100px
 * TrackSize.fr(1)       // 余剰スペースを 1fr で分配
 * TrackSize.auto()      // コンテンツの preferredSize に合わせる
 * }</pre>
 */
public class TrackSize {

    private static final TrackSize AUTO = new TrackSize(TrackSizeType.AUTO, 0);

    private final TrackSizeType type;
    private final float value;

    private TrackSize(TrackSizeType type, float value) {
        this.type = type;
        this.value = value;
    }

    /** 固定ピクセルサイズのトラックを生成する。 */
    public static TrackSize fixed(int pixels) {
        if (pixels < 0) throw new IllegalArgumentException("pixels must be >= 0: " + pixels);
        return new TrackSize(TrackSizeType.FIXED, pixels);
    }

    /** 余剰スペース分配比率 (CSS の fr 単位) のトラックを生成する。 */
    public static TrackSize fr(float fraction) {
        if (fraction <= 0) throw new IllegalArgumentException("fraction must be > 0: " + fraction);
        return new TrackSize(TrackSizeType.FR, fraction);
    }

    /** コンテンツに合わせるトラックを生成する。 */
    public static TrackSize auto() {
        return AUTO;
    }

    TrackSizeType getType() {
        return type;
    }

    float getValue() {
        return value;
    }
}

package app.dinus.com.loadingdrawable.render.circle.rotate

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.support.annotation.Size
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class LevelLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

    private val mPaint = Paint()
    private val mTempBounds = RectF()

    private val mAnimatorListener = object : AnimatorListenerAdapter() {
        override fun onAnimationRepeat(animator: Animator) {
            super.onAnimationRepeat(animator)
            storeOriginals()

            mStartDegrees = mEndDegrees
            mRotationCount = (mRotationCount + 1) % NUM_POINTS
        }

        override fun onAnimationStart(animation: Animator) {
            super.onAnimationStart(animation)
            mRotationCount = 0f
        }
    }

    @Size(3)
    private var mLevelColors: IntArray? = null
    @Size(3)
    var mLevelSwipeDegrees: FloatArray? = null

    private var mStrokeInset: Float = 0.toFloat()

    private var mRotationCount: Float = 0.toFloat()
    private var mGroupRotation: Float = 0.toFloat()

    private var mEndDegrees: Float = 0.toFloat()
    private var mStartDegrees: Float = 0.toFloat()
    private var mOriginEndDegrees: Float = 0.toFloat()
    private var mOriginStartDegrees: Float = 0.toFloat()

    private var mStrokeWidth: Float = 0.toFloat()
    private var mCenterRadius: Float = 0.toFloat()

    init {
        init(context)
        setupPaint()
        addRenderListener(mAnimatorListener)
    }

    private fun init(context: Context) {
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS)

        mLevelSwipeDegrees = FloatArray(3)
        mLevelColors = DEFAULT_LEVEL_COLORS
    }

    private fun setupPaint() {

        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND

        initStrokeInset(mWidth.toInt().toFloat(), mHeight.toInt().toFloat())
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()

        mTempBounds.set(mBounds)
        mTempBounds.inset(mStrokeInset, mStrokeInset)
        canvas.rotate(mGroupRotation, mTempBounds.centerX(), mTempBounds.centerY())

        for (i in 0..2) {
            if (mLevelSwipeDegrees!![i] != 0f) {
                mPaint.color = mLevelColors!![i]
                canvas.drawArc(mTempBounds, mEndDegrees, mLevelSwipeDegrees!![i], false, mPaint)
            }
        }

        canvas.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        // Moving the start trim only occurs in the first 50% of a single ring animation
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            val startTrimProgress = renderProgress / START_TRIM_DURATION_OFFSET
            mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress)

            val mSwipeDegrees = mEndDegrees - mStartDegrees
            val levelSwipeDegreesProgress = Math.abs(mSwipeDegrees) / MAX_SWIPE_DEGREES

            val level1Increment = DECELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress) - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress)
            val level3Increment = ACCELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress) - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress)

            mLevelSwipeDegrees!![0] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[0] * (1.0f + level1Increment)
            mLevelSwipeDegrees!![1] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[1] * 1.0f
            mLevelSwipeDegrees!![2] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[2] * (1.0f + level3Increment)
        }

        // Moving the end trim starts after 50% of a single ring animation
        if (renderProgress > START_TRIM_DURATION_OFFSET) {
            val endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET)
            mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress)

            val mSwipeDegrees = mEndDegrees - mStartDegrees
            val levelSwipeDegreesProgress = Math.abs(mSwipeDegrees) / MAX_SWIPE_DEGREES

            if (levelSwipeDegreesProgress > LEVEL_SWEEP_ANGLE_OFFSETS[1]) {
                mLevelSwipeDegrees!![0] = -mSwipeDegrees
                mLevelSwipeDegrees!![1] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[1]
                mLevelSwipeDegrees!![2] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[2]
            } else if (levelSwipeDegreesProgress > LEVEL_SWEEP_ANGLE_OFFSETS[2]) {
                mLevelSwipeDegrees!![0] = 0f
                mLevelSwipeDegrees!![1] = -mSwipeDegrees
                mLevelSwipeDegrees!![2] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[2]
            } else {
                mLevelSwipeDegrees!![0] = 0f
                mLevelSwipeDegrees!![1] = 0f
                mLevelSwipeDegrees!![2] = -mSwipeDegrees
            }
        }

        mGroupRotation = FULL_GROUP_ROTATION / NUM_POINTS * renderProgress + FULL_GROUP_ROTATION * (mRotationCount / NUM_POINTS)
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun reset() {
        resetOriginals()
    }

    private fun initStrokeInset(width: Float, height: Float) {
        val minSize = Math.min(width, height)
        val strokeInset = minSize / 2.0f - mCenterRadius
        val minStrokeInset = Math.ceil((mStrokeWidth / 2.0f).toDouble()).toFloat()
        mStrokeInset = if (strokeInset < minStrokeInset) minStrokeInset else strokeInset
    }

    private fun storeOriginals() {
        mOriginEndDegrees = mEndDegrees
        mOriginStartDegrees = mEndDegrees
    }

    private fun resetOriginals() {
        mOriginEndDegrees = 0f
        mOriginStartDegrees = 0f

        mEndDegrees = 0f
        mStartDegrees = 0f

        mLevelSwipeDegrees!![0] = 0f
        mLevelSwipeDegrees!![1] = 0f
        mLevelSwipeDegrees!![2] = 0f
    }

    private fun apply(builder: Builder) {
        this.mWidth = if (builder.mWidth > 0) builder.mWidth.toFloat() else this.mWidth
        this.mHeight = if (builder.mHeight > 0) builder.mHeight.toFloat() else this.mHeight
        this.mStrokeWidth = if (builder.mStrokeWidth > 0) builder.mStrokeWidth.toFloat() else this.mStrokeWidth
        this.mCenterRadius = if (builder.mCenterRadius > 0) builder.mCenterRadius.toFloat() else this.mCenterRadius

        this.mDuration = if (builder.mDuration > 0) builder.mDuration.toLong() else this.mDuration

        this.mLevelColors = if (builder.mLevelColors != null) builder.mLevelColors else this.mLevelColors

        setupPaint()
        initStrokeInset(this.mWidth, this.mHeight)
    }

    class Builder(private val mContext: Context) {

        var mWidth: Int = 0
        var mHeight: Int = 0
        var mStrokeWidth: Int = 0
        var mCenterRadius: Int = 0

        var mDuration: Int = 0

        @Size(3)
        var mLevelColors: IntArray? = null

        fun setWidth(width: Int): Builder {
            this.mWidth = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.mHeight = height
            return this
        }

        fun setStrokeWidth(strokeWidth: Int): Builder {
            this.mStrokeWidth = strokeWidth
            return this
        }

        fun setCenterRadius(centerRadius: Int): Builder {
            this.mCenterRadius = centerRadius
            return this
        }

        fun setDuration(duration: Int): Builder {
            this.mDuration = duration
            return this
        }

        fun setLevelColors(@Size(3) colors: IntArray): Builder {
            this.mLevelColors = colors
            return this
        }

        fun setLevelColor(color: Int): Builder {
            return setLevelColors(intArrayOf(oneThirdAlphaColor(color), twoThirdAlphaColor(color), color))
        }

        fun build(): LevelLoadingRenderer {
            val loadingRenderer = LevelLoadingRenderer(mContext)
            loadingRenderer.apply(this)
            return loadingRenderer
        }

        private fun oneThirdAlphaColor(colorValue: Int): Int {
            val startA = colorValue shr 24 and 0xff
            val startR = colorValue shr 16 and 0xff
            val startG = colorValue shr 8 and 0xff
            val startB = colorValue and 0xff

            return startA / 3 shl 24 or (startR shl 16) or (startG shl 8) or startB
        }

        private fun twoThirdAlphaColor(colorValue: Int): Int {
            val startA = colorValue shr 24 and 0xff
            val startR = colorValue shr 16 and 0xff
            val startG = colorValue shr 8 and 0xff
            val startB = colorValue and 0xff

            return startA * 2 / 3 shl 24 or (startR shl 16) or (startG shl 8) or startB
        }
    }

    companion object {
        private val LINEAR_INTERPOLATOR = LinearInterpolator()
        private val MATERIAL_INTERPOLATOR = FastOutSlowInInterpolator()
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator()

        private val NUM_POINTS = 5
        private val DEGREE_360 = 360

        private val MAX_SWIPE_DEGREES = 0.8f * DEGREE_360
        private val FULL_GROUP_ROTATION = 3.0f * DEGREE_360

        private val LEVEL_SWEEP_ANGLE_OFFSETS = floatArrayOf(1.0f, 7.0f / 8.0f, 5.0f / 8.0f)

        private val START_TRIM_DURATION_OFFSET = 0.5f
        private val END_TRIM_DURATION_OFFSET = 1.0f

        private val DEFAULT_CENTER_RADIUS = 12.5f
        private val DEFAULT_STROKE_WIDTH = 2.5f

        private val DEFAULT_LEVEL_COLORS = intArrayOf(Color.parseColor("#55ffffff"), Color.parseColor("#b1ffffff"), Color.parseColor("#ffffffff"))
    }

}

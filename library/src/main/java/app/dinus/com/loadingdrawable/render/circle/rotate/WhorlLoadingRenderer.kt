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
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class WhorlLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

    private val mPaint = Paint()
    private val mTempBounds = RectF()
    private val mTempArcBounds = RectF()

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

    private var mColors: IntArray? = null

    private var mStrokeInset: Float = 0.toFloat()

    private var mRotationCount: Float = 0.toFloat()
    private var mGroupRotation: Float = 0.toFloat()

    private var mEndDegrees: Float = 0.toFloat()
    private var mStartDegrees: Float = 0.toFloat()
    private var mSwipeDegrees: Float = 0.toFloat()
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
        mColors = DEFAULT_COLORS
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS)

        initStrokeInset(mWidth, mHeight)
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()

        mTempBounds.set(mBounds)
        mTempBounds.inset(mStrokeInset, mStrokeInset)

        canvas.rotate(mGroupRotation, mTempBounds.centerX(), mTempBounds.centerY())

        if (mSwipeDegrees != 0f) {
            for (i in mColors!!.indices) {
                mPaint.strokeWidth = mStrokeWidth / (i + 1)
                mPaint.color = mColors!![i]
                canvas.drawArc(createArcBounds(mTempBounds, i), mStartDegrees + DEGREE_180 * (i % 2),
                        mSwipeDegrees, false, mPaint)
            }
        }

        canvas.restoreToCount(saveCount)
    }

    private fun createArcBounds(sourceArcBounds: RectF, index: Int): RectF {
        var intervalWidth = 0

        for (i in 0 until index) {
            intervalWidth += (mStrokeWidth / (i + 1.0f) * 1.5f).toInt()
        }

        val arcBoundsLeft = (sourceArcBounds.left + intervalWidth).toInt()
        val arcBoundsTop = (sourceArcBounds.top + intervalWidth).toInt()
        val arcBoundsRight = (sourceArcBounds.right - intervalWidth).toInt()
        val arcBoundsBottom = (sourceArcBounds.bottom - intervalWidth).toInt()
        mTempArcBounds.set(arcBoundsLeft.toFloat(), arcBoundsTop.toFloat(), arcBoundsRight.toFloat(), arcBoundsBottom.toFloat())

        return mTempArcBounds
    }

    override fun computeRender(renderProgress: Float) {
        // Moving the start trim only occurs in the first 50% of a single ring animation
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            val startTrimProgress = renderProgress / (1.0f - START_TRIM_DURATION_OFFSET)
            mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress)
        }

        // Moving the end trim starts after 50% of a single ring animation
        if (renderProgress > START_TRIM_DURATION_OFFSET) {
            val endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET)
            mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress)
        }

        if (Math.abs(mEndDegrees - mStartDegrees) > 0) {
            mSwipeDegrees = mEndDegrees - mStartDegrees
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

        mSwipeDegrees = 0f
    }

    private fun apply(builder: Builder) {
        this.mWidth = if (builder.mWidth > 0) builder.mWidth.toFloat() else this.mWidth
        this.mHeight = if (builder.mHeight > 0) builder.mHeight.toFloat() else this.mHeight
        this.mStrokeWidth = if (builder.mStrokeWidth > 0) builder.mStrokeWidth.toFloat() else this.mStrokeWidth
        this.mCenterRadius = if (builder.mCenterRadius > 0) builder.mCenterRadius.toFloat() else this.mCenterRadius

        this.mDuration = if (builder.mDuration > 0) builder.mDuration.toLong() else this.mDuration

        this.mColors = if (builder.mColors != null && builder.mColors!!.size > 0) builder.mColors else this.mColors

        setupPaint()
        initStrokeInset(this.mWidth, this.mHeight)
    }

    class Builder(private val mContext: Context) {

        var mWidth: Int = 0
        var mHeight: Int = 0
        var mStrokeWidth: Int = 0
        var mCenterRadius: Int = 0

        var mDuration: Int = 0

        var mColors: IntArray? = null

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

        fun setColors(colors: IntArray): Builder {
            this.mColors = colors
            return this
        }

        fun build(): WhorlLoadingRenderer {
            val loadingRenderer = WhorlLoadingRenderer(mContext)
            loadingRenderer.apply(this)
            return loadingRenderer
        }
    }

    companion object {
        private val MATERIAL_INTERPOLATOR = FastOutSlowInInterpolator()

        private val DEGREE_180 = 180
        private val DEGREE_360 = 360
        private val NUM_POINTS = 5

        private val MAX_SWIPE_DEGREES = 0.6f * DEGREE_360
        private val FULL_GROUP_ROTATION = 3.0f * DEGREE_360

        private val START_TRIM_DURATION_OFFSET = 0.5f
        private val END_TRIM_DURATION_OFFSET = 1.0f

        private val DEFAULT_CENTER_RADIUS = 12.5f
        private val DEFAULT_STROKE_WIDTH = 2.5f

        private val DEFAULT_COLORS = intArrayOf(Color.RED, Color.GREEN, Color.BLUE)
    }
}

package app.dinus.com.loadingdrawable.render.circle.rotate

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.IntRange
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class GearLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

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

    private var mColor: Int = 0

    private var mGearCount: Int = 0
    private var mGearSwipeDegrees: Int = 0

    private var mStrokeInset: Float = 0.toFloat()

    private var mRotationCount: Float = 0.toFloat()
    private var mGroupRotation: Float = 0.toFloat()

    private var mScale: Float = 0.toFloat()
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
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS)

        mColor = DEFAULT_COLOR

        mGearCount = GEAR_COUNT
        mGearSwipeDegrees = DEFAULT_GEAR_SWIPE_DEGREES
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND

        initStrokeInset(mWidth, mHeight)
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()

        mTempBounds.set(mBounds)
        mTempBounds.inset(mStrokeInset, mStrokeInset)
        mTempBounds.inset(mTempBounds.width() * (1.0f - mScale) / 2.0f, mTempBounds.width() * (1.0f - mScale) / 2.0f)

        canvas.rotate(mGroupRotation, mTempBounds.centerX(), mTempBounds.centerY())

        mPaint.color = mColor
        mPaint.alpha = (MAX_ALPHA * mScale).toInt()
        mPaint.strokeWidth = mStrokeWidth * mScale

        if (mSwipeDegrees != 0f) {
            for (i in 0 until mGearCount) {
                canvas.drawArc(mTempBounds, mStartDegrees + DEGREE_360 / mGearCount * i, mSwipeDegrees, false, mPaint)
            }
        }

        canvas.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        // Scaling up the start size only occurs in the first 20% of a single ring animation
        if (renderProgress <= START_SCALE_DURATION_OFFSET) {
            val startScaleProgress = renderProgress / START_SCALE_DURATION_OFFSET
            mScale = DECELERATE_INTERPOLATOR.getInterpolation(startScaleProgress)
        }

        // Moving the start trim only occurs between 20% to 50% of a single ring animation
        if (renderProgress <= START_TRIM_DURATION_OFFSET && renderProgress > START_SCALE_DURATION_OFFSET) {
            val startTrimProgress = (renderProgress - START_SCALE_DURATION_OFFSET) / (START_TRIM_DURATION_OFFSET - START_SCALE_DURATION_OFFSET)
            mStartDegrees = mOriginStartDegrees + mGearSwipeDegrees * startTrimProgress
        }

        // Moving the end trim starts between 50% to 80% of a single ring animation
        if (renderProgress <= END_TRIM_DURATION_OFFSET && renderProgress > START_TRIM_DURATION_OFFSET) {
            val endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET)
            mEndDegrees = mOriginEndDegrees + mGearSwipeDegrees * endTrimProgress
        }

        // Scaling down the end size starts after 80% of a single ring animation
        if (renderProgress > END_TRIM_DURATION_OFFSET) {
            val endScaleProgress = (renderProgress - END_TRIM_DURATION_OFFSET) / (END_SCALE_DURATION_OFFSET - END_TRIM_DURATION_OFFSET)
            mScale = 1.0f - ACCELERATE_INTERPOLATOR.getInterpolation(endScaleProgress)
        }

        if (renderProgress <= END_TRIM_DURATION_OFFSET && renderProgress > START_SCALE_DURATION_OFFSET) {
            val rotateProgress = (renderProgress - START_SCALE_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_SCALE_DURATION_OFFSET)
            mGroupRotation = FULL_GROUP_ROTATION / NUM_POINTS * rotateProgress + FULL_GROUP_ROTATION * (mRotationCount / NUM_POINTS)
        }

        if (Math.abs(mEndDegrees - mStartDegrees) > 0) {
            mSwipeDegrees = mEndDegrees - mStartDegrees
        }
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

        mSwipeDegrees = 1f
    }

    private fun apply(builder: Builder) {
        this.mWidth = if (builder.mWidth > 0) builder.mWidth.toFloat() else this.mWidth
        this.mHeight = if (builder.mHeight > 0) builder.mHeight.toFloat() else this.mHeight
        this.mStrokeWidth = if (builder.mStrokeWidth > 0) builder.mStrokeWidth.toFloat() else this.mStrokeWidth
        this.mCenterRadius = if (builder.mCenterRadius > 0) builder.mCenterRadius.toFloat() else this.mCenterRadius

        this.mDuration = if (builder.mDuration > 0) builder.mDuration.toLong() else this.mDuration

        this.mColor = if (builder.mColor != 0) builder.mColor else this.mColor

        this.mGearCount = if (builder.mGearCount > 0) builder.mGearCount else this.mGearCount
        this.mGearSwipeDegrees = if (builder.mGearSwipeDegrees > 0) builder.mGearSwipeDegrees else this.mGearSwipeDegrees

        setupPaint()
        initStrokeInset(this.mWidth, this.mHeight)
    }

    class Builder(private val mContext: Context) {

        var mWidth: Int = 0
        var mHeight: Int = 0
        var mStrokeWidth: Int = 0
        var mCenterRadius: Int = 0

        var mDuration: Int = 0

        var mColor: Int = 0

        var mGearCount: Int = 0
        var mGearSwipeDegrees: Int = 0

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

        fun setColor(color: Int): Builder {
            this.mColor = color
            return this
        }

        fun setGearCount(gearCount: Int): Builder {
            this.mGearCount = gearCount
            return this
        }

        fun setGearSwipeDegrees(@IntRange(from = 0, to = 360) gearSwipeDegrees: Int): Builder {
            this.mGearSwipeDegrees = gearSwipeDegrees
            return this
        }

        fun build(): GearLoadingRenderer {
            val loadingRenderer = GearLoadingRenderer(mContext)
            loadingRenderer.apply(this)
            return loadingRenderer
        }
    }

    companion object {
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator()

        private val GEAR_COUNT = 4
        private val NUM_POINTS = 3
        private val MAX_ALPHA = 255
        private val DEGREE_360 = 360

        private val DEFAULT_GEAR_SWIPE_DEGREES = 60

        private val FULL_GROUP_ROTATION = 3.0f * DEGREE_360

        private val START_SCALE_DURATION_OFFSET = 0.3f
        private val START_TRIM_DURATION_OFFSET = 0.5f
        private val END_TRIM_DURATION_OFFSET = 0.7f
        private val END_SCALE_DURATION_OFFSET = 1.0f

        private val DEFAULT_CENTER_RADIUS = 12.5f
        private val DEFAULT_STROKE_WIDTH = 2.5f

        private val DEFAULT_COLOR = Color.WHITE
    }
}

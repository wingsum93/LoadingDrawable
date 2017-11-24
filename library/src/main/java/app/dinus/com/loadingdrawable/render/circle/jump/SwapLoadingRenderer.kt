package app.dinus.com.loadingdrawable.render.circle.jump

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class SwapLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mColor: Int = 0

    private var mSwapIndex: Int = 0
    private var mBallCount: Int = 0

    private var mBallSideOffsets: Float = 0.toFloat()
    private var mBallCenterY: Float = 0.toFloat()
    private var mBallRadius: Float = 0.toFloat()
    private var mBallInterval: Float = 0.toFloat()
    private var mSwapBallOffsetX: Float = 0.toFloat()
    private var mSwapBallOffsetY: Float = 0.toFloat()
    private var mASwapThreshold: Float = 0.toFloat()

    private var mStrokeWidth: Float = 0.toFloat()

    init {

        init(context)
        adjustParams()
        setupPaint()
    }

    private fun init(context: Context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH)
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT)
        mBallRadius = DensityUtil.dip2px(context, DEFAULT_BALL_RADIUS)
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)

        mColor = DEFAULT_COLOR
        mDuration = ANIMATION_DURATION
        mBallCount = DEFAULT_CIRCLE_COUNT

        mBallInterval = mBallRadius
    }

    private fun adjustParams() {
        mBallCenterY = mHeight / 2.0f
        mBallSideOffsets = (mWidth - mBallRadius * 2f * mBallCount.toFloat() - mBallInterval * (mBallCount - 1)) / 2.0f

        mASwapThreshold = 1.0f / mBallCount
    }

    private fun setupPaint() {
        mPaint.color = mColor
        mPaint.strokeWidth = mStrokeWidth
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()

        for (i in 0 until mBallCount) {
            if (i == mSwapIndex) {
                mPaint.style = Paint.Style.FILL
                canvas.drawCircle(mBallSideOffsets + mBallRadius * (i * 2 + 1) + i * mBallInterval + mSwapBallOffsetX, mBallCenterY - mSwapBallOffsetY, mBallRadius, mPaint)
            } else if (i == (mSwapIndex + 1) % mBallCount) {
                mPaint.style = Paint.Style.STROKE
                canvas.drawCircle(mBallSideOffsets + mBallRadius * (i * 2 + 1) + i * mBallInterval - mSwapBallOffsetX, mBallCenterY + mSwapBallOffsetY, mBallRadius - mStrokeWidth / 2, mPaint)
            } else {
                mPaint.style = Paint.Style.STROKE
                canvas.drawCircle(mBallSideOffsets + mBallRadius * (i * 2 + 1) + i * mBallInterval, mBallCenterY, mBallRadius - mStrokeWidth / 2, mPaint)
            }
        }

        canvas.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        mSwapIndex = (renderProgress / mASwapThreshold).toInt()

        // Swap trace : x^2 + y^2 = r ^ 2
        val swapTraceProgress = ACCELERATE_DECELERATE_INTERPOLATOR.getInterpolation(
                (renderProgress - mSwapIndex * mASwapThreshold) / mASwapThreshold)

        val swapTraceRadius = if (mSwapIndex == mBallCount - 1)
            (mBallRadius * 2f * (mBallCount - 1).toFloat() + mBallInterval * (mBallCount - 1)) / 2
        else
            (mBallRadius * 2 + mBallInterval) / 2

        // Calculate the X offset of the swap ball
        mSwapBallOffsetX = if (mSwapIndex == mBallCount - 1)
            -swapTraceProgress * swapTraceRadius * 2f
        else
            swapTraceProgress * swapTraceRadius * 2f

        // if mSwapIndex == mBallCount - 1 then (swapTraceRadius, swapTraceRadius) as the origin of coordinates
        // else (-swapTraceRadius, -swapTraceRadius) as the origin of coordinates
        val xCoordinate = if (mSwapIndex == mBallCount - 1)
            mSwapBallOffsetX + swapTraceRadius
        else
            mSwapBallOffsetX - swapTraceRadius

        // Calculate the Y offset of the swap ball
        mSwapBallOffsetY = (if (mSwapIndex % 2 == 0 && mSwapIndex != mBallCount - 1)
            Math.sqrt(Math.pow(swapTraceRadius.toDouble(), 2.0) - Math.pow(xCoordinate.toDouble(), 2.0))
        else
            -Math.sqrt(Math.pow(swapTraceRadius.toDouble(), 2.0) - Math.pow(xCoordinate.toDouble(), 2.0))).toFloat()

    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun reset() {}

    private fun apply(builder: Builder) {
        this.mWidth = if (builder.mWidth > 0) builder.mWidth else this.mWidth
        this.mHeight = if (builder.mHeight > 0) builder.mHeight else this.mHeight
        this.mStrokeWidth = if (builder.mStrokeWidth > 0) builder.mStrokeWidth else this.mStrokeWidth

        this.mBallRadius = if (builder.mBallRadius > 0) builder.mBallRadius else this.mBallRadius
        this.mBallInterval = if (builder.mBallInterval > 0) builder.mBallInterval else this.mBallInterval
        this.mBallCount = if (builder.mBallCount > 0) builder.mBallCount else this.mBallCount

        this.mColor = if (builder.mColor != 0) builder.mColor else this.mColor

        this.mDuration = if (builder.mDuration > 0) builder.mDuration else this.mDuration

        adjustParams()
        setupPaint()
    }

    class Builder(private val mContext: Context) {

        var mWidth: Float = 0f
        var mHeight: Float = 0f
        var mStrokeWidth: Float = 0f

        var mBallCount: Int = 0
        var mBallRadius: Float = 0f
        var mBallInterval: Float = 0f

        var mDuration: Long = 0L

        var mColor: Int = 0

        fun setWidth(width: Float): Builder {
            this.mWidth = width
            return this
        }

        fun setHeight(height: Float): Builder {
            this.mHeight = height
            return this
        }

        fun setStrokeWidth(strokeWidth: Float): Builder {
            this.mStrokeWidth = strokeWidth
            return this
        }

        fun setBallRadius(ballRadius: Float): Builder {
            this.mBallRadius = ballRadius
            return this
        }

        fun setBallInterval(ballInterval: Float): Builder {
            this.mBallInterval = ballInterval
            return this
        }

        fun setBallCount(ballCount: Int): Builder {
            this.mBallCount = ballCount
            return this
        }

        fun setColor(color: Int): Builder {
            this.mColor = color
            return this
        }

        fun setDuration(duration: Long): Builder {
            this.mDuration = duration
            return this
        }

        fun build(): SwapLoadingRenderer {
            val loadingRenderer = SwapLoadingRenderer(mContext)
            loadingRenderer.apply(this)
            return loadingRenderer
        }
    }

    companion object {
        private val ACCELERATE_DECELERATE_INTERPOLATOR = AccelerateDecelerateInterpolator()

        private val ANIMATION_DURATION: Long = 2500

        private val DEFAULT_CIRCLE_COUNT = 5

        private val DEFAULT_BALL_RADIUS = 7.5f
        private val DEFAULT_WIDTH = 15.0f * 11
        private val DEFAULT_HEIGHT = 15.0f * 5
        private val DEFAULT_STROKE_WIDTH = 1.5f

        private val DEFAULT_COLOR = Color.WHITE
    }
}

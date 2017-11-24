package app.dinus.com.loadingdrawable.render.circle.jump

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.support.annotation.Size
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class CollisionLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mOvalRect = RectF()

    @Size(2)
    private var mColors: IntArray? = null
    private var mPositions: FloatArray? = null

    private var mOvalVerticalRadius: Float = 0.toFloat()

    private var mBallRadius: Float = 0.toFloat()
    private var mBallCenterY: Float = 0.toFloat()
    private var mBallSideOffsets: Float = 0.toFloat()
    private var mBallMoveXOffsets: Float = 0.toFloat()
    private var mBallQuadCoefficient: Float = 0.toFloat()

    private var mLeftBallMoveXOffsets: Float = 0.toFloat()
    private var mLeftBallMoveYOffsets: Float = 0.toFloat()
    private var mRightBallMoveXOffsets: Float = 0.toFloat()
    private var mRightBallMoveYOffsets: Float = 0.toFloat()

    private var mLeftOvalShapeRate: Float = 0.toFloat()
    private var mRightOvalShapeRate: Float = 0.toFloat()

    private var mBallCount: Int = 0

    init {
        init(context)
        adjustParams()
        setupPaint()
    }

    private fun init(context: Context) {
        mBallRadius = DensityUtil.dip2px(context, DEFAULT_BALL_RADIUS)
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH)
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT)
        mOvalVerticalRadius = DensityUtil.dip2px(context, DEFAULT_OVAL_HEIGHT)

        mColors = DEFAULT_COLORS
        mPositions = DEFAULT_POSITIONS

        mBallCount = DEFAULT_BALL_COUNT

        //mBallMoveYOffsets = mBallQuadCoefficient * mBallMoveXOffsets ^ 2
        // ==> if mBallMoveYOffsets == mBallMoveXOffsets
        // ==> mBallQuadCoefficient = 1.0f / mBallMoveXOffsets;
        mBallMoveXOffsets = 1.5f * (2 * mBallRadius)
        mBallQuadCoefficient = 1.0f / mBallMoveXOffsets
    }

    private fun adjustParams() {
        mBallCenterY = mHeight / 2.0f
        mBallSideOffsets = (mWidth - mBallRadius * 2.0f * (mBallCount - 2).toFloat()) / 2
    }

    private fun setupPaint() {
        mPaint.style = Paint.Style.FILL
        mPaint.shader = LinearGradient(mBallSideOffsets, 0f, mWidth - mBallSideOffsets, 0f,
                mColors!!, mPositions, Shader.TileMode.CLAMP)
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()

        for (i in 1 until mBallCount - 1) {
            mPaint.alpha = MAX_ALPHA
            canvas.drawCircle(mBallRadius * (i * 2 - 1) + mBallSideOffsets, mBallCenterY, mBallRadius, mPaint)

            mOvalRect.set(mBallRadius * (i * 2 - 2) + mBallSideOffsets, mHeight - mOvalVerticalRadius * 2,
                    mBallRadius * (i * 2) + mBallSideOffsets, mHeight)
            mPaint.alpha = OVAL_ALPHA
            canvas.drawOval(mOvalRect, mPaint)
        }

        //draw the first ball
        mPaint.alpha = MAX_ALPHA
        canvas.drawCircle(mBallSideOffsets - mBallRadius - mLeftBallMoveXOffsets,
                mBallCenterY - mLeftBallMoveYOffsets, mBallRadius, mPaint)

        mOvalRect.set(mBallSideOffsets - mBallRadius - mBallRadius * mLeftOvalShapeRate - mLeftBallMoveXOffsets,
                mHeight - mOvalVerticalRadius - mOvalVerticalRadius * mLeftOvalShapeRate,
                mBallSideOffsets - mBallRadius + mBallRadius * mLeftOvalShapeRate - mLeftBallMoveXOffsets,
                mHeight - mOvalVerticalRadius + mOvalVerticalRadius * mLeftOvalShapeRate)
        mPaint.alpha = OVAL_ALPHA
        canvas.drawOval(mOvalRect, mPaint)

        //draw the last ball
        mPaint.alpha = MAX_ALPHA
        canvas.drawCircle(mBallRadius * (mBallCount * 2 - 3) + mBallSideOffsets + mRightBallMoveXOffsets,
                mBallCenterY - mRightBallMoveYOffsets, mBallRadius, mPaint)

        mOvalRect.set(mBallRadius * (mBallCount * 2 - 3) - mBallRadius * mRightOvalShapeRate + mBallSideOffsets + mRightBallMoveXOffsets,
                mHeight - mOvalVerticalRadius - mOvalVerticalRadius * mRightOvalShapeRate,
                mBallRadius * (mBallCount * 2 - 3) + mBallRadius * mRightOvalShapeRate + mBallSideOffsets + mRightBallMoveXOffsets,
                mHeight - mOvalVerticalRadius + mOvalVerticalRadius * mRightOvalShapeRate)
        mPaint.alpha = OVAL_ALPHA
        canvas.drawOval(mOvalRect, mPaint)

        canvas.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        // Moving the left ball to the left sides only occurs in the first 25% of a jump animation
        if (renderProgress <= START_LEFT_DURATION_OFFSET) {
            val startLeftOffsetProgress = renderProgress / START_LEFT_DURATION_OFFSET
            computeLeftBallMoveOffsets(DECELERATE_INTERPOLATOR.getInterpolation(startLeftOffsetProgress))
            return
        }

        // Moving the left ball to the origin location only occurs between 25% and 50% of a jump ring animation
        if (renderProgress <= START_RIGHT_DURATION_OFFSET) {
            val startRightOffsetProgress = (renderProgress - START_LEFT_DURATION_OFFSET) / (START_RIGHT_DURATION_OFFSET - START_LEFT_DURATION_OFFSET)
            computeLeftBallMoveOffsets(ACCELERATE_INTERPOLATOR.getInterpolation(1.0f - startRightOffsetProgress))
            return
        }

        // Moving the right ball to the right sides only occurs between 50% and 75% of a jump animation
        if (renderProgress <= END_RIGHT_DURATION_OFFSET) {
            val endRightOffsetProgress = (renderProgress - START_RIGHT_DURATION_OFFSET) / (END_RIGHT_DURATION_OFFSET - START_RIGHT_DURATION_OFFSET)
            computeRightBallMoveOffsets(DECELERATE_INTERPOLATOR.getInterpolation(endRightOffsetProgress))
            return
        }

        // Moving the right ball to the origin location only occurs after 75% of a jump animation
        if (renderProgress <= END_LEFT_DURATION_OFFSET) {
            val endRightOffsetProgress = (renderProgress - END_RIGHT_DURATION_OFFSET) / (END_LEFT_DURATION_OFFSET - END_RIGHT_DURATION_OFFSET)
            computeRightBallMoveOffsets(ACCELERATE_INTERPOLATOR.getInterpolation(1 - endRightOffsetProgress))
            return
        }

    }

    private fun computeLeftBallMoveOffsets(progress: Float) {
        mRightBallMoveXOffsets = 0.0f
        mRightBallMoveYOffsets = 0.0f

        mLeftOvalShapeRate = 1.0f - progress
        mLeftBallMoveXOffsets = mBallMoveXOffsets * progress
        mLeftBallMoveYOffsets = (Math.pow(mLeftBallMoveXOffsets.toDouble(), 2.0) * mBallQuadCoefficient).toFloat()
    }

    private fun computeRightBallMoveOffsets(progress: Float) {
        mLeftBallMoveXOffsets = 0.0f
        mLeftBallMoveYOffsets = 0.0f

        mRightOvalShapeRate = 1.0f - progress
        mRightBallMoveXOffsets = mBallMoveXOffsets * progress
        mRightBallMoveYOffsets = (Math.pow(mRightBallMoveXOffsets.toDouble(), 2.0) * mBallQuadCoefficient).toFloat()
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun reset() {}

    private fun apply(builder: Builder) {
        this.mWidth = if (builder.mWidth > 0) builder.mWidth.toFloat() else this.mWidth
        this.mHeight = if (builder.mHeight > 0) builder.mHeight.toFloat() else this.mHeight

        this.mOvalVerticalRadius = if (builder.mOvalVerticalRadius > 0) builder.mOvalVerticalRadius else this.mOvalVerticalRadius
        this.mBallRadius = if (builder.mBallRadius > 0) builder.mBallRadius else this.mBallRadius
        this.mBallMoveXOffsets = if (builder.mBallMoveXOffsets > 0) builder.mBallMoveXOffsets else this.mBallMoveXOffsets
        this.mBallQuadCoefficient = if (builder.mBallQuadCoefficient > 0) builder.mBallQuadCoefficient else this.mBallQuadCoefficient
        this.mBallCount = if (builder.mBallCount > 0) builder.mBallCount else this.mBallCount

        this.mDuration = if (builder.mDuration > 0) builder.mDuration else this.mDuration

        this.mColors = if (builder.mColors != null) builder.mColors else this.mColors

        adjustParams()
        setupPaint()
    }

    class Builder(private val mContext: Context) {

        var mWidth: Int = 0
        var mHeight: Int = 0

        var mOvalVerticalRadius: Float = 0.toFloat()

        var mBallCount: Int = 0
        var mBallRadius: Float = 0.toFloat()
        var mBallMoveXOffsets: Float = 0.toFloat()
        var mBallQuadCoefficient: Float = 0.toFloat()

        var mDuration: Long = 0

        @Size(2)
        var mColors: IntArray? = null

        fun setWidth(width: Int): Builder {
            this.mWidth = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.mHeight = height
            return this
        }

        fun setOvalVerticalRadius(ovalVerticalRadius: Int): Builder {
            this.mOvalVerticalRadius = ovalVerticalRadius.toFloat()
            return this
        }

        fun setBallRadius(ballRadius: Int): Builder {
            this.mBallRadius = ballRadius.toFloat()
            return this
        }

        fun setBallMoveXOffsets(ballMoveXOffsets: Int): Builder {
            this.mBallMoveXOffsets = ballMoveXOffsets.toFloat()
            return this
        }

        fun setBallQuadCoefficient(ballQuadCoefficient: Int): Builder {
            this.mBallQuadCoefficient = ballQuadCoefficient.toFloat()
            return this
        }

        fun setBallCount(ballCount: Int): Builder {
            this.mBallCount = ballCount
            return this
        }

        fun setColors(@Size(2) colors: IntArray): Builder {
            this.mColors = colors
            return this
        }

        fun setDuration(duration: Long): Builder {
            this.mDuration = duration
            return this
        }

        fun build(): CollisionLoadingRenderer {
            val loadingRenderer = CollisionLoadingRenderer(mContext)
            loadingRenderer.apply(this)
            return loadingRenderer
        }
    }

    companion object {
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator()

        private val MAX_ALPHA = 255
        private val OVAL_ALPHA = 64

        private val DEFAULT_BALL_COUNT = 7

        private val DEFAULT_OVAL_HEIGHT = 1.5f
        private val DEFAULT_BALL_RADIUS = 7.5f
        private val DEFAULT_WIDTH = 15.0f * 11
        private val DEFAULT_HEIGHT = 15.0f * 4

        private val START_LEFT_DURATION_OFFSET = 0.25f
        private val START_RIGHT_DURATION_OFFSET = 0.5f
        private val END_RIGHT_DURATION_OFFSET = 0.75f
        private val END_LEFT_DURATION_OFFSET = 1.0f

        private val DEFAULT_COLORS = intArrayOf(Color.parseColor("#FF28435D"), Color.parseColor("#FFC32720"))

        private val DEFAULT_POSITIONS = floatArrayOf(0.0f, 1.0f)
    }
}

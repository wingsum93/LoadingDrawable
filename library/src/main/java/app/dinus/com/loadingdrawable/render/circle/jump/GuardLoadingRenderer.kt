package app.dinus.com.loadingdrawable.render.circle.jump

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Rect
import android.graphics.RectF
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.DisplayMetrics
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class GuardLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

    private val mPaint = Paint()
    private val mTempBounds = RectF()
    private val mCurrentBounds = RectF()
    private val mCurrentPosition = FloatArray(2)

    private var mStrokeInset: Float = 0.toFloat()
    private var mSkipBallSize: Float = 0.toFloat()

    private var mScale: Float = 0.toFloat()
    private var mEndTrim: Float = 0.toFloat()
    private var mRotation: Float = 0.toFloat()
    private var mStartTrim: Float = 0.toFloat()
    private var mWaveProgress: Float = 0.toFloat()

    private var mStrokeWidth: Float = 0.toFloat()
    private var mCenterRadius: Float = 0.toFloat()

    private var mColor: Int = 0
    private var mBallColor: Int = 0

    private var mPathMeasure: PathMeasure? = null

    init {

        mDuration = ANIMATION_DURATION
        init(context)
        setupPaint()
    }

    private fun init(context: Context) {
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS)
        mSkipBallSize = DensityUtil.dip2px(context, DEFAULT_SKIP_BALL_RADIUS)

        mColor = DEFAULT_COLOR
        mBallColor = DEFAULT_BALL_COLOR
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND

        setInsets(mWidth.toInt(), mHeight.toInt())
    }

    override fun draw(canvas: Canvas, bounds: Rect) {
        val arcBounds = mTempBounds
        arcBounds.set(bounds)
        arcBounds.inset(mStrokeInset, mStrokeInset)
        mCurrentBounds.set(arcBounds)

        val saveCount = canvas.save()

        //draw circle trim
        val startAngle = (mStartTrim + mRotation) * 360
        val endAngle = (mEndTrim + mRotation) * 360
        val sweepAngle = endAngle - startAngle
        if (sweepAngle != 0f) {
            mPaint.color = mColor
            mPaint.style = Paint.Style.STROKE
            canvas.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint)
        }

        //draw water wave
        if (mWaveProgress < 1.0f) {
            mPaint.color = Color.argb((Color.alpha(mColor) * (1.0f - mWaveProgress)).toInt(),
                    Color.red(mColor), Color.green(mColor), Color.blue(mColor))
            mPaint.style = Paint.Style.STROKE
            val radius = Math.min(arcBounds.width(), arcBounds.height()) / 2.0f
            canvas.drawCircle(arcBounds.centerX(), arcBounds.centerY(), radius * (1.0f + mWaveProgress), mPaint)
        }
        //draw ball bounce
        if (mPathMeasure != null) {
            mPaint.color = mBallColor
            mPaint.style = Paint.Style.FILL
            canvas.drawCircle(mCurrentPosition[0], mCurrentPosition[1], mSkipBallSize * mScale, mPaint)
        }

        canvas.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            val startTrimProgress = renderProgress / START_TRIM_DURATION_OFFSET
            mEndTrim = -MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress)
            mRotation = START_TRIM_INIT_ROTATION + START_TRIM_MAX_ROTATION * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress)
        }

        if (renderProgress <= WAVE_DURATION_OFFSET && renderProgress > START_TRIM_DURATION_OFFSET) {
            val waveProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (WAVE_DURATION_OFFSET - START_TRIM_DURATION_OFFSET)
            mWaveProgress = ACCELERATE_INTERPOLATOR.getInterpolation(waveProgress)
        }

        if (renderProgress <= BALL_SKIP_DURATION_OFFSET && renderProgress > WAVE_DURATION_OFFSET) {
            if (mPathMeasure == null) {
                mPathMeasure = PathMeasure(createSkipBallPath(), false)
            }

            val ballSkipProgress = (renderProgress - WAVE_DURATION_OFFSET) / (BALL_SKIP_DURATION_OFFSET - WAVE_DURATION_OFFSET)
            mPathMeasure!!.getPosTan(ballSkipProgress * mPathMeasure!!.length, mCurrentPosition, null)

            mWaveProgress = 1.0f
        }

        if (renderProgress <= BALL_SCALE_DURATION_OFFSET && renderProgress > BALL_SKIP_DURATION_OFFSET) {
            val ballScaleProgress = (renderProgress - BALL_SKIP_DURATION_OFFSET) / (BALL_SCALE_DURATION_OFFSET - BALL_SKIP_DURATION_OFFSET)
            if (ballScaleProgress < 0.5f) {
                mScale = 1.0f + DECELERATE_INTERPOLATOR.getInterpolation(ballScaleProgress * 2.0f)
            } else {
                mScale = 2.0f - ACCELERATE_INTERPOLATOR.getInterpolation((ballScaleProgress - 0.5f) * 2.0f) * 2.0f
            }
        }

        if (renderProgress >= BALL_SCALE_DURATION_OFFSET) {
            val endTrimProgress = (renderProgress - BALL_SKIP_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - BALL_SKIP_DURATION_OFFSET)
            mEndTrim = -1 + MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress)
            mRotation = END_TRIM_INIT_ROTATION + END_TRIM_MAX_ROTATION * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress)

            mScale = 1.0f
            mPathMeasure = null
        }
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha

    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf

    }

    override fun reset() {
        mScale = 1.0f
        mEndTrim = 0.0f
        mRotation = 0.0f
        mStartTrim = 0.0f
        mWaveProgress = 1.0f
    }

    private fun createSkipBallPath(): Path {
        val radius = Math.min(mCurrentBounds.width(), mCurrentBounds.height()) / 2.0f
        val radiusPow2 = Math.pow(radius.toDouble(), 2.0).toFloat()
        val originCoordinateX = mCurrentBounds.centerX()
        val originCoordinateY = mCurrentBounds.centerY()

        val coordinateX = floatArrayOf(0.0f, 0.0f, -0.8f * radius, 0.75f * radius, -0.45f * radius, 0.9f * radius, -0.5f * radius)
        val sign = floatArrayOf(1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f)

        val path = Path()
        for (i in coordinateX.indices) {
            // x^2 + y^2 = radius^2 --> y = sqrt(radius^2 - x^2)
            if (i == 0) {
                path.moveTo(
                        originCoordinateX + coordinateX[i],
                        originCoordinateY + sign[i] * Math.sqrt(radiusPow2 - Math.pow(coordinateX[i].toDouble(), 2.0)).toFloat())
                continue
            }

            path.lineTo(
                    originCoordinateX + coordinateX[i],
                    originCoordinateY + sign[i] * Math.sqrt(radiusPow2 - Math.pow(coordinateX[i].toDouble(), 2.0)).toFloat())

            if (i == coordinateX.size - 1) {
                path.lineTo(originCoordinateX, originCoordinateY)
            }
        }
        return path
    }

    private fun setInsets(width: Int, height: Int) {
        val minEdge = Math.min(width, height).toFloat()
        val insets: Float
        if (mCenterRadius <= 0 || minEdge < 0) {
            insets = Math.ceil((mStrokeWidth / 2.0f).toDouble()).toFloat()
        } else {
            insets = minEdge / 2.0f - mCenterRadius
        }
        mStrokeInset = insets
    }

    class Builder(private val mContext: Context) {

        fun build(): GuardLoadingRenderer {
            return GuardLoadingRenderer(mContext)
        }
    }

    companion object {
        private val MATERIAL_INTERPOLATOR = FastOutSlowInInterpolator()
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator()

        private val ANIMATION_DURATION: Long = 5000

        private val DEFAULT_STROKE_WIDTH = 1.0f
        private val DEFAULT_CENTER_RADIUS = 12.5f
        private val DEFAULT_SKIP_BALL_RADIUS = 1.0f

        private val START_TRIM_INIT_ROTATION = -0.5f
        private val START_TRIM_MAX_ROTATION = -0.25f
        private val END_TRIM_INIT_ROTATION = 0.25f
        private val END_TRIM_MAX_ROTATION = 0.75f

        private val START_TRIM_DURATION_OFFSET = 0.23f
        private val WAVE_DURATION_OFFSET = 0.36f
        private val BALL_SKIP_DURATION_OFFSET = 0.74f
        private val BALL_SCALE_DURATION_OFFSET = 0.82f
        private val END_TRIM_DURATION_OFFSET = 1.0f

        private val DEFAULT_COLOR = Color.WHITE
        private val DEFAULT_BALL_COLOR = Color.RED
    }
}

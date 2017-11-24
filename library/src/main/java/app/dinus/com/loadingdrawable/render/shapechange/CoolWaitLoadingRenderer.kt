package app.dinus.com.loadingdrawable.render.shapechange

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Rect
import android.graphics.RectF
import android.util.DisplayMetrics
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class CoolWaitLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {
    private val ACCELERATE_INTERPOLATOR08 = AccelerateInterpolator(0.8f)
    private val ACCELERATE_INTERPOLATOR10 = AccelerateInterpolator(1.0f)
    private val ACCELERATE_INTERPOLATOR15 = AccelerateInterpolator(1.5f)

    private val DECELERATE_INTERPOLATOR03 = DecelerateInterpolator(0.3f)
    private val DECELERATE_INTERPOLATOR05 = DecelerateInterpolator(0.5f)
    private val DECELERATE_INTERPOLATOR08 = DecelerateInterpolator(0.8f)
    private val DECELERATE_INTERPOLATOR10 = DecelerateInterpolator(1.0f)

    private val DEFAULT_WIDTH = 200.0f
    private val DEFAULT_HEIGHT = 150.0f
    private val DEFAULT_STROKE_WIDTH = 8.0f
    private val WAIT_CIRCLE_RADIUS = 50.0f

    private val ANIMATION_DURATION: Long = 2222

    private val mPaint = Paint()

    private val mWaitPath = Path()
    private val mCurrentTopWaitPath = Path()
    private val mCurrentMiddleWaitPath = Path()
    private val mCurrentBottomWaitPath = Path()
    private val mWaitPathMeasure = PathMeasure()

    private val mCurrentBounds = RectF()

    private var mStrokeWidth: Float = 0.toFloat()
    private var mWaitCircleRadius: Float = 0.toFloat()
    private var mOriginEndDistance: Float = 0.toFloat()
    private var mOriginStartDistance: Float = 0.toFloat()
    private var mWaitPathLength: Float = 0.toFloat()

    private var mTopColor: Int = 0
    private var mMiddleColor: Int = 0
    private var mBottomColor: Int = 0

    init {
        init(context)
        setupPaint()
    }

    private fun init(context: Context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH)
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT)
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)
        mWaitCircleRadius = DensityUtil.dip2px(context, WAIT_CIRCLE_RADIUS)

        mTopColor = Color.WHITE
        mMiddleColor = Color.parseColor("#FFF3C742")
        mBottomColor = Color.parseColor("#FF89CC59")

        mDuration = ANIMATION_DURATION
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun draw(canvas: Canvas, bounds: Rect) {
        val saveCount = canvas.save()
        val arcBounds = mCurrentBounds
        arcBounds.set(bounds)

        mPaint.color = mBottomColor
        canvas.drawPath(mCurrentBottomWaitPath, mPaint)

        mPaint.color = mMiddleColor
        canvas.drawPath(mCurrentMiddleWaitPath, mPaint)

        mPaint.color = mTopColor
        canvas.drawPath(mCurrentTopWaitPath, mPaint)

        canvas.restoreToCount(saveCount)
    }

    private fun createWaitPath(bounds: RectF): Path {
        val path = Path()
        //create circle
        path.moveTo(bounds.centerX() + mWaitCircleRadius, bounds.centerY())

        //create w
        path.cubicTo(bounds.centerX() + mWaitCircleRadius, bounds.centerY() - mWaitCircleRadius * 0.5f,
                bounds.centerX() + mWaitCircleRadius * 0.3f, bounds.centerY() - mWaitCircleRadius,
                bounds.centerX() - mWaitCircleRadius * 0.35f, bounds.centerY() + mWaitCircleRadius * 0.5f)
        path.quadTo(bounds.centerX() + mWaitCircleRadius, bounds.centerY() - mWaitCircleRadius,
                bounds.centerX() + mWaitCircleRadius * 0.05f, bounds.centerY() + mWaitCircleRadius * 0.5f)
        path.lineTo(bounds.centerX() + mWaitCircleRadius * 0.75f, bounds.centerY() - mWaitCircleRadius * 0.2f)

        path.cubicTo(bounds.centerX(), bounds.centerY() + mWaitCircleRadius * 1f,
                bounds.centerX() + mWaitCircleRadius, bounds.centerY() + mWaitCircleRadius * 0.4f,
                bounds.centerX() + mWaitCircleRadius, bounds.centerY())

        //create arc
        path.arcTo(RectF(bounds.centerX() - mWaitCircleRadius, bounds.centerY() - mWaitCircleRadius,
                bounds.centerX() + mWaitCircleRadius, bounds.centerY() + mWaitCircleRadius), 0f, -359f)
        path.arcTo(RectF(bounds.centerX() - mWaitCircleRadius, bounds.centerY() - mWaitCircleRadius,
                bounds.centerX() + mWaitCircleRadius, bounds.centerY() + mWaitCircleRadius), 1f, -359f)
        path.arcTo(RectF(bounds.centerX() - mWaitCircleRadius, bounds.centerY() - mWaitCircleRadius,
                bounds.centerX() + mWaitCircleRadius, bounds.centerY() + mWaitCircleRadius), 2f, -2f)
        //create w
        path.cubicTo(bounds.centerX() + mWaitCircleRadius, bounds.centerY() - mWaitCircleRadius * 0.5f,
                bounds.centerX() + mWaitCircleRadius * 0.3f, bounds.centerY() - mWaitCircleRadius,
                bounds.centerX() - mWaitCircleRadius * 0.35f, bounds.centerY() + mWaitCircleRadius * 0.5f)
        path.quadTo(bounds.centerX() + mWaitCircleRadius, bounds.centerY() - mWaitCircleRadius,
                bounds.centerX() + mWaitCircleRadius * 0.05f, bounds.centerY() + mWaitCircleRadius * 0.5f)
        path.lineTo(bounds.centerX() + mWaitCircleRadius * 0.75f, bounds.centerY() - mWaitCircleRadius * 0.2f)

        path.cubicTo(bounds.centerX(), bounds.centerY() + mWaitCircleRadius * 1f,
                bounds.centerX() + mWaitCircleRadius, bounds.centerY() + mWaitCircleRadius * 0.4f,
                bounds.centerX() + mWaitCircleRadius, bounds.centerY())

        return path
    }

    override fun computeRender(renderProgress: Float) {
        if (mCurrentBounds.isEmpty) {
            return
        }

        if (mWaitPath.isEmpty) {
            mWaitPath.set(createWaitPath(mCurrentBounds))
            mWaitPathMeasure.setPath(mWaitPath, false)
            mWaitPathLength = mWaitPathMeasure.length

            mOriginEndDistance = mWaitPathLength * 0.255f
            mOriginStartDistance = mWaitPathLength * 0.045f
        }

        mCurrentTopWaitPath.reset()
        mCurrentMiddleWaitPath.reset()
        mCurrentBottomWaitPath.reset()

        //draw the first half : top
        if (renderProgress <= WAIT_TRIM_DURATION_OFFSET) {
            val topTrimProgress = ACCELERATE_DECELERATE_INTERPOLATOR.getInterpolation(renderProgress / WAIT_TRIM_DURATION_OFFSET)
            val topEndDistance = mOriginEndDistance + mWaitPathLength * 0.3f * topTrimProgress
            val topStartDistance = mOriginStartDistance + mWaitPathLength * 0.48f * topTrimProgress
            mWaitPathMeasure.getSegment(topStartDistance, topEndDistance, mCurrentTopWaitPath, true)
        }

        //draw the first half : middle
        if (renderProgress > 0.02f * WAIT_TRIM_DURATION_OFFSET && renderProgress <= WAIT_TRIM_DURATION_OFFSET * 0.75f) {
            val middleStartTrimProgress = ACCELERATE_INTERPOLATOR10.getInterpolation((renderProgress - 0.02f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.73f))
            val middleEndTrimProgress = DECELERATE_INTERPOLATOR08.getInterpolation((renderProgress - 0.02f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.73f))

            val middleEndDistance = mOriginStartDistance + mWaitPathLength * 0.42f * middleEndTrimProgress
            val middleStartDistance = mOriginStartDistance + mWaitPathLength * 0.42f * middleStartTrimProgress
            mWaitPathMeasure.getSegment(middleStartDistance, middleEndDistance, mCurrentMiddleWaitPath, true)
        }

        //draw the first half : bottom
        if (renderProgress > 0.04f * WAIT_TRIM_DURATION_OFFSET && renderProgress <= WAIT_TRIM_DURATION_OFFSET * 0.75f) {
            val bottomStartTrimProgress = ACCELERATE_INTERPOLATOR15.getInterpolation((renderProgress - 0.04f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.71f))
            val bottomEndTrimProgress = DECELERATE_INTERPOLATOR05.getInterpolation((renderProgress - 0.04f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.71f))

            val bottomEndDistance = mOriginStartDistance + mWaitPathLength * 0.42f * bottomEndTrimProgress
            val bottomStartDistance = mOriginStartDistance + mWaitPathLength * 0.42f * bottomStartTrimProgress
            mWaitPathMeasure.getSegment(bottomStartDistance, bottomEndDistance, mCurrentBottomWaitPath, true)
        }

        //draw the last half : top
        if (renderProgress <= END_TRIM_DURATION_OFFSET && renderProgress > WAIT_TRIM_DURATION_OFFSET) {
            val trimProgress = ACCELERATE_DECELERATE_INTERPOLATOR.getInterpolation((renderProgress - WAIT_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - WAIT_TRIM_DURATION_OFFSET))
            val topEndDistance = mOriginEndDistance + mWaitPathLength * 0.3f + mWaitPathLength * 0.45f * trimProgress
            val topStartDistance = mOriginStartDistance + mWaitPathLength * 0.48f + mWaitPathLength * 0.27f * trimProgress
            mWaitPathMeasure.getSegment(topStartDistance, topEndDistance, mCurrentTopWaitPath, true)
        }

        //draw the last half : middle
        if (renderProgress > WAIT_TRIM_DURATION_OFFSET + 0.02f * WAIT_TRIM_DURATION_OFFSET && renderProgress <= WAIT_TRIM_DURATION_OFFSET + WAIT_TRIM_DURATION_OFFSET * 0.62f) {
            val middleStartTrimProgress = ACCELERATE_INTERPOLATOR08.getInterpolation((renderProgress - WAIT_TRIM_DURATION_OFFSET - 0.02f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.60f))
            val middleEndTrimProgress = DECELERATE_INTERPOLATOR03.getInterpolation((renderProgress - WAIT_TRIM_DURATION_OFFSET - 0.02f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.60f))

            val middleEndDistance = mOriginStartDistance + mWaitPathLength * 0.48f + mWaitPathLength * 0.20f * middleEndTrimProgress
            val middleStartDistance = mOriginStartDistance + mWaitPathLength * 0.48f + mWaitPathLength * 0.10f * middleStartTrimProgress
            mWaitPathMeasure.getSegment(middleStartDistance, middleEndDistance, mCurrentMiddleWaitPath, true)
        }

        if (renderProgress > WAIT_TRIM_DURATION_OFFSET + 0.62f * WAIT_TRIM_DURATION_OFFSET && renderProgress <= END_TRIM_DURATION_OFFSET) {
            val middleStartTrimProgress = DECELERATE_INTERPOLATOR10.getInterpolation((renderProgress - WAIT_TRIM_DURATION_OFFSET - 0.62f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.38f))
            val middleEndTrimProgress = DECELERATE_INTERPOLATOR03.getInterpolation((renderProgress - WAIT_TRIM_DURATION_OFFSET - 0.62f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.38f))

            val middleEndDistance = mOriginStartDistance + mWaitPathLength * 0.68f + mWaitPathLength * 0.325f * middleEndTrimProgress
            val middleStartDistance = mOriginStartDistance + mWaitPathLength * 0.58f + mWaitPathLength * 0.17f * middleStartTrimProgress
            mWaitPathMeasure.getSegment(middleStartDistance, middleEndDistance, mCurrentMiddleWaitPath, true)
        }

        //draw the last half : bottom
        if (renderProgress > WAIT_TRIM_DURATION_OFFSET + 0.10f * WAIT_TRIM_DURATION_OFFSET && renderProgress <= WAIT_TRIM_DURATION_OFFSET + WAIT_TRIM_DURATION_OFFSET * 0.70f) {
            val bottomStartTrimProgress = ACCELERATE_INTERPOLATOR15.getInterpolation((renderProgress - WAIT_TRIM_DURATION_OFFSET - 0.10f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.60f))
            val bottomEndTrimProgress = DECELERATE_INTERPOLATOR03.getInterpolation((renderProgress - WAIT_TRIM_DURATION_OFFSET - 0.10f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.60f))

            val bottomEndDistance = mOriginStartDistance + mWaitPathLength * 0.48f + mWaitPathLength * 0.20f * bottomEndTrimProgress
            val bottomStartDistance = mOriginStartDistance + mWaitPathLength * 0.48f + mWaitPathLength * 0.10f * bottomStartTrimProgress
            mWaitPathMeasure.getSegment(bottomStartDistance, bottomEndDistance, mCurrentBottomWaitPath, true)
        }

        if (renderProgress > WAIT_TRIM_DURATION_OFFSET + 0.70f * WAIT_TRIM_DURATION_OFFSET && renderProgress <= END_TRIM_DURATION_OFFSET) {
            val bottomStartTrimProgress = DECELERATE_INTERPOLATOR05.getInterpolation((renderProgress - WAIT_TRIM_DURATION_OFFSET - 0.70f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.30f))
            val bottomEndTrimProgress = DECELERATE_INTERPOLATOR03.getInterpolation((renderProgress - WAIT_TRIM_DURATION_OFFSET - 0.70f * WAIT_TRIM_DURATION_OFFSET) / (WAIT_TRIM_DURATION_OFFSET * 0.30f))

            val bottomEndDistance = mOriginStartDistance + mWaitPathLength * 0.68f + mWaitPathLength * 0.325f * bottomEndTrimProgress
            val bottomStartDistance = mOriginStartDistance + mWaitPathLength * 0.58f + mWaitPathLength * 0.17f * bottomStartTrimProgress
            mWaitPathMeasure.getSegment(bottomStartDistance, bottomEndDistance, mCurrentBottomWaitPath, true)
        }

    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha

    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf

    }

    override fun reset() {}

    class Builder(private val mContext: Context) {

        fun build(): CoolWaitLoadingRenderer {
            return CoolWaitLoadingRenderer(mContext)
        }
    }

    companion object {

        private val ACCELERATE_DECELERATE_INTERPOLATOR = AccelerateDecelerateInterpolator()

        private val WAIT_TRIM_DURATION_OFFSET = 0.5f
        private val END_TRIM_DURATION_OFFSET = 1.0f
    }
}

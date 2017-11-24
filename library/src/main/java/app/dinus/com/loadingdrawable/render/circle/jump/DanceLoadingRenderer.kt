package app.dinus.com.loadingdrawable.render.circle.jump

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class DanceLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

    private val mPaint = Paint()
    private val mTempBounds = RectF()
    private val mCurrentBounds = RectF()

    private var mScale: Float = 0.toFloat()
    private var rotation: Float = 0.toFloat()
    private var mStrokeInset: Float = 0.toFloat()

    private var mCenterRadius: Float = 0.toFloat()
    private var mStrokeWidth: Float = 0.toFloat()
    private var danceBallRadius: Float = 0.toFloat()
    private var mShapeChangeWidth: Float = 0.toFloat()
    private var mShapeChangeHeight: Float = 0.toFloat()

    private var mColor: Int = 0
    private var mArcColor: Int = 0

    init {
        init(context)
        setupPaint()
    }

    private fun init(context: Context) {
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS)
        danceBallRadius = DensityUtil.dip2px(context, DEFAULT_DANCE_BALL_RADIUS)

        setColor(DEFAULT_COLOR)
        setInsets(mWidth.toInt(), mHeight.toInt())
        mDuration = ANIMATION_DURATION
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
        mPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas, bounds: Rect) {
        val saveCount = canvas.save()

        mTempBounds.set(bounds)
        mTempBounds.inset(mStrokeInset, mStrokeInset)
        mCurrentBounds.set(mTempBounds)

        val outerCircleRadius = Math.min(mTempBounds.height(), mTempBounds.width()) / 2.0f
        val interCircleRadius = outerCircleRadius / 2.0f
        val centerRingWidth = interCircleRadius - mStrokeWidth / 2

        mPaint.style = Paint.Style.STROKE
        mPaint.color = mColor
        mPaint.strokeWidth = mStrokeWidth
        canvas.drawCircle(mTempBounds.centerX(), mTempBounds.centerY(), outerCircleRadius, mPaint)
        mPaint.style = Paint.Style.FILL
        canvas.drawCircle(mTempBounds.centerX(), mTempBounds.centerY(), interCircleRadius * mScale, mPaint)

        if (rotation != 0f) {
            mPaint.color = mArcColor
            mPaint.style = Paint.Style.STROKE
            //strokeWidth / 2.0f + mStrokeWidth / 2.0f is the center of the inter circle width
            mTempBounds.inset(centerRingWidth / 2.0f + mStrokeWidth / 2.0f, centerRingWidth / 2.0f + mStrokeWidth / 2.0f)
            mPaint.strokeWidth = centerRingWidth
            canvas.drawArc(mTempBounds, RING_START_ANGLE.toFloat(), rotation, false, mPaint)
        }

        mPaint.color = mColor
        mPaint.style = Paint.Style.FILL
        for (i in 0 until NUM_POINTS) {
            canvas.rotate((i * DANCE_INTERVAL_ANGLE).toFloat(), POINT_X[i], POINT_Y[i])
            val rectF = RectF(POINT_X[i] - danceBallRadius - mShapeChangeWidth / 2.0f,
                    POINT_Y[i] - danceBallRadius - mShapeChangeHeight / 2.0f,
                    POINT_X[i] + danceBallRadius + mShapeChangeWidth / 2.0f,
                    POINT_Y[i] + danceBallRadius + mShapeChangeHeight / 2.0f)
            canvas.drawOval(rectF, mPaint)
            canvas.rotate((-i * DANCE_INTERVAL_ANGLE).toFloat(), POINT_X[i], POINT_Y[i])
        }

        canvas.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        val radius = Math.min(mCurrentBounds.height(), mCurrentBounds.width()) / 2.0f
        //the origin coordinate is the centerLeft of the field mCurrentBounds
        val originCoordinateX = mCurrentBounds.left
        val originCoordinateY = mCurrentBounds.top + radius

        if (renderProgress <= BALL_FORWARD_END_ENTER_DURATION_OFFSET && renderProgress > BALL_FORWARD_START_ENTER_DURATION_OFFSET) {
            val ballForwardEnterProgress = (renderProgress - BALL_FORWARD_START_ENTER_DURATION_OFFSET) / (BALL_FORWARD_END_ENTER_DURATION_OFFSET - BALL_FORWARD_START_ENTER_DURATION_OFFSET)

            mShapeChangeHeight = (0.5f - ballForwardEnterProgress) * danceBallRadius / 2.0f
            mShapeChangeWidth = -mShapeChangeHeight
            //y = k(x - r)--> k = tan(angle)
            //(x - r)^2 + y^2 = r^2
            // compute crossover point --> (k(x -r)) ^ 2 + (x - )^2 = r^2
            // so x --> [r + r / sqrt(k ^ 2 + 1), r - r / sqrt(k ^ 2 + 1)]
            for (i in 0 until NUM_POINTS) {
                val k = Math.tan((DANCE_START_ANGLE + DANCE_INTERVAL_ANGLE * i) / 360.0f * (2.0f * Math.PI)).toFloat()
                // progress[-1, 1]
                val progress = (ACCELERATE_INTERPOLATOR.getInterpolation(ballForwardEnterProgress) / 2.0f - 0.5f) * 2.0f * DIRECTION[i].toFloat()
                POINT_X[i] = (radius + progress * (radius / Math.sqrt(Math.pow(k.toDouble(), 2.0) + 1.0f))).toFloat()
                POINT_Y[i] = k * (POINT_X[i] - radius)

                POINT_X[i] += originCoordinateX
                POINT_Y[i] += originCoordinateY
            }
        }

        if (renderProgress <= RING_FORWARD_END_ROTATE_DURATION_OFFSET && renderProgress > RING_FORWARD_START_ROTATE_DURATION_OFFSET) {
            val forwardRotateProgress = (renderProgress - RING_FORWARD_START_ROTATE_DURATION_OFFSET) / (RING_FORWARD_END_ROTATE_DURATION_OFFSET - RING_FORWARD_START_ROTATE_DURATION_OFFSET)
            rotation = DEGREE_360 * MATERIAL_INTERPOLATOR.getInterpolation(forwardRotateProgress)
        }

        if (renderProgress <= CENTER_CIRCLE_FORWARD_END_SCALE_DURATION_OFFSET && renderProgress > CENTER_CIRCLE_FORWARD_START_SCALE_DURATION_OFFSET) {
            val centerCircleScaleProgress = (renderProgress - CENTER_CIRCLE_FORWARD_START_SCALE_DURATION_OFFSET) / (CENTER_CIRCLE_FORWARD_END_SCALE_DURATION_OFFSET - CENTER_CIRCLE_FORWARD_START_SCALE_DURATION_OFFSET)

            if (centerCircleScaleProgress <= 0.5f) {
                mScale = 1.0f + DECELERATE_INTERPOLATOR.getInterpolation(centerCircleScaleProgress * 2.0f) * 0.2f
            } else {
                mScale = 1.2f - ACCELERATE_INTERPOLATOR.getInterpolation((centerCircleScaleProgress - 0.5f) * 2.0f) * 0.2f
            }

        }

        if (renderProgress <= BALL_FORWARD_END_EXIT_DURATION_OFFSET && renderProgress > BALL_FORWARD_START_EXIT_DURATION_OFFSET) {
            val ballForwardExitProgress = (renderProgress - BALL_FORWARD_START_EXIT_DURATION_OFFSET) / (BALL_FORWARD_END_EXIT_DURATION_OFFSET - BALL_FORWARD_START_EXIT_DURATION_OFFSET)
            mShapeChangeHeight = (ballForwardExitProgress - 0.5f) * danceBallRadius / 2.0f
            mShapeChangeWidth = -mShapeChangeHeight
            for (i in 0 until NUM_POINTS) {
                val k = Math.tan((DANCE_START_ANGLE + DANCE_INTERVAL_ANGLE * i) / 360.0f * (2.0f * Math.PI)).toFloat()
                val progress = DECELERATE_INTERPOLATOR.getInterpolation(ballForwardExitProgress) / 2.0f * 2.0f * DIRECTION[i].toFloat()
                POINT_X[i] = (radius + progress * (radius / Math.sqrt(Math.pow(k.toDouble(), 2.0) + 1.0f))).toFloat()
                POINT_Y[i] = k * (POINT_X[i] - radius)

                POINT_X[i] += originCoordinateX
                POINT_Y[i] += originCoordinateY
            }
        }

        if (renderProgress <= RING_REVERSAL_END_ROTATE_DURATION_OFFSET && renderProgress > RING_REVERSAL_START_ROTATE_DURATION_OFFSET) {
            val scaledTime = (renderProgress - RING_REVERSAL_START_ROTATE_DURATION_OFFSET) / (RING_REVERSAL_END_ROTATE_DURATION_OFFSET - RING_REVERSAL_START_ROTATE_DURATION_OFFSET)
            rotation = DEGREE_360 * MATERIAL_INTERPOLATOR.getInterpolation(scaledTime) - 360
        } else if (renderProgress > RING_REVERSAL_END_ROTATE_DURATION_OFFSET) {
            rotation = 0.0f
        }

        if (renderProgress <= BALL_REVERSAL_END_ENTER_DURATION_OFFSET && renderProgress > BALL_REVERSAL_START_ENTER_DURATION_OFFSET) {
            val ballReversalEnterProgress = (renderProgress - BALL_REVERSAL_START_ENTER_DURATION_OFFSET) / (BALL_REVERSAL_END_ENTER_DURATION_OFFSET - BALL_REVERSAL_START_ENTER_DURATION_OFFSET)
            mShapeChangeHeight = (0.5f - ballReversalEnterProgress) * danceBallRadius / 2.0f
            mShapeChangeWidth = -mShapeChangeHeight

            for (i in 0 until NUM_POINTS) {
                val k = Math.tan((DANCE_START_ANGLE + DANCE_INTERVAL_ANGLE * i) / 360.0f * (2.0f * Math.PI)).toFloat()
                val progress = (0.5f - ACCELERATE_INTERPOLATOR.getInterpolation(ballReversalEnterProgress) / 2.0f) * 2.0f * DIRECTION[i].toFloat()
                POINT_X[i] = (radius + progress * (radius / Math.sqrt(Math.pow(k.toDouble(), 2.0) + 1.0f))).toFloat()
                POINT_Y[i] = k * (POINT_X[i] - radius)

                POINT_X[i] += originCoordinateX
                POINT_Y[i] += originCoordinateY
            }
        }

        if (renderProgress <= CENTER_CIRCLE_REVERSAL_END_SCALE_DURATION_OFFSET && renderProgress > CENTER_CIRCLE_REVERSAL_START_SCALE_DURATION_OFFSET) {
            val centerCircleScaleProgress = (renderProgress - CENTER_CIRCLE_REVERSAL_START_SCALE_DURATION_OFFSET) / (CENTER_CIRCLE_REVERSAL_END_SCALE_DURATION_OFFSET - CENTER_CIRCLE_REVERSAL_START_SCALE_DURATION_OFFSET)

            if (centerCircleScaleProgress <= 0.5f) {
                mScale = 1.0f + DECELERATE_INTERPOLATOR.getInterpolation(centerCircleScaleProgress * 2.0f) * 0.2f
            } else {
                mScale = 1.2f - ACCELERATE_INTERPOLATOR.getInterpolation((centerCircleScaleProgress - 0.5f) * 2.0f) * 0.2f
            }

        }

        if (renderProgress <= BALL_REVERSAL_END_EXIT_DURATION_OFFSET && renderProgress > BALL_REVERSAL_START_EXIT_DURATION_OFFSET) {
            val ballReversalExitProgress = (renderProgress - BALL_REVERSAL_START_EXIT_DURATION_OFFSET) / (BALL_REVERSAL_END_EXIT_DURATION_OFFSET - BALL_REVERSAL_START_EXIT_DURATION_OFFSET)
            mShapeChangeHeight = (ballReversalExitProgress - 0.5f) * danceBallRadius / 2.0f
            mShapeChangeWidth = -mShapeChangeHeight

            for (i in 0 until NUM_POINTS) {
                val k = Math.tan((DANCE_START_ANGLE + DANCE_INTERVAL_ANGLE * i) / 360.0f * (2.0f * Math.PI)).toFloat()
                val progress = (0.0f - DECELERATE_INTERPOLATOR.getInterpolation(ballReversalExitProgress) / 2.0f) * 2.0f * DIRECTION[i].toFloat()
                POINT_X[i] = (radius + progress * (radius / Math.sqrt(Math.pow(k.toDouble(), 2.0) + 1.0f))).toFloat()
                POINT_Y[i] = k * (POINT_X[i] - radius)

                POINT_X[i] += originCoordinateX
                POINT_Y[i] += originCoordinateY
            }
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
        rotation = 0f
    }

    private fun setColor(color: Int) {
        mColor = color
        mArcColor = halfAlphaColor(mColor)
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

    private fun halfAlphaColor(colorValue: Int): Int {
        val startA = colorValue shr 24 and 0xff
        val startR = colorValue shr 16 and 0xff
        val startG = colorValue shr 8 and 0xff
        val startB = colorValue and 0xff

        return (startA / 2 shl 24
                or (startR shl 16)
                or (startG shl 8)
                or startB)
    }

    class Builder(private val mContext: Context) {

        fun build(): DanceLoadingRenderer {
            return DanceLoadingRenderer(mContext)
        }
    }

    companion object {
        private val MATERIAL_INTERPOLATOR = FastOutSlowInInterpolator()
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator()

        private val ANIMATION_DURATION: Long = 1888

        private val DEFAULT_CENTER_RADIUS = 12.5f
        private val DEFAULT_STROKE_WIDTH = 1.5f
        private val DEFAULT_DANCE_BALL_RADIUS = 2.0f

        private val NUM_POINTS = 3
        private val DEGREE_360 = 360
        private val RING_START_ANGLE = -90
        private val DANCE_START_ANGLE = 0
        private val DANCE_INTERVAL_ANGLE = 60

        private val DEFAULT_COLOR = Color.WHITE

        //the center coordinate of the oval
        private val POINT_X = FloatArray(NUM_POINTS)
        private val POINT_Y = FloatArray(NUM_POINTS)
        //1: the coordinate x from small to large; -1: the coordinate x from large to small
        private val DIRECTION = intArrayOf(1, 1, -1)

        private val BALL_FORWARD_START_ENTER_DURATION_OFFSET = 0f
        private val BALL_FORWARD_END_ENTER_DURATION_OFFSET = 0.125f

        private val RING_FORWARD_START_ROTATE_DURATION_OFFSET = 0.125f
        private val RING_FORWARD_END_ROTATE_DURATION_OFFSET = 0.375f

        private val CENTER_CIRCLE_FORWARD_START_SCALE_DURATION_OFFSET = 0.225f
        private val CENTER_CIRCLE_FORWARD_END_SCALE_DURATION_OFFSET = 0.475f

        private val BALL_FORWARD_START_EXIT_DURATION_OFFSET = 0.375f
        private val BALL_FORWARD_END_EXIT_DURATION_OFFSET = 0.54f

        private val RING_REVERSAL_START_ROTATE_DURATION_OFFSET = 0.5f
        private val RING_REVERSAL_END_ROTATE_DURATION_OFFSET = 0.75f

        private val BALL_REVERSAL_START_ENTER_DURATION_OFFSET = 0.6f
        private val BALL_REVERSAL_END_ENTER_DURATION_OFFSET = 0.725f

        private val CENTER_CIRCLE_REVERSAL_START_SCALE_DURATION_OFFSET = 0.675f
        private val CENTER_CIRCLE_REVERSAL_END_SCALE_DURATION_OFFSET = 0.875f

        private val BALL_REVERSAL_START_EXIT_DURATION_OFFSET = 0.875f
        private val BALL_REVERSAL_END_EXIT_DURATION_OFFSET = 1.0f
    }
}

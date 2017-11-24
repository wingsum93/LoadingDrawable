package app.dinus.com.loadingdrawable.render.animal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.DisplayMetrics
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class GhostsEyeLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {
    private val EYE_BALL_INTERPOLATOR = EyeBallInterpolator()
    private val EYE_CIRCLE_INTERPOLATOR = EyeCircleInterpolator()

    private val mPaint = Paint()
    private val mTempBounds = RectF()

    private var mEyeInterval: Float = 0.toFloat()
    private var mEyeCircleRadius: Float = 0.toFloat()
    private var mMaxEyeJumptDistance: Float = 0.toFloat()
    private var mAboveRadianEyeOffsetX: Float = 0.toFloat()
    private var mEyeBallOffsetY: Float = 0.toFloat()

    private var mEyeEdgeWidth: Float = 0.toFloat()
    private var mEyeBallWidth: Float = 0.toFloat()
    private var mEyeBallHeight: Float = 0.toFloat()

    private var mLeftEyeCircleOffsetY: Float = 0.toFloat()
    private var mRightEyeCircleOffsetY: Float = 0.toFloat()
    private var mLeftEyeBallOffsetY: Float = 0.toFloat()
    private var mRightEyeBallOffsetY: Float = 0.toFloat()

    private var mColor: Int = 0

    init {
        init(context)
        setupPaint()
    }

    private fun init(context: Context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH)
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT)
        mEyeEdgeWidth = DensityUtil.dip2px(context, DEFAULT_EYE_EDGE_WIDTH)

        mEyeInterval = DensityUtil.dip2px(context, DEFAULT_EYE_CIRCLE_INTERVAL)
        mEyeBallOffsetY = DensityUtil.dip2px(context, DEFAULT_EYE_BALL_OFFSET_Y)
        mEyeCircleRadius = DensityUtil.dip2px(context, DEFAULT_EYE_CIRCLE_RADIUS)
        mMaxEyeJumptDistance = DensityUtil.dip2px(context, DEFAULT_MAX_EYE_JUMP_DISTANCE)
        mAboveRadianEyeOffsetX = DensityUtil.dip2px(context, DEFAULT_ABOVE_RADIAN_EYE_CIRCLE_OFFSET)

        mEyeBallWidth = DensityUtil.dip2px(context, DEFAULT_EYE_BALL_WIDTH)
        mEyeBallHeight = DensityUtil.dip2px(context, DEFAULT_EYE_BALL_HEIGHT)

        mColor = DEFAULT_COLOR

        mDuration = ANIMATION_DURATION
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mEyeEdgeWidth
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun draw(canvas: Canvas, bounds: Rect) {
        val saveCount = canvas.save()
        val arcBounds = mTempBounds
        arcBounds.set(bounds)

        mPaint.color = mColor

        mPaint.style = Paint.Style.STROKE
        canvas.drawPath(createLeftEyeCircle(arcBounds, mLeftEyeCircleOffsetY), mPaint)
        canvas.drawPath(createRightEyeCircle(arcBounds, mRightEyeCircleOffsetY), mPaint)

        mPaint.style = Paint.Style.FILL
        //create left eye ball
        canvas.drawOval(createLeftEyeBall(arcBounds, mLeftEyeBallOffsetY), mPaint)
        //create right eye ball
        canvas.drawOval(createRightEyeBall(arcBounds, mRightEyeBallOffsetY), mPaint)

        canvas.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        if (renderProgress <= LEFT_EYE_BALL_END_JUMP_OFFSET && renderProgress >= `LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`) {
            val `eyeCircle$BallJumpUpProgress` = (renderProgress - `LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`) / (LEFT_EYE_BALL_END_JUMP_OFFSET - `LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`)
            mLeftEyeBallOffsetY = -mMaxEyeJumptDistance * EYE_BALL_INTERPOLATOR.getInterpolation(`eyeCircle$BallJumpUpProgress`)
        }

        if (renderProgress <= LEFT_EYE_CIRCLE_END_JUMP_OFFSET && renderProgress >= `LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`) {
            val `eyeCircle$BallJumpUpProgress` = (renderProgress - `LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`) / (LEFT_EYE_CIRCLE_END_JUMP_OFFSET - `LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`)
            mLeftEyeCircleOffsetY = -mMaxEyeJumptDistance * EYE_CIRCLE_INTERPOLATOR.getInterpolation(`eyeCircle$BallJumpUpProgress`)
        }

        if (renderProgress <= RIGHT_EYE_BALL_END_JUMP_OFFSET && renderProgress >= `RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`) {
            val `eyeCircle$BallJumpUpProgress` = (renderProgress - `RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`) / (RIGHT_EYE_BALL_END_JUMP_OFFSET - `RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`)
            mRightEyeBallOffsetY = -mMaxEyeJumptDistance * EYE_BALL_INTERPOLATOR.getInterpolation(`eyeCircle$BallJumpUpProgress`)
        }

        if (renderProgress <= RIGHT_EYE_CIRCLE_END_JUMP_OFFSET && renderProgress >= `RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`) {
            val `eyeCircle$BallJumpUpProgress` = (renderProgress - `RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`) / (RIGHT_EYE_CIRCLE_END_JUMP_OFFSET - `RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET`)
            mRightEyeCircleOffsetY = -mMaxEyeJumptDistance * EYE_CIRCLE_INTERPOLATOR.getInterpolation(`eyeCircle$BallJumpUpProgress`)
        }
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(cf: ColorFilter?) {

    }

    override fun reset() {
        mLeftEyeBallOffsetY = 0.0f
        mRightEyeBallOffsetY = 0.0f
        mLeftEyeCircleOffsetY = 0.0f
        mRightEyeCircleOffsetY = 0.0f
    }

    private fun createLeftEyeBall(arcBounds: RectF, offsetY: Float): RectF {
        //the center of the left eye
        val leftEyeCenterX = arcBounds.centerX() - mEyeInterval / 2.0f - mEyeCircleRadius
        val leftEyeCenterY = arcBounds.centerY() - mEyeBallOffsetY + offsetY

        return RectF(leftEyeCenterX - mEyeBallWidth / 2.0f, leftEyeCenterY - mEyeBallHeight / 2.0f,
                leftEyeCenterX + mEyeBallWidth / 2.0f, leftEyeCenterY + mEyeBallHeight / 2.0f)
    }

    private fun createRightEyeBall(arcBounds: RectF, offsetY: Float): RectF {
        //the center of the right eye
        val rightEyeCenterX = arcBounds.centerX() + mEyeInterval / 2.0f + mEyeCircleRadius
        val rightEyeCenterY = arcBounds.centerY() - mEyeBallOffsetY + offsetY

        return RectF(rightEyeCenterX - mEyeBallWidth / 2.0f, rightEyeCenterY - mEyeBallHeight / 2.0f,
                rightEyeCenterX + mEyeBallWidth / 2.0f, rightEyeCenterY + mEyeBallHeight / 2.0f)
    }


    private fun createLeftEyeCircle(arcBounds: RectF, offsetY: Float): Path {
        val path = Path()

        //the center of the left eye
        val leftEyeCenterX = arcBounds.centerX() - mEyeInterval / 2.0f - mEyeCircleRadius
        val leftEyeCenterY = arcBounds.centerY() + offsetY
        //the bounds of left eye
        val leftEyeBounds = RectF(leftEyeCenterX - mEyeCircleRadius, leftEyeCenterY - mEyeCircleRadius,
                leftEyeCenterX + mEyeCircleRadius, leftEyeCenterY + mEyeCircleRadius)
        path.addArc(leftEyeBounds, 0f, (DEGREE_180 + 15).toFloat())
        //the above radian of of the eye
        path.quadTo(leftEyeBounds.left + mAboveRadianEyeOffsetX, leftEyeBounds.top + mEyeCircleRadius * 0.2f,
                leftEyeBounds.left + mAboveRadianEyeOffsetX / 4.0f, leftEyeBounds.top - mEyeCircleRadius * 0.15f)

        return path
    }

    private fun createRightEyeCircle(arcBounds: RectF, offsetY: Float): Path {
        val path = Path()

        //the center of the right eye
        val rightEyeCenterX = arcBounds.centerX() + mEyeInterval / 2.0f + mEyeCircleRadius
        val rightEyeCenterY = arcBounds.centerY() + offsetY
        //the bounds of left eye
        val leftEyeBounds = RectF(rightEyeCenterX - mEyeCircleRadius, rightEyeCenterY - mEyeCircleRadius,
                rightEyeCenterX + mEyeCircleRadius, rightEyeCenterY + mEyeCircleRadius)
        path.addArc(leftEyeBounds, 180f, (-(DEGREE_180 + 15)).toFloat())
        //the above radian of of the eye
        path.quadTo(leftEyeBounds.right - mAboveRadianEyeOffsetX, leftEyeBounds.top + mEyeCircleRadius * 0.2f,
                leftEyeBounds.right - mAboveRadianEyeOffsetX / 4.0f, leftEyeBounds.top - mEyeCircleRadius * 0.15f)

        return path
    }

    private inner class EyeCircleInterpolator : Interpolator {

        override fun getInterpolation(input: Float): Float {
            return if (input < 0.25f) {
                input * 4.0f
            } else if (input < 0.5f) {
                1.0f - (input - 0.25f) * 4.0f
            } else if (input < 0.75f) {
                (input - 0.5f) * 2.0f
            } else {
                0.5f - (input - 0.75f) * 2.0f
            }

        }
    }

    private inner class EyeBallInterpolator : Interpolator {

        override fun getInterpolation(input: Float): Float {
            return if (input < 0.333333f) {
                input * 3.0f
            } else {
                1.0f - (input - 0.333333f) * 1.5f
            }
        }
    }

    class Builder(private val mContext: Context) {

        fun build(): GhostsEyeLoadingRenderer {
            return GhostsEyeLoadingRenderer(mContext)
        }
    }

    companion object {

        private val DEFAULT_WIDTH = 200.0f
        private val DEFAULT_HEIGHT = 176.0f
        private val DEFAULT_EYE_EDGE_WIDTH = 5.0f

        private val DEFAULT_EYE_BALL_HEIGHT = 9.0f
        private val DEFAULT_EYE_BALL_WIDTH = 11.0f

        private val DEFAULT_EYE_CIRCLE_INTERVAL = 8.0f
        private val DEFAULT_EYE_BALL_OFFSET_Y = 2.0f
        private val DEFAULT_ABOVE_RADIAN_EYE_CIRCLE_OFFSET = 6.0f
        private val DEFAULT_EYE_CIRCLE_RADIUS = 21.0f
        private val DEFAULT_MAX_EYE_JUMP_DISTANCE = 11.0f

        private val `LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET` = 0.0f
        private val `RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET` = 0.067f

        private val LEFT_EYE_BALL_END_JUMP_OFFSET = 0.4f
        private val LEFT_EYE_CIRCLE_END_JUMP_OFFSET = 0.533f
        private val RIGHT_EYE_BALL_END_JUMP_OFFSET = 0.467f
        private val RIGHT_EYE_CIRCLE_END_JUMP_OFFSET = 0.60f

        private val DEGREE_180 = 180

        private val ANIMATION_DURATION: Long = 2333

        private val DEFAULT_COLOR = Color.parseColor("#ff484852")
    }
}

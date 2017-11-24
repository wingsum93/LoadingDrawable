package app.dinus.com.loadingdrawable.render.goods

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.DisplayMetrics
import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class BalloonLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

    private val mPaint = Paint()
    private val mCurrentBounds = RectF()
    private val mGasTubeBounds = RectF()
    private val mPipeBodyBounds = RectF()
    private val mCannulaBounds = RectF()
    private val mBalloonBounds = RectF()

    private val mProgressBounds = Rect()

    private var mTextSize: Float = 0.toFloat()
    private var mProgress: Float = 0.toFloat()

    private var mProgressText: String? = null

    private var mGasTubeWidth: Float = 0.toFloat()
    private var mGasTubeHeight: Float = 0.toFloat()
    private var mCannulaWidth: Float = 0.toFloat()
    private var mCannulaHeight: Float = 0.toFloat()
    private var mCannulaMaxOffsetY: Float = 0.toFloat()
    private var mCannulaOffsetY: Float = 0.toFloat()
    private var mPipeBodyWidth: Float = 0.toFloat()
    private var mPipeBodyHeight: Float = 0.toFloat()
    private var mBalloonWidth: Float = 0.toFloat()
    private var mBalloonHeight: Float = 0.toFloat()
    private var mRectCornerRadius: Float = 0.toFloat()
    private var mStrokeWidth: Float = 0.toFloat()

    private var mBalloonColor: Int = 0
    private var mGasTubeColor: Int = 0
    private var mCannulaColor: Int = 0
    private var mPipeBodyColor: Int = 0

    init {
        init(context)
        setupPaint()
    }

    private fun init(context: Context) {
        mTextSize = DensityUtil.dip2px(context, DEFAULT_TEXT_SIZE)

        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH)
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT)
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)

        mGasTubeWidth = DensityUtil.dip2px(context, DEFAULT_GAS_TUBE_WIDTH)
        mGasTubeHeight = DensityUtil.dip2px(context, DEFAULT_GAS_TUBE_HEIGHT)
        mCannulaWidth = DensityUtil.dip2px(context, DEFAULT_CANNULA_WIDTH)
        mCannulaHeight = DensityUtil.dip2px(context, DEFAULT_CANNULA_HEIGHT)
        mCannulaOffsetY = DensityUtil.dip2px(context, DEFAULT_CANNULA_OFFSET_Y)
        mCannulaMaxOffsetY = DensityUtil.dip2px(context, DEFAULT_CANNULA_MAX_OFFSET_Y)
        mPipeBodyWidth = DensityUtil.dip2px(context, DEFAULT_PIPE_BODY_WIDTH)
        mPipeBodyHeight = DensityUtil.dip2px(context, DEFAULT_PIPE_BODY_HEIGHT)
        mBalloonWidth = DensityUtil.dip2px(context, DEFAULT_BALLOON_WIDTH)
        mBalloonHeight = DensityUtil.dip2px(context, DEFAULT_BALLOON_HEIGHT)
        mRectCornerRadius = DensityUtil.dip2px(context, DEFAULT_RECT_CORNER_RADIUS)

        mBalloonColor = DEFAULT_BALLOON_COLOR
        mGasTubeColor = DEFAULT_GAS_TUBE_COLOR
        mCannulaColor = DEFAULT_CANNULA_COLOR
        mPipeBodyColor = DEFAULT_PIPE_BODY_COLOR

        mProgressText = 10.toString() + PERCENT_SIGN

        mDuration = ANIMATION_DURATION
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
    }

    override fun draw(canvas: Canvas, bounds: Rect) {
        val saveCount = canvas.save()

        val arcBounds = mCurrentBounds
        arcBounds.set(bounds)

        //draw draw gas tube
        mPaint.color = mGasTubeColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mStrokeWidth
        canvas.drawPath(createGasTubePath(mGasTubeBounds), mPaint)

        //draw balloon
        mPaint.color = mBalloonColor
        mPaint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawPath(createBalloonPath(mBalloonBounds, mProgress), mPaint)

        //draw progress
        mPaint.color = mGasTubeColor
        mPaint.textSize = mTextSize
        mPaint.strokeWidth = mStrokeWidth / 5.0f
        canvas.drawText(mProgressText!!, arcBounds.centerX() - mProgressBounds.width() / 2.0f,
                mGasTubeBounds.centerY() + mProgressBounds.height() / 2.0f, mPaint)

        //draw cannula
        mPaint.color = mCannulaColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mStrokeWidth
        canvas.drawPath(createCannulaHeadPath(mCannulaBounds), mPaint)
        mPaint.style = Paint.Style.FILL
        canvas.drawPath(createCannulaBottomPath(mCannulaBounds), mPaint)

        //draw pipe body
        mPaint.color = mPipeBodyColor
        mPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(mPipeBodyBounds, mRectCornerRadius, mRectCornerRadius, mPaint)

        canvas.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        val arcBounds = mCurrentBounds
        //compute gas tube bounds
        mGasTubeBounds.set(arcBounds.centerX() - mGasTubeWidth / 2.0f, arcBounds.centerY(),
                arcBounds.centerX() + mGasTubeWidth / 2.0f, arcBounds.centerY() + mGasTubeHeight)
        //compute pipe body bounds
        mPipeBodyBounds.set(arcBounds.centerX() + mGasTubeWidth / 2.0f - mPipeBodyWidth / 2.0f, arcBounds.centerY() - mPipeBodyHeight,
                arcBounds.centerX() + mGasTubeWidth / 2.0f + mPipeBodyWidth / 2.0f, arcBounds.centerY())
        //compute cannula bounds
        mCannulaBounds.set(arcBounds.centerX() + mGasTubeWidth / 2.0f - mCannulaWidth / 2.0f, arcBounds.centerY() - mCannulaHeight - mCannulaOffsetY,
                arcBounds.centerX() + mGasTubeWidth / 2.0f + mCannulaWidth / 2.0f, arcBounds.centerY() - mCannulaOffsetY)
        //compute balloon bounds
        val insetX = mBalloonWidth * 0.333f * (1 - mProgress)
        val insetY = mBalloonHeight * 0.667f * (1 - mProgress)
        mBalloonBounds.set(arcBounds.centerX() - mGasTubeWidth / 2.0f - mBalloonWidth / 2.0f + insetX, arcBounds.centerY() - mBalloonHeight + insetY,
                arcBounds.centerX() - mGasTubeWidth / 2.0f + mBalloonWidth / 2.0f - insetX, arcBounds.centerY())

        if (renderProgress <= START_INHALE_DURATION_OFFSET) {
            mCannulaBounds.offset(0f, -mCannulaMaxOffsetY * renderProgress / START_INHALE_DURATION_OFFSET)

            mProgress = 0.0f
            mProgressText = 10.toString() + PERCENT_SIGN

            mPaint.textSize = mTextSize
            mPaint.getTextBounds(mProgressText, 0, mProgressText!!.length, mProgressBounds)
        } else {
            val exhaleProgress = ACCELERATE_INTERPOLATOR.getInterpolation(1.0f - (renderProgress - START_INHALE_DURATION_OFFSET) / (1.0f - START_INHALE_DURATION_OFFSET))
            mCannulaBounds.offset(0f, -mCannulaMaxOffsetY * exhaleProgress)

            mProgress = 1.0f - exhaleProgress
            mProgressText = adjustProgress((exhaleProgress * 100.0f).toInt()).toString() + PERCENT_SIGN

            mPaint.textSize = mTextSize
            mPaint.getTextBounds(mProgressText, 0, mProgressText!!.length, mProgressBounds)
        }
    }

    private fun adjustProgress(progress: Int): Int {
        var progress = progress
        progress = progress / 10 * 10
        progress = 100 - progress + 10
        if (progress > 100) {
            progress = 100
        }

        return progress
    }

    private fun createGasTubePath(gasTubeRect: RectF): Path {
        val path = Path()
        path.moveTo(gasTubeRect.left, gasTubeRect.top)
        path.lineTo(gasTubeRect.left, gasTubeRect.bottom)
        path.lineTo(gasTubeRect.right, gasTubeRect.bottom)
        path.lineTo(gasTubeRect.right, gasTubeRect.top)
        return path
    }

    private fun createCannulaHeadPath(cannulaRect: RectF): Path {
        val path = Path()
        path.moveTo(cannulaRect.left, cannulaRect.top)
        path.lineTo(cannulaRect.right, cannulaRect.top)
        path.moveTo(cannulaRect.centerX(), cannulaRect.top)
        path.lineTo(cannulaRect.centerX(), cannulaRect.bottom - 0.833f * cannulaRect.width())
        return path
    }

    private fun createCannulaBottomPath(cannulaRect: RectF): Path {
        val cannulaHeadRect = RectF(cannulaRect.left, cannulaRect.bottom - 0.833f * cannulaRect.width(),
                cannulaRect.right, cannulaRect.bottom)

        val path = Path()
        path.addRoundRect(cannulaHeadRect, mRectCornerRadius, mRectCornerRadius, Path.Direction.CCW)
        return path
    }

    /**
     * Coordinates are approximate, you have better cooperate with the designer's design draft
     */
    private fun createBalloonPath(balloonRect: RectF, progress: Float): Path {

        val path = Path()
        path.moveTo(balloonRect.centerX(), balloonRect.bottom)

        val progressWidth = balloonRect.width() * progress
        val progressHeight = balloonRect.height() * progress
        //draw left half
        val leftIncrementX1 = progressWidth * -0.48f
        val leftIncrementY1 = progressHeight * 0.75f
        val leftIncrementX2 = progressWidth * -0.03f
        val leftIncrementY2 = progressHeight * -1.6f
        val leftIncrementX3 = progressWidth * 0.9f
        val leftIncrementY3 = progressHeight * -1.0f

        path.cubicTo(balloonRect.left + balloonRect.width() * 0.25f + leftIncrementX1, balloonRect.centerY() - balloonRect.height() * 0.4f + leftIncrementY1,
                balloonRect.left - balloonRect.width() * 0.20f + leftIncrementX2, balloonRect.centerY() + balloonRect.height() * 1.15f + leftIncrementY2,
                balloonRect.left - balloonRect.width() * 0.4f + leftIncrementX3, balloonRect.bottom + leftIncrementY3)

        //        the results of the left final transformation
        //        path.cubicTo(balloonRect.left - balloonRect.width() * 0.13f, balloonRect.centerY() + balloonRect.height() * 0.35f,
        //                balloonRect.left - balloonRect.width() * 0.23f, balloonRect.centerY() - balloonRect.height() * 0.45f,
        //                balloonRect.left + balloonRect.width() * 0.5f, balloonRect.bottom － balloonRect.height());

        //draw right half
        val rightIncrementX1 = progressWidth * 1.51f
        val rightIncrementY1 = progressHeight * -0.05f
        val rightIncrementX2 = progressWidth * 0.03f
        val rightIncrementY2 = progressHeight * 0.5f
        val rightIncrementX3 = 0.0f
        val rightIncrementY3 = 0.0f

        path.cubicTo(balloonRect.left - balloonRect.width() * 0.38f + rightIncrementX1, balloonRect.centerY() - balloonRect.height() * 0.4f + rightIncrementY1,
                balloonRect.left + balloonRect.width() * 1.1f + rightIncrementX2, balloonRect.centerY() - balloonRect.height() * 0.15f + rightIncrementY2,
                balloonRect.left + balloonRect.width() * 0.5f + rightIncrementX3, balloonRect.bottom + rightIncrementY3)

        //        the results of the right final transformation
        //        path.cubicTo(balloonRect.left + balloonRect.width() * 1.23f, balloonRect.centerY() - balloonRect.height() * 0.45f,
        //                balloonRect.left + balloonRect.width() * 1.13f, balloonRect.centerY() + balloonRect.height() * 0.35f,
        //                balloonRect.left + balloonRect.width() * 0.5f, balloonRect.bottom);

        return path
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha

    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun reset() {}

    class Builder(private val mContext: Context) {

        fun build(): BalloonLoadingRenderer {
            return BalloonLoadingRenderer(mContext)
        }
    }

    companion object {
        private val PERCENT_SIGN = "%"

        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()

        private val START_INHALE_DURATION_OFFSET = 0.4f

        private val DEFAULT_WIDTH = 200.0f
        private val DEFAULT_HEIGHT = 150.0f
        private val DEFAULT_STROKE_WIDTH = 2.0f
        private val DEFAULT_GAS_TUBE_WIDTH = 48f
        private val DEFAULT_GAS_TUBE_HEIGHT = 20f
        private val DEFAULT_CANNULA_WIDTH = 13f
        private val DEFAULT_CANNULA_HEIGHT = 37f
        private val DEFAULT_CANNULA_OFFSET_Y = 3f
        private val DEFAULT_CANNULA_MAX_OFFSET_Y = 15f
        private val DEFAULT_PIPE_BODY_WIDTH = 16f
        private val DEFAULT_PIPE_BODY_HEIGHT = 36f
        private val DEFAULT_BALLOON_WIDTH = 38f
        private val DEFAULT_BALLOON_HEIGHT = 48f
        private val DEFAULT_RECT_CORNER_RADIUS = 2f

        private val DEFAULT_BALLOON_COLOR = Color.parseColor("#ffF3C211")
        private val DEFAULT_GAS_TUBE_COLOR = Color.parseColor("#ff174469")
        private val DEFAULT_PIPE_BODY_COLOR = Color.parseColor("#aa2369B1")
        private val DEFAULT_CANNULA_COLOR = Color.parseColor("#ff174469")

        private val DEFAULT_TEXT_SIZE = 7.0f

        private val ANIMATION_DURATION: Long = 3333
    }
}
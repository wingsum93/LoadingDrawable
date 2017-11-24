package app.dinus.com.loadingdrawable.render.animal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.util.DisplayMetrics
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class FishLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {
    private val FISH_INTERPOLATOR = FishInterpolator()

    private val FISH_MOVE_POINTS = floatArrayOf(DOTTED_LINE_WIDTH_RATE * 3.0f, DOTTED_LINE_WIDTH_RATE * 6.0f, DOTTED_LINE_WIDTH_RATE * 15f, DOTTED_LINE_WIDTH_RATE * 18f, DOTTED_LINE_WIDTH_RATE * 27.0f, DOTTED_LINE_WIDTH_RATE * 30.0f, DOTTED_LINE_WIDTH_RATE * 39f, DOTTED_LINE_WIDTH_RATE * 42f)

    private val FISH_MOVE_POINTS_RATE = 1.0f / FISH_MOVE_POINTS.size

    private val mPaint = Paint()
    private val mTempBounds = RectF()

    private val mFishHeadPos = FloatArray(2)

    private var mRiverPath: Path? = null
    private var mRiverMeasure: PathMeasure? = null

    private var mFishRotateDegrees: Float = 0.toFloat()

    private var mRiverBankWidth: Float = 0.toFloat()
    private var mRiverWidth: Float = 0.toFloat()
    private var mRiverHeight: Float = 0.toFloat()
    private var mFishWidth: Float = 0.toFloat()
    private var mFishHeight: Float = 0.toFloat()
    private var mFishEyeSize: Float = 0.toFloat()
    private var mPathFullLineSize: Float = 0.toFloat()
    private var mPathDottedLineSize: Float = 0.toFloat()

    private var mColor: Int = 0

    init {
        init(context)
        setupPaint()
    }

    private fun init(context: Context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH)
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT)
        mRiverBankWidth = DensityUtil.dip2px(context, DEFAULT_RIVER_BANK_WIDTH)

        mPathFullLineSize = DensityUtil.dip2px(context, DEFAULT_PATH_FULL_LINE_SIZE)
        mPathDottedLineSize = DensityUtil.dip2px(context, DEFAULT_PATH_DOTTED_LINE_SIZE)
        mFishWidth = DensityUtil.dip2px(context, DEFAULT_FISH_WIDTH)
        mFishHeight = DensityUtil.dip2px(context, DEFAULT_FISH_HEIGHT)
        mFishEyeSize = DensityUtil.dip2px(context, DEFAULT_FISH_EYE_SIZE)
        mRiverWidth = DensityUtil.dip2px(context, DEFAULT_RIVER_WIDTH)
        mRiverHeight = DensityUtil.dip2px(context, DEFAULT_RIVER_HEIGHT)

        mColor = DEFAULT_COLOR

        mDuration = ANIMATION_DURATION
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mRiverBankWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.MITER
        mPaint.pathEffect = DashPathEffect(floatArrayOf(mPathFullLineSize, mPathDottedLineSize), mPathDottedLineSize)
    }

    override fun draw(canvas: Canvas, bounds: Rect) {
        val saveCount = canvas.save()
        val arcBounds = mTempBounds
        arcBounds.set(bounds)

        mPaint.color = mColor

        //calculate fish clip bounds
        //clip the width of the fish need to increase mPathDottedLineSize * 1.2f
        val fishRectF = RectF(mFishHeadPos[0] - mFishWidth / 2.0f - mPathDottedLineSize * 1.2f, mFishHeadPos[1] - mFishHeight / 2.0f,
                mFishHeadPos[0] + mFishWidth / 2.0f + mPathDottedLineSize * 1.2f, mFishHeadPos[1] + mFishHeight / 2.0f)
        val matrix = Matrix()
        matrix.postRotate(mFishRotateDegrees, fishRectF.centerX(), fishRectF.centerY())
        matrix.mapRect(fishRectF)

        //draw river
        val riverSaveCount = canvas.save()
        mPaint.style = Paint.Style.STROKE
        canvas.clipRect(fishRectF, Region.Op.DIFFERENCE)
        canvas.drawPath(createRiverPath(arcBounds), mPaint)
        canvas.restoreToCount(riverSaveCount)

        //draw fish
        val fishSaveCount = canvas.save()
        mPaint.style = Paint.Style.FILL
        canvas.rotate(mFishRotateDegrees, mFishHeadPos[0], mFishHeadPos[1])
        canvas.clipPath(createFishEyePath(mFishHeadPos[0], mFishHeadPos[1] - mFishHeight * 0.06f), Region.Op.DIFFERENCE)
        canvas.drawPath(createFishPath(mFishHeadPos[0], mFishHeadPos[1]), mPaint)
        canvas.restoreToCount(fishSaveCount)

        canvas.restoreToCount(saveCount)
    }

    private fun calculateRotateDegrees(fishProgress: Float): Float {
        if (fishProgress < FISH_MOVE_POINTS_RATE * 2) {
            return 90f
        }

        if (fishProgress < FISH_MOVE_POINTS_RATE * 4) {
            return 180f
        }

        return if (fishProgress < FISH_MOVE_POINTS_RATE * 6) {
            270f
        } else 0.0f

    }

    override fun computeRender(renderProgress: Float) {
        if (mRiverPath == null) {
            return
        }

        if (mRiverMeasure == null) {
            mRiverMeasure = PathMeasure(mRiverPath, false)
        }

        val fishProgress = FISH_INTERPOLATOR.getInterpolation(renderProgress)

        mRiverMeasure!!.getPosTan(mRiverMeasure!!.length * fishProgress, mFishHeadPos, null)
        mFishRotateDegrees = calculateRotateDegrees(fishProgress)
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(cf: ColorFilter?) {

    }

    override fun reset() {}

    private fun createFishEyePath(fishEyeCenterX: Float, fishEyeCenterY: Float): Path {
        val path = Path()
        path.addCircle(fishEyeCenterX, fishEyeCenterY, mFishEyeSize, Path.Direction.CW)

        return path
    }

    private fun createFishPath(fishCenterX: Float, fishCenterY: Float): Path {
        val path = Path()

        val fishHeadY = fishCenterY - mFishHeight / 2.0f

        //the head of the fish
        path.moveTo(fishCenterX, fishHeadY)
        //the left body of the fish
        path.quadTo(fishCenterX - mFishWidth * 0.333f, fishHeadY + mFishHeight * 0.222f, fishCenterX - mFishWidth * 0.333f, fishHeadY + mFishHeight * 0.444f)
        path.lineTo(fishCenterX - mFishWidth * 0.333f, fishHeadY + mFishHeight * 0.666f)
        path.lineTo(fishCenterX - mFishWidth * 0.5f, fishHeadY + mFishHeight * 0.8f)
        path.lineTo(fishCenterX - mFishWidth * 0.5f, fishHeadY + mFishHeight)

        //the tail of the fish
        path.lineTo(fishCenterX, fishHeadY + mFishHeight * 0.9f)

        //the right body of the fish
        path.lineTo(fishCenterX + mFishWidth * 0.5f, fishHeadY + mFishHeight)
        path.lineTo(fishCenterX + mFishWidth * 0.5f, fishHeadY + mFishHeight * 0.8f)
        path.lineTo(fishCenterX + mFishWidth * 0.333f, fishHeadY + mFishHeight * 0.666f)
        path.lineTo(fishCenterX + mFishWidth * 0.333f, fishHeadY + mFishHeight * 0.444f)
        path.quadTo(fishCenterX + mFishWidth * 0.333f, fishHeadY + mFishHeight * 0.222f, fishCenterX, fishHeadY)

        path.close()

        return path
    }

    private fun createRiverPath(arcBounds: RectF): Path? {
        if (mRiverPath != null) {
            return mRiverPath
        }

        mRiverPath = Path()

        val rectF = RectF(arcBounds.centerX() - mRiverWidth / 2.0f, arcBounds.centerY() - mRiverHeight / 2.0f,
                arcBounds.centerX() + mRiverWidth / 2.0f, arcBounds.centerY() + mRiverHeight / 2.0f)

        rectF.inset(mRiverBankWidth / 2.0f, mRiverBankWidth / 2.0f)

        mRiverPath!!.addRect(rectF, Path.Direction.CW)

        return mRiverPath
    }

    private inner class FishInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            var index = (input / FISH_MOVE_POINTS_RATE).toInt()
            if (index >= FISH_MOVE_POINTS.size) {
                index = FISH_MOVE_POINTS.size - 1
            }

            return FISH_MOVE_POINTS[index]
        }
    }

    class Builder(private val mContext: Context) {

        fun build(): FishLoadingRenderer {
            return FishLoadingRenderer(mContext)
        }
    }

    companion object {

        private val DEFAULT_PATH_FULL_LINE_SIZE = 7.0f
        private val DEFAULT_PATH_DOTTED_LINE_SIZE = DEFAULT_PATH_FULL_LINE_SIZE / 2.0f
        private val DEFAULT_RIVER_HEIGHT = DEFAULT_PATH_FULL_LINE_SIZE * 8.5f
        private val DEFAULT_RIVER_WIDTH = DEFAULT_PATH_FULL_LINE_SIZE * 5.5f

        private val DEFAULT_FISH_EYE_SIZE = DEFAULT_PATH_FULL_LINE_SIZE * 0.5f
        private val DEFAULT_FISH_WIDTH = DEFAULT_PATH_FULL_LINE_SIZE * 3.0f
        private val DEFAULT_FISH_HEIGHT = DEFAULT_PATH_FULL_LINE_SIZE * 4.5f

        private val DEFAULT_WIDTH = 200.0f
        private val DEFAULT_HEIGHT = 150.0f
        private val DEFAULT_RIVER_BANK_WIDTH = DEFAULT_PATH_FULL_LINE_SIZE

        private val ANIMATION_DURATION: Long = 800
        private val DOTTED_LINE_WIDTH_COUNT = (8.5f + 5.5f - 2.0f) * 2.0f * 2.0f
        private val DOTTED_LINE_WIDTH_RATE = 1.0f / DOTTED_LINE_WIDTH_COUNT

        private val DEFAULT_COLOR = Color.parseColor("#fffefed6")
    }
}

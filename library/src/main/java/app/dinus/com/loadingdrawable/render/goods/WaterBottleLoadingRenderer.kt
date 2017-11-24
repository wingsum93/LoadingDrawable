package app.dinus.com.loadingdrawable.render.goods

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.DisplayMetrics
import android.view.animation.Interpolator

import java.util.ArrayList
import java.util.Random

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class WaterBottleLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

    private val mRandom = Random()

    private val mPaint = Paint()
    private val mCurrentBounds = RectF()
    private val mBottleBounds = RectF()
    private val mWaterBounds = RectF()
    private val mLoadingBounds = Rect()
    private val mWaterDropHolders = ArrayList<WaterDropHolder>()

    private var mTextSize: Float = 0.toFloat()
    private var mProgress: Float = 0.toFloat()

    private var mBottleWidth: Float = 0.toFloat()
    private var mBottleHeight: Float = 0.toFloat()
    private var mStrokeWidth: Float = 0.toFloat()
    private var mWaterLowestPointToBottleneckDistance: Float = 0.toFloat()

    private var mBottleColor: Int = 0
    private var mWaterColor: Int = 0

    private var mWaveCount: Int = 0

    init {
        init(context)
        setupPaint()
    }

    private fun init(context: Context) {
        mTextSize = DensityUtil.dip2px(context, DEFAULT_TEXT_SIZE)

        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH)
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT)
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)

        mBottleWidth = DensityUtil.dip2px(context, DEFAULT_BOTTLE_WIDTH)
        mBottleHeight = DensityUtil.dip2px(context, DEFAULT_BOTTLE_HEIGHT)
        mWaterLowestPointToBottleneckDistance = DensityUtil.dip2px(context, WATER_LOWEST_POINT_TO_BOTTLENECK_DISTANCE)

        mBottleColor = DEFAULT_BOTTLE_COLOR
        mWaterColor = DEFAULT_WATER_COLOR

        mWaveCount = DEFAULT_WAVE_COUNT

        mDuration = ANIMATION_DURATION
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
        mPaint.strokeJoin = Paint.Join.ROUND
    }

    override fun draw(canvas: Canvas, bounds: Rect) {
        val saveCount = canvas.save()

        val arcBounds = mCurrentBounds
        arcBounds.set(bounds)
        //draw bottle
        mPaint.style = Paint.Style.STROKE
        mPaint.color = mBottleColor
        canvas.drawPath(createBottlePath(mBottleBounds), mPaint)

        //draw water
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.color = mWaterColor
        canvas.drawPath(createWaterPath(mWaterBounds, mProgress), mPaint)

        //draw water drop
        mPaint.style = Paint.Style.FILL
        mPaint.color = mWaterColor
        for (waterDropHolder in mWaterDropHolders) {
            if (waterDropHolder.mNeedDraw) {
                canvas.drawCircle(waterDropHolder.mInitX, waterDropHolder.mCurrentY, waterDropHolder.mRadius, mPaint)
            }
        }

        //draw loading text
        mPaint.color = mBottleColor
        canvas.drawText(LOADING_TEXT, mBottleBounds.centerX() - mLoadingBounds.width() / 2.0f,
                mBottleBounds.bottom + mBottleBounds.height() * 0.2f, mPaint)
        canvas.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        if (mCurrentBounds.width() <= 0) {
            return
        }

        val arcBounds = mCurrentBounds
        //compute gas tube bounds
        mBottleBounds.set(arcBounds.centerX() - mBottleWidth / 2.0f, arcBounds.centerY() - mBottleHeight / 2.0f,
                arcBounds.centerX() + mBottleWidth / 2.0f, arcBounds.centerY() + mBottleHeight / 2.0f)
        //compute pipe body bounds
        mWaterBounds.set(mBottleBounds.left + mStrokeWidth * 1.5f, mBottleBounds.top + mWaterLowestPointToBottleneckDistance,
                mBottleBounds.right - mStrokeWidth * 1.5f, mBottleBounds.bottom - mStrokeWidth * 1.5f)

        //compute wave progress
        val totalWaveProgress = renderProgress * mWaveCount
        val currentWaveProgress = totalWaveProgress - totalWaveProgress.toInt()

        if (currentWaveProgress > 0.5f) {
            mProgress = 1.0f - MATERIAL_INTERPOLATOR.getInterpolation((currentWaveProgress - 0.5f) * 2.0f)
        } else {
            mProgress = MATERIAL_INTERPOLATOR.getInterpolation(currentWaveProgress * 2.0f)
        }

        //init water drop holders
        if (mWaterDropHolders.isEmpty()) {
            initWaterDropHolders(mBottleBounds, mWaterBounds)
        }

        //compute the location of these water drops
        for (waterDropHolder in mWaterDropHolders) {
            if (waterDropHolder.mDelayDuration < renderProgress && waterDropHolder.mDelayDuration + waterDropHolder.mDuration > renderProgress) {
                var riseProgress = (renderProgress - waterDropHolder.mDelayDuration) / waterDropHolder.mDuration
                riseProgress = if (riseProgress < 0.5f) riseProgress * 2.0f else 1.0f - (riseProgress - 0.5f) * 2.0f
                waterDropHolder.mCurrentY = waterDropHolder.mInitY - MATERIAL_INTERPOLATOR.getInterpolation(riseProgress) * waterDropHolder.mRiseHeight
                waterDropHolder.mNeedDraw = true
            } else {
                waterDropHolder.mNeedDraw = false
            }
        }

        //measure loading text
        mPaint.textSize = mTextSize
        mPaint.getTextBounds(LOADING_TEXT, 0, LOADING_TEXT.length, mLoadingBounds)
    }

    private fun createBottlePath(bottleRect: RectF): Path {
        val bottleneckWidth = bottleRect.width() * 0.3f
        val bottleneckHeight = bottleRect.height() * 0.415f
        val bottleneckDecorationWidth = bottleneckWidth * 1.1f
        val bottleneckDecorationHeight = bottleneckHeight * 0.167f

        val path = Path()
        //draw the left side of the bottleneck decoration
        path.moveTo(bottleRect.centerX() - bottleneckDecorationWidth * 0.5f, bottleRect.top)
        path.quadTo(bottleRect.centerX() - bottleneckDecorationWidth * 0.5f - bottleneckWidth * 0.15f, bottleRect.top + bottleneckDecorationHeight * 0.5f,
                bottleRect.centerX() - bottleneckWidth * 0.5f, bottleRect.top + bottleneckDecorationHeight)
        path.lineTo(bottleRect.centerX() - bottleneckWidth * 0.5f, bottleRect.top + bottleneckHeight)

        //draw the left side of the bottle's body
        val radius = (bottleRect.width() - mStrokeWidth) / 2.0f
        val centerY = bottleRect.bottom - 0.86f * radius
        val bodyRect = RectF(bottleRect.left, centerY - radius, bottleRect.right, centerY + radius)
        path.addArc(bodyRect, 255f, -135f)

        //draw the bottom of the bottle
        val bottleBottomWidth = bottleRect.width() / 2.0f
        path.lineTo(bottleRect.centerX() - bottleBottomWidth / 2.0f, bottleRect.bottom)
        path.lineTo(bottleRect.centerX() + bottleBottomWidth / 2.0f, bottleRect.bottom)

        //draw the right side of the bottle's body
        path.addArc(bodyRect, 60f, -135f)

        //draw the right side of the bottleneck decoration
        path.lineTo(bottleRect.centerX() + bottleneckWidth * 0.5f, bottleRect.top + bottleneckDecorationHeight)
        path.quadTo(bottleRect.centerX() + bottleneckDecorationWidth * 0.5f + bottleneckWidth * 0.15f, bottleRect.top + bottleneckDecorationHeight * 0.5f,
                bottleRect.centerX() + bottleneckDecorationWidth * 0.5f, bottleRect.top)

        return path
    }

    private fun createWaterPath(waterRect: RectF, progress: Float): Path {
        val path = Path()

        path.moveTo(waterRect.left, waterRect.top)

        //Similar to the way draw the bottle's bottom sides
        val radius = (waterRect.width() - mStrokeWidth) / 2.0f
        val centerY = waterRect.bottom - 0.86f * radius
        val bottleBottomWidth = waterRect.width() / 2.0f
        val bodyRect = RectF(waterRect.left, centerY - radius, waterRect.right, centerY + radius)

        path.addArc(bodyRect, 187.5f, -67.5f)
        path.lineTo(waterRect.centerX() - bottleBottomWidth / 2.0f, waterRect.bottom)
        path.lineTo(waterRect.centerX() + bottleBottomWidth / 2.0f, waterRect.bottom)
        path.addArc(bodyRect, 60f, -67.5f)

        //draw the water waves
        val cubicXChangeSize = waterRect.width() * 0.35f * progress
        val cubicYChangeSize = waterRect.height() * 1.2f * progress

        path.cubicTo(waterRect.left + waterRect.width() * 0.80f - cubicXChangeSize, waterRect.top - waterRect.height() * 1.2f + cubicYChangeSize,
                waterRect.left + waterRect.width() * 0.55f - cubicXChangeSize, waterRect.top - cubicYChangeSize,
                waterRect.left, waterRect.top - mStrokeWidth / 2.0f)

        path.lineTo(waterRect.left, waterRect.top)

        return path
    }

    private fun initWaterDropHolders(bottleRect: RectF, waterRect: RectF) {
        val bottleRadius = bottleRect.width() / 2.0f
        val lowestWaterPointY = waterRect.top
        val twoSidesInterval = 0.2f * bottleRect.width()
        val atLeastDelayDuration = 0.1f

        val unitDuration = 0.1f
        val delayDurationRange = 0.6f
        val radiusRandomRange = MAX_WATER_DROP_RADIUS - MIN_WATER_DROP_RADIUS
        val currentXRandomRange = bottleRect.width() * 0.6f

        for (i in 0 until DEFAULT_WATER_DROP_COUNT) {
            val waterDropHolder = WaterDropHolder()
            waterDropHolder.mRadius = (MIN_WATER_DROP_RADIUS + mRandom.nextInt(radiusRandomRange)).toFloat()
            waterDropHolder.mInitX = bottleRect.left + twoSidesInterval + mRandom.nextFloat() * currentXRandomRange
            waterDropHolder.mInitY = lowestWaterPointY + waterDropHolder.mRadius / 2.0f
            waterDropHolder.mRiseHeight = getMaxRiseHeight(bottleRadius, waterDropHolder.mRadius, waterDropHolder.mInitX - bottleRect.left) * (0.2f + 0.8f * mRandom.nextFloat())
            waterDropHolder.mDelayDuration = atLeastDelayDuration + mRandom.nextFloat() * delayDurationRange
            waterDropHolder.mDuration = waterDropHolder.mRiseHeight / bottleRadius * unitDuration

            mWaterDropHolders.add(waterDropHolder)
        }
    }

    private fun getMaxRiseHeight(bottleRadius: Float, waterDropRadius: Float, currentX: Float): Float {
        val coordinateX = currentX - bottleRadius
        val bottleneckRadius = bottleRadius * 0.3f
        return if (coordinateX - waterDropRadius > -bottleneckRadius && coordinateX + waterDropRadius < bottleneckRadius) {
            bottleRadius * 2.0f
        } else (Math.sqrt(Math.pow(bottleRadius.toDouble(), 2.0) - Math.pow(coordinateX.toDouble(), 2.0)) - waterDropRadius).toFloat()

    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha

    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf

    }

    override fun reset() {}

    private inner class WaterDropHolder {
        var mCurrentY: Float = 0.toFloat()

        var mInitX: Float = 0.toFloat()
        var mInitY: Float = 0.toFloat()
        var mDelayDuration: Float = 0.toFloat()
        var mRiseHeight: Float = 0.toFloat()

        var mRadius: Float = 0.toFloat()
        var mDuration: Float = 0.toFloat()

        var mNeedDraw: Boolean = false
    }

    class Builder(private val mContext: Context) {

        fun build(): WaterBottleLoadingRenderer {
            return WaterBottleLoadingRenderer(mContext)
        }
    }

    companion object {
        private val MATERIAL_INTERPOLATOR = FastOutSlowInInterpolator()

        private val DEFAULT_WIDTH = 200.0f
        private val DEFAULT_HEIGHT = 150.0f
        private val DEFAULT_STROKE_WIDTH = 1.5f
        private val DEFAULT_BOTTLE_WIDTH = 30f
        private val DEFAULT_BOTTLE_HEIGHT = 43f
        private val WATER_LOWEST_POINT_TO_BOTTLENECK_DISTANCE = 30f

        private val DEFAULT_WAVE_COUNT = 5
        private val DEFAULT_WATER_DROP_COUNT = 25

        private val MAX_WATER_DROP_RADIUS = 5
        private val MIN_WATER_DROP_RADIUS = 1

        private val DEFAULT_BOTTLE_COLOR = Color.parseColor("#FFDAEBEB")
        private val DEFAULT_WATER_COLOR = Color.parseColor("#FF29E3F2")

        private val DEFAULT_TEXT_SIZE = 7.0f

        private val LOADING_TEXT = "loading"

        private val ANIMATION_DURATION: Long = 11111
    }
}
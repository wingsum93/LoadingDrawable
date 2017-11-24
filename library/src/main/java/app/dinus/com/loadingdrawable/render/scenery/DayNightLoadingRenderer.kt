package app.dinus.com.loadingdrawable.render.scenery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.DisplayMetrics
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

import java.util.ArrayList
import java.util.Random

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class DayNightLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

    private val mRandom = Random()
    private val mStarHolders = ArrayList<StarHolder>()

    private val mPaint = Paint()
    private val mTempBounds = RectF()

    private val mAnimatorListener = object : AnimatorListenerAdapter() {
        override fun onAnimationRepeat(animator: Animator) {
            super.onAnimationRepeat(animator)
        }
    }

    private var mCurrentColor: Int = 0

    private var mMaxStarOffsets: Float = 0.toFloat()

    private var mStrokeWidth: Float = 0.toFloat()
    private var mStarRadius: Float = 0.toFloat()
    private var `mSun$MoonRadius`: Float = 0.toFloat()
    private var mSunCoordinateY: Float = 0.toFloat()
    private var mMoonCoordinateY: Float = 0.toFloat()
    //the y-coordinate of the end point of the sun ray
    private var mSunRayEndCoordinateY: Float = 0.toFloat()
    //the y-coordinate of the start point of the sun ray
    private var mSunRayStartCoordinateY: Float = 0.toFloat()
    //the y-coordinate of the start point of the sun
    private var `mInitSun$MoonCoordinateY`: Float = 0.toFloat()
    //the distance from the outside to the center of the drawable
    private var `mMaxSun$MoonRiseDistance`: Float = 0.toFloat()

    private var mSunRayRotation: Float = 0.toFloat()
    private var mMoonRotation: Float = 0.toFloat()

    //the number of sun's rays is increasing
    private var mIsExpandSunRay: Boolean = false
    private var mShowStar: Boolean = false

    private var mSunRayCount: Int = 0

    init {
        init(context)
        setupPaint()
        addRenderListener(mAnimatorListener)
    }

    private fun init(context: Context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH)
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT)
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)

        mStarRadius = DensityUtil.dip2px(context, DEFAULT_STAR_RADIUS)
        `mSun$MoonRadius` = DensityUtil.dip2px(context, `DEFAULT_SUN$MOON_RADIUS`)
        `mInitSun$MoonCoordinateY` = mHeight + `mSun$MoonRadius` + mStrokeWidth * 2.0f
        `mMaxSun$MoonRiseDistance` = mHeight / 2.0f + `mSun$MoonRadius`

        mSunRayStartCoordinateY = (`mInitSun$MoonCoordinateY` - `mMaxSun$MoonRiseDistance` //the center

                - `mSun$MoonRadius` //sub the radius

                - mStrokeWidth // sub the with the sun circle

                - DensityUtil.dip2px(context, DEFAULT_SUN_RAY_OFFSET)) //sub the interval between the sun and the sun ray

        //add strokeWidth * 2.0f because the stroke cap is Paint.Cap.ROUND
        mSunRayEndCoordinateY = mSunRayStartCoordinateY - DensityUtil.dip2px(context, DEFAULT_SUN_RAY_LENGTH) + mStrokeWidth

        mSunCoordinateY = `mInitSun$MoonCoordinateY`
        mMoonCoordinateY = `mInitSun$MoonCoordinateY`

        mCurrentColor = DEFAULT_COLOR

        mDuration = ANIMATION_DURATION
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun draw(canvas: Canvas, bounds: Rect) {
        val saveCount = canvas.save()

        val arcBounds = mTempBounds
        arcBounds.set(bounds)

        mPaint.alpha = MAX_ALPHA
        mPaint.style = Paint.Style.STROKE
        mPaint.color = mCurrentColor

        if (mSunCoordinateY < `mInitSun$MoonCoordinateY`) {
            canvas.drawCircle(arcBounds.centerX(), mSunCoordinateY, `mSun$MoonRadius`, mPaint)
        }

        if (mMoonCoordinateY < `mInitSun$MoonCoordinateY`) {
            val moonSaveCount = canvas.save()
            canvas.rotate(mMoonRotation, arcBounds.centerX(), mMoonCoordinateY)
            canvas.drawPath(createMoonPath(arcBounds.centerX(), mMoonCoordinateY), mPaint)
            canvas.restoreToCount(moonSaveCount)
        }

        for (i in 0 until mSunRayCount) {
            val sunRaySaveCount = canvas.save()
            //rotate 45 degrees can change the direction of 0 degrees to 1:30 clock
            //-mSunRayRotation means reverse rotation
            canvas.rotate(45 - mSunRayRotation + (if (mIsExpandSunRay) i else MAX_SUN_RAY_COUNT - i) * DEGREE_360 / MAX_SUN_RAY_COUNT,
                    arcBounds.centerX(), mSunCoordinateY)

            canvas.drawLine(arcBounds.centerX(), mSunRayStartCoordinateY, arcBounds.centerX(), mSunRayEndCoordinateY, mPaint)
            canvas.restoreToCount(sunRaySaveCount)
        }

        if (mShowStar) {
            if (mStarHolders.isEmpty()) {
                initStarHolders(arcBounds)
            }

            for (i in mStarHolders.indices) {
                mPaint.style = Paint.Style.FILL
                mPaint.alpha = mStarHolders[i].mAlpha
                canvas.drawCircle(mStarHolders[i].mCurrentPoint.x, mStarHolders[i].mCurrentPoint.y, mStarRadius, mPaint)
            }
        }

        canvas.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        if (renderProgress <= SUN_RISE_DURATION_OFFSET) {
            val sunRiseProgress = renderProgress / SUN_RISE_DURATION_OFFSET
            mSunCoordinateY = `mInitSun$MoonCoordinateY` - `mMaxSun$MoonRiseDistance` * MATERIAL_INTERPOLATOR.getInterpolation(sunRiseProgress)
            mMoonCoordinateY = `mInitSun$MoonCoordinateY`
            mShowStar = false
        }

        if (renderProgress <= SUN_ROTATE_DURATION_OFFSET && renderProgress > SUN_RISE_DURATION_OFFSET) {
            val sunRotateProgress = (renderProgress - SUN_RISE_DURATION_OFFSET) / (SUN_ROTATE_DURATION_OFFSET - SUN_RISE_DURATION_OFFSET)
            mSunRayRotation = sunRotateProgress * MAX_SUN_ROTATE_DEGREE

            if ((mSunRayRotation / SUN_RAY_INTERVAL_DEGREE).toInt() <= MAX_SUN_RAY_COUNT) {
                mIsExpandSunRay = true
                mSunRayCount = (mSunRayRotation / SUN_RAY_INTERVAL_DEGREE).toInt()
            }

            if (((MAX_SUN_ROTATE_DEGREE - mSunRayRotation) / SUN_RAY_INTERVAL_DEGREE).toInt() <= MAX_SUN_RAY_COUNT) {
                mIsExpandSunRay = false
                mSunRayCount = ((MAX_SUN_ROTATE_DEGREE - mSunRayRotation) / SUN_RAY_INTERVAL_DEGREE).toInt()
            }
        }

        if (renderProgress <= SUN_DECREASE_DURATION_OFFSET && renderProgress > SUN_ROTATE_DURATION_OFFSET) {
            val sunDecreaseProgress = (renderProgress - SUN_ROTATE_DURATION_OFFSET) / (SUN_DECREASE_DURATION_OFFSET - SUN_ROTATE_DURATION_OFFSET)
            mSunCoordinateY = `mInitSun$MoonCoordinateY` - `mMaxSun$MoonRiseDistance` * (1.0f - ACCELERATE_INTERPOLATOR.getInterpolation(sunDecreaseProgress))
        }

        if (renderProgress <= MOON_RISE_DURATION_OFFSET && renderProgress > SUN_DECREASE_DURATION_OFFSET) {
            val moonRiseProgress = (renderProgress - SUN_DECREASE_DURATION_OFFSET) / (MOON_RISE_DURATION_OFFSET - SUN_DECREASE_DURATION_OFFSET)
            mMoonRotation = MATERIAL_INTERPOLATOR.getInterpolation(moonRiseProgress) * MAX_MOON_ROTATE_DEGREE
            mSunCoordinateY = `mInitSun$MoonCoordinateY`
            mMoonCoordinateY = `mInitSun$MoonCoordinateY` - `mMaxSun$MoonRiseDistance` * MATERIAL_INTERPOLATOR.getInterpolation(moonRiseProgress)
        }

        if (renderProgress <= STAR_DECREASE_START_DURATION_OFFSET && renderProgress > STAR_RISE_START_DURATION_OFFSET) {
            val starProgress = (renderProgress - STAR_RISE_START_DURATION_OFFSET) / (STAR_DECREASE_START_DURATION_OFFSET - STAR_RISE_START_DURATION_OFFSET)
            if (starProgress <= STAR_RISE_PROGRESS_OFFSET) {
                for (i in mStarHolders.indices) {
                    val starHolder = mStarHolders[i]
                    starHolder.mCurrentPoint.y = starHolder.mPoint.y - (1.0f - starHolder.mInterpolator.getInterpolation(starProgress * 5.0f)) * (mMaxStarOffsets * 0.65f)
                    starHolder.mCurrentPoint.x = starHolder.mPoint.x
                }
            }

            if (starProgress > STAR_RISE_PROGRESS_OFFSET && starProgress < STAR_DECREASE_PROGRESS_OFFSET) {
                for (i in mStarHolders.indices) {
                    val starHolder = mStarHolders[i]
                    if (starHolder.mFlashOffset < starProgress && starProgress < starHolder.mFlashOffset + STAR_FLASH_PROGRESS_PERCENTAGE) {
                        starHolder.mAlpha = (MAX_ALPHA * MATERIAL_INTERPOLATOR.getInterpolation(
                                Math.abs(starProgress - (starHolder.mFlashOffset + STAR_FLASH_PROGRESS_PERCENTAGE / 2.0f)) / (STAR_FLASH_PROGRESS_PERCENTAGE / 2.0f))).toInt()
                    }
                }
            }

            if (starProgress >= STAR_DECREASE_PROGRESS_OFFSET) {
                for (i in mStarHolders.indices) {
                    val starHolder = mStarHolders[i]
                    starHolder.mCurrentPoint.y = starHolder.mPoint.y + starHolder.mInterpolator.getInterpolation((starProgress - STAR_DECREASE_PROGRESS_OFFSET) * 5.0f) * mMaxStarOffsets
                    starHolder.mCurrentPoint.x = starHolder.mPoint.x
                }
            }
            mShowStar = true
        }

        if (renderProgress <= MOON_DECREASE_END_DURATION_OFFSET && renderProgress > MOON_DECREASE_START_DURATION_OFFSET) {
            val moonDecreaseProgress = (renderProgress - MOON_DECREASE_START_DURATION_OFFSET) / (MOON_DECREASE_END_DURATION_OFFSET - MOON_DECREASE_START_DURATION_OFFSET)
            mMoonCoordinateY = `mInitSun$MoonCoordinateY` - `mMaxSun$MoonRiseDistance` * (1.0f - ACCELERATE_INTERPOLATOR.getInterpolation(moonDecreaseProgress))
        }
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha

    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf

    }

    override fun reset() {}

    private fun initStarHolders(currentBounds: RectF) {
        mStarHolders.add(StarHolder(0.3f, PointF(currentBounds.left + currentBounds.width() * 0.175f,
                currentBounds.top + currentBounds.height() * 0.0934f)))
        mStarHolders.add(StarHolder(0.2f, PointF(currentBounds.left + currentBounds.width() * 0.175f,
                currentBounds.top + currentBounds.height() * 0.62f)))
        mStarHolders.add(StarHolder(0.2f, PointF(currentBounds.left + currentBounds.width() * 0.2525f,
                currentBounds.top + currentBounds.height() * 0.43f)))
        mStarHolders.add(StarHolder(0.5f, PointF(currentBounds.left + currentBounds.width() * 0.4075f,
                currentBounds.top + currentBounds.height() * 0.0934f)))
        mStarHolders.add(StarHolder(PointF(currentBounds.left + currentBounds.width() * 0.825f,
                currentBounds.top + currentBounds.height() * 0.04f)))
        mStarHolders.add(StarHolder(PointF(currentBounds.left + currentBounds.width() * 0.7075f,
                currentBounds.top + currentBounds.height() * 0.147f)))
        mStarHolders.add(StarHolder(PointF(currentBounds.left + currentBounds.width() * 0.3475f,
                currentBounds.top + currentBounds.height() * 0.2567f)))
        mStarHolders.add(StarHolder(0.6f, PointF(currentBounds.left + currentBounds.width() * 0.5825f,
                currentBounds.top + currentBounds.height() * 0.277f)))
        mStarHolders.add(StarHolder(PointF(currentBounds.left + currentBounds.width() * 0.84f,
                currentBounds.top + currentBounds.height() * 0.32f)))
        mStarHolders.add(StarHolder(PointF(currentBounds.left + currentBounds.width() * 0.8f,
                currentBounds.top + currentBounds.height() / 0.502f)))
        mStarHolders.add(StarHolder(0.6f, PointF(currentBounds.left + currentBounds.width() * 0.7f,
                currentBounds.top + currentBounds.height() * 0.473f)))

        mMaxStarOffsets = currentBounds.height()
    }

    private fun createMoonPath(moonCenterX: Float, moonCenterY: Float): Path {
        val moonRectF = RectF(moonCenterX - `mSun$MoonRadius`, moonCenterY - `mSun$MoonRadius`,
                moonCenterX + `mSun$MoonRadius`, moonCenterY + `mSun$MoonRadius`)
        val path = Path()
        path.addArc(moonRectF, -90f, 180f)
        path.quadTo(moonCenterX + `mSun$MoonRadius` / 2.0f, moonCenterY, moonCenterX, moonCenterY - `mSun$MoonRadius`)
        return path
    }

    private inner class StarHolder(val mFlashOffset: Float, val mPoint: PointF) {
        var mAlpha: Int = 0
        var mCurrentPoint: PointF
        val mInterpolator: Interpolator

        constructor(point: PointF) : this(1.0f, point) {}

        init {
            this.mAlpha = MAX_ALPHA
            this.mCurrentPoint = PointF()
            this.mInterpolator = INTERPOLATORS[mRandom.nextInt(INTERPOLATORS.size)]
        }
    }

    class Builder(private val mContext: Context) {

        fun build(): DayNightLoadingRenderer {
            return DayNightLoadingRenderer(mContext)
        }
    }

    companion object {
        private val MATERIAL_INTERPOLATOR = FastOutSlowInInterpolator()
        private val LINEAR_INTERPOLATOR = LinearInterpolator()
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator()
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val FASTOUTLINEARIN_INTERPOLATOR = FastOutLinearInInterpolator()

        private val INTERPOLATORS = arrayOf(LINEAR_INTERPOLATOR, DECELERATE_INTERPOLATOR, ACCELERATE_INTERPOLATOR, FASTOUTLINEARIN_INTERPOLATOR, MATERIAL_INTERPOLATOR)

        private val MAX_ALPHA = 255
        private val DEGREE_360 = 360
        private val MAX_SUN_RAY_COUNT = 12

        private val DEFAULT_WIDTH = 200.0f
        private val DEFAULT_HEIGHT = 150.0f
        private val DEFAULT_STROKE_WIDTH = 2.5f
        private val `DEFAULT_SUN$MOON_RADIUS` = 12.0f
        private val DEFAULT_STAR_RADIUS = 2.5f
        private val DEFAULT_SUN_RAY_LENGTH = 10.0f
        private val DEFAULT_SUN_RAY_OFFSET = 3.0f

        val STAR_RISE_PROGRESS_OFFSET = 0.2f
        val STAR_DECREASE_PROGRESS_OFFSET = 0.8f
        val STAR_FLASH_PROGRESS_PERCENTAGE = 0.2f

        private val MAX_SUN_ROTATE_DEGREE = DEGREE_360 / 3.0f
        private val MAX_MOON_ROTATE_DEGREE = DEGREE_360 / 6.0f
        private val SUN_RAY_INTERVAL_DEGREE = DEGREE_360.toFloat() / 3.0f / 55f

        private val SUN_RISE_DURATION_OFFSET = 0.143f
        private val SUN_ROTATE_DURATION_OFFSET = 0.492f
        private val SUN_DECREASE_DURATION_OFFSET = 0.570f
        private val MOON_RISE_DURATION_OFFSET = 0.713f
        private val MOON_DECREASE_START_DURATION_OFFSET = 0.935f
        private val MOON_DECREASE_END_DURATION_OFFSET = 1.0f
        private val STAR_RISE_START_DURATION_OFFSET = 0.684f
        private val STAR_DECREASE_START_DURATION_OFFSET = 1.0f

        private val DEFAULT_COLOR = Color.parseColor("#ff21fd8e")

        private val ANIMATION_DURATION: Long = 5111
    }
}

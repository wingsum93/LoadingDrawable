package app.dinus.com.loadingdrawable.render.scenery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.support.annotation.IntDef
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.DisplayMetrics
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.ArrayList
import java.util.Random

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.R
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class ElectricFanLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {
    private val mAnimatorListener = object : AnimatorListenerAdapter() {
        override fun onAnimationRepeat(animator: Animator) {
            super.onAnimationRepeat(animator)
            reset()
        }
    }

    private val mPaint = Paint()
    private val mTempBounds = RectF()
    private val mCurrentProgressBounds = RectF()

    private var mTextSize: Float = 0.toFloat()
    private var mStrokeXInset: Float = 0.toFloat()
    private var mStrokeYInset: Float = 0.toFloat()
    private var mProgressCenterRadius: Float = 0.toFloat()

    private var mScale: Float = 0.toFloat()
    private var mRotation: Float = 0.toFloat()
    private var mProgress: Float = 0.toFloat()

    private var mNextLeafCreateThreshold: Float = 0.toFloat()

    private var mProgressColor: Int = 0
    private var mProgressBgColor: Int = 0
    private var mElectricFanBgColor: Int = 0
    private var mElectricFanOutlineColor: Int = 0

    private var mStrokeWidth: Float = 0.toFloat()
    private var mCenterRadius: Float = 0.toFloat()

    @MODE
    private var mMode: Int = 0
    private var mCurrentLeafCount: Int = 0

    private var mLeafDrawable: Drawable? = null
    private var mLoadingDrawable: Drawable? = null
    private var mElectricFanDrawable: Drawable? = null

    @IntDef(MODE_NORMAL.toLong(), MODE_LEAF_COUNT.toLong())
    @Retention(RetentionPolicy.SOURCE)
    annotation class MODE

    init {
        init(context)
        setupPaint()
        addRenderListener(mAnimatorListener)
    }

    private fun init(context: Context) {
        mMode = MODE_NORMAL

        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH)
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT)
        mTextSize = DensityUtil.dip2px(context, DEFAULT_TEXT_SIZE)
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH)
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS)
        mProgressCenterRadius = DensityUtil.dip2px(context, DEFAULT_PROGRESS_CENTER_RADIUS)

        mProgressColor = DEFAULT_PROGRESS_COLOR
        mProgressBgColor = DEFAULT_PROGRESS_BGCOLOR
        mElectricFanBgColor = DEFAULT_ELECTRIC_FAN_BGCOLOR
        mElectricFanOutlineColor = DEFAULT_ELECTRIC_FAN_OUTLINE_COLOR

        mLeafDrawable = context.resources.getDrawable(R.drawable.ic_leaf)
        mLoadingDrawable = context.resources.getDrawable(R.drawable.ic_loading)
        mElectricFanDrawable = context.resources.getDrawable(R.drawable.ic_eletric_fan)

        mDuration = ANIMATION_DURATION
        setInsets(mWidth.toInt(), mHeight.toInt())
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun draw(canvas: Canvas, bounds: Rect) {
        val saveCount = canvas.save()

        val arcBounds = mTempBounds
        arcBounds.set(bounds)
        arcBounds.inset(mStrokeXInset, mStrokeYInset)

        mCurrentProgressBounds.set(arcBounds.left, arcBounds.bottom - 2 * mCenterRadius,
                arcBounds.right, arcBounds.bottom)

        //draw loading drawable
        mLoadingDrawable!!.setBounds(arcBounds.centerX().toInt() - mLoadingDrawable!!.intrinsicWidth / 2,
                0,
                arcBounds.centerX().toInt() + mLoadingDrawable!!.intrinsicWidth / 2,
                mLoadingDrawable!!.intrinsicHeight)
        mLoadingDrawable!!.draw(canvas)

        //draw progress background
        val progressInset = mCenterRadius - mProgressCenterRadius
        val progressRect = RectF(mCurrentProgressBounds)
        //sub DEFAULT_STROKE_INTERVAL, otherwise will have a interval between progress background and progress outline
        progressRect.inset(progressInset - DEFAULT_STROKE_INTERVAL, progressInset - DEFAULT_STROKE_INTERVAL)
        mPaint.color = mProgressBgColor
        mPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(progressRect, mProgressCenterRadius, mProgressCenterRadius, mPaint)

        //draw progress
        mPaint.color = mProgressColor
        mPaint.style = Paint.Style.FILL
        canvas.drawPath(createProgressPath(mProgress, mProgressCenterRadius, progressRect), mPaint)

        //draw leaves
        for (i in mLeafHolders.indices) {
            val leafSaveCount = canvas.save()
            val leafHolder = mLeafHolders[i]
            val leafBounds = leafHolder.mLeafRect

            canvas.rotate(leafHolder.mLeafRotation, leafBounds.centerX().toFloat(), leafBounds.centerY().toFloat())
            mLeafDrawable!!.bounds = leafBounds
            mLeafDrawable!!.draw(canvas)

            canvas.restoreToCount(leafSaveCount)
        }

        //draw progress background outline,
        //after drawing the leaves and then draw the outline of the progress background can
        //prevent the leaves from flying to the outside
        val progressOutlineRect = RectF(mCurrentProgressBounds)
        val progressOutlineStrokeInset = (mCenterRadius - mProgressCenterRadius) / 2.0f
        progressOutlineRect.inset(progressOutlineStrokeInset, progressOutlineStrokeInset)
        mPaint.style = Paint.Style.STROKE
        mPaint.color = mProgressBgColor
        mPaint.strokeWidth = mCenterRadius - mProgressCenterRadius
        canvas.drawRoundRect(progressOutlineRect, mCenterRadius, mCenterRadius, mPaint)

        //draw electric fan outline
        val electricFanCenterX = arcBounds.right - mCenterRadius
        val electricFanCenterY = arcBounds.bottom - mCenterRadius

        mPaint.color = mElectricFanOutlineColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mStrokeWidth
        canvas.drawCircle(arcBounds.right - mCenterRadius, arcBounds.bottom - mCenterRadius,
                mCenterRadius - mStrokeWidth / 2.0f, mPaint)

        //draw electric background
        mPaint.color = mElectricFanBgColor
        mPaint.style = Paint.Style.FILL
        canvas.drawCircle(arcBounds.right - mCenterRadius, arcBounds.bottom - mCenterRadius,
                mCenterRadius - mStrokeWidth + DEFAULT_STROKE_INTERVAL, mPaint)

        //draw electric fan
        val rotateSaveCount = canvas.save()
        canvas.rotate(mRotation, electricFanCenterX, electricFanCenterY)
        mElectricFanDrawable!!.setBounds((electricFanCenterX - mElectricFanDrawable!!.intrinsicWidth / 2 * mScale).toInt(),
                (electricFanCenterY - mElectricFanDrawable!!.intrinsicHeight / 2 * mScale).toInt(),
                (electricFanCenterX + mElectricFanDrawable!!.intrinsicWidth / 2 * mScale).toInt(),
                (electricFanCenterY + mElectricFanDrawable!!.intrinsicHeight / 2 * mScale).toInt())
        mElectricFanDrawable!!.draw(canvas)
        canvas.restoreToCount(rotateSaveCount)

        //draw 100% text
        if (mScale < 1.0f) {
            mPaint.textSize = mTextSize * (1 - mScale)
            mPaint.color = mElectricFanOutlineColor
            val textRect = Rect()
            mPaint.getTextBounds(PERCENTAGE_100, 0, PERCENTAGE_100.length, textRect)
            canvas.drawText(PERCENTAGE_100, electricFanCenterX - textRect.width() / 2.0f,
                    electricFanCenterY + textRect.height() / 2.0f, mPaint)
        }

        canvas.restoreToCount(saveCount)
    }

    private fun createProgressPath(progress: Float, circleRadius: Float, progressRect: RectF): Path {
        val arcProgressRect = RectF(progressRect.left, progressRect.top, progressRect.left + circleRadius * 2, progressRect.bottom)
        var rectProgressRect: RectF? = null

        val progressWidth = progress * progressRect.width()
        val progressModeWidth = if (mMode == MODE_LEAF_COUNT)
            mCurrentLeafCount.toFloat() / LEAF_COUNT.toFloat() * progressRect.width()
        else
            progress * progressRect.width()

        var swipeAngle = DEGREE_180.toFloat()
        //the left half circle of the progressbar
        if (progressModeWidth < circleRadius) {
            swipeAngle = progressModeWidth / circleRadius * DEGREE_180
        }

        //the center rect of the progressbar
        if (progressModeWidth < progressRect.width() - circleRadius && progressModeWidth >= circleRadius) {
            rectProgressRect = RectF(progressRect.left + circleRadius, progressRect.top, progressRect.left + progressModeWidth, progressRect.bottom)
        }

        //the right half circle of the progressbar
        if (progressWidth >= progressRect.width() - circleRadius) {
            rectProgressRect = RectF(progressRect.left + circleRadius, progressRect.top, progressRect.right - circleRadius, progressRect.bottom)
            mScale = (progressRect.width() - progressWidth) / circleRadius
        }

        //the left of the right half circle
        if (progressWidth < progressRect.width() - circleRadius) {
            mRotation = progressWidth / (progressRect.width() - circleRadius) * FULL_GROUP_ROTATION % DEGREE_360

            val leafRect = RectF(progressRect.left + progressWidth, progressRect.top, progressRect.right - circleRadius, progressRect.bottom)
            addLeaf(progress, leafRect)
        }

        val path = Path()
        path.addArc(arcProgressRect, DEGREE_180 - swipeAngle / 2, swipeAngle)

        if (rectProgressRect != null) {
            path.addRect(rectProgressRect, Path.Direction.CW)
        }

        return path
    }

    override fun computeRender(renderProgress: Float) {
        if (renderProgress < DECELERATE_DURATION_PERCENTAGE) {
            mProgress = DECELERATE_INTERPOLATOR.getInterpolation(renderProgress / DECELERATE_DURATION_PERCENTAGE) * DECELERATE_DURATION_PERCENTAGE
        } else {
            mProgress = ACCELERATE_INTERPOLATOR.getInterpolation((renderProgress - DECELERATE_DURATION_PERCENTAGE) / ACCELERATE_DURATION_PERCENTAGE) * ACCELERATE_DURATION_PERCENTAGE + DECELERATE_DURATION_PERCENTAGE
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
        mCurrentLeafCount = 0
        mNextLeafCreateThreshold = 0.0f
        mLeafHolders.clear()
    }

    protected fun setInsets(width: Int, height: Int) {
        val minEdge = Math.min(width, height).toFloat()
        val insetXs: Float
        if (mCenterRadius <= 0 || minEdge < 0) {
            insetXs = Math.ceil((mCenterRadius / 2.0f).toDouble()).toFloat()
        } else {
            insetXs = mCenterRadius
        }
        mStrokeYInset = Math.ceil((mCenterRadius / 2.0f).toDouble()).toFloat()
        mStrokeXInset = insetXs
    }

    private fun addLeaf(progress: Float, leafFlyRect: RectF) {
        if (progress < mNextLeafCreateThreshold) {
            return
        }
        mNextLeafCreateThreshold += LEAF_CREATE_DURATION_INTERVAL

        val leafHolder = LeafHolder()
        mLeafHolders.add(leafHolder)
        val leafAnimator = getAnimator(leafHolder, leafFlyRect, progress)
        leafAnimator.addListener(AnimEndListener(leafHolder))
        leafAnimator.start()
    }

    private fun getAnimator(target: LeafHolder, leafFlyRect: RectF, progress: Float): Animator {
        val bezierValueAnimator = getBezierValueAnimator(target, leafFlyRect, progress)

        val finalSet = AnimatorSet()
        finalSet.playSequentially(bezierValueAnimator)
        finalSet.interpolator = INTERPOLATORS[mRandom.nextInt(INTERPOLATORS.size)]
        finalSet.setTarget(target)
        return finalSet
    }

    private fun getBezierValueAnimator(target: LeafHolder, leafFlyRect: RectF, progress: Float): ValueAnimator {
        val evaluator = BezierEvaluator(getPoint1(leafFlyRect), getPoint2(leafFlyRect))

        val leafFlyStartY = (mCurrentProgressBounds.bottom - mLeafDrawable!!.intrinsicHeight).toInt()
        val leafFlyRange = (mCurrentProgressBounds.height() - mLeafDrawable!!.intrinsicHeight).toInt()

        val startPointY = leafFlyStartY - mRandom.nextInt(leafFlyRange)
        val endPointY = leafFlyStartY - mRandom.nextInt(leafFlyRange)

        val animator = ValueAnimator.ofObject(evaluator,
                PointF((leafFlyRect.right - mLeafDrawable!!.intrinsicWidth).toInt().toFloat(), startPointY.toFloat()),
                PointF(leafFlyRect.left, endPointY.toFloat()))
        animator.addUpdateListener(BezierListener(target))
        animator.setTarget(target)

        animator.duration = ((mRandom.nextInt(300) + mDuration * DEFAULT_LEAF_FLY_DURATION_FACTOR) * (1.0f - progress)).toLong()

        return animator
    }

    //get the pointF which belong to the right half side
    private fun getPoint1(leafFlyRect: RectF): PointF {
        val point = PointF()
        point.x = leafFlyRect.right - mRandom.nextInt((leafFlyRect.width() / 2).toInt())
        point.y = (leafFlyRect.bottom - mRandom.nextInt(leafFlyRect.height().toInt())).toInt().toFloat()
        return point
    }

    //get the pointF which belong to the left half side
    private fun getPoint2(leafFlyRect: RectF): PointF {
        val point = PointF()
        point.x = leafFlyRect.left + mRandom.nextInt((leafFlyRect.width() / 2).toInt())
        point.y = (leafFlyRect.bottom - mRandom.nextInt(leafFlyRect.height().toInt())).toInt().toFloat()
        return point
    }

    private inner class BezierEvaluator(private val point1: PointF, private val point2: PointF) : TypeEvaluator<PointF> {

        //Third-order Bezier curve formula: B(t) = point0 * (1-t)^3 + 3 * point1 * t * (1-t)^2 + 3 * point2 * t^2 * (1-t) + point3 * t^3
        override fun evaluate(fraction: Float, point0: PointF, point3: PointF): PointF {

            val tLeft = 1.0f - fraction

            val x = (point0.x * Math.pow(tLeft.toDouble(), 3.0) + 3.0 * point1.x.toDouble() * fraction.toDouble() * Math.pow(tLeft.toDouble(), 2.0) + 3.0 * point2.x.toDouble() * Math.pow(fraction.toDouble(), 2.0) * tLeft.toDouble() + point3.x * Math.pow(fraction.toDouble(), 3.0)).toFloat()
            val y = (point0.y * Math.pow(tLeft.toDouble(), 3.0) + 3.0 * point1.y.toDouble() * fraction.toDouble() * Math.pow(tLeft.toDouble(), 2.0) + 3.0 * point2.y.toDouble() * Math.pow(fraction.toDouble(), 2.0) * tLeft.toDouble() + point3.y * Math.pow(fraction.toDouble(), 3.0)).toFloat()

            return PointF(x, y)
        }
    }

    private inner class BezierListener(private val target: LeafHolder) : ValueAnimator.AnimatorUpdateListener {

        override fun onAnimationUpdate(animation: ValueAnimator) {
            val point = animation.animatedValue as PointF
            target.mLeafRect.set(point.x.toInt(), point.y.toInt(),
                    (point.x + mLeafDrawable!!.intrinsicWidth).toInt(), (point.y + mLeafDrawable!!.intrinsicHeight).toInt())
            target.mLeafRotation = target.mMaxRotation * animation.animatedFraction
        }
    }

    private inner class AnimEndListener(private val target: LeafHolder) : AnimatorListenerAdapter() {

        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            mLeafHolders.remove(target)
            mCurrentLeafCount++
        }
    }

    private inner class LeafHolder {
        var mLeafRect = Rect()
        var mLeafRotation = 0.0f

        var mMaxRotation = mRandom.nextInt(120).toFloat()
    }

    class Builder(private val mContext: Context) {

        fun build(): ElectricFanLoadingRenderer {
            return ElectricFanLoadingRenderer(mContext)
        }
    }

    companion object {
        private val LINEAR_INTERPOLATOR = LinearInterpolator()
        private val MATERIAL_INTERPOLATOR = FastOutSlowInInterpolator()
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator()
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val FASTOUTLINEARIN_INTERPOLATOR = FastOutLinearInInterpolator()

        private val INTERPOLATORS = arrayOf(LINEAR_INTERPOLATOR, DECELERATE_INTERPOLATOR, ACCELERATE_INTERPOLATOR, FASTOUTLINEARIN_INTERPOLATOR, MATERIAL_INTERPOLATOR)
        private val mLeafHolders = ArrayList<LeafHolder>()
        private val mRandom = Random()

        const val MODE_NORMAL = 0
        const val MODE_LEAF_COUNT = 1

        private val PERCENTAGE_100 = "100%"

        private val ANIMATION_DURATION: Long = 7333

        private val LEAF_COUNT = 28
        private val DEGREE_180 = 180
        private val DEGREE_360 = 360
        private val FULL_GROUP_ROTATION = (5.25f * DEGREE_360).toInt()

        private val DEFAULT_PROGRESS_COLOR = -0x358d2
        private val DEFAULT_PROGRESS_BGCOLOR = -0x32b61
        private val DEFAULT_ELECTRIC_FAN_BGCOLOR = -0x333a7
        private val DEFAULT_ELECTRIC_FAN_OUTLINE_COLOR = Color.WHITE

        private val DEFAULT_WIDTH = 182.0f
        private val DEFAULT_HEIGHT = 65.0f
        private val DEFAULT_TEXT_SIZE = 11.0f
        private val DEFAULT_STROKE_WIDTH = 2.0f
        private val DEFAULT_STROKE_INTERVAL = .2f
        private val DEFAULT_CENTER_RADIUS = 16.0f
        private val DEFAULT_PROGRESS_CENTER_RADIUS = 11.0f

        private val DEFAULT_LEAF_FLY_DURATION_FACTOR = 0.1f

        private val LEAF_CREATE_DURATION_INTERVAL = 1.0f / LEAF_COUNT
        private val DECELERATE_DURATION_PERCENTAGE = 0.4f
        private val ACCELERATE_DURATION_PERCENTAGE = 0.6f
    }
}

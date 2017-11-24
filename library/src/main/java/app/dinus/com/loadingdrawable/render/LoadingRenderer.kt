package app.dinus.com.loadingdrawable.render

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.LinearInterpolator

import app.dinus.com.loadingdrawable.DensityUtil

abstract class LoadingRenderer(context: Context) {

    private val mAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        computeRender(animation.animatedValue as Float)
        invalidateSelf()
    }

    /**
     * Whenever [LoadingDrawable] boundary changes mBounds will be updated.
     * More details you can see [LoadingDrawable.onBoundsChange]
     */
    protected val mBounds = Rect()

    private var mCallback: Drawable.Callback? = null
    private var mRenderAnimator: ValueAnimator? = null

    protected var mDuration: Long = 0

    var mWidth: Float = 0.toFloat()
    var mHeight: Float = 0.toFloat()

    internal val isRunning: Boolean
        get() = mRenderAnimator!!.isRunning

    init {
        initParams(context)
        setupAnimators()
    }

    @Deprecated("")
    protected open fun draw(canvas: Canvas, bounds: Rect) {
    }

    open fun draw(canvas: Canvas) {
        draw(canvas, mBounds)
    }

    protected abstract fun computeRender(renderProgress: Float)

    abstract fun setAlpha(alpha: Int)

    abstract fun setColorFilter(cf: ColorFilter?)

    protected abstract fun reset()

    protected fun addRenderListener(animatorListener: Animator.AnimatorListener) {
        mRenderAnimator!!.addListener(animatorListener)
    }

    internal fun start() {
        reset()
        mRenderAnimator!!.addUpdateListener(mAnimatorUpdateListener)

        mRenderAnimator!!.repeatCount = ValueAnimator.INFINITE
        mRenderAnimator!!.duration = mDuration
        mRenderAnimator!!.start()
    }

    internal fun stop() {
        // if I just call mRenderAnimator.end(),
        // it will always call the method onAnimationUpdate(ValueAnimator animation)
        // why ? if you know why please send email to me (dinus_developer@163.com)
        mRenderAnimator!!.removeUpdateListener(mAnimatorUpdateListener)

        mRenderAnimator!!.repeatCount = 0
        mRenderAnimator!!.duration = 0
        mRenderAnimator!!.end()
    }

    internal fun setCallback(callback: Drawable.Callback) {
        this.mCallback = callback
    }

    internal fun setBounds(bounds: Rect) {
        mBounds.set(bounds)
    }

    private fun initParams(context: Context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_SIZE)
        mHeight = DensityUtil.dip2px(context, DEFAULT_SIZE)

        mDuration = ANIMATION_DURATION
    }

    private fun setupAnimators() {
        mRenderAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        mRenderAnimator!!.repeatCount = Animation.INFINITE
        mRenderAnimator!!.repeatMode = ValueAnimator.RESTART
        mRenderAnimator!!.duration = mDuration
        //fuck you! the default interpolator is AccelerateDecelerateInterpolator
        mRenderAnimator!!.interpolator = LinearInterpolator()
        mRenderAnimator!!.addUpdateListener(mAnimatorUpdateListener)
    }

    private fun invalidateSelf() {
        mCallback!!.invalidateDrawable(null)
    }

    companion object {
        private val ANIMATION_DURATION: Long = 1333
        private val DEFAULT_SIZE = 56.0f
    }

}

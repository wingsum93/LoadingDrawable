package app.dinus.com.loadingdrawable

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

import app.dinus.com.loadingdrawable.render.LoadingDrawable
import app.dinus.com.loadingdrawable.render.LoadingRenderer
import app.dinus.com.loadingdrawable.render.LoadingRendererFactory

class LoadingView : ImageView {
    private var mLoadingDrawable: LoadingDrawable? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet) {
        try {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.LoadingView)
            val loadingRendererId = ta.getInt(R.styleable.LoadingView_loading_renderer, 0)
            val loadingRenderer = LoadingRendererFactory.createLoadingRenderer(context, loadingRendererId)
            setLoadingRenderer(loadingRenderer)
            ta.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun setLoadingRenderer(loadingRenderer: LoadingRenderer) {
        mLoadingDrawable = LoadingDrawable(loadingRenderer)
        setImageDrawable(mLoadingDrawable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        val visible = visibility == View.VISIBLE && getVisibility() == View.VISIBLE
        if (visible) {
            startAnimation()
        } else {
            stopAnimation()
        }
    }

    private fun startAnimation() {

            mLoadingDrawable?.start()

    }

    private fun stopAnimation() {

            mLoadingDrawable?.stop()

    }
}

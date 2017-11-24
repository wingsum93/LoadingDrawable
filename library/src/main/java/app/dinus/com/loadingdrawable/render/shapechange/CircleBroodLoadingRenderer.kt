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
import android.support.v7.widget.ViewUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

import app.dinus.com.loadingdrawable.DensityUtil
import app.dinus.com.loadingdrawable.render.LoadingRenderer

class CircleBroodLoadingRenderer private constructor(context: Context) : LoadingRenderer(context) {

    private val MOTHER_MOVE_INTERPOLATOR = MotherMoveInterpolator()
    private val CHILD_MOVE_INTERPOLATOR = ChildMoveInterpolator()

    private val ACCELERATE_INTERPOLATOR03 = AccelerateInterpolator(0.3f)
    private val ACCELERATE_INTERPOLATOR05 = AccelerateInterpolator(0.5f)
    private val ACCELERATE_INTERPOLATOR08 = AccelerateInterpolator(0.8f)
    private val ACCELERATE_INTERPOLATOR10 = AccelerateInterpolator(1.0f)

    private val DECELERATE_INTERPOLATOR03 = DecelerateInterpolator(0.3f)
    private val DECELERATE_INTERPOLATOR05 = DecelerateInterpolator(0.5f)
    private val DECELERATE_INTERPOLATOR08 = DecelerateInterpolator(0.8f)
    private val DECELERATE_INTERPOLATOR10 = DecelerateInterpolator(1.0f)

    private val STAGE_MOTHER_FORWARD_TOP_LEFT = 0.34f
    private val STAGE_MOTHER_BACKWARD_TOP_LEFT = 0.5f
    private val STAGE_MOTHER_FORWARD_BOTTOM_LEFT = 0.65f
    private val STAGE_MOTHER_BACKWARD_BOTTOM_LEFT = 0.833f

    private val STAGE_CHILD_DELAY = 0.1f
    private val STAGE_CHILD_PRE_FORWARD_TOP_LEFT = 0.26f
    private val STAGE_CHILD_FORWARD_TOP_LEFT = 0.34f
    private val STAGE_CHILD_PRE_BACKWARD_TOP_LEFT = 0.42f
    private val STAGE_CHILD_BACKWARD_TOP_LEFT = 0.5f
    private val STAGE_CHILD_FORWARD_BOTTOM_LEFT = 0.7f
    private val STAGE_CHILD_BACKWARD_BOTTOM_LEFT = 0.9f

    private val OVAL_BEZIER_FACTOR = 0.55152f

    private val DEFAULT_WIDTH = 200.0f
    private val DEFAULT_HEIGHT = 150.0f
    private val MAX_MATHER_OVAL_SIZE = 19f
    private val MIN_CHILD_OVAL_RADIUS = 5f
    private val MAX_MATHER_SHAPE_CHANGE_FACTOR = 0.8452f

    private val DEFAULT_OVAL_COLOR = Color.parseColor("#FFBE1C23")
    private val DEFAULT_OVAL_DEEP_COLOR = Color.parseColor("#FFB21721")
    private val DEFAULT_BACKGROUND_COLOR = Color.parseColor("#FFE3C172")
    private val DEFAULT_BACKGROUND_DEEP_COLOR = Color.parseColor("#FFE2B552")

    private val ANIMATION_DURATION: Long = 4111

    private val mPaint = Paint()
    private val mCurrentBounds = RectF()
    private val mMotherOvalPath = Path()
    private val mMotherMovePath = Path()
    private val mChildMovePath = Path()

    private val mMotherPosition = FloatArray(2)
    private val mChildPosition = FloatArray(2)
    private val mMotherMovePathMeasure = PathMeasure()
    private val mChildMovePathMeasure = PathMeasure()

    private var mChildOvalRadius: Float = 0.toFloat()
    private var mBasicChildOvalRadius: Float = 0.toFloat()
    private var mMaxMotherOvalSize: Float = 0.toFloat()
    private var mMotherOvalHalfWidth: Float = 0.toFloat()
    private var mMotherOvalHalfHeight: Float = 0.toFloat()

    private var mChildLeftXOffset: Float = 0.toFloat()
    private var mChildLeftYOffset: Float = 0.toFloat()
    private var mChildRightXOffset: Float = 0.toFloat()
    private var mChildRightYOffset: Float = 0.toFloat()

    private var mOvalColor: Int = 0
    private var mOvalDeepColor: Int = 0
    private var mBackgroundColor: Int = 0
    private var mBackgroundDeepColor: Int = 0
    private var mCurrentOvalColor: Int = 0
    private var mCurrentBackgroundColor: Int = 0

    private var mRevealCircleRadius: Int = 0
    private var mMaxRevealCircleRadius: Int = 0

    private var mRotateDegrees: Int = 0

    private var mStageMotherForwardTopLeftLength: Float = 0.toFloat()
    private var mStageMotherBackwardTopLeftLength: Float = 0.toFloat()
    private var mStageMotherForwardBottomLeftLength: Float = 0.toFloat()
    private var mStageMotherBackwardBottomLeftLength: Float = 0.toFloat()

    private var mStageChildPreForwardTopLeftLength: Float = 0.toFloat()
    private var mStageChildForwardTopLeftLength: Float = 0.toFloat()
    private var mStageChildPreBackwardTopLeftLength: Float = 0.toFloat()
    private var mStageChildBackwardTopLeftLength: Float = 0.toFloat()
    private var mStageChildForwardBottomLeftLength: Float = 0.toFloat()
    private var mStageChildBackwardBottomLeftLength: Float = 0.toFloat()

    init {
        init(context)
        setupPaint()
    }

    private fun init(context: Context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH)
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT)

        mMaxMotherOvalSize = DensityUtil.dip2px(context, MAX_MATHER_OVAL_SIZE)
        mBasicChildOvalRadius = DensityUtil.dip2px(context, MIN_CHILD_OVAL_RADIUS)

        mOvalColor = DEFAULT_OVAL_COLOR
        mOvalDeepColor = DEFAULT_OVAL_DEEP_COLOR
        mBackgroundColor = DEFAULT_BACKGROUND_COLOR
        mBackgroundDeepColor = DEFAULT_BACKGROUND_DEEP_COLOR

        mMotherOvalHalfWidth = mMaxMotherOvalSize
        mMotherOvalHalfHeight = mMaxMotherOvalSize

        mMaxRevealCircleRadius = (Math.sqrt((mWidth * mWidth + mHeight * mHeight).toDouble()) / 2 + 1).toInt()

        mDuration = ANIMATION_DURATION
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = 1.0f
    }

    override fun draw(canvas: Canvas, bounds: Rect) {
        val saveCount = canvas.save()

        val arcBounds = mCurrentBounds
        arcBounds.set(bounds)

        //draw background
        canvas.drawColor(mCurrentBackgroundColor)
        //draw reveal circle
        if (mRevealCircleRadius > 0) {
            mPaint.color = if (mCurrentBackgroundColor == mBackgroundColor) mBackgroundDeepColor else mBackgroundColor
            canvas.drawCircle(arcBounds.centerX(), arcBounds.centerY(), mRevealCircleRadius.toFloat(), mPaint)
        }

        //draw mother oval
        mPaint.color = mCurrentOvalColor

        val motherSaveCount = canvas.save()
        canvas.rotate(mRotateDegrees.toFloat(), mMotherPosition[0], mMotherPosition[1])
        canvas.drawPath(createMotherPath(), mPaint)
        canvas.drawPath(createLinkPath(), mPaint)
        canvas.restoreToCount(motherSaveCount)

        val childSaveCount = canvas.save()
        canvas.rotate(mRotateDegrees.toFloat(), mChildPosition[0], mChildPosition[1])
        canvas.drawPath(createChildPath(), mPaint)
        canvas.restoreToCount(childSaveCount)
        canvas.restoreToCount(saveCount)

        //    canvas.drawPath(mMotherMovePath, mPaint);
        //    canvas.drawPath(mChildMovePath, mPaint);
        //    canvas.drawLine(mMotherPosition[0], mMotherPosition[1], mChildPosition[0], mChildPosition[1], mPaint);
    }

    private fun createMotherPath(): Path {
        mMotherOvalPath.reset()

        mMotherOvalPath.addOval(RectF(mMotherPosition[0] - mMotherOvalHalfWidth, mMotherPosition[1] - mMotherOvalHalfHeight,
                mMotherPosition[0] + mMotherOvalHalfWidth, mMotherPosition[1] + mMotherOvalHalfHeight), Path.Direction.CW)

        return mMotherOvalPath
    }

    private fun createChildPath(): Path {
        val bezierOffset = mChildOvalRadius * OVAL_BEZIER_FACTOR

        val path = Path()
        path.moveTo(mChildPosition[0], mChildPosition[1] - mChildOvalRadius)
        //left_top arc
        path.cubicTo(mChildPosition[0] - bezierOffset - mChildLeftXOffset, mChildPosition[1] - mChildOvalRadius,
                mChildPosition[0] - mChildOvalRadius - mChildLeftXOffset, mChildPosition[1] - bezierOffset + mChildLeftYOffset,
                mChildPosition[0] - mChildOvalRadius - mChildLeftXOffset, mChildPosition[1])
        //left_bottom arc
        path.cubicTo(mChildPosition[0] - mChildOvalRadius - mChildLeftXOffset, mChildPosition[1] + bezierOffset - mChildLeftYOffset,
                mChildPosition[0] - bezierOffset - mChildLeftXOffset, mChildPosition[1] + mChildOvalRadius,
                mChildPosition[0], mChildPosition[1] + mChildOvalRadius)

        //right_bottom arc
        path.cubicTo(mChildPosition[0] + bezierOffset + mChildRightXOffset, mChildPosition[1] + mChildOvalRadius,
                mChildPosition[0] + mChildOvalRadius + mChildRightXOffset, mChildPosition[1] + bezierOffset - mChildRightYOffset,
                mChildPosition[0] + mChildOvalRadius + mChildRightXOffset, mChildPosition[1])
        //right_top arc
        path.cubicTo(mChildPosition[0] + mChildOvalRadius + mChildRightXOffset, mChildPosition[1] - bezierOffset + mChildRightYOffset,
                mChildPosition[0] + bezierOffset + mChildRightXOffset, mChildPosition[1] - mChildOvalRadius,
                mChildPosition[0], mChildPosition[1] - mChildOvalRadius)

        return path
    }

    private fun createLinkPath(): Path {
        val path = Path()
        val bezierOffset = mMotherOvalHalfWidth * OVAL_BEZIER_FACTOR

        val distance = Math.sqrt(Math.pow((mMotherPosition[0] - mChildPosition[0]).toDouble(), 2.0) + Math.pow((mMotherPosition[1] - mChildPosition[1]).toDouble(), 2.0)).toFloat()
        if (distance <= mMotherOvalHalfWidth + mChildOvalRadius * 1.2f && distance >= mMotherOvalHalfWidth - mChildOvalRadius * 1.2f) {
            val maxOffsetY = 2f * mChildOvalRadius * 1.2f
            val offsetRate = (distance - (mMotherOvalHalfWidth - mChildOvalRadius * 1.2f)) / maxOffsetY

            val mMotherOvalOffsetY = mMotherOvalHalfHeight - offsetRate * (mMotherOvalHalfHeight - mChildOvalRadius) * 0.85f

            mMotherOvalPath.addOval(RectF(mMotherPosition[0] - mMotherOvalHalfWidth, mMotherPosition[1] - mMotherOvalOffsetY,
                    mMotherPosition[0] + mMotherOvalHalfWidth, mMotherPosition[1] + mMotherOvalOffsetY), Path.Direction.CW)

            val mMotherXOffset = distance - mMotherOvalHalfWidth + mChildOvalRadius
            val distanceUltraLeft = Math.sqrt(Math.pow((mMotherPosition[0] - mMotherOvalHalfWidth - mChildPosition[0]).toDouble(), 2.0) + Math.pow((mMotherPosition[1] - mChildPosition[1]).toDouble(), 2.0)).toFloat()
            val distanceUltraRight = Math.sqrt(Math.pow((mMotherPosition[0] + mMotherOvalHalfWidth - mChildPosition[0]).toDouble(), 2.0) + Math.pow((mMotherPosition[1] - mChildPosition[1]).toDouble(), 2.0)).toFloat()

            path.moveTo(mMotherPosition[0], mMotherPosition[1] + mMotherOvalOffsetY)
            if (distanceUltraRight < distanceUltraLeft) {
                //right_bottom arc
                path.cubicTo(mMotherPosition[0] + bezierOffset + mMotherXOffset, mMotherPosition[1] + mMotherOvalOffsetY,
                        mMotherPosition[0] + distance + mChildOvalRadius, mMotherPosition[1] + mChildOvalRadius * 1.5f,
                        mMotherPosition[0] + distance + mChildOvalRadius, mMotherPosition[1])
                //right_top arc
                path.cubicTo(mMotherPosition[0] + distance + mChildOvalRadius, mMotherPosition[1] - mChildOvalRadius * 1.5f,
                        mMotherPosition[0] + bezierOffset + mMotherXOffset, mMotherPosition[1] - mMotherOvalOffsetY,
                        mMotherPosition[0], mMotherPosition[1] - mMotherOvalOffsetY)
            } else {
                //left_bottom arc
                path.cubicTo(mMotherPosition[0] - bezierOffset - mMotherXOffset, mMotherPosition[1] + mMotherOvalOffsetY,
                        mMotherPosition[0] - distance - mChildOvalRadius, mMotherPosition[1] + mChildOvalRadius * 1.5f,
                        mMotherPosition[0] - distance - mChildOvalRadius, mMotherPosition[1])
                //left_top arc
                path.cubicTo(mMotherPosition[0] - distance - mChildOvalRadius, mMotherPosition[1] - mChildOvalRadius * 1.5f,
                        mMotherPosition[0] - bezierOffset - mMotherXOffset, mMotherPosition[1] - mMotherOvalOffsetY,
                        mMotherPosition[0], mMotherPosition[1] - mMotherOvalOffsetY)
            }
            path.lineTo(mMotherPosition[0], mMotherPosition[1] + mMotherOvalOffsetY)
        }

        return path
    }

    override fun computeRender(renderProgress: Float) {
        if (mCurrentBounds.isEmpty) {
            return
        }

        if (mMotherMovePath.isEmpty) {
            mMotherMovePath.set(createMotherMovePath())
            mMotherMovePathMeasure.setPath(mMotherMovePath, false)

            mChildMovePath.set(createChildMovePath())
            mChildMovePathMeasure.setPath(mChildMovePath, false)
        }

        //mother oval
        val motherMoveProgress = MOTHER_MOVE_INTERPOLATOR.getInterpolation(renderProgress)
        mMotherMovePathMeasure.getPosTan(getCurrentMotherMoveLength(motherMoveProgress), mMotherPosition, null)
        mMotherOvalHalfWidth = mMaxMotherOvalSize
        mMotherOvalHalfHeight = mMaxMotherOvalSize * getMotherShapeFactor(motherMoveProgress)

        //child Oval
        val childMoveProgress = CHILD_MOVE_INTERPOLATOR.getInterpolation(renderProgress)
        mChildMovePathMeasure.getPosTan(getCurrentChildMoveLength(childMoveProgress), mChildPosition, null)
        setupChildParams(childMoveProgress)

        mRotateDegrees = Math.toDegrees(Math.atan(((mMotherPosition[1] - mChildPosition[1]) / (mMotherPosition[0] - mChildPosition[0])).toDouble())).toInt()

        mRevealCircleRadius = getCurrentRevealCircleRadius(renderProgress)
        mCurrentOvalColor = getCurrentOvalColor(renderProgress)
        mCurrentBackgroundColor = getCurrentBackgroundColor(renderProgress)
    }

    private fun setupChildParams(input: Float) {
        mChildOvalRadius = mBasicChildOvalRadius

        mChildRightXOffset = 0.0f
        mChildLeftXOffset = 0.0f

        if (input <= STAGE_CHILD_PRE_FORWARD_TOP_LEFT) {
            if (input >= 0.25) {
                val shapeProgress = (input - 0.25f) / 0.01f
                mChildLeftXOffset = (1.0f - shapeProgress) * mChildOvalRadius * 0.25f
            } else {
                mChildLeftXOffset = mChildOvalRadius * 0.25f
            }
        } else if (input <= STAGE_CHILD_FORWARD_TOP_LEFT) {
            if (input > 0.275f && input < 0.285f) {
                val shapeProgress = (input - 0.275f) / 0.01f
                mChildLeftXOffset = shapeProgress * mChildOvalRadius * 0.25f
            } else if (input > 0.285f) {
                mChildLeftXOffset = mChildOvalRadius * 0.25f
            }
        } else if (input <= STAGE_CHILD_PRE_BACKWARD_TOP_LEFT) {
            if (input > 0.38f) {
                val radiusProgress = (input - 0.38f) / 0.04f
                mChildOvalRadius = mBasicChildOvalRadius * (1.0f + radiusProgress)
            }
        } else if (input <= STAGE_CHILD_BACKWARD_TOP_LEFT) {
            if (input < 0.46f) {
                val radiusProgress = (input - 0.42f) / 0.04f
                mChildOvalRadius = mBasicChildOvalRadius * (2.0f - radiusProgress)
            }
        } else if (input <= STAGE_CHILD_FORWARD_BOTTOM_LEFT) {
            if (input > 0.65f) {
                val radiusProgress = (input - 0.65f) / 0.05f
                mChildOvalRadius = mBasicChildOvalRadius * (1.0f + radiusProgress)
            }
        } else if (input <= STAGE_CHILD_BACKWARD_BOTTOM_LEFT) {
            if (input < 0.71f) {
                mChildOvalRadius = mBasicChildOvalRadius * 2.0f
            } else if (input < 0.76f) {
                val radiusProgress = (input - 0.71f) / 0.05f
                mChildOvalRadius = mBasicChildOvalRadius * (2.0f - radiusProgress)
            }
        } else {
        }

        mChildRightYOffset = mChildRightXOffset / 2.5f
        mChildLeftYOffset = mChildLeftXOffset / 2.5f
    }

    private fun getMotherShapeFactor(input: Float): Float {

        val shapeProgress: Float
        if (input <= STAGE_MOTHER_FORWARD_TOP_LEFT) {
            shapeProgress = input / STAGE_MOTHER_FORWARD_TOP_LEFT
        } else if (input <= STAGE_MOTHER_BACKWARD_TOP_LEFT) {
            shapeProgress = (input - STAGE_MOTHER_FORWARD_TOP_LEFT) / (STAGE_MOTHER_BACKWARD_TOP_LEFT - STAGE_MOTHER_FORWARD_TOP_LEFT)
        } else if (input <= STAGE_MOTHER_FORWARD_BOTTOM_LEFT) {
            shapeProgress = (input - STAGE_MOTHER_BACKWARD_TOP_LEFT) / (STAGE_MOTHER_FORWARD_BOTTOM_LEFT - STAGE_MOTHER_BACKWARD_TOP_LEFT)
        } else if (input <= STAGE_MOTHER_BACKWARD_BOTTOM_LEFT) {
            shapeProgress = (input - STAGE_MOTHER_FORWARD_BOTTOM_LEFT) / (STAGE_MOTHER_BACKWARD_BOTTOM_LEFT - STAGE_MOTHER_FORWARD_BOTTOM_LEFT)
        } else {
            shapeProgress = 1.0f
        }

        return if (shapeProgress < 0.5f)
            1.0f - (1.0f - MAX_MATHER_SHAPE_CHANGE_FACTOR) * shapeProgress * 2.0f
        else
            MAX_MATHER_SHAPE_CHANGE_FACTOR + (1.0f - MAX_MATHER_SHAPE_CHANGE_FACTOR) * (shapeProgress - 0.5f) * 2.0f
    }

    private fun getCurrentMotherMoveLength(input: Float): Float {
        var currentStartDistance = 0.0f
        var currentStageDistance = 0.0f
        var currentStateStartProgress = 0.0f
        var currentStateEndProgress = 0.0f

        if (input > 0.0f) {
            currentStartDistance += currentStageDistance
            currentStageDistance = mStageMotherForwardTopLeftLength
            currentStateStartProgress = 0.0f
            currentStateEndProgress = STAGE_MOTHER_FORWARD_TOP_LEFT
        }

        if (input > STAGE_MOTHER_FORWARD_TOP_LEFT) {
            currentStartDistance += currentStageDistance
            currentStageDistance = mStageMotherBackwardTopLeftLength
            currentStateStartProgress = STAGE_MOTHER_FORWARD_TOP_LEFT
            currentStateEndProgress = STAGE_MOTHER_BACKWARD_TOP_LEFT
        }

        if (input > STAGE_MOTHER_BACKWARD_TOP_LEFT) {
            currentStartDistance += currentStageDistance
            currentStageDistance = mStageMotherForwardBottomLeftLength
            currentStateStartProgress = STAGE_MOTHER_BACKWARD_TOP_LEFT
            currentStateEndProgress = STAGE_MOTHER_FORWARD_BOTTOM_LEFT
        }

        if (input > STAGE_MOTHER_FORWARD_BOTTOM_LEFT) {
            currentStartDistance += currentStageDistance
            currentStageDistance = mStageMotherBackwardBottomLeftLength
            currentStateStartProgress = STAGE_MOTHER_FORWARD_BOTTOM_LEFT
            currentStateEndProgress = STAGE_MOTHER_BACKWARD_BOTTOM_LEFT
        }

        return if (input > STAGE_MOTHER_BACKWARD_BOTTOM_LEFT) {
            currentStartDistance + currentStageDistance
        } else currentStartDistance + (input - currentStateStartProgress) / (currentStateEndProgress - currentStateStartProgress) * currentStageDistance

    }

    private fun getCurrentChildMoveLength(input: Float): Float {
        var currentStartDistance = 0.0f
        var currentStageDistance = 0.0f
        var currentStateStartProgress = 0.0f
        var currentStateEndProgress = 0.0f

        if (input > 0.0f) {
            currentStartDistance += currentStageDistance
            currentStageDistance = mStageChildPreForwardTopLeftLength
            currentStateStartProgress = 0.0f
            currentStateEndProgress = STAGE_CHILD_PRE_FORWARD_TOP_LEFT
        }

        if (input > STAGE_CHILD_PRE_FORWARD_TOP_LEFT) {
            currentStartDistance += currentStageDistance
            currentStageDistance = mStageChildForwardTopLeftLength
            currentStateStartProgress = STAGE_CHILD_PRE_FORWARD_TOP_LEFT
            currentStateEndProgress = STAGE_CHILD_FORWARD_TOP_LEFT
        }

        if (input > STAGE_CHILD_FORWARD_TOP_LEFT) {
            currentStartDistance += currentStageDistance
            currentStageDistance = mStageChildPreBackwardTopLeftLength
            currentStateStartProgress = STAGE_CHILD_FORWARD_TOP_LEFT
            currentStateEndProgress = STAGE_CHILD_PRE_BACKWARD_TOP_LEFT
        }

        if (input > STAGE_CHILD_PRE_BACKWARD_TOP_LEFT) {
            currentStartDistance += currentStageDistance
            currentStageDistance = mStageChildBackwardTopLeftLength
            currentStateStartProgress = STAGE_CHILD_PRE_BACKWARD_TOP_LEFT
            currentStateEndProgress = STAGE_CHILD_BACKWARD_TOP_LEFT
        }

        if (input > STAGE_CHILD_BACKWARD_TOP_LEFT) {
            currentStartDistance += currentStageDistance
            currentStageDistance = mStageChildForwardBottomLeftLength
            currentStateStartProgress = STAGE_CHILD_BACKWARD_TOP_LEFT
            currentStateEndProgress = STAGE_CHILD_FORWARD_BOTTOM_LEFT
        }

        if (input > STAGE_CHILD_FORWARD_BOTTOM_LEFT) {
            currentStartDistance += currentStageDistance
            currentStageDistance = mStageChildBackwardBottomLeftLength
            currentStateStartProgress = STAGE_CHILD_FORWARD_BOTTOM_LEFT
            currentStateEndProgress = STAGE_CHILD_BACKWARD_BOTTOM_LEFT
        }

        return if (input > STAGE_CHILD_BACKWARD_BOTTOM_LEFT) {
            currentStartDistance + currentStageDistance
        } else currentStartDistance + (input - currentStateStartProgress) / (currentStateEndProgress - currentStateStartProgress) * currentStageDistance

    }

    private fun createMotherMovePath(): Path {
        val path = Path()

        val centerX = mCurrentBounds.centerX()
        val centerY = mCurrentBounds.centerY()
        var currentPathLength = 0.0f

        path.moveTo(centerX, centerY)
        //forward top left
        path.quadTo(centerX - mMotherOvalHalfWidth * 2.0f, centerY,
                centerX - mMotherOvalHalfWidth * 2.0f, centerY - mMotherOvalHalfHeight)
        mStageMotherForwardTopLeftLength = getRestLength(path, currentPathLength)
        currentPathLength += mStageMotherForwardTopLeftLength

        //backward top left
        path.quadTo(centerX - mMotherOvalHalfWidth * 1.0f, centerY - mMotherOvalHalfHeight,
                centerX, centerY)
        mStageMotherBackwardTopLeftLength = getRestLength(path, currentPathLength)
        currentPathLength += mStageMotherBackwardTopLeftLength
        //forward bottom left
        path.quadTo(centerX, centerY + mMotherOvalHalfHeight,
                centerX - mMotherOvalHalfWidth / 2, centerY + mMotherOvalHalfHeight * 1.1f)
        mStageMotherForwardBottomLeftLength = getRestLength(path, currentPathLength)
        currentPathLength += mStageMotherForwardBottomLeftLength
        //backward bottom left
        path.quadTo(centerX - mMotherOvalHalfWidth / 2, centerY + mMotherOvalHalfHeight * 0.6f,
                centerX, centerY)
        mStageMotherBackwardBottomLeftLength = getRestLength(path, currentPathLength)

        return path
    }

    private fun createChildMovePath(): Path {
        val path = Path()

        val centerX = mCurrentBounds.centerX()
        val centerY = mCurrentBounds.centerY()
        var currentPathLength = 0.0f

        //start
        path.moveTo(centerX, centerY)
        //pre forward top left
        path.lineTo(centerX + mMotherOvalHalfWidth * 0.75f, centerY)
        mStageChildPreForwardTopLeftLength = getRestLength(path, currentPathLength)
        currentPathLength += mStageChildPreForwardTopLeftLength
        //forward top left
        path.quadTo(centerX - mMotherOvalHalfWidth * 0.5f, centerY,
                centerX - mMotherOvalHalfWidth * 2.0f, centerY - mMotherOvalHalfHeight)
        mStageChildForwardTopLeftLength = getRestLength(path, currentPathLength)
        currentPathLength += mStageChildForwardTopLeftLength
        //pre backward top left
        path.lineTo(centerX - mMotherOvalHalfWidth * 2.0f + mMotherOvalHalfWidth * 0.2f, centerY - mMotherOvalHalfHeight)
        path.quadTo(centerX - mMotherOvalHalfWidth * 2.5f, centerY - mMotherOvalHalfHeight * 2,
                centerX - mMotherOvalHalfWidth * 1.5f, centerY - mMotherOvalHalfHeight * 2.25f)
        mStageChildPreBackwardTopLeftLength = getRestLength(path, currentPathLength)
        currentPathLength += mStageChildPreBackwardTopLeftLength
        //backward top left
        path.quadTo(centerX - mMotherOvalHalfWidth * 0.2f, centerY - mMotherOvalHalfHeight * 2.25f,
                centerX, centerY)
        mStageChildBackwardTopLeftLength = getRestLength(path, currentPathLength)
        currentPathLength += mStageChildBackwardTopLeftLength
        //forward bottom left
        path.cubicTo(centerX, centerY + mMotherOvalHalfHeight,
                centerX - mMotherOvalHalfWidth, centerY + mMotherOvalHalfHeight * 2.5f,
                centerX - mMotherOvalHalfWidth * 1.5f, centerY + mMotherOvalHalfHeight * 2.5f)
        mStageChildForwardBottomLeftLength = getRestLength(path, currentPathLength)
        currentPathLength += mStageChildForwardBottomLeftLength
        //backward bottom left
        path.cubicTo(
                centerX - mMotherOvalHalfWidth * 2.0f, centerY + mMotherOvalHalfHeight * 2.5f,
                centerX - mMotherOvalHalfWidth * 3.0f, centerY + mMotherOvalHalfHeight * 0.8f,
                centerX, centerY)
        mStageChildBackwardBottomLeftLength = getRestLength(path, currentPathLength)

        return path
    }

    private fun getCurrentRevealCircleRadius(input: Float): Int {
        var result = 0
        if (input > 0.44f && input < 0.48f) {
            result = ((input - 0.44f) / 0.04f * mMaxRevealCircleRadius).toInt()
        }

        if (input > 0.81f && input < 0.85f) {
            result = ((input - 0.81f) / 0.04f * mMaxRevealCircleRadius).toInt()
        }

        return result
    }

    private fun getCurrentBackgroundColor(input: Float): Int {
        return if (input < 0.48f || input > 0.85f) mBackgroundColor else mBackgroundDeepColor
    }

    private fun getCurrentOvalColor(input: Float): Int {
        val result: Int

        if (input < 0.5f) {
            result = mOvalColor
        } else if (input < 0.75f) {
            val colorProgress = (input - 0.5f) / 0.2f
            result = evaluateColorChange(colorProgress, mOvalColor, mOvalDeepColor)
        } else if (input < 0.85f) {
            result = mOvalDeepColor
        } else {
            val colorProgress = (input - 0.9f) / 0.1f
            result = evaluateColorChange(colorProgress, mOvalDeepColor, mOvalColor)
        }

        return result
    }

    private fun evaluateColorChange(fraction: Float, startValue: Int, endValue: Int): Int {
        val startA = startValue shr 24 and 0xff
        val startR = startValue shr 16 and 0xff
        val startG = startValue shr 8 and 0xff
        val startB = startValue and 0xff

        val endA = endValue shr 24 and 0xff
        val endR = endValue shr 16 and 0xff
        val endG = endValue shr 8 and 0xff
        val endB = endValue and 0xff

        return startA + (fraction * (endA - startA)).toInt() shl 24 or
                (startR + (fraction * (endR - startR)).toInt() shl 16) or
                (startG + (fraction * (endG - startG)).toInt() shl 8) or
                startB + (fraction * (endB - startB)).toInt()
    }

    private fun getRestLength(path: Path, startD: Float): Float {
        val tempPath = Path()
        val pathMeasure = PathMeasure(path, false)

        pathMeasure.getSegment(startD, pathMeasure.length, tempPath, true)

        pathMeasure.setPath(tempPath, false)

        return pathMeasure.length
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha

    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf

    }

    override fun reset() {}

    private inner class MotherMoveInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            val result: Float

            if (input <= STAGE_MOTHER_FORWARD_TOP_LEFT) {
                result = ACCELERATE_INTERPOLATOR10.getInterpolation(input * 2.941f) / 2.941f
            } else if (input <= STAGE_MOTHER_BACKWARD_TOP_LEFT) {
                result = 0.34f + DECELERATE_INTERPOLATOR10.getInterpolation((input - 0.34f) * 6.25f) / 6.25f
            } else if (input <= STAGE_MOTHER_FORWARD_BOTTOM_LEFT) {
                result = 0.5f + ACCELERATE_INTERPOLATOR03.getInterpolation((input - 0.5f) * 6.666f) / 4.0f
            } else if (input <= STAGE_MOTHER_BACKWARD_BOTTOM_LEFT) {
                result = 0.75f + DECELERATE_INTERPOLATOR03.getInterpolation((input - 0.65f) * 5.46f) / 4.0f
            } else {
                result = 1.0f
            }

            return result
        }
    }

    private inner class ChildMoveInterpolator : Interpolator {

        override fun getInterpolation(input: Float): Float {
            val result: Float

            if (input < STAGE_CHILD_DELAY) {
                return 0.0f
            } else if (input <= STAGE_CHILD_PRE_FORWARD_TOP_LEFT) {
                result = DECELERATE_INTERPOLATOR10.getInterpolation((input - 0.1f) * 6.25f) / 3.846f
            } else if (input <= STAGE_CHILD_FORWARD_TOP_LEFT) {
                result = 0.26f + ACCELERATE_INTERPOLATOR10.getInterpolation((input - 0.26f) * 12.5f) / 12.5f
            } else if (input <= STAGE_CHILD_PRE_BACKWARD_TOP_LEFT) {
                result = 0.34f + DECELERATE_INTERPOLATOR08.getInterpolation((input - 0.34f) * 12.5f) / 12.5f
            } else if (input <= STAGE_CHILD_BACKWARD_TOP_LEFT) {
                result = 0.42f + ACCELERATE_INTERPOLATOR08.getInterpolation((input - 0.42f) * 12.5f) / 12.5f
            } else if (input <= STAGE_CHILD_FORWARD_BOTTOM_LEFT) {
                result = 0.5f + DECELERATE_INTERPOLATOR05.getInterpolation((input - 0.5f) * 5.0f) / 5.0f
            } else if (input <= STAGE_CHILD_BACKWARD_BOTTOM_LEFT) {
                result = 0.7f + ACCELERATE_INTERPOLATOR05.getInterpolation((input - 0.7f) * 5.0f) / 3.33f
            } else {
                result = 1.0f
            }

            return result
        }
    }

    class Builder(private val mContext: Context) {

        fun build(): CircleBroodLoadingRenderer {
            return CircleBroodLoadingRenderer(mContext)
        }
    }
}
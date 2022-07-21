package com.cabe.app.novel.widget

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import kotlin.math.max

/**
 * 作者：沈建芳 on 2018/7/6 22:15
 */
class FlowRadioGroup @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null): RadioGroup(context, attrs) {
    private var mAllViews: MutableList<List<View>>? = null //保存所有行的所有View
    var mLineHeight: MutableList<Int>? = null //保存每一行的行高

    private fun init() {
        orientation = HORIZONTAL
    }

    /** 获取选中按钮的索引,从开始, 未选中返回 -1 */
    private val checkedRadioButtonIndex: Int
        get() = indexOfChild(findViewById(checkedRadioButtonId))

    /** 获取选中按钮的文本,未选中 返回 空字符串 */
    val checkedRadioButtonText: String
        get() = if (checkedRadioButtonId == -1) {
            ""
        } else (findViewById<View>(checkedRadioButtonId) as RadioButton).text.toString()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        var width = 0
        var height = 0
        var lineWidth = 0
        var lineHeight = 0
        var childWidth: Int
        var childHeight: Int
        mAllViews = ArrayList()
        mLineHeight = ArrayList()
        var lineViews: MutableList<View> = ArrayList()
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            val params = child.layoutParams as LinearLayout.LayoutParams
            childWidth = child.measuredWidth + params.leftMargin + params.rightMargin
            childHeight = child.measuredHeight + params.topMargin + params.bottomMargin
            if (lineWidth + childWidth > sizeWidth - paddingLeft - paddingRight) {
                width = max(width, lineWidth)
                height += lineHeight
                mLineHeight?.add(lineHeight)
                mAllViews?.add(lineViews)
                lineWidth = childWidth
                lineHeight = childHeight
                lineViews = ArrayList()
            } else {
                lineWidth += childWidth
                lineHeight = max(childHeight, lineHeight)
            }
            lineViews.add(child)
            if (i == count - 1) {
                width = max(width, lineWidth)
                height += lineHeight
            }
        }
        mLineHeight?.add(lineHeight)
        mAllViews?.add(lineViews)
        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom
        setMeasuredDimension(
            if (modeWidth == MeasureSpec.AT_MOST) width else sizeWidth,
            if (modeHeight == MeasureSpec.AT_MOST) height else sizeHeight
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var top = paddingTop //开始布局子view的 top距离
        var left = paddingLeft //开始布局子view的 left距离
        val lineNum = mAllViews!!.size //行数
        var lineView: List<View>
        var lineHeight: Int
        for (i in 0 until lineNum) {
            lineView = mAllViews!![i]
            lineHeight = mLineHeight!![i]
            for (j in lineView.indices) {
                val child = lineView[j]
                if (child.visibility == GONE) {
                    continue
                }
                val params = child.layoutParams as LinearLayout.LayoutParams
                val ld = left + params.leftMargin
                val td = top + params.topMargin
                val rd = ld + child.measuredWidth //不需要加上 params.rightMargin,
                val bd =
                    td + child.measuredHeight //不需要加上 params.bottomMargin, 因为在 onMeasure , 中已经加在了 lineHeight 中
                child.layout(ld, td, rd, bd)
                left += child.measuredWidth + params.leftMargin + params.rightMargin //因为在 这里添加了;
            }
            left = paddingLeft
            top += lineHeight
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        //        check(getChildAt(0).getId());//默认按钮
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, checkedRadioButtonIndex)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        setCheckedStateForView(ss.checkIndex, true)
    }

    private fun setCheckedStateForView(checkIndex: Int, checked: Boolean) {
        val checkedView = getChildAt(checkIndex)
        if (checkedView != null && checkedView is RadioButton) {
            checkedView.isChecked = checked
        }
    }

    class SavedState : BaseSavedState {
        var checkIndex: Int //选中按钮的索引

        constructor(parcel: Parcelable?, checkIndex: Int) : super(parcel) {
            this.checkIndex = checkIndex
        }

        private constructor(`in`: Parcel) : super(`in`) {
            checkIndex = `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(checkIndex)
        }
    }

    init {
        init()
    }
}
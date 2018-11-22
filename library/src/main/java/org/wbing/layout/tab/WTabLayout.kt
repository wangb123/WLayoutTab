package org.wbing.layout.tab

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.drawable.GradientDrawable
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView


/**
 * https://github.com/jpardogo/PagerSlidingTabStrip
 * 修改
 *
 * @author wangbing
 * @date 2018/8/17
 */
class WTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : HorizontalScrollView(context, attrs, defStyleAttr), ViewPager.OnPageChangeListener {

    /**
     * tab
     */
    private var mTabPadding: Float = 0.toFloat()
    private var mTabSpaceEqual: Boolean = false
    private var mTabWidth: Float = 0.toFloat()
    private var mTabLayout: Int = R.layout.w_tab

    /**
     * Indicator
     */
    private var mIndicatorStyle = STYLE_NORMAL
    private var mIndicatorColor: Int = 0
    private var mIndicatorHeight: Float = 0.toFloat()
    private var mIndicatorWidth: Float = 0.toFloat()
    private var mIndicatorCornerRadius: Float = 0.toFloat()
    private var mIndicatorMarginLeft: Float = 0.toFloat()
    private var mIndicatorMarginTop: Float = 0.toFloat()
    private var mIndicatorMarginRight: Float = 0.toFloat()
    private var mIndicatorMarginBottom: Float = 0.toFloat()
    private var mIndicatorGravity: Int = 0
    private var mIndicatorWidthEqualTitle: Boolean = false

    /**
     * underline
     */
    private var mUnderlineColor: Int = 0
    private var mUnderlineHeight: Float = 0.toFloat()
    private var mUnderlineGravity: Int = 0

    /**
     * divider
     */
    private var mDividerColor: Int = 0
    private var mDividerWidth: Float = 0.toFloat()
    private var mDividerPadding: Float = 0.toFloat()

    /**
     * 文字
     */
    private var mTextSize: Float = 0.toFloat()
    private var mTextSelectColor: Int = 0
    private var mTextUnselectColor: Int = 0
    private var mTextBold: Int = 0
    private var mTextAllCaps: Boolean = false

    /**
     * 临时变量
     */
    private var mLastScrollX: Int = 0
    private var mHeight: Int = 0
    private var mCurrentTab: Int = 0
    private var mCurrentPositionOffset: Float = 0.toFloat()
    private var mSnapOnTabClick: Boolean = false
    private var mTabCount: Int = 0
    /** 用于绘制显示器  */
    private val mIndicatorRect = Rect()
    /** 用于实现滚动居中  */
    private val mTabRect = Rect()

    private val mIndicatorDrawable = GradientDrawable()

    private val mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTrianglePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTrianglePath = Path()

    /**
     * 容器
     */
    private val mTabsContainer: LinearLayout
    private var mViewPager: ViewPager? = null

    /**
     * 回调
     */
    private var mListener: OnTabSelectListener? = null

    init {

        //设置滚动视图是否可以伸缩其内容以填充视口
        isFillViewport = true
        //重写onDraw方法,需要调用这个方法来清除flag
        setWillNotDraw(false)
        clipChildren = false
        clipToPadding = false

        //布局容器
        mTabsContainer = LinearLayout(context)
        addView(mTabsContainer)

        //自定义属性
        obtainAttributes(context, attrs)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isInEditMode || mTabCount <= 0) {
            return
        }

        val height = height
        val paddingLeft = paddingLeft
        // draw divider
        if (mDividerWidth > 0) {
            mDividerPaint.strokeWidth = mDividerWidth
            mDividerPaint.color = mDividerColor
            for (i in 0 until mTabCount - 1) {
                val tab = mTabsContainer.getChildAt(i)
                canvas?.drawLine((paddingLeft + tab.right).toFloat(),
                        mDividerPadding, (paddingLeft + tab.right).toFloat(),
                        height - mDividerPadding,
                        mDividerPaint)
            }
        }

        // draw underline
        if (mUnderlineHeight > 0) {
            mRectPaint.color = mUnderlineColor
            if (mUnderlineGravity == Gravity.BOTTOM) {
                canvas?.drawRect(paddingLeft.toFloat(),
                        height - mUnderlineHeight,
                        (mTabsContainer.width + paddingLeft).toFloat(),
                        height.toFloat(), mRectPaint)
            } else {
                canvas?.drawRect(paddingLeft.toFloat(),
                        0F,
                        (mTabsContainer.width + paddingLeft).toFloat(),
                        mUnderlineHeight,
                        mRectPaint)
            }
        }


        //draw indicator line
        calcIndicatorRect()
        when (mIndicatorStyle) {
            STYLE_TRIANGLE -> {
                if (mIndicatorHeight > 0) {
                    mTrianglePaint.color = mIndicatorColor
                    mTrianglePath.reset()
                    mTrianglePath.moveTo((paddingLeft + mIndicatorRect.left).toFloat(), height.toFloat())
                    mTrianglePath.lineTo((paddingLeft + mIndicatorRect.left / 2 + mIndicatorRect.right / 2).toFloat(), height - mIndicatorHeight)
                    mTrianglePath.lineTo((paddingLeft + mIndicatorRect.right).toFloat(), height.toFloat())
                    mTrianglePath.close()
                    canvas?.drawPath(mTrianglePath, mTrianglePaint)
                }
            }
            STYLE_BLOCK -> {
                if (mIndicatorHeight < 0) {
                    mIndicatorHeight = height - mIndicatorMarginTop - mIndicatorMarginBottom
                } else {

                }

                if (mIndicatorHeight > 0) {
                    if (mIndicatorCornerRadius < 0 || mIndicatorCornerRadius > mIndicatorHeight / 2) {
                        mIndicatorCornerRadius = mIndicatorHeight / 2
                    }

                    mIndicatorDrawable.setColor(mIndicatorColor)
                    mIndicatorDrawable.setBounds((paddingLeft + mIndicatorMarginLeft + mIndicatorRect.left).toInt(),
                            mIndicatorMarginTop.toInt(),
                            ((paddingLeft + mIndicatorRect.right - mIndicatorMarginRight).toInt()),
                            ((mIndicatorMarginTop + mIndicatorHeight).toInt()))
                    mIndicatorDrawable.cornerRadius = mIndicatorCornerRadius
                    mIndicatorDrawable.draw(canvas)
                }
            }
            STYLE_NORMAL -> {
                if (mIndicatorHeight > 0) {
                    mIndicatorDrawable.setColor(mIndicatorColor)

                    if (mIndicatorGravity == Gravity.BOTTOM) {
                        mIndicatorDrawable.setBounds((paddingLeft + mIndicatorMarginLeft + mIndicatorRect.left).toInt(),
                                (height - mIndicatorHeight - mIndicatorMarginBottom).toInt(),
                                (paddingLeft + mIndicatorRect.right - mIndicatorMarginRight).toInt(),
                                (height - mIndicatorMarginBottom).toInt())
                    } else {
                        mIndicatorDrawable.setBounds((paddingLeft + mIndicatorMarginLeft + mIndicatorRect.left).toInt(),
                                mIndicatorMarginTop.toInt(),
                                (paddingLeft + mIndicatorRect.right - mIndicatorMarginRight).toInt(),
                                (mIndicatorHeight + mIndicatorMarginTop).toInt())
                    }
                    mIndicatorDrawable.cornerRadius = mIndicatorCornerRadius
                    mIndicatorDrawable.draw(canvas)
                }
            }
        }

    }

    /**
     * 总定义属性
     */
    private fun obtainAttributes(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WTabLayout)

        mIndicatorStyle = ta.getInt(R.styleable.WTabLayout_w_tab_indicator_style, mIndicatorStyle)

        mIndicatorColor = ta.getColor(R.styleable.WTabLayout_w_tab_indicator_color, Color.parseColor(if (mIndicatorStyle == STYLE_BLOCK) "#4B6A87" else "#ffffff"))
        mIndicatorHeight = ta.getDimension(R.styleable.WTabLayout_w_tab_indicator_height,
                dp2px((if (mIndicatorStyle == STYLE_TRIANGLE) 4 else if (mIndicatorStyle == STYLE_BLOCK) -1 else 2).toFloat()).toFloat())
        mIndicatorWidth = ta.getDimension(R.styleable.WTabLayout_w_tab_indicator_width, dp2px((if (mIndicatorStyle == STYLE_TRIANGLE) 10 else -1).toFloat()).toFloat())
        mIndicatorCornerRadius = ta.getDimension(R.styleable.WTabLayout_w_tab_indicator_corner_radius, dp2px((if (mIndicatorStyle == STYLE_BLOCK) -1 else 0).toFloat()).toFloat())
        mIndicatorMarginLeft = ta.getDimension(R.styleable.WTabLayout_w_tab_indicator_margin_left, dp2px(0f).toFloat())
        mIndicatorMarginTop = ta.getDimension(R.styleable.WTabLayout_w_tab_indicator_margin_top, dp2px((if (mIndicatorStyle == STYLE_BLOCK) 7 else 0).toFloat()).toFloat())
        mIndicatorMarginRight = ta.getDimension(R.styleable.WTabLayout_w_tab_indicator_margin_right, dp2px(0f).toFloat())
        mIndicatorMarginBottom = ta.getDimension(R.styleable.WTabLayout_w_tab_indicator_margin_bottom, dp2px((if (mIndicatorStyle == STYLE_BLOCK) 7 else 0).toFloat()).toFloat())
        mIndicatorGravity = ta.getInt(R.styleable.WTabLayout_w_tab_indicator_gravity, Gravity.BOTTOM)
        mIndicatorWidthEqualTitle = ta.getBoolean(R.styleable.WTabLayout_w_tab_indicator_width_equal_title, false)

        mUnderlineColor = ta.getColor(R.styleable.WTabLayout_w_tab_underline_color, Color.parseColor("#ffffff"))
        mUnderlineHeight = ta.getDimension(R.styleable.WTabLayout_w_tab_underline_height, dp2px(0f).toFloat())
        mUnderlineGravity = ta.getInt(R.styleable.WTabLayout_w_tab_underline_gravity, Gravity.BOTTOM)

        mDividerColor = ta.getColor(R.styleable.WTabLayout_w_tab_divider_color, Color.parseColor("#ffffff"))
        mDividerWidth = ta.getDimension(R.styleable.WTabLayout_w_tab_divider_width, dp2px(0f).toFloat())
        mDividerPadding = ta.getDimension(R.styleable.WTabLayout_w_tab_divider_padding, dp2px(12f).toFloat())

        mTextSize = ta.getDimension(R.styleable.WTabLayout_w_tab_textsize, sp2px(14f).toFloat())
        mTextSelectColor = ta.getColor(R.styleable.WTabLayout_w_tab_textSelectColor, Color.parseColor("#ffffff"))
        mTextUnselectColor = ta.getColor(R.styleable.WTabLayout_w_tab_textUnselectColor, Color.parseColor("#AAffffff"))
        mTextBold = ta.getInt(R.styleable.WTabLayout_w_tab_textBold, TEXT_BOLD_NONE)
        mTextAllCaps = ta.getBoolean(R.styleable.WTabLayout_w_tab_textAllCaps, false)

        mTabSpaceEqual = ta.getBoolean(R.styleable.WTabLayout_w_tab_tab_space_equal, false)
        mTabWidth = ta.getDimension(R.styleable.WTabLayout_w_tab_tab_width, dp2px(-1f).toFloat())
        mTabPadding = ta.getDimension(R.styleable.WTabLayout_w_tab_tab_padding, (if (mTabSpaceEqual || mTabWidth > 0) dp2px(0f) else dp2px(20f)).toFloat())
        mTabLayout = ta.getResourceId(R.styleable.WTabLayout_w_tab_tab_layout, mTabLayout)

        mSnapOnTabClick = ta.getBoolean(R.styleable.WTabLayout_w_tab_smoothScroll, false)

        ta.recycle()
    }


    /**
     * viewpager滚动时候调用
     */
    override fun onPageScrolled(i: Int, v: Float, i1: Int) {
        mCurrentTab = i
        this.mCurrentPositionOffset = v
        scrollToCurrentTab()
        invalidate()
    }

    /**
     * 页面切换完成的时候调用
     */
    override fun onPageSelected(i: Int) {
        updateTabSelection(i)
    }

    /**
     * 页面状态切换
     */
    override fun onPageScrollStateChanged(i: Int) {

    }

    /**
     * 关联viewpager
     *
     * @param viewPager
     */
    fun attachViewPager(viewPager: ViewPager?) {
        if (viewPager == null || viewPager.adapter == null) {
            throw IllegalStateException("ViewPager or ViewPager adapter can not be NULL !")
        }

        if (this.mViewPager === viewPager) {
            return
        }
        this.mViewPager = viewPager
        this.mViewPager!!.removeOnPageChangeListener(this)
        this.mViewPager!!.addOnPageChangeListener(this)
        notifyDataSetChanged()
    }


    //setter and getter
    fun setCurrentTab(currentTab: Int) {
        setCurrentTab(currentTab, true)
    }

    fun setCurrentTab(currentTab: Int, smoothScroll: Boolean) {
        this.mCurrentTab = currentTab
        mViewPager?.setCurrentItem(currentTab, smoothScroll)
    }

    fun setIndicatorStyle(indicatorStyle: Int) {
        this.mIndicatorStyle = indicatorStyle
        invalidate()
    }

    fun setTabPadding(tabPadding: Float) {
        this.mTabPadding = dp2px(tabPadding).toFloat()
        updateTabStyles()
    }

    fun setTabSpaceEqual(tabSpaceEqual: Boolean) {
        this.mTabSpaceEqual = tabSpaceEqual
        updateTabStyles()
    }

    fun setTabWidth(tabWidth: Float) {
        this.mTabWidth = dp2px(tabWidth).toFloat()
        updateTabStyles()
    }

    fun setIndicatorColor(indicatorColor: Int) {
        this.mIndicatorColor = indicatorColor
        invalidate()
    }

    fun setIndicatorHeight(indicatorHeight: Float) {
        this.mIndicatorHeight = dp2px(indicatorHeight).toFloat()
        invalidate()
    }

    fun setIndicatorWidth(indicatorWidth: Float) {
        this.mIndicatorWidth = dp2px(indicatorWidth).toFloat()
        invalidate()
    }

    fun setIndicatorCornerRadius(indicatorCornerRadius: Float) {
        this.mIndicatorCornerRadius = dp2px(indicatorCornerRadius).toFloat()
        invalidate()
    }

    fun setIndicatorGravity(indicatorGravity: Int) {
        this.mIndicatorGravity = indicatorGravity
        invalidate()
    }

    fun setIndicatorMargin(indicatorMarginLeft: Float, indicatorMarginTop: Float,
                           indicatorMarginRight: Float, indicatorMarginBottom: Float) {
        this.mIndicatorMarginLeft = dp2px(indicatorMarginLeft).toFloat()
        this.mIndicatorMarginTop = dp2px(indicatorMarginTop).toFloat()
        this.mIndicatorMarginRight = dp2px(indicatorMarginRight).toFloat()
        this.mIndicatorMarginBottom = dp2px(indicatorMarginBottom).toFloat()
        invalidate()
    }

    fun setIndicatorWidthEqualTitle(indicatorWidthEqualTitle: Boolean) {
        this.mIndicatorWidthEqualTitle = indicatorWidthEqualTitle
        invalidate()
    }

    fun setUnderlineColor(underlineColor: Int) {
        this.mUnderlineColor = underlineColor
        invalidate()
    }

    fun setUnderlineHeight(underlineHeight: Float) {
        this.mUnderlineHeight = dp2px(underlineHeight).toFloat()
        invalidate()
    }

    fun setUnderlineGravity(underlineGravity: Int) {
        this.mUnderlineGravity = underlineGravity
        invalidate()
    }

    fun setDividerColor(dividerColor: Int) {
        this.mDividerColor = dividerColor
        invalidate()
    }

    fun setDividerWidth(dividerWidth: Float) {
        this.mDividerWidth = dp2px(dividerWidth).toFloat()
        invalidate()
    }

    fun setDividerPadding(dividerPadding: Float) {
        this.mDividerPadding = dp2px(dividerPadding).toFloat()
        invalidate()
    }

    fun setTextsize(textsize: Float) {
        this.mTextSize = sp2px(textsize).toFloat()
        updateTabStyles()
    }

    fun setTextSelectColor(textSelectColor: Int) {
        this.mTextSelectColor = textSelectColor
        updateTabStyles()
    }

    fun setTextUnselectColor(textUnselectColor: Int) {
        this.mTextUnselectColor = textUnselectColor
        updateTabStyles()
    }

    fun setTextBold(textBold: Int) {
        this.mTextBold = textBold
        updateTabStyles()
    }

    fun setTextAllCaps(textAllCaps: Boolean) {
        this.mTextAllCaps = textAllCaps
        updateTabStyles()
    }

    fun setSnapOnTabClick(snapOnTabClick: Boolean) {
        mSnapOnTabClick = snapOnTabClick
    }


    fun getTabCount(): Int {
        return mTabCount
    }

    fun getCurrentTab(): Int {
        return mCurrentTab
    }

    fun getIndicatorStyle(): Int {
        return mIndicatorStyle
    }

    fun getTabPadding(): Float {
        return mTabPadding
    }

    fun isTabSpaceEqual(): Boolean {
        return mTabSpaceEqual
    }

    fun getTabWidth(): Float {
        return mTabWidth
    }

    fun getIndicatorColor(): Int {
        return mIndicatorColor
    }

    fun getIndicatorHeight(): Float {
        return mIndicatorHeight
    }

    fun getIndicatorWidth(): Float {
        return mIndicatorWidth
    }

    fun getIndicatorCornerRadius(): Float {
        return mIndicatorCornerRadius
    }

    fun getIndicatorMarginLeft(): Float {
        return mIndicatorMarginLeft
    }

    fun getIndicatorMarginTop(): Float {
        return mIndicatorMarginTop
    }

    fun getIndicatorMarginRight(): Float {
        return mIndicatorMarginRight
    }

    fun getIndicatorMarginBottom(): Float {
        return mIndicatorMarginBottom
    }

    fun getUnderlineColor(): Int {
        return mUnderlineColor
    }

    fun getUnderlineHeight(): Float {
        return mUnderlineHeight
    }

    fun getDividerColor(): Int {
        return mDividerColor
    }

    fun getDividerWidth(): Float {
        return mDividerWidth
    }

    fun getDividerPadding(): Float {
        return mDividerPadding
    }

    fun getTextsize(): Float {
        return mTextSize
    }

    fun getTextSelectColor(): Int {
        return mTextSelectColor
    }

    fun getTextUnselectColor(): Int {
        return mTextUnselectColor
    }

    fun getTextBold(): Int {
        return mTextBold
    }

    fun isTextAllCaps(): Boolean {
        return mTextAllCaps
    }

    fun getTitleView(tab: Int): TextView {
        val tabView = mTabsContainer.getChildAt(tab)
        return tabView.findViewById<TextView>(R.id.tab_title)
    }

    /**
     * 更新数据
     */
    private fun notifyDataSetChanged() {
        mTabsContainer.removeAllViews()
        this.mTabCount = mViewPager?.adapter?.count!!
        var i = 0
        while (i < mTabCount) {
            val tabView = View.inflate(context, mTabLayout, null)
            val pageTitle = mViewPager!!.adapter!!.getPageTitle(i)
            addTab(i, pageTitle, tabView)
            i++
        }
        //更新tab
        updateTabStyles()
    }

    /**
     * 更新样式
     */
    private fun updateTabStyles() {
        var i = 0
        while (i < mTabCount) {
            val v = mTabsContainer.getChildAt(i)
            val tabTitle = v?.findViewById<TextView>(R.id.tab_title)
            tabTitle?.setTextColor(if (i == mCurrentTab) mTextSelectColor else mTextUnselectColor)
            tabTitle?.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
            tabTitle?.setPadding(mTabPadding.toInt(), 0, mTabPadding.toInt(), 0)
            if (mTextAllCaps) {
                tabTitle?.text = tabTitle?.text.toString().toUpperCase()
            }

            if (mTextBold == TEXT_BOLD_BOTH) {
                tabTitle?.paint?.isFakeBoldText = true
            } else if (mTextBold == TEXT_BOLD_NONE) {
                tabTitle?.paint?.isFakeBoldText = false
            }
            i++
        }
    }

    /**
     *添加tab
     */
    private fun addTab(position: Int, title: CharSequence, tabView: View) {
        val tabTitle = tabView.findViewById<TextView>(R.id.tab_title)
        tabTitle?.text = title
        tabView.setOnClickListener {
            val index = mTabsContainer.indexOfChild(it)
            if (index == -1) {
                mListener?.onTabReselect(index)
            } else {
                if (mSnapOnTabClick) {
                    mViewPager?.setCurrentItem(position, false)
                } else {
                    mViewPager?.currentItem = position
                }
                mListener?.onTabSelect(position)
            }
        }
        Log.e("TAG", "" + mTabWidth + mTabSpaceEqual)
        //每一个Tab的布局参数
        val tabParams =
                when {
                    mTabWidth > 10 -> LinearLayout.LayoutParams(mTabWidth.toInt(), FrameLayout.LayoutParams.MATCH_PARENT)
                    mTabSpaceEqual -> LinearLayout.LayoutParams(0, FrameLayout.LayoutParams.MATCH_PARENT, 1.0f)
                    else -> LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT)
                }

        mTabsContainer.addView(tabView, position, tabParams)
    }

    /**
     * 滑动到当前的tab
     */
    private fun scrollToCurrentTab() {
        if (mTabCount <= 0) {
            return
        }

        val offset = (mCurrentPositionOffset * mTabsContainer.getChildAt(mCurrentTab).width).toInt()
        //当前Tab的left+当前Tab的Width乘以positionOffset
        var newScrollX = mTabsContainer.getChildAt(mCurrentTab).left + offset

        if (mCurrentTab > 0 || offset > 0) {
            //HorizontalScrollView移动到当前tab,并居中
            newScrollX -= width / 2 - paddingLeft
            calcIndicatorRect()
            newScrollX += (mTabRect.right - mTabRect.left) / 2
        }

        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX
            /** scrollTo（int x,int y）:x,y代表的不是坐标点,而是偏移量
             * x:表示离起始位置的x水平方向的偏移量
             * y:表示离起始位置的y垂直方向的偏移量
             */
            scrollTo(newScrollX, 0)
        }
    }

    /**
     * 更新tab的选中状态
     */
    private fun updateTabSelection(position: Int) {
        for (i in 0 until mTabCount) {
            val tabView = mTabsContainer.getChildAt(i)
            val isSelect = i == position
            val tabTitle = tabView.findViewById<TextView>(R.id.tab_title)

            tabTitle.setTextColor(if (isSelect) mTextSelectColor else mTextUnselectColor)
            if (mTextBold == TEXT_BOLD_WHEN_SELECT) {
                tabTitle.paint.isFakeBoldText = isSelect
            }
        }
    }

    // show MsgTipView
    private val mTextPaint = Paint(ANTI_ALIAS_FLAG)
    private var margin: Float = 0.toFloat()

    private fun calcIndicatorRect() {
        val currentTabView = mTabsContainer.getChildAt(this.mCurrentTab)
        var left = currentTabView?.left?.toFloat()
        var right = currentTabView?.right?.toFloat()

        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            val tabTitle = currentTabView.findViewById<TextView>(R.id.tab_title)
            tabTitle.textSize = mTextSize
            mTextPaint.textSize = mTextSize
            val textWidth = mTextPaint.measureText(tabTitle.text.toString())
            margin = (right!! - left!! - textWidth) / 2
        }
        if (this.mCurrentTab < mTabCount - 1) {
            val nextTabView = mTabsContainer.getChildAt(this.mCurrentTab + 1)
            val nextTabLeft = nextTabView.left.toFloat()
            val nextTabRight = nextTabView.right.toFloat()

            left = left!! + mCurrentPositionOffset * (nextTabLeft - left)
            right = right!! + mCurrentPositionOffset * (nextTabRight - right)

            //for mIndicatorWidthEqualTitle
            if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
                val tabTitle = currentTabView.findViewById<TextView>(R.id.tab_title)
                mTextPaint.textSize = mTextSize
                val nextTextWidth = mTextPaint.measureText(tabTitle.text.toString())
                val nextMargin = (nextTabRight - nextTabLeft - nextTextWidth) / 2
                margin += mCurrentPositionOffset * (nextMargin - margin)
            }
        }

        mIndicatorRect.left = left!!.toInt()
        mIndicatorRect.right = right!!.toInt()
        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            mIndicatorRect.left = (left + margin - 1).toInt()
            mIndicatorRect.right = (right - margin - 1).toInt()
        }

        mTabRect.left = left.toInt()
        mTabRect.right = right.toInt()

        if (mIndicatorWidth < 0) {   //indicatorWidth小于0时,原jpardogo's PagerSlidingTabStrip

        } else {//indicatorWidth大于0时,圆角矩形以及三角形
            var indicatorLeft = currentTabView.left + (currentTabView.width - mIndicatorWidth) / 2

            if (this.mCurrentTab < mTabCount - 1) {
                val nextTab = mTabsContainer.getChildAt(this.mCurrentTab + 1)
                indicatorLeft += mCurrentPositionOffset * (currentTabView.width / 2 + nextTab.width / 2)
            }

            mIndicatorRect.left = indicatorLeft.toInt()
            mIndicatorRect.right = (mIndicatorRect.left + mIndicatorWidth).toInt()
        }
    }

    private fun dp2px(dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun sp2px(sp: Float): Int {
        val scale = resources.displayMetrics.scaledDensity
        return (sp * scale + 0.5f).toInt()
    }

    companion object {

        /**
         * Indicator
         */
        private const val STYLE_NORMAL = 0
        private const val STYLE_TRIANGLE = 1
        private const val STYLE_BLOCK = 2

        /**
         * title
         */
        private const val TEXT_BOLD_NONE = 0
        private const val TEXT_BOLD_WHEN_SELECT = 1
        private const val TEXT_BOLD_BOTH = 2
    }
}


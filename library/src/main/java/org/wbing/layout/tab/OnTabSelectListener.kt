package org.wbing.layout.tab

/**
 * @author wangbing
 * @date 2018/8/17
 */

interface OnTabSelectListener {
    fun onTabSelect(position: Int)
    fun onTabReselect(position: Int)
}
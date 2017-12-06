package com.mercury.uiinstance;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class FlowLayout extends ViewGroup {

    private int horizontalSpacing = 15;//水平间距
    private int verticalSpacing   = 15;//行与行之间的垂直间距

    //用来存放所有的Line对象
    private ArrayList<Line> lineList = new ArrayList<Line>();

    public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context) {
        super(context);
    }

    /**
     * 设置水平间距
     *
     * @param horizontalSpacing
     */
    public void setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
    }

    /**
     * 设置垂直间距
     *
     * @param verticalSpacing
     */
    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
    }

    /**
     * 分行：遍历所有的子View，判断哪几个子View在同一行(排座位表)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        lineList.clear();

        //1.获取FlowLayout的宽度
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //2.获取用于实际比较的宽度，就是除去2边的padding的宽度
        int noPaddingWidth = width - getPaddingLeft() - getPaddingRight();

        //3.遍历所有的子View，拿子View的宽和noPaddingWidth进行比较
        Line line = new Line();//准备Line对象
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            childView.measure(0, 0);//保证能够获取到宽高

            //4.如果当前line中没有子View，则直接存入，因为要保证每行至少有一个子View;
            if (line.getViewList().size() == 0) {
                line.addLineView(childView);//直接存入
            } else if (line.getLineWidth() + horizontalSpacing + childView.getMeasuredWidth() >
                    noPaddingWidth) {
                //5.如果当前line的宽+水平间距+子View的宽大于noPaddingWidth,则child需要换行
                //要先存放之前的line对象，否则会缺失View
                lineList.add(line);

                line = new Line();//创建新的Line
                line.addLineView(childView);//将当前child放入新的行中
            } else {
                //6.说明当前child应该放入当前Line中
                line.addLineView(childView);
            }

            //7.如果当前child是最后的子View，那么需要保存最后的line对象
            if (i == (getChildCount() - 1)) {
                lineList.add(line);//保存最后的Line
            }
        }

        //循环结束，lineList存放了所有的Line，而每个Line又记录了自己行所有的View;
        //现在计算FLowLayout需要的高度
        int height = getPaddingTop() + getPaddingBottom();//先计算上下的padding值
        for (int i = 0; i < lineList.size(); i++) {
            height += lineList.get(i).getLineHeight();//再加上所有行的高度
        }
        height += (lineList.size() - 1) * verticalSpacing;//最后加上所有的行间距

        //设置当前控件的宽高，或者向父View申请宽高
        setMeasuredDimension(width, height);
    }

    /**
     * 去摆放所有的子View，让每个人真正的坐到自己的位置上
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        for (int i = 0; i < lineList.size(); i++) {
            Line line = lineList.get(i);//获取Line对象

            //从第二行开始，每行的top总是比上一行的top多一个行高和垂直间距
            if (i > 0) {
                paddingTop += verticalSpacing + lineList.get(i - 1).getLineHeight();
            }

            ArrayList<View> viewList = line.getViewList();//获取line所包含的所有view的集合

            //1.获取每行的留白的宽度
            int remainSpacing = getLineRemainSpacing(line);
            //2.计算每个view平均得到的值
            float perSpacing = remainSpacing / viewList.size();

            for (int j = 0; j < viewList.size(); j++) {
                View childView = viewList.get(j);
                //3.将得到的perSpacing增加到view的宽度上面
                int widthSpec = MeasureSpec.makeMeasureSpec((int) (childView.getMeasuredWidth() +
						perSpacing), MeasureSpec.EXACTLY);
                childView.measure(widthSpec, 0);

                if (j == 0) {
                    //如果是每行的第一行，name直接靠左边摆放
                    childView.layout(paddingLeft, paddingTop, paddingLeft + childView
									.getMeasuredWidth(),
                            paddingTop + childView.getMeasuredHeight());
                } else {
                    //如果不是第一个，需要参考前一个view的right
                    View preView = viewList.get(j - 1);
                    //当前view的left是前一个view的right+水平间距
                    int left = preView.getRight() + horizontalSpacing;
                    childView.layout(left, preView.getTop(), left + childView.getMeasuredWidth(),
							preView.getBottom());
                }
            }
        }
    }

    /**
     *
     * @param   line
     * @return  获取指定Line的留白
     */
    private int getLineRemainSpacing(Line line) {
        return getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - line.getLineWidth();
    }

    /**
     * 封装每行的数据，包括所有的子View，行的宽高。每一个Line对象就代表一行
     *
     * @author Administrator
     */
    private class Line {
        private ArrayList<View> viewList;//用来存放当前行所有的子View
        private int             width;//表示所有子View的宽+水平间距
        private int             height;//行的高度

        private Line() {
            viewList = new ArrayList<View>();
        }

        /**
         * 记录子View
         *
         * @param child
         */
        private void addLineView(View child) {
            if (!viewList.contains(child)) {
                viewList.add(child);

                //1.更新Line的width
                if (viewList.size() == 1) {
                    //说明添加的是第一个子View，那么line的宽就是子View的宽度
                    width = child.getMeasuredWidth();
                } else {
                    //如果添加的不是第一个子View，那么应该加等于水平间距和子View的宽度
                    width += child.getMeasuredWidth() + horizontalSpacing;
                }
                //2.更新line的height
                height = Math.max(height, child.getMeasuredHeight());
            }
        }

        /**
         *
         * @return  获取当前行的宽度
         */
        private int getLineWidth() {
            return width;
        }

        /**
         *
         * @return  当前行的高度
         */
        private int getLineHeight() {
            return height;
        }

        /**
         *
         * @return  获取当前行的所有的子View的集合
         */
        private ArrayList<View> getViewList() {
            return viewList;
        }
    }

}
package org.techtown.withotilla2.RecyclerviewOtilla;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public abstract class EditAdapter<ContentData> extends RecyclerView.Adapter {

    protected Context mContext;
    protected ArrayList<ContentData> mList;

    private boolean isEdit;
    private List<EditLayout> allItems = new ArrayList<>();
    private EditLayout mRightOpenItem;

    public EditAdapter(Context context)
    {
        this.mContext = context;
        this.mList = new ArrayList<>();
    }

    public EditAdapter(Context context, ArrayList<ContentData> List) {
        this.mContext = context;
        this.mList = List;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateEditViewHolder(parent, viewType);
    }

    public abstract EditViewHolder onCreateEditViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final EditViewHolder viewHolder = (EditViewHolder) holder;
        final EditLayout editLayout = viewHolder.editLayout;

        if (!allItems.contains(editLayout)) {
            allItems.add(editLayout);
        }

        editLayout.setEdit(isEdit);

        onBindEditViewHolder(viewHolder, position);

        viewHolder.vPreDelete.setOnTouchListener(new View.OnTouchListener() {

            @Override
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (isEdit && mRightOpenItem != null) {
                            mRightOpenItem.openLeft();
                        } else {
                            editLayout.openRight();
                        }
                }
                return true;
            }
        });

        viewHolder.vDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0) {
                    mList.remove(position);
                    mRightOpenItem = null;
                    notifyItemRemoved(position);
                    if (position != mList.size()) {
                        notifyItemRangeChanged(position, mList.size() - position);
                    }
                }
            }
        });

        viewHolder.vSort.setOnTouchListener(new View.OnTouchListener() {

            @Override
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isEdit && mRightOpenItem != null) {
                    mRightOpenItem.openLeft();
                } else {
                    mOnItemSortListener.onStartDrags(viewHolder);
                }
                return false;
            }
        });

        editLayout.setOnDragStateChangeListener(new EditLayout.OnStateChangeListener() {

            @Override
            public void onLeftOpen(EditLayout layout) {
                if (mRightOpenItem == layout) {
                    mRightOpenItem = null;
                }
            }

            @Override
            public void onRightOpen(EditLayout layout) {
                if (mRightOpenItem != layout) {
                    mRightOpenItem = layout;
                }
            }

            @Override
            public void onClose(EditLayout layout) {
                if (mRightOpenItem == layout) {
                    mRightOpenItem = null;
                }
            }
        });

    }

    public abstract void onBindEditViewHolder(EditViewHolder holder, int position);

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * 设置编辑状态
     *
     * @param isEdit 是否为编辑状态
     */
    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
        if (isEdit) {
            openLeftAll();
        } else {
            closeAll();
        }
        for (EditLayout editLayout : allItems) {
            editLayout.setEdit(isEdit);
        }
    }

    private EditLayout.OnItemSortListener mOnItemSortListener;

    public void setOnItemSortListener(EditLayout.OnItemSortListener onItemSortListener) {
        mOnItemSortListener = onItemSortListener;
    }

    /**
     * 关闭所有 item
     */
    private void closeAll() {
        for (EditLayout editLayout : allItems) {
            editLayout.close();
        }
    }

    /**
     * 将所有 item 向左展开
     */
    private void openLeftAll() {
        for (EditLayout editLayout : allItems) {
            editLayout.openLeft();
        }
    }

    /**
     * 获取编辑状态
     *
     * @return 是否为编辑状态
     */
    public boolean isEdit() {
        return isEdit;
    }

    /**
     * 获取向右展开的 item
     *
     * @return 向右展开的 item
     */
    public EditLayout getRightOpenItem() {
        return mRightOpenItem;
    }

    public List<ContentData> getList() {
        return mList;
    }
}
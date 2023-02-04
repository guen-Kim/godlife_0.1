package org.techtown.withotilla2.RecyclerviewOtilla;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;


public class EditRecyclerView extends RecyclerView implements org.techtown.withotilla2.RecyclerviewOtilla.EditLayout.OnItemSortListener {

    private boolean isEdit;             //是否为编辑状态
    private EditLayout rightOpenItem;   //向右展开项
    private ItemTouchHelper mItemTouchHelper;

    public EditRecyclerView(Context context) {
        this(context, null);
    }

    public EditRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setItemAnimator(new DefaultItemAnimator());
        mItemTouchHelper = new ItemTouchHelper(new EditTouchHelperCallback(this));
        mItemTouchHelper.attachToRecyclerView(this);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (getAdapter() instanceof EditAdapter) {
                    EditAdapter adapter = (EditAdapter) getAdapter();
                    rightOpenItem = adapter.getRightOpenItem();
                    isEdit = adapter.isEdit();
                }
                if (isEdit && rightOpenItem != null) {
                    rightOpenItem.openLeft();
                }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter instanceof EditAdapter) {
            ((EditAdapter) adapter).setOnItemSortListener(this);
        }
    }

    @Override
    public void onStartDrags(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        EditAdapter mAdapter = (EditAdapter) getAdapter();
        Collections.swap(mAdapter.getList(), fromPosition, toPosition);
        mAdapter.notifyItemMoved(fromPosition, toPosition);
    }
}
package org.techtown.withotilla2.RecyclerviewOtilla;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class EditViewHolder extends RecyclerView.ViewHolder {
    public EditLayout editLayout;
    public View vImage;
    public View vContent;
    public View vPreDelete;
    public View vDelete;
    public View vSort;

    public EditViewHolder(View itemView) {
        super(itemView);
        editLayout = setEditLayout(itemView);
        vImage = setImage(itemView);
        vContent = setContent(itemView);
        vPreDelete = setPreDelete(itemView);
        vDelete = setDelete(itemView);
        vSort = setSort(itemView);
    }

    public abstract EditLayout setEditLayout(View itemView);

    public abstract View setContent(View itemView);

    public abstract View setImage(View itemView);

    public abstract View setPreDelete(View itemView);

    public abstract View setDelete(View itemView);

    public abstract View setSort(View itemView);

}
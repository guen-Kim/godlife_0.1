package org.techtown.withotilla2.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import org.techtown.withotilla2.R;
import java.util.ArrayList;
import java.util.List;

public class viewPagerAdapter extends RecyclerView.Adapter<viewPagerAdapter.viewPagerViewHolder> {

    private List<String> imageList = new ArrayList<String>();
    private Context ctx; //for Gild



    public viewPagerAdapter(Context ctx, List<String> imageList) {
        this.ctx = ctx;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public viewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new viewPagerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_image, parent, false));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull viewPagerViewHolder holder, int position) {
        //Glide는 url 이미지 빠르게

        Glide.with(ctx)//액티비티 내용
                .load(imageList.get(position))
                .apply(new RequestOptions())
                .placeholder(R.drawable.godlife)// 디폴트 이미지
                .centerCrop()   // 이미지 중앙 중심으로
                .dontTransform() //이미지에 변형 x
                .transition(new DrawableTransitionOptions().crossFade(1000))//fade 효과
                .into(holder.ivdetail);
    }

    public class viewPagerViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivdetail;


        public viewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivdetail = itemView.findViewById(R.id.ivdetail);


        }

    }

}

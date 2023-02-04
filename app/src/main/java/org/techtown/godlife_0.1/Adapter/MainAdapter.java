package org.techtown.withotilla2.Adapter;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.techtown.withotilla2.DataClass.ItemData;
import org.techtown.withotilla2.DetailActivity;
import org.techtown.withotilla2.R;
import org.techtown.withotilla2.Util.AgmPrefer;

import java.util.ArrayList;

public class MainAdapter  extends RecyclerView.Adapter {


    ArrayList<ItemData> al;
    Context ctx; //for Glid
    AgmPrefer ap;

    // del 이벤트 로직을 위한 클릭 이벤트 인터페이스 정의
    public interface OnContentClick
    {
        void ContextClick(int menuid, String idx);
    }

    // del 인터페이스 선언
    OnContentClick onContentClickListener;

    public void setOnContentClick(OnContentClick listener)
    {
        this.onContentClickListener = listener;
    }


    public MainAdapter(Context ctx, ArrayList<ItemData> al) {
        this.ctx = ctx;
        this.al = al;
        ap = new AgmPrefer(ctx);
    }


    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {

        CardView rowContainer;
        ImageView itemProfile;
        TextView itemTitle;
        ImageView  itemImage;
        TextView itemReply;
        ImageButton itemMore;
        TextView itemCount;
        TextView itemDate;
        TextView itemSummary;
        TextView itemmy;


        public ListItemViewHolder(@NonNull View itemView) {
            super(itemView);

            rowContainer = (CardView) itemView.findViewById(R.id.rowContainer);
            itemProfile = (ImageView) itemView.findViewById(R.id.itemProfile);
            itemTitle = (TextView) itemView.findViewById(R.id.itemTitle);
            itemSummary = (TextView) itemView.findViewById(R.id.itemSummary);
            itemImage = (ImageView) itemView.findViewById(R.id.itemImage);
            itemReply = (TextView) itemView.findViewById(R.id.itemReply);
            itemMore = (ImageButton) itemView.findViewById(R.id.itemMore);
            itemCount = (TextView) itemView.findViewById(R.id.itemCount);
            itemDate = (TextView) itemView.findViewById(R.id.itemDate);
            itemmy = (TextView) itemView.findViewById(R.id.itemmy);
        }
    }


    @NonNull
    @Override
    public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_row, parent, false);

        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ListItemViewHolder iviewholder = (ListItemViewHolder) holder;
        Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.slide_in_to_left);
        ((ListItemViewHolder) holder).rowContainer.setAnimation(animation);


        //게시판 식별자
        ItemData idata = al.get(position);

        iviewholder.itemTitle.setText(idata.title);

        iviewholder.rowContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FirebaseAuth.getInstance().getCurrentUser() != null)
                {
                    // 상세로 이동
                    ShowDetailActivity(idata.idx);

                }
                else
                {
                    // -adapter의 초기화 시킨 ctx 넣음
                    Toast.makeText(ctx,"로그인 해주세요!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        iviewholder.itemReply.setText(String.valueOf(idata.reply));
        iviewholder.itemCount.setText(String.valueOf(idata.fire) + " fire");
        iviewholder.itemDate.setText(idata.reg_date);

        Glide.with(ctx)//액티비티 내용
                .load(idata.profile)
                .apply(new RequestOptions())
                .placeholder(R.drawable.nonperson)// 디폴트 이미지
                .centerCrop()   // 이미지 중앙 중심으로
                .dontTransform() //이미지에 변형 x
                .transition(new DrawableTransitionOptions().crossFade(1000))//fade 효과
                .into(iviewholder.itemProfile);



        //이미지가 있는 경우
        if(!idata.image.isEmpty() && idata.image.split(",").length>0)
        {

            Glide.with(ctx)//액티비티 내용
                    .load(idata.image.split(",")[0])
                    .apply(new RequestOptions())
                    .placeholder(R.drawable.godlife)// 디폴트 이미지
                    .centerCrop()   // 이미지 중앙 중심으로
                    .dontTransform() //이미지에 변형 x
                    .transition(new DrawableTransitionOptions().crossFade(1000))//fade 효과
                    .into(iviewholder.itemImage);
            iviewholder.itemSummary.setVisibility(View.GONE);
            iviewholder.itemImage.setVisibility(View.VISIBLE);
        }
        else // 이미지가 없는 경우, 글만 보이도록
        {
            iviewholder.itemSummary.setText(idata.summary);
            iviewholder.itemSummary.setVisibility(View.VISIBLE);
            iviewholder.itemImage.setVisibility(View.GONE);
        }

        // 내 글, 삭제버튼 visible  검사 .
        if(idata.email.equals(ap.getEmail()))
        {
            iviewholder.itemmy.setVisibility(View.VISIBLE);
            iviewholder.itemMore.setVisibility(View.VISIBLE);

            iviewholder.itemMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 여기서 contextmenu
                    PopupMenu popupMenu = new PopupMenu(ctx, iviewholder.itemMore);
                    popupMenu.inflate(R.menu.list_contextmenu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.conItem_del:
                                    //삭제
                                    new AlertDialog.Builder(ctx).setMessage("정말 삭제하겠습니까?")
                                            .setCancelable(false) // 안드로이드 백버튼 할때 닫지(false) 않음.
                                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //삭제 처리
                                                    if(onContentClickListener != null)
                                                    {

                                                        // 오 이런식으로 쓸 수가 있네 인프레이션 이미 시켜서 그런가?
                                                        onContentClickListener.ContextClick(R.id.conItem_del, idata.idx);
                                                        removeItem(iviewholder.getAdapterPosition());
                                                    }

                                                }
                                            })
                                    .setNegativeButton("아니오",null)
                                    .show();
                                    break;
                                default:
                                    break;
                            }

                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });


        }
    }


    @Override
    public int getItemCount() {
        return al.size();
    }

    public void addItem(ItemData item){
        this.al.add(0,item);
        notifyItemInserted(0);
    }

    public void removeItem(int position){
        if (position >= 0) {
            al.remove(position);
            notifyItemRemoved(position);
        }
    }



    private void ShowDetailActivity(String board_idx)
    {
        Intent intent = new Intent(ctx, DetailActivity.class);
        intent.putExtra("board_idx",board_idx);
        ctx.startActivity(intent);

    }
}

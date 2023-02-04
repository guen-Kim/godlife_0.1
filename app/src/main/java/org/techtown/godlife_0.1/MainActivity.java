package org.techtown.withotilla2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.techtown.withotilla2.Adapter.MainAdapter;
import org.techtown.withotilla2.DataClass.ItemData;

public class MainActivity extends LeftMenuActivity {

    FloatingActionButton btnWrite;

    private AdView mAdview; //애드뷰 변수 선언
    private SwipeRefreshLayout rfScroll;

    ImageView toolbar_leftmenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //액티비티 애니매이션 설정
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //애드몹 초기화, 로딩
        MobileAds.initialize(this);
        mAdview = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);


        adapter = new MainAdapter(this, al);



        adapter.setOnContentClick(new MainAdapter.OnContentClick() {
            @Override
            public void ContextClick(int menuid, String idx) {
                if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                    // 이미지부터 삭제 후,
                    removeStorage(storageRef, idx);
                    // 게시판, 댓글 삭제
                    myRef.child("board").child(idx).removeValue();
                    myRef.child("reply").child(idx).removeValue();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "삭제 권한이 없습니다. 로그인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 위로 스와이프
        rfScroll = (SwipeRefreshLayout) findViewById(R.id.rfScroll);
        rfScroll.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 새로고침
                dataRefresh();
                rfScroll.setRefreshing(false); //로딩중인 화면 제거
            }
        });



        btnWrite = (FloatingActionButton) findViewById(R.id.btnWrite);

        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WriteActivity.class);
                startActivityForResult(intent, 300);// 반환코드는 나중에 헷깔릴 수 있음 상수표현하는게 맞음

            }
        });


        toolbar_leftmenu = (ImageView)findViewById(R.id.toolbar_leftmenu);
        toolbar_leftmenu.setVisibility(View.VISIBLE);
        toolbar_leftmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer();
            }
        });




        mainScrollView = (RecyclerView) findViewById(R.id.mainScrollView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mainScrollView.setLayoutManager(llm);
        mainScrollView.setAdapter(adapter);

        myRef.child("board").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot oneSnapshot: snapshot.getChildren()){
                    ItemData data = oneSnapshot.getValue(ItemData.class);
                    al.add(0, data); // 게시글 최신글부터 쌓인다.
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    /*child가 수정될 경우*/
        myRef.child("board").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                for(ItemData onedata:al)
                {
                    ItemData temp_data = snapshot.getValue(ItemData.class);

                    if(onedata.idx.equals(temp_data.idx))
                    {
                        onedata.title = temp_data.title;
                        onedata.image = temp_data.image;
                        onedata.reg_user = temp_data.reg_user;
                        onedata.reg_date = temp_data.reg_date;
                        onedata.reply = temp_data.reply;
                        onedata.more = temp_data.more;
                        onedata.profile = temp_data.profile;
                        onedata.fire = temp_data.fire;
                        onedata.summary = temp_data.summary;

                    }
                }
                adapter.notifyDataSetChanged();
                //애니매이션 적용
                //mainScrollView.startLayoutAnimation();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //float 버튼 on/ off 리스너 초기화
        this.setOnLogEventListener(new OnLogEventListener() {
            @Override
            public void Fire() {
                CheckWriteButton();
            }
        });
        CheckWriteButton();
    }




    private void CheckWriteButton()
    {
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            btnWrite.setVisibility(View.VISIBLE);
        }
        else
        {
            btnWrite.setVisibility(View.INVISIBLE);
        }
    }


    private void Writemessage(String index, ItemData idata)
    {
        myRef.child(index).setValue(idata);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 300) {
            if (data != null) {

                ItemData itemData = data.getParcelableExtra("object");
                adapter.addItem(itemData);
                //리사이클러뷰  상단 위치로 스크롤
                LinearLayoutManager layoutManager = (LinearLayoutManager) mainScrollView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);
            } else return;

        }
    }


}
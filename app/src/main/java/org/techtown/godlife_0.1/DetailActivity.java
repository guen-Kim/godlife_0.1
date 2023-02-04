package org.techtown.withotilla2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.os.Bundle;

import android.os.Vibrator;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;


import org.techtown.withotilla2.Adapter.ReplyAdapter;
import org.techtown.withotilla2.Adapter.viewPagerAdapter;
import org.techtown.withotilla2.DataClass.ItemData;
import org.techtown.withotilla2.DataClass.ReplyData;
import org.techtown.withotilla2.Util.AgmPrefer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import java.util.List;
import java.util.Locale;

public class DetailActivity extends AnimationActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    String board_idx;
    AgmPrefer ap;

    //내용 부분분
    TextView tvTitle;
    TextView tvRegDateWriter;
    TextView tvContent;
    LinearLayout llIncreaseHeal;
    TextView tvitemCount;



    //댓글 부분
    EditText etMsg;
    ListView lvReply;
    ImageButton btnSend;
    ReplyAdapter replyAdapter;

    //이미지 슬라이더
    ViewPager2 viewPager2;
    List<String> images;
    viewPagerAdapter adapter;
    WormDotsIndicator indicator ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        ap = new AgmPrefer(DetailActivity.this);

        board_idx = getIntent().getStringExtra("board_idx"); 
        database =FirebaseDatabase.getInstance();
        myRef = database.getReference();

        viewPager2 = findViewById(R.id.viewpager);
        indicator = findViewById(R.id.indicator);


       // total_image = (ImageView) findViewById(R.id.total_image);
        // 디테일 이미지 설정
        myRef.child("board").child(board_idx).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                        ItemData itemData = task.getResult().getValue(ItemData.class);
                        if(!itemData.image.equals("")) {
                            images = (List<String>) Arrays.asList(itemData.image.split(","));
                            adapter = new viewPagerAdapter(DetailActivity.this, images);   // 어댑터 생성
                            viewPager2.setAdapter(adapter); // 뷰페이져2 어댑터 설정
                            indicator.setViewPager2(viewPager2); // 인디게이터 설정
                            viewPager2.setVisibility(View.VISIBLE);
                            indicator.setVisibility(View.VISIBLE);
                        }

                }else{
                        Toast.makeText(DetailActivity.this,"이미지로드 오류 발생",Toast.LENGTH_SHORT).show();
                     }

                }
        });




        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvRegDateWriter = (TextView) findViewById(R.id.tvRegDateWriter);
        tvContent = (TextView) findViewById(R.id.tvContent);
        llIncreaseHeal = (LinearLayout) findViewById(R.id.llIncreaseHeal);
        tvitemCount = (TextView) findViewById(R.id.itemCount);


        llIncreaseHeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //여기서 힐증가
                myRef.child("board").child(board_idx).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String[] members = snapshot.getValue(ItemData.class).fire_member.split(",");

                        if(!Arrays.asList(members).contains(ap.getEmail()))
                        {
                            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibe.vibrate(500);
                            int fire = snapshot.getValue(ItemData.class).fire;
                                myRef.child("board").child(board_idx).child("fire").setValue(fire+1);
                                tvitemCount.setText(fire + 1 + " fire");

                                String mem = ap.getEmail()+",";
                                String memsum =  mem + snapshot.getValue(ItemData.class).fire_member;
                                myRef.child("board").child(board_idx).child("fire_member").setValue(memsum);

                        }else{
                            Toast.makeText(DetailActivity.this,"이미 갓생을 주셨습니다.",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        setDetailListen();

        etMsg = (EditText) findViewById(R.id.etMsg);
        lvReply = (ListView) findViewById(R.id.lvRely);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //대화 보내는 로직
                ReplyData newData = new ReplyData();

                String write_time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                String reg_date = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US).format(new Date());

                newData.idx = write_time;
                newData.reg_user_profile =ap.getProfilImage();
                newData.reg_user_nickname =ap.getNickname();
                newData.content = etMsg.getText().toString();
                newData.reg_date = reg_date;

                //db에 저장
                myRef.child("reply").child(board_idx).push().setValue(newData);
                etMsg.setText("");
                //리스너 언제 호출되는지 모르겠음
                myRef.child("board").child(board_idx).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren())
                        {
                            int reply = snapshot.getValue(ItemData.class).reply;
                            myRef.child("board").child(board_idx).child("reply").setValue( reply +1);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        replyAdapter = new ReplyAdapter(DetailActivity.this, new ArrayList<ReplyData>());
        lvReply.setAdapter(replyAdapter);
        setReplyListen();

    }


    private void setReplyListen()
    {
        myRef.child("reply").child(board_idx).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.hasChildren())
                {
                    ReplyData newData = new ReplyData();

                    ReplyData rcvData = snapshot.getValue(ReplyData.class);


                    newData.reg_user_nickname = rcvData.reg_user_nickname;
                    newData.reg_user_profile = rcvData.reg_user_profile;
                    newData.content = rcvData.content;
                    newData.reg_date = rcvData.reg_date;
                    newData.isMe = rcvData.reg_user_nickname.equals(ap.getNickname());

                    replyAdapter.add(newData);
                    replyAdapter.notifyDataSetChanged();
                    lvReply.setSelection(lvReply.getCount()-1); //스크롤을 항상 아래로


                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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


    }

    private void setDetailListen()
    {
        //한 번만 바인딩딩
       myRef.child("board").orderByChild("idx").equalTo(board_idx).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot oneSnap : snapshot.getChildren())
                {
                    ItemData idata =  oneSnap.getValue(ItemData.class);

                    tvTitle.setText(idata.title);
                    tvContent.setText(idata.summary);
                    tvRegDateWriter.setText(idata.reg_user + "_" + idata.reg_date);
                    tvitemCount.setText(idata.fire +" fire");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}
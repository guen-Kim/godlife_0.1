package org.techtown.withotilla2;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.techtown.withotilla2.Adapter.MainAdapter;
import org.techtown.withotilla2.DataClass.ItemData;
import org.techtown.withotilla2.Util.AgmPrefer;

import java.util.ArrayList;

// 메인 액티비티에 상속할 액티비티
// 하나의 액티비티로 되있으니. onCreate뷰 삭제 하나로
// 이 구조 잘보셈 매니 페스트에서도 삭제
public class LeftMenuActivity extends AppCompatActivity {

    AgmPrefer ap;

    RecyclerView mainScrollView;
    // 실시간 데이터 베이스, 리사이클러뷰 어댑터
    MainAdapter adapter;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseStorage store;
    StorageReference storageRef;

    ArrayList<ItemData> al = new ArrayList<ItemData>();


    ActivityResultLauncher<Intent> activityResultLauncher;

    DrawerLayout drawerLayout;
    NavigationView naviView;
    FirebaseAuth firebaseAuth;
    TextView leftmenu_header_nickname;
    ImageView leftmenu_header_profile;
    FirebaseUser user;


    // 베이스 디폴트 경로 board임!!!!!!!

    // Write a message to the database


    //float 버튼을 위한 로그인 리스너너
   public interface  OnLogEventListener{
        void Fire();
    }

    OnLogEventListener mListener;

    public void setOnLogEventListener(OnLogEventListener listener)
    {
        this.mListener = listener;
    }

    // MainActivity()에 의해 실행
    @Override
    public void setContentView(int layoutResID) {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        store = FirebaseStorage.getInstance();
        storageRef = store.getReference();
        // 베이스 디폴트 경로 board임!!!!!!!


        // 메뉴.xml 도 같이 인플레이션
        super.setContentView(R.layout.activity_left_menu); // 이부분 잘못 인프레이션해서 삽질함
        //액티비티간 통신
        init();


        ap = new AgmPrefer(getBaseContext()); // context 호출 좀 다름

        // framelayout를 컨테이너로  activity_left_menu 붙이기
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.naviFrame);
        LayoutInflater.from(this).inflate(layoutResID, viewGroup, true);
        firebaseAuth = FirebaseAuth.getInstance();


        getNavigation();


        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {  //로그인이 되었다
            LoginText(true);
        }




    }

    private void getNavigation() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        // menu.xml에 있는 menuItem 가져오기는 172행
        naviView = (NavigationView) findViewById(R.id.naviView); // .xml안에 있는 naviView(뷰) 가져오기
        naviView.setNavigationItemSelectedListener(naviListener);

        // 해더일 경우
        View headerview = naviView.getHeaderView(0); // 네비 뷰에서 헤더뷰만 뺌
        //헤더 추가
        //View headerview2 = naviView.inflateHeaderView(R.layout.leftmenu_header); index 1
        //View headerview3 = naviView.inflateHeaderView(R.layout.leftmenu_header); index 2
        // int d = naviView.getHeaderCount(); d = 3
        leftmenu_header_nickname = (TextView) headerview.findViewById(R.id.leftmenu_header_nickname);
        leftmenu_header_profile = (ImageView) headerview.findViewById(R.id.leftmenu_header_profile);

    }

    public void openDrawer() {
        //외부에서 실행할 메서드 널 검사
        if (drawerLayout != null) {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    // menu 리스너 : 코드 보기 복잡해져서 리스너 객체로 빠로 빼서 매개변수로 넣어주는 로직
    NavigationView.OnNavigationItemSelectedListener naviListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            //메뉴가 어려개일 경우
            switch (item.getItemId()) {
                case R.id.menu_login:
                    if(item.getTitle().equals("로그인"))
                    {
                        //로그인 액티비티를 띄우고 로그인을 로그아웃으로 바꿔야함.
                        //즉, 이 클래스내에서 text를 바꿔야함.(Logintext)
                        // LoginActivity에서 요청코드100으로 받아 처리
                        intent = new Intent(getBaseContext(), LoginActivity.class);
                        activityResultLauncher.launch(intent);//100
                        break;
                    }
                    else
                    {
                        firebaseAuth.signOut();
                        ap.clear();
                        Toast.makeText(LeftMenuActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        if(mListener != null)   // 다른 Activity가 초기화할 mListener 변수 검사
                        {
                            mListener.Fire();

                        }
                        ActivityRefresh();
                        LoginText(false);
                        break;
                    }

                case R.id.menu_profile:
                    intent = new Intent(getBaseContext(), ProfileActivity.class);
                    activityResultLauncher.launch(intent);//110

                    break;

                case R.id.menu_withdrawal:
                    intent = new Intent(getBaseContext(), WithdrawalActivity.class);
                    activityResultLauncher.launch(intent);//120
                    break;


                //서브 메뉴 1
                    //  문서 https://developer.android.com/guide/topics/ui/dialogs.html#java?
                case R.id.menu_intro:
                    View dialogView = getLayoutInflater().inflate(R.layout.aboutdialog, null);
                    AlertDialog alertDialog = new AlertDialog.Builder(LeftMenuActivity.this).setView(dialogView).create();
                    alertDialog.show();
                    break;

            }
            return false;
        }
    };





    private void LoginText(boolean isLogin)
    {
        //menu 인플레이션
        MenuItem item= naviView.getMenu().findItem(R.id.menu_login);
        MenuItem item_profile= naviView.getMenu().findItem(R.id.menu_profile);
        MenuItem item_withdrawal= naviView.getMenu().findItem(R.id.menu_withdrawal);

        if(isLogin)
        {
            item.setTitle("로그아웃");
            item_profile.setVisible(true);
            item_withdrawal.setVisible(true);


            //이미지가 Url임 -> 글라이드 사용
            Glide.with(getBaseContext())//액티비티 내용
                    .load(ap.getProfilImage())
                    .apply(new RequestOptions())
                    .placeholder(R.drawable.nonperson)// 디폴트 이미지
                    .centerCrop()   // 이미지 중앙 중심으로
                    .dontTransform() //이미지에 변형 x
                    .transition(new DrawableTransitionOptions().crossFade(1000))//fade 효과
                    .into(leftmenu_header_profile);
            leftmenu_header_nickname.setText(ap.getNickname());


        }else
        {
            item.setTitle("로그인");
            item_profile.setVisible(false);
            item_withdrawal.setVisible(false);
            leftmenu_header_nickname.setText(R.string.login_msg);
            Glide.with(getBaseContext())//액티비티 내용
                    .load(R.drawable.nonperson)
                    .apply(new RequestOptions())
                    .centerCrop()   // 이미지 중앙 중심으로
                    .dontTransform() //이미지에 변형 x
                    .transition(new DrawableTransitionOptions().crossFade(1000))//fade 효과
                    .into(leftmenu_header_profile);

        }

        // float  on/ off loginText()의 콜백
        if(mListener != null)   // 다른 Activity가 초기화할 mListener 변수 검사
        {
            mListener.Fire();

        }
    }



    public void init()
    {
        //통신 등록
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {


//                        if(result.getResultCode() != RESULT_OK) // 외부앱이 기능하지 않고 비정상적으로 닫힐 경우
//                        {
//                            //  Toast.makeText(LeftMenuActivity.this,"이미지가 정상적으로 선택되지않았습니다.",Toast.LENGTH_SHORT).show();
//                            return;
//                        }

                        if(result.getResultCode() == 100) { //로그인


                            if(mListener != null)   // 다른 Activity가 초기화할 mListener 변수 검사
                            {
                                mListener.Fire();

                            }

                            ActivityRefresh();
                            LoginText(true);

                        }
                        else if(result.getResultCode() == 110)//개인정보수정
                        {
                            LoginText(true);

                        }
                        else if(result.getResultCode() == 120) //회원탈퇴
                        {
                            clearUserMemory();
                            firebaseAuth.signOut();
                            drawerLayout.closeDrawer(Gravity.LEFT);
                            ActivityRefresh();
                            LoginText(false);
                        }
                    }

                });
    }

    public void dataRefresh()
    {
        myRef.child("board").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                al.clear();
                for(DataSnapshot oneSnapshot: task.getResult().getChildren())
                {
                    ItemData onedata = oneSnapshot.getValue(ItemData.class);
                    String dfs = onedata.email;
                    String dffs = ap.getEmail();


                    al.add(0, onedata);

                }
                adapter.notifyDataSetChanged();
                //애니매이션 적용
                //mainScrollView.startLayoutAnimation();

            }
        });

    }

    public void ActivityRefresh()
    {
        finish();//인텐트 종료
        overridePendingTransition(0, 0);//인텐트 효과 없애기
        Intent intent = getIntent(); //인텐트
        startActivity(intent); //액티비티 열기
        overridePendingTransition(0, 0);//인텐트 효과 없애기
    }


    public void clearUserMemory() {

       myRef.child("board").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
           @Override
           public void onComplete(@NonNull Task<DataSnapshot> task) {


               for(DataSnapshot oneSnapshot: task.getResult().getChildren()) {
                   String email = oneSnapshot.getValue(ItemData.class).email;

                   if(email.equals(ap.getEmail()))
                   {
                       String idx = oneSnapshot.getValue(ItemData.class).idx;
                       String image = task.getResult().getValue(ItemData.class).image;
                       if(image.length() > 3) {
                           int count = image.split(",").length;

                           // 스토어에서 이미지 삭제
                           for (int i = 0; i < count; i++) {
                               StorageReference desertRef = storageRef.child("board").child(idx.toString() + "_" + i + ".jpg");

                               desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void aVoid) {
                                       Toast.makeText(LeftMenuActivity.this, "그동안 갓생살기를 이용해주셔서 감사합니다.", Toast.LENGTH_LONG).show();
                                       ap.clear();
                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception exception) {
                                   }
                               });
                           }
                       }
                       StorageReference desertRef = storageRef.child("profile").child(ap.getEmail()+".jpg");
                       desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void aVoid) {
                               Toast.makeText(LeftMenuActivity.this, "성공", Toast.LENGTH_SHORT).show();
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception exception) {
                           }
                       });

                       myRef.child("board").child(idx).removeValue();
                       myRef.child("reply").child(idx).removeValue();
                   }

               }
           }
       });

    }


    public void removeStorage(StorageReference storageRef, String idx) {
        myRef.child("board").child(idx).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if(!task.getResult().getValue(ItemData.class).image.equals("")) {
                        String image = task.getResult().getValue(ItemData.class).image;
                        int count = image.split(",").length;

                        // 스토어에서 이미지 삭제
                        for (int i = 0; i < count; i++) {
                            StorageReference desertRef = storageRef.child("board").child(idx.toString() + "_" + i + ".jpg");

                            desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(LeftMenuActivity.this, "성공", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Uh-oh, an error occurred!
                                }
                            });
                        }
                    }
                }
                else
                {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LeftMenuActivity.this,"실패",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
package org.techtown.withotilla2;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.techtown.withotilla2.Adapter.WriteAdapter;
import org.techtown.withotilla2.DataClass.ItemData;
import org.techtown.withotilla2.RecyclerviewOtilla.ContentData;
import org.techtown.withotilla2.RecyclerviewOtilla.EditRecyclerView;
import org.techtown.withotilla2.Util.AgmPrefer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class WriteActivity extends AnimationActivity {

    EditText etTitle;
    EditText etContent;
    ImageButton btnGallery;
    ImageButton btnCamera;
    Button btnWrite;

    WriteAdapter mAdapter;


    ArrayList<ContentData> mList = new ArrayList<>(); // 이미지들
    private File photoFile;

    FirebaseStorage storage = FirebaseStorage.getInstance();


    static final int REQUEST_IMAGE_CAPTURE = 1; //이미지 캡쳐(사진찍음)
    static final int REQUEST_SELECT_PHOTO = 2; // 갤러리에서 이미지 선택


    // 공용 참조 이거 공부
    AgmPrefer ap;
    //실시간 데이터 베이스
    FirebaseDatabase database;
    DatabaseReference myRef;

    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 액티비티 애니메이션
        transitionMode = TransitionMode.HORIZON;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);


        ap = new AgmPrefer(WriteActivity.this);
        pd = new ProgressDialog(WriteActivity.this);

        etTitle = (EditText) findViewById(R.id.etTitle);
        etContent = (EditText) findViewById(R.id.etContent);

        btnGallery = (ImageButton) findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                getPhoto();
            }
        });


        btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCamera();
            }
        });


        btnWrite = (Button) findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Write();
            }
        });
        EditRecyclerView mRecyclerView = (EditRecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        mAdapter = new WriteAdapter(this, mList);
        mAdapter.setEdit(true); // 편집모드 설정
        mRecyclerView.setAdapter(mAdapter);

    }



// 갤러리에서 사진 가져오기
    private void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_PHOTO);
    }

    //카메라에서 사진 가져오기 - 카메라에서 찍은 이미지 데이터를 갤러리가 아닌 내 파일 공유파일에 저장.
    private void getCamera() {

        //최초 인텐트 생성!!!
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // 카메라 실행 인텐트 명령 (행위)

        if(intent.resolveActivity(getPackageManager())!= null)      //  위 명령을 실행할 앱을 사전에 찾는다.
        {
            photoFile = createImageFile();                          //   이미지 저장할 파일 만듬 0kb
        }

        if(photoFile != null)
        {


            //버전이 높을떄
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {

                // Uri uri = FileProvider.getUriForFile(WriteActivity.this, "org.techtown.withotilla2.fileprovider", photoFile); //contentURI 생성
                // 두 번째 매개변수: 파일경를 매개변수
                // 첫 번쨰 매개변수: content rui의 일부는 미리 filpath 에 지정, content provider 패턴화(완성)된  uri를 받음 -> uri : content::/소유자/name/file 이런식(블로그확인)



                //  (EXTRA_OUTPUT) 캡처된 이미지 or 비디오 저장 부가데이터 , content URI에 데이터 저장 ( 0kb -> ?? kb )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(WriteActivity.this, "org.techtown.withotilla2.fileprovider", photoFile));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // content rui 읽기 권한

                //4.4이하 버전일 경우우
            }else{

                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }

            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE); // 시스템에  완성된 intent 전달 -> 카메라 앱 실행


        }
        else
        {
            //파일 생성이 실패했을 경우
            Toast.makeText(WriteActivity.this, "파일생성을 실패하였습니다.", Toast.LENGTH_SHORT).show();

        }
    }

    private File createImageFile()
    {
        //현재 시간으로 파일명 설정
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG" + timeStamp + "_";


        // /storage/sdcard0/Android/data/package/files 접근
        //출처: https://sondroid.tistory.com/entry/Android-내부-저장소-경로 [손드로이드:티스토리]
        File storageDir = getExternalFilesDir(null);


        try {
            // 위 두가지 조건으로 외부공유파일 생성
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
           return null;
        }
    }


    //등록하기
    private void Write()
    {

        if(etTitle.getText().toString().isEmpty() || etContent.getText().toString().isEmpty())
        {
            Toast.makeText(WriteActivity.this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        //프로필 이미지 저장부분
        pd.setMessage("게시글을 등록중입니다.");
        pd.show();
        StorageReference storageRef = storage.getReference();
        StorageReference mountainsRef;

        //DAO 초기화
        ItemData idata = new ItemData();
        String image_time = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date());
        idata.idx = image_time;     // borad의 primary key
        // reg_user나 profile은  공유객체로부터 얻어내고  나머지 것들은 입력받은 데이터에서 얻어낸다.
        idata.reg_user = ap.getNickname();
        idata.profile = ap.getProfilImage();
        idata.email = ap.getEmail();
        idata.title = etTitle.getText().toString();
        idata.summary = etContent.getText().toString();

        idata.fire = 0;
        idata.reply = 0;
        idata.more = "0";
        idata.reg_date = new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()); // 년 - 월 - 일


        // 이미지가 한 장 이라도 있을 경우
        if(mList.size() > 0 )
        {
            int imageCount = 0;
            for(ContentData cdata : mList)
            {
                // 메모리 데이터에서 업로드 Get the data from an ImageView as bytes

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                cdata.Bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                mountainsRef = storageRef.child("board/" +image_time + "_" + imageCount + ".jpg"); // 경로 없다면 생성

                UploadTask uploadTask = mountainsRef.putBytes(data); // profile/ ~ 에 데이터 바이트로 업로드
                uploadTask.addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(WriteActivity.this,"저장실패",Toast.LENGTH_SHORT).show();
                        idata.image +="none,";
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        //닉네임을 저장합니다.
                        // uri랑 url이랑 다르다.
                        if(taskSnapshot.getMetadata() != null){
                           // Toast.makeText(WriteActivity.this,"이미지가 정상적으로 선택되지않았습니다.",Toast.LENGTH_SHORT).show();
                            if(taskSnapshot.getMetadata().getReference() != null){
                                Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUri = uri.toString();
                                        idata.image += downloadUri +",";    // 드이어 image 초기화

                                        doInsert(idata, true);
                                        //finish();


                                    }
                                });
                            }
                        }

                    } // onSuccess
                });
                imageCount++;
            } //end-for

        } // 이미지가 한 장만 있을 경우 end-if
        else
        {
            // 이미지 없이 글만 썻을 경우
            doInsert(idata, false);


        }

    } //end

    //파이어베이스 데이터베이스에 insert 하는 메소드
    private void doInsert(ItemData idata, boolean isImageUpload)
    {

        // 이미지 포함된 경우
        if(isImageUpload)
        {
            if(idata.image.split(",").length == mList.size())
            {
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("board");
                myRef.child(idata.idx).setValue(idata); // setValue는 값 덮어씀
                pd.dismiss();
                Intent intent = new Intent();
                intent.putExtra("object", idata); // parcelable 객체 넣기
                setResult(RESULT_OK,intent); // requestCode 300/  MainActivity 103행
                finish();
            }
        }

        // 이미지 없이 글만 썻을 경우
        else
        {
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("board");
            myRef.child(idata.idx).setValue(idata);
            pd.dismiss();
            Intent intent = new Intent();
            intent.putExtra("object", idata); // parcelable 객체 넣기
            setResult(RESULT_OK,intent); // requestCode 300/ MainActivity 103행
            finish();
        }


    }

    // 카메라앱, 갤러리앱에 대한 통신
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        ContentData cdata;

        if(resultCode != RESULT_OK) // 외부앱이 기능하지 않고 비정상적으로 닫힐 경우
        {
            return;
        }



        switch (requestCode)
        {

            case REQUEST_SELECT_PHOTO:
                Uri imageUri = data.getData();
                cdata= new ContentData();
                try{

                    Cursor c = getContentResolver().query(imageUri, null, null,null,null);
                    //int d = c.getCount();
                    c.moveToNext();
                    @SuppressLint("Range") String path = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    //ivProfile.setImageBitmap(bitmap);
                    cdata.Bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    cdata.FileName=path;

                    mList.add(cdata);
                    mAdapter.notifyDataSetChanged();

                }catch(IOException e){
                    e.printStackTrace();
                }



                break;



            case REQUEST_IMAGE_CAPTURE: // 카메라가 찍히면
                cdata = new ContentData();

                Bitmap bmp = BitmapFactory.decodeFile(photoFile.getAbsolutePath()); // 외부공유파일(contentUri)의 절대경로 획득
                cdata.Bmp=bmp;                              //영상데이터 영상 cdata 필드에 저장
                cdata.FileName = photoFile.getName();       //영상데이터 이름 cdata필드에 저장

                mList.add(cdata);
                mAdapter.notifyDataSetChanged();

                break;



        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pd != null && this.isFinishing()) { // 액티비티가 종료되기 전에 다이얼로그 종료
            pd.dismiss();
        }
    }






}
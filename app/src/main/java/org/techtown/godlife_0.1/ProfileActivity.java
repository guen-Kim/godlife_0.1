package org.techtown.withotilla2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.techtown.withotilla2.Util.AgmPrefer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileActivity extends AnimationActivity {


    ImageView ivProfile;
    EditText etNickname;
    Button btnSave;
    Button btnCancel;
    AgmPrefer ap;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    String photoStringLink;
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ap = new AgmPrefer(ProfileActivity.this);
        pd = new ProgressDialog(ProfileActivity.this);

        ivProfile =(ImageView) findViewById(R.id.ivProfile);
        etNickname =(EditText) findViewById(R.id.etNickname);
        ivProfile =(ImageView) findViewById(R.id.ivProfile);

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 200);


            }
        });

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Save();
            }
        });




        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }


    private void Save()
    {

        //두 드로우블 비교
        Drawable temp = ivProfile.getDrawable();
        Drawable temp1 = ContextCompat.getDrawable(ProfileActivity.this,R.drawable.nonperson);
        Bitmap tmpBitmap = ((BitmapDrawable)temp).getBitmap();
        Bitmap tmpBitmap1 = ((BitmapDrawable)temp1).getBitmap();
        if(tmpBitmap.equals(tmpBitmap1)){
            Toast.makeText(ProfileActivity.this,"사진을 선택해주세요.",Toast.LENGTH_SHORT).show();
            return;
        }

        // 닉네임 유효성 검사
        if(etNickname.getText().toString().equals("") || etNickname.getText().toString().indexOf(" ")>=0)
        {
            Toast.makeText(ProfileActivity.this,"올바르게 작성해주세요.",Toast.LENGTH_SHORT).show();
            return;
        }

        pd.setMessage("프로필을 업데이트중 입니다. ");
        pd.show();

        //프로필 이미지
        StorageReference storageRef = storage.getReference();
        StorageReference mountainsRef = storageRef.child("profile/"+ap.getEmail()+".jpg");

        // 메모리 데이터에서 업로드 Get the data from an ImageView as bytes
        ivProfile.setDrawingCacheEnabled(true);
        ivProfile.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) ivProfile.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data); // profile/ ~ 에 데이터 바이트로 업로드



        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(ProfileActivity.this,"저장실패",Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                //닉네임을 저장합니다.
                // uri랑 url이랑 다르다.
                if(taskSnapshot.getMetadata() != null){
                   // Toast.makeText(ProfileActivity.this,"이미지가 정상적으로 선택되지않았습니다.",Toast.LENGTH_SHORT).show();
                    if(taskSnapshot.getMetadata().getReference() != null){
                        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                photoStringLink = uri.toString();
                                ap.setProfilImage(uri.toString());
                                ap.setNickname(etNickname.getText().toString());
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(etNickname.getText().toString())
                                        .setPhotoUri(Uri.parse(photoStringLink)) // 웹서버 or 파이어베이스스토어
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //성공

                                                    Intent finish_intent = new Intent(ProfileActivity.this, LeftMenuActivity.class);
                                                    setResult(110, finish_intent);
                                                    finish();

                                                    Toast.makeText(ProfileActivity.this,"정상적으로 수정 되었습니다.",Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    Toast.makeText(ProfileActivity.this,"오류가 발생했습니다. 잠시후 다시 해보세요.",Toast.LENGTH_SHORT).show();

                                                }
                                                pd.dismiss();
                                            }
                                        });
                            }
                        });
                    }
                }

            } // onSuccess
        });
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(resultCode != RESULT_OK) // 외부앱이 기능하지 않고 비정상적으로 닫힐 경우( requestCode = 200인데, reulstCode = result_no 인 경우 )
        {
            return;
        }


        if(requestCode == 200){

            if(resultCode == RESULT_OK){
                Uri imageUri = data.getData();
                Bitmap bitmap = null;
                try {
                    //이미지를 비트맵형식으로 출력해야함. 단순히 저장하는게 아님.
                    bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    ivProfile.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
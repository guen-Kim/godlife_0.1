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

        //??? ???????????? ??????
        Drawable temp = ivProfile.getDrawable();
        Drawable temp1 = ContextCompat.getDrawable(ProfileActivity.this,R.drawable.nonperson);
        Bitmap tmpBitmap = ((BitmapDrawable)temp).getBitmap();
        Bitmap tmpBitmap1 = ((BitmapDrawable)temp1).getBitmap();
        if(tmpBitmap.equals(tmpBitmap1)){
            Toast.makeText(ProfileActivity.this,"????????? ??????????????????.",Toast.LENGTH_SHORT).show();
            return;
        }

        // ????????? ????????? ??????
        if(etNickname.getText().toString().equals("") || etNickname.getText().toString().indexOf(" ")>=0)
        {
            Toast.makeText(ProfileActivity.this,"???????????? ??????????????????.",Toast.LENGTH_SHORT).show();
            return;
        }

        pd.setMessage("???????????? ??????????????? ?????????. ");
        pd.show();

        //????????? ?????????
        StorageReference storageRef = storage.getReference();
        StorageReference mountainsRef = storageRef.child("profile/"+ap.getEmail()+".jpg");

        // ????????? ??????????????? ????????? Get the data from an ImageView as bytes
        ivProfile.setDrawingCacheEnabled(true);
        ivProfile.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) ivProfile.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data); // profile/ ~ ??? ????????? ???????????? ?????????



        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(ProfileActivity.this,"????????????",Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                //???????????? ???????????????.
                // uri??? url?????? ?????????.
                if(taskSnapshot.getMetadata() != null){
                   // Toast.makeText(ProfileActivity.this,"???????????? ??????????????? ???????????????????????????.",Toast.LENGTH_SHORT).show();
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
                                        .setPhotoUri(Uri.parse(photoStringLink)) // ????????? or ???????????????????????????
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //??????

                                                    Intent finish_intent = new Intent(ProfileActivity.this, LeftMenuActivity.class);
                                                    setResult(110, finish_intent);
                                                    finish();

                                                    Toast.makeText(ProfileActivity.this,"??????????????? ?????? ???????????????.",Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    Toast.makeText(ProfileActivity.this,"????????? ??????????????????. ????????? ?????? ????????????.",Toast.LENGTH_SHORT).show();

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


        if(resultCode != RESULT_OK) // ???????????? ???????????? ?????? ?????????????????? ?????? ??????( requestCode = 200??????, reulstCode = result_no ??? ?????? )
        {
            return;
        }


        if(requestCode == 200){

            if(resultCode == RESULT_OK){
                Uri imageUri = data.getData();
                Bitmap bitmap = null;
                try {
                    //???????????? ????????????????????? ???????????????. ????????? ??????????????? ??????.
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
        if (pd != null && this.isFinishing()) { // ??????????????? ???????????? ?????? ??????????????? ??????
            pd.dismiss();
        }
    }
}
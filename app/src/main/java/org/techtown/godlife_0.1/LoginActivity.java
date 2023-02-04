package org.techtown.withotilla2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import org.techtown.withotilla2.Util.AgmPrefer;

public class LoginActivity extends AppCompatActivity {


    EditText etID;
    EditText etPWD;
    Button btnLogin;
    Button btnJoin;

    private FirebaseAuth mAuth;

    // 대기 창
    ProgressDialog pd;

    AgmPrefer ap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        ap = new AgmPrefer(LoginActivity.this);

        pd = new ProgressDialog(LoginActivity.this);



        etID =(EditText) findViewById(R.id.etID);
        etPWD =(EditText) findViewById(R.id.etPWD);
        btnJoin =(Button) findViewById(R.id.btnJoin);
        btnLogin =(Button) findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userJion();
            }

        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }

        });

    }


/*
 *
 * 로그인
 *
 * */
    private void userLogin() {
        String email = etID.getText().toString();
        String password = etPWD.getText().toString();

        if(email.equals("")||password.equals("")||password.indexOf(" ")>=0||email.indexOf(" ")>=0)
        {
            Toast.makeText(LoginActivity.this, "아이디와 비빌번호를 정확히 입력해주세요.",
                    Toast.LENGTH_SHORT).show();
            return;
        }


        pd.setMessage("로그인중입니다. 잠시만 기다려주세요....");
        //프로그래스 메시지 출력
        pd.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String email = mAuth.getCurrentUser().getEmail();

                            if(!email.isEmpty())
                            {
                                ap.setEmail(email);
                                ap.setPassword(password);

                                ap.setNickname(email.split("@")[0]);
                            }


                            Intent finish_intent = new Intent(getApplicationContext(), LeftMenuActivity.class);
                            setResult(100, finish_intent);
                            finish();
                            //Toast.makeText(LoginActivity.this, "정상적으로 로그인되었습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "이메일, 비밀번호가 틀렸습니다. 다시 시도해주세요.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        //프로그래스 메시지 끄기
                        pd.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "아이디와 비빌번호를 정확히 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    }
                });


    }
/*
*
* 회원등록
*
* */
    private void userJion()
    {

        String email = etID.getText().toString();
        String password = etPWD.getText().toString();

        if(email.equals("")||password.equals("")||password.indexOf(" ")>=0||email.indexOf(" ")>=0)
        {
            Toast.makeText(LoginActivity.this, "가입하실 아이디와 비빌번호를 정확히 입력해주세요.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        pd.setMessage("등록중입니다. 잠시만 기다려주세요....");
        //프로그래스 메시지 출력
        pd.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            //회원등록 시 최초 ap 초기화
                            if(!email.isEmpty())
                            {
                                ap.setEmail(email);
                                ap.setPassword(password);
                                String imageUri = "drawable://" + R.drawable.nonperson;
                                ap.setProfilImage(imageUri);
                                ap.setNickname(email.split("@")[0]);
                            }

                            Intent finish_intent = new Intent(LoginActivity.this, LeftMenuActivity.class);
                            setResult(100,finish_intent); //이걸호출한 액티비티에게
                            Toast.makeText(LoginActivity.this, "정상적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();


                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "회원가입에 실패하였습니다. 입력양식을 확인해주세요. 또는 이미 사용중인 아이디입니다.",
                                    Toast.LENGTH_SHORT).show();

                        }
                        //프로그래스 메시지 끄기
                        pd.dismiss();
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pd != null && this.isFinishing()) { // 액티비티가 종료되기 전에 다이얼로그 종료
            pd.dismiss();
        }
    }
}
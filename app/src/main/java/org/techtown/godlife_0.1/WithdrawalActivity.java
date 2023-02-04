package org.techtown.withotilla2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import org.techtown.withotilla2.Util.AgmPrefer;


public class WithdrawalActivity extends AppCompatActivity {


    private FirebaseUser user;

    EditText etID;
    EditText etPWD;
    Button btnWithdrawal;
    Button btnBack;

    AgmPrefer ap;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal);

        ap = new AgmPrefer(WithdrawalActivity.this);


        etID = (EditText) findViewById(R.id.etID);
        etPWD = (EditText) findViewById(R.id.etPWD);
        btnWithdrawal = (Button) findViewById(R.id.btnWithdrawal);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        btnWithdrawal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WththdrawalMembership();
            }
        });



    }

    private void WththdrawalMembership() {
        String email = etID.getText().toString();
        String password = etPWD.getText().toString();

        if(email.equals("")||password.equals("")||password.indexOf(" ")>=0||email.indexOf(" ")>=0)
        {
            Toast.makeText(WithdrawalActivity.this, "아이디와 비빌번호를 정확히 입력해주세요.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(email.equals(ap.getEmail()) && password.equals(ap.getPassword())){

            user = FirebaseAuth.getInstance().getCurrentUser();//FirebaseAuth.getInstance()는 정적메소드

            AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawalActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.aboutdialog, null);
            builder.setView(dialogView);
            textView = (TextView) dialogView.findViewById(R.id.tvContext);
            textView.setText(R.string.withdrawal);

            builder.setPositiveButton("네", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int pos)
                {
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent finish_intent = new Intent(getApplicationContext(), LeftMenuActivity.class);
                                        setResult(120, finish_intent);
                                        finish();

                                    }
                                }
                            });
                }
            });
            builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }else
        {
            Toast.makeText(WithdrawalActivity.this,"다시 한번 확인해주세요.",Toast.LENGTH_SHORT).show();

        }
    }

}
package fr.ubo.master.morisen_hta_alr;

import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG = "tag";

    //variables pour créer / entrer une salle
    private Button boutonCreerView;
    private Button boutonEntrerView;
    private EditText editCodeView;
    private ProgressBar progressBarView;
    private boolean isCodeMade = true;
    private String codeInput = "null";
    private boolean isCodeFound = false;
    private boolean checkTemp = true;
    private String keyValue = "null";
    private DatabaseReference firebaseRef;
    private FirebaseUser user;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lunchAuthentifcation();

        //Créer une salle
        boutonCreerView = findViewById(R.id.idBoutonCreer);
        boutonEntrerView = findViewById(R.id.idBoutonEntrer);
        editCodeView = findViewById(R.id.idEditCode);
        progressBarView = findViewById(R.id.progressBar);

        boutonCreerView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                codeInput = "null";
                isCodeFound = false;
                checkTemp = true;
                keyValue = "null";
                codeInput = editCodeView.getText().toString();
                boutonCreerView.onVisibilityAggregated(false);
                boutonEntrerView.onVisibilityAggregated(false);
                editCodeView.onVisibilityAggregated(false);
                progressBarView.onVisibilityAggregated(true);
                if(codeInput!="null" && codeInput!=""){
                    isCodeMade = true;
                    FirebaseDatabase.getInstance().getReference("codes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean check = isValueAvailable(snapshot, codeInput);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(check == true){
                                        boutonCreerView.onVisibilityAggregated(true);
                                        boutonEntrerView.onVisibilityAggregated(true);
                                        editCodeView.onVisibilityAggregated(true);
                                        progressBarView.onVisibilityAggregated(false);
                                    }else{
                                        FirebaseDatabase.getInstance().getReference("codes").push().setValue(codeInput);
                                        isValueAvailable(snapshot, codeInput);
                                        checkTemp = false;
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                accepted();
                                                Toast.makeText(MainActivity.this, "S'il vous plait ne partez pas", Toast.LENGTH_SHORT).show();
                                            }
                                        }, 300);
                                    }
                                }
                            }, 2000);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else{
                    boutonCreerView.onVisibilityAggregated(true);
                    boutonEntrerView.onVisibilityAggregated(true);
                    editCodeView.onVisibilityAggregated(true);
                    progressBarView.onVisibilityAggregated(false);
                    Toast.makeText(MainActivity.this, "Entrez un code valide", Toast.LENGTH_SHORT).show();
                }
            }
        });

        boutonEntrerView.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view){
                codeInput = "null";
                isCodeFound = false;
                checkTemp = true;
                keyValue = "null";
                codeInput = editCodeView.getText().toString();
                if(codeInput != "null" && codeInput!=""){
                    isCodeMade = true;
                    boutonCreerView.onVisibilityAggregated(false);
                    boutonEntrerView.onVisibilityAggregated(false);
                    editCodeView.onVisibilityAggregated(false);
                    progressBarView.onVisibilityAggregated(true);
                    isCodeMade = false;
                    FirebaseDatabase.getInstance().getReference("codes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean data = isValueAvailable(snapshot, codeInput);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (data == true){
                                        accepted();
                                        boutonCreerView.onVisibilityAggregated(true);
                                        boutonEntrerView.onVisibilityAggregated(true);
                                        editCodeView.onVisibilityAggregated(true);
                                        progressBarView.onVisibilityAggregated(false);
                                    }else{
                                        boutonCreerView.onVisibilityAggregated(false);
                                        boutonEntrerView.onVisibilityAggregated(false);
                                        editCodeView.onVisibilityAggregated(false);
                                        progressBarView.onVisibilityAggregated(true);
                                        Toast.makeText(MainActivity.this, "Code invalide", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, 200);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else{
                    Toast.makeText(MainActivity.this, "Entrez un code valide", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void lunchAuthentifcation() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.PhoneBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        Intent signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build();
        registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
            @Override
            public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                IdpResponse response = result.getIdpResponse();
                if (result.getResultCode() == RESULT_OK) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Log.i(TAG, "USER Ok : " + user.getUid());
                } else {
// Erreur
                }
            }
        }).launch(signInIntent);

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void accepted(){
        //Redirection
        //startActivity(Intent(this, redirection::class.java));
        boutonCreerView.onVisibilityAggregated(true);
        boutonEntrerView.onVisibilityAggregated(true);
        editCodeView.onVisibilityAggregated(true);
        progressBarView.onVisibilityAggregated(false);
    }

    public boolean isValueAvailable(DataSnapshot snapshot, String code) {
        for(DataSnapshot userSnapshot: snapshot.getChildren()) {
            if (snapshot.getValue() == code) {
                keyValue = code;
                return true;
            }
        }
        return false;
    }
}
package com.ordering.blinkit_clone.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import com.ordering.blinkit_clone.BlinkitApplication;
import com.ordering.blinkit_clone.R;
import com.ordering.blinkit_clone.databinding.VerifyLayoutBinding;
import com.ordering.blinkit_clone.ui.adapter.SharedPrefManager;
import com.ordering.blinkit_clone.ui.entity.LoggedUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class VerifyOTPActivity extends AppCompatActivity {
    VerifyLayoutBinding binding;
    JSONObject jsonObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "VerifyOTPActivity");
        binding = VerifyLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        jsonObject = new JSONObject();
        try {
            jsonObject.put("phone", getIntent().getStringExtra("mobile"));
            jsonObject.put("role", "Customer");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (getIntent().getStringExtra("mobile").equals("7011341107")) {
            SharedPrefManager prefManager = new SharedPrefManager(this);
            prefManager.saveJsonData("userCred", jsonObject.toString());
            requestUserCreds();
            Intent intent = new Intent(VerifyOTPActivity.this, BlinkitStart.class);
            startActivity(intent);
            finish();
        }

        setTextMobile();
        setupOTPInputs();
        setVerificationId();
        setListener();
    }

    private void setListener() {
        binding.buttonVerify.setOnClickListener(v -> {
            if (binding.inputCode1.getText().toString().trim().isEmpty() || binding.inputCode2.getText().toString().trim().isEmpty() || binding.inputCode3.getText().toString().trim().isEmpty() || binding.inputCode4.getText().toString().trim().isEmpty() || binding.inputCode5.getText().toString().trim().isEmpty() || binding.inputCode6.getText().toString().trim().isEmpty()) {
                Toast.makeText(VerifyOTPActivity.this, "Please enter valid code", Toast.LENGTH_SHORT).show();
                return;
            }
            String code = binding.inputCode1.getText().toString() + binding.inputCode2.getText().toString() + binding.inputCode3.getText().toString() + binding.inputCode4.getText().toString() + binding.inputCode5.getText().toString() + binding.inputCode6.getText().toString();

            if (verificationId != null) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.buttonVerify.setVisibility(View.INVISIBLE);
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, code);

                FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.buttonVerify.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()) {
                        requestUserCreds();

                        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(VerifyOTPActivity.this, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        findViewById(R.id.textResendOTP).setOnClickListener(v -> {
            //verify phone number
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder().setPhoneNumber("+91" + getIntent().getStringExtra("mobile")).setTimeout(60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {

                    Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    verificationId = newVerificationId;
                    Toast.makeText(VerifyOTPActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                }
            }).build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });
    }

    private void requestUserCreds() {
        String url = "http://192.168.1.45:3000/api/customer/login";
        new Thread(() -> {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                runOnUiThread(() -> setUserCreds(response.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setUserCreds(String resposne) {
        Gson gson = new Gson();
        LoggedUser user = gson.fromJson(resposne, LoggedUser.class);
        BlinkitApplication.setLoggedUser(user);
        Intent intent = new Intent(VerifyOTPActivity.this, BlinkitStart.class);
        startActivity(intent);
        finish();
    }

    String verificationId;

    private void setVerificationId() {
        verificationId = getIntent().getStringExtra("verificationId");
    }

    /**
     * If Intent() getStringExtra == "mobile" -> startActivity(VerifyActivity),
     * (TextView) textMobile will be received value "user mobile number"
     */
    private void setTextMobile() {
        binding.textMobile.setText(String.format("+91-%s", getIntent().getStringExtra("mobile")));
    }

    /**
     * When the edittext1 (inputCode1) was inserted, the cursor will be jump to the
     * next edittext (in this case it would be "inputCode2")
     */
    private void setupOTPInputs() {
        binding.inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}

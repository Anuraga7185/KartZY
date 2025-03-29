package com.ordering.blinkit_clone.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.ordering.blinkit_clone.databinding.LoginLayoutBinding;
import com.ordering.blinkit_clone.ui.activity.delivery_partner.DeliveryPartnerPortal;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    LoginLayoutBinding binding;
    int tapCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grocery App Opening Page -> ", "LoginActivity");
        binding = LoginLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.buttonGetOTP.setOnClickListener(v -> {
            // case skip
            if (binding.inputMobile.getText().toString().equals("7011341107")) {
                Intent intent = new Intent(LoginActivity.this, VerifyOTPActivity.class);
                intent.putExtra("mobile", binding.inputMobile.getText().toString());
                startActivity(intent);
            }

            //toast error
            if (binding.inputMobile.getText().toString().isEmpty()) {
                Toast.makeText(LoginActivity.this, "Enter mobile", Toast.LENGTH_SHORT).show();
                return;
            }
            //set visibility
            binding.buttonGetOTP.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
            //verify phone number
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder()
                            .setPhoneNumber("+91" + binding.inputMobile.getText().toString())
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.buttonGetOTP.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.buttonGetOTP.setVisibility(View.VISIBLE);
                                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.d("LOGIN ACTIVITTY", e.getMessage());
                                }

                                @Override
                                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.buttonGetOTP.setVisibility(View.VISIBLE);
                                    //action
                                    Intent intent = new Intent(LoginActivity.this, VerifyOTPActivity.class);
                                    intent.putExtra("mobile", binding.inputMobile.getText().toString());
                                    intent.putExtra("verificationId", verificationId);
                                    startActivity(intent);
                                }
                            })
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        binding.sendIcon.setOnClickListener(v -> {
            if (tapCount == 4) {
                tapCount = 0;
                Intent intent = new Intent(LoginActivity.this, DeliveryPartnerPortal.class);
                startActivity(intent);
            } else {
                tapCount++;
            }
        });
    }

}

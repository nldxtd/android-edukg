package com.lapluma.knowledg.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lapluma.knowledg.R;
import com.lapluma.knowledg.data.MainPref;
import com.lapluma.knowledg.model.TokenPayload;
import com.lapluma.knowledg.util.Network.CallbackOnResponse;
import com.lapluma.knowledg.util.Network.RestResponse;
import com.lapluma.knowledg.util.Network.JsonPostman;
import com.lapluma.knowledg.util.Network.Api;
import com.lapluma.knowledg.util.Tool;
import com.lapluma.knowledg.databinding.ActivitySignInUpBinding;

public class SignInUpActivity extends AppCompatActivity {

    private MutableLiveData<Integer> state; // 0 for signing in, 1 for signing up
    private MainPref mainPref;
    private ActivitySignInUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mainPref = new MainPref(this);
        state = new MutableLiveData<Integer>(-1);
        final Observer<Integer> stateObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer newState) {
                switch (newState) {
                    case 0: // signing in
                        runOnUiThread(()->{
                            findViewById(R.id.grp_repeat_pwd).setVisibility(View.INVISIBLE);
                            ((Button) findViewById(R.id.btn_sign_in)).setText(R.string.text_sign_in);
                            ((TextView) findViewById(R.id.text_new_user)).setText(R.string.text_new_user);
                            ((TextView) findViewById(R.id.hyperlink_sign_up)).setText(R.string.text_sign_up);
                        });
                        break;
                    case 1: // signing up
                        runOnUiThread(()->{
                            findViewById(R.id.grp_repeat_pwd).setVisibility(View.VISIBLE);
                            ((Button) findViewById(R.id.btn_sign_in)).setText(R.string.text_sign_up);
                            ((TextView) findViewById(R.id.text_new_user)).setText(R.string.text_have_account);
                            ((TextView) findViewById(R.id.hyperlink_sign_up)).setText(R.string.text_sign_in);

                        });
                        break;
                }
            }
        };
        state.observe(this, stateObserver);
        state.setValue(0);  // default state is signing in


        Tool.setSystemBarColor(this, R.color.blue_grey_900);
        Tool.setSystemBarColor(this);

        findViewById(R.id.btn_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = binding.inputUsername.getText().toString();
                String password = binding.inputPassword.getText().toString();
                if (state.getValue() == 1) { // if signing in, confirm repeat pwd is the same
                    String repeatPwd = binding.inputPasswordRepeat.getText().toString();
                    if (!repeatPwd.equals(password)) {
                        Tool.makeSnackBar(SignInUpActivity.this, getString(R.string.warn_pwd_not_the_same));
                        return;
                    }
                }
                JsonPostman postman = new JsonPostman(SignInUpActivity.this, Api.POST_SIGN_IN);
                postman.put("username", username);
                postman.put("password", Tool.encrypt(password));
                if (state.getValue() == 1) {
                    postman.setUrl(Api.POST_SIGN_UP);
                }
                postman.post(new TypeReference<RestResponse<TokenPayload>>() {}, new CallbackOnResponse<TokenPayload>() {
                    @Override
                    public void processResponse(RestResponse<TokenPayload> restResponse) {
                        String token = restResponse.getData().getToken();
                        mainPref.setUsername(username);
                        mainPref.setToken(token);
                        mainPref.setSignedIn(true);
                        SignInUpActivity.this.finish();
                    }
                });
            }
        });

        findViewById(R.id.hyperlink_sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)  {
                state.setValue(1 - state.getValue());
            }
        });
    }
}
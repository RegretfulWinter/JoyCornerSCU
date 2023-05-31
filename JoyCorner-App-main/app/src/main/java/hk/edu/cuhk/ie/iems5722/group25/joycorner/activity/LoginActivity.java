package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.QMUITopBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.animation.NormalDrawStrategy;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.animation.OpeningStartAnimation;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.LoadLoginAvatarEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.SetUserAvatarEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.ImageUtil;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.Security;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener {

    @BindView(R.id.e_username)
    @NotEmpty
    @Length(max = 20)
    EditText username;

    @BindView(R.id.e_password)
    @Password
    EditText password;

    @BindView(R.id.top_bar)
    QMUITopBar topBar;

    @BindView((R.id.loading_view))
    QMUILoadingView loadingView;

    @BindView(R.id.l_logo)
    QMUIRadiusImageView avatar;

    final Gson gson = new Gson();
    final Validator validator = new Validator(this);
    private Unbinder unbinder;
    private OpeningStartAnimation openingStartAnimation;

    public static User currentUser = null;
    public static Bitmap avatarBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        openingStartAnimation = new OpeningStartAnimation.Builder(this)
                .setDrawStrategy(new NormalDrawStrategy())
                .setAppIcon(ImageUtil.zoomDrawable(getResources().getDrawable(R.mipmap.logo), 300))
                .setAppName("JoyCorner")
                .setAppStatement("Joy Corner, Joy CUHK")
                .setAnimationInterval(2500)
                .setAnimationFinishTime(500)
                .create();
        openingStartAnimation.show(this);

        unbinder = ButterKnife.bind(this);
        validator.setValidationListener(this);
        topBar.setTitle("Sign in");
        loadingView.setColor(R.color.qmui_config_color_black);
        loadingView.setBackgroundColor(getResources().getColor(R.color.qmui_s_transparent));
        loadingView.setActivated(true);
        loadingView.setSize(100);
        loadingView.setVisibility(View.INVISIBLE);
        EventBus.getDefault().register(this);
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        loadingView.setVisibility(View.VISIBLE);
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(isLogin());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull Boolean alreadyLogin) {
                loadingView.setVisibility(View.INVISIBLE);
                if (alreadyLogin) {
                    Toast.makeText(getApplicationContext(), "Auto login", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("currentUserId", currentUser.getId());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }, 1000);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @OnTouch(R.id.layout)
    void touchLayout() {
        QMUIKeyboardHelper.hideKeyboard(username);
    }

    @OnClick(R.id.b_login)
    void clickLogin() {
        validator.validate();
        loadingView.setVisibility(View.VISIBLE);
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(login());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull Boolean isLoginSuccess) {
                loadingView.setVisibility(View.INVISIBLE);
                if (isLoginSuccess) {
                    Log.d("Login success", currentUser.toString());
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private boolean login() throws NoSuchAlgorithmException, IOException, JSONException {
        User user = User.builder()
                .username(username.getText().toString())
                .pw(Security.cipher(password.getText().toString()))
                .build();
        String response = NetworkService.postLogin(URL.POST_LOGIN, user);
        JSONObject object = new JSONObject(response);
        String status = object.getString("status");
        if (Status.OK.equals(status)) {
            SharedPreferences sharedPreferences = this.getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            for (Map.Entry<String, List<Cookie>> e : NetworkService.cookieStore.entrySet()) {
                if (URL.HOST.equals(e.getKey())) {
                    for (Cookie cookie : e.getValue()) {
                        editor.putString("rememberMe", cookie.toString());
                        editor.apply();
                        break;
                    }
                }
            }

            String data = object.getString("user");
            currentUser = gson.fromJson(data, User.class);
            EventBus.getDefault().post(new LoadLoginAvatarEvent(currentUser));
            return true;
        }
        return false;
    }

    @OnClick(R.id.b_register)
    void clickRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private boolean isLogin() {
        try {
            SharedPreferences sharedPreferences = this.getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
            String rememberString = sharedPreferences.getString("rememberMe", "none");
            if (rememberString.equals("none")) {
                return false;
            }
            HttpUrl url = HttpUrl.parse(URL.POST_LOGIN).newBuilder().build();
            Cookie cookie = Cookie.parse(url, rememberString);
            List<Cookie> cookies = new ArrayList<>();
            cookies.add(cookie);
            NetworkService.cookieStore.put(URL.HOST, cookies);
            String response = NetworkService.get(URL.GET_CURRENT_USER);
            JSONObject object = new JSONObject(response);
            String status = object.getString("status");
            if (Status.OK.equals(status)) {
                String data = object.getString("user");
                currentUser = gson.fromJson(data, User.class);
                EventBus.getDefault().post(new LoadLoginAvatarEvent(currentUser));
                return true;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onValidationSucceeded() {
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(LoadLoginAvatarEvent event) {
        String avatarPath = event.getUser().getAvatar();
        if ("/default.jpg".equals(avatarPath)) {
            EventBus.getDefault().post(new SetUserAvatarEvent(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar)));
            return;
        }

        avatarBitmap = ImageUtil.bitmapContainer.get(avatarPath);

        if (avatarBitmap != null) {
            EventBus.getDefault().post(new SetUserAvatarEvent(avatarBitmap));
        } else {
            File file = new File(avatarPath);
            if (file.exists()) {
                avatarBitmap = ImageUtil.zoomImg(avatarPath, 700);
                EventBus.getDefault().post(new SetUserAvatarEvent(avatarBitmap));
            } else {
                try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
                    String url = URL.GET_FILE + "/"
                            + "avatar"
                            + "?file_path=" + avatarPath;
                    byte[] response = NetworkService.getFile(url);
                    stream.write(response, 0, response.length);
                    stream.flush();
                    avatarBitmap = ImageUtil.zoomImg(avatarPath, 700);
                    EventBus.getDefault().post(new SetUserAvatarEvent(avatarBitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SetUserAvatarEvent event) {
        avatar.setImageBitmap(event.getBitmap());
        loadingView.setVisibility(View.VISIBLE);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("currentUserId", currentUser.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        loadingView.setVisibility(View.INVISIBLE);
    }
}
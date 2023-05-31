package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.Status;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.RequestLocationEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment.DatePickerFragment;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.Security;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RegisterActivity extends AppCompatActivity implements Validator.ValidationListener, DatePickerFragment.OnDateSetListener {
    @BindView(R.id.e_username)
    @NotEmpty
    @Length(max = 20)
    EditText username;

    @BindView(R.id.e_nickname)
    @NotEmpty
    @Length(max = 20)
    EditText nickname;

    @BindView(R.id.e_password)
    @Password(message = "Minimum length is 6")
    EditText password;

    @BindView(R.id.e_confirm_password)
    @ConfirmPassword
    EditText confirmPassword;

    @BindView(R.id.e_location)
    @NotEmpty
    @Length(max = 20)
    EditText location;

    @BindView(R.id.e_birth)
    EditText birth;

    @BindView(R.id.s_sex)
    Switch sex;

    @BindView(R.id.e_sex_f)
    EditText e_sex_f;

    @BindView(R.id.e_sex_m)
    EditText e_sex_m;

    @BindView(R.id.e_signature)
    @NotEmpty
    @Length(max = 20)
    EditText signature;

    @BindView(R.id.top_bar)
    QMUITopBar topBar;

    final Gson gson = new Gson();
    private Unbinder unbinder;
    final Validator validator = new Validator(this);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE);
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onValidationSucceeded() {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.onNext(register());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull Boolean isRegisterSuccess) {
                if (isRegisterSuccess) {
                    Log.d("Register success", LoginActivity.currentUser.toString());
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("currentUserId", LoginActivity.currentUser.getId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
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

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        unbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        validator.setValidationListener(this);
        topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
        topBar.setTitle("Sign Up");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        EventBus.getDefault().post(new RequestLocationEvent());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location l : locationResult.getLocations()) {
                    Geocoder geocoder = new Geocoder(getApplication());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
                        location.setText(addresses.get(0).getAddressLine(0).split(",")[1].trim());
                        location.setClickable(false);
                        location.setFocusableInTouchMode(false);
                        location.setClickable(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(new RequestLocationEvent());
    }

    @OnTouch(R.id.layout)
    void touchLayout() {
        QMUIKeyboardHelper.hideKeyboard(username);
    }

    @OnClick(R.id.e_birth)
    void clickBirth() {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "datePicker");
    }

    @OnCheckedChanged(R.id.s_sex)
    void checkSex() {
        if (sex.isChecked()) {
            e_sex_m.setText(sex.getTextOn());
            e_sex_f.setText("");
        } else {
            e_sex_f.setText(sex.getTextOff());
            e_sex_m.setText("");
        }
    }

    @OnClick(R.id.b_register)
    void clickRegister() {
        validator.validate();
    }

    private Boolean register() throws NoSuchAlgorithmException, IOException, JSONException {
        User user = User.builder()
                .username(username.getText().toString())
                .nickname(nickname.getText().toString())
                .avatar("/default.jpg")
                .location(location.getText().toString())
                .birth(birth.getText().toString().replaceAll("-", ""))
                .sex(sex.isChecked() ? "1" : "0")
                .signature(signature.getText().toString())
                .pw(Security.cipher(password.getText().toString()))
                .build();
        String response = NetworkService.postRegister(URL.POST_REGISTER, user);
        JSONObject object = new JSONObject(response);
        String status = object.getString("status");
        if (Status.OK.equals(status)) {
            String data = object.getString("user");
            LoginActivity.currentUser = gson.fromJson(data, User.class);
            return true;
        }
        return false;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);
        birth.setText(sdf.format(c.getTime()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(RequestLocationEvent event) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
}

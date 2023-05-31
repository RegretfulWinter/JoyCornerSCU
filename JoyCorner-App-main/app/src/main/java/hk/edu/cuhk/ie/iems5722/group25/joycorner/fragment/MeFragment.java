package hk.edu.cuhk.ie.iems5722.group25.joycorner.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.AboutActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.LoginActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.PatchUserEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.SaveBitmapEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.NetworkService;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.utils.TimeUtil;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MeFragment extends BaseFragment {
    private Unbinder unbinder;
    private ActivityResultLauncher<Intent> takePhotoLauncher;
    private ActivityResultLauncher<Intent> choosePhotoLauncher;
    private ActivityResultLauncher<Intent> cuttingDownLauncher;
    private final String TAG_TAKE_PHOTO = "0";
    private final String TAG_CHOOSE_FROM_ALBUM = "1";
    private Uri photoOutputUri;

    @BindView(R.id.group_list_view)
    QMUIGroupListView groupListView;

    @BindView(R.id.l_logo)
    QMUIRadiusImageView avatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.me_view, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        initActivityResult();
        EventBus.getDefault().register(this);
        return view;
    }

    private void initActivityResult() {
        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        avatar.setImageBitmap(imageBitmap);
                        EventBus.getDefault().post(new SaveBitmapEvent(imageBitmap));
                    }
                });

        choosePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent cropPhotoIntent = new Intent("com.android.camera.action.CROP");
                        // 设置数据Uri 和类型
                        cropPhotoIntent.setDataAndType(result.getData().getData(), "image/*");
                        // 授权应用读取 Uri，这一步要有，不然裁剪程序会崩溃
                        cropPhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        // 设置图片的最终输出目录
                        cropPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                photoOutputUri = Uri.parse("file:////sdcard/image_output.jpg"));
                        cuttingDownLauncher.launch(cropPhotoIntent);
                    }
                });

        cuttingDownLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Bitmap bitmap = BitmapFactory.decodeFile(photoOutputUri.getPath());
                    avatar.setImageBitmap(bitmap);
                    EventBus.getDefault().post(new SaveBitmapEvent(bitmap));
                });
    }

    private void initView() {
        User me = LoginActivity.currentUser;
        if (LoginActivity.avatarBitmap != null) {
            avatar.setImageBitmap(LoginActivity.avatarBitmap);
        }

        QMUICommonListItemView item = groupListView.createItemView(me.getNickname());
        item.setDetailText("username: " + me.getUsername());
        item.setOrientation(QMUICommonListItemView.VERTICAL);
        item.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        QMUICommonListItemView item_location = groupListView.createItemView("Location");
        item_location.setDetailText(me.getLocation());
        item_location.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        QMUICommonListItemView item_birth = groupListView.createItemView("Birth");
        item_birth.setDetailText(TimeUtil.formatBirth(me.getBirth()));

        QMUICommonListItemView item_sex = groupListView.createItemView("Sex");
        item_sex.setDetailText("0".equals(me.getSex()) ? "female" : "male");

        QMUICommonListItemView item_signature = groupListView.createItemView("Signature");
        item_signature.setDetailText(me.getSignature());
        item_signature.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        QMUIGroupListView.newSection(getContext()).addItemView(item, v -> {
            QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getContext());
            QMUIDialogAction cancel = new QMUIDialogAction("Cancel", (dialog, index) -> {
                dialog.dismiss();
            });
            QMUIDialogAction confirm = new QMUIDialogAction("Confirm", (dialog, index) -> {
                String input = builder.getEditText().getText().toString();
                item.setText(input);
                EventBus.getDefault().post(new PatchUserEvent("nickname", input));
                dialog.dismiss();
            });
            builder.setTitle("Nickname")
                    .setPlaceholder("Enter your new nickname here")
                    .setDefaultText(me.getNickname())
                    .addAction(cancel)
                    .addAction(confirm)
                    .show();
        }).addItemView(item_location, v -> {
            QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getContext());
            QMUIDialogAction cancel = new QMUIDialogAction("Cancel", (dialog, index) -> {
                dialog.dismiss();
            });
            QMUIDialogAction confirm = new QMUIDialogAction("Confirm", (dialog, index) -> {
                String input = builder.getEditText().getText().toString();
                item_location.setDetailText(input);
                EventBus.getDefault().post(new PatchUserEvent("location", input));
                dialog.dismiss();
            });
            builder.setTitle("Location")
                    .setPlaceholder("Enter your new location here")
                    .setDefaultText(me.getLocation())
                    .addAction(cancel)
                    .addAction(confirm)
                    .show();
        }).addItemView(item_birth, v -> {
            Toast.makeText(getContext(), "Modifying birthday is not allowed", Toast.LENGTH_SHORT).show();
        }).addItemView(item_sex, v -> {
            Toast.makeText(getContext(), "Gender modification is not allowed", Toast.LENGTH_SHORT).show();
        }).addItemView(item_signature, v -> {
            QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getContext());
            QMUIDialogAction cancel = new QMUIDialogAction("Cancel", (dialog, index) -> {
                dialog.dismiss();
            });
            QMUIDialogAction confirm = new QMUIDialogAction("Confirm", (dialog, index) -> {
                String input = builder.getEditText().getText().toString();
                item_signature.setDetailText(input);
                EventBus.getDefault().post(new PatchUserEvent("signature", input));
                dialog.dismiss();
            });
            builder.setTitle("Signature")
                    .setPlaceholder("Enter your new signature here")
                    .setDefaultText(me.getSignature())
                    .addAction(cancel)
                    .addAction(confirm)
                    .show();
        }).addTo(groupListView);

        QMUICommonListItemView item_about = groupListView.createItemView("About JoyCorner");
        QMUICommonListItemView item_log_out = groupListView.createItemView("Log out");

        QMUIGroupListView.newSection(getContext())
                .addItemView(item_about, v -> {
                    Intent intent = new Intent(getContext(), AboutActivity.class);
                    startActivity(intent);
                })
                .addItemView(item_log_out, v -> {
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("rememberMe");
                    editor.apply();
                    LoginActivity.currentUser = null;
                    LoginActivity.avatarBitmap = null;
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                }).addTo(groupListView);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.l_logo)
    public void clickAvatar() {
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(getContext());
        builder.addItem("Change Personal Avatar")
                .addItem("> Take a photo", TAG_TAKE_PHOTO)
                .addItem("> Choose from phone album", TAG_CHOOSE_FROM_ALBUM)
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {

                    switch (tag) {
                        case TAG_TAKE_PHOTO:
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                                takePhotoLauncher.launch(takePictureIntent);
                            }
                            dialog.dismiss();
                            break;
                        case TAG_CHOOSE_FROM_ALBUM:
                            dialog.dismiss();

                            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            } else {
                                Intent chooseIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                chooseIntent.setType("image/*");
                                choosePhotoLauncher.launch(chooseIntent);
                            }
                            dialog.dismiss();

                            break;
                        default:
                            break;
                    }
                }).build().show();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(SaveBitmapEvent event) {
        try {
            String path = getContext().getFilesDir().getAbsolutePath() + "/avatar";
            File mDir = new File(path);
            if (!mDir.exists()) {
                mDir.mkdirs();
            }
            String imagePath = mDir + new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
            File mPhoto = new File(imagePath);
            FileOutputStream output = new FileOutputStream(mPhoto);
            event.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, output);
            output.flush();
            output.close();
            LoginActivity.currentUser.setAvatar(imagePath);
            RequestBody image = RequestBody.create(mPhoto, MediaType.parse("image/png"));
            RequestBody multiBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("user_id", String.valueOf(LoginActivity.currentUser.getId()))
                    .addFormDataPart("path", imagePath)
                    .addFormDataPart("file", imagePath, image)
                    .build();

            NetworkService.postAvatar(multiBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(PatchUserEvent event) {
        String url = String.format(URL.PATCH_USER, event.getUserId(), event.getType());
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add(event.getType(), event.getValue())
                    .build();
            NetworkService.patch(url, formBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

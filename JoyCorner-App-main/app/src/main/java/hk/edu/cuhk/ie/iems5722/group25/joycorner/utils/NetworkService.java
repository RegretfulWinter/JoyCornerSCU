package hk.edu.cuhk.ie.iems5722.group25.joycorner.utils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.MsgContentType;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.URL;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.Message;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkService {
    public static final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    static final OkHttpClient client = new OkHttpClient().newBuilder().cookieJar(new CookieJar() {
        @Override
        public void saveFromResponse(@NonNull HttpUrl httpUrl, @NonNull List<Cookie> list) {
            cookieStore.put(httpUrl.host(), list);
        }

        @NonNull
        @Override
        public List<Cookie> loadForRequest(@NonNull HttpUrl httpUrl) {
            List<Cookie> cookies = cookieStore.get(httpUrl.host());
            return cookies != null ? cookies : new ArrayList<>();
        }
    }).build();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static byte[] getFile(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().bytes();
        }
    }

    public static String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String post(String url, RequestBody formBody) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String patch(String url, RequestBody formBody) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .patch(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String postLogin(String url, User user) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("username", user.getUsername())
                .add("pw", user.getPw())
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String postRegister(String url, User user) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("username", user.getUsername())
                .add("nickname", user.getNickname())
                .add("avatar", user.getAvatar())
                .add("location", user.getLocation())
                .add("birth", user.getBirth())
                .add("sex", user.getSex())
                .add("signature", user.getSignature())
                .add("pw", user.getPw())
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String postMessage(Message message) throws IOException {
        Request request;
        if (message.getContentType().equals(MsgContentType.TEXT)) {
            RequestBody formBody = new FormBody.Builder()
                    .add("from_id", String.valueOf(message.getFromId()))
                    .add("dest_id", String.valueOf(message.getDestId()))
                    .add("content", message.getContent())
                    .add("content_type", message.getContentType())
                    .add("msg_type", message.getMsgType())
                    .build();
            request = new Request.Builder()
                    .url(URL.POST_MESSAGE)
                    .post(formBody)
                    .build();
        } else if (message.getContentType().equals(MsgContentType.IMAGE)) {
            File imageFile = new File(message.getContent());
            RequestBody image = RequestBody.create(imageFile, MediaType.parse("image/png"));
            RequestBody multiBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("from_id", String.valueOf(message.getFromId()))
                    .addFormDataPart("dest_id", String.valueOf(message.getDestId()))
                    .addFormDataPart("content", message.getContent())
                    .addFormDataPart("content_type", message.getContentType())
                    .addFormDataPart("msg_type", message.getMsgType())
                    .addFormDataPart("file", message.getContent(), image)
                    .build();
            request = new Request.Builder()
                    .url(URL.POST_MESSAGE)
                    .post(multiBody)
                    .build();
        } else if (message.getContentType().equals(MsgContentType.VIDEO)) {
            File videoFile = new File(message.getContent());
            RequestBody video = RequestBody.create(videoFile, MediaType.parse("mp4/*"));
            RequestBody multiBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("from_id", String.valueOf(message.getFromId()))
                    .addFormDataPart("dest_id", String.valueOf(message.getDestId()))
                    .addFormDataPart("content", message.getContent())
                    .addFormDataPart("content_type", message.getContentType())
                    .addFormDataPart("msg_type", message.getMsgType())
                    .addFormDataPart("file", message.getContent(), video)
                    .build();
            request = new Request.Builder()
                    .url(URL.POST_MESSAGE)
                    .post(multiBody)
                    .build();
        } else {
            return null;
        }

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static void postAvatar(RequestBody multiBody) {
        Request request = new Request.Builder()
                .url(URL.POST_AVATAR)
                .post(multiBody)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package hk.edu.cuhk.ie.iems5722.group25.joycorner.constant;

public class URL {

    public static final String HOST = "54.91.186.201";

    private static final String PRO_IP_PORT = "http://" + HOST + ":80";

    public static final String GET_CURRENT_USER = PRO_IP_PORT + "/user/current";
    public static final String POST_LOGIN = PRO_IP_PORT + "/login";
    public static final String POST_REGISTER = PRO_IP_PORT + "/user";
    public static final String GET_USER_FRIENDS = PRO_IP_PORT + "/user/friends/";
    public static final String GET_USER_COMMUNICATIONS = PRO_IP_PORT + "/communications/user/";
    public static final String GET_MESSAGES = PRO_IP_PORT + "/messages/user/%d?contact_id=%d&contact_type=%s&page_index=%d&page_size=%d";
    public static final String POST_MESSAGE = PRO_IP_PORT + "/message";
    public static final String GET_FILE = PRO_IP_PORT + "/file";
    public static final String POST_AVATAR = PRO_IP_PORT + "/avatar";
    public static final String PATCH_USER = PRO_IP_PORT + "/user/%d/%s";
    public static final String PATCH_CONTACT = PRO_IP_PORT + "/contact/%d/%d/%s";
    public static final String GET_USER_BY_CONDITION = PRO_IP_PORT + "/user/attribute/%s";
    public static final String POST_FRIEND = PRO_IP_PORT + "/friend";
    public static final String POST_FCM_TOKEN = PRO_IP_PORT + "/token";
    public static final String GET_USER_BY_ID = PRO_IP_PORT + "/user/%s";
    public static final String PATCH_FRIEND = PRO_IP_PORT + "/friend";
    public static final String POST_ACTIVITY = PRO_IP_PORT + "/activity";
    public static final String GET_ACTIVITY_HISTORY = PRO_IP_PORT + "/activity?min_index=%d&page_size=%d";
    public static final String GET_ACTIVITY_NEW = PRO_IP_PORT + "/activity?max_index=%d&page_size=%d";
    public static final String GET_ACTIVITY_PARTICIPANTS = PRO_IP_PORT + "/activity/participants/%d";
    public static final String GET_ACTIVITY_JOINED = PRO_IP_PORT + "/activity/user/%d";
    public static final String PATCH_ACTIVITY = PRO_IP_PORT + "/activity/%d";
    public static final String GET_BOT_RES = PRO_IP_PORT + "/chatterbot/%s";
}

package hk.edu.cuhk.ie.iems5722.group25.joycorner.constant;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;

public class ActivityGameName {
    public static final String LRS = "The Werewolves";
    public static final String AWL = "Avalon";
    public static final String CCBS = "Splendor";
    public static final String CHESS = "CHESS";
    public static final String HZWY = "Azul";
    public static final String KD = "Catan";
    public static final String KKS = "Carcassonne";
    public static final String MJ = "Mahjong";
    public static final String QDQJ = "7 Wonders Duel";
    public static final String QLDYX = "A Game of Thrones";
    public static final String STLN = "Santorini";
    public static final String WQ = "Go";
    public static final String WS = "Wingspan";

    public static final String[] NAMES = {LRS, AWL, CCBS, CHESS, HZWY, KD, KKS, MJ, QDQJ, QLDYX, STLN, WQ, WS};

    public static int getGameDrawable(String name) {
        switch (name) {
            case LRS:
                return R.drawable.lrs;
            case AWL:
                return R.drawable.awl;
            case CCBS:
                return R.drawable.ccbs;
            case CHESS:
                return R.drawable.chess;
            case HZWY:
                return R.drawable.hzwy;
            case KD:
                return R.drawable.kd;
            case KKS:
                return R.drawable.kks;
            case MJ:
                return R.drawable.mj;
            case QDQJ:
                return R.drawable.qdqj;
            case QLDYX:
                return R.drawable.qldyx;
            case STLN:
                return R.drawable.stln;
            case WQ:
                return R.drawable.wq;
            case WS:
                return R.drawable.ws;
            default:
                return R.drawable.happy;
        }
    }
}

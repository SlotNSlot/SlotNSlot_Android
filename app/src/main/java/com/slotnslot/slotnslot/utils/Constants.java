package com.slotnslot.slotnslot.utils;

import com.slotnslot.slotnslot.R;

public class Constants {
    public static final int EXPANSION_MAIN_VERSION = 2;

    public static final int DURATION = 1500;

    public static int items[] = {R.drawable.symbol_1,
            R.drawable.symbol_2, R.drawable.symbol_3,
            R.drawable.symbol_4, R.drawable.symbol_5,
            R.drawable.symbol_6, R.drawable.symbol_7,
            R.drawable.symbol_8};
    //    TODO: AFTER BETA
//            , R.drawable.symbol_9,
//            R.drawable.symbol_10, R.drawable.symbol_11,
//            R.drawable.symbol_12, R.drawable.symbol_13};
    public static final Integer UNDEFINE = -1;
    public static final String UNDEFINE_STRING = "UNDEFINE";
    public static Integer[][] WIN_LINE = {{0, 0, 0, 0, 0, 0}, {1, 1, 1, 1, 1, 1}, {2, 2, 2, 2, 2, 2}, {2, 1, 0, 1, 2, 3}, {0, 1, 2, 1, 0, 4},
            {0, 0, 1, 2, 2, 5}, {2, 2, 1, 0, 0, 6}, {1, 0, 0, 0, 1, 7}, {1, 2, 2, 2, 1, 8}, {0, 1, 0, 1, 0, 9},
            {2, 1, 2, 1, 2, 10}, {1, 2, 1, 2, 1, 11}, {1, 0, 1, 0, 1, 12}, {1, 1, 0, 1, 1, 13}, {1, 1, 2, 1, 1, 14},
            {0, 1, 1, 1, 0, 15}, {2, 1, 1, 1, 2, 16}, {0, 2, 0, 2, 0, 17}, {2, 0, 2, 0, 2, 18}, {2, 0, 1, 0, 2, 19}};
    public static int[][][] LINE_CASE_2000 = {{{7, 5}},
            {{6, 5}},
            {{7, 4}},
            {{7, 3}, {6, 4}, {5, 5}},
            {{5, 4}, {4, 5}},
            {{6, 3}, {4, 4}},
            {{5, 3}, {3, 5}},
            {{4, 3}, {3, 4}, {2, 5}},
            {{3, 3}, {2, 4}, {1, 5}},
            {{2, 3}, {1, 4}, {0, 5}},
            {{0, 4}},
            {{1, 3}, {0, 3}}};
    public static int[] LINE_CASE_2000_VALUE = {2000, 1000, 500, 250, 150, 125, 100, 75, 50, 25, 10, 5};

    public static int BET_MAX_LINE = 20;
    public static int BET_MIN_LINE = 1;
    public static double BET_MIN_ETH = 0.001;

    public static final int READ_EXTERNAL_PERMISSION_REQUEST_CODE = 0;
    public static final int WRITE_EXTERNAL_PERMISSION_REQUEST_CODE = 1;

    public static final String EMPTY_STRING = "";
    public static final String PLAY_BET_LINES_TEXT_FORMAT = "%d line";
    public static final String PLAY_BET_ETH_TEXT_FORMAT = "%.3f ETH";
    public static final String PLAY_BET_TOTAL_BET_FORMAT = "%.3f ETH";
    public static final String PLAY_PLAYERS_BALANCE_TEXT_FORMAT = "%.4f ETH";
    public static final String PLAY_LAST_WIN_TEXT_FFORMAT = "+ %.3f ETH";
    public static final String HIT_RATIO_TEXT_FORMAT = "%.1f %%";
    public static final String BET_RANGE_TEXT_FORMAT = "%.3f - %.3f ETH";
    public static final String TOTAL_STAKE_TEXT_FORMAT = "%.3f ETH";
    public static final String MAX_PRIZE_TEXT_FORMAT = "x%d ";

    public static final String BUNDLE_KEY_LIST_TYPE = "LIST_TYPE";
    public static final String BUNDLE_KEY_SLOT_ROOM = "SLOT_ROOM";
    public static final String BUNDLE_KEY_SLOT_ROOM_DEPOSIT = "SLOT_ROOM_DEPOSIT";
    public static final String BUNDLE_KEY_STEP_INDEX = "STEP_INDEX";

    public static final String MAKE_SLOT_STEP_TITLE_SUMMARY = "Summary";
    public static final String MY_PAGE_TITLE_WALLET = "Wallet";
    public static final String MY_PAGE_TITLE_WITHDRAW_ETH = "Withdraw ETH";
    public static final String ACTIVITY_EXTRA_KEY_SLOT_TYPE = "SLOT_TYPE";
    public static final String SLOT_PLAYER_TITLE = "Play";
    public static final String SLOT_BANKER_TITLE = "Watch Game";

    public static final String BANKER_SEED_KEY = "banker_seed";
    public static final String PLAYER_SEED_KEY = "player_seed";
}

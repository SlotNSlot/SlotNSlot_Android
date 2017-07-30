package com.slotnslot.slotnslot.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.utils.Constants;

import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.generated.Bytes32;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Seed {
    private int round = 0;

    private String seed1 = String.valueOf(Math.random());
    private String seed2 = String.valueOf(Math.random());
    private String seed3 = String.valueOf(Math.random());

    private int repeat1 = 100;
    private int repeat2 = 100;
    private int repeat3 = 100;

    private String opponentSeed1 = String.valueOf(Math.random());
    private String opponentSeed2 = String.valueOf(Math.random());
    private String opponentSeed3 = String.valueOf(Math.random());

    public Seed() {}

    public Seed(int round, String seed1, String seed2, String seed3, int repeat1, int repeat2, int repeat3, String opponentSeed1, String opponentSeed2, String opponentSeed3) {
        this();
        this.round = round;
        this.seed1 = seed1;
        this.seed2 = seed2;
        this.seed3 = seed3;
        this.repeat1 = repeat1;
        this.repeat2 = repeat2;
        this.repeat3 = repeat3;
        this.opponentSeed1 = opponentSeed1;
        this.opponentSeed2 = opponentSeed2;
        this.opponentSeed3 = opponentSeed3;
    }

    public StaticArray<Bytes32> getInitialSeed() {
        this.seed1 = String.valueOf(Math.random());
        this.seed2 = String.valueOf(Math.random());
        this.seed3 = String.valueOf(Math.random());

        this.repeat1 = 100;
        this.repeat2 = 100;
        this.repeat3 = 100;

        return new StaticArray<>(
                new Bytes32(Utils.generateRandom(seed1, repeat1--)),
                new Bytes32(Utils.generateRandom(seed2, repeat2--)),
                new Bytes32(Utils.generateRandom(seed3, repeat3--))
        );
    }

    private static String getString(Activity activity, int id) {
        return activity.getResources().getString(id);
    }

    public Bytes32 getSeed() {
        return new Bytes32(Utils.generateRandom(selectSeed(), selectRepeat()));
    }

    private String selectSeed() {
        int idx = round % 3;
        String seed;
        if (idx == 0) {
            seed = seed1;
        } else if (idx == 1) {
            seed = seed2;
        } else {
            seed = seed3;
        }
        System.out.println("round : " + round + ", idx : " + idx + ", seed : " + seed);
        return seed;
    }

    private int selectRepeat() {
        int idx = round % 3;
        int repeat;
        if (idx == 0) {
            repeat = repeat1;
        } else if (idx == 1) {
            repeat = repeat2;
        } else {
            repeat = repeat3;
        }
        System.out.println("round : " + round + ", idx : " + idx + ", repeat : " + repeat);
        return repeat;
    }

    public int nextRound() {
        int idx = round % 3;
        if (idx == 0) {
            repeat1--;
        } else if (idx == 1) {
            repeat2--;
        } else {
            repeat3--;
        }
        return ++round;
    }

    public int getIndex() {
        return round % 3;
    }

    public void save(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(getString(activity, R.string.shared_preferences_seed_round), round);

        editor.putString(getString(activity, R.string.shared_preferences_seed_seed1), seed1);
        editor.putString(getString(activity, R.string.shared_preferences_seed_seed2), seed2);
        editor.putString(getString(activity, R.string.shared_preferences_seed_seed3), seed3);

        editor.putInt(getString(activity, R.string.shared_preferences_seed_repeat1), repeat1);
        editor.putInt(getString(activity, R.string.shared_preferences_seed_repeat2), repeat2);
        editor.putInt(getString(activity, R.string.shared_preferences_seed_repeat3), repeat3);

        editor.putString(getString(activity, R.string.shared_preferences_seed_opponent_seed1), opponentSeed1);
        editor.putString(getString(activity, R.string.shared_preferences_seed_opponent_seed2), opponentSeed2);
        editor.putString(getString(activity, R.string.shared_preferences_seed_opponent_seed3), opponentSeed3);
        editor.commit();
    }

    public static Seed load(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        int round = sharedPref.getInt(getString(activity, R.string.shared_preferences_seed_round), Constants.UNDEFINE);
        String seed1 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_seed1), Constants.UNDEFINE_STRING);
        String seed2 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_seed2), Constants.UNDEFINE_STRING);
        String seed3 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_seed3), Constants.UNDEFINE_STRING);
        int repeat1 = sharedPref.getInt(getString(activity, R.string.shared_preferences_seed_repeat1), Constants.UNDEFINE);
        int repeat2 = sharedPref.getInt(getString(activity, R.string.shared_preferences_seed_repeat2), Constants.UNDEFINE);
        int repeat3 = sharedPref.getInt(getString(activity, R.string.shared_preferences_seed_repeat3), Constants.UNDEFINE);
        String opponentSeed1 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_opponent_seed1), Constants.UNDEFINE_STRING);
        String opponentSeed2 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_opponent_seed2), Constants.UNDEFINE_STRING);
        String opponentSeed3 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_opponent_seed3), Constants.UNDEFINE_STRING);
        if (round != Constants.UNDEFINE && !seed1.equals(Constants.UNDEFINE_STRING) && !seed2.equals(Constants.UNDEFINE_STRING) &&
                !seed3.equals(Constants.UNDEFINE_STRING) && repeat1 != Constants.UNDEFINE && repeat2 != Constants.UNDEFINE &&
                repeat3 != Constants.UNDEFINE && !opponentSeed1.equals(Constants.UNDEFINE_STRING) && !opponentSeed2.equals(Constants.UNDEFINE_STRING) &&
                !opponentSeed3.equals(Constants.UNDEFINE_STRING)) {
            return null;
        }
        return new Seed(round, seed1, seed2, seed3, repeat1, repeat2, repeat3, opponentSeed1, opponentSeed2, opponentSeed3);
    }
}

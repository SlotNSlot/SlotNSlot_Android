package com.slotnslot.slotnslot.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.utils.Constants;

import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Hash;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PlayerSeed extends Seed {
    private int index = 0;
    private String[] bankerSeeds = new String[3];
    private String nextBankerSeed;

    public PlayerSeed() {
    }

    public PlayerSeed(int index, String[] seeds, int[] repeats, String[] bankerSeeds) {
        this();
        this.index = index;
        this.seeds = seeds;
        this.repeats = repeats;
        this.bankerSeeds = bankerSeeds;
    }

    public Bytes32 getSeed() {
        return getSeed(index);
    }

    public void confirm(int idx) {
        if (index != idx) {
            return;
        }
        super.confirm(index);
        bankerSeeds[index] = nextBankerSeed;

        index = ++index % 3;
    }

    public boolean isValidSeed(String bankerSeed) {
        String previous = bankerSeeds[index];
        if (Utils.isEmpty(previous)) {
            return false;
        }
        return previous.equals(Hash.sha3(bankerSeed));
    }

    public void setNextBankerSeed(String nextBankerSeed) {
        this.nextBankerSeed = nextBankerSeed;
    }

    public void setBankerSeeds(String seed0, String seed1, String seed2) {
        bankerSeeds[0] = seed0;
        bankerSeeds[1] = seed1;
        bankerSeeds[2] = seed2;
    }

    private static String getString(Activity activity, int id) {
        return activity.getResources().getString(id);
    }


    public void save(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(getString(activity, R.string.shared_preferences_seed_index), index);

        editor.putString(getString(activity, R.string.shared_preferences_seed_seed1), seeds[0]);
        editor.putString(getString(activity, R.string.shared_preferences_seed_seed2), seeds[1]);
        editor.putString(getString(activity, R.string.shared_preferences_seed_seed3), seeds[2]);

        editor.putInt(getString(activity, R.string.shared_preferences_seed_repeat1), repeats[0]);
        editor.putInt(getString(activity, R.string.shared_preferences_seed_repeat2), repeats[1]);
        editor.putInt(getString(activity, R.string.shared_preferences_seed_repeat3), repeats[2]);

        editor.putString(getString(activity, R.string.shared_preferences_seed_opponent_seed1), bankerSeeds[0]);
        editor.putString(getString(activity, R.string.shared_preferences_seed_opponent_seed2), bankerSeeds[1]);
        editor.putString(getString(activity, R.string.shared_preferences_seed_opponent_seed3), bankerSeeds[2]);
        editor.commit();
    }

    public static PlayerSeed load(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        int index = sharedPref.getInt(getString(activity, R.string.shared_preferences_seed_index), Constants.UNDEFINE);
        String seed1 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_seed1), Constants.UNDEFINE_STRING);
        String seed2 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_seed2), Constants.UNDEFINE_STRING);
        String seed3 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_seed3), Constants.UNDEFINE_STRING);
        int repeat1 = sharedPref.getInt(getString(activity, R.string.shared_preferences_seed_repeat1), Constants.UNDEFINE);
        int repeat2 = sharedPref.getInt(getString(activity, R.string.shared_preferences_seed_repeat2), Constants.UNDEFINE);
        int repeat3 = sharedPref.getInt(getString(activity, R.string.shared_preferences_seed_repeat3), Constants.UNDEFINE);
        String opponentSeed1 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_opponent_seed1), Constants.UNDEFINE_STRING);
        String opponentSeed2 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_opponent_seed2), Constants.UNDEFINE_STRING);
        String opponentSeed3 = sharedPref.getString(getString(activity, R.string.shared_preferences_seed_opponent_seed3), Constants.UNDEFINE_STRING);
        if (index != Constants.UNDEFINE && !seed1.equals(Constants.UNDEFINE_STRING) && !seed2.equals(Constants.UNDEFINE_STRING) &&
                !seed3.equals(Constants.UNDEFINE_STRING) && repeat1 != Constants.UNDEFINE && repeat2 != Constants.UNDEFINE &&
                repeat3 != Constants.UNDEFINE && !opponentSeed1.equals(Constants.UNDEFINE_STRING) && !opponentSeed2.equals(Constants.UNDEFINE_STRING) &&
                !opponentSeed3.equals(Constants.UNDEFINE_STRING)) {
            return null;
        }
        return new PlayerSeed(
                index,
                new String[]{seed1, seed2, seed3},
                new int[]{repeat1, repeat2, repeat3},
                new String[]{opponentSeed1, opponentSeed2, opponentSeed3}
        );
    }
}

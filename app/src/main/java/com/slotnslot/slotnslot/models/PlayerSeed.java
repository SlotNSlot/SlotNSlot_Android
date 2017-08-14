package com.slotnslot.slotnslot.models;

import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.utils.StorageUtil;

import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Hash;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
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

    @Override
    public StaticArray<Bytes32> getInitialSeed() {
        this.index = 0;
        return super.getInitialSeed();
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

    public void save(String slotAddress) {
        Completable
                .complete()
                .observeOn(Schedulers.io())
                .subscribe(() -> StorageUtil.save(Constants.PLAYER_SEED_KEY, slotAddress, this));
    }

    public static Single<Seed> load(String slotAddress) {
        return Single
                .<Seed>create(e -> {
                    PlayerSeed load = StorageUtil.load(Constants.PLAYER_SEED_KEY, slotAddress, PlayerSeed.class);
                    if (load == null) {
                        e.onSuccess(new PlayerSeed());
                    } else {
                        e.onSuccess(load);
                    }
                })
                .subscribeOn(Schedulers.io());
    }
}

package com.slotnslot.slotnslot.models;

import com.slotnslot.slotnslot.geth.Utils;

import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.generated.Bytes32;

public class Seed {
    String[] seeds = new String[3];
    int[] repeats = new int[3];

    public StaticArray<Bytes32> getInitialSeed() {
        this.seeds[0] = String.valueOf(Math.random());
        this.seeds[1] = String.valueOf(Math.random());
        this.seeds[2] = String.valueOf(Math.random());

        this.repeats[0] = 100;
        this.repeats[1] = 100;
        this.repeats[2] = 100;

        return new StaticArray<>(
                new Bytes32(Utils.generateRandom(seeds[0], repeats[0]--)),
                new Bytes32(Utils.generateRandom(seeds[1], repeats[1]--)),
                new Bytes32(Utils.generateRandom(seeds[2], repeats[2]--))
        );
    }

    public Bytes32 getSeed(int idx) {
        return new Bytes32(Utils.generateRandom(selectSeed(idx), selectRepeat(idx)));
    }

    private String selectSeed(int idx) {
        String seed = seeds[idx];
        System.out.println("idx : " + idx + ", seed : " + seed);
        return seed;
    }

    private int selectRepeat(int idx) {
        int repeat = repeats[idx];
        System.out.println("idx : " + idx + ", repeat : " + repeat);
        return repeat;
    }

    public int confirm(int idx) {
        return repeats[idx]--;
    }
}

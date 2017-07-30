package com.slotnslot.slotnslot.models;

import java.io.Serializable;
import java.math.BigInteger;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SlotRoom implements Serializable {
    private String address;
    private String title;

    private double hitRatio;
    private int maxWinPrize;
    private double minBet;
    private double maxBet;

    private int playTime;

    private double stake;

    private String playerAddress;
    private String bankerAddress;

    private BigInteger playerBalance = BigInteger.ZERO;
    private BigInteger bankerBalance = BigInteger.ZERO;

    public SlotRoom(String address, String title, double stake, double hitRatio, int maxWinPrize, double minBet, double maxBet) {
        this.address = address;
        this.title = title;
        this.stake = stake;
        this.hitRatio = hitRatio;
        this.maxWinPrize = maxWinPrize;
        this.minBet = minBet;
        this.maxBet = maxBet;
    }

    public int getHitRatio() {
        return (int) (hitRatio * 100);
    }
}

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

    private boolean available = true;
    private boolean bankrupt = false;

    private String playerAddress;
    private String bankerAddress;

    private BigInteger playerBalance = BigInteger.ZERO;
    private BigInteger bankerBalance = BigInteger.ZERO;

    public SlotRoom(String address, String title, double hitRatio, int maxWinPrize, double minBet, double maxBet, String bankerAddress, BigInteger bankerBalance) {
        this.address = address;
        this.title = title;
        this.hitRatio = hitRatio;
        this.maxWinPrize = maxWinPrize;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.bankerAddress = bankerAddress;
        this.bankerBalance = bankerBalance;
    }

    public double getHitRatio() {
        return hitRatio * 100;
    }
}

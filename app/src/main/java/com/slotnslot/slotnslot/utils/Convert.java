package com.slotnslot.slotnslot.utils;

import org.ethereum.geth.BigInt;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Convert {
    private Convert() {
    }

    public static BigInteger toWei(BigDecimal number, Unit unit) {
        BigDecimal weiFactor = unit.getWeiFactor();
        BigDecimal multiply = weiFactor.multiply(number);

        return multiply.toBigInteger();
    }

    public static BigInteger toWei(double number, Unit unit) {
        return toWei(BigDecimal.valueOf(number), unit);
    }

    public static BigInteger toWei(String number, Unit unit) {
        return toWei(new BigDecimal(number), unit);
    }

    public static BigDecimal fromWei(BigInteger wei, Unit unit, int scale) {
        BigDecimal weiFactor = unit.getWeiFactor();
        return new BigDecimal(wei).divide(weiFactor, scale, BigDecimal.ROUND_CEILING);
    }

    public static BigDecimal fromWei(BigInteger wei, Unit unit) {
        return fromWei(wei, unit, 3);
    }

    public static BigInt toBigInt(BigInteger bigInteger) {
        return bigInteger == null ? new BigInt(0) : new BigInt(bigInteger.toByteArray());
    }

    public enum Unit {
        WEI("wei", 0),
        KWEI("kwei", 3),
        MWEI("mwei", 6),
        GWEI("gwei", 9),
        SZABO("szabo", 12),
        FINNEY("finney", 15),
        ETHER("ether", 18),
        KETHER("kether", 21),
        METHER("mether", 24),
        GETHER("gether", 27);

        private String name;
        private BigDecimal weiFactor;

        Unit(String name, int factor) {
            this.name = name;
            this.weiFactor = BigDecimal.TEN.pow(factor);
        }

        public BigDecimal getWeiFactor() {
            return weiFactor;
        }

        @Override
        public String toString() {
            return name;
        }

        public static Unit fromString(String name) {
            if (name != null) {
                for (Unit unit : Unit.values()) {
                    if (name.equalsIgnoreCase(unit.name)) {
                        return unit;
                    }
                }
            }
            return Unit.valueOf(name);
        }
    }
}

package com.slotnslot.slotnslot.contract;

import com.slotnslot.slotnslot.geth.Contract;

import org.ethereum.geth.Receipt;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

public final class SlotMachine extends Contract {
    private static final String BINARY = "0x";

    private static final Event PLAYER_LEFT = new Event(
            "playerLeft",
            Collections.emptyList(),
            Arrays.asList(
                    new TypeReference<Address>() {
                    },
                    new TypeReference<Uint256>() {
                    }
            ));
    private static final Event BANKER_LEFT = new Event(
            "bankerLeft",
            Collections.emptyList(),
            Collections.singletonList(new TypeReference<Address>() {
            }));
    private static final Event GAME_OCCUPIED = new Event(
            "gameOccupied",
            Collections.emptyList(),
            Arrays.asList(
                    new TypeReference<Address>() {
                    },
                    new TypeReference.StaticArrayTypeReference<StaticArray<Bytes32>>(3) {
                    }
            ));
    private static final Event BANKER_SEED_INITIALIZED = new Event(
            "bankerSeedInitialized",
            Collections.emptyList(),
            Collections.singletonList(new TypeReference.StaticArrayTypeReference<StaticArray<Bytes32>>(3) {
            }));
    private static final Event GAME_INITIALIZED = new Event(
            "gameInitialized",
            Collections.emptyList(),
            Arrays.asList(
                    new TypeReference<Address>() {
                    },
                    new TypeReference<Uint256>() {
                    },
                    new TypeReference<Uint8>() {
                    },
                    new TypeReference<Uint8>() {
                    }
            ));
    private static final Event BANKER_SEED_SET = new Event(
            "bankerSeedSet",
            Collections.emptyList(),
            Arrays.asList(
                    new TypeReference<Bytes32>() {
                    },
                    new TypeReference<Uint8>() {
                    }
            ));
    private static final Event PLAYER_SEED_SET = new Event(
            "playerSeedSet",
            Collections.emptyList(),
            Arrays.asList(
                    new TypeReference<Bytes32>() {
                    },
                    new TypeReference<Uint8>() {
                    }
            ));
    private static final Event GAME_CONFIRMED = new Event(
            "gameConfirmed",
            Collections.emptyList(),
            Arrays.asList(
                    new TypeReference<Uint256>() {
                    },
                    new TypeReference<Uint8>() {
                    },
                    new TypeReference<Bytes32>() {
                    }
            ));


    private SlotMachine(String contractAddress) {
        super(BINARY, contractAddress);
    }

    public static SlotMachine load(String contractAddress) {
        return new SlotMachine(contractAddress);
    }

    /**
     * variables
     **/
    public Observable<Bool> mAvailable() {
        Function function = new Function("mAvailable",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Address> owner() {
        Function function = new Function(
                "owner",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Address>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Address> mPlayer() {
        Function function = new Function(
                "mPlayer",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Address>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Bytes16> mName() {
        Function function = new Function(
                "mName",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Bytes16>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint16> mDecider() {
        Function function = new Function(
                "mDecider",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint16>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint256> mMinBet() {
        Function function = new Function("mMinBet",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint256> mMaxBet() {
        Function function = new Function(
                "mMaxBet",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint16> mMaxPrize() {
        Function function = new Function(
                "mMaxPrize",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint16>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint256> bankerBalance() {
        Function function = new Function(
                "bankerBalance",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint256> playerBalance() {
        Function function = new Function(
                "playerBalance",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Bool> initialPlayerSeedReady() {
        Function function = new Function(
                "initialPlayerSeedReady",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Bool> initialBankerSeedReady() {
        Function function = new Function(
                "initialBankerSeedReady",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Bytes32> previousPlayerSeed() {
        Function function = new Function(
                "previousPlayerSeed",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Bytes32>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Bytes32> previousBankerSeed() {
        Function function = new Function(
                "previousBankerSeed",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Bytes32>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint256> mNumGamePlayedByUser(Address param0) {
        Function function = new Function(
                "mNumGamePlayedByUser",
                Collections.singletonList(param0),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Bool> mUsedPlayerSeeds(Bytes32 param0) {
        Function function = new Function(
                "mUsedPlayerSeeds",
                Collections.singletonList(param0),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Bytes32> mCurrentGameId() {
        Function function = new Function(
                "mCurrentGameId",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Bytes32>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<Uint256> mGameInfo(Uint256 param0) {
        Function function = new Function("mGameInfo",
                Collections.singletonList(param0),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
        return executeCallSingleValueReturnObservable(function);
    }

    public Observable<GetInfoResponse> getInfo() {
        Function function = new Function(
                "getInfo",
                Collections.emptyList(),
                Arrays.asList(
                        new TypeReference<Address>() {
                        },
                        new TypeReference<Address>() {
                        },
                        new TypeReference<Bytes16>() {
                        },
                        new TypeReference<Uint16>() {
                        },
                        new TypeReference<Uint256>() {
                        },
                        new TypeReference<Uint256>() {
                        },
                        new TypeReference<Uint16>() {
                        },
                        new TypeReference<Uint256>() {
                        }));
        return executeCallMultipleValueReturnObservable(function)
                .map(response -> {
                    GetInfoResponse getInfo = new GetInfoResponse();
                    getInfo.mPlayer = (Address) response.get(0);
                    getInfo.owner = (Address) response.get(1);
                    getInfo.mName = (Bytes16) response.get(2);
                    getInfo.mDecider = (Uint16) response.get(3);
                    getInfo.mMinBet = (Uint256) response.get(4);
                    getInfo.mMaxBet = (Uint256) response.get(5);
                    getInfo.mMaxPrize = (Uint16) response.get(6);
                    getInfo.bankerBalance = (Uint256) response.get(7);
                    return getInfo;
                });
    }

    public static class GetInfoResponse {
        public Address mPlayer;
        public Address owner;
        public Bytes16 mName;
        public Uint16 mDecider;
        public Uint256 mMinBet;
        public Uint256 mMaxBet;
        public Uint16 mMaxPrize;
        public Uint256 bankerBalance;
    }

    /**
     * methods
     **/
    public Observable<Receipt> occupy(StaticArray<Bytes32> _playerSeed, BigInteger value) {
        Function function = new Function("occupy", Collections.singletonList(_playerSeed), Collections.emptyList());
        return send(getContractAddress(), FunctionEncoder.encode(function), value, null, null);
    }

    public Observable<Receipt> initBankerSeed(StaticArray<Bytes32> _bankerSeed) {
        Function function = new Function("initBankerSeed", Collections.singletonList(_bankerSeed), Collections.emptyList());
        return executeTransaction(function);
    }

    public Observable<Receipt> initGameForPlayer(Uint256 _bet, Uint8 _lines, Uint8 _idx) {
        Function function = new Function("initGameForPlayer", Arrays.asList(_bet, _lines, _idx), Collections.emptyList());
        return executeTransaction(function);
    }

    public Observable<Receipt> setBankerSeed(Bytes32 _bankerSeed, Uint8 _idx) {
        Function function = new Function("setBankerSeed", Arrays.asList(_bankerSeed, _idx), Collections.emptyList());
        return executeTransaction(function);
    }

    public Observable<Receipt> setPlayerSeed(Bytes32 _playerSeed, Uint8 _idx) {
        Function function = new Function("setPlayerSeed", Arrays.asList(_playerSeed, _idx), Collections.emptyList());
        return executeTransaction(function);
    }

    public Observable<Receipt> leave() {
        Function function = new Function("leave", Collections.emptyList(), Collections.emptyList());
        return executeTransaction(function);
    }

    /**
     * events
     **/
    public List<PlayerLeftEventResponse> getPlayerLeftEvents(Receipt transactionReceipt) throws Exception {
        List<EventValues> valueList = extractEventParameters(PLAYER_LEFT, transactionReceipt);
        ArrayList<PlayerLeftEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            PlayerLeftEventResponse typedResponse = new PlayerLeftEventResponse();
            typedResponse.player = (Address) eventValues.getNonIndexedValues().get(0);
            typedResponse.playerBalance = (Uint256) eventValues.getNonIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<PlayerLeftEventResponse> playerLeftEventObservable() {
        return pendingFilterLogs(PLAYER_LEFT)
                .map(eventValues -> {
                    PlayerLeftEventResponse typedResponse = new PlayerLeftEventResponse();
                    typedResponse.player = (Address) eventValues.getNonIndexedValues().get(0);
                    typedResponse.playerBalance = (Uint256) eventValues.getNonIndexedValues().get(1);
                    return typedResponse;
                });
    }

    public List<BankerLeftEventResponse> getBankerLeftEvents(Receipt transactionReceipt) throws Exception {
        List<EventValues> valueList = extractEventParameters(BANKER_LEFT, transactionReceipt);
        ArrayList<BankerLeftEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            BankerLeftEventResponse typedResponse = new BankerLeftEventResponse();
            typedResponse.banker = (Address) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<BankerLeftEventResponse> bankerLeftEventObservable() {
        return filterLogs(BANKER_LEFT)
                .map(eventValues -> {
                    BankerLeftEventResponse typedResponse = new BankerLeftEventResponse();
                    typedResponse.banker = (Address) eventValues.getNonIndexedValues().get(0);
                    return typedResponse;
                });
    }

    public List<GameOccupiedEventResponse> getGameOccupiedEvents(Receipt transactionReceipt) throws Exception {
        List<EventValues> valueList = extractEventParameters(GAME_OCCUPIED, transactionReceipt);
        ArrayList<GameOccupiedEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            GameOccupiedEventResponse typedResponse = new GameOccupiedEventResponse();
            typedResponse.player = (Address) eventValues.getNonIndexedValues().get(0);
            typedResponse.playerSeed = (StaticArray<Bytes32>) eventValues.getNonIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<GameOccupiedEventResponse> gameOccupiedEventObservable() {
        return pendingFilterLogs(GAME_OCCUPIED)
                .map(eventValues -> {
                    GameOccupiedEventResponse typedResponse = new GameOccupiedEventResponse();
                    typedResponse.player = (Address) eventValues.getNonIndexedValues().get(0);
                    typedResponse.playerSeed = (StaticArray<Bytes32>) eventValues.getNonIndexedValues().get(1);
                    return typedResponse;
                });
    }

    public List<BankerSeedInitializedEventResponse> getBankerSeedInitializedEvents(Receipt transactionReceipt) throws Exception {
        List<EventValues> valueList = extractEventParameters(BANKER_SEED_INITIALIZED, transactionReceipt);
        ArrayList<BankerSeedInitializedEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            BankerSeedInitializedEventResponse typedResponse = new BankerSeedInitializedEventResponse();
            typedResponse._bankerSeed = (StaticArray<Bytes32>) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<BankerSeedInitializedEventResponse> bankerSeedInitializedEventObservable() {
        return pendingFilterLogs(BANKER_SEED_INITIALIZED)
                .map(eventValues -> {
                    BankerSeedInitializedEventResponse typedResponse = new BankerSeedInitializedEventResponse();
                    typedResponse._bankerSeed = (StaticArray<Bytes32>) eventValues.getNonIndexedValues().get(0);
                    return typedResponse;
                });
    }

    public List<GameInitializedEventResponse> getGameInitializedEvents(Receipt transactionReceipt) throws Exception {
        List<EventValues> valueList = extractEventParameters(GAME_INITIALIZED, transactionReceipt);
        ArrayList<GameInitializedEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            GameInitializedEventResponse typedResponse = new GameInitializedEventResponse();
            typedResponse.player = (Address) eventValues.getNonIndexedValues().get(0);
            typedResponse.bet = (Uint256) eventValues.getNonIndexedValues().get(1);
            typedResponse.lines = (Uint8) eventValues.getNonIndexedValues().get(2);
            typedResponse.idx = (Uint8) eventValues.getNonIndexedValues().get(3);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<GameInitializedEventResponse> gameInitializedEventObservable() {
        return pendingFilterLogs(GAME_INITIALIZED)
                .map(eventValues -> {
                    GameInitializedEventResponse typedResponse = new GameInitializedEventResponse();
                    typedResponse.player = (Address) eventValues.getNonIndexedValues().get(0);
                    typedResponse.bet = (Uint256) eventValues.getNonIndexedValues().get(1);
                    typedResponse.lines = (Uint8) eventValues.getNonIndexedValues().get(2);
                    typedResponse.idx = (Uint8) eventValues.getNonIndexedValues().get(3);
                    return typedResponse;
                });
    }

    public List<BankerSeedSetEventResponse> getBankerSeedSetEvents(Receipt transactionReceipt) throws Exception {
        List<EventValues> valueList = extractEventParameters(BANKER_SEED_SET, transactionReceipt);
        ArrayList<BankerSeedSetEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            BankerSeedSetEventResponse typedResponse = new BankerSeedSetEventResponse();
            typedResponse.bankerSeed = (Bytes32) eventValues.getNonIndexedValues().get(0);
            typedResponse.idx = (Uint8) eventValues.getNonIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<BankerSeedSetEventResponse> bankerSeedSetEventObservable() {
        return pendingFilterLogs(BANKER_SEED_SET)
                .map(eventValues -> {
                    BankerSeedSetEventResponse typedResponse = new BankerSeedSetEventResponse();
                    typedResponse.bankerSeed = (Bytes32) eventValues.getNonIndexedValues().get(0);
                    typedResponse.idx = (Uint8) eventValues.getNonIndexedValues().get(1);
                    return typedResponse;
                });
    }

    public List<PlayerSeedSetEventResponse> getPlayerSeedSetEvents(Receipt transactionReceipt) throws Exception {
        List<EventValues> valueList = extractEventParameters(PLAYER_SEED_SET, transactionReceipt);
        ArrayList<PlayerSeedSetEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            PlayerSeedSetEventResponse typedResponse = new PlayerSeedSetEventResponse();
            typedResponse.playerSeed = (Bytes32) eventValues.getNonIndexedValues().get(0);
            typedResponse.idx = (Uint8) eventValues.getNonIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<PlayerSeedSetEventResponse> playerSeedSetEventObservable() {
        return pendingFilterLogs(PLAYER_SEED_SET)
                .map(eventValues -> {
                    PlayerSeedSetEventResponse typedResponse = new PlayerSeedSetEventResponse();
                    typedResponse.playerSeed = (Bytes32) eventValues.getNonIndexedValues().get(0);
                    typedResponse.idx = (Uint8) eventValues.getNonIndexedValues().get(1);
                    return typedResponse;
                });
    }

    public List<GameConfirmedEventResponse> getGameConfirmedEvents(Receipt transactionReceipt) throws Exception {
        List<EventValues> valueList = extractEventParameters(GAME_CONFIRMED, transactionReceipt);
        ArrayList<GameConfirmedEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValues eventValues : valueList) {
            GameConfirmedEventResponse typedResponse = new GameConfirmedEventResponse();
            typedResponse.reward = (Uint256) eventValues.getNonIndexedValues().get(0);
            typedResponse.idx = (Uint8) eventValues.getNonIndexedValues().get(1);
            typedResponse.randomSeed = (Bytes32) eventValues.getNonIndexedValues().get(2);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<GameConfirmedEventResponse> gameConfirmedEventObservable() {
        return pendingFilterLogs(GAME_CONFIRMED)
                .map(eventValues -> {
                    GameConfirmedEventResponse typedResponse = new GameConfirmedEventResponse();
                    typedResponse.reward = (Uint256) eventValues.getNonIndexedValues().get(0);
                    typedResponse.idx = (Uint8) eventValues.getNonIndexedValues().get(1);
                    typedResponse.randomSeed = (Bytes32) eventValues.getNonIndexedValues().get(2);
                    return typedResponse;
                });
    }

    public static class PlayerLeftEventResponse {
        public Address player;
        public Uint256 playerBalance;
    }

    public static class BankerLeftEventResponse {
        public Address banker;
    }

    public static class GameOccupiedEventResponse {
        public Address player;
        public StaticArray<Bytes32> playerSeed;
    }

    public static class BankerSeedInitializedEventResponse {
        public StaticArray<Bytes32> _bankerSeed;
    }

    public static class GameInitializedEventResponse {
        public Address player;
        public Uint256 bet;
        public Uint8 lines;
        public Uint8 idx;
    }

    public static class BankerSeedSetEventResponse {
        public Bytes32 bankerSeed;
        public Uint8 idx;
    }

    public static class PlayerSeedSetEventResponse {
        public Bytes32 playerSeed;
        public Uint8 idx;
    }

    public static class GameConfirmedEventResponse {
        public Uint256 reward;
        public Uint8 idx;
        public Bytes32 randomSeed;
    }
}


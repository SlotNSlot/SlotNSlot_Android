package com.slotnslot.slotnslot.activities.example;

import android.os.Bundle;
import android.util.Log;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.contract.SlotMachine;
import com.slotnslot.slotnslot.contract.SlotMachineManager;
import com.slotnslot.slotnslot.contract.SlotMachineStorage;
import com.slotnslot.slotnslot.geth.CredentialManager;
import com.slotnslot.slotnslot.geth.GethConstants;
import com.slotnslot.slotnslot.geth.GethManager;
import com.slotnslot.slotnslot.geth.TransactionManager;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.models.PlayerSeed;
import com.slotnslot.slotnslot.models.Seed;
import com.slotnslot.slotnslot.utils.Convert;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.ethereum.geth.BigInt;
import org.ethereum.geth.Transaction;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.Arrays;
import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class PlayExampleActivity extends RxAppCompatActivity {
    public static final String TAG = PlayExampleActivity.class.getSimpleName();

    private SlotMachine machine;

    private static long playerNonce = 0;
    private static long bankerNonce = 0;

    private PlayerSeed playerSeed = new PlayerSeed();
    private Seed bankerSeed = new Seed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_example);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.load)
    void load() {
        CredentialManager.setDefault(0, "asdf");
        SlotMachineManager
                .load(GethConstants.SLOT_MANAGER_CONTRACT_ADDRESS)
                .getStorageAddr()
                .flatMap(address -> {
                    Log.i(TAG, "slot storage address : " + address.toString());
                    SlotMachineStorage storage = SlotMachineStorage.load(address.toString());

                    storage.totalNumOfSlotMachine()
                            .subscribe(result -> Log.i(TAG, "total number of slot machines : " + result.getValue()));

                    String bankerHex = CredentialManager.getDefaultAccountHex();
                    storage.getNumOfSlotMachine(new Address(bankerHex))
                            .subscribe(result -> Log.i(TAG, "address " + bankerHex + " has " + result.getValue() + " slot machines"));

                    return storage.getSlotMachine(new Address(bankerHex), new Uint256(0));
                })
                .observeOn(Schedulers.io())
                .subscribe(address -> {
                    Log.i(TAG, "slot machine address : " + address.toString());
                    if (Utils.isValidAddress(address.toString())) {
                        machine = SlotMachine.load(address.toString());

                        machine
                                .gameConfirmedEventObservable()
                                .compose(bindToLifecycle())
                                .subscribe(response -> {
                                    Log.i(TAG, "reward : " + response.reward.getValue());
                                    Log.i(TAG, "idx : " + response.idx.getValue());
                                });

                        long banker = GethManager.getClient().getPendingNonceAt(GethManager.getMainContext(), CredentialManager.getAccounts().get(0).getAddress());
                        long player = GethManager.getClient().getPendingNonceAt(GethManager.getMainContext(), CredentialManager.getAccounts().get(1).getAddress());
                        Log.i(TAG, "player nonce : " + player + " , banker nonce : " + banker);
                        bankerNonce = banker + 1;
                        playerNonce = player + 1;
                    }
                }, Throwable::printStackTrace);

    }

    @OnClick(R.id.create)
    void create() {
        CredentialManager.setDefault(0, "asdf");

        SlotMachineManager slotMachineManager = SlotMachineManager.load(GethConstants.SLOT_MANAGER_CONTRACT_ADDRESS);
        slotMachineManager
                .createSlotMachine(
                        new Uint16(150),
                        new Uint256(Convert.toWei(0.001, Convert.Unit.ETHER)),
                        new Uint256(Convert.toWei(0.1, Convert.Unit.ETHER)),
                        new Uint16(1000),
                        new Bytes32("testtesttesttesttesttesttesttest".getBytes()))
                .map(slotMachineManager::getSlotMachineCreatedEvents)
                .subscribe(
                        responses -> {
                            if (responses.isEmpty()) {
                                Log.e(TAG, "event is empty.");
                                return;
                            }
                            Log.i(TAG, "slot created banker : " + responses.get(0)._banker.toString());
                            Log.i(TAG, "slot created decider : " + responses.get(0)._decider.getValue());
                            Log.i(TAG, "slot created min bet : " + responses.get(0)._minBet.getValue());
                            Log.i(TAG, "slot created max bet : " + responses.get(0)._maxBet.getValue());
                            Log.i(TAG, "slot created max prize : " + responses.get(0)._maxPrize.getValue());
                            Log.i(TAG, "slot created total num : " + responses.get(0)._totalNum.getValue());
                            Log.i(TAG, "slot created slot addr : " + responses.get(0)._slotAddr.toString());
                        },
                        Throwable::printStackTrace);
    }

    @OnClick(R.id.send)
    void sendEther() {
        CredentialManager.setDefault(0, "asdf");
        TransactionManager
                .sendFunds(machine.getContractAddress(), Convert.toWei(0.1, Convert.Unit.ETHER))
                .subscribe((o) -> Log.i(TAG, "fund sent."));
    }

    @OnClick(R.id.occupy)
    void occupy() {
        CredentialManager.setDefault(1, "asdf");

        machine
                .occupy(playerSeed.getInitialSeed(), Convert.toWei(0.1, Convert.Unit.ETHER))
                .map(machine::getGameOccupiedEvents)
                .subscribe(
                        gameOccupiedEventResponses -> {
                            if (gameOccupiedEventResponses.isEmpty()) {
                                Log.i(TAG, "response is empty");
                                return;
                            }
                            SlotMachine.GameOccupiedEventResponse response = gameOccupiedEventResponses.get(0);

                            Log.i(TAG, "occupied by : " + response.player.toString());
                            Log.i(TAG, "player seed1 : " + Utils.byteToHex(response.playerSeed.getValue().get(0).getValue()));
                            Log.i(TAG, "player seed2 : " + Utils.byteToHex(response.playerSeed.getValue().get(1).getValue()));
                            Log.i(TAG, "player seed3 : " + Utils.byteToHex(response.playerSeed.getValue().get(2).getValue()));
                        },
                        Throwable::printStackTrace);
    }

    @OnClick(R.id.init_banker_seed)
    void initBankerSeed() {
        CredentialManager.setDefault(0, "asdf");

        machine
                .initBankerSeed(bankerSeed.getInitialSeed())
                .map(receipt -> machine.getBankerSeedInitializedEvents(receipt))
                .subscribe(bankerSeedInitializedEventResponses -> {
                    if (bankerSeedInitializedEventResponses.isEmpty()) {
                        Log.i(TAG, "response is empty");
                        return;
                    }

                    SlotMachine.BankerSeedInitializedEventResponse response = bankerSeedInitializedEventResponses.get(0);

                    Log.i(TAG, "banker seed1 : " + Utils.byteToHex(response._bankerSeed.getValue().get(0).getValue()));
                    Log.i(TAG, "banker seed2 : " + Utils.byteToHex(response._bankerSeed.getValue().get(1).getValue()));
                    Log.i(TAG, "banker seed3 : " + Utils.byteToHex(response._bankerSeed.getValue().get(2).getValue()));
                });
    }

    @OnClick(R.id.init_game)
    void initGame() {
//        machine
//                .initGameForPlayer(new Uint256(Convert.toWei(0.001, Convert.Unit.ETHER)), new Uint256(20))
//                .map(receipt -> machine.getGameInitializedEvents(receipt))
//                .subscribe(gameInitializedEventResponses -> {
//                    if (gameInitializedEventResponses.isEmpty()) {
//                        Log.i(TAG, "response is empty");
//                        return;
//                    }
//                    SlotMachine.GameInitializedEventResponse response = gameInitializedEventResponses.get(0);
//
//                    Log.i(TAG, "player address : " + response.player.toString());
//                    Log.i(TAG, "bet : " + response.bet.getValue());
//                    Log.i(TAG, "lines : " + response.lines.getValue());
//                });

        Completable
                .create(e -> {
                    CredentialManager.setDefault(1, "asdf");

                    Function function = new Function(
                            "initGameForPlayer",
                            Arrays.asList(
                                    new Uint256(Convert.toWei(0.001, Convert.Unit.ETHER)),
                                    new Uint256(20),
                                    new Uint256(playerSeed.getIndex())),
                            Collections.emptyList());
                    Transaction tx = new Transaction(
                            playerNonce++, // nonce
                            new org.ethereum.geth.Address(machine.getContractAddress()), // receiver address
                            new BigInt(0),
                            Convert.toBigInt(GethConstants.DEFAULT_GAS_LIMIT), // gas limit
                            Convert.toBigInt(GethConstants.DEFAULT_GAS_PRICE), // gas price
                            Utils.hexToByte(FunctionEncoder.encode(function))
                    );
                    Transaction signed = CredentialManager.getDefault().sign(tx);
                    GethManager.getClient().sendTransaction(GethManager.getMainContext(), signed);

                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, Throwable::printStackTrace);
    }

    @OnClick(R.id.set_banker_seed)
    void setBankerSeed() {
//        machine
//                .setBankerSeed(new Bytes32(Utils.generateRandom(bankerSeed, banker--)))
//                .map(receipt -> machine.getBankerSeedSetEvents(receipt))
//                .subscribe(bankerSeedSetEventResponses -> {
//                    if (bankerSeedSetEventResponses.isEmpty()) {
//                        Log.i(TAG, "response is empty");
//                        return;
//                    }
//
//                    SlotMachine.BankerSeedSetEventResponse response = bankerSeedSetEventResponses.get(0);
//
//                    Log.i(TAG, "banker seed : " + Utils.byteToHex(response.bankerSeed.getValue()));
//                });

        Completable
                .create(e -> {
                    CredentialManager.setDefault(0, "asdf");

                    Function function = new Function(
                            "setBankerSeed",
                            Arrays.asList(
                                    bankerSeed.getSeed(playerSeed.getIndex()),
                                    new Uint256(playerSeed.getIndex())),
                            Collections.emptyList());
                    Transaction tx = new Transaction(
                            bankerNonce++, // nonce
                            new org.ethereum.geth.Address(machine.getContractAddress()), // receiver address
                            new BigInt(0),
                            Convert.toBigInt(GethConstants.DEFAULT_GAS_LIMIT), // gas limit
                            Convert.toBigInt(GethConstants.DEFAULT_GAS_PRICE), // gas price
                            Utils.hexToByte(FunctionEncoder.encode(function))
                    );
                    Transaction signed = CredentialManager.getDefault().sign(tx);
                    GethManager.getClient().sendTransaction(GethManager.getMainContext(), signed);

                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, Throwable::printStackTrace);
    }

    @OnClick(R.id.set_player_seed)
    void setPlayerSeed() {
//        machine
//                .setPlayerSeed(new Bytes32(Utils.generateRandom(playerSeed, player--)))
//                .map(receipt -> machine.getGameConfirmedEvents(receipt))
//                .subscribe(gameConfirmedEventResponses -> {
//                    if (gameConfirmedEventResponses.isEmpty()) {
//                        Log.i(TAG, "response is empty");
//                        return;
//                    }
//
//                    SlotMachine.GameConfirmedEventResponse response = gameConfirmedEventResponses.get(0);
//
//                    Log.i(TAG, "reward : " + response.reward.getValue());
//                });

        Completable
                .create(e -> {
                    CredentialManager.setDefault(1, "asdf");

                    Function function = new Function(
                            "setPlayerSeed",
                            Arrays.asList(
                                    playerSeed.getSeed(),
                                    new Uint256(playerSeed.getIndex())),
                            Collections.emptyList());
                    Transaction tx = new Transaction(
                            playerNonce++, // nonce
                            new org.ethereum.geth.Address(machine.getContractAddress()), // receiver address
                            new BigInt(0),
                            Convert.toBigInt(GethConstants.DEFAULT_GAS_LIMIT), // gas limit
                            Convert.toBigInt(GethConstants.DEFAULT_GAS_PRICE), // gas price
                            Utils.hexToByte(FunctionEncoder.encode(function))
                    );
                    Transaction signed = CredentialManager.getDefault().sign(tx);
                    GethManager.getClient().sendTransaction(GethManager.getMainContext(), signed);

                    bankerSeed.confirm(playerSeed.getIndex());
                    playerSeed.confirm(playerSeed.getIndex());
                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, Throwable::printStackTrace);
    }

    @OnClick(R.id.leave)
    void leave() {
        CredentialManager.setDefault(1, "asdf");

        machine
                .leave()
                .flatMap(receipt -> machine.mPlayer())
                .subscribe(
                        address -> Log.i(TAG, "player leave : address : " + address.toString()),
                        Throwable::printStackTrace);
    }

    @OnClick(R.id.remove)
    void remove() {
        CredentialManager.setDefault(0, "asdf");

        SlotMachineManager slotMachineManager = SlotMachineManager.load(GethConstants.SLOT_MANAGER_CONTRACT_ADDRESS);
        slotMachineManager.removeSlotMachine(new Address(machine.getContractAddress()))
                .map(slotMachineManager::getSlotMachineRemovedEvents)
                .subscribe(slotMachineRemovedEventResponses -> {
                    if (slotMachineRemovedEventResponses.isEmpty()) {
                        return;
                    }

                    SlotMachineManager.SlotMachineRemovedEventResponse response = slotMachineRemovedEventResponses.get(0);
                    Log.i(TAG, "banker address : " + response._banker.toString());
                    Log.i(TAG, "removed slot address : " + response._slotAddr.toString());
                    Log.i(TAG, "total num of slots after removing : " + response._totalNum.getValue());
                }, Throwable::printStackTrace);
    }

    @OnClick(R.id.get_info)
    void getInfo() {
        if (machine == null) {
            Log.i(TAG, "slot machine is null");
            return;
        }

        Completable
                .create(e -> {
                    machine.mPlayer().subscribe(address -> Log.i(TAG, "player address : " + address.toString()));
                    machine.playerBalance().subscribe(playerBalance -> Log.i(TAG, "player balance : " + playerBalance.getValue()));
                    machine
                            .getInfo()
                            .subscribe(response -> {
                                Log.i(TAG, "mDecider : " + response.mDecider.getValue());
                                Log.i(TAG, "mMinBet : " + response.mMinBet.getValue());
                                Log.i(TAG, "mMaxBet : " + response.mMaxBet.getValue());
                                Log.i(TAG, "mMaxPrize : " + response.mMaxPrize.getValue());
                                Log.i(TAG, "banker balance : " + response.bankerBalance.getValue());
                            });
                })
                .subscribe(() -> {
                }, Throwable::printStackTrace);
    }
}

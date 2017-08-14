package com.slotnslot.slotnslot.geth;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.widget.Toast;

import com.slotnslot.slotnslot.MainApplication;
import com.slotnslot.slotnslot.models.DrawingLine;
import com.slotnslot.slotnslot.models.PayLineTuple;
import com.slotnslot.slotnslot.models.SlotResultDrawingLine;
import com.slotnslot.slotnslot.utils.Constants;

import org.ethereum.geth.Hashes;
import org.ethereum.geth.Log;
import org.ethereum.geth.Logs;
import org.ethereum.geth.Receipt;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Utils {
    private static String TAG = Utils.class.getSimpleName();

    private static final int DEFAULT_SLEEP_DURATION = 5000; // ms
    private static final int DEFAULT_ATTEMPTS = 40;

    public static void showToast(String msg) {
        Toast.makeText(MainApplication.getContext(), msg, Toast.LENGTH_LONG).show();
    }

    public static void showDialog(Activity activity, String title, String message, String okMessage) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(okMessage, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static String getBaseDir() {
        return MainApplication.getContext().getFilesDir().getPath();
    }

    public static String getDataDir() {
        String dataDir;

        if (GethConstants.NETWORK == EthereumNetwork.MAIN) {
            dataDir = "/mainnet";
        } else if (GethConstants.NETWORK == EthereumNetwork.TESTNET) {
            dataDir = "/testnet";
        } else if (GethConstants.NETWORK == EthereumNetwork.RINKEBY) {
            dataDir = "/rinkeby";
        } else {
            dataDir = "/temp";
        }

        return getBaseDir() + dataDir;
    }

    public static byte[] hexToByte(String hex) {
        if (Utils.isEmpty(hex)) {
            return null;
        }
        return Numeric.hexStringToByteArray(hex);
    }

    public static String byteToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return Numeric.toHexString(bytes);
    }

    public static Bytes16 stringToBytes16(String str) {
        return new Bytes16(stringToByte(str, 16));
    }

    public static byte[] stringToByte(String str, int byteLength) {
        byte[] strByteArray = str.getBytes();
        byte strByteLength = (byte) strByteArray.length;
        if (strByteArray.length > byteLength) {
            strByteLength = (byte) byteLength;
        }

        byte[] newByteArray = new byte[byteLength];
        System.arraycopy(strByteArray, 0, newByteArray, 0, strByteLength);

        return newByteArray;
    }

    public static String byteToString(byte[] origin) {
        if (origin.length == 0) {
            return "";
        }
        int byteLength = findByteArrayLength(origin);
        if (byteLength == 0) {
            return "";
        }

        byte[] strByteArray = new byte[byteLength];
        System.arraycopy(origin, 0, strByteArray, 0, byteLength);

        return new String(strByteArray);
    }

    public static int findByteArrayLength(byte[] bytes) {
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] == 0) {
                return i;
            }
        }
        return 0;
    }

    public static boolean isValidAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return false;
        }
        if (!address.startsWith("0x")) {
            return false;
        }
        if (address.length() != 42) {
            return false;
        }
        if (address.substring(2).replace("0", "").isEmpty()) {
            return false;
        }
        return true;
    }

    public static <T> Observable<T> waitResponse(Function<Integer, T> function, int attempts, int sleepDuration) {
        return Observable
                .<T>create(emitter -> {
                    for (int attempt = 0; attempt < attempts; attempt++) {
                        try {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(function.apply(attempt));
                                emitter.onComplete();
                                return;
                            }
                        } catch (Exception e) {
                            if (emitter.isDisposed()) {
                                return;
                            }
                            if (e.getMessage() == null) {
                                emitter.onError(e);
                            }
                            if (e.getMessage().contains("no suitable peers") || e.getMessage().contains("not found")) {
                                android.util.Log.d(TAG, "error : " + e.getMessage());
                                try {
                                    Thread.sleep(sleepDuration);
                                } catch (InterruptedException ignored) {
                                    if (!emitter.isDisposed()) {
                                        emitter.onError(ignored);
                                        return;
                                    }
                                }
                                continue;
                            }
                            if (e.getMessage().contains("insufficient funds ")) {
                                emitter.onError(new InsufficientFundException(e.getMessage()));
                                return;
                            }
                            // remain exception
                            emitter.onError(e);
                            return;
                        }
                    }
                    if (!emitter.isDisposed()) {
                        emitter.onError(new RetryTimeoutException(attempts + " attempts with " + sleepDuration + "ms sleep duration all failed"));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Observable<T> waitResponse(Function<Integer, T> function) {
        return waitResponse(function, DEFAULT_ATTEMPTS, DEFAULT_SLEEP_DURATION);
    }

    public static <T> Observable<T> waitResponse(Callable<T> callable) {
        return waitResponse(attempt -> callable.call(), DEFAULT_ATTEMPTS, DEFAULT_SLEEP_DURATION);
    }

    public static byte[] generateRandom(double seed, int recursive) {
        return generateRandom(String.valueOf(seed), recursive);
    }

    public static byte[] generateRandom(String seed, int recursive) {
        if (isEmpty(seed)) {
            throw new GethException("seed is empty");
        }

        byte[] sha = hexToByte(seed);
        for (int i = 0; i < recursive; i++) {
            sha = Hash.sha3(sha);
        }
        return sha;
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static List<EventValues> extractEventParameters(Event event, Receipt transactionReceipt) throws Exception {
        Logs logs = transactionReceipt.getLogs();
        List<EventValues> values = new ArrayList<>();

        for (int i = 0; i < logs.size(); i++) {
            EventValues eventValues = extractEventParameters(event, logs.get(i));
            if (eventValues != null) {
                values.add(eventValues);
            }
        }
        return values;
    }

    public static EventValues extractEventParameters(Event event, Log log) throws Exception {
        Hashes topics = log.getTopics();
        if (topics == null || topics.size() == 0) {
//            throw new GethException("topic is empty");
            return null;
        }

        String eventSig = EventEncoder.encode(event);
        String topic = topics.get(0).getHex();
        if (!topic.equals(eventSig)) {
//            throw new GethException("topic is not match. topic signature : " + eventSig + ", log topic : " + topic);
            return null;
        }
        android.util.Log.d(TAG, "======= LOG DATA : " + byteToHex(log.getData()));

        List<Type> indexedValues = new ArrayList<>();
        List<Type> nonIndexedValues = FunctionReturnDecoder.decode(byteToHex(log.getData()), event.getNonIndexedParameters());

        List<TypeReference<Type>> indexedParameters = event.getIndexedParameters();
        for (int i = 0; i < indexedParameters.size(); i++) {
            Type value = FunctionReturnDecoder.decodeIndexedValue(topics.get(i + 1).getHex(), indexedParameters.get(i));
            indexedValues.add(value);
        }
        return new EventValues(indexedValues, nonIndexedValues);
    }

    public static boolean isTopicMatch(Event event, Log log) throws Exception {
        Hashes topics = log.getTopics();
        if (topics == null || topics.size() == 0) {
            android.util.Log.e(TAG, "topic is empty");
            return false;
        }

        String eventSig = EventEncoder.encode(event);
        String topic = topics.get(0).getHex();
        if (!topic.equals(eventSig)) {
            android.util.Log.e(TAG, "topic is not match. topic signature : " + eventSig + ", log topic : " + topic);
            return false;
        }

        return true;
    }

    public static Hashes listToHashes(List<String> list) {
        Hashes hashes = new Hashes();
        for (String hash : list) {
            hashes.append(new org.ethereum.geth.Hash(hash));
        }
        return hashes;
    }

    public static SlotResultDrawingLine getDrawLine(int lineNum, int slotResult) {
        ArrayList<DrawingLine> drawingLines = new ArrayList<>();
        ArrayList<PayLineTuple> payLineTuples = getPayLineTuples(slotResult);
        Integer[][] slotLineInfo = new Integer[5][3];
        for (int i = 0; i < slotLineInfo.length; i++) {
            for (int j = 0; j < slotLineInfo[i].length; j++) {
                slotLineInfo[i][j] = Constants.UNDEFINE;
            }
        }
        Random random = new Random();
        if (lineNum < payLineTuples.size()) {
            return new SlotResultDrawingLine(SlotResultDrawingLine.Drawable.BIGWIN, null, null);
        }
        if (payLineTuples.size() == 0) {
            return new SlotResultDrawingLine(SlotResultDrawingLine.Drawable.BIGWIN, null, null);
        }
        if (payLineTuples.size() == 1) {
            payLineTuples.get(0).setLineNumber(random.nextInt(lineNum));
        }
        ArrayList<Integer[]> usableLines = new ArrayList<>();
        Integer[][] winLines = Arrays.copyOfRange(Constants.WIN_LINE, 0, lineNum);
        for (Integer[] winLine : winLines) {
            usableLines.add(winLine);
        }

        Collections.shuffle(usableLines);

        HashMap<Integer, ArrayList<PayLineTuple>> symbolObj = new HashMap<>();
        for (PayLineTuple payLineTuple : payLineTuples) {
            if (symbolObj.containsKey(payLineTuple.SYMBOL_INDEX)) {
                symbolObj.get(payLineTuple.SYMBOL_INDEX).add(payLineTuple);
            } else {
                ArrayList<PayLineTuple> value = new ArrayList<>();
                value.add(payLineTuple);
                symbolObj.put(payLineTuple.SYMBOL_INDEX, value);
            }
        }
        ArrayList<PayLineTuple> singleSymbolList = new ArrayList<>();
        HashMap<Integer, ArrayList<PayLineTuple>> sameSymbolObj = new HashMap<>();
        for (Integer symbolIndex : symbolObj.keySet()) {
            ArrayList<PayLineTuple> symbolObjValue = symbolObj.get(symbolIndex);
            if (symbolObjValue.size() >= 2) {
                sameSymbolObj.put(symbolIndex, symbolObjValue);
            } else {
                singleSymbolList.add(symbolObjValue.get(0));
            }
        }

        if (sameSymbolObj.size() == 0) {
            if (singleSymbolList.size() > 3) {
                return new SlotResultDrawingLine(SlotResultDrawingLine.Drawable.BIGWIN, null, null);
            }
            for (int i = 0; i < payLineTuples.get(0).LENGTH; i++) {
                int slotY = usableLines.get(0)[i];
                slotLineInfo[i][slotY] = payLineTuples.get(0).SYMBOL_INDEX;
            }
            drawingLines.add(new DrawingLine(usableLines.get(0)[5], payLineTuples.get(0).SYMBOL_INDEX, payLineTuples.get(0).LENGTH));
            boolean drawable = true;
            for (int i = 1; i < payLineTuples.size(); i++) {
                for (int j = 1; j < lineNum; j++) {
                    boolean overLapping = false;
                    for (int k = 0; k < payLineTuples.get(i).LENGTH; k++) {
                        int slotY = usableLines.get(j)[k];
                        if (slotLineInfo[k][slotY] != Constants.UNDEFINE) {
                            overLapping = true;
                            break;
                        }
                    }
                    if (!overLapping) {
                        for (int k = 0; k < payLineTuples.get(i).LENGTH; k++) {
                            int slotY = usableLines.get(j)[k];
                            slotLineInfo[k][slotY] = payLineTuples.get(i).SYMBOL_INDEX;
                        }
                        drawingLines.add(new DrawingLine(usableLines.get(j)[5], payLineTuples.get(i).SYMBOL_INDEX, payLineTuples.get(i).LENGTH));
                        break;
                    }
                }
            }
            if (drawingLines.size() < payLineTuples.size()) {
                drawable = false;
            }
            if (drawable) {
                return new SlotResultDrawingLine(SlotResultDrawingLine.Drawable.DRAWABLE, slotLineInfo, drawingLines);
            } else {
                return new SlotResultDrawingLine(SlotResultDrawingLine.Drawable.BIGWIN, null, null);
            }
        } else {
            Integer[][] distanceCompList = new Integer[5][3];
            ArrayList<Integer> impossibleList = new ArrayList<>();
            for (int i = 0; i < sameSymbolObj.size(); i++) {
                Integer symbol = sameSymbolObj.keySet().toArray(new Integer[sameSymbolObj.size()])[i];
                int sameSymbolLinesNum = sameSymbolObj.get(symbol).size();
                int minLength = Constants.UNDEFINE;
                for (int j = 0; j < sameSymbolObj.get(symbol).size(); j++) {
                    int length = sameSymbolObj.get(symbol).get(j).LENGTH;
                    if (minLength == Constants.UNDEFINE) minLength = length;
                    else if (minLength > length) minLength = length;
                }

                List<Integer> lineIndexList = new ArrayList<>();
                for (int p = 0; p < usableLines.size(); p++) {
                    lineIndexList.add(p);
                }
                List<List<Integer>> combList = step(lineIndexList, sameSymbolLinesNum, new ArrayList<>());

                int minDist = 15;
                int minCombIndex = Constants.UNDEFINE;

                for (int j = 0; j < combList.size(); j++) {
                    if (impossibleList.contains(Integer.valueOf(j))) {
                        continue;
                    }
                    boolean overLapping = false;
                    for (int a = 0; a < distanceCompList.length; a++) {
                        for (int b = 0; b < distanceCompList[a].length; b++) {
                            distanceCompList[a][b] = Constants.UNDEFINE;
                        }
                    }
                    int distance = 0;
                    for (int k = 0; k < combList.get(j).size(); k++) {
                        int lineIndex = combList.get(j).get(k);
                        for (int l = 0; l < 5; l++) {
                            int slotY = usableLines.get(lineIndex)[l];
                            if (slotLineInfo[l][slotY] != Constants.UNDEFINE) {
                                overLapping = true;
                                break;
                            } else if (distanceCompList[l][slotY] == 1) {
                                distanceCompList[l][slotY] = 1;
                                if (l >= minLength) distance += 100;
                            } else {
                                distanceCompList[l][slotY] = 1;
                                distance += 1;
                            }
                        }
                        if (overLapping) {
                            break;
                        }
                    }
                    if (!overLapping && distance < minDist) {
                        minDist = distance;
                        minCombIndex = j;
                    }
                }
                if (minCombIndex == Constants.UNDEFINE) {
                    if (i == 0) {
                        return new SlotResultDrawingLine(SlotResultDrawingLine.Drawable.BIGWIN, null, null);
                    } else {
                        i = -1;
                        initSlot(slotLineInfo);
                        drawingLines.clear();
                        continue;
                    }
                } else {
                    impossibleList.add(minCombIndex);
                }

                for (int j = 0; j < combList.get(minCombIndex).size(); j++) {
                    int lineIndex = combList.get(minCombIndex).get(j);
                    int length = sameSymbolObj.get(symbol).get(j).LENGTH;
                    for (int k = 0; k < length; k++) {
                        int slotY = usableLines.get(lineIndex)[k];
                        slotLineInfo[k][slotY] = symbol;
                    }
                    drawingLines.add(new DrawingLine(usableLines.get(lineIndex)[5], symbol, sameSymbolObj.get(symbol).get(j).LENGTH));
                }
            }

            for (int i = 0; i < drawingLines.size(); i++) {
                int drawingLineNum = drawingLines.get(i).lineNum;
                for (int j = 0; j < usableLines.size(); j++) {
                    if (usableLines.get(j)[5] == drawingLineNum) {
                        usableLines.remove(j);
                        break;
                    }
                }
            }

            for (int i = 0; i < singleSymbolList.size(); i++) {
                boolean drawable = false;
                for (int j = 0; j < usableLines.size(); j++) {
                    if (payLineTuples.size() == drawingLines.size()) {
                        break;
                    }
                    boolean overLapping = false;
                    for (int k = 0; k < singleSymbolList.get(i).LENGTH; k++) {
                        int slotY = usableLines.get(j)[k];
                        if (slotLineInfo[k][slotY] != Constants.UNDEFINE) {
                            overLapping = true;
                            break;
                        }
                    }
                    if (!overLapping) {
                        for (int k = 0; k < singleSymbolList.get(i).LENGTH; k++) {
                            int slotY = usableLines.get(j)[k];
                            slotLineInfo[k][slotY] = singleSymbolList.get(i).SYMBOL_INDEX;
                        }
                        drawingLines.add(new DrawingLine(usableLines.get(j)[5], singleSymbolList.get(i).SYMBOL_INDEX, singleSymbolList.get(i).LENGTH));
                        drawable = true;
                    }
                }
                if (!drawable) {
                    return new SlotResultDrawingLine(SlotResultDrawingLine.Drawable.BIGWIN, null, null);
                }
            }
            return new SlotResultDrawingLine(SlotResultDrawingLine.Drawable.DRAWABLE, slotLineInfo, drawingLines);
        }
    }

    public static List<List<Integer>> step(List<Integer> input,
                                           int k,
                                           List<List<Integer>> result) {
        if (k == 0) {
            return result;
        }

        if (result.size() == 0) {
            for (Integer i : input) {
                ArrayList<Integer> subList = new ArrayList<>();
                subList.add(i);
                result.add(subList);
            }

            return step(input, k - 1, result);
        }

        List<List<Integer>> newResult = new ArrayList<>();
        for (List<Integer> subList : result) {
            for (Integer i : input) {
                List<Integer> newSubList = new ArrayList<>();
                newSubList.addAll(subList);
                newSubList.add(i);
                newResult.add(newSubList);
            }
        }

        return step(input, k - 1, newResult);
    }

    private static ArrayList<PayLineTuple> getPayLineTuples(int slotResult) {
        ArrayList<Integer> lineCaseList = getLineCase(slotResult);
        ArrayList<PayLineTuple> payLineTuples = new ArrayList<>();
        int[][] duplicateList = new int[Constants.LINE_CASE_2000.length][2];
        for (int i = 0; i < lineCaseList.size(); i++) {
            for (int j = 0; j < Constants.LINE_CASE_2000[lineCaseList.get(i)].length; j++) {
                duplicateList[Constants.LINE_CASE_2000[lineCaseList.get(i)][j][0]][0] += 1;
                duplicateList[Constants.LINE_CASE_2000[lineCaseList.get(i)][j][0]][1] += Constants.LINE_CASE_2000[lineCaseList.get(i)][j][1];
            }
        }
        for (int i = 0; i < lineCaseList.size(); i++) {
            int minDuplicate = 100;
            int minLength = 0;
            int resultSymbol = Constants.UNDEFINE;
            for (int j = 0; j < Constants.LINE_CASE_2000[lineCaseList.get(i)].length; j++) {
                int[] lineCase = Constants.LINE_CASE_2000[lineCaseList.get(i)][j];
                int symbol = lineCase[0];
                if (minDuplicate > duplicateList[symbol][0]) {
                    minDuplicate = duplicateList[symbol][0];
                    minLength = duplicateList[symbol][1];
                    resultSymbol = symbol;
                } else if (minDuplicate == duplicateList[symbol][0]) {
                    if (minLength > duplicateList[symbol][1]) {
                        minLength = duplicateList[symbol][1];
                        resultSymbol = symbol;
                    }
                }
            }
            for (int j = 0; j < Constants.LINE_CASE_2000[lineCaseList.get(i)].length; j++) {
                if (resultSymbol == Constants.LINE_CASE_2000[lineCaseList.get(i)][j][0]) {
                    payLineTuples.add(new PayLineTuple(resultSymbol, Constants.LINE_CASE_2000[lineCaseList.get(i)][j][1]));
                }
            }
        }

        return payLineTuples;
    }

    private static ArrayList<Integer> getLineCase(int slotResult) {
        ArrayList<Integer> lineCaseList = new ArrayList<>();
        for (int i = 0; i < Constants.LINE_CASE_2000_VALUE.length; i++) {
            int lineCaseValue = Constants.LINE_CASE_2000_VALUE[i];
            if (slotResult - lineCaseValue >= 0) {
                slotResult -= lineCaseValue;
                lineCaseList.add(i);
                i--;
            }
            if (slotResult == 0) {
                return lineCaseList;
            }
        }
        return null;
    }

    private static void initSlot(Integer[][] slot) {
        for (int i = 0; i < slot.length; i++) {
            for (int j = 0; j < slot[i].length; j++) {
                slot[i][j] = Constants.UNDEFINE;
            }
        }
    }
}

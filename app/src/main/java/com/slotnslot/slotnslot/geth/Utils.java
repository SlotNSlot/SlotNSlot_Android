package com.slotnslot.slotnslot.geth;

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
    private static final int DEFAULT_SLEEP_DURATION = 5000; // ms
    private static final int DEFAULT_ATTEMPTS = 40;

    public static void showToast(String msg){
        Toast.makeText(MainApplication.getContext(), msg, Toast.LENGTH_LONG).show();
    }

    public static byte[] hexToByte(String hex) {
        if (TextUtils.isEmpty(hex)) {
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
                            if (e.getMessage() != null && (e.getMessage().contains("no suitable peers") || e.getMessage().contains("not found"))) {
                                System.out.println("error : " + e.getMessage());
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

                            if (!emitter.isDisposed()) {
                                emitter.onError(e);
                                return;
                            }
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
        String shaValue = "";

        for (int i = 0; i < recursive; i++) {
            if (!TextUtils.isEmpty(shaValue)) {
                shaValue = Hash.sha3(shaValue);
            } else {
                shaValue = Hash.sha3(seed);
            }
        }
        return hexToByte(shaValue);
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
        System.out.println("======= LOG DATA : " + byteToHex(log.getData()));

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
            System.out.println("topic is empty");
            return false;
        }

        String eventSig = EventEncoder.encode(event);
        String topic = topics.get(0).getHex();
        if (!topic.equals(eventSig)) {
            System.out.println("topic is not match. topic signature : " + eventSig + ", log topic : " + topic);
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
                if (drawingLines.size() < 1) {
                    drawable = false;
                    break;
                }
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

                ArrayList<Integer[]> lineIndexList = new ArrayList<>();
                for (int p = 0; p < usableLines.size(); p++) {
                    Integer[] lineIndex = {p};
                    lineIndexList.add(lineIndex);
                }
                ArrayList<Integer[]> combList = kComb(lineIndexList, sameSymbolLinesNum);

                int minDist = 15;
                int minCombIndex = Constants.UNDEFINE;

                for (int j = 0; j < combList.size(); j++) {
                    if (impossibleList.contains(Integer.valueOf(j))) { continue; }
                    boolean overLapping = false;
                    for (int a = 0; a < distanceCompList.length; a++) {
                        for (int b = 0; b < distanceCompList[a].length; b++) {
                            distanceCompList[a][b] = Constants.UNDEFINE;
                        }
                    }
                    int distance = 0;
                    for (int k = 0; k < combList.get(j).length; k++) {
                        int lineIndex = combList.get(j)[k];
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

                for (int j = 0; j < combList.get(minCombIndex).length; j++) {
                    int lineIndex = combList.get(minCombIndex)[j];
                    int length = sameSymbolObj.get(symbol).get(j).LENGTH;
                    for (int k = 0; k < length; k++) {
                        int slotY = usableLines.get(lineIndex)[k];
                        slotLineInfo[k][slotY] = symbol;
                    }
                    drawingLines.add(new DrawingLine(usableLines.get(lineIndex)[5], symbol, sameSymbolObj.get(symbol).get(j).LENGTH));
                }
            }

            for(int i=0; i<drawingLines.size(); i++) {
                int drawingLineNum = drawingLines.get(i).lineNum;
                for (int j=0; j<usableLines.size(); j++) {
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

    private static ArrayList<Integer[]> kComb(ArrayList<Integer[]> set, int k) {
        if (k > set.size() || k <= 0) {
            return null;
        }
        if (k == set.size()) {
            return set;
        }
        if (k == 1) {
            ArrayList<Integer[]> comb = new ArrayList<>();
            for (int i = 0; i < set.size(); i++) {
                comb.add(set.get(i));
            }
            return comb;
        }
        ArrayList<Integer[]> comb = new ArrayList<>();
        for (int i = 0; i < set.size() - k + 1; i++) {
            ArrayList<Integer[]> head = new ArrayList<>(set.subList(i, i + 1));
            ArrayList<Integer[]> tailcombs = kComb(new ArrayList<>(set.subList(i + 1, set.size())), k - 1);
            for (int j = 0; j < tailcombs.size(); j++) {
                Integer[] concat = new Integer[head.get(0).length + tailcombs.get(j).length];
                System.arraycopy(head.get(0), 0, concat, 0, head.get(0).length);
                System.arraycopy(tailcombs.get(j), 0, concat, head.get(0).length, tailcombs.get(j).length);
                comb.add(concat);
            }
        }
        return comb;
    }

    private static ArrayList<PayLineTuple> getPayLineTuples(int slotResult) {
        ArrayList<Integer> lineCaseList = getLineCase(slotResult);
        ArrayList<PayLineTuple> payLineTuples = new ArrayList<>();
        int[][] duplicateList = new int[Constants.LINE_CASE_2000.length][2];
        for (int i = 0; i < lineCaseList.size(); i++) {
            for (int j = 0; j < Constants.LINE_CASE_2000[lineCaseList.get(i)].length; j++) {
                duplicateList[Constants.LINE_CASE_2000[lineCaseList.get(i)][j][0]][0] += 1; // symbol의 갯수
                duplicateList[Constants.LINE_CASE_2000[lineCaseList.get(i)][j][0]][1] += Constants.LINE_CASE_2000[lineCaseList.get(i)][j][1]; // symbol의 모든 length의 합
            }
        }
        for (int i = 0; i < lineCaseList.size(); i++) {
            int maxDuplicate = 0;
            int minLength = 0;
            int resultSymbol = Constants.UNDEFINE;
            for (int j = 0; j < Constants.LINE_CASE_2000[lineCaseList.get(i)].length; j++) {
                int[] lineCase = Constants.LINE_CASE_2000[lineCaseList.get(i)][j];
                int symbol = lineCase[0];
                if (maxDuplicate < duplicateList[symbol][0]) {
                    maxDuplicate = duplicateList[symbol][0];
                    minLength = duplicateList[symbol][1];
                    resultSymbol = symbol;
                } else if (maxDuplicate == duplicateList[symbol][0]) {
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
        for (int i=0; i<slot.length; i++) {
            for (int j=0; j<slot[i].length; j++) {
                slot[i][j] = Constants.UNDEFINE;
            }
        }
    }
}

package com.slotnslot.slotnslot.Wheel.adapters;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.utils.SlotUtil;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Random;

public class SlotAdapter extends AbstractWheelAdapter {
    private Activity mActivity;
    private ArrayList<SoftReference<Bitmap>> mImages;
    private int width;
    private int height;
    private int IMAGE_WIDTH;
    private int IMAGE_HEIGHT;
    private final LayoutParams params;
    private Integer[] itemIndexs = {0, 1, 2};

    @SuppressWarnings("deprecation")
    public SlotAdapter(Activity activity) {
        mActivity = activity;
        width = SlotUtil.convertDpToPixel(327.1f, activity);
        height = SlotUtil.convertDpToPixel(194.8f, activity);
        IMAGE_WIDTH = width / 5;
        IMAGE_HEIGHT = height / 3;
        params = new LayoutParams(IMAGE_WIDTH, IMAGE_HEIGHT);
        mImages = new ArrayList<SoftReference<Bitmap>>(Constants.items.length);
        for (int id : Constants.items) {
            mImages.add(new SoftReference<Bitmap>(loadImage(id)));
        }
    }

    private Bitmap loadImage(int id) {
        Bitmap bitmap = decodeSampledBitmapFromResource(mActivity.getResources(), id, 90, 90);
        return bitmap;
    }

    @Override
    public int getItemsCount() {
        return Constants.items.length;
    }

    @Override
    public View getItem(int index, View convertView, ViewGroup parent) {
        ImageView img;
        if (convertView != null) {
            img = (ImageView) convertView;
        } else {
            img = new ImageView(mActivity);
        }
        img.setLayoutParams(params);
        int sidePadding = SlotUtil.convertDpToPixel(10f, mActivity);
        int topBottomPadding = SlotUtil.convertDpToPixel(8.1f, mActivity);
        img.setPadding(sidePadding, topBottomPadding, sidePadding, topBottomPadding);
        SoftReference<Bitmap> bitmapRef = mImages.get(getImageIndex(index));
        Bitmap bitmap = bitmapRef.get();
        if (bitmap == null) {
            bitmap = loadImage(Constants.items[index]);
            mImages.set(index, new SoftReference<Bitmap>(bitmap));
        }
        img.setImageBitmap(bitmap);
        return img;
    }

    public ArrayList<Integer> setItemIndexs(Integer[] itemIndexs, ArrayList<Integer> notWindDrawSymBol) {
        this.itemIndexs = itemIndexs;
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            if (itemIndexs[i] == Constants.UNDEFINE) {
                int ranIndex = random.nextInt(notWindDrawSymBol.size());
                int ranSymbol = notWindDrawSymBol.get(ranIndex);
                itemIndexs[i] = ranSymbol;
                notWindDrawSymBol.remove(ranIndex);
            }
        }
        return notWindDrawSymBol;
    }

    public void setDrawItemIndexs(int symbol) {
        ArrayList<Integer> indexs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (itemIndexs[i] == Constants.UNDEFINE) {
                itemIndexs[i] = getNotDuplicationIndex(symbol);
                indexs.add(Integer.valueOf(itemIndexs[i]));
            } else if (itemIndexs[i] != symbol) {
                if (indexs.contains(Integer.valueOf(itemIndexs[i]))) {
                    itemIndexs[i] = getNotDuplicationIndex(itemIndexs[i]);
                } else {
                    itemIndexs[i] = getNotDuplicationIndex(symbol);
                }
                indexs.add(Integer.valueOf(itemIndexs[i]));
            }
        }
    }

    private Integer getNotDuplicationIndex(int index) {
        Random random = new Random();
        while (true) {
            int ranIndex = random.nextInt(Constants.items.length);
            if (ranIndex == index) {
                continue;
            }
            return ranIndex;
        }
    }

    private int getImageIndex(int index) {
        int randomIndex = new Random().nextInt(Constants.items.length);
        if (this.itemIndexs.length > index) {
            return this.itemIndexs[index] == -1 ? randomIndex : this.itemIndexs[index];
        }
        return randomIndex;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}

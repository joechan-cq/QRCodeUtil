package com.google.zxing.client.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;

import java.lang.ref.WeakReference;

/**
 * 本地图片选择读取工具类
 * Created by YOLANDA on 2015/8/7
 */
public class LocalPictureUtil {
    /**
     * 转换成bitmap,并指定大小
     *
     * @param path 路径
     * @param w    缩放后的宽度
     * @param h    缩放后的高度
     * @return bitmap
     */
    public static Bitmap convertToBitmap(String path, int w, int h) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // 设置为ture只获取图片大小
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 返回为空
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        float scaleWidth = 0.f, scaleHeight = 0.f;
        if (width > w || height > h) {
            // 缩放
            scaleWidth = ((float) width) / w;
            scaleHeight = ((float) height) / h;
        }
        opts.inJustDecodeBounds = false;
        float scale = Math.max(scaleWidth, scaleHeight);
        opts.inSampleSize = (int) scale;
        WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
        return Bitmap.createScaledBitmap(weak.get(), w, h, true);
    }

    /*
    public static Bitmap convertToBItmap(String path, int scale) {
        return BitmapFactory.decodeFile(path, getBitmapOption(scale));
    }

    private static BitmapFactory.Options getBitmapOption(int inSampleSize) {
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
    }*/

    private static Intent getWrapperIntent() {
        Intent innerIntent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            innerIntent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            innerIntent.setAction(Intent.ACTION_GET_CONTENT);
            //innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, "请选择二维码图片");
        return wrapperIntent;
    }

    /**
     * 跳转到系统图形界面,进行选择图片,在选择后,在Activity的onActivityResult中回调
     *
     * @param activity    哪个Activity接收该图片
     * @param requestCode 请求码,用于标识识别
     */
    public static void getLocalPic(Activity activity, int requestCode) {
        activity.startActivityForResult(getWrapperIntent(), requestCode);
    }

    /**
     * 跳转到系统图形界面,进行选择图片,在选择后,在Fragment的onActivityResult中回调
     *
     * @param fragment    哪个v4包的Fragment接收图片
     * @param requestCode 请求码,用于标识识别
     */
    public static void getLocalPic(android.support.v4.app.Fragment fragment, int requestCode) {
        fragment.startActivityForResult(getWrapperIntent(), requestCode);
    }

    /**
     * 跳转到系统图形界面,进行选择图片,在选择后,在Fragment的onActivityResult中回调
     *
     * @param fragment    哪个v7包的Fragment接收图片
     * @param requestCode 请求码,用于标识识别
     */
    public static void getLocalPic(android.app.Fragment fragment, int requestCode) {
        fragment.startActivityForResult(getWrapperIntent(), requestCode);
    }

    /**
     * 接收onActivityResult回调中的Intent,获取图片的路径
     *
     * @param context 上下文
     * @param data    Intent
     * @return
     */
    public static String receiveIntent(Context context, Intent data) {
        String[] proj = {MediaStore.Images.Media.DATA};
        // 获取选中图片的路径
        String uri = data.getData().toString();
        if (uri.startsWith("file://")) {
            uri = uri.substring(7);
            return uri;
        }
        Cursor cursor = context.getContentResolver().query(data.getData(), proj, null, null, null);
        String photo_path = "";
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            photo_path = cursor.getString(column_index);
        }
        cursor.close();
        return photo_path;
    }

    /** 接收示例 在传入参数的Activity或Fragment里
     @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
     if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
     String path = PictureUtil.receiveIntent(getActivity(), data);
     Bitmap bitmap = PictureUtil.convertToBitmap(path, 400, 400); //解析成bitmap
     //do something
     }
     super.onActivityResult(requestCode, resultCode, data);
     }
     */
}

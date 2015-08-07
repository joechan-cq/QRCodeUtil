package com.google.zxing.client.android;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

/**
 * 创建和读取二维码
 * Created by YOLANDA on 2015/8/6
 */

public class BarCodeUtil {
    private static int QR_WIDTH = 400; //生成二维码的长度
    private static int QR_HEIGHT = 400; //生成二维码的高度

    /**
     * 创建QR二维码,并指定大小
     *
     * @param info      url 地址或字符串
     * @param QR_WIDTH  宽
     * @param QR_HEIGHT 高
     * @return Bitmap
     */
    public static Bitmap createBarCode(String info, int QR_WIDTH, int QR_HEIGHT) {
        setCodeSize(QR_WIDTH, QR_HEIGHT);
        return createBarCode(info);
    }

    /**
     * 创建QR二维码
     *
     * @param info 地址或字符串
     * @return Bitmap
     */
    public static Bitmap createBarCode(String info) {
        try {
            // 判断字符串合法性
            if (info == null || "".equals(info) || info.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");//设置编码
            //生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
            BitMatrix bitMatrix = new QRCodeWriter().encode(info, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;// 长乘宽+最后一排宽
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置二维码大小
     *
     * @param qrWidth  宽
     * @param qrHeight 高
     */
    public static void setCodeSize(int qrWidth, int qrHeight) {
        QR_WIDTH = qrWidth;
        QR_HEIGHT = qrHeight;
    }

    /**
     * 解析二维码
     *
     * @param img Bitmap格式的二维码
     * @return Result类
     */
    public static Result ParseQRImage(Bitmap img) {
        int[] intArray = new int[img.getWidth() * img.getHeight()];
        // 将bitmap数据copy到intArray
        img.getPixels(intArray, 0, img.getWidth(), 0, 0, img.getWidth(),
                img.getHeight());

        LuminanceSource source = new RGBLuminanceSource(img.getWidth(),
                img.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));//得到BinaryBitmap

        Reader reader = new MultiFormatReader();
        Result result = null;
        try {
            result = reader.decode(bitmap); //进行解析
        } catch (Resources.NotFoundException | ChecksumException | NotFoundException | com.google.zxing.FormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 解析二维码,得到String
     *
     * @param img Bitmap格式的二维码
     * @return String
     */
    public static String decodeQRImage(Bitmap img) {
        String text = null;
        Result result = ParseQRImage(img);
        if (result != null) {
            text = result.getText();
            return text;
        } else {
            return null;
        }
        // byte[] rawBytes = result.getRawBytes();
        // BarcodeFormat format = result.getBarcodeFormat();
        // ResultPoint[] points = result.getResultPoints();
    }

    /**
     * 解析二维码，得到String\
     *
     * @param imgpath 本地二维码文件的地址
     * @return
     */
    public static String decodeQRImage(String imgpath) {
        Bitmap qrImg = BitmapFactory.decodeFile(imgpath);
        return decodeQRImage(qrImg);
    }
}

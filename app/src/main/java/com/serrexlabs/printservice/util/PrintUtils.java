package com.serrexlabs.printservice.util;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import net.sf.andpdf.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.decrypt.PDFAuthenticationFailureException;

public class PrintUtils {

    private static Bitmap bArr;

    public PrintUtils(String filePath) {
        PdfFind obj = new PdfFind();
        try {
            obj.parsePDF(filePath);
            if (obj.showPage(1, 1) != null)
                bArr = obj.showPage(1, 1);
        } catch (PDFAuthenticationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Bitmap getBitmapImages() {
        return bArr;
    }

    private class PdfFind {
        private PDFFile mPdfFile;
        private PDFPage mPdfPage;

        private void parsePDF(String filename)
                throws PDFAuthenticationFailureException {

            try {
                File f = new File(filename);
                long len = f.length();
                if (len == 0) {
                    Log.i("No File", "File length is 0");
                } else {
                    openFile(f);
                    Log.i("ParsePDF", "parse pdf called f =" + f.toString());
                }
            } catch (PDFAuthenticationFailureException e) {
                throw e;
            } catch (Throwable e) {
                e.printStackTrace();
                Log.i("Error", "Cant Read File");
            }
        }


        public void openFile(File file) throws IOException {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer bb = ByteBuffer.NEW(channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, channel.size()));
            mPdfFile = new PDFFile(bb);

        }

        private Bitmap showPage(int page, int zoom) throws Exception {
            try {
                System.gc();
                mPdfPage = mPdfFile.getPage(page, true);
                Log.i("File Page", "mPdfPage creates width = " + mPdfPage.getWidth());
                float wi = mPdfPage.getWidth();
                float hei = mPdfPage.getHeight();
                RectF clip = null;
                Bitmap bi = mPdfPage.getImage((int) (wi * 1.25),
                        (int) (hei * 1.25), clip, true, true);
                return bi;
            } catch (Throwable e) {
                Log.e("Error", e.getMessage(), e);
            }
            return null;

        }
    }

}
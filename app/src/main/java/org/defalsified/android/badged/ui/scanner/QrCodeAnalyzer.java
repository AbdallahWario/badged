package org.defalsified.android.badged.ui.scanner;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;

/**
 * Image analyzer that detects and decodes QR codes from camera frames
 * visit this page to learn about the zxing lib; https://www.geeksforgeeks.org/how-to-generate-and-read-qr-code-with-java-using-zxing-library/
 */
public class QrCodeAnalyzer implements ImageAnalysis.Analyzer {

    // Interface for QR code result callback
    public interface QrCodeListener {
        void onQrCodeFound(String qrContent);
    }

    private final QrCodeListener listener;
    private final MultiFormatReader multiFormatReader;

    /**
     * Constructor
     *
     * @param listener Callback for QR code detection
     */
    public QrCodeAnalyzer(QrCodeListener listener) {
        this.listener = listener;
        multiFormatReader = new MultiFormatReader();
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        // Get the image data
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        // Get image dimensions
        int width = image.getWidth();
        int height = image.getHeight();

        // Create luminance source
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                data,
                width,
                height,
                0,
                0,
                width,
                height,
                false
        );

        // Create binary bitmap
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            // Try to decode QR code
            Result result = multiFormatReader.decode(bitmap);
            if (result != null) {
                // QR code found, notify listener
                listener.onQrCodeFound(result.getText());
            }
        } catch (NotFoundException e) {
            // No QR code found, that's OK
        } finally {
            //  close the image
            image.close();
        }
    }
}
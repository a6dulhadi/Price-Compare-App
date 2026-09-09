package com.pricecompare.app.pricecompare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.pricecompare.app.R;

import java.util.Collections;
import java.util.List;

public class BarcodeScanActivity extends AppCompatActivity {

    private static final String TAG = "BarcodeScanActivity";
    public static final String EXTRA_BARCODE = "scanned_barcode";

    private LifecycleCameraController cameraController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scan);

        PreviewView previewView = findViewById(R.id.previewView);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        startCamera(previewView);
    }

    private void startCamera(PreviewView previewView) {
        cameraController = new LifecycleCameraController(this);
        cameraController.bindToLifecycle(this);
        previewView.setController(cameraController);

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        BarcodeScanner barcodeScanner = BarcodeScanning.getClient(options);

        cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(this),
                new MlKitAnalyzer(Collections.singletonList(barcodeScanner),
                        CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                        ContextCompat.getMainExecutor(this),
                        result -> {
                            List<Barcode> barcodes = result.getValue(barcodeScanner);
                            if (barcodes != null && !barcodes.isEmpty()) {
                                String code = barcodes.get(0).getRawValue();
                                if (code != null) {
                                    onBarcodeScanned(code);
                                }
                            }
                        }));
    }

    private void onBarcodeScanned(String barcode) {
        
        cameraController.clearImageAnalysisAnalyzer();
        
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BARCODE, barcode);
        setResult(RESULT_OK, intent);
        finish();
    }
}

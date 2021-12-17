package com.aix.memore.interfaces;

import com.aix.memore.models.Memore;

public interface QRScanHistoryInterface {

    void onClick(String memore);
    void onCancelGenerateNewQR();
}

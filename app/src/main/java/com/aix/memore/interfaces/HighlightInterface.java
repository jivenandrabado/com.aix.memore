package com.aix.memore.interfaces;

import com.aix.memore.models.Memore;

public interface HighlightInterface {

    void onHighlightFound(Memore memore);
    void onCredentialsSubmitted();
    void onGenerateNewQRCode();
    void onUploadHighlight();
}

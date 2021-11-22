package com.aix.memore.repositories;

import androidx.annotation.NonNull;

import com.aix.memore.interfaces.HighlightInterface;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.utilities.FirebaseConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HighlightRepo {
    private final FirebaseFirestore db;

    public HighlightRepo() {
        this.db = FirebaseFirestore.getInstance();
    }


    public void getHighlight(HighlightInterface highlightInterface, String id){
        try{

            db.collection(FirebaseConstants.MEMORE)
                    .document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Memore memore = task.getResult().toObject(Memore.class);
                    if (memore != null) {
                        highlightInterface.onHighlightFound(memore);
                    }else{
                        highlightInterface.onHighlightFound(null);
                    }
                }
            });


        }catch (Exception e){
            ErrorLog.WriteErrorLog(e);
        }
    }

    public void addHighlightView(Memore memore) {
//        Viewer vi
        db.collection(FirebaseConstants.MEMORE)
                .document(memore.getMemore_id()).collection(FirebaseConstants.MEMORE_VIEWER)
                .document().set()
    }
}

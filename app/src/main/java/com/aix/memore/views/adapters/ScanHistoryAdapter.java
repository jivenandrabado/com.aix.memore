package com.aix.memore.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aix.memore.R;
import com.aix.memore.interfaces.QRScanHistoryInterface;
import com.aix.memore.models.Memore;
import com.aix.memore.utilities.ErrorLog;
import com.aix.memore.view_models.HighlightViewModel;
import com.aix.memore.views.fragments.QRScanHistoryFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ScanHistoryAdapter  extends RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private int listSize = 0;
    private QRScanHistoryInterface qrScanHistoryInterface;

    // data is passed into the constructor
    public ScanHistoryAdapter(Context context, List<String> data, QRScanHistoryFragment qrScanHistoryInterface) {
        this.mInflater = LayoutInflater.from(context);
        this.qrScanHistoryInterface = qrScanHistoryInterface;
        if(data != null){
            this.mData = data;
            listSize = data.size();

            ErrorLog.WriteDebugLog("LIst size history "+listSize);
        }
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_scan_history, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String memore = mData.get(position);
        try {
            ErrorLog.WriteDebugLog("TO JSON "+ memore);
            JSONObject jsonObject = new JSONObject(memore);
            String full_name = jsonObject.getString("bio_first_name") + " " + jsonObject.getString("bio_last_name");
            holder.myTextView.setText(full_name);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {

        return listSize;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.textViewName);
            myTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        JSONObject jsonObject = new JSONObject(mData.get(getAbsoluteAdapterPosition()));
                        qrScanHistoryInterface.onClick(jsonObject.getString("memore_id"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

}
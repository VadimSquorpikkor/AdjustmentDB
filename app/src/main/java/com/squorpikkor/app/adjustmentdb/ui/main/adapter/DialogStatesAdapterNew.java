package com.squorpikkor.app.adjustmentdb.ui.main.adapter;

import android.annotation.SuppressLint;
import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.squorpikkor.app.adjustmentdb.R;

import java.util.List;

public class DialogStatesAdapterNew extends ArrayAdapter<String> {

    private LayoutInflater inflater;
    private int layout;
    private List<String> sourceList;

    public DialogStatesAdapterNew(Context context, int resource, List<String> sourceList) {
        super(context, resource, sourceList);
        this.sourceList = sourceList;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        @SuppressLint("ViewHolder")
        View view = inflater.inflate(this.layout, parent, false);

        TextView nameView = view.findViewById(R.id.state);

        String state = sourceList.get(position);

        nameView.setText(state);

        return view;
    }
}
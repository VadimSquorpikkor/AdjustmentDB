package com.squorpikkor.app.adjustmentdb.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squorpikkor.app.adjustmentdb.DState;
import com.squorpikkor.app.adjustmentdb.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class StatesAdapter extends RecyclerView.Adapter<StatesAdapter.StatesViewHolder>{

    private final ArrayList<DState> states;
    private final String DATE_PATTERN = "dd.MM.yyyy HH:mm";

    /**Конструктор, в котором передаем ArrayList для RecyclerView */
    public StatesAdapter(ArrayList<DState> states) {
        this.states = states;
    }

    /**Присваиваем xml лэйаут к итему RecyclerView */
    @NonNull
    @Override
    public StatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.state_item, parent, false);
        return new StatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatesAdapter.StatesViewHolder holder, int position) {
        DState state = states.get(position);
        holder.tDate.setText(getRightDate(state.getDate().getTime()));
        holder.tState.setText(state.getState());
    }

    private String getRightDate(long time_stamp_server) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
        return formatter.format(time_stamp_server);
    }

    /**Просто возвращает кол-во элементов в массиве*/
    @Override
    public int getItemCount() {
        return states.size();
    }

    static class StatesViewHolder extends RecyclerView.ViewHolder {
        private final TextView tDate;
        private final TextView tState;

        public StatesViewHolder(@NonNull View itemView) {
            super(itemView);
            tDate = itemView.findViewById(R.id.date);
            tState = itemView.findViewById(R.id.state);
        }
    }
}

package com.squorpikkor.app.adjustmentdb.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squorpikkor.app.adjustmentdb.DEvent;
import com.squorpikkor.app.adjustmentdb.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.Utils.getRightDate;

/**Адаптер для списка всех статусов для выбранного конкретного устройства. Показывает дату, время и сам статус*/
public class StatesAdapter extends RecyclerView.Adapter<StatesAdapter.StatesViewHolder> {

    private final ArrayList<DEvent> states;

    /**
     * Конструктор, в котором передаем ArrayList для RecyclerView
     */
    public StatesAdapter(ArrayList<DEvent> states) {
        this.states = states;
    }

    /**
     * Присваиваем xml лэйаут к итему RecyclerView
     */
    @NonNull
    @Override
    public StatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_state, parent, false);
        return new StatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatesAdapter.StatesViewHolder holder, int position) {
        DEvent state = states.get(position);
        holder.tDate.setText(getRightDate(state.getDate().getTime()));
        holder.tState.setText(state.getState());
    }

    /**
     * Просто возвращает кол-во элементов в массиве
     */
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

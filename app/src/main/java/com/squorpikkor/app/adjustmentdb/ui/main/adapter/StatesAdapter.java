package com.squorpikkor.app.adjustmentdb.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squorpikkor.app.adjustmentdb.DEvent;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import java.util.ArrayList;
import static com.squorpikkor.app.adjustmentdb.Utils.getRightDate;
import static com.squorpikkor.app.adjustmentdb.Utils.getRightTime;

/**Адаптер для списка всех статусов для выбранного конкретного устройства. Показывает дату, время и сам статус*/
public class StatesAdapter extends RecyclerView.Adapter<StatesAdapter.StatesViewHolder> {

    private final ArrayList<DEvent> events;
    MainViewModel mViewModel;

    /**
     * Конструктор, в котором передаем ArrayList для RecyclerView
     */
    public StatesAdapter(ArrayList<DEvent> events, MainViewModel model) {
        this.events = events;
        this.mViewModel = model;
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
        DEvent event = events.get(position);

        holder.tState.setText(mViewModel.getStateNameById(event.getState()));
        holder.tLocation.setText(mViewModel.getLocationNameById(event.getLocation()));
        long time = event.getDate().getTime();
        holder.tDate.setText(String.format("%s / %s", getRightDate(time), getRightTime(time)));
        String daysPassed = String.valueOf(event.daysPassed());
        holder.tDaysPassed.setText(String.format("Дней в работе: %s", daysPassed));
    }

    /**
     * Просто возвращает кол-во элементов в массиве
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    static class StatesViewHolder extends RecyclerView.ViewHolder {
        private final TextView tDate;
        private final TextView tState;
        private final TextView tLocation;
        private final TextView tDaysPassed;

        public StatesViewHolder(@NonNull View itemView) {
            super(itemView);
            tDate = itemView.findViewById(R.id.date);
            tState = itemView.findViewById(R.id.state);
            tLocation = itemView.findViewById(R.id.location2);
            tDaysPassed = itemView.findViewById(R.id.days_passed);
        }
    }
}

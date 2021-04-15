package com.squorpikkor.app.adjustmentdb.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.Utils.insertRightValue;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_UNIT;

public class FoundUnitAdapter extends RecyclerView.Adapter<FoundUnitAdapter.FoundViewHolder>{
    private final ArrayList<DUnit> units;

    /**Конструктор, в котором передаем ArrayList для RecyclerView */
    public FoundUnitAdapter(ArrayList<DUnit> units) {
        this.units = units;
    }

    /**Присваиваем xml лэйаут к итему RecyclerView */
    @NonNull
    @Override
    public FoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_found_dunit, parent, false);
        return new FoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoundViewHolder holder, int position) {
        DUnit unit = units.get(position);
        if (unit.isRepairUnit()) { //Ремонт r_0001 AT6130
            holder.tFirst.setText(REPAIR_UNIT);
            holder.tSecond.setText(insertRightValue(unit.getId()));
            holder.tThird.setText(insertRightValue(unit.getName()));
        }
        if (unit.isSerialUnit()) { //AT6130 13245 123
            holder.tFirst.setText(insertRightValue(unit.getName()));
            holder.tSecond.setText(insertRightValue(unit.getInnerSerial()));
            holder.tThird.setText(insertRightValue(unit.getSerial()));
        }
    }

    /**Просто возвращает кол-во элементов в массиве*/
    @Override
    public int getItemCount() {
        return units.size();
    }

    static class FoundViewHolder extends RecyclerView.ViewHolder {
        private final TextView tFirst;
        private final TextView tSecond;
        private final TextView tThird;

        public FoundViewHolder(@NonNull View itemView) {
            super(itemView);
            tFirst = itemView.findViewById(R.id.textType);
            tSecond = itemView.findViewById(R.id.textFirst);
            tThird = itemView.findViewById(R.id.textSecond);
        }
    }
}

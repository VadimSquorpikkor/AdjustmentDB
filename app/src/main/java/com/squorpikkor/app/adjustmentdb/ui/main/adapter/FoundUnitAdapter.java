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
        holder.tType.setText(unit.getType());
        if (unit.isRepairUnit()) {
            holder.tFirst.setText(unit.getId());
            holder.tSecond.setText("");
        }
        if (unit.isSerialUnit()) {
            holder.tFirst.setText(unit.getName());
            holder.tSecond.setText(unit.getInnerSerial());
        }
    }

    /**Просто возвращает кол-во элементов в массиве*/
    @Override
    public int getItemCount() {
        return units.size();
    }

    static class FoundViewHolder extends RecyclerView.ViewHolder {
        private final TextView tType;
        private final TextView tFirst;
        private final TextView tSecond;

        public FoundViewHolder(@NonNull View itemView) {
            super(itemView);
            tType = itemView.findViewById(R.id.textType);
            tFirst = itemView.findViewById(R.id.textFirst);
            tSecond = itemView.findViewById(R.id.textSecond);
        }
    }
}

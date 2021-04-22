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


//deprecated
public class DRepairUnitAdapter extends RecyclerView.Adapter<DRepairUnitAdapter.DUnitViewHolder>{

    private final ArrayList<DUnit> units;

    /**Конструктор, в котором передаем ArrayList для RecyclerView */
    public DRepairUnitAdapter(ArrayList<DUnit> units) {
        this.units = units;
    }

    /**Присваиваем xml лэйаут к итему RecyclerView */
    @NonNull
    @Override
    public DRepairUnitAdapter.DUnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_runit, parent, false);
        return new DRepairUnitAdapter.DUnitViewHolder(view);
    }

    /**Принимает объект ViewHolder (holder) и порядковый номер элемента массива (position)
     * т.е. у 1-ого элемента View будет порядковый номер 0, он возмёт элемент с этим индексом (заметку)
     * и у ViewHolder'а установить все значения (присвоить значения к TextView) */
    @Override
    public void onBindViewHolder(@NonNull DRepairUnitAdapter.DUnitViewHolder holder, int position) {
        DUnit unit = units.get(position);
        holder.tId.setText(unit.getId());
        holder.tName.setText(unit.getName());
        holder.tSerial.setText(unit.getSerial());
    }

    /**Просто возвращает кол-во элементов в массиве*/
    @Override
    public int getItemCount() {
        return units.size();
    }

    static class DUnitViewHolder extends RecyclerView.ViewHolder {
        private final TextView tId;
        private final TextView tName;
        private final TextView tSerial;

        public DUnitViewHolder(@NonNull View itemView) {
            super(itemView);
            tId = itemView.findViewById(R.id.id);
            tName = itemView.findViewById(R.id.name);
            tSerial = itemView.findViewById(R.id.serial);
        }
    }
}

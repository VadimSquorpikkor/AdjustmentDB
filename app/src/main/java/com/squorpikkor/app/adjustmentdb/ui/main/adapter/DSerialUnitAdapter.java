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

public class DSerialUnitAdapter extends RecyclerView.Adapter<DSerialUnitAdapter.DUnitViewHolder>{

    private final ArrayList<DUnit> units;

    /**Конструктор, в котором передаем ArrayList для RecyclerView */
    public DSerialUnitAdapter(ArrayList<DUnit> units) {
        this.units = units;
    }

    /**Присваиваем xml лэйаут к итему RecyclerView */
    @NonNull
    @Override
    public DUnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dunit_item, parent, false);
        return new DUnitViewHolder(view);
    }

    /**Принимает объект ViewHolder (holder) и порядковый номер элемента массива (position)
    * т.е. у 1-ого элемента View будет порядковый номер 0, он возмёт элемент с этим индексом (заметку)
    * и у ViewHolder'а установить все значения (присвоить значения к TextView) */
    @Override
    public void onBindViewHolder(@NonNull DUnitViewHolder holder, int position) {
        DUnit unit = units.get(position);
        holder.tName.setText(unit.getName());
        holder.tInnerSerial.setText(unit.getInnerSerial());
        holder.tSerial.setText(unit.getSerial());
        holder.tState.setText(unit.getState());
    }

    /**Просто возвращает кол-во элементов в массиве*/
    @Override
    public int getItemCount() {
        return units.size();
    }

    static class DUnitViewHolder extends RecyclerView.ViewHolder {
        private final TextView tName;
        private final TextView tInnerSerial;
        private final TextView tSerial;
        private final TextView tState;

        public DUnitViewHolder(@NonNull View itemView) {
            super(itemView);
            tName = itemView.findViewById(R.id.textName);
            tInnerSerial = itemView.findViewById(R.id.textInnerSerial);
            tSerial = itemView.findViewById(R.id.textSerial);
            tState = itemView.findViewById(R.id.textState);
        }
    }
}

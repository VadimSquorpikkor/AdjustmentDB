package com.squorpikkor.app.adjustmentdb.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;

import java.util.ArrayList;

class DUnitAdapter extends RecyclerView.Adapter<DUnitAdapter.DUnitViewHolder>{

    private final ArrayList<DUnit> units;

    //Конструктор, в котором передаем ArrayList для RecyclerView
    public DUnitAdapter(ArrayList<DUnit> units) {
        this.units = units;
    }

    @NonNull
    @Override
    //Присваиваем xml лэйаут к итему RecyclerView
    public DUnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dunit_item, parent, false);
        return new DUnitViewHolder(view);
    }

    //Принимает объект ViewHolder (holder) и порядковый номер элемента массива (position)
    //т.е. у 1-ого элемента View будет порядковый номер 0, он возмёт элемент с этим индексом (заметку)
    //и у ViewHolder'а установить все значения (присвоить значения к TextView)
    @Override
    public void onBindViewHolder(@NonNull DUnitViewHolder holder, int position) {
        DUnit unit = units.get(position);
        holder.tName.setText(unit.getName());
        holder.tSerial.setText(unit.getSerial());
    }

    //Просто возвращает кол-во элементов в массиве
    @Override
    public int getItemCount() {
        return units.size();
    }

    static class DUnitViewHolder extends RecyclerView.ViewHolder {

        private final TextView tName;
        private final TextView tSerial;

        public DUnitViewHolder(@NonNull View itemView) {
            super(itemView);

            tName = itemView.findViewById(R.id.textName);
            tSerial = itemView.findViewById(R.id.textSerial);
        }
    }
}

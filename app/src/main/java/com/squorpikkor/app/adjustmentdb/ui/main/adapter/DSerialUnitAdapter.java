package com.squorpikkor.app.adjustmentdb.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.Utils.EMPTY_VALUE;
import static com.squorpikkor.app.adjustmentdb.Utils.daysPassed;
import static com.squorpikkor.app.adjustmentdb.Utils.getNameById;
import static com.squorpikkor.app.adjustmentdb.Utils.getRightDate;
import static com.squorpikkor.app.adjustmentdb.Utils.insertRightValue;

//todo это адаптер не для серийных, а для всех типов. надо поменять название
public class DSerialUnitAdapter extends RecyclerView.Adapter<DSerialUnitAdapter.DUnitViewHolder>{

    private final ArrayList<DUnit> units;

    MainViewModel mViewModel;

    /**Конструктор, в котором передаем ArrayList для RecyclerView */
    public DSerialUnitAdapter(ArrayList<DUnit> units, MainViewModel model) {
        this.units = units;
        this.mViewModel = model;
    }

    /**Присваиваем xml лэйаут к итему RecyclerView */
    @NonNull
    @Override
    public DUnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dunit, parent, false);
        return new DUnitViewHolder(view);
    }

    /**Принимает объект ViewHolder (holder) и порядковый номер элемента массива (position)
    * т.е. у 1-ого элемента View будет порядковый номер 0, он возмёт элемент с этим индексом (заметку)
    * и у ViewHolder'а установить все значения (присвоить значения к TextView) */
    @Override
    public void onBindViewHolder(@NonNull DUnitViewHolder holder, int position) {
        DUnit unit = units.get(position);
        holder.tName.setText(getNameById(unit.getName(), mViewModel.getDeviceNameList().getValue(), mViewModel.getDeviceIdList().getValue()));
        holder.tSerial.setText(String.format("№ %s", insertRightValue(unit.getSerial())));
        holder.tInnerSerial.setText(String.format("(вн. %s)", insertRightValue(unit.getInnerSerial())));



        ArrayList<String> statesNames = unit.isSerialUnit()?mViewModel.getSerialStatesNames().getValue():mViewModel.getRepairStatesNames().getValue();
        ArrayList<String> statesId = unit.isSerialUnit()?mViewModel.getSerialStateIdList().getValue():mViewModel.getRepairStateIdList().getValue();
        holder.tState.setText(getNameById(unit.getState(), statesNames, statesId));




        if (unit.getDate()!=null){
            holder.tDate.setText(getRightDate(unit.getDate().getTime()));
            holder.tDatePassed.setText(String.format("%s дней", daysPassed(unit.getDate())));
        } else {
            holder.tDate.setText(EMPTY_VALUE);
            holder.tDatePassed.setText("");
        }
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
        private final TextView tDate;
        private final TextView tDatePassed;

        public DUnitViewHolder(@NonNull View itemView) {
            super(itemView);
            tName = itemView.findViewById(R.id.textName);
            tInnerSerial = itemView.findViewById(R.id.textInnerSerial);
            tSerial = itemView.findViewById(R.id.textSerial);
            tState = itemView.findViewById(R.id.textState);
            tDate = itemView.findViewById(R.id.textDate);
            tDatePassed = itemView.findViewById(R.id.textDatePassed);
        }
    }
}

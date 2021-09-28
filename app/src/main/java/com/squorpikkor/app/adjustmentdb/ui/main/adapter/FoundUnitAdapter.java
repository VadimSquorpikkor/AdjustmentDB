package com.squorpikkor.app.adjustmentdb.ui.main.adapter;

import android.annotation.SuppressLint;
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
import static com.squorpikkor.app.adjustmentdb.Utils.getRightValue;
import static com.squorpikkor.app.adjustmentdb.Constant.REPAIR_UNIT;
import static com.squorpikkor.app.adjustmentdb.Constant.SERIAL_UNIT;

/**Адаптер для списка найденных устройств в режиме мультисканирования*/
public class FoundUnitAdapter extends RecyclerView.Adapter<FoundUnitAdapter.FoundViewHolder>{
    private ArrayList<DUnit> list = new ArrayList<>();
    MainViewModel mViewModel;

    /**Конструктор, в котором передаем ArrayList для RecyclerView */
    public FoundUnitAdapter(MainViewModel model) {
        this.mViewModel = model;
    }

    /**Сеттер. После того, как список передан в адаптер, адаптер автоматом обновляет Recycler, для отображения изменений*/
    @SuppressLint("NotifyDataSetChanged")
    public void setList(ArrayList<DUnit> list) {
        if (list==null) list = new ArrayList<>();//Если list == null, то в ресайклер будет передан пустой лист
        this.list = list;
        notifyDataSetChanged();
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
        DUnit unit = list.get(position);
        String type = unit.isRepairUnit()?REPAIR_UNIT:SERIAL_UNIT;
        String name = mViewModel.getDeviceNameById(unit.getName());
        String serial = getRightValue(unit.getSerial());
        String innerSerial = getRightValue(unit.getInnerSerial());
        String id = getRightValue(unit.getId());
        if (unit.getLastEvent()!=null) holder.deviceState.setText(mViewModel.getStateNameById(unit.getLastEvent().getState()));

        holder.deviceType.setText(type);
        holder.deviceName.setText(name);
        holder.deviceSerial.setText(serial);
        holder.deviceInnerSerial.setText(innerSerial);
        holder.deviceId.setText(id);
    }

    /**Просто возвращает кол-во элементов в массиве*/
    @Override
    public int getItemCount() {
        return list.size();
    }

    static class FoundViewHolder extends RecyclerView.ViewHolder {
        private final TextView deviceType;
        private final TextView deviceName;
        private final TextView deviceSerial;
        private final TextView deviceInnerSerial;
        private final TextView deviceId;
        private final TextView deviceState;

        public FoundViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceType = itemView.findViewById(R.id.dev_type);
            deviceName = itemView.findViewById(R.id.dev_name);
            deviceSerial = itemView.findViewById(R.id.dev_serial);
            deviceInnerSerial = itemView.findViewById(R.id.dev_inner_serial);
            deviceId = itemView.findViewById(R.id.dev_id);
            deviceState = itemView.findViewById(R.id.dev_state);
        }
    }
}

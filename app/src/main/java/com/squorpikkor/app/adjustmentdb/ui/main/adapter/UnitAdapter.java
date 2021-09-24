package com.squorpikkor.app.adjustmentdb.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.Utils;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import java.util.ArrayList;
import static com.squorpikkor.app.adjustmentdb.Utils.getRightDateAndTime;
import static com.squorpikkor.app.adjustmentdb.Utils.getRightValue;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.IS_COMPLETE;

/**Адаптер для элемента списка устройств, найденных поиском по БД по параметрам*/
public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.DUnitViewHolder>{

    private final ArrayList<DUnit> units;

    MainViewModel mViewModel;

    /**Конструктор, в котором передаем ArrayList для RecyclerView */
    public UnitAdapter(ArrayList<DUnit> units, MainViewModel model) {
        this.units = units;
        this.mViewModel = model;
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(DUnit unit);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**Присваиваем xml лэйаут к итему RecyclerView */
    @NonNull
    @Override
    public DUnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dunit, parent, false);
        return new DUnitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DUnitViewHolder holder, int position) {
        DUnit unit = units.get(position);

        holder.tName.setText(mViewModel.getDeviceNameById(unit.getName()));
//        holder.tState.setText(mViewModel.getStateNameById(unit.getState()));
        if (unit.getLastEvent()!=null) holder.tState.setText(mViewModel.getStateNameById(unit.getLastEvent().getState()));
        holder.tSerial.setText(String.format("№ %s", Utils.getRightValue(unit.getSerial())));
        if (unit.getInnerSerial()==null||unit.getInnerSerial().equals(""))holder.tInnerSerial.setText("");
        else holder.tInnerSerial.setText(String.format("(вн. %s)", unit.getInnerSerial()));
        holder.tDatePassed.setText(String.format("%s д.", unit.daysPassed()));

        String dateAndTime = getRightDateAndTime(unit.getDate());
        String location = unit.getLastEvent()==null?"":unit.getLastEvent().getLocation();
        String employee = unit.getEmployee();
        boolean isComplete = unit.isComplete();
        String devPath = mViewModel.getDeviceImageByDevId(unit.getName());
        if (devPath!=null && !devPath.equals("")) Picasso.get().load(devPath).into(holder.deviceImage);//это всё, что нужно для загрузки изображения с помощью Picasso. Кроме простоты, пикассо ещё кэширует загруженные картинки

        holder.tDate.setText(dateAndTime);
        holder.tLocation.setText(getRightValue(mViewModel.getLocationNameById(location)));
        holder.tEmployee.setText(getRightValue(mViewModel.getEmployeeNameById(employee)));
        holder.tIsComplete.setText(isComplete?IS_COMPLETE:"");
    }

    /**Просто возвращает кол-во элементов в массиве*/
    @Override
    public int getItemCount() {
        return units.size();
    }

    class DUnitViewHolder extends RecyclerView.ViewHolder {
        private final TextView tName;
        private final TextView tInnerSerial;
        private final TextView tSerial;
        private final TextView tState;
        private final TextView tDate;
        private final TextView tDatePassed;
        private final TextView tLocation;
        private final TextView tEmployee;
        private final TextView tIsComplete;
        private final ImageView deviceImage;

        public DUnitViewHolder(@NonNull View itemView) {
            super(itemView);
            tName = itemView.findViewById(R.id.textName);
            tInnerSerial = itemView.findViewById(R.id.textInnerSerial);
            tSerial = itemView.findViewById(R.id.textSerial);
            tState = itemView.findViewById(R.id.textState);
            tDate = itemView.findViewById(R.id.textDate);
            tDatePassed = itemView.findViewById(R.id.textDatePassed);
            tLocation = itemView.findViewById(R.id.textLocation);
            tEmployee = itemView.findViewById(R.id.textEmployee);
            tIsComplete = itemView.findViewById(R.id.text_is_complete);
            deviceImage = itemView.findViewById(R.id.deviceImage);

            itemView.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(units.get(getAdapterPosition()));
                }
            });
        }
    }
}

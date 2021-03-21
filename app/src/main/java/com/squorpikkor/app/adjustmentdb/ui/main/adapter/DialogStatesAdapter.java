package com.squorpikkor.app.adjustmentdb.ui.main.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squorpikkor.app.adjustmentdb.R;
import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class DialogStatesAdapter extends RecyclerView.Adapter<DialogStatesAdapter.DialogStatesViewHolder>{

    private ArrayList<String> stateList;

    /**
     * Конструктор, в котором передаем ArrayList для RecyclerView
     */
    public DialogStatesAdapter(ArrayList<String> states) {
        this.stateList = states;
        Log.e(TAG, "**** stateList.size() = "+stateList.size());
    }

    /**
     * Присваиваем xml лэйаут к итему RecyclerView
     */
    @NonNull
    @Override
    public DialogStatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_state_item, parent, false);
        return new DialogStatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DialogStatesAdapter.DialogStatesViewHolder holder, int position) {
        String state = stateList.get(position);
        Log.e(TAG, "onBindViewHolder: "+state+" "+position);
        holder.tState.setText(state);
    }

    /**
     * Просто возвращает кол-во элементов в массиве
     */
    @Override
    public int getItemCount() {
        return stateList.size();
    }

    static class DialogStatesViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        private final TextView tState;

        public DialogStatesViewHolder(@NonNull View itemView) {
            super(itemView);
            tState = itemView.findViewById(R.id.state);
        }

        @Override
        public void onClick(View view) {

        }
    }
}

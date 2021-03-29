package com.squorpikkor.app.adjustmentdb.ui.main.old_stuff;

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

/**Старый вариант адаптера. Для RecyclerView. Не используется*/
public class DialogStatesAdapter_OLD extends RecyclerView.Adapter<DialogStatesAdapter_OLD.DialogStatesViewHolder>{

    private ArrayList<String> stateList;

    /**
     * Конструктор, в котором передаем ArrayList для RecyclerView
     */
    public DialogStatesAdapter_OLD(ArrayList<String> states) {
        this.stateList = states;
        Log.e(TAG, "**** stateList.size() = "+stateList.size());
    }

    /**
     * Присваиваем xml лэйаут к итему RecyclerView
     */
    @NonNull
    @Override
    public DialogStatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_state, parent, false);
        return new DialogStatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DialogStatesAdapter_OLD.DialogStatesViewHolder holder, int position) {
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

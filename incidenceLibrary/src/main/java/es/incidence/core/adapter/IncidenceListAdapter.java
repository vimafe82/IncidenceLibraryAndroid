package es.incidence.core.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.e510.commons.fragment.BaseFragment;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.LogUtil;
import com.e510.commons.utils.Utils;

import java.util.ArrayList;

import es.incidence.core.Constants;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Incidence;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class IncidenceListAdapter extends BaseAdapter
{
    public static final int ROW = 0;

    private static String TAG = makeLogTag(IncidenceListAdapter.class);
    private BaseFragment fragment;
    ArrayList<Incidence> items;

    public IncidenceListAdapter(BaseFragment fragment, ArrayList<Incidence> items)
    {
        this.fragment = fragment;
        this.items = items;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return ROW;
    }

    public View getView (final int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;

        try
        {
            if (convertView == null)
            {
                LayoutInflater inflater = (LayoutInflater) this.fragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.row_incidence, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.layoutRow = rowView.findViewById(R.id.layoutRow);
                viewHolder.txtTitle = rowView.findViewById(R.id.txtTitle);
                viewHolder.txtAddress = rowView.findViewById(R.id.txtAddress);
                viewHolder.txtDate = rowView.findViewById(R.id.txtDate);
                viewHolder.txtStatus = rowView.findViewById(R.id.txtStatus);

                FontUtils.setTypeValueText(viewHolder.txtTitle, Constants.FONT_SEMIBOLD, fragment.getContext());
                FontUtils.setTypeValueText(viewHolder.txtAddress, Constants.FONT_REGULAR, fragment.getContext());
                FontUtils.setTypeValueText(viewHolder.txtDate, Constants.FONT_REGULAR, fragment.getContext());
                FontUtils.setTypeValueText(viewHolder.txtStatus, Constants.FONT_SEMIBOLD, fragment.getContext());

                int radius = 8;//= Integer.parseInt(AppConfiguration.getInstance().appearance.roundedFields);
                GradientDrawable back =  Utils.createGradientDrawable(fragment.getContext(), android.R.color.white, radius);
                back.setStroke(Utils.dpToPx(1), Utils.getColor(fragment.getContext(), R.color.incidence200));
                viewHolder.layoutRow.setBackground(back);

                rowView.setTag(viewHolder);
            }

            final Incidence incidence = items.get(position);
            final ViewHolder holder = (ViewHolder) rowView.getTag();

            String city = incidence.city != null ? " " + fragment.getString(R.string.incidence_in) + " " + incidence.city : "";
            String title = incidence.getTitle() + city;
            holder.txtTitle.setText(title);
            holder.txtAddress.setText(incidence.street);
            holder.txtDate.setText(incidence.dateCreated);
            if (incidence.isClosed())
            {
                holder.txtStatus.setText(fragment.getString(R.string.incidence_status_closed));

                int radius = 100;
                GradientDrawable back =  Utils.createGradientDrawable(fragment.getContext(), R.color.success, radius);
                holder.txtStatus.setBackground(back);
            }
            else if (incidence.isCanceled())
            {
                holder.txtStatus.setText(fragment.getString(R.string.incidence_status_canceled));

                int radius = 100;
                GradientDrawable back =  Utils.createGradientDrawable(fragment.getContext(), R.color.error, radius);
                holder.txtStatus.setBackground(back);
            }
            else
            {
                holder.txtStatus.setText(fragment.getString(R.string.incidence_status_active));

                int radius = 100;
                GradientDrawable back =  Utils.createGradientDrawable(fragment.getContext(), R.color.incidence500, radius);
                holder.txtStatus.setBackground(back);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.onClickRow(incidence);
                }
            });
        }
        catch (Exception e)
        {
            LogUtil.logE(TAG, "Error: " + e.getMessage(), e);
        }

        return rowView;
    }

    private class ViewHolder
    {
        private RelativeLayout layoutRow;
        private TextView txtTitle;
        private TextView txtAddress;
        private TextView txtDate;
        private TextView txtStatus;
    }
}
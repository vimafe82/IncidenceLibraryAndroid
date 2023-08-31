package es.incidence.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.e510.commons.fragment.BaseFragment;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.LogUtil;
import com.e510.commons.utils.Utils;

import java.util.ArrayList;

import es.incidence.core.Constants;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Beacon;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.holder.ListViewHolder;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class BeaconVehicleListAdapter extends BaseAdapter
{
    public static final int ROW = 0;

    private static String TAG = makeLogTag(BeaconVehicleListAdapter.class);
    private BaseFragment fragment;
    ArrayList<Object> items;

    public BeaconVehicleListAdapter(BaseFragment fragment, ArrayList<Object> items)
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
                rowView = inflater.inflate(R.layout.row_list, parent, false);

                ListViewHolder viewHolder = new ListViewHolder();
                viewHolder.layoutRoot = rowView.findViewById(R.id.layoutRoot);
                viewHolder.layoutRow = rowView.findViewById(R.id.layoutRow);
                viewHolder.line = rowView.findViewById(R.id.line);
                viewHolder.imgLeft = rowView.findViewById(R.id.imgLeft);
                viewHolder.imgRight = rowView.findViewById(R.id.imgRight);
                viewHolder.txtTitle = rowView.findViewById(R.id.txtTitle);
                viewHolder.txtSubtitle = rowView.findViewById(R.id.txtSubtitle);
                viewHolder.imgExclamation = rowView.findViewById(R.id.imgExclamation);
                if (viewHolder.txtTitle != null)
                    FontUtils.setTypeValueText(viewHolder.txtTitle, Constants.FONT_REGULAR, fragment.getContext());
                if (viewHolder.txtSubtitle != null)
                    FontUtils.setTypeValueText(viewHolder.txtSubtitle, Constants.FONT_REGULAR, fragment.getContext());

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.imgLeft.getLayoutParams();
                params.width = Utils.dpToPx(18);
                params.height = Utils.dpToPx(18);
                viewHolder.imgLeft.setLayoutParams(params);

                rowView.setTag(viewHolder);
            }

            final ListViewHolder holder = (ListViewHolder) rowView.getTag();
            final Object object = items.get(position);

            if (object instanceof Vehicle)
            {
                Vehicle vehicle = (Vehicle) object;
                if (vehicle.beacon == null)
                {
                    holder.imgLeft.setImageDrawable(Utils.getDrawable(fragment.getContext(), R.drawable.beacon_black_disabled));
                }
                else
                {
                    holder.imgLeft.setImageDrawable(Utils.getDrawable(fragment.getContext(), R.drawable.beacon_black));
                }

                holder.imgLeft.setVisibility(View.VISIBLE);
                holder.txtTitle.setText(vehicle.getName());
                holder.txtTitle.setTextColor(Utils.getColor(fragment.getContext(), R.color.black600));
                holder.imgRight.setImageDrawable(Utils.getDrawable(fragment.getContext(), R.drawable.icon_arrow_right));

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imgRight.getLayoutParams();
                params.width = Utils.dpToPx(6.5f);
                params.height = Utils.dpToPx(11.5f);
                params.rightMargin = Utils.dpToPx(24);
                holder.imgRight.setLayoutParams(params);
            }
            else
            {
                holder.imgLeft.setVisibility(View.GONE);
                holder.txtTitle.setText(fragment.getString(R.string.add_new_vehicle));
                holder.txtTitle.setTextColor(Utils.getColor(fragment.getContext(), R.color.incidence500));
                holder.imgRight.setImageDrawable(Utils.getDrawable(fragment.getContext(), R.drawable.icon_plus));

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imgRight.getLayoutParams();
                params.width = Utils.dpToPx(14);
                params.height = Utils.dpToPx(14);
                params.rightMargin = Utils.dpToPx(21);
                holder.imgRight.setLayoutParams(params);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.onClickRow(object);
                }
            });
        }
        catch (Exception e)
        {
            LogUtil.logE(TAG, "Error: " + e.getMessage(), e);
        }

        return rowView;
    }
}
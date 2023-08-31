package es.incidence.core.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.e510.commons.fragment.BaseFragment;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.LogUtil;
import com.e510.commons.utils.Utils;
import com.e510.commons.utils.config.AppConfiguration;

import java.util.ArrayList;

import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Insurance;
import es.incidence.core.manager.ImageManager;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class InsuranceListAdapter extends BaseAdapter
{
    public static final int HEADER = 0;
    public static final int ROW = 1;

    private static String TAG = makeLogTag(InsuranceListAdapter.class);
    private BaseFragment fragment;
    ArrayList<Object> items;

    public InsuranceListAdapter(BaseFragment fragment, ArrayList<Object> items)
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
        return 2;
    }

    @Override
    public int getItemViewType(int position) {

        Object object = items.get(position);

        if (object instanceof String) {
            return HEADER;
        }

        return ROW;
    }

    public View getView (final int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;

        try
        {
            if (getItemViewType(position) == HEADER)
            {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) this.fragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    rowView = inflater.inflate(R.layout.row_insurance_header, parent, false);
                    FontUtils.setTypeValueText(rowView, this.fragment.getActivity());

                    HeaderHolder viewHolder = new HeaderHolder();
                    viewHolder.txtTitle = rowView.findViewById(R.id.txtTitle);
                    rowView.setTag(viewHolder);

                }

                final String str = (String) items.get(position);

                final HeaderHolder holder = (HeaderHolder) rowView.getTag();
                holder.txtTitle.setText(str);
            }
            else
            {
                if (convertView == null)
                {
                    LayoutInflater inflater = (LayoutInflater) this.fragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    rowView = inflater.inflate(R.layout.row_insurance, parent, false);
                    FontUtils.setTypeValueText(rowView, this.fragment.getActivity());

                    ViewHolder viewHolder = new ViewHolder();
                    viewHolder.layoutRow = rowView.findViewById(R.id.layoutRow);
                    viewHolder.imgLeft = rowView.findViewById(R.id.imgLeft);
                    viewHolder.txtTitle = rowView.findViewById(R.id.txtTitle);
                    viewHolder.imgArrow = rowView.findViewById(R.id.imgArrow);
                    viewHolder.line = rowView.findViewById(R.id.line);

                    rowView.setTag(viewHolder);
                }

                final Insurance insurance = (Insurance) items.get(position);

                final ViewHolder holder = (ViewHolder) rowView.getTag();

                holder.txtTitle.setText(insurance.name);
                //holder.imgLeft.setImageDrawable(Utils.getDrawable(fragment.getActivity(), insurance.image));
                if (insurance.image != null)
                    ImageManager.loadImage(fragment.getContext(), insurance.image, holder.imgLeft);
                else
                    holder.imgLeft.setImageDrawable(null);

                if (position >= getCount()-1)
                {
                    holder.line.setVisibility(View.INVISIBLE);
                }
                else
                {
                    holder.line.setVisibility(View.VISIBLE);
                }


                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fragment.onClickRow(insurance);
                    }
                });
            }
        }
        catch (Exception e)
        {
            LogUtil.logE(TAG, "Error: " + e.getMessage(), e);
        }

        return rowView;
    }

    private class HeaderHolder
    {
        private TextView txtTitle;
    }

    private class ViewHolder
    {
        private RelativeLayout layoutRow;
        private ImageView imgLeft;
        private TextView txtTitle;
        private ImageView imgArrow;
        private View line;
    }


}

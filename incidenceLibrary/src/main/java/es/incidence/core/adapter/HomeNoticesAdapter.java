package es.incidence.core.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.e510.commons.fragment.BaseFragment;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.LogUtil;
import com.e510.commons.utils.ScreenUtils;
import com.e510.commons.utils.Utils;
import com.e510.commons.utils.swipe.OnSwipeTouchListener;

import java.util.ArrayList;

import es.incidence.core.Constants;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Notification;
import es.incidence.core.utils.view.IOnSwipeTouchListener;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class HomeNoticesAdapter extends BaseAdapter
{
    public static final int ROW = 0;

    private static String TAG = makeLogTag(HomeNoticesAdapter.class);
    private BaseFragment fragment;
    ArrayList<Notification> items;

    public HomeNoticesAdapter(BaseFragment fragment, ArrayList<Notification> items)
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
                rowView = inflater.inflate(R.layout.row_notification, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.layoutRow = rowView.findViewById(R.id.layoutRow);
                viewHolder.layoutRowDelete = rowView.findViewById(R.id.layoutRowDelete);
                viewHolder.imgLeft = rowView.findViewById(R.id.imgLeft);
                viewHolder.imgRight = rowView.findViewById(R.id.imgRight);
                viewHolder.txtTitle = rowView.findViewById(R.id.txtTitle);
                viewHolder.txtSubtitle = rowView.findViewById(R.id.txtSubtitle);

                FontUtils.setTypeValueText(viewHolder.txtTitle, Constants.FONT_SEMIBOLD, fragment.getContext());
                FontUtils.setTypeValueText(viewHolder.txtSubtitle, Constants.FONT_REGULAR, fragment.getContext());

                int radius = 8;
                GradientDrawable back =  Utils.createGradientDrawable(fragment.getContext(), android.R.color.white, radius);
                viewHolder.layoutRow.setBackground(back);

                GradientDrawable backDelete =  Utils.createGradientDrawable(fragment.getContext(), R.color.error100, radius);
                viewHolder.layoutRowDelete.setBackground(backDelete);

                rowView.setTag(viewHolder);
            }

            final Notification notification = (Notification) items.get(position);

            final ViewHolder holder = (ViewHolder) rowView.getTag();

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.layoutRow.getLayoutParams();
            params.leftMargin = Utils.dpToPx(24);
            params.rightMargin = Utils.dpToPx(24);
            holder.layoutRow.setLayoutParams(params);
            holder.layoutRowDelete.setVisibility(View.INVISIBLE);

            holder.txtTitle.setText(notification.title);
            holder.txtSubtitle.setText(notification.text);

            if (notification.theme != null && notification.theme.equals("1"))
            {
                holder.imgLeft.setVisibility(View.VISIBLE);
                holder.imgRight.setVisibility(View.GONE);

                int radius = 8;
                GradientDrawable back =  Utils.createGradientDrawable(fragment.getContext(), R.color.error100, radius);
                holder.layoutRow.setBackground(back);

                holder.txtTitle.setTextColor(Utils.getColor(fragment.getContext(), android.R.color.white));
                holder.txtSubtitle.setTextColor(Utils.getColor(fragment.getContext(), android.R.color.white));
            }
            else
            {
                holder.imgLeft.setVisibility(View.GONE);
                holder.imgRight.setVisibility(View.VISIBLE);

                int radius = 8;
                GradientDrawable back =  Utils.createGradientDrawable(fragment.getContext(), android.R.color.white, radius);
                holder.layoutRow.setBackground(back);

                holder.txtTitle.setTextColor(Utils.getColor(fragment.getContext(), R.color.black600));
                holder.txtSubtitle.setTextColor(Utils.getColor(fragment.getContext(), R.color.black600));
            }

            if (notification.themeStatus == 1) // "themeStatus": 0=Default; 1=It can not be eliminated,
            {
                holder.layoutRow.setOnTouchListener(null);
                holder.layoutRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fragment.onClickRow(notification);
                    }
                });
            }
            else
            {
                holder.layoutRow.setOnClickListener(null);
                holder.layoutRow.setOnTouchListener(new IOnSwipeTouchListener(fragment.getContext()) {
                    public void onSwipeRight() {
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.layoutRow.getLayoutParams();
                        params.leftMargin = Utils.dpToPx(24);
                        params.rightMargin = Utils.dpToPx(24);
                        holder.layoutRow.setLayoutParams(params);
                        holder.layoutRowDelete.setVisibility(View.INVISIBLE);
                    }
                    public void onSwipeLeft() {

                        double margin = ScreenUtils.getScreenWidth(fragment.getContext());
                        int distance = (int) (margin * 0.35f);
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.layoutRow.getLayoutParams();
                        params.leftMargin = -distance;//Utils.dpToPx(-distance);
                        params.rightMargin = distance;//Utils.dpToPx(distance);
                        holder.layoutRow.setLayoutParams(params);
                        holder.layoutRowDelete.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onClick() {
                        super.onClick();

                        fragment.onClickRow(notification);
                    }
                });
            }


            holder.layoutRowDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.onClickRow(new DeleteNotice(notification));
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
        private RelativeLayout layoutRowDelete;
        private ImageView imgLeft;
        private ImageView imgRight;
        private TextView txtTitle;
        private TextView txtSubtitle;
    }

    public class DeleteNotice
    {
        public Notification notification;

        public DeleteNotice(Notification n)
        {
            this.notification = n;
        }
    }

}
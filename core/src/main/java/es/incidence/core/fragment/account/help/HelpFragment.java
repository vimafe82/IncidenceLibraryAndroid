package es.incidence.core.fragment.account.help;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.e510.commons.utils.ShareUtils;
import com.e510.commons.utils.StringUtils;
import com.e510.commons.utils.Utils;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.util.ArrayList;

import es.incidence.core.BuildConfig;
import es.incidence.core.Constants;
import es.incidence.core.Core;
import es.incidence.core.R;
import es.incidence.core.activity.FullscreenYoutubeActivity;
import es.incidence.core.activity.SimpleYoutubeActivity;
import es.incidence.core.activity.YoutubeActivity;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.TutorialVideo;
import es.incidence.core.entity.ListItem;
import es.incidence.core.fragment.common.ListFragment;
import es.incidence.core.fragment.common.WebFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.INotification;

public class HelpFragment extends ListFragment
{
    private static final String TAG = makeLogTag(HelpFragment.class);

    private static final int ROW_HELP = 0;
    private static final int ROW_PHONE = 1;
    private static final int ROW_EMAIL = 2;
    private static final int ROW_FAQS = 3;
    private static final int ROW_PRIVACY = 4;
    private static final int ROW_TUTORIAL = 5;

    private boolean isHelpOpen = true;
    private boolean isTutorialOpen = false;
    private ArrayList<TutorialVideo> tutos = new ArrayList<>();

    public static HelpFragment newInstance()
    {
        HelpFragment fragment = new HelpFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.help;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
        }
    }

    @Override
    public ListAdapter.Type getType() {
        return ListAdapter.Type.TITLE;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);
        rootView.findViewById(R.id.inavigation).setVisibility(View.GONE);
        RelativeLayout layoutRoot = rootView.findViewById(getLayoutRootId());
        layoutRoot.setBackgroundColor(Utils.getColor(getContext(), R.color.incidencePrincipal));

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_help_header, null);


        RelativeLayout menuEllipseLayout = view.findViewById(R.id.menuEllipseLayout);
        menuEllipseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeThis();
            }
        });

        layoutTopListView.addView(view);
        layoutTopListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadData()
    {
        setNavigationTitle(getString(getTitleId()));


        showHud();
        Api.getTutorialVideos(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {
                hideHud();
                tutos.clear();

                if (response.isSuccess())
                {
                    ArrayList<TutorialVideo> temp = response.getList("data", TutorialVideo.class);
                    if (temp != null && temp.size() > 0)
                    {
                        tutos.addAll(temp);
                    }
                }

                setItems();
            }
        });
    }

    private void setItems()
    {
        ArrayList<ListItem> temp = new ArrayList<>();

        int color = Utils.getColor(getContext(), R.color.white);

        Drawable arrowRight = Utils.getDrawable(getContext(), R.drawable.icon_arrow_right_white);
        Drawable arrowDown = Utils.getDrawable(getContext(), R.drawable.icon_arrow_down_white);
        Drawable arrowUp = Utils.getDrawable(getContext(), R.drawable.icon_arrow_up_white);




        ListItem l0 = new ListItem(getString(R.string.help), ROW_HELP);
        l0.titleColor = color;
        l0.rightDrawable = isHelpOpen ? arrowUp : arrowDown;
        l0.rightDrawableSize = 30;
        temp.add(l0);

        if (isHelpOpen)
        {
            Drawable drawable = Utils.getDrawable(getContext(), R.drawable.icon_phone);
            drawable.setTint(color);
            ListItem l1 = new ListItem(" " + getString(R.string.contact_phone), ROW_PHONE);
            l1.titleColor = color;
            l1.leftDrawable = drawable;
            l1.rightDrawable = arrowRight;
            //temp.add(l1);


            drawable = Utils.getDrawable(getContext(), R.drawable.icon_document);
            drawable.setTint(color);
            ListItem l4 = new ListItem(getString(R.string.privacy), ROW_PRIVACY);
            l4.leftDrawableSize = Utils.dpToPx(20);
            l4.titleColor = color;
            l4.leftDrawable = drawable;
            l4.rightDrawable = arrowRight;
            temp.add(l4);

            drawable = Utils.getDrawable(getContext(), R.drawable.icon_email);
            drawable.setTint(color);
            ListItem l2 = new ListItem(getString(R.string.contact_email), ROW_EMAIL);
            l2.titleColor = color;
            l2.leftDrawable = drawable;
            l2.rightDrawable = arrowRight;
            temp.add(l2);

            drawable = Utils.getDrawable(getContext(), R.drawable.icon_question);
            drawable.setTint(color);
            ListItem l3 = new ListItem(getString(R.string.contact_faqs), ROW_FAQS);
            l3.titleColor = color;
            l3.leftDrawable = drawable;
            l3.rightDrawable = arrowRight;
            temp.add(l3);
        }

        Drawable drawable = Utils.getDrawable(getContext(), R.drawable.ic_youtube);
        drawable.setTint(color);
        ListItem l5 = new ListItem(getString(R.string.contact_tutorial), ROW_TUTORIAL);
        l5.titleColor = color;
        l5.leftDrawable = drawable;
        l5.rightDrawable = isTutorialOpen ? arrowUp : arrowDown;
        l5.rightDrawableSize = 30;
        temp.add(l5);


        if (isTutorialOpen)
        {
            for (int i = 0; i < tutos.size(); i++)
            {
                TutorialVideo tutorialVideo = tutos.get(i);
                ListItem l3 = new ListItem(tutorialVideo.title, tutorialVideo);
                l3.titleColor = color;
                l3.image = tutorialVideo.img;
                l3.leftDrawableSize = 150;
                l3.rightDrawableSize = 0;
                temp.add(l3);
            }
        }


        renewItems(temp);
    }

    @Override
    public void onClickRow(Object object)
    {
        if (object instanceof ListItem)
        {
            ListItem listItem = (ListItem) object;

            if (listItem.object != null && listItem.object instanceof TutorialVideo)
            {
                TutorialVideo tutorialVideo = (TutorialVideo) listItem.object;

                //mListener.addFragmentAnimated(WebFragment.newInstance(tutorialVideo.url, tutorialVideo.title));
                //mListener.addFragmentAnimated(YoutubeFragment.newInstance(tutorialVideo.url, tutorialVideo.title));
                /*
                Intent intent;
                intent = new Intent(getActivity(), YoutubeActivity.class);
                Bundle b = new Bundle();
                b.putString("code", tutorialVideo.code);
                b.putString("title", tutorialVideo.title);
                intent.putExtras(b); //
                getActivity().startActivity(intent);
                */
                Intent intent;
                //intent = new Intent(getActivity(), SimpleYoutubeActivity.class);
                intent = new Intent(getActivity(), FullscreenYoutubeActivity.class);
                Bundle b = new Bundle();
                b.putString("code", tutorialVideo.code);
                b.putString("title", tutorialVideo.title);
                intent.putExtras(b); //
                getActivity().startActivity(intent);

                /*
                ActivityOptions options =
                        ActivityOptions.makeCustomAnimation(getActivity(), R.anim.enter_from_right, R.anim.enter_from_right);
                getActivity().startActivity(intent, options.toBundle());
                */

                /*
                Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                        getBaseActivity(),
                        BuildConfig.YOUTUBE_ID,
                        //DeveloperKey.DEVELOPER_KEY,
                        tutorialVideo.code,
                        200,
                        true,
                        true
                );
                startActivity(intent);
                */

                //mListener.addFragmentAnimated(YoutubeFragment.newInstance(tutorialVideo.code, tutorialVideo.title));

                return;
            }

            int row = (int) listItem.object;

            if (row == ROW_HELP)
            {
                isHelpOpen = !isHelpOpen;
                setItems();
            }
            else if (row == ROW_PHONE)
            {
                showCallPopUp(listItem);
            }
            else if (row == ROW_EMAIL)
            {
                String emails[] = {Constants.EMAIL_CONTACT};
                ShareUtils.shareEmail(getContext(), getString(R.string.contact_email), emails, "");
            }
            else if (row == ROW_FAQS)
            {

                String url = getString(R.string.faqs_url);
                if (url == null || "".equals(url)) {
                    url = Constants.URL_FAQS;
                }

                mListener.addFragmentAnimated(WebFragment.newInstance(url, getString(getTitleId())));
            }
            else if (row == ROW_PRIVACY)
            {
                mListener.addFragmentAnimated(PrivacyFragment.newInstance());
            }
            else if (row == ROW_TUTORIAL)
            {
                isTutorialOpen = !isTutorialOpen;
                setItems();
            }
        }
    }

    private void showCallPopUp(ListItem listItem)
    {
        hideKeyboard();

        String title = null;
        String message = null;
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.call_to, Constants.PHONE_CONTACT));
        options.add(getString(R.string.cancel));
        ArrayList<Integer> optionsColors = new ArrayList<>();
        optionsColors.add(Utils.getColor(getContext(), R.color.black600));
        optionsColors.add(Utils.getColor(getContext(), R.color.error));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null)
                {
                    int index = (int)view.getTag();
                    if (index == 0)
                    {
                        Core.callPhone(Constants.PHONE_CONTACT);
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }
}
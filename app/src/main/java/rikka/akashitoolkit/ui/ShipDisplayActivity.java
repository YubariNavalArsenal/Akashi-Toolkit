package rikka.akashitoolkit.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rikka.akashitoolkit.R;
import rikka.akashitoolkit.adapter.ViewPagerStateAdapter;
import rikka.akashitoolkit.model.Equip;
import rikka.akashitoolkit.model.ExtraIllustration;
import rikka.akashitoolkit.model.Ship;
import rikka.akashitoolkit.model.ShipClass;
import rikka.akashitoolkit.model.ShipVoice;
import rikka.akashitoolkit.network.RetrofitAPI;
import rikka.akashitoolkit.staticdata.EquipList;
import rikka.akashitoolkit.staticdata.ExtraIllustrationList;
import rikka.akashitoolkit.staticdata.ShipClassList;
import rikka.akashitoolkit.staticdata.ShipList;
import rikka.akashitoolkit.support.MusicPlayer;
import rikka.akashitoolkit.support.Settings;
import rikka.akashitoolkit.support.StaticData;
import rikka.akashitoolkit.utils.KCStringFormatter;
import rikka.akashitoolkit.utils.MyPasswordTransformationMethod;
import rikka.akashitoolkit.utils.MySpannableFactory;
import rikka.akashitoolkit.utils.Utils;

/**
 * Created by Rikka on 2016/3/30.
 */
public class ShipDisplayActivity extends BaseItemDisplayActivity implements View.OnTouchListener {
    public static final String EXTRA_ITEM_ID = "EXTRA_ITEM_ID";

    private Toolbar mToolbar;
    private CoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private RecyclerView mRecyclerView;
    private Ship mItem;
    private int mId;
    private Adapter mAdapter;

    private int mScrollY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int id = -1;
        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ITEM_ID)) {
            id = intent.getIntExtra(EXTRA_ITEM_ID, 0);
        } else if (intent.getData() != null) {
            try {
                id = Integer.parseInt(intent.getData().toString().split("/")[3]);
            } catch (Exception ignored) {
            }
        }

        if (getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
            String extra = getIntent().getStringExtra(EXTRA_EXTRA);
            if (extra != null) {
                try {
                    id = Integer.parseInt(extra);
                } catch (Exception ignored) {
                }
            }
        }

        setContentView(R.layout.activity_ship_display);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, OrientationHelper.VERTICAL, false));

        mScrollY = 0;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mScrollY += dy;
            }
        });

        setViews();

        setItem(id, false);
        mCoordinatorLayout.setBackgroundResource(R.color.background);
    }

    private void setItem(int id, boolean reveal) {
        if (id == mId) {
            return;
        }

        Ship item = ShipList.findItemById(this, id);

        if (item == null) {
            Log.d("ShipDisplayActivity", "Ship not found? id=" + Integer.toString(mId));
            finish();
            return;
        }

        setItem(item, reveal);
    }

    private int getEquipCount(Ship item) {
        if (item == null) {
            return 0;
        }

        return item.getSlot();
    }

    @SuppressLint("DefaultLocale")
    private void setToolbarTitle() {
        if (mItem.getName() != null) {
            ShipClass shipClass = ShipClassList.findItemById(this, mItem.getCtype());

            String c = "";
            if (shipClass != null) {
                c = String.format("%s%s号舰", shipClass.getName(), Utils.getChineseNumberString(mItem.getCnum()));
            } else {
                Log.d("ShipDisplayActivity", "No ship class: " + mItem.getName().get(this));
            }
            ((TextView) mToolbar.findViewById(android.R.id.title)).setText(mItem.getName().get(ShipDisplayActivity.this));
            ((TextView) mToolbar.findViewById(android.R.id.summary)).setText(String.format("No.%s %s",
                    mItem.getWiki_id(),
                    c/*,
                    ShipList.shipType[mItem.getType()]*/));
        }
    }

    private static final int FADE_IN = 100;
    private static final int FADE_OUT = 200;

    private void setItem(Ship item, final boolean reveal) {
        if (item.getId() == mId) {
            return;
        }

        int offsetY = 0;
        if (mItem != null) {
            offsetY = (getEquipCount(item) - getEquipCount(mItem)) * Utils.dpToPx(48);
        }

        final int y = mScrollY + offsetY;
        mScrollY = 0;

        mItem = item;
        mId = item.getId();

        mAdapter = new Adapter();

        if (!reveal || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setToolbarTitle();
        }

        // so bad
        if (!reveal) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AlphaAnimation animation = new AlphaAnimation(1, 0);
                animation.setDuration(FADE_IN);
                animation.setInterpolator(new FastOutSlowInInterpolator());
                mAppBarLayout.startAnimation(animation);

                if (!StaticData.instance(this).isTablet)
                    getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(ShipDisplayActivity.this, R.color.colorItemDisplayStatusBar)));

                Utils.colorAnimation(
                        ContextCompat.getColor(this, R.color.background),
                        ContextCompat.getColor(this, R.color.colorItemDisplayStatusBar),
                        FADE_IN,
                        new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                mCoordinatorLayout.setBackgroundColor(
                                        (int) animator.getAnimatedValue());
                            }
                        });
            }

            AlphaAnimation animation = new AlphaAnimation(1, 0);
            animation.setDuration(FADE_IN);
            animation.setFillAfter(true);
            animation.setInterpolator(new FastOutSlowInInterpolator());
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation _animation) {
                    _animation.cancel();

                    if (mItem.getName() != null) {
                        setToolbarTitle();
                    }

                    mRecyclerView.setAdapter(mAdapter);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mCoordinatorLayout.setBackgroundColor(ContextCompat.getColor(ShipDisplayActivity.this, R.color.background));

                        animateRevealColorFromCoordinates(mCoordinatorLayout, R.color.background, mX, mY, true);

                        mAppBarLayout.setAlpha(1);
                        mRecyclerView.setAlpha(1);
                    } else {
                        AlphaAnimation animation = new AlphaAnimation(0, 1);
                        animation.setDuration(FADE_IN);
                        animation.setInterpolator(new AccelerateDecelerateInterpolator());
                        mRecyclerView.startAnimation(animation);
                    }

                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.scrollBy(0, y);
                        }
                    });
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mRecyclerView.startAnimation(animation);
        }

        cache_file = getCacheDir().getAbsolutePath() + "/json/" + mItem.getId();
        File file = new File(cache_file);

        if (file.exists()) {
            try {
                mAdapter.setData((List<ShipVoice>) new Gson().fromJson(
                        new FileReader(cache_file),
                        new TypeToken<ArrayList<ShipVoice>>() {
                        }.getType()));

                Log.d(getClass().getSimpleName(), "use cached file: " + cache_file);

                // get new after 1 day
                if (System.currentTimeMillis() - file.lastModified() > 60 * 60 * 24 * 1000L) {
                    downloadVoiceList();
                }
            } catch (Exception ignored) {
                downloadVoiceList();
            }
        } else {
            downloadVoiceList();
        }
    }

    private String cache_file;

    private void downloadVoiceList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.kcwiki.moe")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI.Voice service = retrofit.create(RetrofitAPI.Voice.class);
        Call<List<ShipVoice>> call = service.get(mItem.getId());

        call.enqueue(new Callback<List<ShipVoice>>() {
            @Override
            public void onResponse(Call<List<ShipVoice>> call, Response<List<ShipVoice>> response) {
                //Gson gson = new Gson();
                //mAdapter.setData((List<ShipVoice>) gson.fromJson(new JsonReader(new InputStreamReader(response.body().byteStream())), new TypeToken<ArrayList<ShipVoice>>() {}.getType()));
                mAdapter.setData(response.body());

                Gson gson = new Gson();
                Utils.saveStreamToFile(new ByteArrayInputStream(gson.toJson(response.body()).getBytes()),
                        cache_file);
            }

            @Override
            public void onFailure(Call<List<ShipVoice>> call, Throwable t) {

            }
        });
    }

    int mX, mY;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mX = (int) event.getRawX();
        mY = (int) event.getRawY() - Utils.dpToPx(24);
        return false;
    }

    private class Adapter extends RecyclerView.Adapter {
        class ViewHolderHead extends RecyclerView.ViewHolder {
            public ViewHolderHead(View itemView) {
                super(itemView);
            }
        }

        class ViewHolderHead2 extends RecyclerView.ViewHolder {
            protected TextView mTitle;
            public ViewHolderHead2(View itemView) {
                super(itemView);

                mTitle = (TextView) itemView.findViewById(android.R.id.title);
            }
        }

        class ViewHolderItem extends RecyclerView.ViewHolder {
            protected TextView mScene;
            protected TextView mTitle;
            protected TextView mContent;
            protected TextView mContent2;
            protected LinearLayout mLinearLayout;

            public ViewHolderItem(View itemView) {
                super(itemView);

                mTitle = (TextView) itemView.findViewById(R.id.textView);
                mScene = (TextView) itemView.findViewById(android.R.id.title);
                mContent = (TextView) itemView.findViewById(R.id.text_content);
                mContent2 = (TextView) itemView.findViewById(R.id.text_content2);
                mLinearLayout = (LinearLayout) itemView.findViewById(R.id.content_container);
            }
        }

        class Voice {
            String type;
            ShipVoice voice;

            public Voice(String type, ShipVoice voice) {
                this.type = type;
                this.voice = voice;
            }
        }

        private static final int TYPE_HEAD = 0;
        private static final int TYPE_HEAD2 = 1;
        private static final int TYPE_ITEM = 2;

        private List<Voice> mData;

        public Adapter() {
            mData = new ArrayList<>();
        }

        public void setData(List<ShipVoice> data) {
            mData.clear();

            if (data == null) {
                return;
            }

            for (ShipVoice voice : data) {
                mData.add(new Voice(""/*voice.getZh()*/, voice));
            }

            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0:
                    return TYPE_HEAD;
                case 1:
                    return TYPE_HEAD2;
                default:
                    return TYPE_ITEM;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_HEAD:
                    LinearLayout linearLayout = new LinearLayout(parent.getContext());
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    return new ViewHolderHead(linearLayout);
                case TYPE_HEAD2:
                    return new ViewHolderHead2(
                            LayoutInflater.from(parent.getContext()).inflate(R.layout.content_item_display_cell, parent, false));
                default:
                    return new ViewHolderItem(
                            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ship_voice, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case TYPE_HEAD:
                    setView((LinearLayout) holder.itemView);
                    break;
                case TYPE_HEAD2:
                    ((ViewHolderHead2) holder).mTitle.getLayoutParams().height = Utils.dpToPx(48);
                    ((ViewHolderHead2) holder).mTitle.setText(R.string.voice);
                    break;
                default:
                    final Voice item = mData.get(position - 2);

                        /*if (position == 2 || !mData.get(position - 3).type.equals(item.type)) {
                            ((ViewHolderItem) holder).mTitle.setVisibility(View.VISIBLE);
                            ((ViewHolderItem) holder).mTitle.setText(item.type);
                        } else {*/
                    //((ViewHolderItem) holder).mTitle.setVisibility(View.GONE);
                    //}

                        /*if (item.voice.getScene().length() == 0) {
                            ((ViewHolderItem) holder).mScene.setVisibility(View.GONE);
                        } else {
                            ((ViewHolderItem) holder).mScene.setVisibility(View.VISIBLE);
                        }*/

                    ((ViewHolderItem) holder).mScene.setText(item.voice.getScene());
                    ((ViewHolderItem) holder).mContent.setText(item.voice.getJp());
                    ((ViewHolderItem) holder).mContent2.setText(item.voice.getZh());

                    if (item.voice.getVoiceId() == 22) {
                        setTextViewMode((ViewHolderItem) holder, true);
                    }

                    ((ViewHolderItem) holder).mLinearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (((ViewHolderItem) holder).mContent.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                                setTextViewMode((ViewHolderItem) holder, false);
                            } else {
                                try {
                                    final String url = item.voice.getUrl();
                                    Log.d("VoicePlay", "url " + url);
                                    MusicPlayer.play(url);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    break;
            }
        }

        private void setTextViewMode(ViewHolderItem holder, boolean hide) {
            if (hide) {
                holder.mContent.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                holder.mContent2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

                holder.mContent.setTransformationMethod(MyPasswordTransformationMethod.getInstance());
                holder.mContent2.setTransformationMethod(MyPasswordTransformationMethod.getInstance());
            } else {
                holder.mContent.setInputType(InputType.TYPE_NULL);
                holder.mContent2.setInputType(InputType.TYPE_NULL);

                holder.mContent.setTransformationMethod(null);
                holder.mContent2.setTransformationMethod(null);
            }
        }

        @Override
        public int getItemCount() {
            return mData.size() == 0 ? 1 : mData.size() + 2;
        }
    }


    @Override
    protected void onStop() {
        MusicPlayer.stop();

        super.onStop();
    }

    @Override
    protected String getTaskDescriptionLabel() {
        return mItem.getName().get(this);
    }

    private void setViews() {
        setToolBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolbar.findViewById(R.id.content_container).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                PopupMenu popupMenu = new PopupMenu(ShipDisplayActivity.this, v);

                Ship cur = mItem;
                while (cur.getRemodel().getId_from() != 0) {
                    cur = ShipList.findItemById(v.getContext(), cur.getRemodel().getId_from());
                }
                popupMenu.getMenu().add(0, cur.getId(), 0, cur.getName().get(v.getContext()));

                int i = 1;
                while (cur.getRemodel().getId_to() != 0 &&
                        cur.getRemodel().getId_to() != cur.getRemodel().getId_from()) {
                    cur = ShipList.findItemById(v.getContext(), cur.getRemodel().getId_to());
                    popupMenu.getMenu().add(0, cur.getId(), i, cur.getName().get(v.getContext()));
                    i++;
                }

                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        setItem(item.getItemId(), true);
                        mX = Utils.dpToPx(48 + 4) + v.getWidth() / 2;
                        mY = mAppBarLayout.getHeight() / 2 + Utils.dpToPx(24);
                        return false;
                    }
                });

                if (popupMenu.getDragToOpenListener() instanceof ListPopupWindow.ForwardingListener) {
                    ListPopupWindow.ForwardingListener listener = (ListPopupWindow.ForwardingListener) popupMenu.getDragToOpenListener();
                    listener.getPopup().setVerticalOffset(-v.getHeight());
                    listener.getPopup().setHorizontalOffset(Utils.dpToPx(2));
                    listener.getPopup().show();
                }
            }
        });
    }

    private void setView(LinearLayout linearLayout) {
        if (linearLayout.getChildCount() > 0) {
            return;
        }

        TabLayout tabLayout = new TabLayout(this);
        linearLayout.addView(tabLayout);

        ViewPager viewPager = (ViewPager) LayoutInflater.from(this).inflate(R.layout.content_viewpager, linearLayout, true).findViewById(R.id.view_pager);
        //ViewPager viewPager = new ViewPager(this);
        viewPager.setPadding(0, Utils.dpToPx(4), 0, 0);
        viewPager.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(32) * 6 + Utils.dpToPx(16)));
        ViewPagerStateAdapter adapter = new ViewPagerStateAdapter(getSupportFragmentManager()) {
            @Override
            public Bundle getArgs(int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("TYPE", position);
                bundle.putInt("ITEM", mId);
                return bundle;
            }
        };
        adapter.addFragment(AttrFragment.class, getString(R.string.initial));
        adapter.addFragment(AttrFragment.class, "Lv.99");
        adapter.addFragment(AttrFragment.class, "LV.155");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.colorPrimaryItemActivity));

        addEquip(linearLayout);
        addRemodel(linearLayout);
        addIllustration(linearLayout);
    }

    private void addIllustration(ViewGroup parent) {
        parent = addCell(parent, R.string.illustration);

        ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.ship_illustrations_container, parent);
        LinearLayout container = (LinearLayout) view.findViewById(R.id.content_container);

        final List<String> urlList = new ArrayList<>();

        if (mItem.getWiki_id().equals("030a")) {
            urlList.add(Utils.getKCWikiFileUrl(String.format("KanMusu%sIllust.png", mItem.getWiki_id())));
            urlList.add(Utils.getKCWikiFileUrl(String.format("KanMusu%sDmgIllust.png", mItem.getWiki_id())));

        } else {
            urlList.add(Utils.getKCWikiFileUrl(String.format("KanMusu%sIllust.png", mItem.getWiki_id().replace("a", ""))));
            urlList.add(Utils.getKCWikiFileUrl(String.format("KanMusu%sDmgIllust.png", mItem.getWiki_id().replace("a", ""))));
        }


        ExtraIllustration extraIllustration = ExtraIllustrationList.findItemById(this, mItem.getWiki_id());
        if (extraIllustration != null) {
            for (String name :
                    extraIllustration.getImage()) {
                urlList.add(Utils.getKCWikiFileUrl(name));
            }
        }

        for (int i = 0; i < urlList.size(); i++) {
            String url = urlList.get(i);

            Log.d(getClass().getSimpleName(), url);

            ImageView imageView = (ImageView) LayoutInflater.from(this)
                    .inflate(R.layout.ship_illustrations, container, false)
                    .findViewById(R.id.imageView);
            container.addView(imageView);

            final int finalI = i;
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageDisplayActivity.start(v.getContext(), urlList, finalI, getTaskDescriptionLabel());
                }
            });

            /*if (Settings.instance(this).getBoolean(Settings.DOWNLOAD_WIFI_ONLY, false)) {
                Glide.with(this)
                        .using(GlideHelper.cacheOnlyStreamLoader)
                        .load(url)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .crossFade()
                        .into(imageView);
            } else */{
                Glide.with(this)
                        .load(Utils.getGlideUrl(url))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade()
                        .into(imageView);
            }

        }
    }

    private void addRemodel(ViewGroup parent) {
        if (mItem.getRemodel() != null) {
            parent = addCell(parent, R.string.remodel);
            final GridLayout gridLayout = new GridLayout(this);
            gridLayout.setColumnCount(2);

            //StringBuilder sb = new StringBuilder();
            Ship cur = mItem;
            while (cur.getRemodel().getId_from() != 0) {
                cur = ShipList.findItemById(this, cur.getRemodel().getId_from());
            }

            while (true) {
                StringBuilder sb = new StringBuilder();
                sb.append(cur.getName().get(this));
                ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.ship_remodel_item, gridLayout, false);
                view.setLayoutParams(
                        new GridLayout.LayoutParams(
                                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                                GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        )
                );
                gridLayout.addView(view);

                final Ship finalCur = cur;

                view.setOnTouchListener(this);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < gridLayout.getChildCount(); i++) {
                            View view = gridLayout.getChildAt(i);

                            if (view.findViewById(android.R.id.title) != null) {
                                ((TextView) view.findViewById(android.R.id.title)).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                            }
                        }

                        ((TextView) v.findViewById(android.R.id.title)).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                        setItem(finalCur.getId(), true);
                    }
                });

                if (cur.getRemodel().getId_from() != 0) {
                    Ship prev = ShipList.findItemById(this, cur.getRemodel().getId_from());
                    if (prev.getRemodel().getBlueprint() == 0) {
                        sb.append(String.format(" (%d)", prev.getRemodel().getLevel()));
                    } else {
                        sb.append(String.format(" (%d + 改装设计图)", prev.getRemodel().getLevel()));
                    }
                }

                ((TextView) view.findViewById(android.R.id.title)).setText(sb.toString());

                if (mItem == cur) {
                    ((TextView) view.findViewById(android.R.id.title)).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                }

                if (cur.getRemodel().getId_to() == 0 ||
                        cur.getRemodel().getId_from() == cur.getRemodel().getId_to()) {

                    view.findViewById(R.id.imageView).setVisibility(View.INVISIBLE);
                    break;
                }

                cur = ShipList.findItemById(this, cur.getRemodel().getId_to());
                if (cur.getRemodel().getId_from() != cur.getRemodel().getId_to()) {
                    //sb.append(" → ");
                } else {
                    //sb.append(" ↔ ");
                    ((ImageView) view.findViewById(R.id.imageView)).setImageResource(R.drawable.ic_compare_arrows_24dp);
                }


            }
            //addTextView(parent, Html.fromHtml(sb.toString()));
            parent.addView(gridLayout);
        }
    }

    private void addEquip(ViewGroup parent) {
        parent = addCell(parent, R.string.equip_and_load);

        List<Integer> equipId = mItem.getEquip().get(0);
        List<Integer> equipSlot = mItem.getEquip().get(1);

        for (int i = 0; i < mItem.getSlot(); i++) {
            ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.ship_equip, parent, false);

            if (equipId.get(i) > 0) {
                final Equip item = EquipList.findItemById(this, equipId.get(i));
                if (item == null) {
                    ((TextView) view.findViewById(android.R.id.title)).setText(String.format(getString(R.string.equip_not_found), equipId.get(i)));
                    view.findViewById(android.R.id.title).setEnabled(false);
                } else {
                    ((TextView) view.findViewById(android.R.id.title)).setText(item.getName().get(this));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ShipDisplayActivity.this, EquipDisplayActivity.class);
                            intent.putExtra(EquipDisplayActivity.EXTRA_ITEM_ID, item.getId());
                            startActivity(intent);
                        }
                    });
                }
            } else {
                ((TextView) view.findViewById(android.R.id.title)).setText(getString(R.string.not_equipped));
                view.findViewById(android.R.id.title).setEnabled(false);
            }

            ((TextView) view.findViewById(R.id.textView)).setText(Integer.toString(equipSlot.get(i)));

            parent.addView(view);
        }
    }

    private ViewGroup addCell(ViewGroup parent, String title) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.content_item_display_cell, parent, false);
        ((TextView) view.findViewById(android.R.id.title)).setText(title);
        parent.addView(view);
        return view;
    }

    private ViewGroup addCell(ViewGroup parent, int ResId) {
        return addCell(parent, getString(ResId));
    }

    private TextView addTextView(ViewGroup parent, String text, int size) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        parent.addView(textView);
        return textView;
    }

    private TextView addTextView(ViewGroup parent, String text) {
        return addTextView(parent, text, 16);
    }

    private TextView addTextView(ViewGroup parent, Spanned text, int size) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        textView.setMovementMethod(new LinkMovementMethod());
        textView.setClickable(true);
        textView.setSpannableFactory(MySpannableFactory.getInstance());
        //textView.setLinkTextColor(getColor(R.color.material_red_300));
        parent.addView(textView);
        return textView;
    }

    private TextView addTextView(ViewGroup parent, Spanned text) {
        return addTextView(parent, text, 16);
    }

    Toast mToast;

    @SuppressLint("DefaultLocale")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_bookmark:
                mItem.setBookmarked(!mItem.isBookmarked());

                Settings.instance(this)
                        .putBoolean(String.format("ship_%d_%d", mItem.getCtype(), mItem.getCnum()), mItem.isBookmarked());

                if (mToast != null) {
                    mToast.cancel();
                }

                mToast = Toast.makeText(this, mItem.isBookmarked() ? getString(R.string.bookmark_add) : getString(R.string.bookmark_remove), Toast.LENGTH_SHORT);
                mToast.show();

                item.setIcon(
                        AppCompatDrawableManager.get().getDrawable(this, mItem.isBookmarked() ? R.drawable.ic_bookmark_24dp : R.drawable.ic_bookmark_border_24dp));

                break;
            case R.id.action_feedback:
                SendReportActivity.sendEmail(this,
                        "Akashi Toolkit 舰娘数据反馈",
                        String.format("应用版本: %d\n舰娘名称: %s\n\n请写下您的建议或是指出错误的地方。\n\n",
                                StaticData.instance(this).versionCode,
                                getTaskDescriptionLabel()));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected ViewGroup getRootView() {
        return mCoordinatorLayout;
    }

    @Override
    protected View[] getAnimFadeViews() {
        return new View[] {
                mAppBarLayout,
                mRecyclerView
        };
    }

    public static class AttrFragment extends Fragment {
        private Ship mItem;
        private LinearLayout mLinearLayout;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            int i = args.getInt("TYPE");
            int id = args.getInt("ITEM");

            mLinearLayout = new LinearLayout(getContext());
            mLinearLayout.setOrientation(LinearLayout.VERTICAL);
            mLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mLinearLayout.setPadding(0, Utils.dpToPx(4), 0, 0);
            mItem = ShipList.findItemById(getContext(), id);
            addAttrView(mLinearLayout, "耐久", mItem.getAttr().getHp(), i == 2 ? 2 : 0, R.drawable.item_attr_hp);
            addAttrView(mLinearLayout, "火力", mItem.getAttr().getFire(), i, R.drawable.item_attr_fire);
            addAttrView(mLinearLayout, "对空", mItem.getAttr().getAa(), i, R.drawable.item_attr_aa);
            addAttrView(mLinearLayout, "雷装", mItem.getAttr().getTorpedo(), i, R.drawable.item_attr_torpedo);
            addAttrView(mLinearLayout, "装甲", mItem.getAttr().getArmor(), i, R.drawable.item_attr_armor);
            addAttrView(mLinearLayout, "对潜", mItem.getAttr().getAsw(), i, R.drawable.item_attr_asw);
            addAttrView(mLinearLayout, "回避", mItem.getAttr().getEvasion(), i, R.drawable.item_attr_dodge);
            addAttrView(mLinearLayout, "索敌", mItem.getAttr().getSearch(), i, R.drawable.item_attr_search);
            addAttrView(mLinearLayout, "航速", mItem.getAttr().getSpeed(), R.drawable.item_attr_speed);
            addAttrView(mLinearLayout, "射程", mItem.getAttr().getRange(), R.drawable.item_attr_range);
            addAttrView(mLinearLayout, "运", mItem.getAttr().getLuck(), i, R.drawable.item_attr_luck);
            /*attr = 0;
            mCurAttrLinearLayout = null;
            addAttrView(mLinearLayout, "燃料消耗", mItem.getRemodel().getCost().get(0), 0);
            addAttrView(mLinearLayout, "弹药消耗", mItem.getRemodel().getCost().get(1), 0);*/

            return mLinearLayout;
        }

        private void addAttrView(ViewGroup parent, String title, List<Integer> value, int i, int icon) {
            if (value.size() <= i) {
                i = value.size() - 1;
            }

            if (value.size() == 2) {
                addAttrView(parent, title, String.format("%d / %d", value.get(0), value.get(1)), icon);
            } else {
                addAttrView(parent, title, Integer.toString(value.get(i)), icon);
            }
        }

        private void addAttrView(ViewGroup parent, String title, int value, int icon) {
            if (value == 0) {
                return;
            }
            if (icon == R.drawable.item_attr_range) {
                addAttrView(parent, title, KCStringFormatter.getRange(value), icon);
            } else if (icon == R.drawable.item_attr_speed) {
                addAttrView(parent, title, KCStringFormatter.getSpeed(value), icon);
            } else {
                addAttrView(parent, title, Integer.toString(value), icon);
            }
        }

        private LinearLayout mCurAttrLinearLayout;
        private int attr = 0;

        private void addAttrView(ViewGroup parent, String title, String value, int icon) {
            attr ++;

            if (mCurAttrLinearLayout == null) {
                mCurAttrLinearLayout = new LinearLayout(getContext());
                mCurAttrLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                mCurAttrLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(32)));
                mCurAttrLinearLayout.setBaselineAligned(false);
                mCurAttrLinearLayout.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.item_attr_cell, mCurAttrLinearLayout);
                parent.addView(mCurAttrLinearLayout);
            }

            View cell = mCurAttrLinearLayout
                    .findViewById((attr % 2 == 0) ? R.id.item_attr_cell2 : R.id.item_attr_cell);

            cell.setVisibility(View.VISIBLE);

            ((TextView) cell.findViewById(R.id.textView)).setText(title);
            ((TextView) cell.findViewById(R.id.textView2)).setText(value);
            //((ImageView) cell.findViewById(R.id.imageView)).setImageDrawable(ContextCompat.getDrawable(getContext(), icon));

            if (attr % 2 == 0) {
                mCurAttrLinearLayout = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Animator animateRevealColorFromCoordinates(ViewGroup viewRoot, @ColorRes int color, int x, int y, boolean expand) {
        float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim;
        if (expand) {
            anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0, finalRadius);
        } else {
            anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, finalRadius, 0);
        }

        viewRoot.setBackgroundColor(ContextCompat.getColor(this, color));
        anim.setDuration(FADE_OUT/*getResources().getInteger(R.integer.anim_duration_long)*/);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
        return anim;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ship_display, menu);

        menu.findItem(R.id.action_bookmark).setIcon(
                AppCompatDrawableManager.get().getDrawable(this, mItem.isBookmarked() ? R.drawable.ic_bookmark_24dp : R.drawable.ic_bookmark_border_24dp));

        /*Drawable drawable = ContextCompat.getDrawable(this, mItem.isBookmarked() ? R.drawable.ic_bookmark_24dp : R.drawable.ic_bookmark_border_24dp);
        drawable.setColorFilter(ContextCompat.getColor(this, R.color.itemDisplayTitle), PorterDuff.Mode.SRC_ATOP);

        menu.findItem(R.id.action_bookmark).setIcon(drawable);*/

        return true;
    }
}

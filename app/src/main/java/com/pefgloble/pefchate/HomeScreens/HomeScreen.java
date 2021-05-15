 package com.pefgloble.pefchate.HomeScreens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.HomeScreens.Adopters.HomePagerAdopter;
import com.pefgloble.pefchate.JsonClasses.status.NewStatus;
import com.pefgloble.pefchate.JsonClasses.status.StatusResponse;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.APIContact;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.fragments.ContactsFragment;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.ContactsChangeObserver;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.jobs.WorkJobsManager;
import com.pefgloble.pefchate.presenter.ContactsPresenter;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

 public class HomeScreen extends AppCompatActivity {

     MeowBottomNavigation bottomNavigation;
     @BindView(R.id.app_bar)
     Toolbar toolbar;

    final Context context = this;
    FrameLayout home_frame;
    ContactsChangeObserver observer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //------------------------------------ custom Toolbar --------------------------------------


        JSONObject json = new JSONObject();
        try {
            json.put("connected", true);
            json.put("senderId", PreferenceManager.getInstance().getID(getApplicationContext()));
        } catch (JSONException e) {
            e.printStackTrace();
        }


                String DatabaseName = "PEFChat" + PreferenceManager.getInstance().getToken(getApplicationContext()) + "_db" + ".realm";
                Realm.init(getApplicationContext());
                RealmConfiguration configuration = new RealmConfiguration.Builder()
                        .name(DatabaseName)
                        .schemaVersion(AppConstants.DatabaseVersion)
                        // .migration(new RealmMigrations())
                        .deleteRealmIfMigrationNeeded()
                        .build();
                Realm.setDefaultConfiguration(configuration);
                bottom_bar();

                if (AppHelper.isOnline(getApplicationContext())){
                SocketConnectionManager.getInstance().checkSocketConnection();
                }
        if( ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 123);
        }
        else {
        observer = new ContactsChangeObserver(null, this);
        getApplicationContext().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, observer);
        }

        //WorkJobsManager.getInstance().initializerApplicationService();

         WorkJobsManager.getInstance().syncingContactsWithServerWorkerInit();
         WorkJobsManager.getInstance().sendUserMessagesToServer();
         WorkJobsManager.getInstance().sendUserStoriesToServer();
         WorkJobsManager.getInstance().sendDeliveredStatusToServer();
         WorkJobsManager.getInstance().sendDeliveredGroupStatusToServer();
         WorkJobsManager.getInstance().sendDeletedStoryToServer();
            }

            private void bottom_bar() {

                final ViewPager viewPager = findViewById(R.id.vp_horizontal_ntb);
                HomePagerAdopter adopter = new HomePagerAdopter(getSupportFragmentManager());
                viewPager.setAdapter(adopter);

             //   final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
                bottomNavigation = findViewById(R.id.meowBar);
                final ArrayList<MeowBottomNavigation.Model> models = new ArrayList<>();

                if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.M){
                    bottomNavigation.add(new MeowBottomNavigation.Model(1,R.drawable.newfriend));
                    bottomNavigation.add(new MeowBottomNavigation.Model(2,R.drawable.ic_contacts_white_24dp));
                    bottomNavigation.add(new MeowBottomNavigation.Model(3,R.drawable.newcamera));
                    bottomNavigation.add(new MeowBottomNavigation.Model(4,R.drawable.newstory));
                    bottomNavigation.add(new MeowBottomNavigation.Model(5,R.drawable.icon_setting));
                }
                else {
                    bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.icon_message));
                    bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.icon_friends));
                    bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.icon_camera));
                    bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.icon_call));
                    bottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.icon_setting));
                }
//                bottomNavigation.show(3,true);


                bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
                    @Override
                    public void onShowItem(MeowBottomNavigation.Model item) {
                        switch (item.getId()) {
                            case 1:
                                viewPager.setCurrentItem(0);
                                break;
                            case 2:
                                viewPager.setCurrentItem(1);
                                break;
                            case 3:
                                viewPager.setCurrentItem(2);
                                break;
                            case 4:
                                viewPager.setCurrentItem(3);
                                break;
                            case 5:
                                viewPager.setCurrentItem(4);
                                break;
                        }
                    }
                });
                bottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
                    @Override
                    public void onClickItem(MeowBottomNavigation.Model item) {
                        switch (item.getId()) {
                            case 1:
                                viewPager.setCurrentItem(0);
                                break;
                            case 2:
                                viewPager.setCurrentItem(1);
                                break;
                            case 3:
                                viewPager.setCurrentItem(2);
                                break;
                            case 4:
                                viewPager.setCurrentItem(3);
                                break;
                            case 5:
                                viewPager.setCurrentItem(4);
                                break;
                        }
                    }
                });

                viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        int id=bottomNavigation.getModels().get(position).getId();
                        switch (id){
                            case 1:
                                bottomNavigation.show(1,true);
                                break;
                            case 2:
                                bottomNavigation.show(2,true);
                                break;
                            case 3:
                                bottomNavigation.show(3,true);
                                break;
                            case 4:
                                bottomNavigation.show(4,true);
                                break;
                            case 5:
                                bottomNavigation.show(5,true);
                                break;
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
/*                final ArrayList<MeowBottomNavigation.Model> models = new ArrayList<>();

                models.add(
                        new MeowBottomNavigation.Model.Builder(
                                getResources().getDrawable(R.drawable.icon_message),
                                R.color.theme_green)
                                .selectedIcon(getResources().getDrawable(R.drawable.icon_message))
                                .title("Chat")
                                .build()
                );
                models.add(
                        new MeowBottomNavigation.Model.Builder(
                                getResources().getDrawable(R.drawable.icon_call),
                                0)
                                .selectedIcon(getResources().getDrawable(R.drawable.icon_call))
                                .title("Feed")
                                .badgeTitle("5")
                                .build()
                );
                models.add(
                        new MeowBottomNavigation.Model.Builder(
                                getResources().getDrawable(R.drawable.icon_camera),
                                0)
                                .selectedIcon(getResources().getDrawable(R.drawable.icon_camera))
                                .title("Camera")
                                .badgeTitle("")
                                .build()
                );
                models.add(
                        new MeowBottomNavigation.Model.Builder(
                                getResources().getDrawable(R.drawable.icon_friends),
                                0)
                                .selectedIcon(getResources().getDrawable(R.drawable.icon_friends))
                                .title("Friends")
                                .badgeTitle("icon")
                                .build()
                );
                models.add(
                        new MeowBottomNavigation.Model.Builder(
                                getResources().getDrawable(R.drawable.icon_setting),
                                0)
                                .selectedIcon(getResources().getDrawable(R.drawable.icon_setting))
                                .title("Setting")
                                .badgeTitle("")
                                .build()
                );*/


                //bottomNavigation.setModels(models);

              //  navigationTabBar.setViewPager(viewPager, 2);
               /* navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(final int position) {
                        navigationTabBar.getModels().get(position).hideBadge();
                    }

                    @Override
                    public void onPageScrollStateChanged(final int state) {

                    }
                });

                navigationTabBar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                            final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                            navigationTabBar.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    model.showBadge();
                                }
                            }, i * 100);
                        }
                    }
                }, 500);*/

            }

     @Override
     protected void onDestroy() {
         super.onDestroy();
         EventBus.getDefault().unregister(this);

         if (!WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance()).isClosed()) {
             WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance()).close();
         }
     }
     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         if (requestCode==123 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
             observer = new ContactsChangeObserver(null, this);
             getApplicationContext().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, observer);
         }
     }
 }



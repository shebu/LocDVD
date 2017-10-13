package com.exemple.locdvd;

/**
 * Développez une application Android - Sylvain Hébuterne - 2017 Edition ENI.
 */


import android.app.Application;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity implements
        ListDVDFragment.OnDVDSelectedListener {

    DrawerLayout drawerLayout;
    private static final String TAG_FRAGMENT_LISTDVD="FragementListDVD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout)findViewById(R.id.main_Drawer);

        ListView listDrawer =
                (ListView)findViewById(R.id.main_DrawerList);
        String [] drawerItems =
                getResources().getStringArray(R.array.drawer_Items);
        listDrawer.setAdapter(new ArrayAdapter<String>(this,
                R.layout.listitem_drawer, drawerItems));

        listDrawer.setOnItemClickListener(new
              ListView.OnItemClickListener() {
                  @Override
                  public void onItemClick(AdapterView<?> parent, View v, int pos,
                                          long id) {
                      if(pos==0) {
                          Intent intent = new Intent(MainActivity.this,
                                  MainActivity.class);
                          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                          startActivity(intent);
                      }
                      if(pos==1)
                          startActivity(new Intent(MainActivity.this,
                                  AddDVDActivity.class));
                      if(pos==2) {
                          SearchFragment searchFragment = new SearchFragment();
                          openDetailFragment(searchFragment);
                      }
                      drawerLayout.closeDrawer(android.view.Gravity.START);
                  }
              });

        SharedPreferences sharedPreferences =
                getSharedPreferences("com.exemple.locDVD.prefs",Context.MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("embeddedDataInserted", false))
            readEmbeddedData();

        ListDVDFragment listDVDFragment = new ListDVDFragment();
        openFragment(listDVDFragment, TAG_FRAGMENT_LISTDVD);
        Toast.makeText(this, "Vous executez la version :"  + BuildConfig.FLAVOR, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onResume()  {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater  = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_partager);
        ShareActionProvider shareActionProvider =
                (ShareActionProvider)
                        MenuItemCompat.getActionProvider(shareItem);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Mon Application");
        intent.putExtra(Intent.EXTRA_TEXT,"J'aime mon application LocDVD !!!");
        shareActionProvider.setShareIntent(intent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reinitialiser:
                // l'entrée Réinitialiser la base a été sélectionnée
                ensureReInitializeApp();
                return true;
            case R.id.menu_informations:
                // l'entrée Informations a été sélectionnée
                showInformations();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDVDSelected(long dvdId) {
        startViewDVDActivity(dvdId);
    }

    private void startViewDVDActivity(long dvdId) {
        ViewDVDFragment viewDVDFragment = new ViewDVDFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("dvdId",dvdId);
        viewDVDFragment.setArguments(bundle);
        openDetailFragment(viewDVDFragment);
    }

    private  void openFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_placeHolder, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openDetailFragment(Fragment fragment)  {
        FragmentManager fragmentManager =
                getSupportFragmentManager();

        FragmentTransaction transaction =
                fragmentManager.beginTransaction();

        if(findViewById(R.id.detail_placeHolder)==null)
            transaction.replace(R.id.main_placeHolder, fragment);
        else {
            transaction.setCustomAnimations(R.anim.anim_in, R.anim.anim_out);
            transaction.replace(R.id.detail_placeHolder, fragment);
        }

        transaction.addToBackStack(null);
        transaction.commit();
    }


    private void readEmbeddedData() {
        AsyncReadEmbeddedData asyncReadEmbeddedData =
                new AsyncReadEmbeddedData();
        asyncReadEmbeddedData.execute("data.txt");
    }


    private void ensureReInitializeApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmer_reinitialisation_title);
        builder.setMessage(R.string.confirmer_reinitialisation_message);
        builder.setNegativeButton(R.string.non, null);
        builder.setPositiveButton(R.string.oui,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalSQLiteOpenHelper.deleteDatabase(MainActivity.this);
                        readEmbeddedData();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showInformations()  {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.infos);
        builder.setPositiveButton(R.string.fermer, null);

        LayoutInflater inflater  = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_informations, null);
        TextView message =(TextView)view.findViewById(R.id.dialog_message);
        message.setText(R.string.informations_message);
        message.setMovementMethod(
                new android.text.method.ScrollingMovementMethod());

        builder.setView(view);

        builder.create().show();
    }


    class AsyncReadEmbeddedData extends AsyncTask<String, Integer, Boolean> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()  {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog
                    .setTitle(R.string.initialisation_de_la_base_de_donnees);
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }


        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;
            String dataFile = params[0];
            InputStreamReader reader = null;
            InputStream file=null;
            BufferedReader bufferedReader=null;
            try {
                int counter = 0;
                file = getAssets().open(dataFile);
                reader = new InputStreamReader(file);
                bufferedReader = new BufferedReader(reader);
                String line= null;
                while((line=bufferedReader.readLine())!=null) {
                    String [] data = line.split("\\|");
                    if(data!=null && data.length==4) {
                        DVD dvd = new DVD();
                        dvd.titre = data[0];
                        dvd.annee = Integer.decode(data[1]);
                        dvd.acteurs = data[2].split(",");
                        dvd.resume = data[3];
                        dvd.insert(MainActivity.this);
                        publishProgress(++counter);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }  finally {
                if(bufferedReader!=null) {
                    try {
                        bufferedReader.close();
                        reader.close();
                        SharedPreferences sharedPreferences =
                                getSharedPreferences("com.exemple.locDVD.prefs",
                                        Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor =
                                sharedPreferences.edit();
                        editor.putBoolean("embeddedDataInserted", true);
                        editor.commit();
                        result = true;
                    }  catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setMessage(
                    String.format(getString(R.string.x_dvd_inseres_dans_la_base),
                            values[0]));
        }

        @Override
        protected void onPostExecute(Boolean result)  {
            progressDialog.dismiss();
            FragmentManager fragmentManager =
                    getSupportFragmentManager();
            ListDVDFragment listDVDFragment =
                    (ListDVDFragment)fragmentManager
                            .findFragmentByTag(TAG_FRAGMENT_LISTDVD);
            if(listDVDFragment!=null)
                listDVDFragment.updateDVDList();

        }

    };


    private void startService() {
        BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String receivedMessage = intent.getStringExtra("replyMessage");
                Toast.makeText(MainActivity.this, receivedMessage, Toast.LENGTH_LONG).show();
            }
        };


        Intent intent = new Intent(MainActivity.this, LocDVDIntentService.class);
        intent.putExtra("waitDuration", 3000);

        IntentFilter intentFilter = new IntentFilter("LocDVD.ServiceEnded");
        registerReceiver(myBroadcastReceiver,intentFilter);

        startService(intent);
    }

}

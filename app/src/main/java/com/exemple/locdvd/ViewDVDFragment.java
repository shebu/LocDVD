package com.exemple.locdvd;

/**
 * Développez une application Android - Sylvain Hébuterne - 2017 Edition ENI.
 */

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ViewDVDFragment extends Fragment{


    static final int TAKE_PHOTO  = 2015;

    Button txtTitreDVD;
    TextView txtAnneeDVD;
    TextView txtResumeFilm;
    TextView txtDateDernierVisionnage;
    Button setDateVisionnage;
    Button setNotification;
    LinearLayout layoutActeurs;

    Button takePhoto;
    ImageView photoDVD;

    File savedImage;
    DVD dvd;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup
            container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // affectation du fichier de layout
        View view = inflater.inflate(R.layout.activity_viewdvd,
                null);

        // Obtention des références sur les composants
        txtTitreDVD = view.findViewById(R.id.titreDVD);
        txtAnneeDVD= (TextView)view.findViewById(R.id.anneeDVD);
        txtResumeFilm= (TextView)view.findViewById(R.id.resumeFilm);
        layoutActeurs =
                (LinearLayout)view.findViewById(R.id.layoutActeurs);
        setDateVisionnage =
                (Button)view.findViewById(R.id.setDateVisionnage);
        txtDateDernierVisionnage =
                (TextView)view.findViewById(R.id.dateVisionnage);
        setNotification =
                (Button)view.findViewById(R.id.setNotification);
        takePhoto = (Button)view.findViewById(R.id.takePhoto);
        photoDVD=(ImageView)view.findViewById(R.id.photoDVD);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTakePhoto();
            }
        });


        setNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });

        long dvdId = getArguments().getLong("dvdId",-1);
        dvd = DVD.getDVD(getActivity(), dvdId);


        setDateVisionnage.setOnClickListener(new
                                                     View.OnClickListener() {
                                                         @Override
                                                         public void onClick(View v) {
                                                             showDatePicker();
                                                         }
                                                     });

       Typeface typeface =
                Typeface.createFromAsset(getActivity().getAssets(), "font/roboto_thin.ttf");
        txtTitreDVD.setTypeface(typeface);


        return view;
    }

    private void showDatePicker()  {
        DatePickerDialog datePickerDialog;

        DatePickerDialog.OnDateSetListener onDateSetListener =
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int
                            monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);

                        dvd.setDateVisionnage(calendar.getTimeInMillis());
                        dvd.update(getActivity());

                        SimpleDateFormat simpleDateFormat = new
                                SimpleDateFormat("dd-MM-yyyy");
                        String dateValue =
                                String.format(getString(R.string.date_dernier_visionnage_avec_valeur),
                                        simpleDateFormat.format(calendar.getTime()));
                        txtDateDernierVisionnage.setText( dateValue);
                    }
                };

        Calendar calendar = Calendar.getInstance();
        if(dvd.dateVisionnage>0) {
            calendar.setTimeInMillis(dvd.dateVisionnage);
        }

        datePickerDialog = new
                DatePickerDialog(getActivity(),onDateSetListener,
                calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar
                .get(Calendar.DAY_OF_MONTH));


        datePickerDialog.show();
    }




    @Override
    public void onResume() {
        super.onResume();

        txtTitreDVD.setText(dvd.getTitre());
        txtAnneeDVD.setText(
                String.format(getString(R.string.annee_de_sortie), dvd.getAnnee()));

        for(String acteur : dvd.getActeurs()) {
            TextView textView = new TextView(getActivity());
            textView.setText(acteur);
            layoutActeurs.addView(textView);
        }
        txtResumeFilm.setText(dvd.getResume());
        if(dvd.cheminPhoto!=null)
            photoDVD.setImageURI(Uri.parse(dvd.cheminPhoto));

    }

    String CHANNEL_ID="CHANNEL_LOCDVD";

    private void sendNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getActivity());

        NotificationManager notificationManager  =
                (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);


        builder.setContentTitle(dvd.getTitre());
        builder.setContentText(dvd.getResume());
        builder.setSmallIcon(R.drawable.ic_notification);

       if(Build.VERSION.SDK_INT>25) {

           if(notificationManager.getNotificationChannel(CHANNEL_ID)==null) {
               Log.d("Notification","Création du channel de notification");
               NotificationChannel notificationChannel =
                       new NotificationChannel(CHANNEL_ID, "Notifications LocDVD", NotificationManager.IMPORTANCE_HIGH);
               notificationChannel.setDescription("Canal de notifications de l'application LocDVD.");
               notificationChannel.enableLights(true);
               notificationChannel.enableVibration(true);
               notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200, 200, 100, 200, 100, 400});
               notificationManager.createNotificationChannel(notificationChannel);
           } else {
               Log.d("Notification","Channel déjà créé, pas la peine d'en faire un autre");
           }
           builder.setChannelId(CHANNEL_ID);
       }

        Intent intent = new Intent(getActivity(), MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getActivity(), 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        Notification notification;
        if(Build.VERSION.SDK_INT<16)
            notification = builder.getNotification();
        else
            notification = builder.build();

        notificationManager.notify((int)dvd.id, notification);
    }

    private void initTakePhoto()  {
        savedImage =
                new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "dvd_" + dvd.id + ".jpg");

        Intent intent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(savedImage));
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (requestCode == TAKE_PHOTO) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(getActivity(),
                        "La photo n'a pas été prise",
                        Toast.LENGTH_LONG).show();
                return;
            } else {
                if(savedImage!=null) {
                    dvd.cheminPhoto = savedImage.getAbsolutePath();
                    dvd.update(getActivity());
                    photoDVD.setImageURI(Uri.fromFile(savedImage));
                }
            }
        }
    }

}

package com.exemple.locdvd;

/**
 * Développez une application Android - Sylvain Hébuterne - 2017 Edition ENI.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class DVDAdapter extends ArrayAdapter<DVD> {

    Context context;
    public DVDAdapter(Context context, List<DVD> objects) {
        super(context, -1, objects);
        this.context = context;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup
            parent) {
        View view=null;
        if(convertView==null) {
            LayoutInflater layoutInflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.listitem_dvd,
                    null);
        } else {
            view = convertView;
        }

        DVD dvd = getItem(pos);
        // la référence au DVD courant est stockée dans la vue courante.
        view.setTag(dvd);

        TextView titre
                =(TextView)view.findViewById(R.id.listItemDVD_titre);
        TextView annee
                =(TextView)view.findViewById(R.id.listItemDVD_annee);
        TextView resume
                =(TextView)view.findViewById(R.id.listItemDVD_resume);

        titre.setText(dvd.getTitre());
        annee.setText(String.valueOf(dvd.getAnnee()));
        resume.setText(dvd.getResume());

        return view;
    }

}

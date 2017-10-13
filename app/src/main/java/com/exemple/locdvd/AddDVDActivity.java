package com.exemple.locdvd;

/**
 * Développez une application Android - Sylvain Hébuterne - 2017 Edition ENI.
 */

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class AddDVDActivity extends Activity{

    EditText editTitreFilm;
    EditText editAnnee;
    EditText editResume;
    Button btnAddActeur;
    Button btnOk;
    LinearLayout addActeursLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);

        // affectation du fichier de layout
        setContentView(R.layout.activity_adddvd);

        // Obtention des références sur les composants
        editTitreFilm = (EditText)findViewById(R.id.addDVD_titre);
        editAnnee= (EditText)findViewById(R.id.addDVD_annee);
        editResume= (EditText)findViewById(R.id.addDVD_resume);
        btnAddActeur = (Button)findViewById(R.id.addDVD_addActeur);
        btnOk = (Button)findViewById(R.id.addDVD_ok);

        addActeursLayout =
                (LinearLayout)findViewById(R.id.addDVD_addActeurLayout);

        btnAddActeur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addActeur(null);
            }
        });

        // Est-ce une recréation suite à une rotation de l'écran ?
        if(savedInstanceState!=null) {
            String [] acteurs
                    =savedInstanceState.getStringArray("acteurs");
            for(String s : acteurs) {
                addActeur(s);
            }
        }

        else {
            // Aucun acteur saisi, on affiche un composant editText vide
            addActeur(null);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        String[] acteurs = new String[addActeursLayout.getChildCount()];
        for(int i=0;i<addActeursLayout.getChildCount();i++) {
            View child = addActeursLayout.getChildAt(i);
            if(child instanceof EditText)
                acteurs[i] = ((EditText)child).getText().toString();
        }
        savedInstanceState.putStringArray("acteurs",acteurs);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void addActeur(String content) {
        EditText editNewActeur = new EditText(this);
        editNewActeur.
                setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                        | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        if(content!=null)
            editNewActeur.setText(content);

        addActeursLayout.addView(editNewActeur);
    }
}


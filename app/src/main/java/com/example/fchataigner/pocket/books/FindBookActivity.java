package com.example.fchataigner.pocket.books;

import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.fchataigner.pocket.FindtemActivity;
import com.example.fchataigner.pocket.Language;
import com.example.fchataigner.pocket.R;

import java.util.ArrayList;

public class FindBookActivity extends FindtemActivity<Book>
{
    private BookFinder book_finder=null;

    @Override
    public int getListItemLayout() { return R.layout.book_item; }

    @Override
    public int getActivityLayout() { return R.layout.add_book_activity; }

    @Override
    public String getQueryHints() { return "book title, author"; }

    @Override
    public @NonNull
    ListView getListView() { return findViewById(R.id.results_list); }

    @Override
    public @NonNull
    SearchView getSearchView() { return findViewById(R.id.search_view); }

    @Override
    public @NonNull
    TextView getInfoView() { return findViewById(R.id.info_text); }

    @Override
    public @NonNull
    ProgressBar getProgressBar() { return findViewById(R.id.progress_bar); }

    @Override
    public void setupSearchInterface()
    {
        ArrayList<Language> languages = new ArrayList<>();
        languages.add( Language.English );
        languages.add( Language.Spanish );
        languages.add( Language.French );

        Spinner language_spinner = findViewById(R.id.language_spinner);
        language_spinner.setAdapter( new ArrayAdapter<Language>(
                this, R.layout.spinner_item, R.id.spinner_text, languages ) );

        ArrayList<String> types = new ArrayList<>();
        for ( String type : this.getResources().getStringArray(R.array.book_types) ) types.add(type);

        Spinner type_spinner = findViewById(R.id.type_spinner);
        type_spinner.setAdapter( new ArrayAdapter<>(
                this, R.layout.spinner_item, R.id.spinner_text, types ) );
    }

    @Override
    public boolean startSearch( String query )
    {
        Spinner language_spinner = findViewById(R.id.language_spinner);
        Language language = (Language) language_spinner.getSelectedItem();

        Spinner type_spinner = findViewById(R.id.type_spinner);
        String type = (String) type_spinner.getSelectedItem();

        if ( book_finder != null ) book_finder.cancel(true);
        book_finder = new BookFinder( this, language, type, this );
        book_finder.execute( query.split(QUERY_DELIMITER) );

        return true;
    }

    @Override
    public void stopSearch()
    {
        if ( book_finder != null ) book_finder.cancel(true);
    }
}
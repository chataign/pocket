package com.example.fchataigner.pocket;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.example.fchataigner.pocket.books.Book;
import com.example.fchataigner.pocket.books.BookListFragment;
import com.example.fchataigner.pocket.places.Place;
import com.example.fchataigner.pocket.places.PlaceListFragment;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener
{
    private boolean loadFragment( int item_id )
    {
        Fragment fragment = null;
        String title = "";

        switch (item_id) {
            case R.id.item_book:
                title = getString(R.string.category_books);
                fragment = new BookListFragment();
                break;
            case R.id.item_food:
                title = getString(R.string.item_food);
                fragment = new PlaceListFragment();
                break;
            case R.id.item_shop:
                title = getString(R.string.category_shops);
                fragment = new PlaceListFragment();
                break;
            case R.id.item_event:
                title = getString(R.string.category_events);
                fragment = new EventFragment();
                break;
        }

        if (fragment == null) {
            Log.e("MainActivity", "null fragment, id=" + item_id);
            return false;
        }

        getSupportActionBar().setTitle(title);

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            //.addToBackStack(null)
            .commit();

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        return loadFragment( item.getItemId() );
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        setSupportActionBar( (Toolbar) findViewById(R.id.toolbar) );

        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.ic_pocket);

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        loadFragment( R.id.item_book );
    }
}

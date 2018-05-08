package com.example.fchataigner.pocket;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FindBookActivity extends Activity implements AdapterView.OnItemClickListener
{
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri photoUri;

    private File createFile( String pattern, String extension ) throws IOException
    {
        String timeStamp = new SimpleDateFormat(pattern).format(new Date());
        String name = "JPEG_" + timeStamp;
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile( name, extension, dir );
    }

    void takePhotoHandler( View view )
    {
        File photoFile;
        try { photoFile = createFile( "yyyyMMdd_HHmmss", ".jpg "); }
        catch( IOException ex ) { Log.e( "createFile", ex.getMessage() ); return; }

        photoUri = FileProvider.getUriForFile( this, "com.example.fchataigner.pocket.fileprovider", photoFile );
        Log.i( "StartCapture", photoUri.getPath() );

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) return;

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    public void onItemClick( AdapterView<?> bookList, View view, int position, long id )
    {
        Book book = (Book) bookList.getItemAtPosition(position);
        Log.i( "clicked","title=" + book.title );

        Intent intent = new Intent();
        intent.putExtra("book", book );
        setResult( Activity.RESULT_OK, intent );

        finish();
    }

    @Override
    protected void onActivityResult( int request, int result, Intent intent )
    {
        if ( request == REQUEST_IMAGE_CAPTURE && result == RESULT_OK )
        {
            Bitmap bitmap;
            try { bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri ); }
            catch( Exception ex ) { Log.e( "onActivityResult", ex.getMessage() ); return; }

            //double ratio = 512.0 / bitmap.getWidth();
            //Bitmap scaled = Bitmap.createScaledBitmap( bitmap, (int)(ratio*bitmap.getWidth()), (int)(ratio*bitmap.getHeight()), true );
            //ImageView imgview = (ImageView) findViewById(R.id.img);
            //imgview.setImageBitmap(scaled);

            ListView bookFindList = (ListView) findViewById(com.example.fchataigner.pocket.R.id.book_find_list);
            bookFindList.setOnItemClickListener(this);

            BookFinder bookFinder = new BookFinder( this.getApplicationContext(), bookFindList );
            bookFinder.execute(bitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(com.example.fchataigner.pocket.R.layout.find_book);
    }
}

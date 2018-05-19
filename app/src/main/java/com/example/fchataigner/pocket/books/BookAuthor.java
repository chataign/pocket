package com.example.fchataigner.pocket.books;

import android.util.Log;
import android.util.Xml;

import com.example.fchataigner.pocket.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static com.example.fchataigner.pocket.Utils.skip;

public class BookAuthor
{
    static private String TAG = "BookAuthor";

    String id;
    String name;
    String link;

    private static final String RESPONSE_TAG = "GoodreadsResponse";
    private static final String AUTHOR_TAG = "author";
    private static final String NAME_TAG = "name";
    private static final String LINK_TAG = "link";
    private static final String ID_ATTRIBUTE = "id";

    private static final String ns = null; // we don't use namespaces

    private void parseXml( XmlPullParser parser ) throws XmlPullParserException, IOException
    {
        parser.require( XmlPullParser.START_TAG, ns, AUTHOR_TAG );
        id = parser.getAttributeValue(null, ID_ATTRIBUTE );

        while ( parser.next() != XmlPullParser.END_TAG )
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String tag = parser.getName();

            if (tag.equals(NAME_TAG)) this.name = Utils.readXmlText(parser, NAME_TAG);
            if (tag.equals(LINK_TAG)) this.link = Utils.readXmlText(parser, LINK_TAG);
            //else Utils.skip(parser);
        }
    }

    static BookAuthor parseGoodreadsXML( String xml_response ) throws XmlPullParserException, IOException
    {
        InputStream stream = new ByteArrayInputStream( xml_response.getBytes("UTF-8"));

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput( stream, null);
        parser.nextTag();

        //<GoodreadsResponse>
        // <Request>
        // <authentication>true</authentication>
        // <key>lWhRU6SpBbLrSzr11U1Jg</key>
        // <method>api_author_link</method>
        // </Request>
        // <author id="11291">
        // <name>Chimamanda Ngozi Adichie</name>
        // <link>https://www.goodreads.com/author/show/11291.Chimamanda_Ngozi_Adichie?utm_medium=api&utm_source=author_link</link>
        // </author>
        // </GoodreadsResponse>

        parser.require(XmlPullParser.START_TAG, ns, RESPONSE_TAG);

        while ( parser.next() != XmlPullParser.END_TAG )
        {
            if ( parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String tag = parser.getName();

            if ( tag.equals(AUTHOR_TAG) )
            {
                BookAuthor author = new BookAuthor();
                author.parseXml(parser);
                return author;
            }
            else Utils.skip(parser);
        }

        throw new IOException("author tag not found");
    }
}


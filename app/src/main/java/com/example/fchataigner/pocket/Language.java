package com.example.fchataigner.pocket;

public class Language
{
    final public String name;
    final public String code;

    private Language( String name, String code )
    {
        this.name = name;
        this.code = code;
    }

    @Override
    public String toString()
    {
        return name;
    } // for use in SpinnerAdapter

    static public Language English = new Language( "english", "en" );
    static public Language Spanish = new Language( "español", "es" );
    static public Language French = new Language( "français", "fr" );
}
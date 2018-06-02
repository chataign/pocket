package com.example.fchataigner.pocket.books;

import com.example.fchataigner.pocket.ItemListFragment;
import com.example.fchataigner.pocket.R;

public class BookListFragment extends ItemListFragment<Book>
{
    protected int getFileResource() { return R.string.books_file; }
    protected int getItemListLayout() { return R.layout.book_item; }
    protected Class<?> getAddItemActivity() { return FindBookActivity.class; }
    protected Book.Builder getBuilder() { return new Book().new Builder(); }
}
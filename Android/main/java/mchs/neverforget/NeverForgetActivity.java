/*      Copyright 2016 Marcello de Paula Ferreira Costa

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License. */
package mchs.neverforget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import mchs.neverforget.model.Book;
import mchs.neverforget.receivers.NeverForgetReceiver;
import mchs.neverforget.services.NotificationIntentService;
import mchs.neverforget.services.UpdateIntentService;
import mchs.neverforget.util.Crawler;
import mchs.neverforget.util.NeverForgetDatabaseAdapter;

public class NeverForgetActivity extends AppCompatActivity {
    private static final String GREETING = "GREETING";
    private static final String TAG = "NeverForgetActivity";
    private static final String SELECTED_BOOKS = "SELECTED_BOOKS";
    private static final int NO_BOOKS_RENEWED = 0;
    private static final int AT_LEAST_ONE_BOOK_RENEWED = 1;
    private static final int ALL_BOOKS_RENEWED = 2;
    private ArrayList<Book> bookArrayList = new ArrayList<>();
    private ListView booksListView;
    private TextView nameTextView;
    private Crawler crawler;
    private boolean[] isBookSelected;
    private NeverForgetDatabaseAdapter databaseAdapter;
    private ArrayAdapter<Book> bookListAdapter;
    private String greeting;

    private boolean isDatasetChanged = false;

    /* First function called when a activity is created */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_never_forget);
        nameTextView = (TextView) findViewById(R.id.nameTextView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setupBookListViewListener();

        databaseAdapter = new NeverForgetDatabaseAdapter(this);
        crawler = Crawler.getInstance();

        if (savedInstanceState == null) {
            if (isFirstLogin(this)) {
                populateBooksList(true);
                isDatasetChanged = true;
                NeverForgetReceiver.broadcastFirstLogin(this);
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(LoginScreenActivity.IS_FIRST_LOGIN_PREFS, false)
                        .apply();
            } else {
                populateBooksList(false);
            }
        }
    }

    protected static boolean isFirstLogin(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(LoginScreenActivity.IS_FIRST_LOGIN_PREFS, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isDatasetChanged) {
            updateDatabase();
            isDatasetChanged = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBooleanArray(SELECTED_BOOKS, isBookSelected);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isBookSelected = savedInstanceState.getBooleanArray(SELECTED_BOOKS);
        populateBooksList(false); /* False tells that it isn't first login */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.never_forget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(NeverForgetActivity.this);
        alertBuilder.setTitle(R.string.nfa_logout_dialog_title)
                .setMessage(R.string.nfa_logout_dialog_message)
                .setCancelable(false)
                .setPositiveButton(R.string.nfa_logout_dialog_yes_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseAdapter.clearDatabase();
                                UpdateIntentService
                                        .startActionCancelRecurrentUpdateAlarm(NeverForgetActivity.this);
                                NotificationIntentService.cancelCustomRecurrentNotificationAlarm(
                                        getApplicationContext(),
                                        1);
                                NotificationIntentService.cancelCustomRecurrentNotificationAlarm(
                                        getApplicationContext(),
                                        2);
                                NotificationIntentService.cancelCustomRecurrentNotificationAlarm(
                                        getApplicationContext(),
                                        3);
                                PreferenceManager.getDefaultSharedPreferences(NeverForgetActivity.this)
                                        .edit()
                                        .putBoolean(LoginScreenActivity.IS_LOGGED_IN, false)
                                        .apply();
                                dialog.dismiss();
                                NeverForgetActivity.super.onBackPressed();
                            }
                        })
                .setNegativeButton(R.string.nfa_logout_dialog_no_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        alertBuilder.create().show();
    }

    private void updateDatabase() {
        databaseAdapter.clearDatabase();
        for (Book book : bookArrayList) {
            if (databaseAdapter.insertBook(book) > 0) {
                Log.v(TAG,"Book added to database");
            } else {
                Log.e(TAG,"Failed to add book to database");
            }
        }
    }

    private void setupInformativeColors() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        for (Book b : bookArrayList) {
            Calendar returnDate = new GregorianCalendar();
            Calendar currentDate = new GregorianCalendar();
            try {
                returnDate.setTime(sdf.parse(b.getReturnDate()));
                currentDate.setTime(sdf.parse(sdf.format(System.currentTimeMillis())));
                if (returnDate.before(currentDate)) {
                    b.setDrawableId(R.drawable.item_book_expired);
                } else {
                    int days = daysBetween(returnDate, currentDate);
                    if (days < 2) {
                        b.setDrawableId(R.drawable.item_book_red);
                    } else b.setDrawableId(R.drawable.item_book);
                }
            } catch (ParseException e) {
                e.printStackTrace();

            }
        }
    }

    public static int daysBetween(Calendar day1, Calendar day2) {
        Calendar dayOne = (Calendar) day1.clone(),
                dayTwo = (Calendar) day2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays;
        }
    }

    private void setupBookListViewListener() {
        booksListView = (ListView) findViewById(R.id.booksListView);
        booksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                booksListView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isBookSelected[position]) {
                            view.setBackgroundColor(ContextCompat.getColor(
                                    getApplicationContext(),
                                    R.color.colorListView
                            ));
                            isBookSelected[position] = false;
                            Toast.makeText(getApplicationContext(),
                                    R.string.nfa_book_removed,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            isBookSelected[position] = true;
                            view.setBackgroundColor(ContextCompat.getColor(
                                    getApplicationContext(),
                                    R.color.colorSelected));
                            Toast.makeText(getApplicationContext(),
                                    R.string.nfa_book_added,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    private void populateBooksListView() {
        bookListAdapter = new BookListAdapter();
        booksListView.setAdapter(bookListAdapter);
    }

    private void populateBooksList(boolean isFirstLogin) {
        if (isFirstLogin) {
            Thread getBooksThread = new Thread() {
                public void run() {
                    bookArrayList.addAll(crawler.getBooksListPostLogin());
                    isBookSelected = new boolean[bookArrayList.size()];
                    setupInformativeColors();
                    greeting = "Olá, " + crawler.getName();
                    PreferenceManager.getDefaultSharedPreferences(NeverForgetActivity.this).edit()
                            .putString(GREETING, greeting)
                            .apply();


                    booksListView.post(new Runnable() {
                        @Override
                        public void run() {
                            nameTextView.setText(greeting);
                            populateBooksListView();
                        }
                    });
                }
            };
            getBooksThread.start();
        } else {
            bookArrayList.addAll(databaseAdapter.getBooks());

            setupInformativeColors();
            nameTextView.setText(PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(GREETING, "Olá!"));
            populateBooksListView();
        }
    }

    /* Callback function that is called from portrait mode by clicking a button*/
    public void renewSelectedBooks(View view) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(NeverForgetActivity.this);
        alertBuilder.setTitle(R.string.action_renew_selected)
                .setMessage(getString(R.string.nfa_renew_selected_question))
                .setCancelable(false)
                .setPositiveButton(R.string.nfa_logout_dialog_yes_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                renewSelectedBooks();
                            }
                        })
                .setNegativeButton(R.string.nfa_logout_dialog_no_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        alertBuilder.create().show();
    }

    /* Callback function that is called from landscape mode by clicking the action bar */
    public void renewSelectedBooks(MenuItem menuItem) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(NeverForgetActivity.this);
        alertBuilder.setTitle(R.string.action_renew_selected)
                .setMessage(getString(R.string.nfa_renew_selected_question))
                .setCancelable(false)
                .setPositiveButton(R.string.nfa_logout_dialog_yes_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                renewSelectedBooks();
                            }
                        })
                .setNegativeButton(R.string.nfa_logout_dialog_no_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        alertBuilder.create().show();
    }

    /* Function that implements "Renew Selected" feature */
    public void renewSelectedBooks() {
        Thread renewSelectedThread = new Thread() {
            @Override
            public void run() {
                ArrayList<Book> temp = crawler.renewSelected(isBookSelected, bookArrayList);
                if (temp == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    R.string.nfa_renew_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    int returnFlag = replaceBooks(temp);
                    if (returnFlag == NO_BOOKS_RENEWED) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        R.string.nfa_no_books_renewed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (returnFlag == ALL_BOOKS_RENEWED) {
                        isDatasetChanged = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bookListAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(),
                                        R.string.nfa_renew_at_least_one_selected,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        isDatasetChanged = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bookListAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(),
                                        R.string.nfa_renewed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        };
        renewSelectedThread.start();
    }

    private int replaceBooks(ArrayList<Book> renewedBookList) {
        int size = bookArrayList.size();
        int returnValue = NO_BOOKS_RENEWED;
        int count = 0;
        boolean[] shouldNotPersist = new boolean[size];
        for (int i = 0; i < size; i++) {
            Book book = bookArrayList.get(i);
            for (Book renewedBook : renewedBookList)
                if (book.getTitle().equals(renewedBook.getTitle()) &&
                        !book.getReturnDate().equals(renewedBook.getReturnDate())) {
                    returnValue++;
                    shouldNotPersist[i] = true;
                }
        }

        for (int i = 0; i < size; i++) {
            if (!shouldNotPersist[i]) renewedBookList.add(bookArrayList.get(i));
        }

        for (boolean b : isBookSelected) {
            if (b) count++;
        }

        bookArrayList.clear();
        bookArrayList.addAll(renewedBookList);

        return (count == returnValue) ? ALL_BOOKS_RENEWED : AT_LEAST_ONE_BOOK_RENEWED;
    }

    /* Callback function that is called from portrait mode by clicking a button*/
    public void renewAllBooks(View view) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(NeverForgetActivity.this);
        alertBuilder.setTitle(R.string.action_renew_all)
                .setMessage(getString(R.string.nfa_renew_all_question))
                .setCancelable(false)
                .setPositiveButton(R.string.nfa_logout_dialog_yes_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                renewAllBooks();
                            }
                        })
                .setNegativeButton(R.string.nfa_logout_dialog_no_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        alertBuilder.create().show();
    }

    /* Callback function that is called from landscape mode by clicking the action bar */
    public void renewAllBooks(MenuItem menuItem) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(NeverForgetActivity.this);
        alertBuilder.setTitle(R.string.action_renew_all)
                .setMessage(getString(R.string.nfa_renew_all_question))
                .setCancelable(false)
                .setPositiveButton(R.string.nfa_logout_dialog_yes_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                renewAllBooks();
                            }
                        })
                .setNegativeButton(R.string.nfa_logout_dialog_no_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        alertBuilder.create().show();
    }

    /* Function that implements "Renew All" feature */
    public void renewAllBooks() {
        Thread renewAllThread = new Thread() {
            @Override
            public void run() {
                ArrayList<Book> temp = crawler.renewAll();
                if (temp == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NeverForgetActivity.this,
                                    R.string.nfa_renew_failed,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    int returnFlag = replaceBooks(temp);
                    if (returnFlag == NO_BOOKS_RENEWED) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        R.string.nfa_no_books_renewed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (returnFlag == ALL_BOOKS_RENEWED) {
                        isDatasetChanged = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bookListAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(),
                                        R.string.nfa_renew_at_least_one,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        isDatasetChanged = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bookListAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(),
                                        R.string.nfa_renewed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        };
        renewAllThread.start();
    }

    /* Callback function that can be called from both portrait and landscape mode and implements
     * a intentional update of the book list */
    public void updateBooks(MenuItem item) {
        Thread updateBooksListThread = new Thread() {
            public void run() {
                ArrayList<Book> temp = crawler.updateBooksList();
                if (temp == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    R.string.nfa_update_failed,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    bookArrayList.clear();
                    bookArrayList.addAll(temp);
                    isDatasetChanged = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupInformativeColors();
                            bookListAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(),
                                    R.string.nfa_updated,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        updateBooksListThread.start();
    }

    /* Callback function that can be called from both portrait and landscape mode.
     * startSettingsActivity is responsible of starting the settings activity as
     * its name says */
    public void startSettingsActivity(MenuItem item) {
        Intent neverForgetIntent = new Intent(
                NeverForgetActivity.this,
                SettingsActivity.class);
        startActivity(neverForgetIntent);
    }

    public void startAboutActivity(MenuItem item) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void startHelpActivity(MenuItem item) {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    /* Custom array adapter implementation became required when a custom layout
     * item was chose to be used. It fills all fields correctly and is also
     * responsible to check if a item was previously selected in a scenario
     * where the activity was re-built due to a orientation change for example */
    private class BookListAdapter extends ArrayAdapter<Book> {

        public BookListAdapter() {
            super(NeverForgetActivity.this, R.layout.item_book, bookArrayList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View bookView = convertView;
            if (bookView == null) {
                bookView = getLayoutInflater().inflate(R.layout.item_book, parent, false);
            }
            /* Find current book */
            final Book currentBook = bookArrayList.get(position);

            /* Fill view's fields with proper gathered information */
            TextView textViewAuthor = (TextView) bookView.findViewById(R.id.item_book_author);
            textViewAuthor.setText(currentBook.getAuthor());
            TextView textViewTitle = (TextView) bookView.findViewById(R.id.item_book_title);
            textViewTitle.setText(currentBook.getTitle());
            TextView textViewReturnDate = (TextView) bookView.findViewById(R.id.item_book_return_date);
            textViewReturnDate.setText(currentBook.getReturnDate());
            ImageView imageView = (ImageView) bookView.findViewById(R.id.item_book_image_view);
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),currentBook.getDrawableId()));
            if (isBookSelected != null) {
                if (isBookSelected[position])
                    bookView.setBackgroundColor(ContextCompat.getColor(
                            getContext(),
                            R.color.colorSelected));
            }
            return bookView;
        }
    }
}
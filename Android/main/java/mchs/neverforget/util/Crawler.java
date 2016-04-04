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
package mchs.neverforget.util;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import mchs.neverforget.model.Book;

@SuppressWarnings("unused")
public class Crawler {
    public static final int REG_SIZE_DEFAULT = 12;
    public static final int REG_SIZE_REDUCED = 8;
    private static final String TAG = "Crawler";
    private final String baseURL;
    private final int DEFAULT_HTML_STYLE = 1;
    private final int RENEWED_HTML_STYLE = 2;
    private String username;
    private String password;
    private int numOfBooks;
    private Document firstPageDocument;
    private Document loginPageDocument;
    private Document postLoginPageDocument;
    private Document userPageDocument;
    private Document loansPageDocument;

    private Crawler() {
        this.baseURL = "http://dedalus.usp.br:80/F?RN=";
    }

    public int login() {
        try {
            setFirstPageDocument();
            setLoginPageDocument();
            setPostLoginPageDocument();
            Elements elements = getPostLoginPageDocument().select(".feedbackbar");
            if (elements.first().text().length() > 1) {
                return ReturnValues.LOGIN_FAILURE.getLevelCode();
            } else {
                return ReturnValues.LOGIN_SUCCESSFUL.getLevelCode();
            }
        } catch (IOException | NullPointerException | IndexOutOfBoundsException e) {
            Log.e(TAG, "Login failure", e);
            return ReturnValues.RETRIEVE_PAGE_FAILURE.getLevelCode();
        }
    }

    public ArrayList<Book> getBooksListPostLogin() {

        try {
            setUserPageDocument();
            setLoansPageDocument();
            return extractListFromHTML(getLoansPageDocument(), DEFAULT_HTML_STYLE);
        } catch (IOException | NullPointerException | IndexOutOfBoundsException e) {
            Log.e(TAG, "Post login failure", e);
            return null;
        }
    }

    public ArrayList<Book> updateBooksList() {
        try {
            setFirstPageDocument();
            setLoginPageDocument();
            setPostLoginPageDocument();
            setUserPageDocument();
            setLoansPageDocument();
            return extractListFromHTML(getLoansPageDocument(), DEFAULT_HTML_STYLE);
        } catch (IOException | NullPointerException | IndexOutOfBoundsException e) {
            Log.e(TAG, "Update failure", e);
            return null;
        }
    }

    public ArrayList<Book> renewSelected(boolean[] isBookSelected, ArrayList<Book> bookArrayList) {
        String renewSelectedURL = arrangeRenewSelectedLink(isBookSelected, bookArrayList);
        try {
            return extractListFromHTML(Jsoup.connect(renewSelectedURL).get(), RENEWED_HTML_STYLE);
        } catch (IOException e) {
            Log.e(TAG, "Failed to \"get\" renew selected page", e);
            return null;
        }
    }

    public ArrayList<Book> renewAll() {
        String renewAllURL = arrangeRenewAllLink();
        try {
            return extractListFromHTML(Jsoup.connect(renewAllURL).get(), RENEWED_HTML_STYLE);
        } catch (IOException e) {
            Log.e(TAG, "Failed to \"get\" renew all page", e);
            return null;
        }
    }

    private String arrangeLoginLink(Element formElement) {
        String loginLink = formElement.attr("action");
        String func = "login-session";
        String loginSource = "";
        String borLibrary = "USP50";

        loginLink = loginLink + "?func=" + func
                + "&login_source=" + loginSource
                + "&bor_id=" + this.username
                + "&bor_verification=" + this.password
                + "&bor_library=" + borLibrary
                + "&x=0&y=0";

        return loginLink;
    }

    private String arrangeRenewAllLink() {
        Elements elements = loansPageDocument.getElementsByAttributeValue("title", "Renovar Todos");
        String renewAllURL = elements.attr("href");
        renewAllURL = renewAllURL.substring(
                renewAllURL.indexOf("\'") + 1,
                renewAllURL.lastIndexOf("\'"));
        return renewAllURL;
    }

    private String arrangeRenewSelectedLink(boolean[] isBookSelected, ArrayList<Book> bookArrayList) {
        Elements elements = loansPageDocument.getElementsByAttributeValue("title", "Renovar Todos");
        String renewSelectedURL = elements.attr("href");
        renewSelectedURL = renewSelectedURL.substring(
                renewSelectedURL.indexOf("\'") + 1,
                renewSelectedURL.lastIndexOf("\'"));

        renewSelectedURL = renewSelectedURL + "&renew_selected=Y";
        for (int i = 0; i < this.numOfBooks; i++) {
            if (isBookSelected[i]) {
                renewSelectedURL = renewSelectedURL +
                        "&" + bookArrayList.get(i).getNameAttribute() +
                        "=Y";
            }
        }
        return renewSelectedURL;
    }

    private ArrayList<Book> extractListFromHTML(Document document, int style) {
        ArrayList<Book> bookArrayList = new ArrayList<>();
        Elements elements = document.select(".td1").not("[id]");
        inflateBooksList(bookArrayList, elements, style);

        return bookArrayList;
    }

    private void inflateBooksList(ArrayList<Book> bookArrayList, Elements elements,
                                  int style) {
        switch (style) {
            case DEFAULT_HTML_STYLE:
                for (int i = 0; i < this.numOfBooks; i++) {
                    int index = i * REG_SIZE_DEFAULT;
                    bookArrayList.add(new Book(
                            elements.get(index + 1).text(), // Author
                            elements.get(index + 2).text(),// Title
                            elements.get(index + 4).text(),// Return Date
                            elements.get(index + 7).text(),// Library
                            elements.get(index).select("input").attr("name")
                    ));
                }
                break;
            case RENEWED_HTML_STYLE:
                for (int i = 0; i < bookArrayList.size() / REG_SIZE_REDUCED; i++) {
                    int index = i * REG_SIZE_REDUCED;
                    bookArrayList.add(new Book(
                            null, // Author
                            elements.get(index).text(),// Title
                            elements.get(index + 2).text(),// Return Date
                            elements.get(index + 4).text()// Library
                    ));
                }
                break;
        }
    }

    private static class SingletonHolder {
        private static final Crawler CRAWLER = new Crawler();
    }

    public static Crawler getInstance() {
        return SingletonHolder.CRAWLER;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstPageDocument() throws IOException {
        String url = this.baseURL + Math.round(Math.random() * 1000000000);
        this.firstPageDocument = Jsoup.connect(url).get();
    }

    public void setLoginPageDocument() throws IOException, NullPointerException {
        Elements elements = this.firstPageDocument
                .getElementsByAttributeValue("title", "Informa usuário e senha");
        this.loginPageDocument = Jsoup.connect(elements.get(0).attr("href")).get();
    }

    public void setPostLoginPageDocument() throws IOException, NullPointerException {
        Elements elements = this.loginPageDocument.getElementsByAttributeValue("name", "form1");
        this.postLoginPageDocument = Jsoup.connect(arrangeLoginLink(elements.get(0))).get();
    }

    public void setUserPageDocument() throws IOException, NullPointerException {
        Elements elements = this.postLoginPageDocument
                .getElementsByAttributeValue("title", "Mostra situação do usuário na biblioteca");
        System.out.println();
        this.userPageDocument = Jsoup.connect(elements.get(0).attr("href")).get();
    }

    public void setLoansPageDocument() throws IOException, IndexOutOfBoundsException, NullPointerException {
        Elements elements = userPageDocument.getElementsByClass("td1");
        elements = elements.select("a[href]");
        setNumOfBooks(Integer.parseInt(elements.get(1).text()));
        loansPageDocument = Jsoup.connect(elements.get(1).attr("href")
                .substring(
                        elements.get(1).attr("href").indexOf("\'") + 1,
                        elements.get(1).attr("href").lastIndexOf("\'")))
                .get();
    }

    public void setLoansPageDocument(Document loansPageDocument) throws IOException {
        this.loansPageDocument = loansPageDocument;
    }

    public Document getFirstPageDocument() {
        return this.firstPageDocument;
    }

    public Document getLoginPageDocument() {
        return this.loginPageDocument;
    }

    public Document getPostLoginPageDocument() {
        return this.postLoginPageDocument;
    }

    public Document getUserPageDocument() {
        return this.userPageDocument;
    }

    public Document getLoansPageDocument() {
        return this.loansPageDocument;
    }

    public String getName() {
        try {
            String temp = getUserPageDocument().select(".td2").not("[id]").get(2).text();
            int i = temp.indexOf(' ');
            return temp.substring(0, i);
        } catch (NullPointerException e){
            Log.e(TAG,"Couldn't get user name getUserPageDocument()");
            return null;
        }

    }

    private void setNumOfBooks(int numOfBooks) {
        this.numOfBooks = numOfBooks;
    }

    public int getNumOfBooks() {
        return this.numOfBooks;
    }
}
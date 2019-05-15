import oracle.jdbc.driver.OracleDriver;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class hw3 {
    private JPanel backgroundPanel;
    private JPanel bottomPanel;
    private JPanel topPanel;
    private JPanel leftbottomPanel;
    private JPanel rightbottomPanel;
    private JPanel queryPanel;
    private JPanel buttonPanel;
    private JTextArea queryDisplay;
    private JButton moviequeryButton;
    private JButton userqueryButton;
    private JScrollPane userresultsPane;
    private JPanel genrepanelpp;
    private JPanel countryPanelpp;
    private JPanel castPanel;
    private JPanel tagPanel;
    private JPanel movieresultsPanel;
    private JScrollPane movieresultsPane;
    private JPanel tagweightPanel;
    private JPanel valuePanel;
    private JTextField tagvalue;
    private JComboBox tagweightcombo;
    private JPanel directorPanel;
    private JPanel castsearchPanel;
    private JComboBox actor1;
    private JComboBox actor2;
    private JComboBox actor3;
    private JComboBox actor4;
    private JComboBox directorsearch;
    private JLabel Genres;
    private JLabel Country;
    private JLabel Cast;
    private JLabel TagidVal;
    private JLabel MovieResults;
    private JLabel UserResults;
    private JTable queryresulttable;
    private JTable userresulttable;
    private JScrollPane genreScrollPanel;
    private JPanel genrepanel;
    private JPanel countryPanel;
    private JPanel todatePanel;
    private JPanel fromdatePanel;
    private JTextField selectedEndYear;

    //global variables
    private ArrayList<JCheckBox> respected_genres = new ArrayList<>();
    private ArrayList<JCheckBox> respected_countries= new ArrayList<>();
    private ArrayList<JCheckBox> respected_tags = new ArrayList<>();
    private Connection conn;
    public enum AttrType {
        Genres,
        Countries,
        Tags}
    private String cod_search;
    private JButton loadcountrybtn;
    private JTextField selectedStartYear;
    private JScrollPane queryscroll;
    private JScrollPane tagselectionScroll;
    private JPanel tagselectionPanel;
    private JPanel andorPanel;
    private JComboBox selectAndOr;
    private DefaultTableModel tModel;
    private DefaultTableModel uModel;
    private int[] selectedRowsFromMovieResult;
    private HashMap<Integer, Integer> selectedMoviesFromQuery = new HashMap<>();

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private void update_genre(ResultSet result) throws SQLException {
        removeCountryPanel();
        while (result.next()) {
            genrepanel.setLayout(new GridLayout(0, 1));
            JCheckBox cb = new JCheckBox(result.getString(1));

            cb.addActionListener(e -> {
                loadCountry();
                cast_director_fill();
            });
            respected_genres.add(cb);
            genrepanel.add(cb);
            genrepanel.revalidate();
            genrepanel.repaint();
        }
    }

private ArrayList<String> selectCheckBox(AttrType type) {
    ArrayList<String> checkList = new ArrayList<>();
    ArrayList<JCheckBox> typeList = new ArrayList<>();
    if (type.equals(AttrType.Genres)) {
        typeList = respected_genres;
    } else if (type.equals(AttrType.Countries)) {
        typeList = respected_countries;
    } else if (type.equals(AttrType.Tags)) {
        typeList = respected_tags;
    }
    for (JCheckBox cb : typeList) {
        if (cb.isSelected()) {
            checkList.add(cb.getText());
        }
    }
    return checkList;
}

    private void tag_update(ResultSet result) throws SQLException {
        removeTagPanel();
        DefaultComboBoxModel<Integer> defaultComboBoxModel6 = new DefaultComboBoxModel<>();
        HashMap<Integer, Integer> uniqueWeight = new HashMap<>();
        while (result.next()) {
            tagselectionPanel.setLayout(new GridLayout(0, 1));
            String tagid = result.getString(1);
            String tagtext = result.getString(2);
            Integer tagweight = result.getInt(3);

            if (!tagid.equals(" ") && !tagtext.equals(" ")) { // some genres don't have country
                JCheckBox cb = new JCheckBox(tagid + " " + tagtext);
                cb.addActionListener(e -> {
                });
                respected_tags.add(cb);
                tagselectionPanel.add(cb);
                tagselectionPanel.revalidate();
                tagselectionPanel.repaint();
            }
        }
    }


    private void tag_fill() {
        ArrayList<String> checkList = selectCheckBox(AttrType.Countries);
        ArrayList<String> gcheckList = selectCheckBox(AttrType.Genres);
        ResultSet result;

        if (checkList.size() != 0) {
            StringBuilder sb = new StringBuilder();

            sb.append("select distinct mtag.TAGID,tag.VALUE, mtag.TAGWEIGHT\n");
            sb.append("from MOVIE_TAGS mtag, TAGS tag, MOVIE_COUNTRIES loc, MOVIES mov1,\n");
            sb.append("(SELECT movieID, LISTAGG(genre, ',') WITHIN GROUP (ORDER BY genre) AS Genres\n" +
                    "FROM movie_genres\n" +
                    "GROUP BY movieID) select_genre\n");
            sb.append("where mov1.MOVIEID = mtag.MOVIEID\n");
            sb.append("and mov1.MOVIEID = select_genre.MOVIEID\n");
            sb.append("and mov1.MOVIEID = loc.MOVIEID\n");
            sb.append("and tag.TAGID = mtag.TAGID\n");
            sb.append("and loc.COUNTRY in (");
            for (int i = 0; i < checkList.size(); i++) {
                if (i != checkList.size() - 1) {
                    sb.append("'").append(checkList.get(i)).append("',");
                } else {
                    sb.append("'").append(checkList.get(i)).append("'");
                }
            }
            sb.append(")");
            sb.append("AND");
            sb.append("(");
            for (int i = 0; i < gcheckList.size(); i++) {
                if (i == 0) {
                    sb.append("select_genre.Genres LIKE '%" + gcheckList.get(i) + "%'\n");
                } else {
                    sb.append(cod_search + " select_genre.Genres LIKE '%" + gcheckList.get(i) + "%'\n");
                }
            }
            sb.append(")\n");
            queryDisplay.setText(sb.toString());
            try {
                result = executeQuery(sb.toString());
                tag_update(result);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            removeTagPanel();
            tagvalue.setText("");
        }
    }

private void country_update(ResultSet result) throws SQLException {
    removeCountryPanel();
    while (result.next()) {
        countryPanel.setLayout(new GridLayout(0, 1));
        String text = result.getString(1);
        if (!text.equals(" ")) {
            JCheckBox cb = new JCheckBox(text);
            cb.addActionListener(e -> {
                tag_fill();
                cast_director_fill();
            });
            respected_countries.add(cb);
            countryPanel.add(cb);
            countryPanel.revalidate();
            countryPanel.repaint();
        }
    }
}

private void loadCountry() {
    ArrayList<String> checkList = selectCheckBox(AttrType.Genres);
    ResultSet result;
    String yearFrom;
    String yearTo;
    yearFrom = selectedStartYear.getText();
    yearTo = selectedEndYear.getText();

    if (checkList.size() != 0) {
        StringBuilder sb = new StringBuilder();
            sb.append("SELECT DISTINCT country\n");
            sb.append("FROM movie_countries loc, ");
            sb.append("(");
            sb.append("SELECT movieID, LISTAGG(genre, ',') WITHIN GROUP (ORDER BY genre) AS Genres\n");
            sb.append("FROM movie_genres\n");
            sb.append("GROUP BY movieID) select_genre");
            sb.append(", MOVIES mov\n");
            sb.append("WHERE select_genre.movieID = loc.movieID AND ");
            sb.append(" loc.MOVIEID = mov.MOVIEID AND");
            sb.append(" mov.YEAR between ").append(yearFrom).append(" and ").append(yearTo).append(" AND ");
            sb.append("(");
            for (int i = 0; i < checkList.size(); i++) {
                if (i == 0) {
                    sb.append("select_genre.Genres LIKE '%" + checkList.get(i) + "%'\n");
                } else {
                    sb.append(cod_search + " select_genre.Genres LIKE '%" + checkList.get(i) + "%'\n");
                }
            }
            sb.append(")\n");
            sb.append("ORDER BY country");

//        }
        queryDisplay.setText(sb.toString());
        try {
            result = executeQuery(sb.toString());
            country_update(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } else {
        removeCountryPanel();
    }
}

private void removeCountryPanel() {
    for (JCheckBox cb : respected_countries) {
        cb.setVisible(false);
        countryPanel.remove(cb);
    }
    respected_countries.clear();
    countryPanel.updateUI();
}

private void removeGenrePanel() {
    for (JCheckBox cb : respected_genres) {
        cb.setVisible(false);
        genreScrollPanel.remove(cb);
    }
    respected_genres.clear();
    genreScrollPanel.updateUI();
}

    private void removeAllText() {
        queryDisplay.setText("");
    }

    private void removeComboBox() {
        actor1.removeAllItems();
        actor2.removeAllItems();
        actor3.removeAllItems();
        actor4.removeAllItems();
        directorsearch.removeAllItems();
    }

    private void removeTagPanel() {
        for (JCheckBox cb : respected_tags) {
            cb.setVisible(false);
            tagselectionPanel.remove(cb);
        }
        respected_tags.clear();
        tagselectionPanel.updateUI();
    }



    private String gen_query() {
        ArrayList<String> sGenre = selectCheckBox(AttrType.Genres);
        ArrayList<String> sCountry = selectCheckBox(AttrType.Countries);
        ArrayList<String> sTags = selectCheckBox(AttrType.Tags);

        if (sGenre.size() == 0) {
            JOptionPane.showMessageDialog(null, "Please select at least one Genre and Country");
            return " ";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("select distinct mov.MOVIEID, mov.TITLE, mg.Genres, mov.YEAR, moc.COUNTRY,md.DIRECTORNAME\n");
        sb.append("from MOVIES mov, MOVIE_COUNTRIES moc, MOVIE_ACTORS ma, MOVIE_DIRECTORS md,\n");
        sb.append("(SELECT movieID, LISTAGG(genre, ',') WITHIN GROUP (ORDER BY genre) AS Genres\n");
        sb.append("\t\t\t\t\t\tFROM movie_genres\n");
        sb.append("\t\t\t\t\t\tGROUP BY movieID) mg\n");
        if (!(tagselectionPanel.getComponentCount() == 0 || sTags.size() == 0)) {
            sb.append(",MOVIE_TAGS mt\n");
        }
        sb.append("where mov.MOVIEID = moc.MOVIEID\n");
        sb.append("  and mov.MOVIEID = ma.MOVIEID\n");
        sb.append("  and mov.MOVIEID = md.MOVIEID\n");
        sb.append("  and mov.MOVIEID = mg.MOVIEID\n");
        if (!(tagselectionPanel.getComponentCount() == 0 || sTags.size() == 0)) {
            sb.append("and mov.MOVIEID = mt.MOVIEID\n");
        }
        sb.append("AND");
        sb.append("(");
        for (int i = 0; i < sGenre.size(); i++) {
            if (i == 0) {
                sb.append("mg.Genres LIKE '%" + sGenre.get(i) + "%'\n");
            } else {
                sb.append(cod_search + " mg.Genres LIKE '%" + sGenre.get(i) + "%'\n");
            }
        }
        sb.append(")\n");

        if (sCountry.size() == 0) {
            return sb.toString();
        }
        sb.append("and moc.COUNTRY in (");
        for (int i = 0; i < sCountry.size(); i++) {
            if (!(i == sCountry.size() - 1)) {
                sb.append("'").append(sCountry.get(i)).append("', ");
            } else {
                sb.append("'").append(sCountry.get(i)).append("')\n");
            }
        }
        boolean castset1 = castset(actor1);
        boolean castset2 = castset(actor2);
        boolean castset3 = castset(actor3);
        boolean castset4 = castset(actor4);
        boolean direcset = directorset(directorsearch);
        StringBuilder movieActor = new StringBuilder();
        movieActor.append(" ");
        if (castset1 || castset2 || castset3 || castset4) {
//            if (!(actor1.getSelectedItem().toString().equals("Choose Actor")))
            movieActor.append("and ma.ACTORNAME in (");
            if (castset1) movieActor.append("'").append(actor1.getSelectedItem().toString()).append("',");
            if (castset2) movieActor.append("'").append(actor2.getSelectedItem().toString()).append("',");
            if (castset3) movieActor.append("'").append(actor3.getSelectedItem().toString()).append("',");
            if (castset4) movieActor.append("'").append(actor4.getSelectedItem().toString()).append("',");
            movieActor.append(")");
            movieActor.setCharAt(movieActor.length() - 2, ' ');
        }
        sb.append(movieActor.toString());

        if (direcset) {
            sb.append(" and md.DIRECTORNAME = '" + directorsearch.getSelectedItem().toString() + "'\n");
        }

        if (respected_tags.size() != 0) {

        }


        if (tagvalue.getText()!= null && !tagvalue.getText().isEmpty()){
            System.out.println(sTags.size());
            if (!tagweightcombo.getSelectedItem().toString().equals("=,<,>,>=,<=")) {
                sb.append(" and mt.TAGWEIGHT " + tagweightcombo.getSelectedItem().toString() + " " + tagvalue.getText());
            }
        }

        if (sTags.size() != 0) {
            sb.append("and ");
            sb.append("mt.TAGID in (");
            StringBuilder tempTags = new StringBuilder();
            for (int i = 0; i < sTags.size(); i++) {
                tempTags.append("'" + Integer.parseInt(sTags.get(i).replaceAll("[\\D]", "")) + "',");
            }
            tempTags.setCharAt(tempTags.length() - 1, ' ');
            tempTags.append(")");
            sb.append(tempTags.toString());
        }
        queryDisplay.append("\n");
        queryDisplay.setText("");
        queryDisplay.append(sb.toString());
        return sb.toString();
    }

    private boolean castset(JComboBox<String> jb) {
        return jb.getItemCount() != 0 && !jb.getSelectedItem().toString().equals("Choose Actor");
    }

    private boolean directorset(JComboBox<String> jb) {
        return jb.getItemCount() != 0 && !jb.getSelectedItem().toString().equals("Choose Director");
    }



    private void performUserQuery() {
        uModel = new DefaultTableModel();
        uModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{
                        "#",
                        "User ID"
                }
        );
        userresulttable.setModel(uModel);
        StringBuilder uquery = new StringBuilder();
        if (queryresulttable.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Please select from Movie Results");
        } else {
            selectedRowsFromMovieResult = queryresulttable.getSelectedRows();
            uquery.append("select  distinct utm.USERID from USER_TAGGEDMOVIES utm \n");
            uquery.append("where utm.MOVIEID in (");
            for (int i1 : selectedRowsFromMovieResult) {
                uquery.append("'" + selectedMoviesFromQuery.get(i1).toString() + "',");
            }
            uquery.setCharAt(uquery.length() - 1, ' ');
            uquery.append(")");
            System.out.println(uquery);
            ResultSet uresult;
            queryDisplay.setText("");
            queryDisplay.append(uquery.toString());

            uresult = executeQuery(uquery.toString());
            int numRow = 1;
            try {
                while (uresult.next()) {
                    Object[] objects = new Object[2];
                    objects[0] = numRow;
                    objects[1] = uresult.getObject(1);
                    uModel.addRow(objects);
                    numRow++;
                }
                if (numRow == 1) {
                    JOptionPane.showMessageDialog(null, "No user has tagged this movie");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }





    private void results_query() {

        ResultSet result;
        ResultSetMetaData metaresult;
        String query;
        int numofCol = 0;
        int numofRow = 1;

        tModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{
                        "#", "MovieID", "Title", "Genre", "Year", "Country",
                        "Director Name"
                }
        );
        queryresulttable.setModel(tModel);
        try {
            query = gen_query();
            System.out.println(query);
            if (!query.equals(" ")) {
                result = executeQuery(query);
                metaresult = result.getMetaData();
                numofCol = metaresult.getColumnCount();
                System.out.println("Fetching data from DB server ....");
                while (result.next()) {
                    Object[] objects = new Object[numofCol + 1];
                    objects[0] = numofRow;
                    for (int i = 1; i <= numofCol; i++) {
                        if (i == 1)
                            selectedMoviesFromQuery.put(numofRow - 1, Integer.valueOf(result.getObject(1).toString()));
                        objects[i] = result.getObject(i);
                    }
                    numofRow++;
                    tModel.addRow(objects);
                }
                if (numofRow == 1) {
                    JOptionPane.showMessageDialog(null, "No data found in DB based on the query conditions");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnect();
        }

    }


    private hw3() {
        String queryForStartYear = "select * from (select mov.YEAR from MOVIES mov order by mov.YEAR asc) where ROWNUM <=1";
        String queryForEndYear = "select * from (select mov.YEAR from MOVIES mov order by mov.YEAR desc) where ROWNUM <=1";
        ResultSet qfsy;
        ResultSet qfey;

        try {
            qfsy = executeQuery(queryForStartYear);
            qfey = executeQuery(queryForEndYear);
            while (qfsy.next()) {
                selectedStartYear.setText(qfsy.getString(1));
            }
            while (qfey.next()) {
                selectedEndYear.setText(qfey.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        cod_search = "OR";

        tModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{
                        "#", "Title", "Genre", "Year", "Country",
                        "Director Name"
                }
        );
        queryresulttable.setModel(tModel);
        uModel = new DefaultTableModel();
        uModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{
                        "#",
                        "User ID"
                }
        );
        userresulttable.setModel(uModel);

        ResultSet result;
        removeGenrePanel();
        removeCountryPanel();
        removeTagPanel();
        removeAllText();
        tagweightcombo.setSelectedIndex(0);
        selectAndOr.setSelectedIndex(0);

        System.out.println("Start to load all Genres data");
        try {
            String query = "SELECT DISTINCT genre\n" + "FROM movie_genres\n" + "ORDER BY genre";
            result = executeQuery(query);
            update_genre(result);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnect();
        }

        moviequeryButton.addActionListener(e -> results_query());


        loadcountrybtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadCountry();
            }
        });
        selectedStartYear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                selectedMovieFrom=Integer.parseInt(selectedStartYear.getText());
            }
        });
        selectedEndYear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                selectedMovieTo=Integer.parseInt(selectedEndYear.getText());
            }
        });
        selectAndOr.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectAndOr.getSelectedItem() == "AND" || selectAndOr.getSelectedItem() == "OR") {
                    cod_search = selectAndOr.getSelectedItem().toString();
                    loadCountry();
                }
            }
        });
        userqueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performUserQuery();
            }
        });
    }

    private Connection openConnect() throws SQLException, ClassNotFoundException {
        DriverManager.registerDriver(new OracleDriver());
        String host = "localhost";
        String port = "1521";
        String dbname = "oracle";
        String username = "scott";
        String password = "tiger";

        String dburl = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbname;
        return DriverManager.getConnection(dburl, username, password);
    }


    private void closeConnect() {
        try {
            conn.close();
//            System.out.println("Disconnect DB ...");
        } catch (SQLException e) {
            System.err.println("[Error]: Cannot close Oracle DB connection: " + e.getMessage());
        }
    }

    /*
     *  Query Execute function
     */
    private ResultSet executeQuery(String query) {
        Statement stmt;
        ResultSet result = null;
        try {
//            System.out.print("Connect DB .... ");
            conn = openConnect();
//            System.out.println("successfully ");
            stmt = conn.createStatement();
            result = stmt.executeQuery(query);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }


private void cast_director_fill() {
    removeComboBox();
    ArrayList<String> CountrycheckList = selectCheckBox(AttrType.Countries);
    ArrayList<String> genreCheckList = selectCheckBox(AttrType.Genres);
    HashMap<String, Integer> uniqueCast = new HashMap<>();
    HashMap<String, Integer> uniqueDirector = new HashMap<>();
    ResultSet castRS;
    ResultSet directorRS;
    StringBuilder cast1 = new StringBuilder();
    StringBuilder director1 = new StringBuilder();

    if (genreCheckList.size() != 0) {
        if (CountrycheckList.size() == 0) {
            cast1.append("select distinct ma.ACTORNAME from MOVIE_ACTORS ma,\n");
            cast1.append(" (SELECT movieID, LISTAGG(genre, ',') WITHIN GROUP (ORDER BY genre) AS Genres\n" +
                    "                                                    FROM movie_genres\n" +
                    "                                                    GROUP BY movieID) select_genre, MOVIES mov\n");
            cast1.append("WHERE select_genre.movieID = ma.movieID\n");
            cast1.append("and ma.MOVIEID = mov.MOVIEID AND\n");
            cast1.append("(");

            director1.append("select distinct md.DIRECTORNAME from MOVIE_DIRECTORS md,\n");
            director1.append(" (SELECT movieID, LISTAGG(genre, ',') WITHIN GROUP (ORDER BY genre) AS Genres\n" +
                    "                                                    FROM movie_genres\n" +
                    "                                                    GROUP BY movieID) select_genre, MOVIES mov\n");
            director1.append("WHERE select_genre.movieID = md.movieID\n");
            director1.append("and md.MOVIEID = mov.MOVIEID AND\n");
            director1.append("(");


            for (int i = 0; i < genreCheckList.size(); i++) {
                if (i == 0) {
                    cast1.append("select_genre.Genres LIKE '%").append(genreCheckList.get(i)).append("%'\n");
                    director1.append("select_genre.Genres LIKE '%").append(genreCheckList.get(i)).append("%'\n");
                } else {
                    cast1.append(cod_search).append(" select_genre.Genres LIKE '%").append(genreCheckList.get(i)).append("%'\n");
                    director1.append(cod_search).append(" select_genre.Genres LIKE '%").append(genreCheckList.get(i)).append("%'\n");
                }
            }
            cast1.append(")\n");
            cast1.append("ORDER BY ma.ACTORNAME");
            director1.append(")\n");
            director1.append("ORDER BY md.DIRECTORNAME");

        } else {
            cast1.append("select distinct  ma.ACTORNAME from MOVIE_ACTORS ma, MOVIES mov,\n" +
                    "(SELECT movieID, LISTAGG(genre, ',')\n" +
                    "WITHIN GROUP (ORDER BY genre) AS Genres\n" +
                    "FROM movie_genres\n" +
                    "GROUP BY movieID) select_genre,\n" +
                    "MOVIE_COUNTRIES loc\n");
            cast1.append("where ma.MOVIEID = mov.MOVIEID\n" +
                    "and mov.MOVIEID = select_genre.MOVIEID\n" +
                    "and mov.MOVIEID = loc.MOVIEID\n");
            cast1.append("and loc.COUNTRY in (");

            director1.append("select distinct  md.DIRECTORNAME from MOVIE_DIRECTORS md, MOVIES mov,\n" +
                    "(SELECT movieID, LISTAGG(genre, ',')\n" +
                    "WITHIN GROUP (ORDER BY genre) AS Genres\n" +
                    "FROM movie_genres\n" +
                    "GROUP BY movieID) select_genre,\n" +
                    "MOVIE_COUNTRIES loc\n");
            director1.append("where md.MOVIEID = mov.MOVIEID\n" +
                    "and mov.MOVIEID = select_genre.MOVIEID\n" +
                    "and mov.MOVIEID = loc.MOVIEID\n");
            director1.append("and loc.COUNTRY in (");
            for (int i = 0; i < CountrycheckList.size(); i++) {
                if (i != CountrycheckList.size() - 1) {
                    cast1.append("'").append(CountrycheckList.get(i)).append("',");
                    director1.append("'").append(CountrycheckList.get(i)).append("',");
                } else {
                    cast1.append("'").append(CountrycheckList.get(i)).append("'");
                    director1.append("'").append(CountrycheckList.get(i)).append("'");
                }
            }
            cast1.append(")\n");
            cast1.append(" AND \n");
            cast1.append("(");
            director1.append(")\n");
            director1.append(" AND \n");
            director1.append("(");
            for (int i = 0; i < genreCheckList.size(); i++) {
                if (i == 0) {
                    cast1.append("select_genre.Genres LIKE '%").append(genreCheckList.get(i)).append("%'\n");
                    director1.append("select_genre.Genres LIKE '%").append(genreCheckList.get(i)).append("%'\n");
                } else {
                    cast1.append(cod_search).append(" select_genre.Genres LIKE '%").append(genreCheckList.get(i)).append("%'\n");
                    director1.append(cod_search).append(" select_genre.Genres LIKE '%").append(genreCheckList.get(i)).append("%'\n");
                }
            }
            cast1.append(")\n");
            director1.append(")\n");

            cast1.append("ORDER BY ma.ACTORNAME");
            director1.append("ORDER BY md.DIRECTORNAME");

        }
        queryDisplay.append("\n");

        try {
            castRS = executeQuery(cast1.toString());
            directorRS = executeQuery(director1.toString());
            actor1.addItem("Choose Actor");
            actor2.addItem("Choose Actor");
            actor3.addItem("Choose Actor");
            actor4.addItem("Choose Actor");
            directorsearch.addItem("Choose Director");

            while (castRS.next()) {
                String castName = castRS.getString(1);
                if (!uniqueCast.containsKey(castName)) {
                    actor1.addItem(castName);
                    actor2.addItem(castName);
                    actor3.addItem(castName);
                    actor4.addItem(castName);
                    uniqueCast.put(castName, 1);
                }
            }
            while (directorRS.next()) {
                String directorName = directorRS.getString(1);
                if (!uniqueDirector.containsKey(directorName)) {
                    directorsearch.addItem(directorName);
                    uniqueDirector.put(directorName, 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } else {
        removeComboBox();

    }

}
    private void startQueryEngine() {

        JFrame frame = new JFrame("Movie Engine");
        frame.setContentPane(new hw3().backgroundPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public static void main(String[] args){
        hw3 startPoint = new hw3();
        startPoint.startQueryEngine();
    }
    }



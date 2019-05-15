
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class Populate {
    File path = new File("inputs");
    File[] listOfFiles = path.listFiles();

    public static void main(String[] args) {
        Populate insert = new Populate();
        insert.execute();
        System.out.println("Finished connection");
    }


    public void execute() {
        Connection conn = null;
        try {
            conn = openConnection();
            insertData(conn, "inputs/tags.dat");


            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String temp = listOfFiles[i].getName();
                    if (temp.trim().contains("user")) {
                        userData(conn, "inputs/" + temp);
                    } else {
                        if (!temp.equals("tags.dat")) {
                            insertData(conn, "inputs/" + temp);
                        }
                    }

                }
            }
        } catch (SQLException e) {
            System.err.println("Errors occurs when connecting to the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            closeConnection(conn);
        }
    }

    private Connection openConnection() throws SQLException, ClassNotFoundException {
        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        String host = "localhost";
        String port = "1521";
        String dbName = "oracle";
        String uName = "scott";
        String pWord = "tiger";
        String dbURL = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
        return DriverManager.getConnection(dbURL, uName, pWord);
    }

    private void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    private void insertData(Connection conn, String fName) throws SQLException, IOException {
        Statement stmt = conn.createStatement();
        FileReader fReader = new FileReader(fName);
        BufferedReader bufReader = new BufferedReader(fReader);

        String tableName = fName.replaceFirst(".dat", "");
        tableName = tableName.replaceFirst("inputs/", "");
        stmt.executeUpdate("DELETE FROM " + tableName);
        String tuple = bufReader.readLine(); // ignore first line
        int len = tuple.split("\t").length;

        if (tableName.equals("movie_locations")) {
            len = 5;
        }
        while ((tuple = bufReader.readLine()) != null) {
            String[] fields = tuple.split("\t");
            String prefix = "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                String attr = " ";
                if (i < fields.length && fields[i] != null) {
                    attr = fields[i].replaceAll("'", "''");
                }
                if (attr.equals("\\N")) {
                    attr = "";
                }

                sb.append(prefix + "'" + attr + "'");
                prefix = ", ";
            }
            stmt.executeUpdate("INSERT INTO " + tableName + " VALUES (" + sb.toString() + ")");
        }

        fReader.close();
        stmt.close();
    }

    private void userData(Connection conn, String fName) throws SQLException, IOException {
        Statement stmt = conn.createStatement();
        FileReader fReader = new FileReader(fName);
        BufferedReader bufReader = new BufferedReader(fReader);

        String tableName = fName.replaceFirst(".dat", "");
        tableName = tableName.replaceFirst("inputs/", "");
        boolean timestamp = false;
        if (tableName.contains("-timestamps")) {
            timestamp = true;
            tableName = tableName.replaceFirst("-timestamps", "");
        }

        stmt.executeUpdate("DELETE FROM " + tableName);

        String tuple = bufReader.readLine();
        int len = 3;
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO " + tableName + " VALUES(?,?,?,?)");
        while ((tuple = bufReader.readLine()) != null) {
            String[] fields = tuple.split("\t");
            for (int i = 0; i < len; i++) {
                pstmt.setString(i + 1, fields[i]);
            }
            StringBuilder sb = new StringBuilder();
            if (!timestamp) {
                sb.append(fields[5] + "-" + fields[4] + "-" + fields[3] + " ");
                sb.append(fields[6] + ":" + fields[7] + ":" + fields[8]);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
                Date date = new Date(Long.parseLong(fields[3]));
                sb.append(sdf.format(date));
            }
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(sb.toString()));
            pstmt.executeUpdate();
        }
        fReader.close();
        pstmt.close();
        stmt.close();
    }
}

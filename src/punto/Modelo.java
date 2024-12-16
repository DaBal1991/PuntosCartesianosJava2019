package punto;

import java.sql.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class Modelo {

    private String driver, prefijoConexion, ip, usr, psw, bd, tabla;
    private Connection conexion;
    private ActionListener listener;
    private String ejeX, ejeY, resultadoConsulta;

    public Modelo() {

        ejeX = "x";
        ejeY = "y";
        driver = "com.mysql.cj.jdbc.Driver";
        prefijoConexion = "jdbc:mysql://";
        ip = "127.0.0.1";
        bd = "puntos";
        tabla = "coordenadas";
        usr = "";
        psw = "";
        conexion = obtenerConexion();
    }

    private ArrayList<Puntos> consulta() {

        conexion = obtenerConexion();

        ArrayList<Puntos> coorPlano = new ArrayList();
        String q = "SELECT * FROM " + tabla;

        try {
            Statement statement = conexion.createStatement();
            ResultSet resultSet = statement.executeQuery(q);

            while (resultSet.next()) {
                coorPlano.add(new Puntos(resultSet.getInt(1), resultSet.getInt(2)));
            }

            resultSet.close();
            statement.close();

        } catch (SQLException ex) {
            reportException(ex.getMessage());
        }

        return coorPlano;
    }

    private ArrayList<Puntos> calcularDistancias(int x, int y) {

        ArrayList<Puntos> coordenadas = consulta();
        ArrayList<Puntos> compilacion = new ArrayList();

        for (Puntos p : coordenadas) {

            p.calcularDistancia(x, y);
            compilacion.add(p);

        }

        Collections.sort(compilacion, Comparator.comparing(Puntos::getDistancia));

        for (int i = 0; i < compilacion.size(); i++) {

            if (compilacion.get(i).getDistancia() == 0.0) {
                compilacion.remove(i);
            }

        }

        return compilacion;
    }

    private ArrayList<Puntos> calcularDistanciasRestantes(int x, int y, ArrayList<Puntos> restantes) {

        ArrayList<Puntos> compilacion = new ArrayList();

        for (Puntos p : restantes) {

            p.calcularDistancia(x, y);
            compilacion.add(p);

        }

        Collections.sort(compilacion, Comparator.comparing(Puntos::getDistancia));

        for (int i = 0; i < compilacion.size(); i++) {

            if (compilacion.get(i).getDistancia() == 0.0) {
                compilacion.remove(i);
            }

        }

        return compilacion;
    }

    public ArrayList<Puntos> cercanos(int x, int y) {

        ArrayList<Puntos> cercanos = new ArrayList();
        ArrayList<Puntos> auxiliar = calcularDistancias(x, y);

        int tam = auxiliar.size();

        for (int i = 0; i < tam; i++) {

            cercanos.add(auxiliar.get(0));
            auxiliar.remove(0);

            x = cercanos.get(i).getX();
            y = cercanos.get(i).getY();

            auxiliar = calcularDistanciasRestantes(x, y, auxiliar);

        }

        return cercanos;
    }

    private Connection obtenerConexion() {
        if (conexion == null) {
            try {
                Class.forName(driver); // driver = "com.mysql.cj.jdbc.Driver";
            } catch (ClassNotFoundException ex) {
                reportException(ex.getMessage());
            }
            try { // prefijoConexion = "jdbc:mysql://";
                conexion
                        = DriverManager.getConnection(prefijoConexion + ip + "/" + bd, usr, psw);
            } catch (Exception ex) {
                reportException(ex.getMessage());
            }
            Runtime.getRuntime().addShutdownHook(new ShutDownHook());
        }
        return conexion;
    }

    private class ShutDownHook extends Thread {

        public void run() {
            try {
                if (conexion != null) {
                    conexion.close();
                }
            } catch (SQLException ex) {
                reportException(ex.getMessage());
            }
        }
    }

    public void addExceptionListener(ActionListener listener) {
        this.listener = listener;
    }

    private void reportException(String exception) {
        if (listener != null) {
            ActionEvent evt = new ActionEvent(this, 0, exception);
            listener.actionPerformed(evt);
        }
    }
}

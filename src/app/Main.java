package app;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import exportacion.Exportable;
import exportacion.ExportadorCSV;
import gui.VentanaPrincipal;
import persistencia.Repositorio;
import persistencia.RepositorioArchivo;

/**
 * Punto de entrada de la aplicación gráfica (Swing).
 * Carga los datos persistidos y abre VentanaPrincipal.
 */
public class Main {

    public static void main(String[] args) {
        Repositorio repositorio = new RepositorioArchivo("datos/notas.txt");
        Exportable exportador = new ExportadorCSV();
        GestorNotas gestor = new GestorNotas(repositorio);

        try {
            gestor.cargar();
            ArrayList<String> advertencias = gestor.getAdvertenciasCarga();
            if (!advertencias.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Se encontraron ").append(advertencias.size())
                  .append(" advertencia(s) al cargar los datos:\n\n");
                int limite = Math.min(10, advertencias.size());
                for (int i = 0; i < limite; i++) {
                    sb.append("- ").append(advertencias.get(i)).append("\n");
                }
                if (advertencias.size() > 10) {
                    sb.append("... y ").append(advertencias.size() - 10).append(" más.\n");
                }
                sb.append("\nLa versión anterior se conservará como notas.txt.bak al guardar.");
                JOptionPane.showMessageDialog(null, sb.toString(),
                        "Advertencias al cargar", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudieron cargar los datos previos: " + e.getMessage()
                    + "\nLa versión anterior se conservará como notas.txt.bak al guardar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VentanaPrincipal(gestor, exportador);
            }
        });
    }
}
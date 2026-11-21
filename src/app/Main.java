package app;

import exportacion.ExportadorCSV;
import exportacion.Exportable;
import persistencia.Repositorio;
import persistencia.RepositorioArchivo;

/**
 * Punto de entrada: arma las piezas y arranca el menú.
 * Aquí (y solo aquí) se elige la implementación concreta: para pasar a
 * base de datos o a PDF bastaría cambiar estas dos líneas.
 */
public class Main {
    public static void main(String[] args) {
        // 1. Elegimos dónde y cómo se guardan los datos
        Repositorio repositorio = new RepositorioArchivo("datos/notas.txt");
        Exportable exportador = new ExportadorCSV();

        // 2. El gestor carga lo guardado al iniciar
        GestorNotas gestor = new GestorNotas(repositorio);
        gestor.cargarTodo();

        // 3. Arrancamos el menú
        new MenuPrincipal(gestor, exportador).iniciar();
    }
}
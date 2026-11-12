package persistencia;

import java.io.IOException;
import java.util.ArrayList;
import modelo.Estudiante;

/**
 * Contrato para guardar y cargar los datos de la aplicación.
 *
 * GestorNotas solo conoce esta interfaz (no las clases concretas),
 * así podemos cambiar de archivo a base de datos sin tocar el gestor.
 */
public interface Repositorio {

    // Escribe todos los estudiantes con sus ciclos, cursos y evaluaciones
    void guardar(ArrayList<Estudiante> estudiantes) throws IOException;

    // Lee lo guardado y devuelve la lista de estudiantes (vacía si no hay datos)
    ArrayList<Estudiante> cargar() throws IOException;
}
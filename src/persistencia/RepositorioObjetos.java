package persistencia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import modelo.Estudiante;

/**
 * Guarda y carga el modelo completo mediante serialización de objetos Java
 * (ObjectOutputStream y ObjectInputStream).
 */
public class RepositorioObjetos implements Repositorio {

    private String rutaArchivo;
    private boolean respaldoRealizado;

    public RepositorioObjetos(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía.");
        }
        this.rutaArchivo = rutaArchivo.trim();
        this.respaldoRealizado = false;
    }

    @Override
    public void guardar(ArrayList<Estudiante> estudiantes) throws IOException {
        File archivo = new File(rutaArchivo);
        File carpetaPadre = archivo.getParentFile();
        if (carpetaPadre != null && !carpetaPadre.exists()) {
            carpetaPadre.mkdirs();
        }

        if (!respaldoRealizado && archivo.exists()) {
            File archivoBak = new File(rutaArchivo + ".bak");
            if (archivoBak.exists()) {
                archivoBak.delete();
            }
            archivo.renameTo(archivoBak);
            respaldoRealizado = true;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(estudiantes);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<Estudiante> cargar() throws IOException {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            Object obj = ois.readObject();
            return (ArrayList<Estudiante>) obj;
        } catch (ClassNotFoundException e) {
            throw new IOException("No se pudo reconstruir el archivo de objetos: " + e.getMessage(), e);
        }
    }
}
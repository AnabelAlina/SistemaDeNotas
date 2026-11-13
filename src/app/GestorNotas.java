package app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import evaluaciones.Evaluacion;
import excepciones.NotaInvalidaException;
import modelo.Curso;
import modelo.Estudiante;
import persistencia.Repositorio;

/**
 * Coordina la aplicación: guarda los estudiantes en memoria, registra notas
 * y delega el guardado/carga en un Repositorio.
 *
 * IMPORTANTE: solo conoce la INTERFAZ Repositorio, no RepositorioArchivo.
 * Así se puede cambiar a base de datos sin tocar esta clase.
 */
public class GestorNotas {

    // Clave = código del estudiante. Se usa LinkedHashMap (hijo de HashMap)
    // para que al guardar los estudiantes salgan en el orden en que se registraron.
    private HashMap<String, Estudiante> estudiantes;
    private Estudiante actual;          // estudiante con el que se está trabajando
    private Repositorio repositorio;    // dependencia de la interfaz

    public GestorNotas(Repositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio no puede ser nulo.");
        }
        this.repositorio = repositorio;
        this.estudiantes = new LinkedHashMap<>();
        this.actual = null;
    }

    // Devuelve true si se registró, false si el código ya existía
    public boolean registrarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo.");
        }
        // 1. Evita duplicados: el código es la clave única
        if (estudiantes.containsKey(estudiante.getCodigo())) {
            System.out.println("Ya existe un estudiante con el código " + estudiante.getCodigo() + ".");
            return false;
        }
        // 2. Lo guardamos en el mapa y lo dejamos como estudiante actual
        estudiantes.put(estudiante.getCodigo(), estudiante);
        actual = estudiante;
        return true;
    }

    // Devuelve el estudiante o null si no existe (el menú debe revisar el null)
    public Estudiante buscarEstudiante(String codigo) {
        if (codigo == null) {
            return null;
        }
        return estudiantes.get(codigo.trim());
    }

    // Registra la nota de una evaluación. Devuelve true si se pudo.
    // Atrapa NotaInvalidaException para mostrar el mensaje sin que la app se caiga.
    public boolean registrarNota(Curso curso, Evaluacion evaluacion, double nota) {
        if (curso == null || evaluacion == null) {
            System.out.println("Debes elegir un curso y una evaluación.");
            return false;
        }
        // Comprobamos que la evaluación realmente pertenece a ese curso
        if (!curso.getEvaluaciones().contains(evaluacion)) {
            System.out.println("Esa evaluación no pertenece al curso " + curso.getCodigo() + ".");
            return false;
        }
        try {
            evaluacion.setNota(nota); // valida 0..20 y marca como rendida
            return true;
        } catch (NotaInvalidaException e) {
            System.out.println("Nota inválida: " + e.getMessage());
            return false;
        }
    }

    // Escribe todos los estudiantes usando el repositorio. Devuelve true si salió bien.
    public boolean guardarTodo() {
        try {
            repositorio.guardar(new ArrayList<>(estudiantes.values()));
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo guardar: " + e.getMessage());
            return false;
        }
    }

    // Llena el mapa con lo guardado. Se llama una vez al iniciar la app.
    public boolean cargarTodo() {
        try {
            ArrayList<Estudiante> cargados = repositorio.cargar();
            estudiantes.clear();
            for (Estudiante e : cargados) {
                estudiantes.put(e.getCodigo(), e);
            }
            // Si había datos, dejamos como actual al último cargado
            actual = cargados.isEmpty() ? null : cargados.get(cargados.size() - 1);
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo cargar: " + e.getMessage());
            return false;
        }
    }

    // Lista de estudiantes registrados (copia, para no modificar el mapa desde fuera)
    public ArrayList<Estudiante> getEstudiantes() {
        return new ArrayList<>(estudiantes.values());
    }

    public Estudiante getActual() { return actual; }

    // Cambia el estudiante con el que trabaja el menú
    public void setActual(Estudiante estudiante) { this.actual = estudiante; }
}
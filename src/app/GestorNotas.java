package app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import evaluaciones.Evaluacion;
import evaluaciones.TrabajoContinua;
import excepciones.NotaInvalidaException;
import modelo.Curso;
import modelo.Estudiante;
import persistencia.Repositorio;

/**
 * Coordina la aplicación: guarda los estudiantes en memoria, registra notas
 * y delega el guardado/carga en un Repositorio.
 *
 * IMPORTANTE: solo conoce la INTERFAZ Repositorio, no RepositorioArchivo.
 */
public class GestorNotas {

    // Clave = código (CUI) del estudiante. LinkedHashMap conserva el orden de registro.
    private HashMap<String, Estudiante> estudiantes;
    private Estudiante actual;
    private Repositorio repositorio;

    public GestorNotas(Repositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio no puede ser nulo.");
        }
        this.repositorio = repositorio;
        this.estudiantes = new LinkedHashMap<>();
        this.actual = null;
    }

    // ---------- MÉTODOS NUEVOS (para las ventanas): lanzan excepción con el mensaje ----------

    // Registra un estudiante. Lanza IllegalArgumentException si es null o si el CUI o el DNI ya existen
    public void agregarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo.");
        }
        // 1. Evita duplicados por CUI (código)
        if (estudiantes.containsKey(estudiante.getCodigo())) {
            throw new IllegalArgumentException(
                "Ya existe un estudiante con el CUI/código " + estudiante.getCodigo() + ".");
        }
        // 2. Evita duplicados por DNI
        if (existeDni(estudiante.getDni())) {
            throw new IllegalArgumentException(
                "Ya existe un estudiante con el DNI " + estudiante.getDni() + ".");
        }
        // 3. Lo guardamos en el mapa y lo dejamos como estudiante actual
        estudiantes.put(estudiante.getCodigo(), estudiante);
        actual = estudiante;
    }

    // Asigna la nota de un examen o del sustitutorio
    public void asignarNota(Curso curso, Evaluacion evaluacion, double nota) throws NotaInvalidaException {
        if (curso == null || evaluacion == null) {
            throw new IllegalArgumentException("Debes elegir un curso y una evaluación.");
        }
        if (!curso.getEvaluaciones().contains(evaluacion)) {
            throw new IllegalArgumentException(
                "Esa evaluación no pertenece al curso " + curso.getCodigo() + ".");
        }
        try {
            evaluacion.setNota(nota);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    // Asigna la nota de un trabajo de una Continua
    public void asignarNotaTrabajo(TrabajoContinua trabajo, double nota) throws NotaInvalidaException {
        if (trabajo == null) {
            throw new IllegalArgumentException("Debes elegir un trabajo.");
        }
        trabajo.setNota(nota);
    }

    // Borra un estudiante por CUI. Devuelve true si existía
    public boolean quitarEstudiante(String cui) {
        if (cui == null) {
            return false;
        }
        Estudiante quitado = estudiantes.remove(cui.trim());
        if (quitado == null) {
            return false;
        }
        if (actual == quitado) {
            actual = null;
            for (Estudiante e : estudiantes.values()) {
                actual = e; // el último que quede
            }
        }
        return true;
    }

    public void guardar() throws IOException {
        repositorio.guardar(new ArrayList<>(estudiantes.values()));
    }

    public void cargar() throws IOException {
        ArrayList<Estudiante> cargados = repositorio.cargar();
        estudiantes.clear();
        for (Estudiante e : cargados) {
            estudiantes.put(e.getCodigo(), e);
        }
        actual = cargados.isEmpty() ? null : cargados.get(cargados.size() - 1);
    }

    // ---------- MÉTODOS VIEJOS (consola): llaman a los nuevos y devuelven true/false ----------

    // Devuelve true si se registró, false si el CUI o el DNI ya existían
    public boolean registrarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo.");
        }
        try {
            agregarEstudiante(estudiante);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // Verifica si ya hay un estudiante registrado con ese DNI
    public boolean existeDni(String dni) {
        if (dni == null) {
            return false;
        }
        String limpio = dni.trim();
        for (Estudiante e : estudiantes.values()) {
            if (e.getDni().equals(limpio)) {
                return true;
            }
        }
        return false;
    }

    // Devuelve el estudiante por CUI/código o null si no existe
    public Estudiante buscarEstudiante(String codigo) {
        if (codigo == null) {
            return null;
        }
        return estudiantes.get(codigo.trim());
    }

    // Registra la nota de una evaluación (ej. Examen o ExamenSustitutorio).
    public boolean registrarNota(Curso curso, Evaluacion evaluacion, double nota) {
        try {
            asignarNota(curso, evaluacion, nota);
            return true;
        } catch (NotaInvalidaException | IllegalArgumentException e) {
            System.out.println("No se pudo registrar la nota: " + e.getMessage());
            return false;
        }
    }

    // Registra la nota de un trabajo dentro de una Continua
    public boolean registrarNotaTrabajo(TrabajoContinua trabajo, double nota) {
        try {
            asignarNotaTrabajo(trabajo, nota);
            return true;
        } catch (NotaInvalidaException | IllegalArgumentException e) {
            System.out.println("No se pudo registrar la nota: " + e.getMessage());
            return false;
        }
    }

    public boolean guardarTodo() {
        try {
            guardar();
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo guardar: " + e.getMessage());
            return false;
        }
    }

    public boolean cargarTodo() {
        try {
            cargar();
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo cargar: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<Estudiante> getEstudiantes() {
        return new ArrayList<>(estudiantes.values());
    }

    public Estudiante getActual() { return actual; }

    public void setActual(Estudiante estudiante) { this.actual = estudiante; }
}
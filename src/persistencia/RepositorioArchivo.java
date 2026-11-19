package persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenFinal;
import evaluaciones.ExamenSustitutorio;
import evaluaciones.Parcial;
import evaluaciones.Practica;
import evaluaciones.Trabajo;
import evaluaciones.TrabajoContinua;
import excepciones.NotaInvalidaException;
import excepciones.PesoInvalidoException;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Guarda y carga los datos en un archivo de texto, una línea por registro,
 * con los campos separados por ';'.
 *
 * Formato jerárquico:
 *   ESTUDIANTE;nombre;apellido;dni;correo;codigo;carrera
 *   CICLO;nombre;anio
 *   CURSO;codigo;nombre;creditos;metaPromedio
 *   EVALUACION;tipo;nombre;peso;fecha;rendida;nota;extra
 *   TRABAJO;nombre;peso;esGrupal;rendido;nota   (pertenece a la última Continua leída)
 */
public class RepositorioArchivo implements Repositorio {

    private static final String SEP = ";";
    private static final String SIN_FECHA = "-";

    private String rutaArchivo;

    public RepositorioArchivo(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía.");
        }
        this.rutaArchivo = rutaArchivo.trim();
    }

    // ===================== GUARDAR =====================

    @Override
    public void guardar(ArrayList<Estudiante> estudiantes) throws IOException {
        Path ruta = Paths.get(rutaArchivo);
        if (ruta.getParent() != null) {
            Files.createDirectories(ruta.getParent());
        }
        try (BufferedWriter out = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            for (Estudiante est : estudiantes) {
                escribir(out, "ESTUDIANTE", est.getNombre(), est.getApellido(), est.getDni(),
                        est.getCorreo(), est.getCodigo(), est.getCarrera());
                for (Ciclo ciclo : est.getCiclos()) {
                    escribir(out, "CICLO", ciclo.getNombre(), String.valueOf(ciclo.getAnio()));
                    for (Curso curso : ciclo.getCursos()) {
                        escribir(out, "CURSO", curso.getCodigo(), curso.getNombre(),
                                String.valueOf(curso.getCreditos()),
                                String.valueOf(curso.getMetaPromedio()));
                        for (Evaluacion ev : curso.getEvaluaciones()) {
                            escribirEvaluacion(out, ev);
                        }
                    }
                }
            }
        }
    }

    private void escribirEvaluacion(BufferedWriter out, Evaluacion ev) throws IOException {
        String extra;
        if (ev instanceof Examen) {
            extra = String.valueOf(((Examen) ev).getNumero());
        } else if (ev instanceof Continua) {
            extra = String.valueOf(((Continua) ev).getNumero());
        } else if (ev instanceof ExamenSustitutorio) {
            extra = "";
        } else if (ev instanceof Practica) {
            extra = String.valueOf(((Practica) ev).getNumero());
        } else if (ev instanceof Parcial) {
            extra = String.valueOf(((Parcial) ev).isTieneRecuperacion());
        } else if (ev instanceof ExamenFinal) {
            extra = String.valueOf(((ExamenFinal) ev).isSustitutorio());
        } else if (ev instanceof Trabajo) {
            extra = String.valueOf(((Trabajo) ev).isEsGrupal());
        } else {
            extra = "";
        }

        String fecha = ev.getFecha() == null ? SIN_FECHA : ev.getFecha().toString();
        // En Continua la nota se calcula con sus trabajos, guardamos 0 en la cabecera
        double notaCabecera = (ev instanceof Continua) ? 0.0 : ev.getNota();

        escribir(out, "EVALUACION", ev.getTipo(), ev.getNombre(),
                String.valueOf(ev.getPeso()), fecha,
                String.valueOf(ev.isRendida()), String.valueOf(notaCabecera), extra);

        // Si es Continua, guardamos justo debajo todos sus TrabajoContinua
        if (ev instanceof Continua) {
            for (TrabajoContinua t : ((Continua) ev).getTrabajos()) {
                escribir(out, "TRABAJO", t.getNombre(),
                        String.valueOf(t.getPeso()),
                        String.valueOf(t.isEsGrupal()),
                        String.valueOf(t.isRendido()),
                        String.valueOf(t.getNota()));
            }
        }
    }

    private void escribir(BufferedWriter out, String... campos) throws IOException {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                linea.append(SEP);
            }
            linea.append(limpiar(campos[i]));
        }
        out.write(linea.toString());
        out.newLine();
    }

    private String limpiar(String texto) {
        return texto.replace(SEP, ",").replace("\n", " ").replace("\r", " ");
    }

    // ===================== CARGAR =====================

    @Override
    public ArrayList<Estudiante> cargar() throws IOException {
        ArrayList<Estudiante> resultado = new ArrayList<>();
        Path ruta = Paths.get(rutaArchivo);

        if (!Files.exists(ruta)) {
            return resultado;
        }

        Estudiante estudiante = null;
        Ciclo ciclo = null;
        Curso curso = null;
        Continua ultimaContinua = null;

        try (BufferedReader in = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea;
            int numLinea = 0;
            while ((linea = in.readLine()) != null) {
                numLinea++;
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] p = linea.split(SEP, -1);
                try {
                    switch (p[0]) {
                        case "ESTUDIANTE":
                            estudiante = null;
                            ciclo = null;
                            curso = null;
                            ultimaContinua = null;
                            estudiante = new Estudiante(p[1], p[2], p[3], p[4], p[5], p[6]);
                            resultado.add(estudiante);
                            break;
                        case "CICLO":
                            ciclo = null;
                            curso = null;
                            ultimaContinua = null;
                            if (estudiante != null) {
                                ciclo = new Ciclo(p[1], Integer.parseInt(p[2]));
                                estudiante.agregarCiclo(ciclo);
                            }
                            break;
                        case "CURSO":
                            curso = null;
                            ultimaContinua = null;
                            if (ciclo != null) {
                                curso = new Curso(p[1], p[2], Integer.parseInt(p[3]),
                                        Double.parseDouble(p[4]));
                                ciclo.agregarCurso(curso);
                            }
                            break;
                        case "EVALUACION":
                            ultimaContinua = null;
                            if (curso != null) {
                                Evaluacion ev = leerEvaluacion(p);
                                if (ev instanceof ExamenSustitutorio) {
                                    // Peso 0: se añade directo si Curso aún no permite peso 0
                                    curso.getEvaluaciones().add(ev);
                                } else {
                                    curso.agregarEvaluacion(ev);
                                }
                                if (ev instanceof Continua) {
                                    ultimaContinua = (Continua) ev;
                                }
                            }
                            break;
                        case "TRABAJO":
                            if (ultimaContinua != null) {
                                TrabajoContinua trabajo = leerTrabajo(p);
                                ultimaContinua.agregarTrabajo(trabajo);
                            }
                            break;
                        default:
                            System.out.println("Línea " + numLinea + " ignorada: tipo desconocido.");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Línea " + numLinea + " ignorada: faltan campos.");
                } catch (IllegalArgumentException | DateTimeParseException
                         | PesoInvalidoException | NotaInvalidaException e) {
                    System.out.println("Línea " + numLinea + " ignorada: " + e.getMessage());
                }
            }
        }
        return resultado;
    }

    private Evaluacion leerEvaluacion(String[] p) throws NotaInvalidaException {
        String tipo = p[1];
        String nombre = p[2];
        double peso = Double.parseDouble(p[3]);
        LocalDate fecha = p[4].equals(SIN_FECHA) ? null : LocalDate.parse(p[4]);
        boolean rendida = Boolean.parseBoolean(p[5]);
        double nota = Double.parseDouble(p[6]);
        String extra = p[7];

        Evaluacion ev;
        switch (tipo) {
            case "Examen":
                ev = new Examen(Integer.parseInt(extra), peso);
                break;
            case "Continua":
                ev = new Continua(Integer.parseInt(extra), peso);
                break;
            case "Sustitutorio":
                ev = new ExamenSustitutorio();
                break;
            case "Practica":
                ev = new Practica(nombre, peso, fecha, Integer.parseInt(extra));
                break;
            case "Parcial":
                ev = new Parcial(nombre, peso, fecha, Boolean.parseBoolean(extra));
                break;
            case "Examen Final":
                ev = new ExamenFinal(nombre, peso, fecha, Boolean.parseBoolean(extra));
                break;
            case "Trabajo":
                ev = new Trabajo(nombre, peso, fecha, Boolean.parseBoolean(extra));
                break;
            default:
                throw new IllegalArgumentException("Tipo de evaluación desconocido: " + tipo);
        }

        // En Continua la nota sale de sus líneas TRABAJO; en las demás se asigna si estaba rendida
        if (rendida && !(ev instanceof Continua)) {
            ev.setNota(nota);
        }
        return ev;
    }

    private TrabajoContinua leerTrabajo(String[] p) throws NotaInvalidaException {
        String nombre = p[1];
        double peso = Double.parseDouble(p[2]);
        boolean esGrupal = Boolean.parseBoolean(p[3]);
        boolean rendido = Boolean.parseBoolean(p[4]);
        double nota = Double.parseDouble(p[5]);

        TrabajoContinua trabajo = new TrabajoContinua(nombre, peso, esGrupal);
        if (rendido) {
            trabajo.setNota(nota);
        }
        return trabajo;
    }
}
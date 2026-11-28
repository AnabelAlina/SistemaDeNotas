package persistencia;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;
import evaluaciones.TrabajoContinua;
import excepciones.NotaInvalidaException;
import excepciones.PesoInvalidoException;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Guarda y carga los datos en un archivo de texto usando PrintWriter y Scanner,
 * una línea por registro, con los campos separados por ';'.
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
    private boolean respaldoRealizado;
    private ArrayList<String> advertencias;

    public RepositorioArchivo(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía.");
        }
        this.rutaArchivo = rutaArchivo.trim();
        this.respaldoRealizado = false;
        this.advertencias = new ArrayList<>();
    }

    @Override
    public ArrayList<String> getAdvertencias() {
        return new ArrayList<>(advertencias);
    }

    // ===================== GUARDAR =====================

    @Override
    public void guardar(ArrayList<Estudiante> estudiantes) throws IOException {
        File archivo = new File(rutaArchivo);
        File carpetaPadre = archivo.getParentFile();
        if (carpetaPadre != null && !carpetaPadre.exists()) {
            carpetaPadre.mkdirs();
        }

        // Respaldo .bak solo en el primer guardado de la sesión si ya existe el archivo
        if (!respaldoRealizado && archivo.exists()) {
            File archivoBak = new File(rutaArchivo + ".bak");
            if (archivoBak.exists()) {
                archivoBak.delete();
            }
            archivo.renameTo(archivoBak);
            respaldoRealizado = true;
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
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

    private void escribirEvaluacion(PrintWriter out, Evaluacion ev) {
        String extra;
        if (ev instanceof Examen) {
            extra = String.valueOf(((Examen) ev).getNumero());
        } else if (ev instanceof Continua) {
            extra = String.valueOf(((Continua) ev).getNumero());
        } else {
            extra = "";
        }

        String fecha = ev.getFecha() == null ? SIN_FECHA : ev.getFecha().toString();
        double notaCabecera = (ev instanceof Continua) ? 0.0 : ev.getNota();

        escribir(out, "EVALUACION", ev.getTipo(), ev.getNombre(),
                String.valueOf(ev.getPeso()), fecha,
                String.valueOf(ev.isRendida()), String.valueOf(notaCabecera), extra);

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

    private void escribir(PrintWriter out, String... campos) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                linea.append(SEP);
            }
            linea.append(escapar(campos[i]));
        }
        out.println(linea.toString());
    }

    private String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("%", "%25")
                .replace(SEP, "%3B")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private String desescapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("%3B", SEP).replace("%25", "%");
    }

    // ===================== CARGAR =====================

    @Override
    public ArrayList<Estudiante> cargar() throws IOException {
        advertencias.clear();
        ArrayList<Estudiante> resultado = new ArrayList<>();
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            return resultado;
        }

        Estudiante estudiante = null;
        Ciclo ciclo = null;
        Curso curso = null;
        Continua ultimaContinua = null;

        try (Scanner in = new Scanner(new FileReader(archivo, StandardCharsets.UTF_8))) {
            int numLinea = 0;
            while (in.hasNextLine()) {
                String linea = in.nextLine();
                numLinea++;
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] p = linea.split(SEP, -1);
                for (int i = 0; i < p.length; i++) {
                    p[i] = desescapar(p[i]);
                }
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
                                curso.agregarEvaluacion(ev);
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
                            advertencias.add("Línea " + numLinea + " ignorada: tipo desconocido.");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    advertencias.add("Línea " + numLinea + " ignorada: faltan campos.");
                } catch (IllegalArgumentException | DateTimeParseException
                         | PesoInvalidoException | NotaInvalidaException e) {
                    advertencias.add("Línea " + numLinea + " ignorada: " + e.getMessage());
                }
            }
        }
        return resultado;
    }

    private Evaluacion leerEvaluacion(String[] p) throws NotaInvalidaException {
        String tipo = p[1];
        double peso = Double.parseDouble(p[3]);
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
            default:
                throw new IllegalArgumentException("Tipo de evaluación desconocido: " + tipo);
        }

        // La fecha se guarda como AAAA-MM-DD, o SIN_FECHA ("-") si no tiene
        String fecha = p[4];
        if (!fecha.equals(SIN_FECHA)) {
            ev.setFecha(LocalDate.parse(fecha));
        }

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
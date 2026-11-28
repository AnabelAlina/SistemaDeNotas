package pruebas;

import app.GestorNotas;
import evaluaciones.Continua;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;
import evaluaciones.TrabajoContinua;
import excepciones.PesoInvalidoException;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
import modelo.Persona;
import persistencia.RepositorioArchivo;

// Pruebas del modelo: validaciones, plan de estudios en Ciclo,
// curso manual y sustitutorio con peso 0. Ejecutar y revisar que todo diga OK.
public class PruebaModelo {
    private static int fallos = 0;

    private static void verificar(String descripcion, boolean condicion) {
        if (!condicion) fallos++;
        System.out.println((condicion ? "OK    " : "FALLO ") + descripcion);
    }

    private static void verificar(String descripcion, double esperado, double obtenido) {
        verificar(descripcion + " (esperado " + esperado + ", obtenido " + obtenido + ")",
                Math.abs(esperado - obtenido) < 0.0001);
    }

    private static Estudiante crearEstudiante(String dni, String correo, String cui) {
        return new Estudiante("Anabel", "Quispe", dni, correo, cui, "Ingeniería de Sistemas");
    }

    // Devuelve true si crear el estudiante con esos datos lanza IllegalArgumentException
    private static boolean rechaza(String dni, String correo, String cui) {
        try {
            crearEstudiante(dni, correo, cui);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        // --- Validaciones de Persona / Estudiante ---
        verificar("estudiante válido se crea", !rechaza("71234567", "aquispe@unsa.edu.pe", "20261234"));
        verificar("DNI de 7 dígitos se rechaza", rechaza("7123456", "aquispe@unsa.edu.pe", "20261234"));
        verificar("DNI con letras se rechaza", rechaza("7123456A", "aquispe@unsa.edu.pe", "20261234"));
        verificar("DNI vacío se rechaza", rechaza("", "aquispe@unsa.edu.pe", "20261234"));
        verificar("CUI de 7 cifras se rechaza", rechaza("71234567", "aquispe@unsa.edu.pe", "2026123"));
        verificar("CUI con letras se rechaza", rechaza("71234567", "aquispe@unsa.edu.pe", "2026ABCD"));
        verificar("correo gmail se rechaza", rechaza("71234567", "aquispe@gmail.com", "20261234"));
        verificar("correo sin @ se rechaza", rechaza("71234567", "aquispe.unsa.edu.pe", "20261234"));
        verificar("Persona.validarDni acepta 8 dígitos", Persona.validarDni(" 71234567 ").equals("71234567"));

        // --- Ciclos ---
        Estudiante est = crearEstudiante("71234567", "aquispe@unsa.edu.pe", "20261234");
        est.agregarCiclo(new Ciclo("2026-B", 2026));
        boolean cicloRepetido = false;
        try {
            est.agregarCiclo(new Ciclo("2026-B", 2026));
        } catch (IllegalArgumentException e) {
            cicloRepetido = true;
        }
        verificar("ciclo repetido se rechaza", cicloRepetido);

        // --- Cursos predeterminados ---
        Ciclo ciclo = est.getCicloActual();
        int agregados = ciclo.agregarCursosPredeterminados(14);
        verificar("se cargan 7 cursos predeterminados", 7, agregados);
        int creditos = 0;
        boolean seis = true;
        for (Curso c : ciclo.getCursos()) {
            creditos += c.getCreditos();
            if (c.getEvaluaciones().size() != 6) seis = false;
        }
        verificar("los cursos suman 24 créditos", 24, creditos);
        verificar("cada curso trae 6 evaluaciones", seis);
        verificar("cargar de nuevo no duplica", 0, ciclo.agregarCursosPredeterminados(14));

        // --- Curso manual ---
        Curso manual = ciclo.agregarCursoManual("9999999", "Curso de prueba", 3, 12,
                new double[]{10, 10, 10}, new double[]{20, 25, 25});
        verificar("curso manual con pesos que suman 100 se agrega", manual.getPesoTotal(), 100);
        boolean rechazado = false;
        try {
            ciclo.agregarCursoManual("8888888", "Curso malo", 3, 12,
                    new double[]{10, 10, 10}, new double[]{20, 25, 30});
        } catch (PesoInvalidoException e) {
            rechazado = true;
        }
        verificar("curso manual con pesos que no suman 100 se rechaza", rechazado);
        verificar("el curso rechazado no quedó en el ciclo", ciclo.buscarCurso("8888888") == null);

        // --- Sustitutorio con peso 0 ---
        Curso curso = ciclo.buscarCurso("2501208");
        verificar("al inicio no hay sustitutorio", curso.getSustitutorio() == null);
        curso.agregarEvaluacion(new ExamenSustitutorio());
        verificar("el sustitutorio (peso 0) se acepta", curso.getSustitutorio() != null);
        verificar("el sustitutorio no cambia el 100%", curso.getPesoTotal(), 100);
        boolean segundo = false;
        try {
            curso.agregarEvaluacion(new ExamenSustitutorio());
        } catch (PesoInvalidoException e) {
            segundo = true;
        }
        verificar("un segundo sustitutorio se rechaza", segundo);
        boolean pesoCero = false;
        try {
            curso.agregarEvaluacion(new Examen(1, 0));
        } catch (PesoInvalidoException e) {
            pesoCero = true;
        }
        verificar("un Examen con peso 0 sigue rechazado", pesoCero);

        // --- Borrar y corregir (PR 1) ---
        verificar("quitarCurso de uno que existe devuelve true", ciclo.quitarCurso("9999999"));
        verificar("el curso quitado ya no está en el ciclo", ciclo.buscarCurso("9999999") == null);
        verificar("quitarCurso de uno que no existe devuelve false", !ciclo.quitarCurso("0000000"));

        verificar("quitarCiclo de uno que no existe devuelve false", !est.quitarCiclo("1999-Z"));
        est.agregarCiclo(new Ciclo("2027-A", 2027));
        verificar("quitarCiclo de uno que existe devuelve true", est.quitarCiclo("2027-A"));
        verificar("el ciclo quitado ya no está", est.buscarCiclo("2027-A") == null);

        Curso conMeta = ciclo.buscarCurso("2501208");
        conMeta.setMetaPromedio(15);
        verificar("setMetaPromedio(15) cambia la meta", 15, conMeta.getMetaPromedio());
        boolean metaMala = false;
        try {
            conMeta.setMetaPromedio(25);
        } catch (IllegalArgumentException e) {
            metaMala = true;
        }
        verificar("setMetaPromedio(25) lanza IllegalArgumentException", metaMala);
        verificar("la meta no cambió tras el error", 15, conMeta.getMetaPromedio());

        Continua continua = new Continua(1, 20);
        TrabajoContinua trabajo = new TrabajoContinua("Informe", 40, false);
        continua.agregarTrabajo(trabajo);
        verificar("quitarTrabajo de uno que existe devuelve true", continua.quitarTrabajo(trabajo));
        verificar("el peso interno vuelve a 0", 0, continua.getPesoInterno());
        verificar("quitarTrabajo de uno que no está devuelve false", !continua.quitarTrabajo(trabajo));

        // --- GestorNotas sin consola (PR 1) ---
        GestorNotas gestor = new GestorNotas(new RepositorioArchivo("datos/prueba_modelo.txt"));
        Estudiante g1 = crearEstudiante("71234567", "aquispe@unsa.edu.pe", "20261234");
        gestor.agregarEstudiante(g1);
        verificar("agregarEstudiante registra al estudiante", gestor.getEstudiantes().size() == 1);
        boolean cuiRep = false;
        try {
            gestor.agregarEstudiante(crearEstudiante("70000001", "otra@unsa.edu.pe", "20261234"));
        } catch (IllegalArgumentException e) {
            cuiRep = e.getMessage().contains("20261234");
        }
        verificar("CUI repetido lanza IllegalArgumentException con el CUI", cuiRep);
        boolean dniRep = false;
        try {
            gestor.agregarEstudiante(crearEstudiante("71234567", "otra@unsa.edu.pe", "20269999"));
        } catch (IllegalArgumentException e) {
            dniRep = e.getMessage().contains("71234567");
        }
        verificar("DNI repetido lanza IllegalArgumentException con el DNI", dniRep);
        verificar("quitarEstudiante de uno que no existe devuelve false", !gestor.quitarEstudiante("11111111"));
        verificar("quitarEstudiante de uno que existe devuelve true", gestor.quitarEstudiante("20261234"));
        verificar("ya no quedan estudiantes y no hay actual", gestor.getEstudiantes().isEmpty() && gestor.getActual() == null);
        verificar("el método viejo registrarEstudiante sigue devolviendo true", gestor.registrarEstudiante(g1));
        verificar("el método viejo devuelve false si está repetido", !gestor.registrarEstudiante(g1));

        System.out.println(fallos == 0 ? "\nTodas las pruebas pasaron." : "\nHubo " + fallos + " fallo(s).");
    }
}
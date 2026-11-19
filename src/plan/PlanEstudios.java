package plan;

import java.util.ArrayList;
import evaluaciones.Continua;
import evaluaciones.Examen;
import excepciones.PesoInvalidoException;
import modelo.Curso;

// Estudiante 2 - Ayelen Choquecota - Catalogo de cursos predeterminados (Primer anio, Segundo semestre, UNSA)
// con sus creditos y los porcentajes de cada Examen y cada Continua segun el silabo, y la forma de
// crear un Curso ya armado con sus 3 continuas y 3 examenes.
// Los porcentajes son sobre la nota final del curso; cada curso suma 100.
//
// NOTA para Estudiante 1 y Estudiante 3 (solo si hace falta):
//   - Para el ciclo con los cursos predeterminados:
//       for (CursoPlan p : PlanEstudios.getCursosPrimerAnioSegundoSemestre()) {
//           ciclo.agregarCurso(PlanEstudios.crearCurso(p, meta));   // atrapar PesoInvalidoException
//       }
//   - Para un curso manual (repitente / otros anios): pedir codigo, nombre, creditos, meta y los 3 % de
//     examen y los 3 % de continua del silabo, y llamar PlanEstudios.crearCurso(codigo, nombre, ...).
//   - El curso queda con 6 evaluaciones en este orden: Continua 1, Examen 1, Continua 2, Examen 2,
//     Continua 3, Examen 3.
public class PlanEstudios {

    // Datos de un curso del plan: codigo, nombre, creditos y % de cada examen y continua
    public static class CursoPlan {
        private String codigo;
        private String nombre;
        private int creditos;
        private double[] pesosExamen;   // Examen 1, 2, 3
        private double[] pesosContinua; // Continua 1, 2, 3

        public CursoPlan(String codigo, String nombre, int creditos, double[] pesosExamen, double[] pesosContinua) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.creditos = creditos;
            this.pesosExamen = pesosExamen.clone();
            this.pesosContinua = pesosContinua.clone();
        }

        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public int getCreditos() { return creditos; }
        public double[] getPesosExamen() { return pesosExamen.clone(); }
        public double[] getPesosContinua() { return pesosContinua.clone(); }

        @Override
        public String toString() {
            return codigo + " - " + nombre + " (" + creditos + " creditos)";
        }
    }

    private static final ArrayList<CursoPlan> PRIMER_ANIO_SEGUNDO_SEMESTRE = new ArrayList<>();

    static {
        // codigo, nombre, creditos, {examen 1,2,3}, {continua 1,2,3}
        PRIMER_ANIO_SEGUNDO_SEMESTRE.add(new CursoPlan("2501208", "Cálculo en una Variable", 5,
            new double[]{16, 16, 18}, new double[]{16, 16, 18}));
        PRIMER_ANIO_SEGUNDO_SEMESTRE.add(new CursoPlan("2501209", "Estructuras Discretas II", 3,
            new double[]{10, 10, 10}, new double[]{20, 25, 25}));
        PRIMER_ANIO_SEGUNDO_SEMESTRE.add(new CursoPlan("2501210", "Introducción al Desarrollo Web", 4,
            new double[]{10, 10, 20}, new double[]{15, 15, 30}));
        PRIMER_ANIO_SEGUNDO_SEMESTRE.add(new CursoPlan("2501211", "Desarrollo de Software Orientado a Objetos", 4,
            new double[]{15, 20, 20}, new double[]{10, 10, 25}));
        PRIMER_ANIO_SEGUNDO_SEMESTRE.add(new CursoPlan("2501212", "Lingüística, Comprensión y Redacción Académica", 3,
            new double[]{10, 10, 10}, new double[]{20, 25, 25}));
        PRIMER_ANIO_SEGUNDO_SEMESTRE.add(new CursoPlan("2501213", "Realidad Nacional", 2,
            new double[]{10, 10, 10}, new double[]{20, 25, 25}));
        PRIMER_ANIO_SEGUNDO_SEMESTRE.add(new CursoPlan("2501214", "Taller de Narrativa Digital", 3,
            new double[]{10, 15, 15}, new double[]{15, 20, 25}));
    }

    // Copia de la lista de los 7 cursos del semestre
    public static ArrayList<CursoPlan> getCursosPrimerAnioSegundoSemestre() {
        return new ArrayList<>(PRIMER_ANIO_SEGUNDO_SEMESTRE);
    }

    // Crea el Curso de un curso del plan, ya con sus 3 continuas y 3 examenes
    public static Curso crearCurso(CursoPlan plan, double metaPromedio) throws PesoInvalidoException {
        return crearCurso(plan.getCodigo(), plan.getNombre(), plan.getCreditos(), metaPromedio,
            plan.getPesosExamen(), plan.getPesosContinua());
    }

    // Crea un Curso (tambien para cursos manuales) con 3 continuas y 3 examenes con los % dados
    public static Curso crearCurso(String codigo, String nombre, int creditos, double metaPromedio,
                                   double[] pesosExamen, double[] pesosContinua) throws PesoInvalidoException {
        if (pesosExamen.length != 3 || pesosContinua.length != 3) {
            throw new IllegalArgumentException("Se necesitan 3 porcentajes de examen y 3 de continua.");
        }
        Curso curso = new Curso(codigo, nombre, creditos, metaPromedio);
        for (int i = 0; i < 3; i++) {
            curso.agregarEvaluacion(new Continua(i + 1, pesosContinua[i]));
            curso.agregarEvaluacion(new Examen(i + 1, pesosExamen[i]));
        }
        curso.verificarPesos(); // los 6 porcentajes deben sumar 100
        return curso;
    }
}

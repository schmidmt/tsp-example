import com.ampl.*;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.*;

public class Tsp {
    final String model = """
            set NODES;
            param hpos {NODES};
            param vpos {NODES};
                        
            set PAIRS := {i in NODES, j in NODES: i != j};
                        
            param distance {(i,j) in PAIRS}
               := sqrt((hpos[j]-hpos[i])**2 + (vpos[j]-vpos[i])**2);
                        
            var X {PAIRS} binary;
                        
            minimize Tour_Length: sum {(i,j) in PAIRS} distance[i,j] * X[i,j];
                        
            subject to Has_In {i in NODES}:
                sum {j in NODES: i != j} X[j, i] = 1;
                
            subject to Has_Out {i in NODES}:
                sum {j in NODES: i != j} X[i, j] = 1;
                
            param nSubtours >= 0 integer;
            set SUB {1..nSubtours} within NODES;

            subject to Subtour_Elimination {k in 1..nSubtours}:
                  sum {(i, j) in PAIRS : i in SUB[k] and j in NODES diff SUB[k]} X[i, j] >= 1;
            """;
    AMPL ampl;
    File model_file;
    DataFrame dataframe;

    public Tsp(DataFrame new_dataframe) throws IOException {
        model_file = File.createTempFile("model_", ".mod");
        BufferedWriter model_writer = new BufferedWriter(new FileWriter(model_file));
        model_writer.write(model);
        model_writer.close();
        dataframe = new_dataframe;
    }

    private static @NotNull List<String> traverse(String start, Set<Pair<String, String>> arcs, Set<String> nodes) {
        List<String> tour = new LinkedList<>();
        HashSet<String> seen = new HashSet<>();

        tour.add(start);
        seen.add(start);

        String cur = start;

        while (true) {
            HashSet<Pair<String, String>> relevant_arcs = new HashSet<>();
            for (Pair<String, String> arc : arcs) {
                if (arc.getValue0().equals(cur)) {
                    relevant_arcs.add(arc);
                }
            }
            // Quit if there's no more arcs to traverse.
            if (relevant_arcs.isEmpty()) {
                break;
            }
            Pair<String, String> next_arc = relevant_arcs.iterator().next();
            String next = next_arc.getValue1();
            if (seen.contains(next)) {
                break;
            }
            cur = next;
            tour.add(cur);
            seen.add(cur);
        }

        return tour;
    }

    private static @NotNull List<List<String>> findSubtours(Set<Pair<String, String>> arcs, Set<String> nodes) {
        List<List<String>> subtours = new LinkedList<>();
        HashSet<String> to_traverse = new HashSet<>(nodes);

        while (!to_traverse.isEmpty()) {
            List<String> subtour = traverse(to_traverse.iterator().next(), arcs, to_traverse);
            subtour.forEach(to_traverse::remove);
            subtours.add(subtour);
        }
        return subtours;
    }

    private void reset() throws IOException {
        Environment env = new Environment("/Users/schmidmt/Downloads/ampl_macos64");

        ampl = new AMPL(env);
        ampl.read(model_file.getAbsolutePath());
        ampl.setData(dataframe, "NODES");
        ampl.setOption("solver", "cplex");
    }

    public Optional<List<String>> solve() throws IOException {
        List<List<String>> found_subtours = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            reset();
            com.ampl.Set sub = ampl.getSet("SUB");
            Parameter nsubtours = ampl.getParameter("nSubtours");
            nsubtours.set(found_subtours.size());

            for (int j = 0; j < found_subtours.size(); j++) {
                String[] subtour_arr = found_subtours.get(j).toArray(new String[0]);
                DataFrame df = new DataFrame(1, "SUB");
                df.setColumn("SUB", subtour_arr);
                SetInstance si = sub.get(j + 1);
                si.setValues(df);
            }

            ampl.solve();
            DataFrame indicators = ampl.getVariable("X").getValues();

            HashSet<Pair<String, String>> arcs = new HashSet<>();
            HashSet<String> nodes = new HashSet<>();
            for (Object[] row : indicators) {
                String a = (String) row[0];
                String b = (String) row[1];
                boolean selected = ((double) row[2]) == 1.0;
                if (selected) {
                    arcs.add(new Pair<>(a, b));
                }
                nodes.add(a);
                nodes.add(b);
            }

            List<List<String>> subtours = findSubtours(arcs, nodes);

            if (subtours.size() > 1) {
                System.out.println("Found Subtours:");
                System.out.println(subtours);
                found_subtours.addAll(subtours);
                System.out.println("---------- Restarting ----------");
            } else {
                return Optional.of(subtours.get(0));
            }

        }

        return Optional.empty();
    }

}

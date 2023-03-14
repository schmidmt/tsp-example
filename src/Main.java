import com.ampl.DataFrame;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


public class Main {


    public static void main(String[] args) throws IOException {
        DataFrame stars = new DataFrame(1, "NODES", "hpos", "vpos");
        stars.addRow("ω-Gem", 105.04021666666667, 24.215444444444444);
        stars.addRow("χ-Gem", 120.05863888888888, 27.794416666666667);
        stars.addRow("φ-Gem", 105.89162222222222, 26.76586111111111);
        stars.addRow("υ-Gem", 105.59871388888888, 26.896);
        stars.addRow("τ-Gem", 105.1856638888889, 30.24527777777778);
        stars.addRow("σ-Gem", 105.72185833333333, 28.884083333333333);
        stars.addRow("ρ-Gem", 105.48516944444445, 31.784083333333335);
        stars.addRow("π-Gem", 105.79176111111111, 33.41577777777778);
        stars.addRow("ο-Gem", 105.65276666666668, 34.58463888888889);
        stars.addRow("ξ-Gem", 90.75484166666666, 12.896055555555556);
        stars.addRow("ν-Gem-A", 90.48271944444444, 20.212166666666665);
        stars.addRow("ν-Gem-B", 90.48158333333333, 20.23888888888889);
        stars.addRow("μ-Gem", 90.38266388888889, 22.51386111111111);
        stars.addRow("λ-Gem", 105.30155833333333, 16.540472222222224);
        stars.addRow("κ-Gem", 105.74079722222223, 24.398138888888887);
        stars.addRow("ι-Gem", 105.42880000000001, 27.79827777777778);
        stars.addRow("θ-Gem", 90.87981666666666, 33.96136111111112);
        stars.addRow("η-Gem", 90.24797222222222, 22.506833333333333);
        stars.addRow("ζ-Gem", 105.06848333333333, 20.570305555555557);
        stars.addRow("ε-Gem", 90.73220277777779, 25.13116666666667);
        stars.addRow("δ-Gem", 105.3353861111111, 21.982333333333333);
        stars.addRow("γ-Gem", 90.62852777777778, 16.399416666666667);
        stars.addRow("Pollux", 105.75537777777778, 28.026305555555556);
        stars.addRow("Castor-C", 105.57710555555555, 31.86975);
        stars.addRow("Castor-A", 105.57666666666667, 31.888638888888888);

        Tsp tsp = new Tsp(stars);

        Optional<List<String>> soln = tsp.solve();
        if (soln.isPresent()) {
            System.out.println("Found Solution:");
            System.out.println(soln.get());
        } else {
            System.out.println("No solution found...");
        }
    }
}
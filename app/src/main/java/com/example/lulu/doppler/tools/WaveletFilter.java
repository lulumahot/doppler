package com.example.lulu.doppler.tools;

public class WaveletFilter {

    // Daubechies 10 filters :
// TODO : essayer de mettre des valeurs plus précises pour les filtres (0.0000 -> -1.3264e-05 en réalité)
    // high-pass filter decomposition
    static double HI_D[] = {-0.0267, 0.1882, -0.5272, 0.6885, -0.2812, -0.2498, 0.1959, 0.1274,
            -0.0931, -0.0714, 0.0295, 0.0332, -0.0036, -0.0107, -0.0014, 0.0020, 0.0007, -0.0001,
            -0.0001, -0.0000};
    // low-pass filter decomposition
    static double LO_D[] = {-0.0000, 0.0001, -0.0001, -0.0007, 0.0020, 0.0014, -0.0107, 0.0036,
            0.0332, -0.0295, -0.0714, 0.0931, 0.1274, -0.1959, -0.2498, 0.2812, 0.6885, 0.5272,
            0.1882, 0.0267};

    // high-pass filter reconstruction
    static double HI_R[] = {-0.0000, -0.0001, -0.0001, 0.0007, 0.0020, -0.0014, -0.0107, -0.0036,
            0.0332, 0.0295, -0.0714, -0.0931, 0.1274, 0.1959, -0.2498, -0.2812, 0.6885, -0.5272,
            0.1882, -0.0267};
    // low-pass filter reconstruction
    static double LO_R[] = {0.0267, 0.1882, 0.5272, 0.6885, 0.2812, -0.2498, -0.1959, 0.1274,
            0.0931, -0.0714, -0.0295, 0.0332, 0.0036, -0.0107, 0.0014, 0.0020, -0.0007, -0.0001,
            0.0001, -0.0000};

    static short level = 4;

    static int [] tab_size = new int[level];

    public static void filter(double[] buffer) {
        // TODO : Implement filter treatment
        System.out.println("size LO_D : %d \n" + LO_D.length);
        //double LO_D_reverse[] = reverseTab(LO_D);
        //double HI_D_reverse[] = reverseTab(HI_D);

        // nbEch dans le buffer => = buffer.length
        int nbEch = buffer.length;
        //double nb_fragments =  nbEch / LO_D.length;

        double new_buffer[] = new double[buffer.length];

    //for level here
        tab_size[0/*level_iteration*/] = buffer.length;

        double[] buffer_dec = decomposition(buffer, HI_D, LO_D);
        //passer les hautes et basses fréquences séparemment
        double [] buffer_low = new double [buffer_dec.length/2];
        double [] buffer_high = new double [buffer_dec.length/2];
        //on remplie les tableaux pour la reconstruction
        //tableau des hautes fréquences à garder à part après chaque décomposition
        for (int i = 0; i < buffer_dec.length/2; i++) {
            buffer_low[i] = buffer_dec[i];
            buffer_high[i] = buffer_dec[i + buffer_dec.length/2];
        }

        double[] buffer_rec = reconstruction(buffer_low,buffer_high, HI_D, LO_D,tab_size[0/*level_iteration*/]);


        // test convolve
        /*double x[] = {5, 4, 3, 2, 1, 1, 2, 3, 4, 5};
        double h[] = {-0.1294, 0.2241, 0.8365, 0.4830};
        double y[] = convolve(x, h);
        for (int i=0; i< y.length; i++) {
            System.out.print(y[i] + " ");
        }*/

    }

    static double[] decomposition(double[] buffer, double HI_D [], double LO_D[]) {
        double [] tmp_lo = convolve(buffer,LO_D);
        double [] tmp_hi = convolve(buffer,HI_D);
        int size = tmp_lo.length;
        double result [] = new double[size];

        for (int i = 1; i < size/2; i++) {
            result[i-1] = tmp_lo[(i*2)-1]; //sous-echantillonnage low
            result[i+(size/2)-1] = tmp_hi[(i*2)-1]; //high
        }
        return result;
    }

    static double[] reconstruction(double[] buffer_low, double [] buffer_high, double HI_D [], double LO_D[], int current_size) {
        double [] buffer_low_up = new double[buffer_low.length * 2];
        double [] buffer_high_up =  new double[buffer_high.length * 2];

        //sur echantillonnage
        for (int i = 0; i < buffer_low_up.length; i++){
            buffer_high_up[i] = 0.0;
            buffer_low_up[i] = 0.0;
        }
        for (int i = 0; i < buffer_low.length; i++){
            buffer_high_up[i*2] = buffer_high[i];
            buffer_low_up[i*2] = buffer_low[i];
        }

        double tmp_lo [] = convolve(buffer_low_up,LO_R);
        double tmp_hi [] = convolve(buffer_high_up,HI_R);
        //le nombre de valeur à supprimer avant et après la convolution + somme
        int nb_val = ((tmp_lo.length-1)-current_size)/2;
        double result [] = new double [tmp_hi.length - (nb_val*2)];

        for (int i = 0; i < result.length; i++) {
            result [i] = tmp_lo[i+nb_val+1] + tmp_hi[i+nb_val+1];
        }

        return result;
    }


    /*
    function who make convolution between two array of short
    */
    static double[] convolve(double[] buffer, double[] filter) {
        double result [] = new double[buffer.length + filter.length -1];
        for (int i= 0; i< result.length; i++) {
            result[i] = 0.0;
        }

        for (int n=0; n< buffer.length; n++) {
            int i=n;
            for (int m=0; m< filter.length; m++) {
                result[i] += buffer[n] * filter[m];
                i ++;
            }
        }
        return result;
    }

    /* reverse an array
     */
    static double[] reverseTab(double[] filter) {
        double tab[] = new double[filter.length];

        for (int i=0; i< filter.length; i++)
            tab[filter.length - i] = filter[i];

        return tab ;
    }

    /* get portion of a short array of size size
     */
    static double[] getPortion(double[] buffer, int startId, int size) {
        double tab[] = new double[size];

        for (int i=0; i<size; i++) {
            tab[i] = buffer[startId +i];
        }
        return tab;
    }

    /*
     function product between two array (of same size)
     */
    static double[] productElemByElem(double[] buffer, double[] filter) {
        double result [] = new double[buffer.length + filter.length -1];
        for (int n=0; n< buffer.length; n++) {
            result[n] = buffer[n] * filter[n];
        }
        return result;
    }

}

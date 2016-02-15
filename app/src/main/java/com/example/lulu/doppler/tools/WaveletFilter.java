package com.example.lulu.doppler.tools;

public class WaveletFilter {

    // Daubechies 10 filters :
// TODO : essayer de mettre des valeurs plus pr�cises pour les filtres (0.0000 -> -1.3264e-05 en r�alit�)
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

    public static void filter(short[] buff) {

        double buffer[] = new double[buff.length];
        for (int k = 0; k < buff.length; k++) {
            buffer[k] = buff[k];
        }

        int current_size = buffer.length;

        double new_buffer[] = new double[buffer.length];

        double buffer_high1 [];
        double buffer_high2 [];
        double buffer_high3 [];
        double buffer_high4 [];

        double buffer_low_final [] = new double[1];
        int taille_buffer_low_final = 0;

        // On peut se servir du premier élément pour décrire la longueur des sous tableaux
        double tab_conv_High_lvl[][] = new double[4][buffer.length * 2];

        //decomposition
        for (int j = 0; j < level; j++){
            double buffer_process [] = new double [current_size];

            tab_size[j] = current_size;

            double[] buffer_dec = decomposition(buffer, HI_D, LO_D);
            //passer les hautes et basses fr�quences s�paremment
            double[] buffer_low = new double[buffer_dec.length / 2];
            double[] buffer_high = new double[buffer_dec.length / 2];
            //on remplie les tableaux pour la reconstruction
            //tableau des hautes fr�quences � garder � part apr�s chaque d�composition
            for (int i = 0; i < buffer_dec.length / 2; i++) {
                buffer_low[i] = buffer_dec[i];
                buffer_high[i] = buffer_dec[i + buffer_dec.length / 2];
            }
            //stockage pour reconstruction

            tab_conv_High_lvl[j][0] = (double) buffer_high.length;
            for (int k = 0; k < buffer_high.length; k++) {
                tab_conv_High_lvl[j][k+1] = buffer_high[k];
            }
            if (j == 3) {
                //stockage reconstruction buffer low final + sur �chantillonnage
                buffer_low_final = new double [current_size*2];
                taille_buffer_low_final = buffer_low_final.length;
                for (int k = 0; k < current_size*2; k++){
                    buffer_low_final[k] = 0.0;
                }
                for (int k = 0; k < current_size; k++) {
                    buffer_low_final[k*2] = buffer_low[k];
                }
            }
            //m�j size pour calcul reconstruction plus tard
            current_size = buffer_low.length;

            //double[] buffer_rec = reconstruction(buffer_low, buffer_high, HI_R, LO_R, tab_size[j]);
        }
        //reconstruction
        double [] buffer_reconstruit = new double[1];
        for (int j = level-1; j >= 0; j--){
            double [] buffer_high;
            double [] buffer_low;
            //recuperation bon buffer + le sur �chantillonnage se fait dans la fonction reconstruction
            buffer_high = new double[(int)tab_conv_High_lvl[j][0]];
            if (j == 3) {
                buffer_low = new double[taille_buffer_low_final]; //de la meme taille que buffer_high donc
                for (int k = 0; k < tab_conv_High_lvl[j][0]; k++) {
                    buffer_high[k] = tab_conv_High_lvl[j][k+1];
                    buffer_low[k] = buffer_low_final[k];
                }
            } else {
                buffer_low = new double[buffer_reconstruit.length]; //de la meme taille que buffer_high donc
                for (int k = 0; k < tab_conv_High_lvl[j][0]; k++) {
                    buffer_high[k] = tab_conv_High_lvl[j][k+1];
                    buffer_low[k] = buffer_low_final[k];
                }
            }
            buffer_reconstruit = reconstruction(buffer_low,buffer_high,HI_R, LO_R, tab_size[j]);
        }
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
        //le nombre de valeur � supprimer avant et apr�s la convolution + somme
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

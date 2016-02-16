package com.example.lulu.doppler.tools;

import java.util.ArrayList;
import java.util.List;

public class WaveletFilter {

    // Daubechies 10 filters :
    static double LO_D[] = {Math.pow((double)-1.3264, (double)-05), Math.pow((double)9.3589,
            (double)-05), -1.1647e-04, -6.8586e-04, 0.0020, 0.0014, -0.0107, 0.0036, 0.0332, -0.0295,
            -0.0714, 0.0931, 0.1274, -0.1959, -0.2498, 0.2812, 0.6885, 0.5272, 0.1882, 0.0267 };

    static double HI_D[] = {-0.0267,  0.1882, -0.5272,  0.6885, -0.2812, -0.2498,  0.1959,  0.1274,
            -0.0931, -0.0714,  0.0295,  0.0332, -0.0036, -0.0107, -0.0014,  0.0020, 6.8586e-04,
            -1.1647e-04, Math.pow((double)-9.3589, (double)-05), Math.pow((double)-1.3264, (double)-05)};

    static double LO_R[] = {0.0267,  0.1882,  0.5272,  0.6885,  0.2812, -0.2498, -0.1959,  0.1274,
            0.0931, -0.0714, -0.0295,  0.0332,  0.0036, -0.0107,  0.0014,  0.0020, -6.8586e-04,
            -1.1647e-04, Math.pow((double)9.3589, (double)-05), Math.pow((double)-1.3264, (double)-05)};

    static double HI_R[] = {Math.pow((double)-1.3264, (double)-05), Math.pow((double)-9.3589, (double)-05),
            -1.1647e-04, 6.8586e-04,  0.0020, -0.0014, -0.0107, -0.0036,  0.0332,  0.0295, -0.0714, -0.0931,
            0.1274,  0.1959, -0.2498, -0.2812,  0.6885, -0.5272,  0.1882, -0.0267};

    static short level = 4;
    static int [] tab_size = new int[level];

    public static short[] filter(short[] buff) {

        //double buffer[] = new double[buff.length];
        List<Double> buffer = new ArrayList<Double>();
        for (int k = 0; k < buff.length; k++) {
            buffer.add((double)buff[k]);
        }

        int current_size = buffer.size();

        //double buffer_low_final [] = new double[1];
        List<Double> buffer_low_final = new ArrayList<Double>();

        //int taille_buffer_low_final = 0;

        // On peut se servir du premier élément pour décrire la longueur des sous tableaux
        //double tab_conv_High_lvl[][] = new double[4][buffer.length * 2];
        List<List<Double>> tab_conv_High_lvl = new ArrayList<List<Double>>();
        // Ici on créé les 4 sous-tableaux pour la suite
        for(int k = 0; k < level; k++) {
            tab_conv_High_lvl.add(new ArrayList<Double>());
        }

        //decomposition
        for (int j = 0; j < level; j++){
            tab_size[j] = current_size;

            List<Double> buffer_dec = decomposition(buffer, HI_D, LO_D);
            //passer les hautes et basses fr�quences s�paremment
            //double[] buffer_low = new double[buffer_dec.length / 2];
            List<Double> buffer_low = new ArrayList<Double>();
            //double[] buffer_high = new double[buffer_dec.length / 2];
            List<Double> buffer_high = new ArrayList<Double>();
            //on remplie les tableaux pour la reconstruction
            //tableau des hautes fr�quences � garder � part apr�s chaque d�composition
            for (int i = 0; i < buffer_dec.size() / 2; i++) {
                //buffer_low[i] = buffer_dec[i];
                //buffer_high[i] = buffer_dec[i + buffer_dec.length / 2];
                buffer_low.add(buffer_dec.get(i));
                buffer_high.add(buffer_dec.get(i + buffer_dec.size() / 2));
            }
            //stockage pour reconstruction

            //tab_conv_High_lvl[j][0] = (double) buffer_high.length;
            tab_conv_High_lvl.get(j).set(0, (double) buffer_high.size());
            for (int k = 0; k < buffer_high.size(); k++) {
                tab_conv_High_lvl.get(j).add(buffer_high.get(k));
            }
            if (j == 3) {
                //stockage reconstruction buffer low final + sur �chantillonnage
                //buffer_low_final = new double [current_size*2];
                //taille_buffer_low_final = buffer_low_final.length;
                for (int k = 0; k < current_size*2; k++){
                    buffer_low_final.add(0.0);
                }
                for (int k = 0; k < current_size; k++) {
                    buffer_low_final.set(k*2, buffer_low.get(k));
                }
            }
            //m�j size pour calcul reconstruction plus tard
            current_size = buffer_low.size();

            //double[] buffer_rec = reconstruction(buffer_low, buffer_high, HI_R, LO_R, tab_size[j]);
        }
        //reconstruction
        //double [] buffer_reconstruit = new double[1];
        List<Double> buffer_reconstruit = new ArrayList<Double>();
        List<Double> buffer_high = new ArrayList<Double>();
        List<Double> buffer_low = new ArrayList<Double>();
        for (int j = level-1; j >= 0; j--){
            //double [] buffer_high;
            buffer_high.clear();
            //double [] buffer_low;
            buffer_low.clear();
            //recuperation bon buffer + le sur �chantillonnage se fait dans la fonction reconstruction
            //buffer_high = new double[(int)tab_conv_High_lvl[j][0]];
            //if (j == 3) {
            //buffer_low = new double[taille_buffer_low_final]; //de la meme taille que buffer_high donc
            for (int k = 0; k < tab_conv_High_lvl.get(j).get(0); k++) {
                //buffer_high[k] = tab_conv_High_lvl[j][k+1];
                //buffer_low[k] = buffer_low_final[k];
                buffer_high.add(tab_conv_High_lvl.get(j).get(k+1));
                buffer_low.add(buffer_low_final.get(k));
            }
            /*} else {
                //buffer_low = new double[buffer_reconstruit.length]; //de la meme taille que buffer_high donc
                for (int k = 0; k < tab_conv_High_lvl.get(j).get(0); k++) {
                    //buffer_high[k] = tab_conv_High_lvl[j][k+1];
                    //buffer_low[k] = buffer_low_final[k];
                    buffer_high.add(tab_conv_High_lvl.get(j).get(k+1));
                    buffer_low.add(buffer_low_final.get(k));
                }
            }*/
            buffer_reconstruit = reconstruction(buffer_low,buffer_high,HI_R, LO_R, tab_size[j]);
        }
        short buffer_res[] = new short[buffer_reconstruit.size()];
        for (int k = 0; k < buffer_res.length; k++) {
            buffer_res[k] = (short) Math.round(buffer_reconstruit.get(k));
        }

        return buffer_res;
    }

    static List<Double> decomposition(List<Double> buff, double HI_D [], double LO_D[]) {
        //maybe revoir les classes mais pour l'instant ...
        double buffer[] = new double[buff.size()];
        for (int k = 0; k < buff.size(); k++) {
            buffer[k] = buff.get(k);
        }

        double [] tmp_lo = convolve(buffer,LO_D);
        double [] tmp_hi = convolve(buffer,HI_D);
        int size = tmp_lo.length;
        double result [] = new double[size];

        for (int i = 1; i < size/2; i++) {
            result[i-1] = tmp_lo[(i*2)-1]; //sous-echantillonnage low
            result[i+(size/2)-1] = tmp_hi[(i*2)-1]; //high
        }
        List<Double> res = new ArrayList<Double>();
        for (int k = 0; k < result.length; k++) {
            res.add(result[k]);
        }
        return res;
    }

    static List<Double> reconstruction(List<Double> buff_low, List<Double> buff_high, double HI_D [], double LO_D[], int current_size) {
        double [] buffer_low_up = new double[buff_low.size() * 2];
        double [] buffer_high_up =  new double[buff_high.size() * 2];

        double buffer_high[] = new double[buff_high.size()];
        double buffer_low[] = new double[buff_low.size()];
        for (int k = 0; k < buff_high.size(); k++) {
            buffer_low[k] = buff_high.get(k);
            buffer_high[k] = buff_low.get(k);
        }

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
        //le nombre de valeur a supprimer avant et apres la convolution + somme
        int nb_val = ((tmp_lo.length-1)-current_size)/2;
        double result [] = new double [tmp_hi.length - (nb_val*2)];

        for (int i = 0; i < result.length; i++) {
            result [i] = tmp_lo[i+nb_val+1] + tmp_hi[i+nb_val+1];
        }

        List<Double> res = new ArrayList<Double>();
        for (int k = 0; k < result.length; k++) {
            res.add(result[k]);
        }
        return res;
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

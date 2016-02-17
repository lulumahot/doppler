package com.example.lulu.doppler.tools;

import java.util.ArrayList;
import java.util.List;

public class WaveletFilter {

    
    // Daubechies 10 filters :
    public static double LO_D[] = {-1.3264e-05, 9.3589e-05, -1.1647e-04, -6.8586e-04, 0.0020, 0.0014, -0.0107, 0.0036, 0.0332, -0.0295,
            -0.0714, 0.0931, 0.1274, -0.1959, -0.2498, 0.2812, 0.6885, 0.5272, 0.1882, 0.0267 };

    public static double HI_D[] = {-0.0267,  0.1882, -0.5272,  0.6885, -0.2812, -0.2498,  0.1959,  0.1274,
            -0.0931, -0.0714,  0.0295,  0.0332, -0.0036, -0.0107, -0.0014,  0.0020, 6.8586e-04,
            -1.1647e-04,-9.3589e-05, -1.3264e-05};

    static double LO_R[] = {0.0267,  0.1882,  0.5272,  0.6885,  0.2812, -0.2498, -0.1959,  0.1274,
            0.0931, -0.0714, -0.0295,  0.0332,  0.0036, -0.0107,  0.0014,  0.0020, -6.8586e-04,
            -1.1647e-04, 9.3589e-05,-1.3264e-05};

    static double HI_R[] = {-1.3264e-05, -9.3589e-05,
            -1.1647e-04, 6.8586e-04,  0.0020, -0.0014, -0.0107, -0.0036,  0.0332,  0.0295, -0.0714, -0.0931,
            0.1274,  0.1959, -0.2498, -0.2812,  0.6885, -0.5272,  0.1882, -0.0267};

    static short level = 4;
    static int lvlech = 1;
    static int [] tab_size = new int[level];

    public static short[] filter(short[] buff) {

        //double buffer[] = new double[buff.length];
        List<Double> buffer = new ArrayList();
        for (int k = 0; k < buff.length; k=k+lvlech) {
            buffer.add((double)buff[k]);
        }
        //System.out.println("bdlra "+ buffer.size());
        int current_size = buffer.size();

        //double buffer_low_final [] = new double[1];
        List<Double> buffer_low_final = new ArrayList<Double>();

        //int taille_buffer_low_final = 0;

        // On peut se servir du premier Ã©lÃ©ment pour dÃ©crire la longueur des sous tableaux
        //double tab_conv_High_lvl[][] = new double[4][buffer.length * 2];
        List<List<Double>> tab_conv_High_lvl = new ArrayList<List<Double>>();
        // Ici on crÃ©Ã© les 4 sous-tableaux pour la suite
        for(int k = 0; k < level; k++) {
            tab_conv_High_lvl.add(new ArrayList<Double>());
        }

        //decomposition
        for (int j = 0; j < level; j++){
            tab_size[j] = current_size;

            List<Double> buffer_dec = decomposition(buffer, HI_D, LO_D);
            //passer les hautes et basses frï¿½quences sï¿½paremment
            List<Double> buffer_low = new ArrayList<Double>();
            List<Double> buffer_high = new ArrayList<Double>();
            //on remplie les tableaux pour la reconstruction
            //tableau des hautes frï¿½quences ï¿½ garder ï¿½ part aprï¿½s chaque dï¿½composition
            for (int i = 0; i < buffer_dec.size() / 2; i++) {
                buffer_low.add(buffer_dec.get(i));
                buffer_high.add(buffer_dec.get(i + buffer_dec.size() / 2));
            }
            //stockage pour reconstruction

            //tab_conv_High_lvl[j][0] = (double) buffer_high.length;
            tab_conv_High_lvl.get(j).add(0, (double) buffer_high.size() + 1);
            //System.out.println("\n taille : " + tab_conv_High_lvl.get(j).get(0));
            for (int k = 0; k < buffer_high.size(); k++) {
                tab_conv_High_lvl.get(j).add(buffer_high.get(k));
            }
            /*if (j == 3) {
                //stockage reconstruction buffer low final + sur ï¿½chantillonnage
                //buffer_low_final = new double [current_size*2];
                //taille_buffer_low_final = buffer_low_final.length;
                for (int k = 0; k < current_size*2; k++){
                    buffer_low_final.add(0.0);
                }
                for (int k = 0; k < current_size; k++) {
                    buffer_low_final.set(k*2, buffer_low.get(k));
                }
            }*/
            //mï¿½j size pour calcul reconstruction plus tard
            current_size = buffer_low.size();
            buffer = buffer_low;
            //double[] buffer_rec = reconstruction(buffer_low, buffer_high, HI_R, LO_R, tab_size[j]);
        }

        /*for (int j = 0 ; j < 4 ; j++){
            System.out.println("\nOrdre de decomposition : "+(j+1)+" taille finale : "+tab_conv_High_lvl.get(j).size());
            for(int k = 0 ; k < tab_conv_High_lvl.get(j).get(0) ; k++){
                System.out.print(tab_conv_High_lvl.get(j).get(k) + " ");
            }
        }*/

        //reconstruction
        //double [] buffer_reconstruit = new double[1];
        List<Double> buffer_reconstruit = new ArrayList<Double>();
        List<Double> buffer_high = new ArrayList<Double>();
        List<Double> buffer_low = new ArrayList<Double>();
        List<Double> a = new ArrayList<>();
        for (int j = level-1; j >= 0; j--){
            //double [] buffer_high;
            buffer_high.clear();
            //double [] buffer_low;
            buffer_low.clear();
            a.clear();
            //recuperation bon buffer + le sur ï¿½chantillonnage se fait dans la fonction reconstruction
            //buffer_high = new double[(int)tab_conv_High_lvl[j][0]];
            //if (j == 3) {
            //buffer_low = new double[taille_buffer_low_final]; //de la meme taille que buffer_high donc
            if(j==level-1) a=buffer; else a=buffer_reconstruit;
            for (int k = 1; k < tab_conv_High_lvl.get(j).get(0); k++) {
                //buffer_high[k] = tab_conv_High_lvl[j][k+1];
                //buffer_low[k] = buffer_low_final[k];
                buffer_high.add(tab_conv_High_lvl.get(j).get(k));
                buffer_low.add(a.get(k-1));
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
            /*System.out.println("\nordre : "+(j+1)+" High");
            for (int k =0 ; k < buffer_high.size() ; k++){
            	System.out.print(buffer_high.get(k)+" ");
            }
            System.out.println("\nordre : "+(j+1)+" Low");
            for (int k =0 ; k < buffer_low.size() ; k++){
            	System.out.print(buffer_low.get(k)+" ");
            }
            System.out.println("ok");*/
            System.out.println("\nordre : "+(j+1));
            buffer_reconstruit = reconstruction(buffer_low,buffer_high,HI_R, LO_R, tab_size[j]);
        }

        short buffer_res[] = new short[buff.length];
        for(int i = 0 ; i < buff.length ; i++)
            buffer_res[i]=0;
        for (int k = 0; k < buff.length; k=k+lvlech) {
            //System.out.println(buffer_reconstruit.get(k));
            buffer_res[k] = (short) Math.round(buffer_reconstruit.get(k / lvlech));
        }

        /*short somme;
        for(int i = 0 ; i < buff.length ; i++){
            somme=0;
            for(int j = i ; j < i+ 3*lvlech+1 ; j++){
                if(j < buff.length){
                    somme+=buffer_res[j];
                }else {
                    if (buff.length - j > 0)
                        somme += buffer_res[buff.length - j];
                }

            }
            buffer_res[i]= (short) (somme/(lvlech + 1));
        }*/
        //System.out.println("bdlra "+buffer_res.length);
        return buffer_res;
    }

    public static List<Double> decomposition(List<Double> buffer, double HI_D [], double LO_D[]) {
        //maybe revoir les classes mais pour l'instant ...

        List<Double> tmp_lo = convolve(buffer,LO_D);
        List<Double> tmp_hi = convolve(buffer,HI_D);
        int size = tmp_lo.size();
        //double result [] = new double[size];
        List<Double> result = new ArrayList<Double>(size);

        for (int i = 0 ; i<size ; i++){
            result.add((double) 0);
        }

        for (int i = 1; i < size/2; i++) {
            //result[i-1] = tmp_lo[(i*2)-1]; //sous-echantillonnage low
            //result[i+(size/2)-1] = tmp_hi[(i*2)-1]; //high
            result.set(i-1, tmp_lo.get((i*2)-1));
            result.set(i+(size/2)-1, tmp_hi.get((i*2)-1));
        }

        return result;
    }

    static List<Double> reconstruction(List<Double> buffer_low, List<Double> buffer_high, double HI_D [], double LO_D[], int current_size) {
        //double [] buffer_low_up = new double[buff_low.size() * 2];
        //double [] buffer_high_up =  new double[buff_high.size() * 2];
        List<Double> buffer_low_up = new ArrayList<Double>();
        List<Double> buffer_high_up =  new ArrayList<Double>();

        /*double buffer_high[] = new double[buff_high.size()];
        double buffer_low[] = new double[buff_low.size()];
        for (int k = 0; k < buff_high.size(); k++) {
            buffer_high[k] = buff_high.get(k);
            buffer_low[k] = buff_low.get(k);
        }*/

        /*System.out.println("high");
        for (int i= 0 ; i < buffer_high.length ; i++){
        	System.out.print(buffer_high[i] + " ");
        }
        System.out.println("\nlow");
        for (int i= 0 ; i < buffer_low.length ; i++){
        	System.out.print(buffer_low[i] + " ");
        }*/


        //sur echantillonnage
        //List<Integer> list = new ArrayList<Integer>(Collections.nCopies(buffer_low_up.size(), 0.0));
        for (int i= 0; i< buffer_low_up.size(); i++) {
            buffer_high_up.add(0.0);
            buffer_low_up.add(0.0);
        }
        for (int i = buffer_low_up.size() ; i < 2*buffer_low.size() ; i++) {
            buffer_high_up.add(0.0);
            buffer_low_up.add(0.0);
        }
        for (int i = 0; i < buffer_low.size(); i++){
            buffer_high_up.set(i*2, buffer_high.get(i));
            buffer_low_up.set(i*2, buffer_low.get(i));
        }
        /*for (int i = 0; i < buffer_low_up.length; i++){
            buffer_high_up[i] = 0.0;
            buffer_low_up[i] = 0.0;
        }
        for (int i = 0; i < buffer_low.length; i++){
            buffer_high_up[i*2] = buffer_high[i];
            buffer_low_up[i*2] = buffer_low[i];
        }*/


        /*System.out.println("high");
        for (int i= 0 ; i < buffer_high_up.length ; i++){
        	System.out.print(buffer_high_up[i] + " ");
        }
        System.out.println("\nlow");
        for (int i= 0 ; i < buffer_low_up.length ; i++){
        	System.out.print(buffer_low_up[i] + " ");
        }*/


        List<Double> tmp_lo = convolve(buffer_low_up,LO_R);
        List<Double> tmp_hi = convolve(buffer_high_up,HI_R);


        /*System.out.println("high");
        for (int i= 0 ; i < tmp_hi.length ; i++){
        	System.out.print(tmp_hi[i] + " ");
        }
        System.out.println("\nlow");
        for (int i= 0 ; i < tmp_lo.length ; i++){
        	System.out.print(tmp_lo[i] + " ");
        }*/


        //le nombre de valeur a supprimer avant et apres la convolution + somme
        int nb_val = Math.round(((tmp_lo.size()-1)-current_size)/2);
        //System.out.println("nb_val : "+nb_val);
        //double result [] = new double [tmp_lo.length - ((nb_val)*2)];
        List<Double> result = new ArrayList<Double>(tmp_lo.size() - ((nb_val)*2));
        //System.out.println("taille result : "+result.size());
        //for (int i = 0 ; i < tmp_hi.length ; i++)
        //	System.out.println("somme : " + (tmp_lo[i] + tmp_hi[i]));

        for (int i = 0; i < tmp_lo.size() - ((nb_val)*2); i++) {
            //result [i] = tmp_lo[i+nb_val+1] + tmp_hi[i+nb_val+1];
            result.add( tmp_lo.get(i+nb_val+1) + tmp_hi.get(i+nb_val+1));
            //System.out.println("la valeur de result vaut :" + result.get(i));
        }

        return result;
    }


    /*
    function who make convolution between two array of short
    */
    static List<Double> convolve(List<Double> buffer, double[] filter) {
        //double result [] = new double[buffer.length + filter.length -1];
        List<Double> result = new ArrayList<>();

        //List<Integer> list = new ArrayList<Integer>(Collections.nCopies(buffer.size() + filter.length -1, 0.0));
        for (int i= 0; i< buffer.size() + filter.length -1; i++) {
            result.add(0.0);
        }

        for (int n=0; n< buffer.size(); n++) {
            int i=n;
            for (int m=0; m< filter.length; m++) {
                //result[i] += buffer[n] * filter[m];
                double d = result.get(i);
                double d2 = buffer.get(n);
                result.set(i, d+(d2 * filter[m]));
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
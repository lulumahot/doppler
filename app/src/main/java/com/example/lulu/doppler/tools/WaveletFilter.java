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
        //List<Double> buffer_low_final = new ArrayList<Double>();

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

            tab_conv_High_lvl.get(j).add(0, (double) buffer_high.size() + 1);
            for (double k : buffer_high) {
                tab_conv_High_lvl.get(j).add(k);
            }
            //mï¿½j size pour calcul reconstruction plus tard
            current_size = buffer_low.size();
            buffer = buffer_low;
        }

        //reconstruction
        List<Double> buffer_reconstruit = new ArrayList<Double>();
        List<Double> buffer_high = new ArrayList<Double>();
        List<Double> buffer_low = new ArrayList<Double>();
        List<Double> a = new ArrayList<>();
        for (int j = level-1; j >= 0; j--){
            buffer_high.clear();
            buffer_low.clear();
            a.clear();
            if(j==level-1) a=buffer; else a=buffer_reconstruit;
            for (int k = 1; k < tab_conv_High_lvl.get(j).get(0); k++) {
                buffer_high.add(tab_conv_High_lvl.get(j).get(k));
                buffer_low.add(a.get(k-1));
            }
    //seuillage
            double seuil = Math.sqrt(2*Math.log((double)buffer_high.size()));
            System.out.println("seuil = " + seuil);
            //y = mean(bufferH);
            double y = 0.0;
            for (double i : buffer_high){
                y += i;
            }
            y /= buffer_high.size();
            System.out.println("y = " + y);
            //ecart = (1/length(bufferH))*sum(((bufferH)-mean(bufferH)).^2);
            double ecart = 0.0;
            for (double  i : buffer_high){
                ecart += Math.pow((i - y),2);
            }
            ecart /= buffer_high.size();
            System.out.println("ecart = " + ecart);
            //bufferH = (bufferH - y)/sqrt(ecart);
            for (int i = 0; i < buffer_high.size(); i++){
                double current_val_normalize = (buffer_high.get(i) - y)/Math.sqrt(ecart);
                //System.out.println(current_val_normalize);
                if (Math.abs(current_val_normalize) >= seuil){
                    //(current_val_normalize - Math.signum(current_val_normalize)*seuil)
                    //-> nouvelle valeur, et à dénormaliser
                    buffer_high.set(i, (current_val_normalize - Math.signum(current_val_normalize)*seuil)*Math.sqrt(ecart) + y);
                }
                else
                {
                    buffer_high.set(i, y);
                }
            }
            System.out.println("\nordre : "+(j+1));
            buffer_reconstruit = reconstruction(buffer_low,buffer_high,HI_R, LO_R, tab_size[j]);
        }

        short buffer_res[] = new short[buff.length];
        for(int i = 0 ; i < buff.length ; i++)
            buffer_res[i]=0;
        for (int k = 0; k < buff.length; k=k+lvlech) {
            buffer_res[k] = (short) Math.round(buffer_reconstruit.get(k / lvlech));
        }
        return buffer_res;
    }

    public static List<Double> decomposition(List<Double> buffer, double HI_D [], double LO_D[]) {
        //maybe revoir les classes mais pour l'instant ...

        List<Double> tmp_lo = convolve(buffer,LO_D);
        List<Double> tmp_hi = convolve(buffer,HI_D);
        int size = tmp_lo.size();
        List<Double> result = new ArrayList<Double>(size);

        for (int i = 0 ; i<size ; i++){
            result.add((double) 0);
        }

        for (int i = 1; i < size/2; i++) {
            result.set(i-1, tmp_lo.get((i*2)-1));
            result.set(i+(size/2)-1, tmp_hi.get((i*2)-1));
        }

        return result;
    }

    static List<Double> reconstruction(List<Double> buffer_low, List<Double> buffer_high, double HI_R [], double LO_R[], int current_size) {
        List<Double> buffer_low_up = new ArrayList<Double>();
        List<Double> buffer_high_up =  new ArrayList<Double>();

        //sur echantillonnage
        //List<Integer> list = new ArrayList<Integer>(Collections.nCopies(buffer_low_up.size(), 0.0));
        for (int i = 0 ; i < 2*buffer_low.size() ; i++) {
            buffer_high_up.add(0.0);
            buffer_low_up.add(0.0);
        }
        for (int i = 0; i < buffer_low.size(); i++){
            buffer_high_up.set(i*2, buffer_high.get(i));
            buffer_low_up.set(i*2, buffer_low.get(i));
        }

        List<Double> tmp_lo = convolve(buffer_low_up,LO_R);
        List<Double> tmp_hi = convolve(buffer_high_up, HI_R);

        //le nombre de valeur a supprimer avant et apres la convolution + somme
        //int nb_val = Math.round(((tmp_lo.size()-1)-current_size)/2);
        int nb_val = ((tmp_lo.size()-1)-current_size)/2;
        List<Double> result = new ArrayList<Double>(tmp_lo.size() - ((nb_val)*2) - 1);

        System.out.println("size conv = " + tmp_lo.size() + "; nb_val = " + nb_val);

        for (int i = 0; i < tmp_lo.size() - ((nb_val)*2) - 1; i++) {
            result.add( tmp_lo.get(i+nb_val) + tmp_hi.get(i+nb_val));
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
            for (double m : filter) {
                //result[i] += buffer[n] * filter[m];
                result.set(i, result.get(i)+(buffer.get(n) * m));
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
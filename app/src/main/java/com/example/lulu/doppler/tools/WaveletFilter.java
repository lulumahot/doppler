package com.example.lulu.doppler.tools;

public class WaveletFilter {

    // Daubechies 8 filters :
    // hight-pass filter
    static double HI_D[] = {-0.0544,    0.3129,   -0.6756,    0.5854,    0.0158,   -0.2840,   -0.0005,
            0.1287,    0.0174,   -0.0441,   -0.0140,    0.0087,     0.0049,   -0.0004,   -0.0007,
            -0.0001};
    // low-pass filter
    static double LO_D[] = {-0.0001,    0.0007,   -0.0004,   -0.0049,    0.0087,    0.0140,   -0.0441,
            -0.0174,    0.1287,    0.0005,   -0.2840,   -0.0158, 0.5854,    0.6756,    0.3129,
            0.0544};
    static int level = 4;

    public static void filter(short[] buffer) {
        // TODO : Implement filter treatment
        System.out.println("size LO_D : %d \n" + LO_D.length);
        //double LO_D_reverse[] = reverseTab(LO_D);
        //double HI_D_reverse[] = reverseTab(HI_D);

        // nbEch dans le buffer => = buffer.length
        int nbEch = buffer.length;
        //double nb_fragments =  nbEch / LO_D.length;

        short new_buffer[] = new short[buffer.length];

        short[] buffer_dec = decomposition(buffer, HI_D, LO_D);
        short[] buffer_rec = reconstruction(buffer_dec, HI_D, LO_D);


       /* // test convolve
        short x[] = {1,2,-1};
        short h[] = {4,1,2,5};
        double y[] = convolve(x, h);
        for (int i=0; i< y.length; i++) {
            System.out.print(y[i] + " ");
        }
        */
    }

    static short[] decomposition(short[] buffer, double HI_D [], double LO_D[]) {
        for (int i=0; i<level; i++) {
            // TODO : decomposition
        }
        return null;
    }

    static short[] reconstruction(short[] buffer, double HI_D [], double LO_D[]) {
        for (int i=0; i<level; i++) {
            // TODO : reconstruction
        }
        return null;
    }


    /*
    function who make convolution between two array of short
    */
    static double[] convolve(short[] buffer, short[] filter) {
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
    static short[] getPortion(short[] buffer, int startId, int size) {
        short tab[] = new short[size];

        for (int i=0; i<size; i++) {
            tab[i] = buffer[startId +i];
        }
        return tab;
    }

    /*
     function product between two array (of same size)
     */
    static double[] productElemByElem(short[] buffer, double[] filter) {
        double result [] = new double[buffer.length + filter.length -1];
        for (int n=0; n< buffer.length; n++) {
            result[n] = buffer[n] * filter[n];
        }
        return result;
    }

}

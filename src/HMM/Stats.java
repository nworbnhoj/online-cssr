/*
 * Copyright 2007, 2011 John A Brown
 * www.nhoj.info           nworbnhoj
 *
 * This file is part of oCSSR.
 *
 * oCSSR is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * oCSSR is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * oCSSR.  If not, see <http://www.gnu.org/licenses/>.
 */

package HMM;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author nworbnhoj
 */
public class Stats {
    
    private static final int ITMAX = 100;
    private static final double EPS = 3.0e-7;
    private static final double FPMIN = 1.0e-30;
    
    
    
    /**
     * KSsig returns an estimate of the significant difference between the
     * Morph of this State and the supplied Morph based on the
     * Kolmogorov-Smirnov test.
     * The Kolmogorov-Smirnov test considers the maximum difference between
     * the cumulative probability distribution of two samples.
     * note; d==0 is different to "Numerical recipies in C"
     */
    public static double KSsig(List<Integer> data1, List<Integer> data2){
              
        double d = 0, prob = 0;
        int n1 = data1.size();
        int n2 = data2.size();
        
        int j1=1, j2=1;
        double d1, d2, dt, en1, en2, en, fn1=0.0, fn2=0.0, sen, qest;
        
        Collections.sort(data1);
        Collections.sort(data2);
        en1 = n1;
        en2 = n2;
        d=0.0;
        
        while (j1<=n1 && j2<=n2){
            if((d1=data1.get(j1-1)) <= (d2=data2.get(j2-1))) fn1=j1++/en1;
            if(d2 <= d1) fn2=j2++/en2;
            if((dt=Math.abs(fn2-fn1)) > d) d=dt;
        }
        
        en = (en1*en2/(en1 + en2));
        sen=Math.sqrt(en);
        qest = (sen+0.12+0.11/sen) * d;
        
        if (d==0) return 1;
        if (en >= 4){
            return qest;
        } else { // refine the estimate
              return probks(qest);
        }
    }
    
        
    
    
    /** refine the estimate of KS significance as "Numerical recipies in C" pg 626
     * Note that is does not converge for alam = 0
     */
    private static double probks(double alam) {
        int j;
        double a2, fac=2.0, sum=0.0, term, termbf = 0.0;
        double EPS1=0.001 ;        //KS numbers
        double EPS2=1.0e-8 ;       //KS numbers
        
        a2 = -2.0 * alam * alam;
        
        for (j=1; j <= 100; j++) {
            term = fac* Math.exp(a2*j*j);
            sum += term;
            if (Math.abs(term) <= EPS1*termbf || Math.abs(term) <= EPS2*sum)
                return sum;
            fac = -fac;
            termbf = Math.abs(term);
        }
        return 1.0;
    }
    
    
    
    
    
    /**chstwo ---- calculates chi-square for two distributions
     * After Numerical Recipes in C
     * Given the arrays bins1[0...n1] and bins2[1...n2], containing two sets of binned
     * data, and given the number of constraints knstrn (normally 1 or 0), this routine
     * returns the significance probability.  A small value of prob indicates a significant
     * difference between the distributions bins1 and bins2.
     * Note that there are modifications to the routines provided in Numerical Recipies in C
     * - the data is provided as Integers rather than float.
     * - it is assumed that knstr <= 0
     * - it is assumed that n1 = n1 = nbins
     * - it is assumed that df is ok
     */
    public static double chstwo(List<Integer> bins1, List<Integer> bins2, int knstrn) {
        int n1 = 0, n2 = 0, nbins = 0;
        for (int value : bins1) {n1 += value;}
        for (int value : bins2) {n2 += value;}
        if (bins1.size() != bins2.size()) {} //error
        nbins = bins1.size();
        
        double df = 0.0, chsq = 0.0, prob = 0.0;
        
        int j;
        double temp;
        
        df = nbins - knstrn;
        double ratio1;
        double ratio2;
        
        if(n1 > 0) ratio1 = (double) Math.sqrt(((double) n2)/((double)n1));
        else ratio1 = 0.0;
        
        if(n2 > 0) ratio2 = (double) Math.sqrt(((double) n1)/((double)n2));
        else ratio2 = 0.0;
        
        for (j = 0; j < nbins; j++) {
            if(bins1.get(j) == 0.0 && bins2.get(j) == 0.0)
                --(df);		//No data means one less degree of freedom
            else {
                temp = ratio1*bins1.get(j) - ratio2*bins2.get(j);
                chsq += temp*temp/(bins1.get(j)+bins2.get(j));
            }
        }
        prob = gammq(0.5*(df), 0.5*(chsq));
        
        return prob;
    }
    
    

    
    /**gammq  ---- returns the incomplete gamma function Q(a,x) = 1 - P(a,x).
     * After Numerical Recipes in C
     **/
    
    private static double gammq(double a, double x) {
        double gamser, gammcf;
        
        //cout <<"a " << a<< " x "<<x<<endl;
        if(x < 0.0 || a <= 0.0){
            //  nerror("Invalid arguments in routine gammq");
        }
        if( x < (a + 1.0)){	//use the series representation
            gamser = gser(a, x);
            return 1.0 - gamser;		//and take its complement
        } else {
            gammcf = gcf(a, x);  //use the continued fraction representation
            return gammcf;
        }
    }
    
    
    /** gser --- Returns the incomplete gamma function P(a,x)
     * After Numerical Recipes in C
     * evaluated by its series representation.
     */
    private static double gser(double a, double x) {
        double gamser = 0.0;
        int n;
        double sum, del, ap;
        double gln = gammln(a);
        
        if(x <= 0.0) {
            if(x < 0.0){
                //  nerror("x less than zero in series expansion gamma function");
            }
            return gamser = 0.0;
        } else {
            ap = a;
            del = sum = 1.0/a;
            for(n = 1; n <= ITMAX; n++) {
                ++ap;
                del *= x/ap;
                sum += del;
                if(Math.abs(del) < (Math.abs(sum)*EPS)) {
                    return gamser = sum*Math.exp(-x + (a*Math.log(x)) - (gln));
                }
            }
            // nerror("a is too large, ITMAX is too small, in series expansion gamma function");
            return 0.0;
        }
    }
    
    
    /**gcf--- Returns the incomplete gamma function Q(a,x), evaulated by its
     * After Numerical Recipes in C
     *continued fraction representation as gammcf.  Also returns natural log
     *of gamma as gln
     */
    
    private static double gcf(double a, double x) {
        double gammcf;
        double gln;
        int i;
        double an, b, c, d, del, h;
        
        gln = gammln(a);
        b = x + 1.0 - a;
        c = 1.0/FPMIN;
        d = 1.0/b;
        h = d;
        
        for(i = 1; i <= ITMAX; i++)     //iterate to convergence
        {
            an = -i*(i - a);
            b += 2.0;      //Set up for evaluating continued
            d = an*d + b;  //fraction by modified Lentz's method with b_0 = 0.
            if(Math.abs(d) < FPMIN)
                d = FPMIN;
            c = b + an/c;
            if(Math.abs(c) < FPMIN)
                c = FPMIN;
            d = 1.0/d;
            del = d*c;
            h *= del;
            
            if(Math.abs(del - 1.0) < EPS)
                break;
        }
        if (i > ITMAX){
            // nerror("a too large, ITMAX too small in continued fraction gamma function");
        }
        return gammcf = Math.exp(-x + a*Math.log(x) - (gln))*h;   //Put factors in front
        
    }
    
    
    
    /**gammln --- returns natural log of gamma(xx), for xx> 0
     * After Numerical Recipes in C
     **/
    private static double gammln(double xx) {
        double x, y, tmp, ser;
        double[] cof = new double[6];
        cof[0] = 76.18009172947146;
        cof[1] = -86.50532032941677;
        cof[2] = 24.01409824083091;
        cof[3] = -1.231739572450155;
        cof[4] = 0.1208650973866179e-2;
        cof[5] = -0.5395239384953e-5;
        
        int j;
        
        y = x = xx;
        tmp = x + 5.5;
        tmp -= (x + 0.5)*Math.log(tmp);
        ser = 1.000000000190015;
        
        for(j = 0; j <= 5; j++)
            ser += cof[j]/++y;
        return -tmp + Math.log(2.5066282746310005*ser/x);
    }
    
    
    
    
}